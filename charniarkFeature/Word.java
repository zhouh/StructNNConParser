package nncon.charniarkFeature;

import java.util.Vector;

//! Word{} collects information on words in their vertical context.
//!
//! Identifier is Word:<nanccats>
//!
public class Word extends NodeFeatureClass {
	static class WordFeature extends Feature {
		Vector<Symbol> feature;
		
		public WordFeature () {
			feature = new Vector<Symbol>();
		}
		
		public void push(Symbol cat) {
			feature.add(cat);
		}
		
		@Override
		public int hashCode(){
			long h = 0;
			long g = 0;

			for (int i = 0; i < feature.size(); i++) {
				int ch = feature.get(i).hashCode();

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
			if (getClass() != obj.getClass())
				return false;
			
			WordFeature f = ((WordFeature)obj);
			Vector<Symbol> symbols = f.feature;
			if(feature.size() != symbols.size())
				return false;
			
			for(int i = 0; i < feature.size(); i++) 
				if(!feature.get(i).equals(symbols.get(i)))
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
	
	long nanccats;
	
	public Word() {
		this(1);
	}
	
	public Word(long nanccats) {
		super();
		this.nanccats = nanccats;
		identifier_string = "Word:" + nanccats;
	}
	
	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if(!node.is_preterminal())
			return ;
		
		WordFeature f = new WordFeature();
		f.push(node.child.label.cat);
		
		for(int i = 0; i < nanccats; i++) {
			if(node == null)
				return ;
			
			f.push(node.label.cat);
			node = node.label.parent;
		}
		
		double v = feat_count.getValueOf(f);
		feat_count.setValueOf(f, v + 1);
	}

	@Override
	String identifier() {
		return identifier_string;
	}
	
	@Override
	public void read_feature(String fs, long id) {
		String tokens[] = fs.substring(1, fs.length() - 1).trim().split("[ ]+");
		
		WordFeature feature = new WordFeature();
		for (String token : tokens) {
			Symbol symbol = Symbol.readSymbolFromString(token.trim());
			if(symbol == null)
				return ;
			feature.push(symbol);
		}
		
		feature_id.put(feature, id);
	}
}
