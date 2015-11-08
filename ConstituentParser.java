package nncon;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Random;


import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.Timing;

/**
 * This class defines a transition-based dependency parser which makes
 * use of a classifier powered by a neural network. The neural network
 * accepts distributed representation inputs: dense, continuous
 * representations of words, their part of speech tags, and the labels
 * which connect words in a partial dependency parse.
 *
 * <p>
 * This is an implementation of the method described in
 *
 * <blockquote>
 *   Danqi Chen and Christopher Manning. A Fast and Accurate Dependency
 *   Parser Using Neural Networks. In EMNLP 2014.
 * </blockquote>
 *
 * <p>
 * New models can be trained from the command line; see {@link #main}
 * for details on training options. This parser will also output
 * CoNLL-X format predictions; again see {@link #main} for available
 * options.
 *
 * <p>
 * This parser can also be used programmatically. The easiest way to
 * prepare the parser with a pre-trained model is to call
 * {@link #loadFromModelFile(String)}. Then call
 * {@link #predict(edu.stanford.nlp.util.CoreMap)} on the returned
 * parser instance in order to get new parses.
 *
 * @author Hao Zhou
 */
public class ConstituentParser {
  public static final String DEFAULT_MODEL = "edu/stanford/nlp/models/parser/nndep/PTB_Stanford_params.txt.gz";

  /**
   * Words, parts of speech, and dependency relation labels which were
   * observed in our corpus / stored in the model
   *
   * @see #genDictionaries(java.util.List, java.util.List)
   */
  private List<String> knownWords, knownPos, knownLabels, labelDict;

  /**
   * Mapping from word / POS / dependency relation label to integer ID
   */
  private Map<String, Integer> wordIDs, posIDs, labelIDs;

  private List<Integer> preComputed;

  /**
   * Given a particular parser configuration, this classifier will
   * predict the best transition to make next.
   *
   * The {@link edu.stanford.nlp.parser.nndep.Classifier} class
   * handles both training and inference.
   */
  private Classifier classifier;

  private ParsingSystem system;

  private Map<String, Integer> embedID;
  private double[][] embeddings;

  private final Config config;

  /**
   * Language used to generate
   * {@link edu.stanford.nlp.trees.GrammaticalRelation} instances.
   */
  private final GrammaticalRelation.Language language;

  ConstituentParser() {
    this(new Properties());
  }

  public ConstituentParser(Properties properties) {
    config = new Config(properties);

    // Convert Languages.Language instance to
    // GrammaticalLanguage.Language
    switch (config.language) {
      case English:
        language = GrammaticalRelation.Language.English;
        break;
      case Chinese:
        language = GrammaticalRelation.Language.Chinese;
        break;
      default:
        language = GrammaticalRelation.Language.Any;
        break;
    }
  }

  /**
   * Get an integer ID for the given word. This ID can be used to index
   * into the embeddings {@link #embeddings}.
   *
   * @return An ID for the given word, or an ID referring to a generic
   *         "unknown" word if the word is unknown
   */
  public int getWordID(String s) {
      return wordIDs.containsKey(s) ? wordIDs.get(s) : wordIDs.get(Config.UNKNOWN);
  }

  public int getPosID(String s) {
      return posIDs.containsKey(s) ? posIDs.get(s) : posIDs.get(Config.UNKNOWN);
  }

  public int getLabelID(String s) {
    return labelIDs.get(s);
  }

  public List<Integer> getFeatures(ConParseTSSState c) {
	  
	  int[] feature = getFeatureArray(c);
	  List<Integer> featureList = new ArrayList<Integer>(feature.length);
	  for(int a : feature)
		  featureList.add(a);
    return featureList;
  }

