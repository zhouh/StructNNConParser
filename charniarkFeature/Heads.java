package nncon.charniarkFeature;

import java.util.HashMap;
import java.util.Vector;

//! Heads is a feature of n levels of head-to-head dependencies.
//! Heads takes special care to follow head dependencies through
//! conjunctions.
//!
//! The identifier string is Heads:nheads:governorlex:dependentlex:headtype.
//
public class Heads extends NodeFeatureClass {
	static class HeadsFeature extends Feature {
		Vector<Symbol> feature;

		public HeadsFeature() {
			feature = new Vector<Symbol>();
		}
		
		public HeadsFeature(HeadsFeature f) {
			feature = new Vector<Symbol>(f.feature);
		}

		public void push(Symbol cat) {
			feature.add(cat);
		}

		public void pop() {
			assert (feature.size() > 0);
			feature.remove(feature.size() - 1);
		}

		public int size() {
			return feature.size();
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

			HeadsFeature f = ((HeadsFeature) obj);
			Vector<Symbol> symbols = f.feature;
			if (feature.size() != symbols.size())
				return false;

			for (int i = 0; i < feature.size(); i++)
				if (!feature.get(i).equals(symbols.get(i)))
					return false;

			return true;
		}

		@Override
		public String toString() {
			StringBuilder res = new StringBuilder("");
			res.append("(");
			for (Symbol symbol : feature) {
				res.append(symbol.toString() + " ");
			}
			res.deleteCharAt(res.length() - 1);
			res.append(")");
			return res.toString();
		}
	}

	enum head_type_type {
		syntactic, semantic
	};

	static int httype_2_int(head_type_type type) {
		if (type == head_type_type.syntactic)
			return 0;
		else
			return 1;
	}

	static head_type_type int_2_httype(int i) {
		if (i == 0)
			return head_type_type.syntactic;
		else
			return head_type_type.semantic;
	}

	long nheads;
	boolean governolex;
	boolean dependentlex;
	head_type_type head_type;

	public Heads(long nheads, boolean governorlex, boolean dependentlex) {
		this(nheads, governorlex, dependentlex, head_type_type.syntactic);
	}

	public Heads(long nheads, boolean governorlex, boolean dependentlex,
			head_type_type head_type) {
		this.nheads = nheads;
		this.governolex = governorlex;
		this.dependentlex = dependentlex;
		this.head_type = head_type;
		feature_id = new HashMap<Feature, Long>();
		
		identifier_string = "Heads:" + nheads + ":" + (governorlex ? "1" : "0")
				+ ":" + (dependentlex ? "1" : "0") + ":"
				+ httype_2_int(head_type);
	}

	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if (!node.is_preterminal())
			return;

		HeadsFeature f = new HeadsFeature();

		f.push(node.label.cat);

		if (dependentlex)
			f.push(node.child.label.cat);

		visit_ancestors(feat_count, node, 1, f);
		assert (f.size() == (dependentlex ? 2 : 1));
	}

	private void visit_ancestors(ParseValAccessor feat_count, SPTreeNode node,
			long nsofar, HeadsFeature f) {
		if (nsofar == nheads) {
//			System.err.println("nsofar == nheads: " + f.toString());
			HeadsFeature new_f = new HeadsFeature(f);
			double v = feat_count.getValueOf(new_f);
			feat_count.setValueOf(new_f, v + 1);
			return;
		}

		SPTreeNode ancestor = node.label.parent;

		if (ancestor == null)
			return;

		if (ancestor.is_coordination())
			visit_ancestors(feat_count, ancestor, nsofar, f);
		else {
			SPTreeNode hchild = headchild(ancestor);

			if (hchild != null && node != hchild)
				visit_decendants(feat_count, ancestor, nsofar, f, hchild);
			else
				visit_ancestors(feat_count, ancestor, nsofar, f);
		}
	}

	private void visit_decendants(ParseValAccessor feat_count,
			SPTreeNode ancestor, long nsofar, HeadsFeature f, SPTreeNode head) {
		if (head.is_preterminal()) {
			f.push(head.label.cat);
			if (governolex)
				f.push(head.child.label.cat);
			visit_ancestors(feat_count, ancestor, nsofar + 1, f);
			if (governolex)
				f.pop();
			f.pop();
		} else {
			if (head.is_coordination()) {
				for (SPTreeNode child = head.child; child != null; child = child.next)
					if (child.label.cat.equalTo(head.label.cat))
						visit_decendants(feat_count, ancestor, nsofar, f, child);
			} else {
				SPTreeNode child = headchild(head);
				if (child != null)
					visit_decendants(feat_count, ancestor, nsofar, f, child);
			}
		}
	}

	private SPTreeNode headchild(SPTreeNode node) {
		return head_type == head_type_type.semantic ? node.label.semantic_headchild
				: node.label.syntactic_headchild;
	}

	@Override
	String identifier() {
		return identifier_string;
	}
	
	@Override
	public void read_feature(String fs, long id) {
		String tokens[] = fs.substring(1, fs.length() - 1).trim().split("[ ]+");
		
		HeadsFeature feature = new HeadsFeature();
		for (String token : tokens) {
			Symbol symbol = Symbol.readSymbolFromString(token.trim());
			if(symbol == null)
				return ;
			feature.push(symbol);
		}
		
		feature_id.put(feature, id);
	}
}
