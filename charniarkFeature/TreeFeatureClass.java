package nncon.charniarkFeature;

import nncon.charniarkFeature.SPData.sp_parse_type;

//! A TreeFeatureClass is an ABC for classes of features where the
//! feature count for a parse is defined by the parse's tree (most
//! features are like this).
//!
//! Every subclass to TreeFeatureClass must define a method:
//!
//!  tree_featurecount(fc, tp, feat_count);
//
public abstract class TreeFeatureClass extends FeatureClass {
	public TreeFeatureClass() {
		super();
	}
	
	@Override
	public void parse_featurecount(FeatureClass fc, sp_parse_type p, ParseValAccessor feat_count){
		assert(p.parse != null);
		
		TreeFeatureClass nfc = (TreeFeatureClass)fc;
		nfc.tree_featurecount(nfc, p.parse, feat_count);
	}
	
	abstract public void tree_featurecount(FeatureClass fc, SPTreeNode tp, ParseValAccessor feat_count);
}
