package nncon;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constituent Action class
 * used in shift-reduce constituent parser
 * shift
 * unary-reduce
 * binary-reduce 
 * terminate
 * and some parameter like temporary, leftHead
 * */
public class ConAction{

	/* The action type of each action */
	public enum actionType{SHIFT,END,BINARY_REDUCE_LEFT,BINARY_REDUCE_RIGHT,UNARY_REDUCE,OTHERS};
	
	/* action that only */
	public static final ConAction NOT_AVAILABLE = new ConAction("NA","NA",0,actionType.OTHERS);
	public static final ConAction END_STATE = new ConAction("E","E",1,actionType.END);
	public static final ConAction SHIFT = new ConAction("S","S",2,actionType.SHIFT);
	
	public static int size = 3;
	
	/*action list map, from String to constituent action*/
	private static  Map<String,ConAction> m_shiftTagActions;	// shift with a pos-tag action
																		// used in the parse and pos joint model
	private static  Map<String,ConAction> m_labeledUnaryReduceActions;
	/*binary reduce action*/
	private static  Map<String,ConAction> m_labeledBinaryReduceLActions;
	private static  Map<String,ConAction> m_labeledBinaryReduceRActions;
	private static  Map<String,ConAction> m_labeledBinaryReduceLTempActions;
	private static  Map<String,ConAction> m_labeledBinaryReduceRTempActions;
	
	/*
	 * String to label action, used in constructor and toString() 
	 * and Judge which kind of action this action is.
	 * */
	private static final String m_actShiftTag = "ST";
	private static final String m_actUnaryReduce ="UR";
	private static final String m_actBinaryReduceL="BRL";
	private static final String m_actBinaryReduceR="BRR";
	private static final String m_actBinaryReduceLTemp="BRL*";
	private static final String m_actBinaryReduceRTemp="BRR*";
	
	/*
	 * Initial the action list 
	 * */
	static
	{
		m_shiftTagActions = new ConcurrentHashMap<String,ConAction>();
		m_labeledUnaryReduceActions = new ConcurrentHashMap<String,ConAction>();
		m_labeledBinaryReduceLActions =new ConcurrentHashMap<String,ConAction>();
		m_labeledBinaryReduceRActions =new ConcurrentHashMap<String,ConAction>();		
		m_labeledBinaryReduceLTempActions =new ConcurrentHashMap<String,ConAction>();
		m_labeledBinaryReduceRTempActions =new ConcurrentHashMap<String,ConAction>();
	}
	
	/* para member */
	private final String m_action;
	private final String m_tag;
	private final int code;
	private final boolean isTemp;
	private final actionType type;

	/**
	 * Constructor
	 * */
	private ConAction(String sAct, String sTag, actionType type)
	{
		this(sAct, sTag,false,type);
	}
	private ConAction(String sAct, String sTag,int code, actionType type)
	{
		this(sAct, sTag,code,false,type);
	}

	public ConAction(String sAct, String sTag,boolean isTemp, actionType type)
	{
		m_action = sAct;
		m_tag = sTag;
		code = size++;	//the code of a tag is the create order of the object, unique in the action space
		this.isTemp=isTemp;
		this.type=type;
	}
	
	public ConAction(String sAct, String sTag,int code,boolean isTemp, actionType type)
	{
		m_action = sAct;
		m_tag = sTag;
		this.code=code;
		this.isTemp=isTemp;
		this.type=type;
	}
	

