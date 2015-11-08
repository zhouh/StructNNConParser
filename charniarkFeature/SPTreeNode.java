package nncon.charniarkFeature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

public class SPTreeNode {
	SPTreeLabel label;

	public SPTreeNode child; // subtrees
	public SPTreeNode next; // sibling

	public SPTreeNode() {
		label = new SPTreeLabel();
		child = null;
		next = null;
	}

	public SPTreeNode(SPTreeLabel label) {
		this(label, null, null);
	}

	public SPTreeNode(SPTreeLabel label, SPTreeNode child, SPTreeNode next) {
		this.label = label;
		this.child = child;
		this.next = next;
	}

	public SPTreeNode(Symbol cat) {
		this(cat, null, null);
	}

	public SPTreeNode(Symbol cat, SPTreeNode child, SPTreeNode next) {
		this.label = new SPTreeLabel(cat);
		this.child = child;
		this.next = next;
	}

	/**
	 * The TreeNode(TreeNode node) copy constructors makes "deep" copies.
	 * 
	 * @param node
	 */
	public SPTreeNode(SPTreeNode node) {
		this.label = new SPTreeLabel(node.label);

		if (node.child != null) {
			this.child = new SPTreeNode(node.child);
		} else {
			node.child = null;
		}

		if (node.next != null) {
			this.next = new SPTreeNode(node.next);
		} else {
			node.next = null;
		}
	}

	/**
	 * Equlity looks at the entire tree, not just this node.
	 * 
	 * @param t
	 * @return
	 */
	public boolean equalTo(SPTreeNode t) {
		if (this == t)
			return true;
		if (!label.equalTo(t.label))
			return false;

		return ((child.equalTo(t.child) || (child != null && t.child != null && child
				.equalTo(t.child))) && (next.equalTo(t.next) || (next != null
				&& t.next != null && next.equalTo(t.next))));
	}

	public boolean lessThan(SPTreeNode t) {
		if (this == t)
			return false;
		if (label.lessThan(t.label))
			return true;
		if (t.label.lessThan(label))
			return false;
		// label == t.label
		if (child.equalTo(t.child))
			return false;
		if (child == null)
			return true;
		if (t.child == null)
			return false;
		if (child.lessThan(t.child))
			return true;
		if (t.child.lessThan(child))
			return false;
		// child == t.child
		if (next.equalTo(t.next))
			return false;
		if (next == null)
			return true;
		if (t.next == null)
			return false;
		return next.lessThan(t.next);
	}

	public boolean is_termminal() {
		return child == null;
	}

	public boolean is_preterminal() {
		return (child != null) && child.is_termminal();
	}

	public boolean is_nonterminal() {
		return (child != null) && (child.child != null);
	}

	public boolean is_root() {
		return is_nonterminal() && label.is_root();
	}

	public boolean is_none() {
		return is_preterminal() && label.is_none();
	}

	public boolean is_punctuation() {
		return is_preterminal() && label.is_punctuation();
	}

	public boolean is_conjunction() {
		return is_preterminal() && label.is_conjunction();
	}

	public boolean is_closed_class() {
		return is_preterminal() && label.is_closed_class();
	}

	public boolean is_functional() {
		return is_preterminal() && label.is_functional();
	}

	void delete_this() {
		child = next = null;
	}

	/**
	 * copy_tree() returns a pointer to a ``deep copy'' of this node and all of
	 * the nodes it points to. Modified so that it defaults to the copy_tree()
	 * function (which can be overridden).
	 */
	public SPTreeNode copy_tree() {
		return copy_treeptr(this);
	}

	// public static SPTreeNode copy_treeptr(SPTreeNode tp) {
	// if (tp == null)
	// return null;
	//
	// SPTreeNode t = new SPTreeNode(tp.label, copy_treeptr(tp.child),
	// copy_treeptr(tp.next));
	//
	// return t;
	// }

