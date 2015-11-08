package nncon.charniarkFeature;

//! A NodeFeatureClass is an ABC for classes of features where the
//! feature count for a tree is the sum of the feature counts for
//! each node.
//!
//! Every subclass to NodeFeatureClass must define a method:
//!
//!  node_featurecount(fc, tp, feat_count);
//
public abstract class NodeFeatureClass extends TreeFeatureClass {
	enum annotation_type { semantic, syntactic };
	static annotation_type int_2_type(int i) {
		if(i == 0) return annotation_type.semantic;
		else return annotation_type.syntactic;
	}
	static int type_2_int(annotation_type t) {
		if(t == annotation_type.semantic) return 0;
		return 1;
	}
	public NodeFeatureClass() {
		super();
	}
	
	@Override 
	public void tree_featurecount(FeatureClass fc, SPTreeNode tp, ParseValAccessor feat_count) {
		assert(tp != null);
		
		NodeFeatureClass nfc = (NodeFeatureClass)fc;
		nfc.node_featurecount(nfc, tp, feat_count);
		
		if(tp.is_nonterminal()) {
			tree_featurecount(nfc, tp.child, feat_count);
		} 
		if(tp.next != null) {
			tree_featurecount(nfc, tp.next, feat_count);
		}
	}
	
	abstract public void node_featurecount(FeatureClass fc, SPTreeNode node, ParseValAccessor feat_count);
}
