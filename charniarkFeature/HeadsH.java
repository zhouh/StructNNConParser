package nncon.charniarkFeature;

import java.util.HashMap;
import java.util.Vector;

public class HeadsH {
	static public class syntactic_data {
		Vector<Symset> adjective, conjunction, interjection,noun,preposition, unknown, verb;
		HashMap<Symbol, Vector<Symset>> head_type;
		Symset rightheaded_nominals;
		
		static syntactic_data instance = new syntactic_data();
		
		static public syntactic_data getInstance(){
			return instance;
		}
		
		private syntactic_data(){
			adjective = new Vector<Symset>();
			conjunction = new Vector<Symset>();
			interjection = new Vector<Symset>();
			noun = new Vector<Symset>();
			preposition = new Vector<Symset>();
			unknown = new Vector<Symset>();
			verb = new Vector<Symset>();
			
			adjective.setSize(2);
			conjunction.setSize(1);
			interjection.setSize(1);
			noun.setSize(6);
			preposition.setSize(2);
			unknown.setSize(0);
			verb.setSize(4);
			
			rightheaded_nominals = new Symset("NN NNS NNP NNPS $");
			
		    adjective.set(0, new Symset("$ CD JJ JJR JJS RB RBR RBS WRB"));
		    adjective.set(1, new Symset("ADJP ADVP"));
		    conjunction.set(0, new Symset("CC"));
		    interjection.set(0, new Symset("INTJ UH"));
		    noun.set(0, new Symset("POS"));
		    noun.set(1, new Symset("DT WDT WP$ WP PRP EX"));
		    noun.set(2, new Symset("NN NNS"));
		    noun.set(3, new Symset("$ NNP NNPS"));
		    noun.set(4, new Symset("-NONE- QP NP NP$ WHNP"));
		    noun.set(5, new Symset("CD IN JJ JJR JJS PDT RB PP"));
		    preposition.set(0, new Symset("IN RP TO"));
		    preposition.set(1, new Symset("PP"));
		    verb.set(0, new Symset("AUX AUXG MD"));
		    verb.set(1, new Symset("VB VBD VBG VBN VBP VBZ"));
		    verb.set(2, new Symset("VP"));
		    verb.set(3, new Symset("ADJP JJ S SINV SQ TO"));
		    
		    head_type = new HashMap<Symbol, Vector<Symset>>();
		    
		    head_type.put(new Symbol("ADJP"), adjective);
		    head_type.put(new Symbol("ADVP"), verb);
		    head_type.put(new Symbol("CONJP"), conjunction);
		    head_type.put(new Symbol("FRAG"), noun);
		    head_type.put(new Symbol("INTJ"), interjection);
		    head_type.put(new Symbol("LST"), noun);
		    head_type.put(new Symbol("NAC"), noun);
		    head_type.put(new Symbol("NP"), noun);
		    head_type.put(new Symbol("NX"), noun);
		    head_type.put(new Symbol("PP"), preposition);
		    head_type.put(new Symbol("PRN"), noun);
		    head_type.put(new Symbol("PRT"), preposition);
		    head_type.put(new Symbol("QP"), noun);
		    head_type.put(new Symbol("ROOT"), verb);
		    head_type.put(new Symbol("RRC"), verb);
		    head_type.put(new Symbol("S"), verb);
		    head_type.put(new Symbol("SBAR"), verb);
		    head_type.put(new Symbol("SBARQ"), verb);
		    head_type.put(new Symbol("SINV"), verb);
		    head_type.put(new Symbol("SQ"), verb);
		    head_type.put(new Symbol("S1"), verb);
		    head_type.put(new Symbol("UCP"), adjective);
		    head_type.put(new Symbol("VP"), verb);
		    head_type.put(new Symbol("WHADJP"), adjective);
		    head_type.put(new Symbol("WHADVP"), adjective);
		    head_type.put(new Symbol("WHNP"), noun);
		    head_type.put(new Symbol("WHPP"), preposition);
		    head_type.put(new Symbol("X"), unknown);
		}
	    