	/**
	 * exists_cut_p() is true if there is a cut through this tree such that
	 * every node satsifies check()
	 * 
	 * @param p
	 * @return
	 */
	public boolean exists_cut_p(SPTreePropertyChecker p) {
		if (p.check(this))
			return true;

		if (child == null)
			return false;

		for (SPTreeNode c = child; c != null; c = c.next)
			if (!c.exists_cut_p(p))
				return false;

		return true;
	}

	/**
	 * returns the first ndoe for which pred is true
	 * 
	 * @param args
	 */
	public SPTreeNode preorder_find(SPPredicater pred) {
		if (pred.check(this))
			return this;
		if (child != null) {
			SPTreeNode rc = child.preorder_find(pred);
			if (rc != null)
				return rc;
		}
		if (next != null) {
			SPTreeNode rn = next.preorder_find(pred);
			if (rn != null)
				return rn;
		}

		return null;
	}

	/**
	 * counts the number of nodes on which pred is true
	 * 
	 * @param args
	 */
	public long count(SPPredicater pred) {
		return count(pred, 0);
	}

	public long count(SPPredicater pred, long cnt) {
		if (pred.check(this))
			++cnt;

		if (child != null)
			cnt = child.count(pred, cnt);

		return (next != null) ? next.count(pred, cnt) : cnt;
	}

	private long size_helper(long n) {
		++n;
		if (child != null)
			n = child.size_helper(n);

		return (next != null) ? next.size_helper(n) : n;
	}

	public long size() {
		return size_helper(0);
	}

	/**
	 * max_depth() returns the length of the longest path of nodes to a
	 * terminal. All siblings are at same depth, children are at depth 1, etc.
	 * 
	 * @return
	 */
	public long max_depth() {
		return Math.max((child != null) ? 1 + child.max_depth() : 0,
				(next != null) ? next.max_depth() : 0);
	}

	/**
	 * is_empty() is true iff all terminals have POS -NONE-
	 * 
	 * @param args
	 */
	public boolean is_empty() {
		if (label.is_none())
			return true;
		if (child == null)
			return false;
		for (SPTreeNode c = child; c != null; c = c.next)
			if (!c.is_empty())
				return false;
		return true;
	}

	// ! is_coordination() is true iff one of the non-first, non-last children
	// ! is a conjunction (the first and last child are ignored so constructions
	// ! beginning with a conjunction don't count as coordinated).
	//
	public boolean is_coordination() {
		if (!is_nonterminal())
			return false;
		SPTreeNode c = child;
		if (c != null && c.next != null)
			for (c = c.next; c.next != null; c = c.next)
				if (c.next != null && c.is_conjunction())
					return true;
		return false;
	}

	// ! is_adjunction() is true iff all of the non-punctuation children
	// ! have the same label as the parent.
	//
	public boolean is_adjunction() {
		if (!is_nonterminal())
			return false;
		for (SPTreeNode c = child; c != null; c = c.next)
			if (c.label.cat.notEqualTo(label.cat) && !c.is_punctuation())
				return false;
		return true;
	}

	// ! is_last_nonpunctuation() is true iff all right siblings are
	// ! punctuation.
	//
	public boolean is_last_nonpunctuation() {
		SPTreeNode sibling = next;
		while (sibling != null && sibling.is_punctuation())
			sibling = sibling.next;

		return sibling == null;
	}

	// ! is_adjunction_site() is true iff all but one child is empty(), and
	// ! that child has the same label as this node.
	//
	public boolean is_adjunction_site() {
		if (!is_nonterminal())
			return false;

		assert (child != null);

		@SuppressWarnings("unused")
		int non_empty_same = 0, empty = 0;

		for (SPTreeNode c = child; c != null; c = c.next) {
			if (c.is_empty())
				++empty;
			else {
				if (c.label.cat.notEqualTo(label.cat))
					return false;
				else {
					if (non_empty_same == 0)
						++non_empty_same;
					else
						return false;
				}
			}
		}

		return non_empty_same == 1;
	}

