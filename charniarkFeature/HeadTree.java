package nncon.charniarkFeature;

import java.util.Vector;

//! HeadTree
//!
//! Identifier is HeadTree:collapse:lexicalize:nancs:headtype
//
public class HeadTree extends TreeFeatureClass {
	static class HeadTreeFeature extends Feature {
		String feature;

		public HeadTreeFeature() {
			feature = new String();
		}

		public HeadTreeFeature(String str) {
			if(str == null)
				this.feature = new String();
			else 
				this.feature = str;
		}

		@Override
		public int hashCode() {
			long h = 0;
			long g = 0;

			for (int i = 0; i < feature.length(); i++) {
				char ch = feature.charAt(i);

				h = (h << 4) + (long) ch;

				if ((g = h & 0xf0000000) != 0) {
					h = h ^ (g >> 24);
					h = h ^ g;
				}
			}

			return (int) h;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;

			HeadTreeFeature f = (HeadTreeFeature) obj;

			return feature.equals(f.feature);
		}

		@Override
		public String toString() {
			return Symbol.readStringFromFeatureString(feature);
		}
	}

	enum head_type {
		syntactic, semantic
	};

	static int htype_2_int(head_type type) {
		switch (type) {
		case syntactic:
			return 0;
		default:
			return 1;
		}
	}

	static head_type int_2_htype(int i) {
		if (i == 0)
			return head_type.syntactic;
		else
			return head_type.semantic;
	}

	boolean collapse; // !< collapse nodes not dominating ngram
	boolean lexicalize; // !< include lexical item
	int nancs; // !< extra ancestors to go up
	head_type htype; // !< type of heads to project

	public HeadTree(boolean collapse, boolean lexicalize, int nancs,
			head_type htype) {
		super();
		this.collapse = collapse;
		this.lexicalize = lexicalize;
		this.nancs = nancs;
		this.htype = htype;
		
		identifier_string = "HeadTree:" + (collapse ? "1" : "0") + ":"
				+ (lexicalize ? "1" : "0") + ":" + nancs + ":" + htype_2_int(htype);
	}

	private TreeNode selective_copy(SPTreeNode sp, long headleft) {
		if (sp == null)
			return null;

		SPTreeLabel label = sp.label;

		if (collapse) {
			long left = label.previous != null ? label.previous.label.left
					: label.left;
			long right = sp.next != null ? sp.next.label.right : label.right;

			if (right <= headleft)
				return selective_copy(sp.next, headleft);
			else if (left > headleft)
				return null;
		}

		TreeNode child = (sp.is_nonterminal()
				|| (lexicalize && label.left == headleft) ? selective_copy(
				sp.child, headleft) : null);
		TreeNode next = selective_copy(sp.next, headleft);

		return new TreeNode(label, child, next);
	}

	@Override
	String identifier() {
		return identifier_string;
	}

	@Override
	public void tree_featurecount(FeatureClass fc, SPTreeNode root,
			ParseValAccessor feat_count) {
		Vector<SPTreeNode> preterms = new Vector<SPTreeNode>();
		root.preterminal_nodes(preterms);

		for (int i = 0; i < preterms.size(); i++) {
			SPTreeNode t0 = preterms.get(i);

			while (true) {
				SPTreeNode parent = t0.label.parent;
				if (parent == null)
					break;

				SPTreeNode hchild = (htype == head_type.syntactic) ? parent.label.syntactic_headchild
						: parent.label.semantic_headchild;

				if (hchild != t0)
					break;

				t0 = parent;
			}
			
			assert(t0 != null);
			
			for(int ianc = 0; ianc < nancs && t0 != null; ++ianc)
				t0 = t0.label.parent;
			
			if(t0 == null)
				return ;
			
			TreeNode frag = selective_copy(t0, i);
			
//			if(this.identifier_string.equals("HeadTree:1:1:0:1"))
//				System.out.println("reach");
			
 			HeadTreeFeature feat = new HeadTreeFeature(frag.toString());
			
			double v = feat_count.getValueOf(feat);
			
			feat_count.setValueOf(feat, v + 1);
		}
	}
	@Override
	public void read_feature(String fs, long id) {
		String vs = null;
		
		vs = Symbol.readFeatureStringFromString(fs.trim());
		
		HeadTreeFeature feature = new HeadTreeFeature(vs);
		
		feature_id.put(feature, id);
	}
}
