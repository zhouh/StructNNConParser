package nncon.charniarkFeature;

import java.util.Vector;

//! A RuleFeatureClass is an ABC for classes of features
//! with rule-like features
//!
public abstract class RuleFeatureClass extends NodeFeatureClass {
	enum annotation_level { none, pos, lexical }
	
	int nanccats;
	boolean label_root;
	boolean label_conjunct;
	annotation_level head; 		 //annocation on rule's head
	annotation_level functional; //annotation on (projections of) functional categories
	annotation_level all;		 //annotation on all words
	annotation_type type; 		 //syntactic or semantic annotation
	
	annotation_level max_annotation_level;

	public RuleFeatureClass(
			String identifier_stem, //stem of identifier
			int nanccats, 			//number of ancestor categories above trees
			boolean label_root,     //annotate with "in root context"
			boolean label_conjunct, //annotate with "belongs to conjunction"
			annotation_level head,  //amount of head annotation
			annotation_level functional, //amount of function word annotation
			annotation_level all,   //amount of lexical word word annotation
			annotation_type type   //head type
			) {
		super();
		
		this.nanccats = nanccats;
		this.label_root = label_root;
		this.label_conjunct = label_conjunct;
		this.head = head;
		this.functional = functional;
		this.all = all;
		this.type = type;
		this.max_annotation_level = int_2_level(Math.max(level_2_int(this.head), Math.max(level_2_int(this.functional), level_2_int(this.all))));
		
		StringBuilder s = new StringBuilder("");
		
		s.append(identifier_stem);
		s.append(":");
		s.append(this.nanccats + ":");
		s.append((this.label_root ? "1" : "0") + ":");
		s.append((this.label_conjunct ? "1" : "0") + ":");
		s.append(level_2_int(this.head) + ":");
		s.append(level_2_int(this.functional) + ":");
		s.append(level_2_int(this.all) + ":");
		s.append(type_2_int(this.type));
		
		this.identifier_string = s.toString();
	}
	
	static annotation_level int_2_level(int i) {
		if(i == 0) return annotation_level.none;
		else if(i == 1) return annotation_level.pos;
		else return annotation_level.lexical;
	}
	
	static int level_2_int(annotation_level l){
		if(l == annotation_level.none) return 0;
		else if(l == annotation_level.pos) return 1;
		else return 2;
	}
	
	static boolean lessthan(annotation_level l1, annotation_level l2) {
		if(level_2_int(l1) < level_2_int(l2)) return true;
		return false;
	}
	
	static class RuleFeature extends Feature {
		Vector<Symbol> feature;
		
		public RuleFeature () {
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
			
			RuleFeature f = ((RuleFeature)obj);
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

	static class annotation_level_wrapper{
		annotation_level value;
		public annotation_level_wrapper(annotation_level value) {
			this.value = value;
		}
	}
	
	@Override
	public void read_feature(String fs, long id) {
		String tokens[] = fs.substring(1, fs.length() - 1).trim().split("[ ]+");
		
		RuleFeature feature = new RuleFeature();
		for (String token : tokens) {
			Symbol symbol = Symbol.readSymbolFromString(token.trim());
			if(symbol == null)
				return ;
			feature.push(symbol);
		}
		
		feature_id.put(feature, id);
	}
	
	//! push_child_features() pushes the features for this child node
	//
	void push_child_features(final SPTreeNode node, final SPTreeNode parent, RuleFeature f, annotation_level_wrapper highest_level) {
		final SPTreeNode parent_headchild = (type == annotation_type.semantic ? parent.label.semantic_headchild : parent.label.syntactic_headchild);
		boolean is_headchild = (node == parent_headchild) ? true : false;
		
		final SPTreeLabel label = node.label;
		f.push(label.cat);
		
		final SPTreeNode lexhead = (type == annotation_type.semantic ? label.semantic_lexhead : label.syntactic_lexhead);
		if(lexhead == null) return ;
		
		if(lessthan(all, annotation_level.pos) 
				&& (!lexhead.is_functional() || lessthan(functional, annotation_level.pos)) 
				 && (!is_headchild || lessthan(head, annotation_level.pos)))
			return ;
		 if(lexhead != node) {
			 f.push(headmarker());
			 f.push(lexhead.label.cat);
			 highest_level.value = int_2_level(Math.max(level_2_int(highest_level.value), level_2_int(annotation_level.pos)));
		 }
		 
		 if(lessthan(all, annotation_level.lexical)
				 && (!lexhead.is_functional() || lessthan(functional, annotation_level.lexical))
				 && (!is_headchild || lessthan(head, annotation_level.lexical)))
			 return ;
		 f.push(lexhead.child.label.cat);
		 highest_level.value = int_2_level(Math.max(level_2_int(highest_level.value), level_2_int(annotation_level.lexical)));
	}
	
	void push_ancestor_features(SPTreeNode node, RuleFeature f) {
		f.push(endmarker());
		
		SPTreeNode parent = node.label.parent;
		
		for(int i = 0; i <= nanccats && parent != null; i++){
			f.push(node.label.cat);
			if(label_conjunct && parent != null){
				if(parent.is_coordination()) {
					f.push(parent.is_last_nonpunctuation() ? lastconjunctmarker() : conjunctmarker());
				} else if(parent.is_adjunction()){
					f.push(parent.is_last_nonpunctuation() ? lastadjunctmarker() : adjunctmarker());
				}
			}
			node = parent;
			parent = node.label.parent;
		}
		
		if(label_root) {
			for(node = parent; node != null; node = node.label.parent) {
				if(is_bounding_node(node) && !is_bounding_node(node.label.parent)) {
					f.push(nonrootmarker());
					break;
				}
			}
		}
	}
}