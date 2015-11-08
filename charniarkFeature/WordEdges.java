package nncon.charniarkFeature;

import java.util.Vector;

//! The WordEdges{} includes the node's category, its binned length
//! and the left and right words preceding and following the constituent edges
//!
//! Its identifier is WordEdges:<binnedlength>:<nleftprec>:<nleftsucc>:<nrightprec>:<nrightsucc>
//
public class WordEdges extends PTsFeatureClass{
	static class WordEdgesFeature extends Feature {
		Vector<Symbol> feature;

		public WordEdgesFeature() {
			feature = new Vector<Symbol>();
		}

		public void push(Symbol cat) {
			feature.add(cat);
		}

		@Override
		public int hashCode() {
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

			WordEdgesFeature f = ((WordEdgesFeature) obj);
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

	boolean binned_length; // collect binned length
	long nleftprec, nleftsucc, nrightprec, nrightsucc; // number of words
														// surrounding edges
	public WordEdges(boolean binned_length, // include binned length
			long nleftprec, // include nleft left preceding words
			long nleftsucc, // include nleft left following words
			long nrightprec, // include nright right preceding words
			long nrightsucc // include nright right following words
	) {
		this.binned_length = binned_length;
		this.nleftprec = nleftprec;
		this.nleftsucc = nleftsucc;
		this.nrightprec = nrightprec;
		this.nrightsucc = nrightsucc;

		identifier_string = "WordEdges:" + (binned_length ? "1" : "0") + ":"
				+ nleftprec + ":" + nleftsucc + ":" + nrightprec + ":"
				+ nrightsucc;
	}
	@Override
	public void node_featurecount(FeatureClass fc, Vector<SPTreeNode> preterms,
			SPTreeNode node, ParseValAccessor feat_count) {
		if(!node.is_nonterminal())
			return ;
		
		long left = node.label.left;
		long right = node.label.right;
		int nwords = preterms.size();
		
		WordEdgesFeature f = new WordEdgesFeature();
		if(binned_length)
			f.push(symbol_quantize((int)(right - left)));
		f.push(node.label.cat);
		
		for(long i = 1; i <= nleftprec; i++)
			f.push(i <= left ? preterms.get((int)(left - i)).child.label.cat : endmarker());
			
		for(long i = 0; i < nleftsucc; i++)
			f.push(left + i < nwords ? preterms.get((int)(left + i)).child.label.cat : endmarker());
		
		for(long i = 1; i <= nrightprec; i++)
			f.push(i <= right ? preterms.get((int)(right - i)).child.label.cat : endmarker());
		
		for(long i = 0; i < nrightsucc; i++)
			f.push(right + i < nwords ? preterms.get((int)(right + i)).child.label.cat : endmarker());
			
		double v = feat_count.getValueOf(f);
		feat_count.setValueOf(f, v + 1);
	}

	@Override
	String identifier(){
		return identifier_string;
	}
	
	@Override
	public void read_feature(String fs, long id) {
		String tokens[] = fs.substring(1, fs.length() - 1).trim().split("[ ]+");
		
		WordEdgesFeature feature = new WordEdgesFeature();
		for (String token : tokens) {
			Symbol symbol = Symbol.readSymbolFromString(token.trim());
			if(symbol == null)
				return ;
			feature.push(symbol);
		}
		
		feature_id.put(feature, id);
	}
}
