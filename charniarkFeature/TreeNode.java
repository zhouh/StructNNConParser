package nncon.charniarkFeature;

import java.util.Vector;

public class TreeNode {
	TreeLabel label;

	public TreeNode child; // subtrees
	public TreeNode next; // sibling

	public TreeNode() {
		label = new TreeLabel();
		child = null;
		next = null;
	}

	public TreeNode(TreeLabel label) {
		this(label, null, null);
	}

	public TreeNode(TreeLabel label, TreeNode child, TreeNode next) {
		this.label = label;
		this.child = child;
		this.next = next;
	}

	public TreeNode(Symbol cat) {
		this(cat, null, null);
	}

	public TreeNode(Symbol cat, TreeNode child, TreeNode next) {
		this.label = new TreeLabel(cat);
		this.child = child;
		this.next = next;
	}

	/**
	 * The TreeNode(TreeNode node) copy constructors makes "deep" copies.
	 * 
	 * @param node
	 */
	public TreeNode(TreeNode node) {
		this.label = new TreeLabel(node.label);

		if (node.child != null) {
			this.child = new TreeNode(node.child);
		} else {
			node.child = null;
		}

		if (node.next != null) {
			this.next = new TreeNode(node.next);
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
	public boolean equalTo(TreeNode t) {
		if (this == t)
			return true;
		if (!label.equalTo(t.label))
			return false;

		return ((child.equalTo(t.child) || (child != null && t.child != null && child
				.equalTo(t.child))) && (next.equalTo(t.next) || (next != null
				&& t.next != null && next.equalTo(t.next))));
	}

	public boolean lessThan(TreeNode t) {
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
	public TreeNode copy_tree() {
		return copy_treeptr(this);
	}

	public static TreeNode copy_treeptr(TreeNode tp) {
		if (tp == null)
			return null;

		TreeNode t = new TreeNode(tp.label, copy_treeptr(tp.child),
				copy_treeptr(tp.next));

		return t;
	}

	/**
	 * exists_cut_p() is true if there is a cut through this tree such that
	 * every node satsifies check()
	 * 
	 * @param p
	 * @return
	 */
	public boolean exists_cut_p(TreePropertyChecker p) {
		if (p.check(this))
			return true;

		if (child == null)
			return false;

		for (TreeNode c = child; c != null; c = c.next)
			if (!c.exists_cut_p(p))
				return false;

		return true;
	}

	/**
	 * returns the first ndoe for which pred is true
	 * 
	 * @param args
	 */
	public TreeNode preorder_find(Predicater pred) {
		if (pred.check(this))
			return this;
		if (child != null) {
			TreeNode rc = child.preorder_find(pred);
			if (rc != null)
				return rc;
		}
		if (next != null) {
			TreeNode rn = next.preorder_find(pred);
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
	public long count(Predicater pred) {
		return count(pred, 0);
	}

	public long count(Predicater pred, long cnt) {
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
		for (TreeNode c = child; c != null; c = c.next)
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
		TreeNode c = child;
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
		for (TreeNode c = child; c != null; c = c.next)
			if (c.label.cat.notEqualTo(label.cat) && !c.is_punctuation())
				return false;
		return true;
	}

	// ! is_last_nonpunctuation() is true iff all right siblings are
	// ! punctuation.
	//
	public boolean is_last_nonpunctuation() {
		TreeNode sibling = next;
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

		for (TreeNode c = child; c != null; c = c.next) {
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
				TreeNode grandchild = child.child;
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
	public TreeNode copy_without_empties() {
		return copy_without_empties(false, null);
	}

	public TreeNode copy_without_empties(boolean delete_adjunctions,
			TreeNode endp) {
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
			TreeNode c = new TreeNode(new TreeLabel(label),
					(child != null) ? child.copy_without_empties(
							delete_adjunctions, null) : null,
					(next != null) ? next.copy_without_empties(
							delete_adjunctions, endp) : endp);

			return c;
		}
	}

	// ! copy_left_binarize() returns a pointer to a left binarized copy of
	// ! this tree.
	//
	public TreeNode copy_left_binarize() {
		TreeNode next_copy = null;
		if (next != null) {
			next_copy = next.copy_left_binarize();
			if (next_copy.next != null)
				next_copy = new TreeNode(new TreeLabel(), next_copy, null);
		}

		return new TreeNode(new TreeLabel(label), (child != null) ? child.copy_left_binarize()
				: null, next_copy);
	}

	// ! copy_parent_annotate() returns a pointer to a parent-annotated
	// ! copy of this tree.
	//
	public TreeNode copy_parent_annotate() {
		return copy_parent_annotate(Symbol.undefined());
	}

	public TreeNode copy_parent_annotate(Symbol parent_cat) {
		TreeNode t = new TreeNode(label);
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
	public void preorder(TreeProcesser fn) {
		fn.process(this);

		if (child != null)
			child.preorder(fn);
		if (next != null)
			next.preorder(fn);
	}

	// ! postorder() calls the function object fn() on every subtree
	// ! in a postorder traversal.
	//
	public void postorder(TreeProcesser fn) {
		if (child != null)
			child.postorder(fn);
		fn.process(this);
		if (next != null)
			next.postorder(fn);
	}

	// ! find() returns the first node in a preorder traversal
	// ! for which the function object fn() is true.
	//
	public boolean find(TreeProcesser fn) {
		return fn.process(this) || (child != null && child.find(fn))
				|| (next != null && next.find(fn));
	}

	private void preorder_ancestors(TreeAncestorsProcessor fn,
			Vector<TreeNode> ancestors) {
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
	void preorder_ancestors(TreeAncestorsProcessor fn) {
		Vector<TreeNode> ancestors = new Vector<TreeNode>();
		preorder_ancestors(fn, ancestors);
		assert (ancestors.isEmpty());
	}

	// ! terminals() appends the terminals to terms, optionally skipping empty
	// nodes
	//
	void terminals(Vector<Object> terms) {
		terminals(terms, false);
	}

	void terminals(Vector<Object> terms, boolean include_empty) {
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
	void preterminal_nodes(Vector<Object> preterms) {
		preterminal_nodes(preterms, false);
	}

	void preterminal_nodes(Vector<Object> preterms, boolean include_empty) {
		assert (child != null);

		if (is_preterminal()) {
			if (!label.is_none() || include_empty)
				preterms.add(this);
		} else {
			child.preterminal_nodes(preterms, include_empty);
		}

		if (next != null)
			next.preterminals(preterms, include_empty);
	}

	static public String display_tree(TreeNode t) {
		return display_tree(t, 0);
	}

	static public String display_tree(TreeNode t, int indent) {
		if (t == null)
			return "";
		StringBuilder res = new StringBuilder("");
		if (t.child != null) {
			res.append("(" + t.label.toString() + " ");
			indent += t.label.toString().length() + 2;

			res.append(display_tree(t.child, indent));

			for (TreeNode p = t.child.next; p != null; p = p.next) {
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

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder("");
		write_tree(res, this);
		return res.toString();
	}
	private void write_tree(StringBuilder s, TreeNode t) {
		assert(t != null);
		
		if(t.child != null) {
			s.append("(" + t.label.toString());
			
			for(TreeNode p = t.child; p != null; p = p.next) {
				s.append(" ");
				write_tree(s, p);
			}
			
			s.append(")");
		} else {
			s.append(t.label.toString());
		}
	}
	// /////////////////////////////////////////////////////////////////////////
	// //
	// precision and recall //
	// //
	// /////////////////////////////////////////////////////////////////////////

	// ! precrec_type{} calculates standard parseval precision and recall
	// scores.
	// ! It uses bag-type evaluation.
	//
	static public class precrec_type{
		long ncommon;
		long ngold;
		long ntest;
		
		public precrec_type(){
			this(0, 0, 0);
		}
		
		public precrec_type(long ncommon, long ngold, long ntest){
			this.ncommon = ncommon;
			this.ngold = ngold;
			this.ntest = ntest;
		}
		
		public double precision(){
			return (ntest == 0) ? 0 : (double)ncommon / ntest;
		}
		
		public double recall() {
			return (ngold == 0) ? 1 : (double)ncommon / ngold;
		}
		
		public double f_score() {
			return (ntest == 0 && ngold == 0) ? 0 : (2.0 *ncommon) / (ntest + ngold);
		}
		
		public double error_rate() {
			return (ngold + ntest - 2.0 * ncommon) / ngold;
		}
		
//		public static class edge extends pair
	}
}
