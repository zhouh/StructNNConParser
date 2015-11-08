package nncon.charniarkFeature;

//! CoPar{} counts the number of parallel and non-parallel coordinations
//! at various levels
//!
//! Identifier is CoPar:IgnorePreterms
//
public class CoPar extends NodeFeatureClass {
	static class CoParFeature extends Feature {
		int first;
		int second;

		public CoParFeature(int first, int second) {
			this.first = first;
			this.second = second;
		}
		@Override
		public String toString() {
			return "(" + this.first + " " + this.second + ")";
		}
		@Override
		public int hashCode() {
			int res = (new Integer(first)).hashCode()
					^ (new Integer(second)).hashCode();

			return res;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;

			CoParFeature f = ((CoParFeature) obj);
			if (this.first != f.first)
				return false;
			if (this.second != f.second)
				return false;

			return true;
		}

	}

	boolean ignore_preterms;

	public CoPar() {
		this(false);
	}

	public CoPar(boolean ignore_preterms) {
		super();
		this.ignore_preterms = ignore_preterms;
		identifier_string = "CoPar:" + (ignore_preterms ? "1" : "0");
	}

	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if(!node.is_coordination())
			return ;
		
		for(int depth = 1; depth <= 5; ++depth) {
			SPTreeNode last_child = null;
			
			for(SPTreeNode child = node.child; child != null; child = child.next) {
				if(child.is_punctuation() || child.is_conjunction())
					continue;
				
				if(last_child != null) {
					int m = match(depth, last_child, child);
					if(m != -1) {
						CoParFeature feat = new CoParFeature(depth, m);
						double v = feat_count.getValueOf(feat);
						feat_count.setValueOf(feat, v + 1);
					}
				}
				last_child = child;
			}
		}
	}

	// ! match() returns 1 if node1 and node2 match to depth, 0 if they mismatch
	// ! and -1 if they match but do not have any subnodes at depth.
	//
	private int match(int depth, SPTreeNode node1, SPTreeNode node2) {
		assert (node1 != null);
		assert (node2 != null);

		if (!node1.label.cat.equalTo(node2.label.cat))
			return 0;

		if (depth == 1)
			return 1;

		if (node1.is_preterminal()) {
			assert (node2.is_preterminal());
			return -1;
		}

		return matches(depth - 1, node1.child, node2.child);
	}

	// ! matches() is responsible for matching node1 and node2 and their right
	// ! siblings
	//
	private int matches(int depth, SPTreeNode node1, SPTreeNode node2) {
		assert (depth >= 1);

		if (ignore_preterms) {
			while (node1 != null && node1.is_preterminal())
				node1 = node1.next;
			while (node2 != null && node2.is_preterminal())
				node2 = node2.next;
		}

		if (node1 == null)
			return (node2 == null) ? -1 : 0;

		if (node2 == null)
			return 0;

		int m1 = match(depth, node1, node2);
		int m2 = matches(depth, node1.next, node2.next);

		if (m1 == 0 || m2 == 0)
			return 0;
		else if (m1 == 1 || m2 == 1)
			return 1;
		else
			return -1;
	}

	@Override
	String identifier() {
		return identifier_string;
	}
	
	@Override
	public void read_feature(String fs, long id) {
		String tokens[] = fs.substring(1, fs.length() - 1).split("[ ]+");
		int first = Integer.parseInt(tokens[0]);
		int second = Integer.parseInt(tokens[1]);
		
		CoParFeature feature = new CoParFeature(first, second);
		
		feature_id.put(feature, id);
	}
}
