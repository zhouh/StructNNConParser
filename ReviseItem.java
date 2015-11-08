package nncon;

public class ReviseItem implements Comparable{

	int stateIndex;
	int reviseActID;
	double margin;
	
	public ReviseItem(int stateIndex, int reviseActID, double margin) {
		this.stateIndex = stateIndex;
		this.reviseActID = reviseActID;
		this.margin = margin;
	}
	
	/**
	 *   For sort from large to small
	 */
	@Override
	public int compareTo(Object o) {

		ReviseItem s = (ReviseItem)o;
		int retval = margin > s.margin ? 1 : (margin == s.margin ? 0 : -1);
		return retval;
	}

}
