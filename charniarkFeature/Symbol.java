package nncon.charniarkFeature;

import java.util.HashMap;
import java.util.HashSet;

public class Symbol {
	/**
	 * Providing a static method giving a hash function for a string
	 * 
	 * @author cc
	 *
	 */
	static public class hashstr {
		int hashcode(String s) {
			long h = 0;
			long g;

			for (int i = 0; i < s.length(); i++) {
				h = (h << 4) + s.charAt(i);
				if ((g = h & 0xf0000000) != 0) {
					h = h ^ (g >> 24);
					h = h ^ g;
				}
			}

			return (int) h;
		}
	}

	/**
	 * Single instance, maintaining a symbol table
	 * 
	 * @author cc
	 *
	 */
	static public class Table {
		static long counter;
		static HashMap<String, Long> table;
		static HashMap<Long, String> id_2_str;

		static Table instance = new Table();

		private Table() {
			counter = 1;
			table = new HashMap<String, Long>(65536);
			id_2_str = new HashMap<Long, String>(65536);
		}

		public static Table getInstance() {
			return instance;
		}

		public long insert(String s) {
			if (table.containsKey(s))
				return table.get(s);
			else {
				long old_counter = counter;
				table.put(s, old_counter);
				id_2_str.put(old_counter, s);
				counter++;
				return old_counter;
			}
		}

		public String id2str(long id) {
			String str = id_2_str.get(id);

			assert (str != null);

			return str;
		}

		public int size() {
			return table.size();
		}
	}

	private long sp;
	static private Table sym_table = Table.getInstance();

	static final char ESCAPE = '\\';
	static final char OPENQUOTE = '\"';
	static final char CLOSEQUOTE = '\"';
	static final String UNDEFINED = "%UNDEFINED%";

	public Symbol() {
		sp = 0;
	}

	public Symbol(Symbol symbol) {
		sp = symbol.sp;
	}
	
	public Symbol(String s) {
		sp = sym_table.insert(s);
	}

	public boolean is_defined() {
		return sp != 0;
	}

	public boolean is_undefined() {
		return !is_defined();
	}

	public String getString() {
		assert (is_defined());

		return sym_table.id2str(sp);
	}

//	static public Symbol readSymbolFromStream(StringReader sr) throws IOException {
//		StringBuilder str = new StringBuilder("");
//		
//		char c;
//		if((c = (char)(sr.read())) == -1) return null;
//		if(dont_escape(c) || c == ESCAPE) {
//			do {
//				if(c == ESCAPE) {
//					if(((c = (char)(sr.read())) == -1))
//						return 
//				}
//			} while(((c = (char)(sr.read())) != -1) && (dont_escape(c) || c == ESCAPE));
//		}
//	}
	static public String readStringFromFeatureString(String s) {
		StringBuilder os = new StringBuilder("");
		if(s.isEmpty()) 
			os.append(OPENQUOTE + CLOSEQUOTE);
		else {
			boolean needs_escapeing = false;
			
			for(int i = 0; i < s.length(); i++)
				if(!dont_escape(s.charAt(i))) {
					needs_escapeing = true;
					break;
				}
			
			if(needs_escapeing) {
				os.append(OPENQUOTE);
				for(int i = 0; i < s.length(); i++) {
					switch (s.charAt(i)) {
					case OPENQUOTE:
					case ESCAPE:
						os.append(ESCAPE);
						os.append(s.charAt(i));
						break;
					case '\007':
						os.append(ESCAPE);
						os.append('a');
						break;
					case '\b':
						os.append(ESCAPE);
						os.append('b');
						break;
					case '\f':
						os.append(ESCAPE);
						os.append('f');
						break;
					case '\n':
						os.append(ESCAPE);
						os.append('n');
						break;
					case '\r':
						os.append(ESCAPE);
						os.append('r');
						break;
					case '\t':
						os.append(ESCAPE);
						os.append('t');
						break;
					case '\013':
						os.append(ESCAPE);
						os.append('v');
						break;
					default:
						os.append(s.charAt(i));
						break;
					}
				}
				
				os.append(CLOSEQUOTE);
			} else {
				for(int i = 0; i < s.length(); i++)
					os.append(s.charAt(i));
			}
			
		}
		return os.toString();
	}
	