		//! headchild() returns the head child of node t.
	    //! It is defined in heads.h because it is a template function.
	    //
		public TreeNode headchild(TreeNode t){
			if(t == null || !t.is_nonterminal())
				return null;
			
			if(t.child.label.is_none() && t.child.next == null)
				return t.child;
			
			TreeNode head = null;
			Vector<Symset> se = head_type.get(t.label.cat);
			if(se == null) {
				System.err.println("heads::syntactic_data::headchild() Error: can't find entry for catetory" + t.label.cat.getString() + "\n" + t.toString());
				System.exit(0);
			}
			
			for (Symset cats : se) {
				for(TreeNode child = t.child; child != null; child = child.next) 
					if(cats.contains(child.label.cat)) {
						head = child;
						
						if(se == verb || se == preposition || (se == noun && !rightheaded_nominals.contains(child.label.cat))) break;
					}
				if(head != null)
					return head;
			}
			
			// didn't find a head; return right-most non-punctuation preterminal
			for(TreeNode child = t.child; child != null; child = child.next)
				if(child.is_preterminal() && !child.is_punctuation())
					head = child;
			if(head != null) {
				return head;
			}
			
			// still no head -- return right-most non-punctuation
			for(TreeNode child = t.child; child != null; child = child.next)
				if(!child.is_punctuation())
					head = child;
			
			if(head != null)
				return head;
			
			return null;
		}
		public SPTreeNode headchild(SPTreeNode t){
			if(t == null || !t.is_nonterminal())
				return null;
			
			if(t.child.label.is_none() && t.child.next == null)
				return t.child;
			
			SPTreeNode head = null;
			Vector<Symset> se = head_type.get(t.label.cat);
			if(se == null) {
				System.err.println("heads::syntactic_data::headchild() Error: can't find entry for catetory" + t.label.cat.getString() + "\n" + t.toString());
				System.exit(0);
			}
			
			for (Symset cats : se) {
				for(SPTreeNode child = t.child; child != null; child = child.next) 
					if(cats.contains(child.label.cat)) {
						head = child;
						
						if(se == verb || se == preposition || (se == noun && !rightheaded_nominals.contains(child.label.cat))) break;
					}
					
				if(head != null)
					return head;
			}
			
			// didn't find a head; return right-most non-punctuation preterminal
			for(SPTreeNode child = t.child; child != null; child = child.next)
				if(child.is_preterminal() && !child.is_punctuation())
					head = child;
			if(head != null) {
				return head;
			}
			
			// still no head -- return right-most non-punctuation
			for(SPTreeNode child = t.child; child != null; child = child.next)
				if(!child.is_punctuation())
					head = child;
			
			if(head != null)
				return head;
			
			return null;
		}
	}

	static public class semantic_data {
		Vector<Symset> adjective, conjunction, interjection, noun, preposition, unknown, verb;
		HashMap<Symbol, Vector<Symset>> head_type;
		
		static semantic_data instance = new semantic_data();
		
		static public semantic_data getInstance() {
			return instance;
		}
		
		private semantic_data() {
			adjective = new Vector<Symset>();
			conjunction = new Vector<Symset>();
			interjection = new Vector<Symset>();
			noun = new Vector<Symset>();
			preposition = new Vector<Symset>();
			unknown = new Vector<Symset>();
			verb = new Vector<Symset>();
			head_type = new HashMap<Symbol, Vector<Symset>>();
			
			adjective.setSize(2);
			conjunction.setSize(1);
			interjection.setSize(1);
			noun.setSize(4);
			preposition.setSize(2);
			unknown.setSize(0);
			verb.setSize(4);
			
		    adjective.set(0, new Symset("$ CD JJ JJR JJS RB RBR RBS WRB"));
		    adjective.set(1, new Symset("ADJP ADVP"));
		    conjunction.set(0, new Symset("CC"));
		    interjection.set(0, new Symset("INTJ UH"));
		    noun.set(0, new Symset("EX NN NNS PRP WP"));
		    noun.set(1, new Symset("$ NNP NNPS"));
		    noun.set(2, new Symset("QP NP WP$"));
		    noun.set(3, new Symset("CD DT IN JJ JJR JJS PDT POS RB WDT"));
		    preposition.set(0, new Symset("IN RP TO"));
		    preposition.set(1, new Symset("PP"));
		    verb.set(0, new Symset("VP"));
		    verb.set(1, new Symset("VB VBD VBG VBN VBP VBZ"));
		    verb.set(2, new Symset("ADJP JJ S SINV SQ TO"));
		    verb.set(3, new Symset("AUX AUXG MD"));
		    
		    head_type.put(new Symbol("ADJP"), adjective);
		    head_type.put(new Symbol("ADVP"), verb);
		    head_type.put(new Symbol("CONJP"), conjunction);
		    head_type.put(new Symbol("FRAG"), noun);
		    head_type.put(new Symbol("INTJ"), interjection);
		    head_type.put(new Symbol("LST"), noun);
		    head_type.put(new Symbol("NAC"), noun);
		    head_type.put(new Symbol("NP"), noun);
		    head_type.put(new Symbol("NX"), noun);
		    head_type.put(new Symbol("PP"), preposition);
		    head_type.put(new Symbol("PRN"), noun);
		    head_type.put(new Symbol("PRT"), preposition);
		    head_type.put(new Symbol("QP"), noun);
		    head_type.put(new Symbol("ROOT"), verb);
		    head_type.put(new Symbol("RRC"), verb);
		    head_type.put(new Symbol("S"), verb);
		    head_type.put(new Symbol("SBAR"), verb);
		    head_type.put(new Symbol("SBARQ"), verb);
		    head_type.put(new Symbol("SINV"), verb);
		    head_type.put(new Symbol("SQ"), verb);
		    head_type.put(new Symbol("S1"), verb);
		    head_type.put(new Symbol("UCP"), adjective);
		    head_type.put(new Symbol("VP"), verb);
		    head_type.put(new Symbol("WHADJP"), adjective);
		    head_type.put(new Symbol("WHADVP"), adjective);
		    head_type.put(new Symbol("WHNP"), noun);
		    head_type.put(new Symbol("WHPP"), preposition);
		    head_type.put(new Symbol("X"), unknown);
		}
		