	// ! delete_unary_same_label_chains() excises and deletes nodes with
	// ! repeated labels in unary chains.
	//
	public void delete_unary_same_label_chains() {
		if (child != null) {
			while (child.child != null && child.next == null
					&& child.label.cat.equalTo(label.cat)) {
				SPTreeNode grandchild = child.child;
				child.delete_this();
				child = grandchild;
			}
		}
		if (next != null)
			next.delete_unary_same_label_chains();
	}

	// ! copy_without_empties() returns a ptr to a copy of this subtree with all
	// ! empty nodes deleted.
	//
	public SPTreeNode copy_without_empties() {
		return copy_without_empties(false, null);
	}

	public SPTreeNode copy_without_empties(boolean delete_adjunctions,
			SPTreeNode endp) {
		if (is_empty())
			return (next != null) ? next.copy_without_empties(
					delete_adjunctions, endp) : endp;
		else if (delete_adjunctions && is_adjunction_site()) {
			assert (child != null);

			return child.copy_without_empties(
					delete_adjunctions,
					(next != null) ? next.copy_without_empties(
							delete_adjunctions, endp) : endp);
		} else {
			SPTreeNode c = new SPTreeNode(label,
					(child != null) ? child.copy_without_empties(
							delete_adjunctions, endp) : null,
					(next != null) ? next.copy_without_empties(
							delete_adjunctions, endp) : endp);

			return c;
		}
	}

	// ! copy_left_binarize() returns a pointer to a left binarized copy of
	// ! this tree.
	//
	public SPTreeNode copy_left_binarize() {
		SPTreeNode next_copy = null;
		if (next != null) {
			next_copy = next.copy_left_binarize();
			if (next_copy.next != null)
				next_copy = new SPTreeNode(new SPTreeLabel(), next_copy, null);
		}

		return new SPTreeNode(label,
				(child != null) ? child.copy_left_binarize() : null, next_copy);
	}

	// ! copy_parent_annotate() returns a pointer to a parent-annotated
	// ! copy of this tree.
	//
	public SPTreeNode copy_parent_annotate() {
		return copy_parent_annotate(Symbol.undefined());
	}

	public SPTreeNode copy_parent_annotate(Symbol parent_cat) {
		SPTreeNode t = new SPTreeNode(label);
		assert (child != null);

		if (is_preterminal() || is_empty())
			t.child = child.copy_tree();
		else {
			t.child = child.copy_parent_annotate(label.cat);
			if (parent_cat.is_defined())
				t.label.cat = new Symbol(label.cat.getString() + " "
						+ parent_cat.getString());
		}
		if (next != null)
			t.next = next.copy_parent_annotate(parent_cat);

		return t;
	}

	// ! preorder() calls the function object fn() on every subtree
	// ! in a preorder traversal.
	//
	public void preorder(SPTreeProcesser fn) {
		fn.process(this);

		if (child != null)
			child.preorder(fn);
		if (next != null)
			next.preorder(fn);
	}

	// ! postorder() calls the function object fn() on every subtree
	// ! in a postorder traversal.
	//
	public void postorder(SPTreeProcesser fn) {
		if (child != null)
			child.postorder(fn);
		fn.process(this);
		if (next != null)
			next.postorder(fn);
	}

	// ! find() returns the first node in a preorder traversal
	// ! for which the function object fn() is true.
	//
	public boolean find(SPTreeProcesser fn) {
		return fn.process(this) || (child != null && child.find(fn))
				|| (next != null && next.find(fn));
	}

	private void preorder_ancestors(SPTreeAncestorsProcessor fn,
			Vector<SPTreeNode> ancestors) {
		ancestors.add(this);
		fn.process(ancestors);

		if (child != null)
			child.preorder_ancestors(fn, ancestors);
		ancestors.remove(ancestors.size() - 1);
		if (next != null)
			next.preorder_ancestors(fn, ancestors);
	}

	// ! preorder_ancestors() calls fn(as) where as.back() is the
	// ! node being visited, as[0] is the root node and as[i-1] is
	// ! the parent of as[i].
	//
	void preorder_ancestors(SPTreeAncestorsProcessor fn) {
		Vector<SPTreeNode> ancestors = new Vector<SPTreeNode>();
		preorder_ancestors(fn, ancestors);
		assert (ancestors.isEmpty());
	}

