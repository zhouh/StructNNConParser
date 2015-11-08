package nncon;

public class RevisedState implements Comparable{

	public ReviseItem item;
	public ConParseTSSState state;
	public double score;  // the score is the negative product of margin in item and score in state 
						  // the small, the better
	public RevisedState(ReviseItem item, ConParseTSSState state, double initStateScore) {
		super();
		this.item = item;
		this.state = state;
		this.score = - item.margin * initStateScore;
	}
	
	@Override
	public int compareTo(Object o) {

		RevisedState s = (RevisedState)o;
		int retval = score > s.score ? 1 : (score == s.score ? 0 : -1);
		return retval;
	}
	
}
