package nncon;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines an arc-standard transition-based constituent parsing system
 * 
 */
public class ArcStandard extends ParsingSystem {

	public Rules rule;

	public ArcStandard(TreebankLanguagePack tlp, List<String> labels, boolean verbose) {
		super(tlp, labels, verbose);
		rule = new Rules(labels);

	}

	@Override
	public boolean isTerminal(ConParseTSSState state) {
		return state.isEnd();
	}

	@Override
	public void makeTransitions() {
		transitions = new ArrayList<ConAction>();

		transitions.add(ConAction.NOT_AVAILABLE);
		transitions.add(ConAction.END_STATE);
		transitions.add(ConAction.SHIFT);

		// add binary reduce actions to transition
		boolean[] left = { true, false };
		boolean[] tmps = { true, false };
		for (String label : labels) {

			for (boolean bLeft : left)
				for (boolean temp : tmps) {
					ConAction action = ConAction.getLabeledReduceAction(true, bLeft, temp, label);
					if (action.code() == transitions.size())
						transitions.add(action);
					else {
						throw new RuntimeException("The new action cannot connect to the transition tail!");
					}
				}
		}

		// add unary reduce action to transition
		for (String label : labels) {
			if (label.endsWith("*") || label.equals(Config.NULL))
				continue;
			ConAction action = ConAction.getLabeledReduceAction(false, false, false, label);
			if (action.code() == transitions.size())
				transitions.add(action);
			else {
				throw new RuntimeException("The new action cannot connect to the transition tail!");
			}
		}

	}

	@Override
	public ConParseTSSState initialConfiguration(CoreMap s) {

		List<CoreLabel> sentence = s.get(CoreAnnotations.TokensAnnotation.class);
		List<String> words = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		for (CoreLabel label : sentence) {
			words.add(label.word());
			tags.add(label.tag());
		}

		ConParseInput input = new ConParseInput(words, tags);
		ConParseTSSState c = new ConParseTSSState(input);

		return c;
	}

	@Override
	public boolean canApply(ConParseTSSState c, int act) {

		List<ConAction> validActs = c.getNextActions(rule);
		for (ConAction action : validActs)
			if (action.code() == act)
				return true;

		return false;
	}

	@Override
	public List<Integer> getValidActs(ConParseTSSState c) {

		List<Integer> retval = new ArrayList<Integer>();
		List<ConAction> validActs = c.getNextActions(rule);
		for (ConAction action : validActs)
			retval.add(action.code());
		return retval;
	}

	@Override
	public ConParseTSSState apply(ConParseTSSState c, int act) {

		// we are going to construct a greedy parser
		// so, we set all bGold false
		return c.move(transitions.get(act), true);
	}

	@Override
	public ConParseTSSState apply(ConParseTSSState c, int act, int label) {

		int conActCode = -1;

		switch (act) {
		case nShift:
			conActCode = ConAction.SHIFT.code();
			break;
		case nL:
			conActCode = ConAction.getLabeledReduceAction(true, true, false, labels.get(label)).code();
			break;
		case nR:
			conActCode = ConAction.getLabeledReduceAction(true, false, false, labels.get(label)).code();
			break;
		case nLTemp:
			conActCode = ConAction.getLabeledReduceAction(true, true, true, labels.get(label)).code();
			break;
		case nRTemp:
			conActCode = ConAction.getLabeledReduceAction(true, false, true, labels.get(label)).code();
			break;
		default:
			conActCode = ConAction.getLabeledReduceAction(false, false, false, labels.get(label)).code();

		}
		return c.move(transitions.get(conActCode), true);
	}

	@Override
	public int getOracle(ConParseTSSState c, CFGTree dTree) {
		return c.getGoldAction(dTree).code();
	}

	@Override
	public List<Integer> getValidReduceActs(int actID, ConParseTSSState c) {

		switch (actID) {
		case nShift:
			return null;
		case nL:
			return rule.getValidReduceActs(true, true, false, c);
		case nR:
			return rule.getValidReduceActs(true, false, false, c);
		case nLTemp:
			return rule.getValidReduceActs(true, true, true, c);
		case nRTemp:
			return rule.getValidReduceActs(true, false, true, c);
		default:
			return rule.getValidReduceActs(false, false, false, c);

		}

	}

	@Override
	public boolean isShiftValid(ConParseTSSState c) {
		return rule.isShiftValid(c);
	}

	@Override
	public List<Integer> canApplyActsWithoutLabel(ConParseTSSState c) {
		List<Integer> retval = new ArrayList<Integer>(Collections.nCopies(conActs.size(), 0));

		if (!isShiftValid(c))
			retval.set(nShift, -1);
		List<Integer> UActs = rule.getValidReduceActs(false, false, false, c);
		List<Integer> LActs = rule.getValidReduceActs(true, true, false, c);
		List<Integer> LTempActs = rule.getValidReduceActs(true, true, true, c);
		List<Integer> RActs = rule.getValidReduceActs(true, false, false, c);
		List<Integer> RTempActs = rule.getValidReduceActs(true, false, true, c);

		if (UActs == null || UActs.size() == 0)
			retval.set(nU, -1);
		if (LActs == null || LActs.size() == 0)
			retval.set(nL, -1);
		if (RActs == null || RActs.size() == 0)
			retval.set(nR, -1);
		if (LTempActs == null || LTempActs.size() == 0)
			retval.set(nLTemp, -1);
		if (RTempActs == null || RTempActs.size() == 0)
			retval.set(nRTemp, -1);

		return retval;

	}

	/**
	 * 
	 * @param c
	 * @param dTree
	 * @return
	 */
	@Override
	public Pair<Integer, String> getOracleActAndConlabel(ConParseTSSState c, CFGTree dTree) {

		int actLabel = -1;
		ConAction oracle = c.getGoldAction(dTree);

		/*
		 * S 0 L 1 R 2 L* 3 R* 4 U 5
		 */
		if (oracle.isShiftAction())
			actLabel = nShift;
		else if (oracle.isLabeledUnaryReduce())
			actLabel = nU;
		else if (oracle.isLeftReduce()) {
			if (!oracle.isTemp())
				actLabel = nL;
			else
				actLabel = nLTemp;
		} else if (oracle.isRightReduce()) {
			if (!oracle.isTemp())
				actLabel = nR;
			else
				actLabel = nRTemp;
		} else {
			throw new RuntimeException("the oracle action is not valid!");
		}

		return new Pair<Integer, String>(actLabel, oracle.getTag());

	}

}