	// ! terminals() appends the terminals to terms, optionally skipping empty
	// nodes
	//
	// void terminals(Vector<Object> terms) {
	// terminals(terms, false);
	// }
	// void terminals(Vector<Object> terms, boolean include_empty){
	// if(is_termminal())
	// terms.add(label.cat);
	// else {
	// if(!label.is_none() || include_empty) {
	// child.terminals(terms, include_empty);
	// }
	// }
	//
	// if(next != null)
	// next.terminals(terms, include_empty);
	// }
	void terminals(Vector<Symbol> terms) {
		terminals(terms, false);
	}

	void terminals(Vector<Symbol> terms, boolean include_empty) {
		if (is_termminal())
			terms.add(label.cat);
		else {
			if (!label.is_none() || include_empty) {
				child.terminals(terms, include_empty);
			}
		}

		if (next != null)
			next.terminals(terms, include_empty);
	}

	// ! preterminals() appends the preterminal categories to preterms,
	// ! optionally skipping empty nodes
	//
//	void preterminals(Vector<SPTreeNode> preterms) {
//		preterminals(preterms, false);
//	}
//
//	void preterminals(Vector<SPTreeNode> preterms, boolean include_empty) {
//		assert (child != null);
//
//		if (is_preterminal()) {
//			if (!label.is_none() || include_empty)
//				preterms.add(label.cat);
//		} else {
//			child.preterminals(preterms, include_empty);
//		}
//
//		if (next != null)
//			next.preterminals(preterms, include_empty);
//	}
	void preterminals(Vector<Object> preterms) {
		preterminals(preterms, false);
	}

	void preterminals(Vector<Object> preterms, boolean include_empty) {
		assert (child != null);

		if (is_preterminal()) {
			if (!label.is_none() || include_empty)
				preterms.add(label.cat);
		} else {
			child.preterminals(preterms, include_empty);
		}

		if (next != null)
			next.preterminals(preterms, include_empty);
	}

	// ! preterminal_nodes() appends the preterminal nodes to preterms,
	// ! optionally skipping empty nodes
	//
	void preterminal_nodes(Vector<SPTreeNode> preterms) {
		preterminal_nodes(preterms, false);
	}

	void preterminal_nodes(Vector<SPTreeNode> preterms, boolean include_empty) {
		assert (child != null);

		if (is_preterminal()) {
			if (!label.is_none() || include_empty)
				preterms.add(this);
		} else {
			child.preterminal_nodes(preterms, include_empty);
		}

		if (next != null)
			next.preterminal_nodes(preterms, include_empty);
	}
//	void preterminal_nodes(Vector<Object> preterms) {
//		preterminal_nodes(preterms, false);
//	}
//
//	void preterminal_nodes(Vector<Object> preterms, boolean include_empty) {
//		assert (child != null);
//
//		if (is_preterminal()) {
//			if (!label.is_none() || include_empty)
//				preterms.add(this);
//		} else {
//			child.preterminal_nodes(preterms, include_empty);
//		}
//
//		if (next != null)
//			next.preterminals(preterms, include_empty);
//	}

	static public Symbol downcase(Symbol cat) {
		String s = cat.getString();

		return new Symbol(s.toLowerCase());
	}
	
	static class LongWrapper{
		long position;
		
		public LongWrapper(long position) {
			this.position = position;
		}
	}