  private int[] getFeatureArray(ConParseTSSState  state) {
    int[] feature = new int[config.numTokens];  //

    int offect = 0;
    
    // get the first 4 element of the top stack 
    ConParseTSSStateNode S0 = state.node;
    ConParseTSSStateNode S1 = state.stackSize>=2?state.stackPtr.node:null;
    ConParseTSSStateNode S2 = state.stackSize>=3?state.stackPtr.stackPtr.node:null;
    ConParseTSSStateNode S3 = state.stackSize>=4?state.stackPtr.stackPtr.stackPtr.node:null;
    ConParseTSSStateNode S0u=null;
    ConParseTSSStateNode S0r=null;
    ConParseTSSStateNode S0l=null;
    ConParseTSSStateNode S1u=null;
    ConParseTSSStateNode S1r=null;
    ConParseTSSStateNode S1l=null;
    
    int N0=100;
    int N1=100;
    int N2=100;
    int N3=100;
    
    int idx = state.currentWord;     //the index of the first element of the queue
    int sentLen=state.input.sentLen;     //the sentence size
    
    //N0
	if(idx<sentLen)
		N0 = idx;
	else
		N0=-1;

	//N1
	if((idx+1)<sentLen)
		N1 = idx+1;
	else
		N1=-1;

	//N2
	if((idx+2)<sentLen)
		N2 = idx+2;
	else
		N2=-1;
	
	//N3
	if((idx+3)<sentLen)
		N3 = idx+3;
	else
		N3=-1;
    
    //get the left right unary children of stack element
    if(S0!=null&&!S0.beLeaf()) {
    	
    	if(S0.singleChild()) {
    		S0u=S0.left_child;
    	}
    	else {
    		S0l=S0.left_child;
    		S0r=S0.right_child;
    	}
    	
    }
    
    if(S1!=null&&!S1.beLeaf()) {
    	if(S1.singleChild()) {
    		S1u=S1.left_child;
    	}
    	else {
    		S1l=S1.left_child;
    		S1r=S1.right_child;
    	}
    	
    }
    
    /*
     *   S0w, S0c, S1w, S1c, S2w, S2c, S3w, S3c
     *   N0w, N0t, N1w, N1t, N2w, N2t, N3w, N3t
     *   S0lw, S0lc, S0rw, S0rc, S0uw, S0uc
     *   S1lw, S1lc, S1rw, S1rc, S1uw, S1uc
     *   S0llw, S0llc, S0lrw, S0lrc, S0luw, S0luc
     *   S0rlw, S0rlc, S0rrw, S0rrc, S0ruw, S0ruc
     *   S0ulw, S0ulc, S0urw, S0urc, S0uuw, S0uuc
     *   S1llw, S1llc, S1lrw, S1lrc, S1luw, S1luc
     *   S1rlw, S1rlc, S1rrw, S1rrc, S1ruw, S1ruc
     */

    String str;
    
    //S0w 0
    str = S0!=null ? state.input.getWord(S0.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S0c 1
    str = S0!=null ? S0.constituent : Config.NULL;
    if(S0!=null)
    	if(S0.beLeaf())
    		feature[offect++] = getPosID(str);
    	else {
    		feature[offect++] = getLabelID(str);
    		
    	}
    else feature[offect++] = getLabelID(str);
		
   //S1w 2
    str = S1!=null ? state.input.getWord(S1.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S1c 3
    str = S1!=null ? S1.constituent : Config.NULL;
    if(S1!=null)
    	if(S1.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S2w 4
    str = S2!=null ? state.input.getWord(S2.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S2c 5
    str = S2!=null ? S2.constituent : Config.NULL;
    if(S2!=null)
    	if(S2.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S3w 6
    str = S3!=null ? state.input.getWord(S3.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S3c 7
    str = S3!=null ? S3.constituent : Config.NULL;
    if(S3!=null)
    	if(S3.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S0t 8
    str = S0!=null ? state.input.getTag(S0.lexical_head) : Config.NULL;
    feature[offect++] = getPosID(str);
    
    //S1t 9
    str = S1!=null ? state.input.getTag(S1.lexical_head) : Config.NULL;
    feature[offect++] = getPosID(str);
    
    //S2t 10
    str = S2!=null ? state.input.getTag(S2.lexical_head) : Config.NULL;
    feature[offect++] = getPosID(str);
    
    //S3t 11
    str = S3!=null ? state.input.getTag(S3.lexical_head) : Config.NULL;
    feature[offect++] = getPosID(str);
    
  //S0lw 12
    str = S0l!=null ? state.input.getWord(S0l.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S0lc 13
    str = S0l!=null ? S0l.constituent : Config.NULL;
    if(S0l!=null)
    	if(S0l.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S0rw 14
    str = S0r!=null ? state.input.getWord(S0r.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S0rc 15
    str = S0r!=null ? S0r.constituent : Config.NULL;
    if(S0r!=null)
    	if(S0r.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S0uw 16
    str = S0u!=null ? state.input.getWord(S0u.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S0uc 17
    str = S0u!=null ? S0u.constituent : Config.NULL;
    if(S0u!=null)
    	if(S0u.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S1lw 18
    str = S1l!=null ? state.input.getWord(S1l.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S1lc 19
    str = S1l!=null ? S1l.constituent : Config.NULL;
    if(S1l!=null)
    	if(S1l.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S1rw 20
    str = S1r!=null ? state.input.getWord(S1r.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S1rc 21
    str = S1r!=null ? S1r.constituent : Config.NULL;
    if(S1r!=null)
    	if(S1r.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    //S1uw 22
    str = S1u!=null ? state.input.getWord(S1u.lexical_head) : Config.NULL;
    feature[offect++] = getWordID(str);
    
    //S1uc 23
    str = S1u!=null ? S1u.constituent : Config.NULL;
    if(S1u!=null)
    	if(S1u.beLeaf())
    		feature[offect++] = getPosID(str);
    	else 
    		feature[offect++] = getLabelID(str);
    else feature[offect++] = getLabelID(str);
    
    /*
     *   features from queue
     */
    //N0w 24
    str = N0!=-1 ? state.input.getWord(N0) : Config.NULL;
    feature[offect++] = getWordID(str);
    
   //N0t 25
    str = N0!=-1 ? state.input.getTag(N0) : Config.NULL;
    feature[offect++] = getPosID(str);

    //N1w 26
    str = N1!=-1 ? state.input.getWord(N1) : Config.NULL;
    feature[offect++] = getWordID(str);
    
   //N1t 27
    str = N1!=-1 ? state.input.getTag(N1) : Config.NULL;
    feature[offect++] = getPosID(str);
    
    //N2w 28
    str = N2!=-1 ? state.input.getWord(N2) : Config.NULL;
    feature[offect++] = getWordID(str);
    
   //N2t 29
    str = N2!=-1 ? state.input.getTag(N2) : Config.NULL;
    feature[offect++] = getPosID(str);
    
    //N3w 30
    str = N3!=-1 ? state.input.getWord(N3) : Config.NULL;
    feature[offect++] = getWordID(str);
    
   //N3t 31
    str = N3!=-1 ? state.input.getTag(N3) : Config.NULL;
    feature[offect++] = getPosID(str);
    
    
    return feature;
  }

  public Dataset genTrainExamples(List<CoreMap> sents, List<CFGTree> trees) {
    Dataset ret = new Dataset(config.numTokens, system.transitions.size());

    Counter<Integer> tokPosCount = new IntCounter<>();
    System.err.println(Config.SEPARATOR);
    System.err.println("Generate training examples...");

    /*
     *  construct a hash map of constituent label for generate oracle label
     */
    HashMap<String, Integer> conLabelMap = new HashMap<String, Integer>();
    int mapIndex = 0;
    for(String s : system.labels)
    	conLabelMap.put(s, mapIndex++);
    
    for (int i = 0; i < sents.size(); ++i) {
    	

      if (i > 0) {
        if (i % 1000 == 0)
          System.err.print(i + " ");
        if (i % 10000 == 0 || i == sents.size() - 1)
          System.err.println();
      }
      
      ConParseTSSState c = system.initialConfiguration(sents.get(i));
      
      while (!system.isTerminal(c)) {
    	  Pair<Integer, String> oracle = system.getOracleActAndConlabel(c, trees.get(i));
    	  List<Integer> feature = getFeatures(c);
    	  
    	  int oracleAct = oracle.first;
    	  int oracleLabel = oracleAct == system.nShift ? -1 : conLabelMap.get(oracle.second);
    	  
    	  List<Integer> actLabel = system.canApplyActsWithoutLabel(c);
    	  List<Integer> labelLabel = oracleAct == system.nShift ? null : system.getValidReduceActs(oracleAct ,c);
    	  actLabel.set(oracleAct, 1);
    	  if(oracleAct != system.nShift)
    		  labelLabel.set(oracleLabel, 1);
    	  
    	  ret.addExample(feature, actLabel, labelLabel);
    	  for (int j = 0; j < feature.size(); ++j)
    		  tokPosCount.incrementCount(feature.get(j) * feature.size() + j);
    	  
    	  c = system.apply(c, oracleAct, oracleLabel);

        }
    }
    
    System.err.println("#Train Examples: " + ret.n);

    preComputed = new ArrayList<>(config.numPreComputed);
    List<Integer> sortedTokens = Counters.toSortedList(tokPosCount, false);

    preComputed = new ArrayList<>(sortedTokens.subList(0, Math.min(config.numPreComputed, sortedTokens.size())));

    return ret;
  }

  /**
   * Generate unique integer IDs for all known words / part-of-speech
   * tags / dependency relation labels.
   *
   * All three of the aforementioned types are assigned IDs from a
   * continuous range of integers; all IDs 0 <= ID < n_w are word IDs,
   * all IDs n_w <= ID < n_w + n_pos are POS tag IDs, and so on.
   */
  private void generateIDs() {
    wordIDs = new HashMap<>();
    posIDs = new HashMap<>();
    labelIDs = new HashMap<>();

    int index = 0;
    for (String word : knownWords)
      wordIDs.put(word, (index++));
    for (String pos : knownPos)
      posIDs.put(pos, (index++));
    for (String label : knownLabels)
      labelIDs.put(label, (index++));
  }

  /**
   * Scan a corpus and store all words, part-of-speech tags, and
   * dependency relation labels observed. Prepare other structures
   * which support word / POS / label lookup at train- / run-time.
   */
  private void genDictionaries(List<CoreMap> sents, List<CFGTree> trees) {
    // Collect all words (!), etc. in lists, tacking on one sentence
    // after the other
    List<String> word = new ArrayList<>();
    List<String> pos = new ArrayList<>();
    List<String> label = new ArrayList<>();

    for (CoreMap sentence : sents) {
      List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

      for (CoreLabel token : tokens) {
        word.add(token.word());
        pos.add(token.tag());
      }
    }

    for (CFGTree tree : trees)
      for (CFGTreeNode node : tree.nodes)
         if(node.is_constituent){
        	 label.add(node.constituent);
        	 label.add(node.constituent+"*");
         }

    // Generate "dictionaries," possibly with frequency cutoff
    knownWords = Util.generateDict(word, config.wordCutOff);
    knownPos = Util.generateDict(pos);
    knownLabels = Util.generateDict(label);

    knownWords.add(0, Config.UNKNOWN);
    knownWords.add(1, Config.NULL);

    knownPos.add(0, Config.UNKNOWN);
    knownPos.add(1, Config.NULL);

    knownLabels.add(0, Config.NULL);
    
    labelDict = new ArrayList<String>();
    for(int i = 0; i < knownLabels.size(); i++)
    	if(knownLabels.get(i).equals(Config.NULL) || knownLabels.get(i).endsWith("*"))
    		continue;
    	else
    		labelDict.add(knownLabels.get(i));
    
    generateIDs();

    System.out.println(Config.SEPARATOR);
    System.out.println("#Word: " + knownWords.size());
    System.out.println("#POS:" + knownPos.size());
    System.out.println("#Label: " + knownLabels.size());
  }

  public void writeModelFile(String modelFile) {
    try {
      double[][] W1 = classifier.getW1();
      double[] b1 = classifier.getb1();
      double[][] W2 = classifier.getW2();
      double[][] E = classifier.getE();
      double[][][] labelLayer = classifier.getLabelLayer();

      Writer output = IOUtils.getPrintWriter(modelFile);

      output.write("dict=" + knownWords.size() + "\n");
      output.write("pos=" + knownPos.size() + "\n");
      output.write("label=" + knownLabels.size() + "\n");
      output.write("embeddingSize=" + E[0].length + "\n");
      output.write("hiddenSize=" + b1.length + "\n");
      output.write("numTokens=" + (W1[0].length / E[0].length) + "\n");
      output.write("preComputed=" + preComputed.size() + "\n");

      int index = 0;

      // First write word / POS / label embeddings
      for (String word : knownWords) {
        output.write(word);
        for (int k = 0; k < E[index].length; ++k)
          output.write(" " + E[index][k]);
        output.write("\n");
        index = index + 1;
      }
      for (String pos : knownPos) {
        output.write(pos);
        for (int k = 0; k < E[index].length; ++k)
          output.write(" " + E[index][k]);
        output.write("\n");
        index = index + 1;
      }
      for (String label : knownLabels) {
        output.write(label);
        for (int k = 0; k < E[index].length; ++k)
          output.write(" " + E[index][k]);
        output.write("\n");
        index = index + 1;
      }

      // Now write classifier weights
      for (int j = 0; j < W1[0].length; ++j)
        for (int i = 0; i < W1.length; ++i) {
          output.write("" + W1[i][j]);
          if (i == W1.length - 1)
            output.write("\n");
          else
            output.write(" ");
        }
      for (int i = 0; i < b1.length; ++i) {
        output.write("" + b1[i]);
        if (i == b1.length - 1)
          output.write("\n");
        else
          output.write(" ");
      }
      for (int j = 0; j < W2[0].length; ++j)
        for (int i = 0; i < W2.length; ++i) {
          output.write("" + W2[i][j]);
          if (i == W2.length - 1)
            output.write("\n");
          else
            output.write(" ");
        }
      
      //write labelLayer matrix
      for(int i = 0; i < labelLayer.length; i++)
    	  for(int j = 0; j < labelLayer[0].length; j++)
    		  for(int k = 0; k < labelLayer[0][0].length; k++){
    			  output.write("" + labelLayer[i][j][k]);
    			  if(k == labelLayer[0][0].length - 1)
    				  output.write("\n");
    			  else
    				  output.write(" ");
    		  }

      // Finish with pre-computation info
      for (int i = 0; i < preComputed.size(); ++i) {
        output.write("" + preComputed.get(i));
        if ((i + 1) % 100 == 0 || i == preComputed.size() - 1)
          output.write("\n");
        else
          output.write(" ");
      }

      output.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  /**
   * Convenience method; see {@link #loadFromModelFile(String, java.util.Properties)}.
   *
   * @see #loadFromModelFile(String, java.util.Properties)
   */
  public static ConstituentParser loadFromModelFile(String modelFile) {
    return loadFromModelFile(modelFile, null);
  }

  /**
   * Load a saved parser model.
   *
   * @param modelFile       Path to serialized model (may be GZipped)
   * @param extraProperties Extra test-time properties not already associated with model (may be null)
   *
   * @return Loaded and initialized (see {@link #initialize(boolean)} model
   */
  public static ConstituentParser loadFromModelFile(String modelFile, Properties extraProperties) {
    ConstituentParser parser = extraProperties == null ? new ConstituentParser() : new ConstituentParser(extraProperties);
    parser.loadModelFile(modelFile, false);
    return parser;
  }

  /** Load a parser model file, printing out some messages about the grammar in the file.
   *
   *  @param modelFile The file (classpath resource, etc.) to load the model from.
   */
  public void loadModelFile(String modelFile) {
    loadModelFile(modelFile, true);
  }

  private void loadModelFile(String modelFile, boolean verbose) {
    Timing t = new Timing();
    try {
      // System.err.println(Config.SEPARATOR);
      System.err.println("Loading depparse model file: " + modelFile + " ... ");
      String s;
      BufferedReader input = IOUtils.readerFromString(modelFile);

      int nDict, nPOS, nLabel;
      int eSize, hSize, nTokens, nPreComputed;
      nDict = nPOS = nLabel = eSize = hSize = nTokens = nPreComputed = 0;

      for (int k = 0; k < 7; ++k) {
        s = input.readLine();
        if (verbose) {
          System.err.println(s);
        }
        int number = Integer.parseInt(s.substring(s.indexOf('=') + 1));
        switch (k) {
          case 0:
            nDict = number;
            break;
          case 1:
            nPOS = number;
            break;
          case 2:
            nLabel = number;
            break;
          case 3:
            eSize = number;
            break;
          case 4:
            hSize = number;
            break;
          case 5:
            nTokens = number;
            break;
          case 6:
            nPreComputed = number;
            break;
          default:
            break;
        }
      }


      knownWords = new ArrayList<String>();
      knownPos = new ArrayList<String>();
      knownLabels = new ArrayList<String>();
      double[][] E = new double[nDict + nPOS + nLabel][eSize];
      String[] splits;
      int index = 0;

      for (int k = 0; k < nDict; ++k) {
        s = input.readLine();
        splits = s.split(" ");
        knownWords.add(splits[0]);
        for (int i = 0; i < eSize; ++i)
          E[index][i] = Double.parseDouble(splits[i + 1]);
        index = index + 1;
      }
      for (int k = 0; k < nPOS; ++k) {
        s = input.readLine();
        splits = s.split(" ");
        knownPos.add(splits[0]);
        for (int i = 0; i < eSize; ++i)
          E[index][i] = Double.parseDouble(splits[i + 1]);
        index = index + 1;
      }
      for (int k = 0; k < nLabel; ++k) {
        s = input.readLine();
        splits = s.split(" ");
        knownLabels.add(splits[0]);
        for (int i = 0; i < eSize; ++i)
          E[index][i] = Double.parseDouble(splits[i + 1]);
        index = index + 1;
      }
      generateIDs();
      
      //because I need to use the system in building classifier
      //So, I construct the system here instead of in the initialize() module
      // NOTE: remove -NULL-, and then pass the label set to the ParsingSystem
      List<String> lDict = new ArrayList<>(knownLabels);
      lDict.remove(0);
      system = new ArcStandard(config.tlp, lDict, verbose);

      double[][] W1 = new double[hSize][eSize * nTokens];
      for (int j = 0; j < W1[0].length; ++j) {
        s = input.readLine();
        splits = s.split(" ");
        for (int i = 0; i < W1.length; ++i)
          W1[i][j] = Double.parseDouble(splits[i]);
      }

      double[] b1 = new double[hSize];
      s = input.readLine();
      splits = s.split(" ");
      for (int i = 0; i < b1.length; ++i)
        b1[i] = Double.parseDouble(splits[i]);

      
      double[][] W2 = new double[system.conActs.size()][hSize];
      for (int j = 0; j < W2[0].length; ++j) {
        s = input.readLine();
        splits = s.split(" ");
        for (int i = 0; i < W2.length; ++i)
          W2[i][j] = Double.parseDouble(splits[i]);
      }
      
      double[][][] labelLayer = new double[system.conActs.size()][system.labels.size()][hSize];
      for(int i = 0; i < labelLayer.length; i++)
    	  for(int j = 0; j < labelLayer[0].length; j++){
    		  s = input.readLine();
          	  splits = s.split(" ");
    		  for(int k = 0; k < labelLayer[0][0].length; k++){
    			  labelLayer[i][j][k] = Double.parseDouble(splits[k]);
    		  }
    	  }


      preComputed = new ArrayList<Integer>();
      while (preComputed.size() < nPreComputed) {
        s = input.readLine();
        splits = s.split(" ");
        for (String split : splits) {
          preComputed.add(Integer.parseInt(split));
        }
      }
      input.close();
      classifier = new Classifier(config, E, W1, b1, W2, labelLayer, preComputed);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }

    // initialize the loaded parser
    initialize(verbose);
    t.done("Initializing dependency parser");
  }

  // TODO this should be a function which returns the embeddings array + embedID
  // otherwise the class needlessly carries around the extra baggage of `embeddings`
  // (never again used) for the entire training process
  private void readEmbedFile(String embedFile) {
    embedID = new HashMap<String, Integer>();
    if (embedFile == null)
      return;
    BufferedReader input = null;
    try {
      input = IOUtils.readerFromString(embedFile);
      List<String> lines = new ArrayList<String>();
      for (String s; (s = input.readLine()) != null; ) {
        lines.add(s);
      }

      int nWords = lines.size();
      String[] splits = lines.get(0).split("\\s+");

      int dim = splits.length - 1;
      embeddings = new double[nWords][dim];
      System.err.println("Embedding File " + embedFile + ": #Words = " + nWords + ", dim = " + dim);

      //TODO: how if the embedding dim. does not match..?
      if (dim != config.embeddingSize)
        System.err.println("ERROR: embedding dimension mismatch");

      for (int i = 0; i < lines.size(); ++i) {
        splits = lines.get(i).split("\\s+");
        embedID.put(splits[0], i);
        for (int j = 0; j < dim; ++j)
          embeddings[i][j] = Double.parseDouble(splits[j + 1]);
      }
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(input);
    }
  }

  /**
   * Train a new dependency parser model.
   *
   * @param trainFile Training data
   * @param devFile Development data (used for regular UAS evaluation
   *                of model)
   * @param modelFile String to which model should be saved
   * @param embedFile File containing word embeddings for words used in
   *                  training corpus
   */
  public void train(String trainFile, String devFile, String modelFile, String embedFile) {
    System.err.println("Train File: " + trainFile);
    System.err.println("Dev File: " + devFile);
    System.err.println("Model File: " + modelFile);
    System.err.println("Embedding File: " + embedFile);

    List<CoreMap> trainSents = new ArrayList<>();
    List<CFGTree> trainTrees = new ArrayList<CFGTree>();
    Util.loadFile(trainFile, trainSents, trainTrees);
    //Util.printTreeStats("Train", trainTrees);

    List<CoreMap> devSents = new ArrayList<CoreMap>();
    List<CFGTree> devTrees = new ArrayList<CFGTree>();
    if (devFile != null) {
      Util.loadFile(devFile, devSents, devTrees);
      //Util.printTreeStats("Dev", devTrees);
    }
    genDictionaries(trainSents, trainTrees);

    //NOTE: remove -NULL-, and the pass it to ParsingSystem
    
    
    system = new ArcStandard(config.tlp, labelDict, true);
    

    // Initialize a classifier; prepare for training
    setupClassifierForTraining(trainSents, trainTrees, embedFile);

    System.err.println(Config.SEPARATOR);
    config.printParameters();

    long startTime = System.currentTimeMillis();
    /**
     * Track the best UAS performance we've seen.
     */
    double bestUAS = 0;

    for (int iter = 0; iter < config.maxIter; ++iter) {
      System.err.println("##### Iteration " + iter);

      Classifier.Cost cost = classifier.computeCostFunction(config.batchSize, config.regParameter, config.dropProb);
      System.err.println("Cost = " + cost.getCost() + ", Correct(%) = " + cost.getPercentCorrect());
      classifier.takeAdaGradientStep(cost, config.adaAlpha, config.adaEps);

      System.err.println("Elapsed Time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " (s)");

      // UAS evaluation
      if (devFile != null && iter % config.evalPerIter == 0) {
        // Redo precomputation with updated weights. This is only
        // necessary because we're updating weights -- for normal
        // prediction, we just do this once in #initialize
        classifier.preCompute();

        List<CFGTree> predicted = devSents.stream().map(this::predictInner).collect(toList());

        double uas = 0;
		try {
			uas = system.evaluate(devSents, predicted, devTrees);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.err.println("UAS: " + uas);

        if (config.saveIntermediate && uas > bestUAS) {
          System.err.printf("Exceeds best previous UAS of %f. Saving model file..%n", bestUAS);

          bestUAS = uas;
          writeModelFile(modelFile);
        }
      }

      // Clear gradients
      if (config.clearGradientsPerIter > 0 && iter % config.clearGradientsPerIter == 0) {
        System.err.println("Clearing gradient histories..");
        classifier.clearGradientHistories();
      }
    }

    classifier.finalizeTraining();

    if (devFile != null) {
      // Do final UAS evaluation and save if final model beats the
      // best intermediate one
      List<CFGTree> predicted = devSents.stream().map(this::predictInner).collect(toList());
      double uas = 0;
	try {
		uas = system.evaluate(devSents, predicted, devTrees);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

      if (uas > bestUAS) {
        System.err.printf("Final model UAS: %f%n", uas);
        System.err.printf("Exceeds best previous UAS of %f. Saving model file..%n", bestUAS);

        writeModelFile(modelFile);
      }
    } else {
      writeModelFile(modelFile);
    }
  }

  /**
   * @see #train(String, String, String, String)
   */
  public void train(String trainFile, String devFile, String modelFile) {
    train(trainFile, devFile, modelFile, null);
  }

  /**
   * @see #train(String, String, String, String)
   */
  public void train(String trainFile, String modelFile) {
    train(trainFile, null, modelFile);
  }

  /**
   * Prepare a classifier for training with the given dataset.
   */
  private void setupClassifierForTraining(List<CoreMap> trainSents, List<CFGTree> trainTrees, String embedFile) {
	  
    double[][] E = new double[knownWords.size() + knownPos.size() + knownLabels.size()][config.embeddingSize];
    double[][] W1 = new double[config.hiddenSize][config.embeddingSize * config.numTokens];
    double[] b1 = new double[config.hiddenSize];
    double[][] W2 = new double[system.conActs.size()][config.hiddenSize];
    double[][][] labelLayer = new double[system.conActs.size()][system.labels.size()][config.hiddenSize];

    // Randomly initialize weight matrices / vectors
    Random random = Util.getRandom();
    for (int i = 0; i < W1.length; ++i)
      for (int j = 0; j < W1[i].length; ++j)
        W1[i][j] = random.nextDouble() * 2 * config.initRange - config.initRange;

    for (int i = 0; i < b1.length; ++i)
      b1[i] = random.nextDouble() * 2 * config.initRange - config.initRange;

    for (int i = 0; i < W2.length; ++i)
      for (int j = 0; j < W2[i].length; ++j)
        W2[i][j] = random.nextDouble() * 2 * config.initRange - config.initRange;
    
    for (int i = 0; i < labelLayer.length; ++i)
        for (int j = 0; j < labelLayer[i].length; ++j)
        	for(int k = 0; k < labelLayer[i][j].length; k++)
        		labelLayer[i][j][k] = random.nextDouble() * 2 * config.initRange - config.initRange;

    // Read embeddings into `embedID`, `embeddings`
    readEmbedFile(embedFile);

    // Try to match loaded embeddings with words in dictionary
    int foundEmbed = 0;
    for (int i = 0; i < E.length; ++i) {
      int index = -1;
      if (i < knownWords.size()) {
        String str = knownWords.get(i);
        //NOTE: exact match first, and then try lower case..
        if (embedID.containsKey(str)) index = embedID.get(str);
        else if (embedID.containsKey(str.toLowerCase())) index = embedID.get(str.toLowerCase());
      }

      if (index >= 0) {
        ++foundEmbed;
        for (int j = 0; j < E[i].length; ++j)
          E[i][j] = embeddings[index][j];
      } else {
        for (int j = 0; j < E[i].length; ++j)
          E[i][j] = random.nextDouble() * config.initRange * 2 - config.initRange;
      }
    }
    System.err.println("Found embeddings: " + foundEmbed + " / " + knownWords.size());

    Dataset trainSet = genTrainExamples(trainSents, trainTrees);
    classifier = new Classifier(config, trainSet, E, W1, b1, W2, labelLayer, preComputed);
  }
  
  public ConParseTSSState partialParser(ConParseTSSState state, ConAction givenAction, CoreMap sentence, boolean keepGold){
	 
	  	int numTrans = system.transitions.size();

	    ConParseTSSState c = state == null? system.initialConfiguration(sentence) : state;
	    
	    while (!system.isTerminal(c)) {
	    	
	      if(c!=null && keepGold){  //exec the given action, the scores have been computed already 
	    	  double nextStateScore = 0;
	    	  double[] scores = c.scores;
		      
	    	  //generate the next state
	    	  c = system.apply(c,  givenAction.code());
	    	  nextStateScore = c.score + Math.log(scores[givenAction.code()]);
	    	  keepGold = false;  //only keep one action
		      
	    	  c.setScore(nextStateScore);
		     
		      continue;  //exec the given action, continue directly
	      }
	      
	      double[] scores = classifier.computeScores(getFeatureArray(c));

	      double optScore = Double.NEGATIVE_INFINITY;
	      int optTrans = -1;
	      List<Integer> label = new ArrayList<Integer>();

	      for (int j = 0; j < numTrans; ++j) {
	    	  
	    	  if(system.canApply(c, j))
	    		  label.add(0);
	    	  else
	    		  label.add(-1);
	  	    	
	    	  if (scores[j] > optScore && label.get(j) >= 0) {
	    		  optScore = scores[j];
	    		  optTrans = j;
	    	  }
		    	  
	      }
	      
	      label.set(optTrans, 1);  //best action label set 1
	      
	      //softmax the score
	      double sum = 0;
	      for(int j = 0 ; j < numTrans; ++j){
	    	  if(label.get(j) >= 0){
	    		  scores[j] = Math.exp(scores[j] - optScore);
	    		  sum += scores[j]; 
	    	  }
	      }
	      for(int j = 0 ; j < numTrans; ++j){
	    	  if(label.get(j) >= 0)
	    		  scores[j] = scores[j]/sum;
	      }
	      
	      c.setLabel(label);
	      c.setScores(scores);
	      c.setBestActIndex(optTrans);
	      
	      //#NOTE the score in the state is log() score, but the score array is
	      //      still probability, for convenient compare
	      double nextStateScore = c.score + Math.log(scores[optTrans]); 
	      c =  system.apply(c, optTrans);
	      c.setScore(nextStateScore);
	      
	    }
	    
	    return c;
  }
  
  public CFGTree oracleCompute(CoreMap sentence, CFGTree goldTree){
	  
	  int currentDepth = 0;
	  int oracleDepth = config.nHcDepth;
	  
	  int numTrans = system.transitions.size();
	  int iter = 0;

	    ConParseTSSState c = system.initialConfiguration(sentence);
	    while (!system.isTerminal(c)) {
	      
	      iter++;
	      double[] scores = classifier.computeScores(getFeatureArray(c));
	      
	      int goldAct = -1;
	      if(currentDepth < oracleDepth)
	    	  goldAct = system.getOracle(c, goldTree);
	      
	      double optScore = Double.NEGATIVE_INFINITY;
	      int optTrans = -1;
	      for (int j = 0; j < numTrans; ++j) {

	        if (scores[j] > optScore && system.canApply(c, j)) {
	          optScore = scores[j];
	          optTrans = j;
	        }
	      }
	      
	      //softmax the score
	      double sum = 0;
	      for(int j = 0 ; j < numTrans; ++j){
	    	  if(system.canApply(c, j)){
	    		  scores[j] = Math.exp(scores[j] - optScore);
	    		  sum += scores[j]; 
	    	  }
	      }
	      
	      for(int j = 0 ; j < numTrans; ++j){
	    	  if(system.canApply(c, j))
	    		  scores[j] = scores[j]/sum;
	    	  else scores[j] = 0;
	      }
	      
	      //set the optScore 
	      
	      if(currentDepth < oracleDepth){
	    	  //if(!system.transitions.get(optTrans).isSameType(system.transitions.get(goldAct))){
	    	  if(optTrans!=goldAct){
//	    		  System.err.println(iter +" "+system.transitions.get(optTrans).toString()+ " -> "+
//	    				  system.transitions.get(goldAct).toString());
//	    		  
//	    		  System.err.println(c.toString());
	    		  
	    		  currentDepth++;
	    		  optTrans = goldAct;
	    	  }
	      }
	     c =  system.apply(c, optTrans);
	    }
	    return c.convert2CFGTree();
  }

  /**
   * Determine the dependency parse of the given sentence.
   * <p>
   * This "inner" method returns a structure unique to this package; use {@link #predict(edu.stanford.nlp.util.CoreMap)}
   * for general parsing purposes.
   */
  private CFGTree predictInner(CoreMap sentence) {
    int numTrans = system.transitions.size();

    ConParseTSSState c = system.initialConfiguration(sentence);
    while (!system.isTerminal(c)) {
      Pair<Integer, Integer> optActPair= classifier.computeHierarchicalScore(getFeatureArray(c), c, system);
      c = system.apply(c, optActPair.first, optActPair.second);
      
//      double optScore = Double.NEGATIVE_INFINITY;
//      int optTrans = -1;
//
//      for (int j = 0; j < numTrans; ++j) {
//        if (scores[j] > optScore && system.canApply(c, j)) {
//          optScore = scores[j];
//          optTrans = j;
//        }
//      }
      
//     c =  system.apply(c, optTrans);
    }
    return c.convert2CFGTree();
  }

  //TODO: support sentence-only files as input

  /** Run the parser in the modelFile on a testFile and perhaps save output.
   *
   *  @param testFile File to parse. In CoNLL-X format. Assumed to have gold answers included.
   *  @param outFile File to write results to in CoNLL-X format.  If null, no output is written
   *  @return The LAS score on the dataset
   */
  public double testCoNLL(String testFile, String outFile) {
    System.err.println("Test File: " + testFile);
    Timing timer = new Timing();
    List<CoreMap> testSents = new ArrayList<>();
    List<CFGTree> testTrees = new ArrayList<CFGTree>();
    Util.loadFile(testFile, testSents, testTrees);
    // count how much to parse
    int numWords = 0;
    int numSentences = 0;
    for (CoreMap testSent : testSents) {
      numSentences += 1;
      numWords += testSent.get(CoreAnnotations.TokensAnnotation.class).size();
    }

    List<CFGTree> predicted = testSents.stream().map(this::predictInner).collect(toList());
    double f1 = 0;
	try {
		f1 = system.evaluate(testSents, predicted, testTrees);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    System.err.printf("f1 = %.4f%n", f1);
    long millis = timer.stop();
    double wordspersec = numWords / (((double) millis) / 1000);
    double sentspersec = numSentences / (((double) millis) / 1000);
    System.err.printf("%s tagged %d words in %d sentences in %.1fs at %.1f w/s, %.1f sent/s.%n",
            StringUtils.getShortClassName(this), numWords, numSentences, millis / 1000.0, wordspersec, sentspersec);

    return f1;
  }

  /**
   * Prepare for parsing after a model has been loaded.
   */
  private void initialize(boolean verbose) {
    if (knownLabels == null)
      throw new IllegalStateException("Model has not been loaded or trained");

    

    

    // Pre-compute matrix multiplications
    if (config.numPreComputed > 0) {
      classifier.preCompute();
    }
  }

  /**
   * Explicitly specifies the number of arguments expected with
   * particular command line options.
   */
  private static final Map<String, Integer> numArgs = new HashMap<>();
  static {
    numArgs.put("textFile", 1);
    numArgs.put("outFile", 1);
  }
  
  public static void main(String[] args) {
    Properties props = StringUtils.argsToProperties(args, numArgs);
    ConstituentParser parser = new ConstituentParser(props);

    // Train with CoNLL-X data
    if (props.containsKey("trainFile"))
      parser.train(props.getProperty("trainFile"), props.getProperty("devFile"), props.getProperty("model"),
          props.getProperty("embedFile"));

    //get selection braching oracle
    if(props.containsKey("getoracle")){
    	parser.loadModelFile(props.getProperty("model"));
    	parser.getOracle(props.getProperty("testFile"), props.getProperty("outFile"));
    }
    
    // Test with CoNLL-X data
    if (!props.containsKey("getNBest")&&!props.containsKey("getoracle")&&props.containsKey("testFile")) {
      parser.loadModelFile(props.getProperty("model"));

      parser.testCoNLL(props.getProperty("testFile"), props.getProperty("outFile"));
    }
    
    // get reranking n-best candidates
    if(props.containsKey("getNBest")&&props.containsKey("NBestTestFile")){
    	parser.loadModelFile(props.getProperty("model"));
    	
    	try {
			parser.getNBest(props.getProperty("NBestTestFile"), props.getProperty("outFile"),
					props.getProperty("outputGoldFile"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
  }

private void getNBest(String testFile, String outputFile, String outputGoldFile) throws IOException {

	PrintWriter output = IOUtils.getPrintWriter(outputFile);
	PrintWriter outputGold = IOUtils.getPrintWriter(outputGoldFile);
	List<List<Pair<Double, CFGTree>>> outputNBest = new ArrayList<List<Pair<Double,CFGTree>>>();
	List<CFGTree> goldTree = new ArrayList<CFGTree>();
	List<CoreMap> testSents = new ArrayList<>();
	
	List<CFGTree> bestTrees = new ArrayList<CFGTree>();
	List<CFGTree> worstTrees = new ArrayList<CFGTree>();
	List<CFGTree> originalTrees = new ArrayList<CFGTree>();
	
	for(int i = 1; i<=config.nNBestFileNum; i++){
		
		System.err.println("Begin to revise file "+i);
		testSents.clear();
		goldTree.clear();
		bestTrees.clear();
		worstTrees.clear();
		
		//Util.loadFile(testFile+"."+i, testSents, goldTree);
		Util.loadFile(testFile, testSents, goldTree);
		
		for(int j = 0; j<testSents.size(); j++){
			
			System.err.println("Begin to revise sentence "+j);
			
			CFGTree gTree = goldTree.get(j);
			CoreMap sent = testSents.get(j);
			
			if(config.bBeamNBest){
				outputNBest.add(beamDecode(gTree, sent));
			}
			else if(config.bBestFirstRevise)
				outputNBest.add(bestfirstRevise(gTree, sent));
			else if(config.bReviseOneAct)
				outputNBest.add(reviseTree(gTree, sent));
			//outputNBest.get(j).add(new Pair<Double, CFGTree>(0.0, oracleCompute(sent, gTree)));
			
		}

		// oracle computing
		double bestF1 = 0;
		double worstF1 = 0;
		CFGTree bestTree = null;
		CFGTree worstTree = null;
		
		//output
		outputGold.println(goldTree.size());
		for(int j = 0; j<testSents.size(); j++){
			
			List<Pair<Double, CFGTree>> nbest = outputNBest.get(j);
			output.println(nbest.size() + " WSJ_"+config.nFileIndex+"_"+j);
			output.flush();
			String goldTreeStr = goldTree.get(j).toString();
			originalTrees.add(nbest.get(0).second);  //the original tree is always
													 //the first one of returns
			
			for(int k = 0; k<nbest.size(); k++){
				output.println(nbest.get(k).first);
				output.println(nbest.get(k).second.toCharniarkString());
				
				//get oracle
				double f1 = system.evaluateSent(nbest.get(k).second.toString(), goldTreeStr);
				if(f1 > bestF1 || bestTree == null){
					bestF1 = f1;
					bestTree = nbest.get(k).second;
				}
				
				if(f1 < worstF1 || worstTree == null){
					worstF1 = f1;
					worstTree = nbest.get(k).second;
				}
			}
			
			output.println();
			
			outputGold.print("WSJ_"+config.nFileIndex+"_"+j+" ");
			outputGold.println(goldTree.get(j).toCharniarkString());
			bestTrees.add(bestTree);
			worstTrees.add(worstTree);
			bestTree = null;
			worstTree = null;
			bestF1 = 0;
			worstF1 = 0;
		}
		
		
	}
	
	output.close();
	outputGold.close();
	
	System.err.println("Best Oracle");
	System.err.println(system.evaluate(testSents, bestTrees, goldTree));
	
	System.err.println("Original Result");
	System.err.println(system.evaluate(testSents, originalTrees, goldTree));
	
	System.err.println("Worst Oracle");
	System.err.println(system.evaluate(testSents, worstTrees, goldTree));

}

	
private List<Pair<Double, CFGTree>> beamDecode(CFGTree gTree, CoreMap sentence) {

	List<Pair<Double, CFGTree>> retval = new ArrayList<Pair<Double,CFGTree>>();
	
	int numTrans = system.transitions.size();
	PriorityQueue<BeamState> queue = new PriorityQueue<BeamState>();
	List<BeamState> beam = new ArrayList<BeamState>();
	boolean beamAllEnd = false;
	
	ConParseTSSState initState = system.initialConfiguration(sentence);
	beam.add(new BeamState(initState));
	
	while(true){
		
		queue.clear();
		for(int i = 0; i < beam.size(); i++){
			
			/*
			 *  expand the base state
			 */
			BeamState bs = beam.get(i);
			if(bs.isEnd()){
				queue.add(bs);
				continue;  //if end, add to queue and continue
			}
			
			ConParseTSSState state = bs.state;
			double[] scores = classifier.computeScores(getFeatureArray(state));
			
			double optScore = Double.NEGATIVE_INFINITY;
			
			//softmax
			int optTrans = -1;
			List<Integer> label = new ArrayList<Integer>();
			
			for (int j = 0; j < numTrans; ++j) {
				
				if(system.canApply(state, j))
					label.add(0);
				else
					label.add(-1);
				
				if (scores[j] > optScore && label.get(j) >= 0) {
					optScore = scores[j];
					optTrans = j;
				}
				
			}
			
			label.set(optTrans, 1);  //best action label set 1
			
			double sum = 0;
			for(int j = 0 ; j < numTrans; ++j){
				if(label.get(j) >= 0){
					scores[j] = Math.exp(scores[j] - optScore);
					sum += scores[j]; 
				}
			}
			for(int j = 0 ; j < numTrans; ++j){
				if(label.get(j) >= 0){
					scores[j] = scores[j]/sum;
					queue.add(new BeamState(state.score + Math.log(scores[j]), state, j));
				}
			}
			
			state.setLabel(label);
			state.setScores(scores);
			state.setBestActIndex(optTrans);
			
			//#NOTE the score in the state is log() score, but the score array is
			//      still probability, for convenient compare
			double nextStateScore = state.score + Math.log(scores[optTrans]); 
			state =  system.apply(state, optTrans);
			state.setScore(nextStateScore);
			
			
		}
		
		//insert from queue to beam
		beam.clear();
		beamAllEnd = true;
		for(int i = 0; i < config.nMaxN; i++){
			
			if(queue.size() == 0)
				break;
			
			BeamState bs = queue.poll();
			if(!bs.isEnd()){
				bs.StateApply(system);
			}
			beam.add(bs);
			beamAllEnd = beamAllEnd && bs.isEnd();
		}
		
		//beam all end, add and return
		if(beamAllEnd){
			for(int i = 0; i < beam.size(); i++){
				BeamState bs = beam.get(i);
				retval.add(new Pair<Double, CFGTree>(bs.averageScore, bs.state.convert2CFGTree()));
			}
			
			return retval;
		}
	}
    
}

private List<Pair<Double, CFGTree>> reviseTree(CFGTree gTree, CoreMap sent) {

	List<Pair<Double, CFGTree>> retval = new ArrayList<Pair<Double,CFGTree>>();
	int transionNum = system.transitions.size();
	
	// get the initial result
	ConParseTSSState initState = partialParser(null, null, sent, false); 
	
	//add the greedy parser result first.
	retval.add(new Pair<Double, CFGTree>(initState.score/initState.actionSize, initState.convert2CFGTree()));
	
	//save the state point to a arraylist
	ConParseTSSState state = initState.statePtr;
	int stateIndex = 0;
	List<ConParseTSSState> stateArray = new ArrayList<ConParseTSSState>();
	while(state != null){
		stateArray.add(state);
		state = state.statePtr;
	}
	Collections.reverse(stateArray);

	//get the acts in the greedy state
	List<Integer> triedActs = new ArrayList<Integer>();
	for(int i = 1; i<stateArray.size(); i++)
		triedActs.add(stateArray.get(i).action.code());
	
	
	
	// priority queue from small to large
	PriorityQueue<ReviseItem> queue = new PriorityQueue<ReviseItem>();
	
	for(int i = 0; i < (stateArray.size()-1); i++){
		List<Integer> label = stateArray.get(i).label;
		double[] scores = stateArray.get(i).scores;
		int goldAct = triedActs.get(i);
		
		for(int j = 0; j < transionNum; j++){
			double margin = scores[goldAct] - scores[j];
			
			//if action margin is larger than max margin, skip
			if(margin > config.dMargin)
				continue;
			
			//only revise different branching action. S BR BL BR* BL* UR
//			if(system.transitions.get(j).isSameType(system.transitions.get(goldAct)))
//				continue;
			if(label.get(j) >= 0 && j!=goldAct )
				queue.add(new ReviseItem(i, j, margin));
			
		}
		
	}
	
	
	//if the queque do not have elements, then just return
//	if(queue.size() == 0)
//		return retval;
	
	for(int i = 0; i<config.nMaxN&&i<queue.size(); i++){
		
		ReviseItem item = queue.poll();
		
		ConParseTSSState predictState = partialParser(stateArray.get(item.stateIndex), 
				system.transitions.get(item.reviseActID), sent, true);
		retval.add(new Pair<Double, CFGTree>(predictState.score/predictState.actionSize, predictState.convert2CFGTree()));
	}
	
	return retval;
}

/**
 *   Best-First Revise Module
 *   
 *   Revise the greedy result of parser with a best-first framework
 *   The best-first queue is constructed with action margin and tree model score
 * 
 */
private List<Pair<Double, CFGTree>> bestfirstRevise(CFGTree gTree, CoreMap sent) {

	List<Pair<Double, CFGTree>> retval = new ArrayList<Pair<Double,CFGTree>>();

	/*
	 *   Given a parsing state, get top config.nMaxReviseActNum revisedItem from 
	 *   revisedItemsFromOneState queue and insert them into the queue according to the 
	 *   product of margin and average of log probability of a complete parsing state
	 *   
	 *   In each step, we peek one revised item form the queue and get the revised CFGTree.
	 *   Then insert the revised CFGTree into the revisedTrees queue.
	 *   
	 *   In the end, when the revisedTrees queue euqal the config.nMaxN, stop and return 
	 *   these revised CFGTrees.
	 */

	// priority queue from small to large
	PriorityQueue<RevisedState> queue = new PriorityQueue<RevisedState>();
	// priority queue of revised parsing tree
	PriorityQueue<ScoredCFGTree> revisedTrees = new PriorityQueue<ScoredCFGTree>();
	// priority queue of revised items from one complete parsing state
	PriorityQueue<RevisedState> revisedItemsFromOneState = new PriorityQueue<RevisedState>();
	
	int transionNum = system.transitions.size();
	
	// get the initial result
	ConParseTSSState initState = partialParser(null, null, sent, false); 
	
	//add the greedy parser result first.
	retval.add(new Pair<Double, CFGTree>(initState.score/initState.actionSize, initState.convert2CFGTree()));
	boolean firstRevise = true;
	
	//loop until the revised tree size to nMaxN
	while(revisedTrees.size() < config.nMaxN){
		
		if(firstRevise){  //revise from the greedy classifier output
			
			ConParseTSSState state = initState.statePtr; 
			double initStateScore = state.score/state.actionSize;
			
			state =state.statePtr; //from last state to second last state!
								   //because the last state do not need devise
			
			revisedItemsFromOneState.clear();
			
			//get the acts in the greedy state
			while(state != null){
				
				List<Integer> label = state.label;
				double[] scores =state.scores;
				int goldAct = state.bestActIndex;
				
				for(int j = 0; j < transionNum; j++){
					double margin = scores[goldAct] - scores[j];
					
					//if action margin is larger than max margin, or is the best or valid
					//action, just skip
					if(margin < config.dMargin &&
							label.get(j) == 0)
						revisedItemsFromOneState.add( new RevisedState(new ReviseItem(state.actionSize, j, margin), state, initStateScore) );
					
				}
				
				state = state.statePtr;
			}
			
			firstRevise = false;
		}
		else{  //revise from already revised parsing state
			
			if(queue.size() == 0) //no candidate in the queue
				break;
			
			RevisedState rs = queue.poll();
			//generate the new state and insert the revised tree to revisedTree Queue
				
			ConParseTSSState state = partialParser(rs.state, system.transitions.get(rs.item.reviseActID), 
						sent, true);
			double initStateScore = state.score/state.actionSize;
			revisedTrees.add(new ScoredCFGTree(state.convert2CFGTree(), initStateScore));
		
			
			int revisePos = rs.item.stateIndex;
			
			revisedItemsFromOneState.clear();
			state = state.statePtr; //from last state to second last state!
									//because the last state do not need devise
			
			//get the inherit revision candidate
			while(state.actionSize != revisePos){
				
				List<Integer> label = state.label;
				double[] scores =state.scores;
				int goldAct = state.bestActIndex;
				
				for(int j = 0; j < transionNum; j++){
					double margin = scores[goldAct] - scores[j];
					
					//if action margin is larger than max margin, or is the best or valid
					//action, just skip
					if(margin < config.dMargin && label.get(j) == 0){
						revisedItemsFromOneState.add( new RevisedState(new ReviseItem(state.actionSize, j, margin), state, initStateScore) );
					
						if(j == 0 || j == 1)
							System.err.println("Not Avaliable Action!");
					}
				}
				
				state = state.statePtr;
			}
			
		
		}
		
		//add the best n reviseItem to revisedState queue
		for(int k = 0; k < config.nMaxReviseActNum; k++){
			if(revisedItemsFromOneState.size()==0)
				break;
			
			queue.add(revisedItemsFromOneState.poll());
		}
	}
	
	//save the state point to a arraylist
	
	
	
	//if the queque do not have elements, then just return
//	if(queue.size() == 0)
//		return retval;
	
	for(int i = 0; i < config.nMaxN; i++){
		
		if(revisedTrees.size() == 0)
			break;
		
		ScoredCFGTree st = revisedTrees.poll();
		retval.add(new Pair<Double, CFGTree>(st.score, st.tree));
	}
	return retval;
}


private double getOracle(String testFile, String property2) {

	
	System.err.println("Test File: " + testFile);
    Timing timer = new Timing();
    List<CoreMap> testSents = new ArrayList<>();
    List<CFGTree> testTrees = new ArrayList<CFGTree>();
    Util.loadFile(testFile, testSents, testTrees);
    // count how much to parse
    int numWords = 0;
    int numSentences = 0;
    for (CoreMap testSent : testSents) {
      numSentences += 1;
      numWords += testSent.get(CoreAnnotations.TokensAnnotation.class).size();
    }

    List<CFGTree> predicted = new ArrayList<CFGTree>();
    for(int i = 0; i<testSents.size(); i++){
    	predicted.add(oracleCompute(testSents.get(i), testTrees.get(i)));
    }

    double f1 = 0;
	try {
		f1 = system.evaluate(testSents, predicted, testTrees);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    System.err.printf("f1 = %.4f%n", f1);
    long millis = timer.stop();
    double wordspersec = numWords / (((double) millis) / 1000);
    double sentspersec = numSentences / (((double) millis) / 1000);
    System.err.printf("%s tagged %d words in %d sentences in %.1fs at %.1f w/s, %.1f sent/s.%n",
            StringUtils.getShortClassName(this), numWords, numSentences, millis / 1000.0, wordspersec, sentspersec);

    return f1;
}

}