		public TreeNode headchild(TreeNode t) {
			if(t == null || !t.is_nonterminal()) 
				return null;
			if(t.child.label.is_none() && t.child.next == null)
				return t.child;
			
			TreeNode head = null;
			Vector<Symset> it = head_type.get(t.label.cat);
			
			if(it == null) {
				System.err.println("heads::semantic_data::headchild() Error: can't find entry for category " + t.label.cat.getString() + "\n" + t.toString());
				System.exit(0);
			}
			
			Vector<Symset> type = it;
			for (Symset cats : type) {
				for(TreeNode child = t.child; child != null; child = child.next){
					if(cats.contains(child.label.cat)) {
						head = child;
						if(type == verb || type == preposition)
							break;
					}
				}
				if(head != null)
					return head;
			}
			
			for(TreeNode child = t.child; child != null; child = child.next)
				if(child.is_preterminal() && !child.is_punctuation())
					head = child;
			if(head != null)
				return head;
			
			for(TreeNode child = t.child; child != null; child = child.next)
				if(!child.is_punctuation())
					head = child;
			if(head != null)
				return head;
			
			return null;
		}
		public SPTreeNode headchild(SPTreeNode t) {
			if(t == null || !t.is_nonterminal()) 
				return null;
			if(t.child.label.is_none() && t.child.next == null)
				return t.child;
			
			SPTreeNode head = null;
			Vector<Symset> it = head_type.get(t.label.cat);
			
			if(it == null) {
				System.err.println("heads::semantic_data::headchild() Error: can't find entry for category " + t.label.cat.getString() + "\n" + t.toString());
				System.exit(0);
			}
			
			Vector<Symset> type = it;
			for (Symset cats : type) {
				for(SPTreeNode child = t.child; child != null; child = child.next){
					if(cats.contains(child.label.cat)) {
						head = child;
						if(type == verb || type == preposition)
							break;
					}
				}
				if(head != null)
					return head;
			}
			
			for(SPTreeNode child = t.child; child != null; child = child.next)
				if(child.is_preterminal() && !child.is_punctuation())
					head = child;
			if(head != null)
				return head;
			
			for(SPTreeNode child = t.child; child != null; child = child.next)
				if(!child.is_punctuation())
					head = child;
			if(head != null)
				return head;
			
			return null;
		}
	}
	
	//! tree_syntacticHeadChild() returns a pointer to the
	//!  child of t that is the syntactic head of t, or NULL
	//!  if none exists (e.g., if t is a preterminal).
	//
	static TreeNode tree_syntacticHeadChild(TreeNode t) {
		return syntactic_data.getInstance().headchild(t);
	}
	static SPTreeNode tree_syntacticHeadChild(SPTreeNode t) {
		return syntactic_data.getInstance().headchild(t);
	}
	
	//! tree_syntacticLexicalHead() returns a pointer to a
	//!  preterminal node dominated by t that is its lexical
	//! syntactic head.  Note that this node may be empty!
	//
	static TreeNode tree_syntacticLexicalHead(TreeNode t) {
		TreeNode head;
		while((head = tree_syntacticHeadChild(t)) != null)
			t = head;
		return t;
	}
	static SPTreeNode tree_syntacticLexicalHead(SPTreeNode t) {
		SPTreeNode head;
		while((head = tree_syntacticHeadChild(t)) != null)
			t = head;
		return t;
	}

	//! tree_semanticHeadChild() returns a pointer to the
	//!  child of t that is the semantic head of t, or NULL
	//!  if none exists (e.g., if t is a preterminal).
	//
	static TreeNode tree_semanticHeadChild(TreeNode t) {
		return semantic_data.getInstance().headchild(t);
	}
	static SPTreeNode tree_semanticHeadChild(SPTreeNode t) {
		return semantic_data.getInstance().headchild(t);
	}
	
	//! tree_semanticLexicalHead() returns a pointer to a
	//!  preterminal node dominated by t that is its lexical
	//!  semantic head.  Note that this node may be empty!
	//
	static TreeNode tree_semanticLexicalHead(TreeNode t) {
		TreeNode head;
		while((head = tree_semanticHeadChild(t)) != null)
			t = head;
		return t;
	}
	static SPTreeNode tree_semanticLexicalHead(SPTreeNode t) {
		SPTreeNode head;
		while((head = tree_semanticHeadChild(t)) != null)
			t = head;
		return t;
	}
}