	static public SPTreeNode tree_sptree_helper(boolean downcase_flag,
			SPTreeNode tp0, SPTreeNode parent, SPTreeNode previous,
			LongWrapper position) {
		SPTreeNode tp = new SPTreeNode(
				(downcase_flag && tp0.is_termminal()) ? downcase(tp0.label.cat)
						: tp0.label.cat);

		tp.label.left = position.position;
		tp.label.parent = parent;
		tp.label.previous = previous;

		if (tp0.child == null) {
			tp.child = null;
			++position.position;
		} else {
			tp.child = tree_sptree_helper(downcase_flag, tp0.child, tp, null,
					position);
		}

		tp.label.right = position.position;

		if (tp0.next == null)
			tp.next = null;
		else {
			tp.next = tree_sptree_helper(downcase_flag, tp0.next, parent, tp,
					position);
		}

		SPTreeLabel label = tp.label;

		if (tp.is_nonterminal()) {
			label.syntactic_headchild = HeadsH.tree_syntacticHeadChild(tp);
			label.syntactic_lexhead = (label.syntactic_headchild == null ? null
					: label.syntactic_headchild.label.syntactic_lexhead);

			label.semantic_headchild = HeadsH.tree_semanticHeadChild(tp);
			label.semantic_lexhead = (label.semantic_headchild == null ? null
					: label.semantic_headchild.label.semantic_lexhead);
		} else {
			label.syntactic_headchild = null;
			label.semantic_headchild = null;
			label.syntactic_lexhead = (tp.is_termminal() ? null : tp);
			label.semantic_lexhead = (tp.is_termminal() ? null : tp);
		}

		return tp;
	}

	static public SPTreeNode tree_sptree_helper(boolean downcase_flag,
			TreeNode tp0, SPTreeNode parent, SPTreeNode previous, LongWrapper position) {
		SPTreeNode tp = new SPTreeNode(
				(downcase_flag && tp0.is_termminal()) ? downcase(tp0.label.cat)
						: tp0.label.cat);

		tp.label.left = position.position;
		tp.label.parent = parent;
		tp.label.previous = previous;

		if (tp0.child == null) {
			tp.child = null;
			++position.position;
		} else {
			tp.child = tree_sptree_helper(downcase_flag, tp0.child, tp, null,
					position);
		}

		// if(tp.label.cat.getString().equals("goodman")) {
		// System.err.println("equal goodman");
		// }
		tp.label.right = position.position;

		if (tp0.next == null)
			tp.next = null;
		else {
			tp.next = tree_sptree_helper(downcase_flag, tp0.next, parent, tp,
					position);
		}

		SPTreeLabel label = tp.label;

		if (tp.is_nonterminal()) {
			label.syntactic_headchild = HeadsH.tree_syntacticHeadChild(tp);
			label.syntactic_lexhead = label.syntactic_headchild == null ? null
					: label.syntactic_headchild.label.syntactic_lexhead;

			label.semantic_headchild = HeadsH.tree_semanticHeadChild(tp);
			label.semantic_lexhead = label.semantic_headchild == null ? null
					: label.semantic_headchild.label.semantic_lexhead;
		} else {
			label.syntactic_headchild = label.semantic_headchild = null;
			label.syntactic_lexhead = label.semantic_lexhead = tp
					.is_termminal() ? null : tp;
		}

		return tp;
	}

	// ! tree_sptree() maps a standard tree to an sptree. This does
	// ! not free tp.
	//
	static public SPTreeNode tree_sptree(TreeNode tp) {
		LongWrapper position = new LongWrapper(0);
		return tree_sptree_helper(false, tp, null, null, position);
	}

	static public SPTreeNode tree_sptree(TreeNode tp, boolean downcase_flag) {
		LongWrapper position = new LongWrapper(0);
		return tree_sptree_helper(downcase_flag, tp, null, null, position);
	}

	static public SPTreeNode tree_sptree(SPTreeNode tp) {
		LongWrapper position = new LongWrapper(0);
		return tree_sptree_helper(false, tp, null, null, position);
	}

	static public SPTreeNode tree_sptree(SPTreeNode tp, boolean downcase_flag) {
		LongWrapper position = new LongWrapper(0);
		return tree_sptree_helper(downcase_flag, tp, null, null, position);
	}

	static public SPTreeNode copy_treeptr(SPTreeNode tp) {
		return tree_sptree(tp);
	}

	static public String display_tree(SPTreeNode t) {
		return display_tree(t, 0);
	}

