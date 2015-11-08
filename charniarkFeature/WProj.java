package nncon.charniarkFeature;

import java.util.Vector;

//! WProj{} collects information on words in their vertical context.
//! It projects each word up to its maximal projection.
//!
//! Identifier is WProj:<HeadType>:<IncludeNonMaximal>:<NAncs>
//!
public class WProj extends NodeFeatureClass {
	static class WProjFeature extends Feature {
		Vector<Symbol> feature;
		
		public WProjFeature () {
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
			
			WProjFeature f = ((WProjFeature)obj);
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
	
	annotation_type type;
	boolean include_nonmaximal;
	long nancs;
	
	public WProj() {
		this(annotation_type.semantic, false, 1);
	}

	public WProj(annotation_type type, boolean include_nonmaximal, long nancs){
		super();
		this.type = type;
		this.include_nonmaximal = include_nonmaximal;
		this.nancs = nancs;
		identifier_string = "WProj:" + type_2_int(type) + ":";
		identifier_string += (include_nonmaximal ? "1" : "0") + ":";
		identifier_string += nancs;
	}
	
	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if(node.is_punctuation() || !node.is_preterminal())
			return ;

		WProjFeature f = new WProjFeature();
		f.push(node.child.label.cat);
		
		while(node.label.parent != null){
			SPTreeNode parent = node.label.parent;
			SPTreeNode parent_headchild = (type == annotation_type.semantic ? parent.label.semantic_headchild : parent.label.syntactic_headchild);
			
			boolean is_headchild = (node == parent_headchild && !parent.is_root()) ? true : false;
			
			if(is_headchild) {
				if(include_nonmaximal)
					f.push(node.label.cat);
			} else 
				break;
			
			node = parent;
		}
		
		for(long i = 0; node != null && i <= nancs; node = node.label.parent, i++)
			f.push(node.label.cat);
		
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
		
		WProjFeature feature = new WProjFeature();
		for (String token : tokens) {
			Symbol symbol = Symbol.readSymbolFromString(token.trim());
			if(symbol == null)
				return ;
			feature.push(symbol);
		}
		
		feature_id.put(feature, id);
	}
}
