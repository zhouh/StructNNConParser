package nncon;

public class ScoredCFGTree implements Comparable{
	public CFGTree tree;
	public double score;
	
	public ScoredCFGTree(CFGTree tree, double score) {
		super();
		this.tree = tree;
		this.score = score;
	}
	
	@Override
	public int compareTo(Object o) {

		ScoredCFGTree s = (ScoredCFGTree)o;
		int retval = score > s.score ? -1 : (score == s.score ? 0 : 1);
		return retval;
	}
	
}
