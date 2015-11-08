package nncon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * The Tree structure stack state used in shift-reduce parser for 
 * avoiding copy the whole stack and action list and making the
 * parser from O(n^2) to O(n) 
 * 
 * @author Hao Zhou
 *
 */
public class ConParseTSSState {

	/**
	 * @param args
	 */
	
	public int bestActIndex;
	public double score;
	public double[] scores = null;
	public List<Integer> label = null;
	public int actionSize = 0;
	public boolean gold;
	public ConParseTSSStateNode node;
	public ConParseTSSState statePtr;	//point the last state for the reconstruct the whole tree node
	ConParseTSSState stackPtr;	//point the last state with last stack node to get the last stack node
	int currentWord;
	public ConAction action;
	int stackSize;
	public ConParseInput input;
	
	public ConParseTSSState(ConParseInput input){
		score=0;
		node=null;
		stackPtr=null;
		statePtr=null;
		action=null;
		currentWord=0;
		stackSize=0;
		gold=true;
		this.input=input;
	}
	
	public ConParseTSSState(){
		score=0;
		node=null;
		stackPtr=null;
		statePtr=null;
		action=null;
		currentWord=0;
		stackSize=0;
		gold=true;
		this.input=null;
	}
	
	public void setScores(double[] scores){
		this.scores = scores;
	}
	
	public void setLabel(List<Integer> label){
		this.label = label;
	}
	
	public void setBestActIndex(int act){
		this.bestActIndex = act;
	}
	
	public void setParameter(double[] scores, List<Integer> label, int act){
		this.scores = scores;
		this.label = label;
		this.bestActIndex = act;
	}
	
	public boolean empty(){
		if(currentWord==0) return true;
		return false;
	}
	
	/**
	 * change the code by store the stack size
	 * when running 
	 * 
	 * @return
	 * return the stack size of the state
	 */
	public int stackSize(){
//		int retval=0;
//		ConParseTSSState current=this;
//		while(current!=null&&current.node.valid()){
//			retval++;
//			current=current.statePtr;
//		}
//		return retval;
		return stackSize;
	}
	
	/**
	 * @return
	 * return the unary reduce action nums from current action
	 */
	public int unaryReduceSize(){
		int retval = 0;
		ConParseTSSState current=this;
		//The initial state has a null action and null node
		//So the initial case must be considered
		while(current!=null&&current.action!=null){
			if(current.action.isLabeledUnaryReduce())
				retval++;
			else return retval;
			current=current.statePtr;
		}
		return retval;
	}
	
	public int newNodeIndex(){
		return node.id+1;
	}
	
	/**
	 * The shift action, return the new state after the 
	 * shift action.
	 * The action could be used with a pos-tag prediction
	 * 
	 * @return
	 */
	private ConParseTSSState shift(String constituent,boolean bGold){
		ConParseTSSState retval=new ConParseTSSState(input);
		//the new leaf node's constituent must be null
		//In the first state of a parse, the state's node must be null
		//So the node id will be set as 0 directly
		if(node!=null) retval.node=new ConParseTSSStateNode(node.id+1,ConParseTSSStateNode.NODETYPE.LEAF,false,constituent,null,null,currentWord);
		else retval.node=new ConParseTSSStateNode(0,ConParseTSSStateNode.NODETYPE.LEAF,false,constituent,null,null,currentWord);
		retval.currentWord=currentWord+1;
		retval.stackPtr=this;
		retval.stackSize=stackSize+1;
		retval.gold=bGold;
		
		return retval;
	}
	