	static public String readFeatureStringFromString(String s) {
		StringBuilder str = new StringBuilder("");
		
		int len = s.length();
		
		char c = s.charAt(0);
		int index = 1;
		if(dont_escape(c) || c == ESCAPE) {
			do {
				if(c == ESCAPE) {
					if(index == len) return null;
					c = s.charAt(index);
					index++;
					str.append(escaped_char(c));
				} else 
					str.append(c);
				
				if(index == len)
					break;
				c = s.charAt(index);
				index++;
			} while (dont_escape(c) || c == ESCAPE);
			
			return (str.toString());
		} else if(c == OPENQUOTE) {
			if(index == len) return null;
			c = s.charAt(index);
			index++;
			while(c != CLOSEQUOTE) {
				if(c == ESCAPE) {
					if(index == len) return null;
					c = s.charAt(index);
					index++;
					str.append(escaped_char(c));
				} else {
					str.append(c);
				}
				
				if(index == len)
					return null;
				c = s.charAt(index);
				index++;
			}
			
			return (str.toString());
		} else
			return null;
	}
	static public Symbol readSymbolFromString(String s) {
		StringBuilder str = new StringBuilder("");
		
		int len = s.length();
		
		char c = s.charAt(0);
		int index = 1;
		if(dont_escape(c) || c == ESCAPE) {
			do {
				if(c == ESCAPE) {
					if(index == len) return null;
					c = s.charAt(index);
					index++;
					str.append(escaped_char(c));
				} else 
					str.append(c);
				
				if(index == len)
					break;
				c = s.charAt(index);
				index++;
			} while (dont_escape(c) || c == ESCAPE);
			
			return new Symbol(str.toString());
		} else if(c == OPENQUOTE) {
			if(index == len) return null;
			c = s.charAt(index);
			index++;
			while(c != CLOSEQUOTE) {
				if(c == ESCAPE) {
					if(index == len) return null;
					c = s.charAt(index);
					index++;
					str.append(escaped_char(c));
				} else {
					str.append(c);
				}
				
				if(index == len)
					return null;
				c = s.charAt(index);
				index++;
			}
			
			return new Symbol(str.toString());
		} else if(c == UNDEFINED.charAt(0)) {
			if(s.equals(UNDEFINED)) {
				return Symbol.undefined();
			} else {
				return null;
			}
		} else
			return null;
	}
	
	public String toString() {
		if (is_undefined())
			return UNDEFINED;
		else {
			String str = sym_table.id2str(sp);

			if (str.isEmpty())
				return "" + OPENQUOTE + CLOSEQUOTE;
			else {
				StringBuilder res = new StringBuilder("");
				for (int i = 0; i < str.length(); i++) {
					if (!dont_escape(str.charAt(i))) {
						res.append(ESCAPE);
					}
					res.append(str.charAt(i));
				}
				return res.toString();
			}

		}
	}

	static public Symbol undefined() {
		Symbol res = new Symbol();

		return res;
	}

	static public long tableSize() {
		return sym_table.size();
	}

	public boolean equalTo(Symbol s) {
		return this.sp == s.sp;
	}

	public boolean notEqualTo(Symbol s) {
		return !equals(s);
	}

	public boolean lessThan(Symbol s) {
		return this.sp < s.sp;
	}

	public boolean geqTo(Symbol s) {
		return !lessThan(s);
	}

	public boolean leqTo(Symbol s) {
		return this.sp <= s.sp;
	}

	public boolean greaterThan(Symbol s) {
		return !leqTo(s);
	}

	@Override
	public int hashCode() {
		return (int) sp;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Symbol other = (Symbol) obj;

		if (sp != other.sp)
			return false;

		return true;
	}

	static public boolean dont_escape(char c) {
		return Symbol.isgraph(c) && c != '%' && c != '(' && c != ')'
				&& c != ESCAPE && c != OPENQUOTE && c != CLOSEQUOTE;

	}

	static public boolean isgraph(char c) {
		int c_v = (int)c;
		return (c_v >= 33 && c_v <= 126);
	}

	static public char escaped_char(char c) {
		switch (c) {
		case 'a':
			return '\007';
		case 'b':
			return '\b';
		case 'f':
			return '\f';
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		case 'v':
			return '\013';
		default:
			return c;
		}
	}

	public static void main(String[] args) {
//		final int ns = 1000000;

		String syms[] = { "Hello world", "1", "2.0e-5", "this", "is", "a",
				"test", "_"};

		int nsyms = syms.length;

		HashSet<Symbol> s = new HashSet<Symbol>();
		for (int i = 0; i < nsyms; i++)
			s.add(new Symbol(syms[i]));

		s.add(Symbol.undefined());

		for (Symbol symbol : s) {
			System.err.println(symbol.getString() + " ");
		}
//		Vector<Symbol> ss = new Vector<Symbol>();
//		for (int i = 0; i < ns; i++) {
//			ss.add(new Symbol("" + i));
//		}
//
//		System.out.println("table's size is: " + Symbol.tableSize());
//
//		for (int i = 0; i < ns; i++) {
//			ss.add(new Symbol("" + i));
//		}
//
//		System.out.println("table's size is: " + Symbol.tableSize());
	}
}
