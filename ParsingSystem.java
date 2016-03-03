package nncon;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Defines a transition-based parsing framework for dependency parsing.
 *
 * @author Danqi Chen
 * @author Hao Zhou
 */
public abstract class ParsingSystem {

  /**
   * Defines language-specific settings for this parsing instance.
   */
  private final TreebankLanguagePack tlp;
  
  public static final int nShift = 0;
	public static final int nL = 1;
	public static final int nR = 2;
	public static final int nLTemp = 3;
	public static final int nRTemp = 4;
	public static final int nU = 5;

  /**
   * Dependency label used between root of sentence and ROOT node
   */

  protected List<String> labels;
  protected List<ConAction> transitions;
	public List<String> conActs;
  
  /**
   *   evalb for sentence
   */
  EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval1 = null;

  /**
   * Generate all possible transitions which this parsing system can
   * take for any given configuration.
   */
  protected abstract void makeTransitions();

  /**
   * Determine whether the given transition is legal for this
   * configuration.
   *
   * @param c Parsing configuration
   * @param t Transition string
   * @return Whether the given transition is legal in this
   *         configuration
   */
  public abstract boolean canApply(ConParseTSSState c, int t);
  
  public abstract List<Integer> getValidActs(ConParseTSSState c);

  /**
   * Apply the given transition to the given configuration, modifying
   * the configuration's state in place.
   */
  public abstract ConParseTSSState apply(ConParseTSSState c, int t);

  public abstract int getOracle(ConParseTSSState c, CFGTree gTree);

  /**
   * Build an initial parser configuration from the given sentence.
   */
  public abstract ConParseTSSState initialConfiguration(CoreMap sentence);

  /**
   * Determine if the given configuration corresponds to a parser which
   * has completed its parse.
   */
  abstract boolean isTerminal(ConParseTSSState state);

  // TODO pass labels as Map<String, GrammaticalRelation>; use
  // GrammaticalRelation throughout
  
  abstract List<Integer> getValidReduceActs(int actID, ConParseTSSState c);
  
  abstract boolean isShiftValid(ConParseTSSState c);
  
  abstract Pair<Integer, String> getOracleActAndConlabel(ConParseTSSState c, CFGTree dTree);
  
  abstract List<Integer> canApplyActsWithoutLabel(ConParseTSSState c);
  
  abstract ConParseTSSState apply(ConParseTSSState c, int act, int label);

  /**
   * @param tlp TreebankLanguagePack describing the language being
   *            parsed
   * @param labels A list of possible dependency relation labels, with
   *               the ROOT relation label as the first element
   */
  public ParsingSystem(TreebankLanguagePack tlp, List<String> labels, boolean verbose) {
    this.tlp = tlp;
    this.labels = new ArrayList<String>(labels);

    makeTransitions();
    
    conActs = new ArrayList<String>();
    conActs.add("S");
    conActs.add("L");
    conActs.add("R");
    conActs.add("L*");
    conActs.add("R*");
    conActs.add("U");
    
    eval1 = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(
			new HashSet<String>(Arrays.asList(new String[] { "ROOT"})),
			new HashSet<String>());

    if (verbose) {
      System.err.println(Config.SEPARATOR);
      System.err.println("#Transitions: " + transitions.size());
      System.err.println("#Labels: " + labels.size());
    }
  }

  public int getTransitionID(ConAction action) {

	  if(action!=null)
		  return action.code();
	  else {
		return -1;
	}
  }
  
  /**
   * Evaluate performance on a list of sentences, predicted parses,
   * and gold parses.
   *
   * @return A map from metric name to metric value
 * @throws IOException 
   */
  public double evaluate(List<CoreMap> sentences, List<CFGTree> trees,
                                      List<CFGTree> goldTrees) throws IOException {
	  
	  PrintWriter outguess = IOUtils.getPrintWriter("guessTrees");
	  PrintWriter outgold = IOUtils.getPrintWriter("goldTrees");
	  
	  for(CFGTree guesstree : trees)
		  outguess.println(guesstree.toString());
	  outguess.close();
	  
	  for(CFGTree goldtree:goldTrees)
		  outgold.println(goldtree.toString());
	  outgold.close();
	  
	  if (tlp instanceof PennTreebankLanguagePack) {
		  String[] path = {"goldTrees", "guessTrees"};
		  return Evalb.massEvalb(path);
	    } else {
	    	String[] path = {"goldTrees", "guessTrees", "-l Chinese"};
			  return Evalb.massEvalb(path);
	    }
  }
  
  public double evaluateSent(String treeStr, String goldTreeStr) throws IOException {
	  
	  Tree<String> guessedTree = (new Trees.PennTreeReader(new StringReader(
			  treeStr))).next();
	  Tree<String> goldTree = (new Trees.PennTreeReader(new StringReader(
			  goldTreeStr))).next();
	  
	 return eval1.evaluateAndReturnPara(guessedTree, goldTree, null);
  }

  
  public static void main(String[] args){
	  String[] path = { "./data/wsj/testr.txt", "./data/wsj/test.result" };
	  System.out.println(Evalb.massEvalb(path));
  }

}
