/* The following code was generated by JFlex 1.6.0 */

package nncon.charniarkFeature;

/* JFlex example: part of Java language lexer specification */
import java_cup.sym;

import java.util.Stack;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.StringReader;

/**
 * This class is a simple example lexer.
 */

class ReadTreeFlex implements java_cup.runtime.Scanner {

	/** This character denotes the end of file */
	public static final int YYEOF = -1;

	/** initial size of the lookahead buffer */
	private static final int ZZ_BUFFERSIZE = 16384;

	/** lexical states */
	public static final int YYINITIAL = 0;
	public static final int RT = 2;
	public static final int RTC = 4;
	public static final int FC = 6;
	public static final int NC = 8;
	public static final int CAT = 10;
	public static final int PC = 12;
	public static final int IND = 14;

	/**
	 * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
	 * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l at the
	 * beginning of a line l is of the form l = 2*k, k a non negative integer
	 */
	private static final int ZZ_LEXSTATE[] = { 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5,
			5, 6, 6, 0, 0 };

	/**
	 * Translates characters to character classes
	 */
	private static final String ZZ_CMAP_PACKED = "\11\0\1\2\1\16\1\17\1\17\1\17\22\0\1\2\3\0\1\12"
			+ "\3\0\1\1\1\3\1\12\1\15\1\0\1\4\1\11\1\0\12\13"
			+ "\3\0\1\15\1\0\1\10\1\0\4\14\1\7\10\14\1\5\1\6"
			+ "\13\14\41\0\1\15\10\0\1\17\u1fa2\0\1\17\1\17\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\udfe6\0";

	/**
	 * Translates characters to character classes
	 */
	private static final char[] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

	/**
	 * Translates DFA states to action switch labels.
	 */
	private static final int[] ZZ_ACTION = zzUnpackAction();

	private static final String ZZ_ACTION_PACKED_0 = "\7\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7"
			+ "\1\10\1\11\1\12\1\13\2\14\2\15\3\16\1\14"
			+ "\1\0\1\14\1\17\1\20\1\21\1\14\1\15\2\14" + "\1\22";

	private static int[] zzUnpackAction() {
		int[] result = new int[36];
		int offset = 0;
		offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
		return result;
	}

	private static int zzUnpackAction(String packed, int offset, int[] result) {
		int i = 0; /* index in packed string */
		int j = offset; /* index in unpacked array */
		int l = packed.length();
		while (i < l) {
			int count = packed.charAt(i++);
			int value = packed.charAt(i++);
			do
				result[j++] = value;
			while (--count > 0);
		}
		return j;
	}

	/**
	 * Translates a state to a row index in the transition table
	 */
	private static final int[] ZZ_ROWMAP = zzUnpackRowMap();

	private static final String ZZ_ROWMAP_PACKED_0 = "\0\0\0\20\0\40\0\60\0\100\0\120\0\140\0\160"
			+ "\0\200\0\160\0\160\0\160\0\160\0\220\0\160\0\160"
			+ "\0\160\0\160\0\240\0\260\0\300\0\320\0\160\0\340"
			+ "\0\360\0\u0100\0\u0110\0\u0110\0\360\0\u0120\0\u0130\0\u0140"
			+ "\0\u0110\0\u0150\0\u0160\0\240";

	private static int[] zzUnpackRowMap() {
		int[] result = new int[36];
		int offset = 0;
		offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
		return result;
	}

	private static int zzUnpackRowMap(String packed, int offset, int[] result) {
		int i = 0; /* index in packed string */
		int j = offset; /* index in unpacked array */
		int l = packed.length();
		while (i < l) {
			int high = packed.charAt(i++) << 16;
			result[j++] = high | packed.charAt(i++);
		}
		return j;
	}

	/**
	 * The transition table of the DFA
	 */
	private static final int[] ZZ_TRANS = zzUnpackTrans();

