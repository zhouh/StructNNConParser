package nncon.charniarkFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

public class SPData {
	static public class sp_parse_type {
		double logprob;
		double logcondprob;
		long nedges;
		long ncorrect;
		double f_score;
		SPTreeNode parse;
		TreeNode parse0;

		public sp_parse_type() {
			logprob = 0;
			logcondprob = 0;
			nedges = 0;
			ncorrect = 0;
			f_score = 0;
			parse = null;
			parse0 = null;
		}

		public String toString() {
			StringBuilder res = new StringBuilder("");

			res.append("(" + logprob + " ");
			res.append(logcondprob + " ");
			res.append(nedges + " ");
			res.append(ncorrect + " ");
			res.append(parse.toString() + ")");

			return res.toString();
		}

		public void read_ec_nbest(double logprob, String parse) {
			read_ec_nbest(logprob, parse, false);
		}

		public void read_ec_nbest(double logprob, String parse,
				boolean downcase_flag) {
			parse0 = null;
			try {
				parse0 = ReadTreeFlex.readTree(parse.trim());
			} catch (IOException e) {
				e.printStackTrace();
			}
			assert (parse0 != null);

			if (!Double.isFinite(logprob)) {
				System.err
						.println("## SPData error reading n-best parses: logprob read from treebank is not finite, logprob = "
								+ logprob);
				System.exit(0);
			}
			
			this.logprob = logprob;

			parse0.label.cat = TreeLabel.root();

			this.parse = SPTreeNode.tree_sptree(parse0, downcase_flag);
			assert (this.parse != null);
		}

		public boolean read(BufferedReader fp) throws IOException {
			return read(fp, false);
		}

		/**
		 * read score1 parse1
		 * 
		 * @param fp
		 * @param downcase_flag
		 * @return
		 * @throws IOException
		 */
		public boolean read(BufferedReader fp, boolean downcase_flag)
				throws IOException {
			String line = fp.readLine().trim();
			while (line.equals("")) {
				line = fp.readLine().trim();
			}

			String[] tokens = line.split("[ \t]+");

			if (tokens.length != 1) {
				System.err.println("## Only read " + tokens.length
						+ " of 1 parse header variables");
				System.exit(0);
			}

			this.logprob = Double.parseDouble(tokens[0]);

			if (logprob > 0) {
				System.err.println("## Positive logprob = " + logprob);

				return false;
			}

			line = fp.readLine().trim();
			if (line == null || line.equals("")) {
				System.err.println("## Reading parse failed.\n## buffer "
						+ line);
				return false;
			}

			parse0 = ReadTreeFlex.readTree(line);
			assert (parse0 != null);
			parse0.label.cat = TreeLabel.root();

			parse = SPTreeNode.tree_sptree(parse0, downcase_flag);

			return true;
		}
	}

	static public class sp_sentence_type {
		SPTreeNode gold;
		TreeNode gold0;
		long gold_nedges;
		double max_fscore;
		Vector<sp_parse_type> parses;
		double logsumprob;
		String label;

		public sp_sentence_type() {
			gold = null;
			gold0 = null;
			gold_nedges = 0;
			max_fscore = 0;
			logsumprob = 0;
			parses = new Vector<sp_parse_type>();
		}

		public sp_sentence_type(sp_sentence_type s) {
			gold_nedges = s.gold_nedges;
			max_fscore = s.max_fscore;
			parses = new Vector<sp_parse_type>(s.nparses());

			if (s.gold == null)
				gold = null;
			else
				gold = s.gold.copy_tree();

			for (int i = 0; i < s.nparses(); i++) {
				sp_parse_type new_parse = new sp_parse_type();

				sp_parse_type s_parse = s.parses.get(i);
				new_parse.logprob = s_parse.logprob;
				new_parse.logcondprob = s_parse.logcondprob;
				new_parse.ncorrect = s_parse.ncorrect;
				new_parse.nedges = s_parse.nedges;
				new_parse.f_score = s_parse.f_score;
				new_parse.parse = s_parse.parse.copy_tree();
				new_parse.parse0 = s_parse.parse0.copy_tree();
			}
		}

		public void set_logcondprob() {
			if (parses.size() > 0) {
				double logmaxprob = parses.get(0).logprob;

				for (int i = 1; i < parses.size(); i++)
					logmaxprob = Math.max(logmaxprob, parses.get(i).logprob);

				assert (Double.isFinite(logmaxprob));

				double sumprob_maxprob = 0;
				for (int i = 0; i < parses.size(); i++)
					sumprob_maxprob += Math.exp(parses.get(i).logprob
							- logmaxprob);

				assert (Double.isFinite(sumprob_maxprob));
				logsumprob = Math.log(sumprob_maxprob) + logmaxprob;
				assert (Double.isFinite(logsumprob));

				for (int i = 0; i < parses.size(); i++) {
					parses.get(i).logcondprob = parses.get(i).logprob
							- logsumprob;
					assert (Double.isFinite(parses.get(i).logcondprob));
				}
			}
		}

		public int nparses() {
			return parses.size();
		}

		public String toString() {
			StringBuilder res = new StringBuilder("");

			res.append("(");
			res.append(gold.toString() + " ");
			res.append(gold_nedges + " ");
			res.append(max_fscore + " ");
			res.append(parses.toString() + " ");
			res.append(logsumprob + " ");

			return res.toString();
		}

