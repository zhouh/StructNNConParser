package nncon.charniarkFeature;

import java.util.Vector;

//! NGram
//!
//! Identifier is NGram:<frag_len>:<nanccats>:<root>:<conj>:<head>:<functional>:<all>:<type>
//
public class NGram extends RuleFeatureClass {
	int fraglen;

	public NGram(int fraglen, int nancecats, boolean label_root,
			boolean label_conjunct) {
		this(fraglen, nancecats, label_root, label_conjunct,
				annotation_level.none);
	}

	public NGram(int fraglen, int nancecats, boolean label_root,
			boolean label_conjunct, annotation_level head) {
		this(fraglen, nancecats, label_root, label_conjunct, head,
				annotation_level.none);
	}

	public NGram(int fraglen, int nancecats, boolean label_root,
			boolean label_conjunct, annotation_level head,
			annotation_level functional) {
		this(fraglen, nancecats, label_root, label_conjunct, head, functional,
				annotation_level.none, annotation_type.syntactic);
	}

	public NGram(int fraglen, // !< Number of children in sequence
			int nanccats, // !< Number of ancestor categories above trees
			boolean label_root, // !< Annotate with "in root context"
			boolean label_conjunct, // !< Annotate with "belongs to conjunction"
			annotation_level head, // !< Amount of head annotation
			annotation_level functional, // !< Amount of function word
											// annotation
			annotation_level all, // !< Amount of lexical word annotation
			annotation_type type) {
		super("NGram:" + fraglen, nanccats, label_root, label_conjunct, head,
				functional, all, type);
		this.fraglen = fraglen;
	}

	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if (!node.is_nonterminal())
			return;

		int nchildren = 0;
		for (SPTreeNode child = node.child; child != null; child = child.next)
			nchildren++;

		if (nchildren < fraglen)
			return;

		SPTreeNode headchild = (type == annotation_type.semantic) ? node.label.semantic_headchild
				: node.label.syntactic_headchild;
		
		Vector<SPTreeNode> children = new Vector<SPTreeNode>();
		
		children.add(null);
		for(SPTreeNode child = node.child; child != null; child = child.next)
			children.add(child);
		children.add(null);
		
		Symbol headpostion = preheadmarker();
		
		for(int start = 0; start + fraglen <= children.size(); start++) {
			if(children.get(start) == headchild)
				headpostion = postheadmarker();
			
			RuleFeature f = new RuleFeature();
			
			annotation_level_wrapper highest_level = new annotation_level_wrapper(annotation_level.none);
			boolean includes_headchild = false;
			
			for(int pos = start; pos < start + fraglen; pos++) {
				SPTreeNode child = children.get(pos);
				
				if(child != null)
					push_child_features(child, node, f, highest_level);
				else {
					f.push(endmarker());
				}
				
				if(child == headchild)
					includes_headchild = true;
			}
			
			f.push(headpostion);
			
			if(includes_headchild == false && head != annotation_level.none)
				push_child_features(headchild, node, f, highest_level);
			
			if(highest_level.value != max_annotation_level)
				return ;
			
			push_ancestor_features(node, f);
			
			double v = feat_count.getValueOf(f);
			feat_count.setValueOf(f, v + 1);
		}
		
	}

	@Override
	String identifier() {
		return identifier_string;
	}
}
