package nncon.charniarkFeature;

import java.util.Vector;

//! NGramTree identifies n-gram tree fragments.  The identifier is
//!
//! NGramTree:ngram:lexicalize:collapse:nancs
//
public class NGramTree extends TreeFeatureClass{
	static class NGramTreeFeature extends Feature {
		String feature;
		
		public NGramTreeFeature() {
			feature = new String();
		}
		
		public NGramTreeFeature(String str) {
			this.feature = str;
		}
		
		@Override
		public int hashCode() {
			long h = 0;
			long g = 0;
			
			for(int i = 0; i < feature.length(); i++){
				char ch = feature.charAt(i);
				
				h = (h << 4) + (long)ch;
				
				if((g = h & 0xf0000000) != 0) {
					h = h ^ (g >> 24);
					h = h ^ g;
				}
			}
			
			return (int)h;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(this.getClass() != obj.getClass())
				return false;
			
			NGramTreeFeature f = (NGramTreeFeature)obj;
			
			return feature.equals(f.feature);
		}
		
		@Override
		public String toString(){
			return Symbol.readStringFromFeatureString(feature);
		}
	}
	
	enum lexicalize_type { none, closed_class, functional, all};
	
	int ngram;					//!< # of words in context
	lexicalize_type lexicalize;	//!< lexicalize preterminals
	boolean collapse;			//!< collapse nodes not dominating ngram
	int nancs;					//!< extra ancestors to go up
	
	static int ltype_2_int(lexicalize_type type) {
		if(type == lexicalize_type.none) return 0;
		else if(type == lexicalize_type.closed_class) return 1;
		else if(type == lexicalize_type.functional) return 2;
		else return 3;
	}
	static lexicalize_type int_2_ltype(int i) {
		if(i == 0) return lexicalize_type.none;
		else if(i == 1) return lexicalize_type.closed_class;
		else if(i == 2) return lexicalize_type.functional;
		else return lexicalize_type.all;
	}
	
	public NGramTree(int ngram, lexicalize_type lexicalize, boolean collapse) {
		this(ngram, lexicalize, collapse, 0);
	}
	
	public NGramTree(int ngram, lexicalize_type lexicalize, boolean collapse, int nancs) {
		super();
		this.ngram = ngram;
		this.lexicalize = lexicalize;
		this.collapse = collapse;
		this.nancs = nancs;
		this.identifier_string = "NGramTree:" + ngram + ":" + ltype_2_int(lexicalize) + ":" + (collapse ? "1" : "0") + ":" + nancs;
	}
	
	private TreeNode selective_copy(SPTreeNode sp, long left, long right) {
		return selective_copy(sp, left, right, false);
	}
	
	private TreeNode selective_copy(SPTreeNode sp, long left, long right, boolean copy_next) {
		SPTreeLabel label = sp.label;
		
		if(collapse) {
			if(label.right <= left)
				return (sp.next != null && copy_next) ? selective_copy(sp.next, left, right, copy_next) : null;
			else if(label.left >= right)
				return null;
		}
		
		TreeNode t = new TreeNode(label);
		
		if(sp.child != null 
				&& label.left < right 
				&& label.right > left 
				&& (sp.is_nonterminal() || lexicalize == lexicalize_type.all || (lexicalize == lexicalize_type.functional && sp.is_functional()) || (lexicalize == lexicalize_type.closed_class && sp.is_closed_class())))
			t.child = selective_copy(sp.child, left, right, true);
			
		if(copy_next && sp.next != null)
			t.next = selective_copy(sp.next, left, right, copy_next);
		return t;
	}
	
	@Override
	public void tree_featurecount(FeatureClass fc, SPTreeNode root,
			ParseValAccessor feat_count) {
		Vector<SPTreeNode> preterms = new Vector<SPTreeNode>();
		
		root.preterminal_nodes(preterms);
		for(int i = 0; i + ngram < preterms.size(); i++){
			SPTreeNode t0 = null;
			for(t0 = preterms.get(i); t0 != null && t0.label.right < i + ngram; t0 = t0.label.parent) 
				;
			assert(t0 != null);
			for(int ianc = 0; ianc < nancs && t0 != null; ianc++)
				t0 = t0.label.parent;
			
			if(t0 == null) return  ;
			
			TreeNode frag = selective_copy(t0, i, i + ngram);
			
			NGramTreeFeature feat = new NGramTreeFeature(frag.toString());
			
			double v = feat_count.getValueOf(feat);
			feat_count.setValueOf(feat, v + 1);
			
		}
	}

	@Override
	String identifier() {
		return identifier_string;
	}
	@Override
	public void read_feature(String fs, long id) {
//		System.out.println(fs.trim());
		String vs = Symbol.readFeatureStringFromString(fs.trim());
		
		NGramTreeFeature feature = new NGramTreeFeature(vs);
		
		feature_id.put(feature, id);
	}
}
