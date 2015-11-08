package nncon.charniarkFeature;

import java.util.HashMap;

public class Rule extends RuleFeatureClass {
	int nanctrees;
	
	public Rule() {
		this(0, 0);
	}
	public Rule(int nanctrees, int nanccats) {
		this(nanctrees, nanccats, false);
	}
	public Rule(int nanctrees, int nanccats, boolean label_root){
		this(nanctrees, nanccats, label_root, false);
	}
	public Rule(int nanctrees, int nanccats, boolean label_root, boolean label_conjunct) {
		this(nanctrees, nanccats, label_root, label_conjunct, RuleFeatureClass.annotation_level.none);
	}
	public Rule(int nanctrees, int nanccats, boolean label_root, boolean label_conjunct, annotation_level head) {
		this(nanctrees, nanccats, label_root, label_conjunct, head, RuleFeatureClass.annotation_level.none);
	}
	public Rule(int nanctrees, int nanccats, boolean label_root, boolean label_conjunct, annotation_level head, annotation_level functional) {
		this(nanctrees, nanccats, label_root, label_conjunct, head, functional, RuleFeatureClass.annotation_level.none, RuleFeatureClass.annotation_type.syntactic);
	}
	public Rule(int nanctrees, int nanccats, boolean label_root, boolean label_conjunct, annotation_level head, annotation_level functional, annotation_level all, annotation_type type) {
		super("Rule:" + nanctrees, nanccats, label_root, label_conjunct, head, functional, all, type);
		this.nanctrees = nanctrees;
		feature_id = new HashMap<Feature, Long>();
	}
	
	@Override
	public void node_featurecount(FeatureClass fc, SPTreeNode node,
			ParseValAccessor feat_count) {
		if(!node.is_nonterminal())
			return ;
		
		RuleFeature f = new RuleFeature();
		annotation_level_wrapper highest_level = new annotation_level_wrapper(annotation_level.none);
		
		// push (possibly lexicalized) children
		for(SPTreeNode child = node.child; child != null; child = child.next)
			push_child_features(child, node, f, highest_level);
		
		// push (possibly lexicalized) ancestor rules
		for(int i = 0; i < nanctrees && node.label.parent != null; i++) {
			f.push(endmarker());
			for(SPTreeNode child = node.label.parent.child; child != null; child = child.next)
				if(child == node) {
					f.push(childmarker());
					f.push(child.label.cat);
				} else {
					push_child_features(child, node, f, highest_level);
					node = node.label.parent;
				}
		}
		
		if(highest_level.value != max_annotation_level)
			return ;
		
		push_ancestor_features(node, f);
		
		/* TODO */
		double v = feat_count.getValueOf(f);
		feat_count.setValueOf(f, v + 1);
	}
}
