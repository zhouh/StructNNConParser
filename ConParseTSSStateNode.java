package nncon;

/**
 * constituent state node 
 * 
 * the constituent state is implemented with a tree structure stack
 * and every constituent state is composed by many constituent state node
 * 
 * 
 * @author Hao Zhou
 *
 */
public class ConParseTSSStateNode {

	public enum NODETYPE{LEAF,SINGLE_CHILD,HEAD_LEFT,HEAD_RIGHT};
	public NODETYPE type;
	public boolean temp;
	public int id;
	public String constituent;
	public ConParseTSSStateNode left_child;
	public ConParseTSSStateNode right_child;
	public int lexical_head;
	
	public boolean headBeLeft(){
		return type==NODETYPE.HEAD_LEFT;
	}
	
	public boolean headBeRight(){
		return type==NODETYPE.HEAD_RIGHT;
	}
	
	public boolean singleChild(){
		return type==NODETYPE.SINGLE_CHILD;
	}
	
	public boolean beLeaf(){
		return type==NODETYPE.LEAF;
	}
	/**
	 * 
	 * construct function
	 * 
	 * @param id
	 * @param type
	 * @param temp
	 * @param constituent
	 * @param leftChild
	 * @param rightChild
	 * @param lexicalHead
	 */
	public ConParseTSSStateNode(int id, NODETYPE type, boolean temp, String constituent, 
			ConParseTSSStateNode leftChild, ConParseTSSStateNode rightChild, int lexicalHead){
		
		this.id=id;
		this.type=type;
		this.temp=temp;
		this.constituent=constituent;
		this.left_child=leftChild;
		this.right_child=rightChild;
		this.lexical_head=lexicalHead;
	}
	
	public boolean valid(){
		return id!=-1;
	}
	
	public void set(int id, NODETYPE type, boolean temp, String constituent, 
			ConParseTSSStateNode leftChild, ConParseTSSStateNode rightChild, int lexicalHead){
		
		this.id=id;
		this.type=type;
		this.temp=temp;
		this.constituent=constituent;
		this.left_child=leftChild;
		this.right_child=rightChild;
		this.lexical_head=lexicalHead;
	}
	
	/**
	 * equal compare function
	 * 
	 * @param node
	 * @return
	 */
	public boolean equals(ConParseTSSStateNode node){
		
		
		return id==node.id&&
				type==node.type&&
				temp==node.temp&&
				constituent.equals(node.constituent)&&
				left_child.equals(node.left_child)&&
				right_child.equals(node.right_child)&&
				lexical_head==node.lexical_head;
			   
	}
	
	/**
	 * copy the node 
	 * 
	 * @param node
	 */
	public void copy(ConParseTSSStateNode node){
		this.id=node.id;
		this.type=node.type;
		this.temp=node.temp;
		this.constituent=node.constituent;
		this.left_child=node.left_child;
		this.right_child=node.right_child;
		this.lexical_head=node.lexical_head;
	}
	
	/**
	 * convert the state node to the CFG tree node
	 * 
	 * @return
	 */
	public CFGTreeNode toCFGTreeNode(ConParseInput input){
		
		CFGTreeNode cfgTreeNode=new CFGTreeNode();
		cfgTreeNode.constituent=beLeaf()?input.tags.get(lexical_head):constituent;
		cfgTreeNode.is_constituent=beLeaf()?false:true;
		cfgTreeNode.temp=temp;
		cfgTreeNode.single_child=singleChild();
		cfgTreeNode.head_left=headBeLeft();
		cfgTreeNode.left_child=left_child!=null?left_child.id:-1;
		cfgTreeNode.right_child=right_child!=null?right_child.id:-1;
		cfgTreeNode.token=lexical_head;
		cfgTreeNode.word=input.words.get(lexical_head);
		
		return cfgTreeNode;
	}

	public String toString(ConParseInput input) {
		
		String left = left_child!=null?left_child.constituent : "NONE"; 
		String right = right_child!=null?right_child.constituent : "NONE";
		
		if(left_child!=null && left_child.beLeaf()) 
			left=input.words.get(left_child.lexical_head);
		if(right_child!=null && right_child.beLeaf()) 
			right=input.words.get(right_child.lexical_head);
		
		if(beLeaf()) return " "+input.words.get(lexical_head)+" ";
		return " [" + constituent
				+ "[" +left+" " + right+" ]" +   "]";
	}
	
	
	
}
