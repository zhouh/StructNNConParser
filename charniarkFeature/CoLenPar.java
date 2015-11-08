package nncon.charniarkFeature;

//! CoLenPar{} counts the number of adjacent conjuncts that have
//!  the same length, are shorter, and are longer.
//
public class CoLenPar extends NodeFeatureClass {
	static class CoLenParFeature extends Feature {
		int first;
		int second;

		public CoLenParFeature(int first, int second) {
			super();
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

			CoLenParFeature f = ((CoLenParFeature) obj);
			if (this.first != f.first)
				return false;
			if (this.second != f.second)
				return false;

			return true;
		}

	}

	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if(!node.is_coordination())
			return ;
		
		SPTreeNode last_child = null;
		long last_size = 0;
		for(SPTreeNode child = node.child; child != null; child = child.next) {
			if(child.is_punctuation() || child.is_conjunction())
				continue;
			
			long size = child.label.right - child.label.left;
			
			if(last_child != null){
				long dsize = size - last_size;
				
				if(dsize > 4)
					dsize = 5;
				else if(dsize < -4)
					dsize = -5;
				
				CoLenParFeature f = new CoLenParFeature((int)dsize, (child.next == null) ? 1 : 0);
				double v = feat_count.getValueOf(f);
				feat_count.setValueOf(f, v + 1);
			}
			
			last_child = child;
			last_size = size;
		}
	}

	@Override
	String identifier() {
		return "CoLenPar";
	}

	@Override
	public void read_feature(String fs, long id) {
		String tokens[] = fs.substring(1, fs.length() - 1).split("[ ]+");
		int first = Integer.parseInt(tokens[0]);
		int second = Integer.parseInt(tokens[1]);
		
		CoLenParFeature feature = new CoLenParFeature(first, second);
		
		feature_id.put(feature, id);
	}
}
