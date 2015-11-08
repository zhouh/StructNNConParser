package nncon;

import java.util.ArrayList;
import java.util.List;

public class ConParseInput {

	public List<String> words;
	public List<String> tags;
	public final int sentLen;
	public boolean withGoldTag;
	
	public String getWord(int i){
		return words.get(i);
	}
	
	public String getTag(int i){
		return tags.get(i);
	}
	
	/**
	 * the constructor 
	 * used in parsing with gold pos-tag
	 * */
	public ConParseInput(List<String> words,List<String> tags){
		if(words.size()!=tags.size())	//the pairs' length must match
			System.err.println("The words and tags pairs do not match!"); 
		this.words=words;
		this.tags=tags;
		this.withGoldTag=true;
		this.sentLen=words.size();
	}
	
	/**
	 * Constructor
	 * with a CFG tree  
	 * if addGoldTag==false, then tags=null
	 * */
	public ConParseInput(CFGTree cTree, boolean addGoldTag) {
		
		this.withGoldTag=addGoldTag;
		
		if(addGoldTag){
			List<String> wordsList=new ArrayList<String>();
			List<String> tagsList=new ArrayList<String>();
			for(int i=0;i<cTree.nodes.size();i++){
				CFGTreeNode node=cTree.nodes.get(i);
				if(!node.is_constituent) {
					wordsList.add(node.word);
					tagsList.add(node.constituent);
					node.constituent="NONE";
				}
			}
			words=wordsList;
			tags=tagsList;
		}
		else{
			List<String> wordsList=new ArrayList<String>();
			for(int i=0;i<cTree.nodes.size();i++){
				CFGTreeNode node=cTree.nodes.get(i);
				if(node.is_constituent) 
					wordsList.add(node.word);
			}
			words=wordsList;
			tags=null;
		}
		
		this.sentLen=words.size();
		
	}

}
