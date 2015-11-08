package nncon.charniarkFeature;

//! The RightBranch defines two features: 1, which is true
//! of lexical nodes on the right-most branch and 0, which is true of
//! nodes that are not on the right-most branch.
//
public class RightBranch extends TreeFeatureClass{
	
	static class RightBranchFeature extends Feature{
		Long feature;
		
		public RightBranchFeature(int feature) {
			this.feature = new Long(feature);
		}
		
		@Override
		public String toString(){
			return "" + feature;
		}
		
		@Override
		public int hashCode() {
			return feature.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			return ((RightBranchFeature)obj).feature.equals(feature);
		}
		
	}
	
	public RightBranch() {
		super();
	}
	@Override
	public void tree_featurecount(FeatureClass fc, SPTreeNode tp,
			ParseValAccessor feat_count) {
		rightbranch_count(tp, 1, feat_count);
	}
	
	private int rightbranch_count(SPTreeNode tp, int rightmost, ParseValAccessor fc) {
		if(tp.next != null)
			rightmost = rightbranch_count(tp.next, rightmost, fc);
		if(tp.is_punctuation())
			return rightmost;
		
		RightBranchFeature rFeature = new RightBranchFeature(rightmost);
		fc.setValueOf(rFeature, fc.getValueOf(rFeature) + 1);
		
		if(tp.is_nonterminal())
			rightbranch_count(tp.child, rightmost, fc);
		
		return 0;
	}
	
	@Override
	String identifier(){
		return "RightBranch";
	}
	
	@Override
	public void read_feature(String fs, long id) {
		RightBranchFeature feature = new RightBranchFeature(Integer.parseInt(fs));
		
		feature_id.put(feature, id);
	}
}