	private static final String ZZ_TRANS_PACKED_0 = "\2\10\1\11\13\10\1\12\1\13\1\10\1\14\1\11"
			+ "\13\10\1\12\1\13\1\10\1\15\1\11\13\10\1\12"
			+ "\1\13\1\16\1\17\1\11\1\20\12\16\1\12\1\16"
			+ "\1\10\1\21\1\11\1\22\12\10\1\12\1\13\1\23"
			+ "\1\10\1\11\1\10\1\24\3\25\1\26\1\23\3\25"
			+ "\1\23\1\12\1\23\4\27\1\30\10\27\1\31\24\0"
			+ "\1\11\15\0\1\16\3\0\12\16\1\0\1\16\1\23"
			+ "\3\0\12\23\1\0\2\23\3\0\1\23\1\32\10\23"
			+ "\1\0\1\23\5\0\4\25\1\33\3\25\3\0\1\23"
			+ "\3\0\1\23\4\26\1\34\3\26\1\23\1\0\1\23"
			+ "\1\35\4\0\3\36\3\35\1\37\1\36\1\35\1\0"
			+ "\2\35\4\0\11\35\1\0\1\35\1\23\3\0\2\23"
			+ "\1\40\7\23\1\0\1\23\1\41\3\0\12\41\1\0"
			+ "\1\41\1\35\4\0\3\36\4\35\1\36\1\35\1\0"
			+ "\2\35\4\0\6\35\1\37\2\35\1\0\1\35\1\23"
			+ "\3\0\1\23\1\42\10\23\1\0\2\23\3\0\3\23"
			+ "\1\43\6\23\1\0\2\23\3\0\1\44\11\23\1\0" + "\1\23";

	private static int[] zzUnpackTrans() {
		int[] result = new int[368];
		int offset = 0;
		offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
		return result;
	}

	private static int zzUnpackTrans(String packed, int offset, int[] result) {
		int i = 0; /* index in packed string */
		int j = offset; /* index in unpacked array */
		int l = packed.length();
		while (i < l) {
			int count = packed.charAt(i++);
			int value = packed.charAt(i++);
			value--;
			do
				result[j++] = value;
			while (--count > 0);
		}
		return j;
	}

	/* error codes */
	private static final int ZZ_UNKNOWN_ERROR = 0;
	private static final int ZZ_NO_MATCH = 1;
	private static final int ZZ_PUSHBACK_2BIG = 2;

	/* error messages for the codes above */
	private static final String ZZ_ERROR_MSG[] = {
			"Unkown internal scanner error", "Error: could not match input",
			"Error: pushback value was too large" };

	/**
	 * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
	 */
	private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();

	private static final String ZZ_ATTRIBUTE_PACKED_0 = "\7\0\1\11\1\1\4\11\1\1\4\11\4\1\1\11"
			+ "\3\1\1\0\11\1";

	private static int[] zzUnpackAttribute() {
		int[] result = new int[36];
		int offset = 0;
		offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
		return result;
	}

	private static int zzUnpackAttribute(String packed, int offset, int[] result) {
		int i = 0; /* index in packed string */
		int j = offset; /* index in unpacked array */
		int l = packed.length();
		while (i < l) {
			int count = packed.charAt(i++);
			int value = packed.charAt(i++);
			do
				result[j++] = value;
			while (--count > 0);
		}
		return j;
	}

	/** the input device */
	private java.io.Reader zzReader;

	/** the current state of the DFA */
	private int zzState;

	/** the current lexical state */
	private int zzLexicalState = YYINITIAL;

	/**
	 * this buffer contains the current text to be matched and is the source of
	 * the yytext() string
	 */
	private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

	/** the textposition at the last accepting state */
	private int zzMarkedPos;

	/** the current text position in the buffer */
	private int zzCurrentPos;

	/** startRead marks the beginning of the yytext() string in the buffer */
	private int zzStartRead;

	/**
	 * endRead marks the last character in the buffer, that has been read from
	 * input
	 */
	private int zzEndRead;

