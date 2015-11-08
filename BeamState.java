package nncon;

public class BeamState implements Comparable{
	
	public double averageScore;
	public double score;
	public ConParseTSSState state;
	public int act;
	
	public BeamState(ConParseTSSState state){
		this.score = state.score;
		this.state = state;
		this.averageScore= 0; 
		this.act = -1;
	}
	
	public BeamState(double score, ConParseTSSState state, int act) {
		super();
		this.score = score;
		this.averageScore= score/ (state.actionSize+1); //when this object is constructed, the 
												   //state is still last BeamState's  state,
												   //it has not done StateApply. So to compute 
												   //the average score, we need ...
		this.state = state;
		this.act = act;
	}

	public void StateApply(ParsingSystem system){
		
		state = system.apply(state,  act);
		state.score = score;
	}
	
	public boolean isEnd(){
		return this.state.isEnd();
	}

	/**
	 *   For sort from large to small
	 */
	@Override
	public int compareTo(Object o) {

		BeamState s = (BeamState)o;
		int retval = averageScore > s.averageScore ? -1 : (averageScore == s.averageScore ? 0 : 1);
		return retval;
	}

}