		public boolean read_ec_nbest(BufferedReader is) throws IOException {
			return read_ec_nbest(is, false);
		}

		public boolean read_ec_nbest(BufferedReader is, boolean downcase_flag)
				throws IOException {
			gold = null;
			gold0 = null;
			parses.clear();

			String line = is.readLine();
			
			while(line != null && line.trim().equals("")){
				line = is.readLine();
			}
			
			if(line == null)
				return false;
			
			int nparses = Integer.parseInt(line.trim().split("[\t ]+")[0]);
			assert (nparses > 0);

			parses.setSize(nparses);
			for(int i = 0; i < nparses; i++)
				parses.set(i, new sp_parse_type());
			
			for (int i = 0; i < nparses; i++) {
				line = is.readLine().trim();
				double logprob = Double.parseDouble(line);
				line = is.readLine().trim();
				String parse = line;
				parses.get(i).read_ec_nbest(logprob, parse, downcase_flag);

				assert (parses.get(i) != null);
			}

			/* MARK */
			set_logcondprob();
			
			return true;
		}

		public boolean read(BufferedReader parsefp, BufferedReader goldfp)
				throws IOException {
			return read(parsefp, goldfp, false);
		}

		/**
		 * for goldfp, read a single line sentencelabel goldparse for parsefp,
		 * read totalcount sentencelabel score1 parse1 score2 parse2 ... scoren
		 * parsen
		 * 
		 * @param parsefp
		 * @param goldfp
		 * @param downcase_flag
		 * @return
		 * @throws IOException
		 */
		public boolean read(BufferedReader parsefp, BufferedReader goldfp,
				boolean downcase_flag) throws IOException {
			gold = null;
			gold0 = null;

			parses.clear();

			int nparses;
			String line = parsefp.readLine().trim();
			while (line.equals("")) {
				line = parsefp.readLine().trim();
			}

			String tokens[] = line.split("[ \t]+");
			if (tokens.length != 2) {
				System.err.println("## Fatal error: Only read" + tokens.length
						+ " of 2 parse sentence header variables.");
				return false;
			}

			nparses = Integer.parseInt(tokens[0]);
			String parselabel = tokens[1];
			line = goldfp.readLine().trim();

			// split parselabel and parse
			int f_whitespace = -1;
			int f_non_whitespace = -1;
			for (int i = 0; i < line.length(); i++) {
				if (f_whitespace == -1
						&& (line.charAt(i) != ' ' && line.charAt(i) != '\t'))
					continue;

				if (f_whitespace == -1) {
					f_whitespace = i;
					continue;
				}

				if (f_whitespace != -1
						&& (line.charAt(i) == ' ' || line.charAt(i) == '\t'))
					continue;

				if (f_whitespace != -1) {
					f_non_whitespace = i;
					break;
				}
			}

			if (f_non_whitespace == -1 || f_whitespace == -1)
				return false;

			String goldlabel = line.substring(0, f_whitespace);

			if (!parselabel.equals(goldlabel)) {
				System.err.println("## Fatal error: parselabel = " + parselabel
						+ ", goldlabel = " + goldlabel);
				return false;
			}
			label = parselabel;

			line = line.substring(f_non_whitespace, line.length()).trim();

			try {
				this.gold0 = ReadTreeFlex.readTree(line);
			} catch (IOException e) {
				this.gold0 = null;
				e.printStackTrace();
			}
			assert (this.gold0 != null);
			gold0.label.cat = TreeLabel.root();

			TreeNode gold1 = gold0.copy_without_empties();

			// System.err.println(TreeNode.display_tree(gold1));

			gold = SPTreeNode.tree_sptree(gold1, downcase_flag);

			assert (gold != null);
			/* TODO */
			Vector<Symbol> gold_words = new Vector<Symbol>();
			gold.terminals(gold_words);

			parses.setSize(nparses);
			for (int i = 0; i < nparses; i++)
				parses.set(i, new sp_parse_type());

			for (int i = 0; i < nparses; i++)
				if (parses.get(i).read(parsefp, downcase_flag)) {
					Vector<Symbol> parse_words = new Vector<Symbol>();
					parses.get(i).parse.terminals(parse_words);

					boolean equ = true;
					if (gold_words.size() != parse_words.size())
						equ = false;

					for (int ii = 0; ii < gold_words.size(); ii++)
						if (!gold_words.get(ii).equalTo(parse_words.get(ii)))
							equ = false;

					if (!equ) {
						StringBuilder pBuilder = new StringBuilder("");
						StringBuilder gBuilder = new StringBuilder("");

						for (int ii = 0; ii < gold_words.size(); ii++) {
							pBuilder.append(parse_words.get(ii).getString()
									+ " ");
							gBuilder.append(gold_words.get(ii).getString()
									+ " ");
						}
						System.err.println("## Error on example " + label
								+ ", gold_words = " + gBuilder.toString()
								+ ", parse_words = " + pBuilder.toString());
						System.exit(0);
					}

					// TreeNode.precrec_type pr = new
					// TreeNode.precrec_type(ncommon, ngold, ntest)
					/* TODO */

				} else {
					System.err.println("## Reading parse tree " + i
							+ "failed. ");
				}

			set_logcondprob();

			return true;
		}
	}

	static public class sp_corpus_type {
		Vector<sp_sentence_type> sentences;

		public sp_corpus_type() {
			sentences = new Vector<sp_sentence_type>();
		}

		public int nsentences() {
			return sentences.size();
		}
	}
}