	/**
	 * The reduce action, return the new state after the 
	 * action.
	 * The function contains unary reduce and binary reduce.
	 * 
	 * @param constituent
	 * @param singleChild
	 * @param headLeft
	 * @param temporary
	 * @return
	 */
	private ConParseTSSState reduce(String constituent, boolean singleChild, boolean headLeft, boolean temporary,boolean bGold){
		ConParseTSSState retval=new ConParseTSSState(input);
		ConParseTSSStateNode l,r;
		
		if(singleChild){
			assert(headLeft==false);
			assert(temporary==false);
			l=node;
			//fill all the element in the node but the statePtr will be fill in the move function
			retval.node=new ConParseTSSStateNode(node.id+1, ConParseTSSStateNode.NODETYPE.SINGLE_CHILD,
					false, constituent, l,null,l.lexical_head);
			retval.stackPtr=stackPtr;
			retval.stackSize=stackSize;
		}
		else{
			assert(stackSize()>=2);
			r=node;
			l=stackPtr.node;
			//fill all the element in the node but the statePtr will be fill in the move function
			retval.node=new ConParseTSSStateNode(node.id+1, (headLeft?ConParseTSSStateNode.NODETYPE.HEAD_LEFT:ConParseTSSStateNode.NODETYPE.HEAD_RIGHT),
					temporary, encodeTmp(constituent,temporary), l, r, (headLeft?l.lexical_head:r.lexical_head));
			retval.stackPtr=stackPtr.stackPtr;
			retval.stackSize=stackSize-1;
		}
		retval.currentWord=currentWord;	//In the reduce action, current ConstituentLabel never change!
		retval.gold=bGold;
		return retval;
	}
	
	/**
	 * The terminate action
	 * pop the root of the stack
	 * 
	 * @return
	 */
	private ConParseTSSState end(boolean bGold){
		ConParseTSSState retval=new ConParseTSSState(input);
		retval.stackPtr=this.stackPtr;
		retval.node=this.node;
		retval.currentWord=currentWord;
		retval.gold=bGold;
		return retval;
	}
	
	/**
	 * Get the gold action from the gold tree
	 * By check the node in the gold tree next to the current state's node
	 * If the state is not a gold state, return NOT_AVAILABLE action 
	 * 
	 * @param gTree
	 * @return
	 */
	public ConAction getGoldAction(final CFGTree gTree){
		/*
		 * if the state is not gold, return a NOT_AVAILABLE Action
		 * And throw Exception
		 * 
		 * for test , the gTree is null
		 * SO, return NOT_AVAILABLE
		 * */
		
		if (gold == false||gTree==null) return ConAction.NOT_AVAILABLE;
		if (!gold) throw new IllegalArgumentException("Cannot get the gold action for non-gold state.");
		
		if(empty()) return ConAction.SHIFT;	//if it's the empty state, then teh first action may be shift

		/* get the gold node, 
		 * and judge next gold action from the gold node 
		 * 
		 * when the stack have only one subtree and the queue is empty
		 * if the next gold node's root is single child,then return ternimate action 
		 * */
		int nextGoldNodeIndex=node.id+1;
		int treeLen=gTree.size();
		CFGTreeNode nextGoldNode=nextGoldNodeIndex>=treeLen?null:gTree.nodes.get(nextGoldNodeIndex);
		boolean isEnd=isEnd();
		if(isEnd&&nextGoldNode==null) return ConAction.END_STATE;
		else if(!isEnd&&nextGoldNode==null) 
			throw new RuntimeException("Unexpected null of nextGoldNode!");
		
		if(nextGoldNodeIndex<treeLen){
			if(nextGoldNode.is_constituent==false) return ConAction.SHIFT;   
			else if(nextGoldNode.single_child==true) return ConAction.getLabeledReduceAction(false,true,nextGoldNode.temp,nextGoldNode.constituent.toString());
			else if(nextGoldNode.right_child!=-1){
				if(nextGoldNode.head_left) return ConAction.getLabeledReduceAction(true,true,nextGoldNode.temp,nextGoldNode.constituent.toString());
				else return ConAction.getLabeledReduceAction(true,false,nextGoldNode.temp,nextGoldNode.constituent.toString());
			}
		}
		else return ConAction.END_STATE;
		
		
		throw new RuntimeException("No action matched, something must be wrong!"); 
		
	}
	
