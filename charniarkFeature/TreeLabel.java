package nncon.charniarkFeature;

public class TreeLabel {
	public Symbol cat; //label category
	
	static final Symbol ROOT_SYMBOL = new Symbol("S1");
	static final Symbol NONE_SYMBOL = new Symbol("-NONE-");
	static final Symset PUNCTUATION_SYMSET = new Symset("'' : # , . `` -LRB- -RRB-");
	static final Symset CONJUNCTION_SYMSET = new Symset("CC CONJP");
	static final Symset CLOSED_CLASS_SYMSET = new Symset("CC DT EX IN MD PDT POS PRP PRP$ PRT RP TO UH WDT WP WP$");
	static final Symset FUNCTIONAL_SYMSET = new Symset("CC DT EX IN MD POS PRP PRP$ RP TO WDT WP WP$");
	
	public TreeLabel(){
		this(Symbol.undefined());
	}
	
	public TreeLabel(TreeLabel label){
		this.cat = new Symbol(label.cat);
	}
	
	public TreeLabel(Symbol cat){
		this.cat = new Symbol(cat);
	}
	
	public boolean equalTo(TreeLabel l){
		return cat.equalTo(l.cat);
	}
	
	public boolean lessThan(TreeLabel l){
		return cat.lessThan(l.cat);
	}
	
	static Symbol root() {
		return ROOT_SYMBOL;
	}
	
	static Symbol none() {
		return NONE_SYMBOL;
	}
	
	static Symset punctuation(){
		return PUNCTUATION_SYMSET;
	}
	
	static Symset conjunction(){
		return CONJUNCTION_SYMSET;
	}
	
	static Symset closed_class(){
		return CLOSED_CLASS_SYMSET;
	}
	
	static Symset functional(){
		return FUNCTIONAL_SYMSET;
	}
	
	public boolean is_root() {
		return cat.equalTo(root());
	}
	
	public boolean is_none() {
		return cat.equalTo(none());
	}
	
	public boolean is_punctuation(){
		return punctuation().contains(cat);
	}
	
	public boolean is_conjunction() {
		return conjunction().contains(cat);
	}
	
	public boolean is_closed_class() {
		return closed_class().contains(cat);
	}
	
	public boolean is_functional() {
		return functional().contains(cat);
	}
	
	public Symbol simplified_cat() {
		String s = cat.getString();
		
		int pos = s.indexOf("-");
		if(pos != -1 && pos + 1 < s.length()){
			return new Symbol(s.substring(0, pos));
		} else {
			return cat;
		}
	}
	
	@Override
	public int hashCode(){
		return this.cat.hashCode();
	}
	
	public String toString(){
		if(!cat.is_defined()) return "%undefined%";
		
		StringBuilder stringBuilder = new StringBuilder("");
		
		stringBuilder.append(this.cat.getString());
		
		return stringBuilder.toString();
	}
}
