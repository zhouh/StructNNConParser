package nncon.charniarkFeature;

import java.util.Vector;

//! The Heavy{} classifies nodes by their size and 
//! how close to the end of the sentence they occur, as well as whether
//! they are followed by punctuation or coordination.
//
public class Heavy extends PTsFeatureClass{
	static class HeavyFeature extends Feature {
		Vector<Integer> first;
		Vector<Symbol> second;
		
		public HeavyFeature() {
			first = new Vector<Integer>();
			second = new Vector<Symbol>();
		}
		
		@Override
		public int hashCode() {
			int f_hc = first.hashCode();
			int s_hc = second.hashCode();
			
			return f_hc ^ s_hc;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			HeavyFeature f = ((HeavyFeature)obj);
			Vector<Symbol> symbols = f.second;
			if(second.size() != symbols.size())
				return false;
			
			Vector<Integer> ints = f.first;
			if(first.size() != ints.size())
				return false;
			
			for(int i = 0; i < symbols.size(); i++) 
				if(!second.get(i).equals(symbols.get(i)))
					return false;
			
			for(int i = 0; i < ints.size(); i++)
				if(!first.get(i).equals(ints.get(i)))
					return false;
			
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder res = new StringBuilder("");
			
			res.append("(");
			res.append("(");
			for (Integer i : first) {
				res.append(i + " ");
			}
			res.deleteCharAt(res.length() - 1);
			res.append(") (");
			
			for (Symbol symbol : second) {
				res.append(symbol.getString() + " ");
			}
			res.deleteCharAt(res.length() - 1);
			res.append(")");
			res.append(")");
			
			return res.toString();
		}
	}
	
	public Heavy() {
		super();
	}
	@Override
	public void node_featurecount(FeatureClass fc, Vector<SPTreeNode> preterms,
			SPTreeNode node, ParseValAccessor feat_count) {
		if(!node.is_nonterminal())
			return ;
		
		Symbol final_punct = endmarker();
		Symbol following_punct = endmarker();
		
		assert(node.label.right > 0);
		assert(node.label.right <= preterms.size());
		
		if(preterms.get((int)node.label.right - 1).is_punctuation())
			final_punct = preterms.get((int)node.label.right - 1).child.label.cat;
		
		if(node.label.right < preterms.size()
				&& preterms.get((int)node.label.right).is_punctuation())
				following_punct = preterms.get((int)node.label.right).child.label.cat;
		
		HeavyFeature f = new HeavyFeature();
		
		f.first.add(quantize((int)node.label.right - (int)node.label.left));
		f.first.add(quantize(preterms.size() - (int)node.label.right));
		
		f.second.add(node.label.cat);
		f.second.add(final_punct);
		f.second.add(following_punct);
		
		double v = feat_count.getValueOf(f);
		feat_count.setValueOf(f, v + 1);
	}
	
	@Override
	String identifier(){
		return "Heavy";
	}
	@Override
	public void read_feature(String fs, long id) {
		HeavyFeature feature = new HeavyFeature();
		
		String ifs[] = fs.substring(2, fs.length() - 2).trim().split("\\) \\(");
		String ints[] = ifs[0].trim().split("[ ]");
		String syms[] = ifs[1].trim().split("[ ]");
		
		for (String i : ints) {
			feature.first.add(Integer.parseInt(i));
		}
		for (String sym : syms) {
			Symbol symbol = Symbol.readSymbolFromString(sym);
			if(symbol == null)
				return ;
			feature.second.add(symbol);
		}
		
		feature_id.put(feature, id);
	}
}