	/**
	 * If the state is a end state return true 
	 * else return false
	 * 
	 * @return
	 */
	public boolean isEnd() {
		
		return currentWord == input.sentLen&&
				(stackSize==1);
	}
	
	/**
	 * Move the state with action
	 * set the state's gold with bGold
	 * if the state has been ended,just return itself 
	 * 
	 * @param action
	 * @param bGold
	 * @return
	 */
	public ConParseTSSState move(ConAction action, boolean bGold){
		
		ConParseTSSState retval=null;
		
		if(action.isShiftAction())  retval= shift("NONE", bGold);
		else if(action.isLabeledUnaryReduce()) retval= reduce(action.getTag(),true, false, false, bGold);
		else if(action.isRightReduce()) retval= reduce(action.getTag(), false, false, action.isTemp(), bGold);
		else if(action.isLeftReduce()) retval= reduce(action.getTag(), false, true, action.isTemp(), bGold);
		else if(action.isEndAction()) retval= end(bGold);
		
		if(retval==null) 
			throw new RuntimeException("No avaiable action!");
		
		retval.action=action;
		retval.statePtr=this;
		retval.actionSize = this.actionSize + 1;
		
		return retval;
	}

	public ConParseTSSState moveNextGold(final CFGTree gTree){
		ConAction goldAction=getGoldAction(gTree);
		return move(goldAction, true);
	}
	
	/**
	 * return the true constituent label
	 * if the node are temporary return label*
	 * else return label
	 * 
	 * @param constituent
	 * @param temporary
	 * @return
	 */
	private String encodeTmp(String constituent, boolean temporary) {
		return (temporary?(constituent+"*"):constituent);
	}
	
	public double getScore() {
		return score;
	}


	/**
	 * get all the valid actions from current state
	 * 
	 * @return
	 * return all the valid actions
	 */
	public ArrayList<ConAction> getNextActions(Rules rule) {

		return rule.getActions(this);
	}

	public void setScore(double score) {

		this.score=score;
	}

	
	public CFGTree convert2CFGTree(){
		assert(isEnd());
		if(stackSize>1) throw new RuntimeException("One sentence does not parse end!");
		
		CFGTree retval=new CFGTree();
		
		ConParseTSSState current=this;
		while(current!=null){
			/*
			 * IDLE and END action will be ignored here.
			 */
			if(current.node!=null){
				retval.nodes.add(current.node.toCFGTreeNode(input));
			}
			current=current.statePtr;
		}
		 
		//reverse the nodes in the CFGTree
		Collections.reverse(retval.nodes);
		//set the root node index as the last node in the tree
		retval.setRoot(retval.nodes.size());
		
		return retval;
	}

	public String getActSeq() {

		String retval="";
		ConParseTSSState current=this;
		while(current!=null){
			if(current.action!=null){
				retval=current.action.toString()+" "+retval;
			}
			current=current.statePtr;
		}
		return retval;
	}
	
	public ArrayList<ConAction> getActionList(){
		
		ArrayList<ConAction> retval=new ArrayList<ConAction>();
		ConParseTSSState current=this;
		while(current!=null){
			if(current.action!=null){
				retval.add(current.action);
			}
			current=current.statePtr;
		}
		
		Collections.reverse(retval);
		
		return retval;
	}
	
	/**
	 *   output the first 3 states in the stack and
	 *   the first 3 item in the queue
	 */
	public String toString(){
		
		String retval = "";
		
		retval = node!=null?(node.toString(input)+retval):" -1 ";
		if(stackPtr!=null){
			retval=stackPtr.node.toString(input)+retval;
			
			if(stackPtr.stackPtr!=null)
				retval=stackPtr.stackPtr.node.toString(input)+retval;
			else 
				retval = " -1 "+retval;
		}
		else
			retval=" -1 -1 "+retval;
		
	
		//add the queue item
		for(int i = currentWord; i<input.sentLen; i++)
			retval+= "{" + input.getWord(i) +"}";
		
		return retval;
		
	}


}
