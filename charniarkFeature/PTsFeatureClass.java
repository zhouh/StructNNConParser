package nncon.charniarkFeature;

import java.util.Vector;

//! The PTsFeatureClass is an ABC for feature classes where the feature
//! count for a tree is the sum of feature counts for each node, and the
//! feature counts for each node depends on the local tree and its ancestors,
//! and the node's left and right string positions.
//!
//! Every subclass of PTsFeatureClass must define a method:
//!
//!  node_featurecount(fc, preterminals, tp, feat_count)
//
abstract public class PTsFeatureClass extends TreeFeatureClass {
	public PTsFeatureClass() {
		super();
	}
	@Override
	public void tree_featurecount(FeatureClass fc, SPTreeNode tp,
			ParseValAccessor feat_count) {
		assert (tp != null);

		Vector<SPTreeNode> preterms = new Vector<SPTreeNode>();

		tp.preterminal_nodes(preterms, true);

		if (preterms.size() != tp.label.right) {
			System.err.println("## preterms = ");
			for (SPTreeNode spTreeNode : preterms) {
				System.err.print(spTreeNode.toString() + " ");
			}
			System.err.println("\n" + "## tp " + tp.toString());
			return;
		}

		assert (preterms.size() == tp.label.right);
		tree_featurecount(fc, preterms, tp, feat_count);
	}

	private void tree_featurecount(FeatureClass fc,
			Vector<SPTreeNode> preterms, SPTreeNode tp,
			ParseValAccessor feat_count) {
		PTsFeatureClass nfc = (PTsFeatureClass) fc;

		nfc.node_featurecount(nfc, preterms, tp, feat_count);
		if (tp.is_nonterminal())
			tree_featurecount(nfc, preterms, tp.child, feat_count);
		if (tp.next != null)
			tree_featurecount(nfc, preterms, tp.next, feat_count);
	}

	abstract public void node_featurecount(FeatureClass fc,
			Vector<SPTreeNode> preterms, SPTreeNode tp,
			ParseValAccessor feat_count);
}
