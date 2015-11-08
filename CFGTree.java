package nncon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;




/**
 * the whole syntax tree structure class
 * the syntax tree is connected by many CFGTreeNode
 * and the CTB bracket tree to syntax tree function is also in the class
 * 
 * @author hao zhou 
 * */
public class CFGTree{

	int root;   // the root node id
	public List<CFGTreeNode> nodes;   //the node list in the tree
	 
	/**
	 * No-argument constructor
	 * Construct a CFGTree with the empty parameter
	 * @return a empty CFGTree with empty nodes
	 * */
	public CFGTree(){
		root=-1;
		nodes=new ArrayList<CFGTreeNode>();
	}
	
	/**
	 * Copy Constructor
	 * return a CFGTree with the same nodes with the input CFGTree
	 * 
	 * @param forclone the tree to be copied
	 * @return a tree have the same nodes with input tree, but the root is -1
	 * 
	 * */
	public CFGTree(CFGTree forclone){
		root=-1;
//		nodes=(ArrayList<CFGTreeNode>)((ArrayList<CFGTreeNode>)forclone.nodes).clone();
		nodes=new ArrayList<CFGTreeNode>();
		for(int i=0;i<forclone.nodes.size();i++)
			this.nodes.add(new CFGTreeNode(forclone.nodes.get(i)));	/*TODO in beam search the node here may not need copy constructor
			 																because the tree node never delete or change*/ 
	}
	

	/**
	 * construct a unParsed ConstituentLabel and tag sequence from the string
	 * the sequence saved in the nodes array to be the input of the parser
	 * 
	 * @param tokens a sequence of ConstituentLabel and tag pairs split by '_' character
	 * @return a CFGTee only contains ConstituentLabel and tag information of a unParsed sentence
	 * 
	 * */
	public CFGTree(String[] tokens){
		
		root=-1;
		nodes=new ArrayList<CFGTreeNode>();
		
		for(int i=0;i<tokens.length;i++){
			CFGTreeNode node=new CFGTreeNode();

			String[] t=tokens[i].split("_");
			String word=t[0];
			String tag=t[1];
			
			//System.out.println(ConstituentLabel+" "+tag);
			
			node.word=word;
			node.is_constituent=false;
			node.constituent=tag;
			node.token=i;
			
			//System.out.print(node.word+" "+node.constituent);
			
			nodes.add(node);
		}
		
	}
	
	
	
	public void setRoot(int root) {
		this.root = root;
	}

	/**
	 * the function read CTB Node recursively from a list of 
	 * String named tokens consisted of a sentence
	 * a node like ( NP l* ( VP t XXX ) ( NP t XXX ) ), first 
	 * read '(' and the node's label NP and read the left and 
	 * right node recursively. The label l is a indicator for head ConstituentLabel 
	 * '*' means the node is a temporary node, make up by CNF binarization
	 * 
	 * TODO:// set the input bracket tree format much more free 
	 *         instead of keeping a space between every tag or 
	 *         ConstituentLabel or bracket 
	 * @param String tokens split by space from the CTB sentence
	 * @return the node id just read by the function
	 * 
	 * */
	public int CTBReadNote(List<String> tokens){
		
		int node;   // the read node id to be returned
		
		//get the node's name and 
		//keep a space between ConstituentLabel , tag and bracket 
		String s,name;
		s=tokens.get(0);
		
		//the first character of a node must be a left bracket
		assert(!s.equals("("));  
		tokens.remove(0);
		name=tokens.get(0);   //get the tag 
		tokens.remove(0);
		//get s and judge the node is constituent or a ConstituentLabel
		s=tokens.get(0);   
		tokens.remove(0);
		
		assert(s.length()<=2);
		
		int left,right;
		boolean temporary;
		//default temp is false
		temporary=false;
		
		if(s.length()==2){
			assert(s.charAt(1)=='*');
			temporary=true;
		}
		
		/*
		 * if the label is l or r or e, the node is not a leaf node
		 * read recursively is is a binary tree
		 * */
		if(s.charAt(0)=='l'||s.charAt(0)=='r'||s.charAt(0)=='e'){
			left=CTBReadNote(tokens);
			right=CTBReadNote(tokens);
			
			//new a node and return the node's id and set para
			node=newNode();
			nodes.get(node).is_constituent=true;
			nodes.get(node).single_child=false;
			
			//l -head left; r/e -head right
			if(s.charAt(0)=='l') nodes.get(node).head_left=true;
			else nodes.get(node).head_left=false;
			
			nodes.get(node).left_child=left;
			nodes.get(node).right_child=right;
			
			//e -NONE node; l/r labeled node
			if(s.charAt(0)=='e')
				nodes.get(node).constituent="NONE";
			else
				nodes.get(node).constituent=name;
			
			//e - no token ; l/r -has token
			if(s.charAt(0)=='e') nodes.get(node).token=-1;
			else nodes.get(node).token=s.charAt(0)=='l' ? nodes.get(left).token : nodes.get(right).token;
		
			nodes.get(node).temp=temporary;
			
			s=tokens.get(0);
			tokens.remove(0);
			assert(s.equals(")"));
		}
		/*
		 * the node is a unary node
		 * */
		else if(s.charAt(0)=='s'){
			
			left=CTBReadNote(tokens);
			node=newNode();
			CFGTreeNode cNode=nodes.get(node);
			cNode.is_constituent=true;
			cNode.single_child=true;
			cNode.left_child=left;
			cNode.constituent=name;
			cNode.right_child=-1;
			cNode.token=nodes.get(left).token;
			cNode.head_left=false;
			cNode.temp=temporary;
			assert(temporary==false); //single node can't be binarized temp
			
			s=tokens.get(0);
			tokens.remove(0);
			assert(s.equals(")"));
			
		}
		/*
		 * the node is a leaf node
		 * */
		else{
			String token;
			node = newNode();
			CFGTreeNode cNode=nodes.get(node);
			cNode.is_constituent=false;
			cNode.single_child=false;
			cNode.head_left=false;
		    cNode.constituent=name;
				
			token=tokens.get(0);
			tokens.remove(0);
			s=tokens.get(0);
			tokens.remove(0);
			
			while(!s.equals(")")){
				token=token+""+s;
				s=tokens.get(0);
				tokens.remove(0);
				
			}
			cNode.word=token;
			cNode.token=node;
			cNode.left_child=-1;
			cNode.right_child=-1;
		}

		return node;
	}
	
