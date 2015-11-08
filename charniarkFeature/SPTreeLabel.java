package nncon.charniarkFeature;

public class SPTreeLabel extends TreeLabel {
	
	SPTreeNode parent;
	SPTreeNode previous;
	SPTreeNode syntactic_headchild;
	SPTreeNode syntactic_lexhead;
	SPTreeNode semantic_headchild;
	SPTreeNode semantic_lexhead;
	long left, right;
	
	public SPTreeLabel(){
		super(Symbol.undefined());
	}
	
	public SPTreeLabel(TreeLabel label) {
		super(label);
		parent = null;
		previous = null;
		syntactic_headchild = null;
		syntactic_lexhead = null;
		semantic_headchild = null;
		semantic_lexhead = null;
	}
	
	public SPTreeLabel(SPTreeLabel label){
		this.cat = new Symbol(label.cat);
	}
	
	public SPTreeLabel(Symbol cat){
		this.cat = new Symbol(cat);
	}
	
	public boolean equalTo(SPTreeLabel l){
		return cat.equalTo(l.cat);
	}
	
	public boolean lessThan(SPTreeLabel l){
		return cat.lessThan(l.cat);
	}
	
	public boolean is_syntactic_headchild(){
		return parent != null && parent.label.syntactic_headchild.label == this;
	}
	
	public boolean is_semantic_headchild() {
		return parent != null && parent.label.semantic_headchild.label == this;
	}
	
	@Override
	public int hashCode() {
		return cat.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder("");
		res.append(cat.getString());
//		res.append(cat.getString() + ' ');
//		res.append(":" + this.left + "-" + this.right);
//		res.append(":" + (this.parent != null ? this.parent.label.cat.getString() : none().getString()));
//		res.append(":" + (this.previous != null ? this.previous.label.cat.getString() : none().getString()));
//		res.append(":" + (this.syntactic_lexhead != null ? this.syntactic_lexhead.child.label.cat.getString() : none().getString()));
//		res.append(":" + (this.semantic_lexhead != null ? this.semantic_lexhead.child.label.cat.getString() : none().getString()));
		
		return res.toString();
	}
}