	/** number of newlines encountered up to the start of the matched text */
	@SuppressWarnings("unused")
	private int yyline;

	/** the number of characters up to the start of the matched text */
	@SuppressWarnings("unused")
	private int yychar;

	/**
	 * the number of characters from the last newline up to the start of the
	 * matched text
	 */
	@SuppressWarnings("unused")
	private int yycolumn;

	/**
	 * zzAtBOL == true <=> the scanner is currently at the beginning of a line
	 */
	@SuppressWarnings("unused")
	private boolean zzAtBOL = true;

	/** zzAtEOF == true <=> the scanner is at the EOF */
	private boolean zzAtEOF;

	/** denotes if the user-EOF-code has already been executed */
	private boolean zzEOFDone;

	/**
	 * The number of occupied positions in zzBuffer beyond zzEndRead. When a
	 * lead/high surrogate has been read from the input stream into the final
	 * zzBuffer position, this will have a value of 1; otherwise, it will have a
	 * value of 0.
	 */
	private int zzFinalHighSurrogate = 0;

	/* user code: */
	static int readtree_line_no = 1;
	static String readtree_name = "filename";

	static void message(String s1, String s2) {
		System.err.println(readtree_name + ":" + readtree_line_no + ": " + s1
				+ " " + s2);
		// OutputToFile.output(readtree_name + ":" + readtree_line_no + ": " +
		// s1 + " " + s2);
	}

	static public TreeNode readTree(String input) throws IOException {
		return readTree(input, false);
	}
	
	static public TreeNode readTree(String input, boolean downcase_flag) throws IOException {

		BufferedReader reader = new BufferedReader(new StringReader(input));

		ReadTreeFlex readTree = new ReadTreeFlex(reader, downcase_flag);

		readTree.yybegin(RTC);

		readTree.next_token();

		reader.close();

		return readTree.root;
	}

