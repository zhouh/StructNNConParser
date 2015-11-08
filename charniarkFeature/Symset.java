package nncon.charniarkFeature;


import java.util.HashSet;

public class Symset extends HashSet<Symbol> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1740410426611107105L;

	public Symset(){
		set(null, ' ');
	}
	
	public Symset(String s){
		set(s, ' ');
	}
	
	public Symset(String s, char sep) {
		set(s, sep);
	}

	public void set(){
		set(null, ' ');
	}
	
	public void set(String s) {
		set(s, ' ');
	}
	public void set(String c, char sep) {
		this.clear();

		if (c != null) {
			int i = 0;
			for (StringBuilder s = new StringBuilder(""); true; ++i) {
				if (i == c.length() || c.charAt(i) == sep) {
					this.add(new Symbol(s.toString()));

					s = new StringBuilder("");

					if (i == c.length())
						break;
				} else {
					s.append(c.charAt(i));
				}

			}
		}
	}

	public boolean contains(Symbol s) {
		return super.contains(s);
	}
	
	public String toString(){
		StringBuilder res = new StringBuilder("");
		
		res.append("{");
		
		for (Symbol s : this) {
			res.append(s.toString());
			res.append(" ");
		}

		res.append("}");
		
		return res.toString();
	}
	
	public static void main(String args[]){
		Symset set = new Symset();
		set.set("a b c d e", ' ');
		
		System.out.println(set.toString());
	}
}
