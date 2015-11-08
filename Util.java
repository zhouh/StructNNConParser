
/*
* 	@Author:  Danqi Chen
* 	@Email:  danqi@cs.stanford.edu
*	@Created:  2014-08-25
* 	@Last Modified:  2014-10-05
*/

package nncon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.util.CoreMap;


/**
 *
 *  Some utility functions
 *
 *  @author Danqi Chen
 *  @author Jon Gauthier
 */

class Util {

  private Util() {} // static methods

  private static Random random;

  // return strings sorted by frequency, and filter out those with freq. less than cutOff.

  /**
   * Build a dictionary of words collected from a corpus.
   * <p>
   * Filters out words with a frequency below the given {@code cutOff}.
   *
   * @return Words sorted by decreasing frequency, filtered to remove
   *         any words with a frequency below {@code cutOff}
   */
  public static List<String> generateDict(List<String> str, int cutOff)
  {
    Counter<String> freq = new IntCounter<>();
    for (String aStr : str)
      freq.incrementCount(aStr);

    List<String> keys = Counters.toSortedList(freq, false);
    List<String> dict = new ArrayList<>();
    for (String word : keys) {
      if (freq.getCount(word) >= cutOff)
        dict.add(word);
    }
    return dict;
  }

  public static List<String> generateDict(List<String> str)
  {
    return generateDict(str, 1);
  }

  /**
   * @return Shared random generator used in this package
   */
  static Random getRandom() {
    if (random != null)
      return random;
    else
      return getRandom(System.currentTimeMillis());
  }

  /**
   * Set up shared random generator to use the given seed.
   *
   * @return Shared random generator object
   */
  static Random getRandom(long seed) {
    random = new Random(seed);
    System.err.printf("Random generator initialized with seed %d%n", seed);

    return random;
  }

  public static <T> List<T> getRandomSubList(List<T> input, int subsetSize)
  {
    int inputSize = input.size();
    if (subsetSize > inputSize)
      subsetSize = inputSize;

    Random random = getRandom();
    for (int i = 0; i < subsetSize; i++)
    {
      int indexToSwap = i + random.nextInt(inputSize - i);
      T temp = input.get(i);
      input.set(i, input.get(indexToSwap));
      input.set(indexToSwap, temp);
    }
    return input.subList(0, subsetSize);
  }
  
  /**
	 * get tokens from a String line
	 * 
	 * @param line the line of input corpus
	 * @return the list of string split by space in the line
	 * */
	public static List<String> getTokens(String line) {
		
		List<String> rtn=new ArrayList<String>();    //返回值
		String[] str=line.split("\\s{1,}");
		
		for(int i=0;i<str.length;i++)  {
			if(str[i].equals(" ")||str[i].equals("　")) continue;
			rtn.add(str[i]);
		}
		
		return rtn;
	}
	
  // TODO replace with GrammaticalStructure#readCoNLLGrammaticalStructureCollection
  public static void loadFile(String inFile, List<CoreMap> sents, List<CFGTree> trees, boolean labeled)
  {
    CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);

    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(inFile);

      for (String line : IOUtils.getLineIterable(reader, false)) {
    	  
    	// empty line skip
    	if(line.matches("\\S*"))
				continue;
    	
    	CoreMap sentence = new CoreLabel();
    	List<CoreLabel> sentenceTokens = new ArrayList<>();
        String[] splits = line.split("\\s{1,}");
        
        //get tree
        CFGTree tree=new CFGTree();
        tree.CTBReadNote(getTokens(line));
        
        //get sentence
        List<CFGTreeNode> wordNotes = tree.getSentFromTree();
        for(CFGTreeNode node : wordNotes){
        	CoreLabel token = tf.makeToken(node.word, 0, 0);
            token.setTag(node.constituent);
            sentenceTokens.add(token);
        }
        
        trees.add(tree);
        sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
        sents.add(sentence);
        
      }    } catch (IOException e) {
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

  public static void loadFile(String inFile, List<CoreMap> sents, List<CFGTree> trees)
  {
    loadFile(inFile, sents, trees, true);
  }

  public static void writeConllFile(String outFile, List<CoreMap> sentences, List<DependencyTree> trees)
  {
    try
    {
      PrintWriter output = IOUtils.getPrintWriter(outFile);
      for (CoreMap sentence : sentences)
      {
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

        for (int j = 1, size = tokens.size(); j <= size; ++j)
        {
          CoreLabel token = tokens.get(j - 1);
          output.printf("%d\t%s\t_\t%s\t%s\t_\t%d\t%s\t_\t_%n",
                  j, token.word(), token.tag(), token.tag(),
                  token.get(CoreAnnotations.CoNLLDepParentIndexAnnotation.class),
                  token.get(CoreAnnotations.CoNLLDepTypeAnnotation.class));
        }
        output.println();
      }
      output.close();
    }
    catch (Exception e) {
      throw new RuntimeIOException(e);
    }
  }

  public static void printTreeStats(String str, List<DependencyTree> trees)
  {
    System.err.println(Config.SEPARATOR + " " + str);
    System.err.println("#Trees: " + trees.size());
    int nonTrees = 0;
    int nonProjective = 0;
    for (DependencyTree tree : trees) {
      if (!tree.isTree())
        ++nonTrees;
      else if (!tree.isProjective())
        ++nonProjective;
    }
    System.err.println(nonTrees + " tree(s) are illegal.");
    System.err.println(nonProjective + " tree(s) are legal but not projective.");
  }

  public static void printTreeStats(List<DependencyTree> trees)
  {
    printTreeStats("", trees);
  }

}