	static public void main(String[] args) {
		try {
			System.setErr(new PrintStream(new File(OutputToFile.filePath)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		String str1 = "(S1 (S (PP-LOC (IN In) (NP (NP (DT an) (NNP Oct.) (CD 19) (NN review)) (PP (IN of) (NP (`` ``) (NP-TTL (DT The) (NN Misanthrope)) ('' '') (PP-LOC (IN at) (NP (NP (NNP Chicago) (POS 's)) (NNP Goodman) (NNP Theatre))))) (PRN (-LRB- -LRB-) (`` ``) (S-HLN (NP-SBJ (VBN Revitalized) (NNS Classics)) (VP (VBP Take) (NP (DT the) (NN Stage)) (PP-LOC (IN in) (NP (NNP Windy) (NNP City))))) (, ,) ('' '') (NP-TMP (NN Leisure) (CC &) (NNS Arts)) (-RRB- -RRB-)))) (, ,) (NP-SBJ-2 (NP (NP (DT the) (NN role)) (PP (IN of) (NP (NNP Celimene)))) (, ,) (VP (VBN played) (NP (-NONE- *)) (PP (IN by) (NP-LGS (NNP Kim) (NNP Cattrall)))) (, ,)) (VP (VBD was) (VP (ADVP-MNR (RB mistakenly)) (VBN attributed) (NP (-NONE- *-2)) (PP-CLR (TO to) (NP (NNP Christina) (NNP Haag))))) (. .)))";
		String str2 = "(S1 (S (NP-SBJ (NNP Ms.) (NNP Haag)) (VP (VBZ plays) (NP (NNP Elianti))) (. .)))";
		String str3 = "(S1 (S (NP-SBJ (NNP Rolls-Royce) (NNP Motor) (NNPS Cars) (NNP Inc.)) (VP (VBD said) (SBAR (-NONE- 0) (S (NP-SBJ (PRP it)) (VP (VBZ expects) (S (NP-SBJ (PRP$ its) (NNP U.S.) (NNS sales)) (VP (TO to) (VP (VB remain) (ADJP-PRD (JJ steady)) (PP-LOC-CLR (IN at) (NP (QP (IN about) (CD 1,200)) (NNS cars))) (PP-TMP (IN in) (NP (CD 1990)))))))))) (. .)))";
		
		try {
			ReadTreeFlex.readTree(str1, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ReadTreeFlex.readTree(str2, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ReadTreeFlex.readTree(str3, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	ReadTreeFlex(java.io.Reader in) {
		this(in, true);
	}

	ReadTreeFlex(java.io.Reader in, boolean downcase_flag) {
		this.zzReader = in;
		s = new Stack<TreeNode>();
		this.downcase_flag = downcase_flag;
	}

	TreeNode root = null;
	Stack<TreeNode> s;
	boolean downcase_flag;

	/**
	 * Creates a new scanner
	 *
	 * @param in
	 *            the java.io.Reader to read input from.
	 */

	/**
	 * Unpacks the compressed character translation table.
	 *
	 * @param packed
	 *            the packed character translation table
	 * @return the unpacked character translation table
	 */
	private static char[] zzUnpackCMap(String packed) {
		char[] map = new char[0x110000];
		int i = 0; /* index in packed string */
		int j = 0; /* index in unpacked array */
		while (i < 110) {
			int count = packed.charAt(i++);
			char value = packed.charAt(i++);
			do
				map[j++] = value;
			while (--count > 0);
		}
		return map;
	}

	/**
	 * Refills the input buffer.
	 *
	 * @return <code>false</code>, iff there was new input.
	 * 
	 * @exception java.io.IOException
	 *                if any I/O-Error occurs
	 */
	private boolean zzRefill() throws java.io.IOException {

		/* first: make room (if you can) */
		if (zzStartRead > 0) {
			zzEndRead += zzFinalHighSurrogate;
			zzFinalHighSurrogate = 0;
			System.arraycopy(zzBuffer, zzStartRead, zzBuffer, 0, zzEndRead
					- zzStartRead);

			/* translate stored positions */
			zzEndRead -= zzStartRead;
			zzCurrentPos -= zzStartRead;
			zzMarkedPos -= zzStartRead;
			zzStartRead = 0;
		}

		/* is the buffer big enough? */
		if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
			/* if not: blow it up */
			char newBuffer[] = new char[zzBuffer.length * 2];
			System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
			zzBuffer = newBuffer;
			zzEndRead += zzFinalHighSurrogate;
			zzFinalHighSurrogate = 0;
		}

		/* fill the buffer with new input */
		int requested = zzBuffer.length - zzEndRead;
		int totalRead = 0;
		while (totalRead < requested) {
			int numRead = zzReader.read(zzBuffer, zzEndRead + totalRead,
					requested - totalRead);
			if (numRead == -1) {
				break;
			}
			totalRead += numRead;
		}

		if (totalRead > 0) {
			zzEndRead += totalRead;
			if (totalRead == requested) { /* possibly more input available */
				if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
					--zzEndRead;
					zzFinalHighSurrogate = 1;
				}
			}
			return false;
		}

		// totalRead = 0: End of stream
		return true;
	}

	/**
	 * Closes the input stream.
	 */
	public final void yyclose() throws java.io.IOException {
		zzAtEOF = true; /* indicate end of file */
		zzEndRead = zzStartRead; /* invalidate buffer */

		if (zzReader != null)
			zzReader.close();
	}

	/**
	 * Resets the scanner to read from a new input stream. Does not close the
	 * old reader.
	 *
	 * All internal variables are reset, the old input stream <b>cannot</b> be
	 * reused (internal buffer is discarded and lost). Lexical state is set to
	 * <tt>ZZ_INITIAL</tt>.
	 *
	 * Internal scan buffer is resized down to its initial length, if it has
	 * grown.
	 *
	 * @param reader
	 *            the new input stream
	 */
	public final void yyreset(java.io.Reader reader) {
		zzReader = reader;
		zzAtBOL = true;
		zzAtEOF = false;
		zzEOFDone = false;
		zzEndRead = zzStartRead = 0;
		zzCurrentPos = zzMarkedPos = 0;
		zzFinalHighSurrogate = 0;
		yyline = yychar = yycolumn = 0;
		zzLexicalState = YYINITIAL;
		if (zzBuffer.length > ZZ_BUFFERSIZE)
			zzBuffer = new char[ZZ_BUFFERSIZE];
	}

	/**
	 * Returns the current lexical state.
	 */
	public final int yystate() {
		return zzLexicalState;
	}

	/**
	 * Enters a new lexical state
	 *
	 * @param newState
	 *            the new lexical state
	 */
	public final void yybegin(int newState) {
		zzLexicalState = newState;
	}

	/**
	 * Returns the text matched by the current regular expression.
	 */
	public final String yytext() {
		return new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
	}

	/**
	 * Returns the character at position <tt>pos</tt> from the matched text.
	 * 
	 * It is equivalent to yytext().charAt(pos), but faster
	 *
	 * @param pos
	 *            the position of the character to fetch. A value from 0 to
	 *            yylength()-1.
	 *
	 * @return the character at position pos
	 */
	public final char yycharat(int pos) {
		return zzBuffer[zzStartRead + pos];
	}

	/**
	 * Returns the length of the matched text region.
	 */
	public final int yylength() {
		return zzMarkedPos - zzStartRead;
	}

	/**
	 * Reports an error that occured while scanning.
	 *
	 * In a wellformed scanner (no or only correct usage of yypushback(int) and
	 * a match-all fallback rule) this method will only be called with things
	 * that "Can't Possibly Happen". If this method is called, something is
	 * seriously wrong (e.g. a JFlex bug producing a faulty scanner etc.).
	 *
	 * Usual syntax/scanner level error handling should be done in error
	 * fallback rules.
	 *
	 * @param errorCode
	 *            the code of the errormessage to display
	 */
	private void zzScanError(int errorCode) {
		String message;
		try {
			message = ZZ_ERROR_MSG[errorCode];
		} catch (ArrayIndexOutOfBoundsException e) {
			message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
		}

		throw new Error(message);
	}

	/**
	 * Pushes the specified amount of characters back into the input stream.
	 *
	 * They will be read again by then next call of the scanning method
	 *
	 * @param number
	 *            the number of characters to be read again. This number must
	 *            not be greater than yylength()!
	 */
	public void yypushback(int number) {
		if (number > yylength())
			zzScanError(ZZ_PUSHBACK_2BIG);

		zzMarkedPos -= number;
	}

	/**
	 * Contains user EOF-code, which will be executed exactly once, when the end
	 * of file is reached
	 */
	private void zzDoEOF() throws java.io.IOException {
		if (!zzEOFDone) {
			zzEOFDone = true;
			yyclose();
		}
	}

	/**
	 * Resumes scanning until the next regular expression is matched, the end of
	 * input is encountered or an I/O-Error occurs.
	 *
	 * @return the next token
	 * @exception java.io.IOException
	 *                if any I/O-Error occurs
	 */
	public java_cup.runtime.Symbol next_token() throws java.io.IOException {
		int zzInput;
		int zzAction;

		// cached fields:
		int zzCurrentPosL;
		int zzMarkedPosL;
		int zzEndReadL = zzEndRead;
		char[] zzBufferL = zzBuffer;
		char[] zzCMapL = ZZ_CMAP;

		int[] zzTransL = ZZ_TRANS;
		int[] zzRowMapL = ZZ_ROWMAP;
		int[] zzAttrL = ZZ_ATTRIBUTE;

		while (true) {
			zzMarkedPosL = zzMarkedPos;

			boolean zzR = false;
			int zzCh;
			int zzCharCount;
			for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL; zzCurrentPosL += zzCharCount) {
				zzCh = Character.codePointAt(zzBufferL, zzCurrentPosL,
						zzMarkedPosL);
				zzCharCount = Character.charCount(zzCh);
				switch (zzCh) {
				case '\u000B':
				case '\u000C':
				case '\u0085':
				case '\u2028':
				case '\u2029':
					yyline++;
					yycolumn = 0;
					zzR = false;
					break;
				case '\r':
					yyline++;
					yycolumn = 0;
					zzR = true;
					break;
				case '\n':
					if (zzR)
						zzR = false;
					else {
						yyline++;
						yycolumn = 0;
					}
					break;
				default:
					zzR = false;
					yycolumn += zzCharCount;
				}
			}

			if (zzR) {
				// peek one character ahead if it is \n (if we have counted one
				// line too much)
				boolean zzPeek;
				if (zzMarkedPosL < zzEndReadL)
					zzPeek = zzBufferL[zzMarkedPosL] == '\n';
				else if (zzAtEOF)
					zzPeek = false;
				else {
					boolean eof = zzRefill();
					zzEndReadL = zzEndRead;
					zzMarkedPosL = zzMarkedPos;
					zzBufferL = zzBuffer;
					if (eof)
						zzPeek = false;
					else
						zzPeek = zzBufferL[zzMarkedPosL] == '\n';
				}
				if (zzPeek)
					yyline--;
			}
			zzAction = -1;

			zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

			zzState = ZZ_LEXSTATE[zzLexicalState];

			// set up zzAction for empty match case:
			int zzAttributes = zzAttrL[zzState];
			if ((zzAttributes & 1) == 1) {
				zzAction = zzState;
			}

			zzForAction: {
				while (true) {

					if (zzCurrentPosL < zzEndReadL) {
						zzInput = Character.codePointAt(zzBufferL,
								zzCurrentPosL, zzEndReadL);
						zzCurrentPosL += Character.charCount(zzInput);
					} else if (zzAtEOF) {
						zzInput = YYEOF;
						break zzForAction;
					} else {
						// store back cached positions
						zzCurrentPos = zzCurrentPosL;
						zzMarkedPos = zzMarkedPosL;
						boolean eof = zzRefill();
						// get translated positions and possibly new buffer
						zzCurrentPosL = zzCurrentPos;
						zzMarkedPosL = zzMarkedPos;
						zzBufferL = zzBuffer;
						zzEndReadL = zzEndRead;
						if (eof) {
							zzInput = YYEOF;
							break zzForAction;
						} else {
							zzInput = Character.codePointAt(zzBufferL,
									zzCurrentPosL, zzEndReadL);
							zzCurrentPosL += Character.charCount(zzInput);
						}
					}
					int zzNext = zzTransL[zzRowMapL[zzState] + zzCMapL[zzInput]];
					if (zzNext == -1)
						break zzForAction;
					zzState = zzNext;

					zzAttributes = zzAttrL[zzState];
					if ((zzAttributes & 1) == 1) {
						zzAction = zzState;
						zzMarkedPosL = zzCurrentPosL;
						if ((zzAttributes & 8) == 8)
							break zzForAction;
					}

				}
			}

			// store back cached position
			zzMarkedPos = zzMarkedPosL;

			switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
			case 1: {
				message("Unexpected character", yytext());
				System.err.println("Parse tree so far: " + root + "\n");
				System.exit(0);
			}
			case 19:
				break;
			case 2: {
//				message("in whitespace ", yytext());
			}
			case 20:
				break;
			case 3: {
				readtree_line_no++;
			}
			case 21:
				break;
			case 4: {
				throw new Error("Illegal character <" + yytext() + ">");
			}
			case 22:
				break;
			case 5: {
//				message("in RT\"(\"", yytext());
				assert (s.empty());
				root = new TreeNode();
				/* TODO */
				s.push(root);
				s.peek().label.cat = TreeLabel.root();
//				System.err.println(TreeNode.display_tree(root));
				yybegin(FC);
			}
			case 23:
				break;
			case 6: {
//				message("in RTC\"(\" ", yytext());
				assert (s.empty());
				root = new TreeNode();
				s.push(root);
//				System.err.println(TreeNode.display_tree(root));
				yybegin(CAT);
			}
			case 24:
				break;
			case 7: {
//				message("in FC1 ", yytext());
				assert (!s.empty());
				/* TODO */
				s.peek().child = new TreeNode();
				s.push(s.peek().child);
				if (downcase_flag)
					s.peek().label.cat = new Symbol(yytext().toLowerCase());
				else 
					s.peek().label.cat = new Symbol(yytext());
//				System.err.println(TreeNode.display_tree(root));
				yybegin(NC);
			}
			case 25:
				break;
			case 8: {
//				message("in <FC>\"(\" ", yytext());
				assert (!s.empty());
				s.peek().child = new TreeNode();
				s.push(s.peek().child);
//				System.err.println(TreeNode.display_tree(root));
				/* TODO */
				yybegin(CAT);
			}
			case 26:
				break;
			case 9: {
//				message("in FC2 ", yytext());
				assert (!s.empty());
				s.pop();
//				System.err.println(TreeNode.display_tree(root));
				if (s.size() == 1)
					return new java_cup.runtime.Symbol(sym.EOF);
				yybegin(NC);
			}
			case 27:
				break;
			case 10: {
//				message("in NC2 ", yytext());
				assert (!s.empty());
				/* TODO */
				s.peek().next = new TreeNode();
				s.set(s.size() - 1, s.peek().next);
//				System.err.println(TreeNode.display_tree(root));
				yybegin(CAT);
			}
			case 28:
				break;
			case 11: {
//				message("in NC1 ", yytext());
				assert (!s.empty());
				s.pop();
//				System.err.println(TreeNode.display_tree(root));
				if (s.size() == 1)
					return new java_cup.runtime.Symbol(sym.EOF);
			}
			case 29:
				break;
			case 12: {
//				message("in CAT3 ", yytext());
				assert (!s.empty());
				/* TODO */
				s.peek().label.cat = new Symbol(yytext());
//				System.err.println(TreeNode.display_tree(root));
				yybegin(PC);
			}
			case 30:
				break;
			case 13: {
//				message("in CAT2 ", yytext());
				assert (!s.empty());
				/* TODO */
				s.peek().label.cat = new Symbol(yytext());
//				System.err.println(TreeNode.display_tree(root));
				yybegin(PC);
			}
			case 31:
				break;
			case 14: {
//				message("in PC4 ", yytext());
				yypushback(1);
				/* TODO */
				yybegin(FC);
			}
			case 32:
				break;
			case 15: {
//				message("in PC3 ", yytext());
			}
			case 33:
				break;
			case 16: {
//				message("in PC2 ", yytext());
			}
			case 34:
				break;
			case 17: {
//				message("in PC1 ", yytext());
			}
			case 35:
				break;
			case 18: {
//				message("in CAT1 ", yytext());
				assert (!s.empty());
				/* TODO */
				s.peek().label.cat = new Symbol(yytext());
				s.peek().child = new TreeNode();
				s.push(s.peek().child);
//				System.err.println(TreeNode.display_tree(root));
			}
			case 36:
				break;
			default:
				if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
					zzAtEOF = true;
					zzDoEOF();
					switch (zzLexicalState) {
					case RT: {
//						message("in <RT><<EOF>> ", yytext());
						assert (s.empty());
						return new java_cup.runtime.Symbol(sym.EOF);
					}
					case 37:
						break;
					case RTC: {
//						message("in <RTC><<EOF>> ", yytext());
						assert (s.empty());
						return new java_cup.runtime.Symbol(sym.EOF);
					}
					case 38:
						break;
					default: {
						return new java_cup.runtime.Symbol(sym.EOF);
					}
					}
				} else {
					zzScanError(ZZ_NO_MATCH);
				}
			}
		}
	}

}