	/**
	 * return labeled reduce constituent action
	 * if do not exits, insert into the hashmap
	 * else directly return
	 * 
	 * @param bBinary binary reduce or unary reduce
	 * @param bLeft after binary reduce, the head ConstituentLabel is in left or right child
	 * @param Temp after action, the node is temporary or not
	 * @param label the label of the newly generated node
	 * @return the constituent action 
	 * */
	public static ConAction getLabeledReduceAction(boolean bBinary, boolean bLeft,boolean Temp,String label)
	{	
		if(Temp){	//if the node is temporary
			if (bBinary)	//if the action is Binary
			{
				if(bLeft){	//head ConstituentLabel is in the left child
					if (!m_labeledBinaryReduceLTempActions.containsKey(label))
						m_labeledBinaryReduceLTempActions.put(label, new ConAction(m_actBinaryReduceLTemp, label,true,actionType.BINARY_REDUCE_LEFT));
					return m_labeledBinaryReduceLTempActions.get(label);
				}
				else{
					if (!m_labeledBinaryReduceRTempActions.containsKey(label))
						m_labeledBinaryReduceRTempActions.put(label, new ConAction(m_actBinaryReduceRTemp, label,true,actionType.BINARY_REDUCE_RIGHT));
					return m_labeledBinaryReduceRTempActions.get(label);
				}
			}
			else{
				throw new RuntimeException("Temporary unary node do not exit!");
			}
		}
		else{
			if (bBinary)
			{
				if(bLeft){
					if (!m_labeledBinaryReduceLActions.containsKey(label))
						m_labeledBinaryReduceLActions.put(label, new ConAction(m_actBinaryReduceL, label,false,actionType.BINARY_REDUCE_LEFT));
					return m_labeledBinaryReduceLActions.get(label);
				}
				else{
					if (!m_labeledBinaryReduceRActions.containsKey(label))
						m_labeledBinaryReduceRActions.put(label, new ConAction(m_actBinaryReduceR, label,false,actionType.BINARY_REDUCE_RIGHT));
					return m_labeledBinaryReduceRActions.get(label);
				}
			}
			else	//unary reduce , unary reduce mustn't be temporary
			{
				if (!m_labeledUnaryReduceActions.containsKey(label))
					m_labeledUnaryReduceActions.put(label, new ConAction(m_actUnaryReduce, label,false,actionType.UNARY_REDUCE));
				return m_labeledUnaryReduceActions.get(label);
			}
		}
	}
	
	public static ConAction getShiftTagAction(String sPos)
	{
		if (!m_shiftTagActions.containsKey(sPos))
			m_shiftTagActions.put(sPos, new ConAction(m_actShiftTag, sPos,actionType.SHIFT));
		return m_shiftTagActions.get(sPos);
	}
	
	public boolean isLeftReduce(){
		
		return (type==actionType.BINARY_REDUCE_LEFT);
	}
	
	public boolean isRightReduce(){
		
		return (type==actionType.BINARY_REDUCE_RIGHT);
	}
	
	public boolean isBinaryReduce(){
		return type==actionType.BINARY_REDUCE_LEFT||
				type==actionType.BINARY_REDUCE_RIGHT;
	}

	public boolean isLabeledUnaryReduce()
	{
		return type==actionType.UNARY_REDUCE;
	}
	
	public boolean isReduce(){
		return type==actionType.BINARY_REDUCE_LEFT||
				type==actionType.BINARY_REDUCE_RIGHT||
			   type==actionType.UNARY_REDUCE;
	}
	
	public boolean isShiftTagAction()
	{
		return m_action.equals(m_actShiftTag);
	}
	
	public boolean isShiftAction(){
		return type==actionType.SHIFT;
	}
	
	public boolean isEndAction(){
		return type==actionType.END;
	}
	
	/**
	 * returns a part of speech for tagging actions, and an arc label for reduce
	 * actions
	 * 
	 * @return
	 */
	public String getTag()
	{
		return m_tag;
	}

	public boolean isTemp(){
		
		return this.isTemp;
	
	}
	
	@Override
	public String toString()
	{
		return m_action + "-" + m_tag;
	}
	
	public boolean isSameType(ConAction act){
		
		return m_action.equals(act.m_action);
	}
	
	/**
	 * @return the unique code of the action
	 */
	public int code(){
		return code;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof ConAction))
			return false;
		ConAction a = (ConAction)o;
		return m_action.equals(a.m_action)
				&& m_tag.equals(a.m_tag);
	}

	@Override
	public int hashCode()
	{
		return code;
	}
	
}