	/**
	 * write the tree use pw from the root node
	 * for the binary tree with head indicator
	 * 
	 * @param pw PrintWriter para 
	 * @author zhouh
	 * */
	public void writeTree(PrintWriter pw) throws IOException{
		
		writeNode(root,pw);
		
	}
	
	/**
	 * write the sub tree of root node 
	 * */
	public void writeNode(int root,PrintWriter pw) throws IOException{
	
		CFGTreeNode node=nodes.get(root);
		pw.print("( "+node.constituent+" ");
		
		if(node.is_constituent==false){
			
			pw.print("t "+node.word+" )");
			return;
		}
		else{
			String childLabel=node.head_left?"l":"r";
			String isTemp=node.temp?"*":"";
			
			pw.print(childLabel+isTemp+" ");
			
			if(node.left_child!=-1) writeNode(node.left_child,pw);
			pw.print("  ");
			if(node.right_child!=-1) writeNode(node.right_child,pw);
			pw.print(" )");
		}
		
	}
	public int size(){
		return nodes.size();
	}
	
	/**
	 * create a new node in the CFGTree nodes list
	 * and return the new node index
	 * */
	private int newNode() {
		CFGTreeNode newTree=new CFGTreeNode();
		nodes.add(newTree);	
		return nodes.size()-1;  //返回刚刚新建的node的index
	}
	
	/**
	 * return the String of CFGTree
	 */
	public String toString(){
		String retval="";
		
		retval=getNodeString(nodes.size()-1,retval);
		
		return retval;
	}
	
	/**
	 * return the Charniark String of CFGTree
	 * contains a (S1 ) in the outside of the tree
	 */
	public String toCharniarkString(){
		String retval="(S1 ";
		
		retval=getNodeString(nodes.size()-1,retval);
		
		return retval+")";
	}
	
	public String toBiTreeString(){
		String retval="";
		
		retval=getNodeBiTreeString(nodes.size()-1,retval);
		
		return retval;
	}

	/**
	 * get the binary tree string of a CFGTree node
	 * 
	 * @param i
	 * @param retval
	 * @return
	 */
	private String getNodeBiTreeString(int i, String retval) {

		CFGTreeNode node=nodes.get(i);

		retval+="("+node.constituent+" ";
		
		if(node.is_constituent==false){
			
			retval+=node.word+")";
			return retval;
		}
		else{
			if(node.left_child!=-1) retval=getNodeBiTreeString(node.left_child,retval);
			retval+=" ";
			if(node.right_child!=-1) retval=getNodeBiTreeString(node.right_child,retval);
			retval+=")";
		}
        
        return retval;
	}

	/**
	 * get the bracket string of a CFGTree node
	 * 
	 * @param i
	 * @param retval
	 */
	private String getNodeString(int i, String retval) {

		 CFGTreeNode node=nodes.get(i);

	        if(node.temp){
	        	if(node.left_child!=-1) retval=getNodeString(node.left_child,retval);
				retval+=" ";
				if(node.right_child!=-1) retval=getNodeString(node.right_child,retval);
	        }
	        else{
	        	retval+="("+node.constituent+" ";
	    		
	    		if(node.is_constituent==false){
	    			
	    			retval+=node.word+")";
	    			return retval;
	    		}
	    		else{
	    			if(node.left_child!=-1) retval=getNodeString(node.left_child,retval);
	    			retval+=" ";
	    			if(node.right_child!=-1) retval=getNodeString(node.right_child,retval);
	    			retval+=")";
	    		}
	        }
	        
	        return retval;
	}
	
	public List<CFGTreeNode>getSentFromTree(){
		
		List<CFGTreeNode> retval = new ArrayList<CFGTreeNode>();
		for(CFGTreeNode node : nodes)
			if(!node.is_constituent)
				retval.add(node);
		return retval;
	}
}
