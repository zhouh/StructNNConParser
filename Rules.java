package nncon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * check the current state's validation
 * And return the validation
 * @author Hao Zhou
 * */
public class Rules {

	public List<String> m_dic;	//the dictionary here is a simple dic TODO be more functional 
	final int UNARY_MOVES=3;
	
	public Rules(List<String> dic){
		m_dic= dic;
	}                                                  
	
	private String extractConstituent(String tmpLabel){
		return tmpLabel.endsWith("*")?tmpLabel.substring(0, tmpLabel.length()-1):tmpLabel;
	}
	
	
	public List<Integer> getValidReduceActs(boolean bBinary, boolean bLeft, boolean bTemp, ConParseTSSState state){
		List<Integer> retval = new ArrayList<Integer>(Collections.nCopies(m_dic.size(), -1));
		
		int stackSize=state.stackSize();
		int length=state.input.sentLen;
		
		
		if(!bBinary){	// unary reduce
			if( !(stackSize>=1&&state.unaryReduceSize()<UNARY_MOVES&&!state.node.temp) )
				return null;
			
			boolean modify = false;
			ConParseTSSStateNode child=state.node;
			for(int i = 0; i < m_dic.size(); i++){
				String constituent = m_dic.get(i);
				//the unary reduce will not contain a temporary label so NP* will not exists here.
				//so the constituent could be equal directly
				if(!constituent.equals(child.constituent)){
					retval.set(i, 0);
					modify = true;
				}
			}
			
			return modify ? retval : null;
		}
		
		if(! (stackSize>1) ){ // binary reduce
			return null;
		}
		
		boolean modify = false;
		int stack_size=state.stackSize();
		final ConParseTSSStateNode right=state.node;
		final ConParseTSSStateNode left=state.stackPtr.node;
		int sentSize=state.input.sentLen;
	    // the normal method
		boolean prev_temp = stack_size>2 ? state.stackPtr.stackPtr.node.temp:false;	//the third node's temporary in the stack
		      for (int k = 0; k < m_dic.size(); k++) {
		    	  String constituent = m_dic.get(k);
		               
		    	  boolean head_left = bLeft;
		    	  boolean temporary = bTemp;
		    	  if ( ( !left.temp || !right.temp ) &&
		    			  ( !(stack_size==2 && state.currentWord==sentSize) || !temporary ) &&
		    			  ( !(stack_size==2) || (!temporary||head_left) ) &&
		    			  ( !(prev_temp && state.currentWord==sentSize) || !temporary ) &&
		    			  ( !(prev_temp) || (!temporary||head_left) ) &&
		    			  ( !left.temp || (head_left&&constituent.equals(extractConstituent(left.constituent))) ) &&
		    			  ( !right.temp || (!head_left&&constituent.equals(extractConstituent(right.constituent))) ) //&&
//		                     ( !temporary || CConstituent::canBeTemporary(constituent) ) 
		    			  ) {
		    		  retval.set(k, 0);
		    		  modify = true;
		    	  }
		         } // for constituent
		      
		      return modify ? retval : null;
	}
	
	public boolean isShiftValid(ConParseTSSState state){

		int stackSize=state.stackSize();
		int length=state.input.sentLen;
		if(state.currentWord<length){
			if(stackSize>0&&state.node.temp&&state.node.headBeLeft()==false){
				
			}
			else{
				return true;                              
			}
		}
		return false;
	}
	
	public ArrayList<ConAction> getActions(ConParseTSSState state){
		ArrayList<ConAction> actions=new ArrayList<ConAction>();
		
//		DEBUG
//		if(state.gold)
//			System.out.print("");
		
		int stackSize=state.stackSize();
		int length=state.input.sentLen;
		
		//add the end action
		if(state.isEnd()) actions.add(ConAction.END_STATE);
		
		//add the shift action
		if(state.currentWord<length){
			if(stackSize>0&&state.node.temp&&state.node.headBeLeft()==false){
				
			}
			else{
				actions.add(ConAction.SHIFT);                                
			}
		}
		
		//add the binary reduce action
		if(stackSize>1){
			getBinaryRules(state,actions);
		}
		//add the unary reduce
		if(stackSize>=1&&state.unaryReduceSize()<UNARY_MOVES&&!state.node.temp)
			getUnaryRules(state,actions);
		
		return actions;
	}

	private void getUnaryRules(ConParseTSSState state,
			ArrayList<ConAction> actions) {

		ConParseTSSStateNode child=state.node;
		for(String constituent:m_dic){
			//the unary reduce will not contain a temporary label so NP* will not exists here.
			//so the constituent could be equal directly
			if(!constituent.equals(child.constituent))
				actions.add(ConAction.getLabeledReduceAction(false,false,false, constituent));
		}
		
	}

	private void getBinaryRules(ConParseTSSState state,
			ArrayList<ConAction> actions) {

		int stack_size=state.stackSize();
		final ConParseTSSStateNode right=state.node;
		final ConParseTSSStateNode left=state.stackPtr.node;
		int sentSize=state.input.sentLen;
	    // the normal method
		boolean prev_temp = stack_size>2 ? state.stackPtr.stackPtr.node.temp:false;	//the third node's temporary in the stack
		      for (String constituent:m_dic) {
		         for (int i=0; i<=1; ++i) {
		        	 boolean head_left = i==0?false:true;
		            for (int j=0; j<=1; ++j) {
		               boolean temporary = j==0?false:true;
		               if ( ( !left.temp || !right.temp ) &&
		                     ( !(stack_size==2 && state.currentWord==sentSize) || !temporary ) &&
		                     ( !(stack_size==2) || (!temporary||head_left) ) &&
		                     ( !(prev_temp && state.currentWord==sentSize) || !temporary ) &&
		                     ( !(prev_temp) || (!temporary||head_left) ) &&
		                     ( !left.temp || (head_left&&constituent.equals(extractConstituent(left.constituent))) ) &&
		                     ( !right.temp || (!head_left&&constituent.equals(extractConstituent(right.constituent))) ) //&&
//		                     ( !temporary || CConstituent::canBeTemporary(constituent) ) 
		                 ) {
		                        actions.add(ConAction.getLabeledReduceAction(true,head_left,temporary, constituent));
		                  }
		               } // for j
		            } // for i
		         } // for constituent
	}
	
}