	static public String display_tree(SPTreeNode t, int indent) {
		if (t == null)
			return "";
		StringBuilder res = new StringBuilder("");
		if (t.child != null) {
			res.append("(" + t.label.toString());
			indent += t.label.toString().length() + 2;

			res.append(display_tree(t.child, indent));

			for (SPTreeNode p = t.child.next; p != null; p = p.next) {
				res.append('\n');
				for (int i = 0; i < indent; i++)
					res.append('-');
				res.append(display_tree(p, indent));
			}
			res.append(')');
		} else {
			res.append(t.label.toString());
		}

		return res.toString();
	}

	static public void main(String args[]) {
		try {
			System.setErr(new PrintStream(new File(OutputToFile.filePath)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		String str1 = "(S1 (S (PP-LOC (IN In) (NP (NP (DT an) (NNP Oct.) (CD 19) (NN review)) (PP (IN of) (NP (`` ``) (NP-TTL (DT The) (NN Misanthrope)) ('' '') (PP-LOC (IN at) (NP (NP (NNP Chicago) (POS 's)) (NNP Goodman) (NNP Theatre))))) (PRN (-LRB- -LRB-) (`` ``) (S-HLN (NP-SBJ (VBN Revitalized) (NNS Classics)) (VP (VBP Take) (NP (DT the) (NN Stage)) (PP-LOC (IN in) (NP (NNP Windy) (NNP City))))) (, ,) ('' '') (NP-TMP (NN Leisure) (CC &) (NNS Arts)) (-RRB- -RRB-)))) (, ,) (NP-SBJ-2 (NP (NP (DT the) (NN role)) (PP (IN of) (NP (NNP Celimene)))) (, ,) (VP (VBN played) (NP (-NONE- *)) (PP (IN by) (NP-LGS (NNP Kim) (NNP Cattrall)))) (, ,)) (VP (VBD was) (VP (ADVP-MNR (RB mistakenly)) (VBN attributed) (NP (-NONE- *-2)) (PP-CLR (TO to) (NP (NNP Christina) (NNP Haag))))) (. .)))";
		String str2 = "(S1 (S (NP-SBJ (NNP Ms.) (NNP Haag)) (VP (VBZ plays) (NP (NNP Elianti))) (. .)))";
		String str3 = "(S1 (S (NP-SBJ (NNP Rolls-Royce) (NNP Motor) (NNPS Cars) (NNP Inc.)) (VP (VBD said) (SBAR (-NONE- 0) (S (NP-SBJ (PRP it)) (VP (VBZ expects) (S (NP-SBJ (PRP$ its) (NNP U.S.) (NNS sales)) (VP (TO to) (VP (VB remain) (ADJP-PRD (JJ steady)) (PP-LOC-CLR (IN at) (NP (QP (IN about) (CD 1,200)) (NNS cars))) (PP-TMP (IN in) (NP (CD 1990)))))))))) (. .)))";
		
		try {
			TreeNode res = ReadTreeFlex.readTree(str1, true);
			TreeNode res_copy = res.copy_without_empties();
			SPTreeNode res_sp = SPTreeNode.tree_sptree(res_copy, true);
			System.err.println(TreeNode.display_tree(res_copy, 0));
			System.err.println("Begin display_sptree");
			System.err.println(SPTreeNode.display_tree(res_sp, 0));

			res = ReadTreeFlex.readTree(str2, true);
			res_copy = res.copy_without_empties();
			res_sp = SPTreeNode.tree_sptree(res_copy, true);
			System.err.println(TreeNode.display_tree(res_copy, 0));
			System.err.println("Begin display_sptree");
			System.err.println(SPTreeNode.display_tree(res_sp, 0));
			
			res = ReadTreeFlex.readTree(str3, true);
			res_copy = res.copy_without_empties();
			res_sp = SPTreeNode.tree_sptree(res_copy, true);
			System.err.println(TreeNode.display_tree(res_copy, 0));
			System.err.println("Begin display_sptree");
			System.err.println(SPTreeNode.display_tree(res_sp, 0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
}
