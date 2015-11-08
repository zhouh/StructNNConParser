package nncon.charniarkFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import nncon.charniarkFeature.RuleFeatureClass.annotation_level;
import nncon.charniarkFeature.SPData.sp_parse_type;
import nncon.charniarkFeature.SPData.sp_sentence_type;

public class FeatureClassPtrs extends Vector<FeatureClass> {
	private static final long serialVersionUID = 1L;

	static class extract_features_visitor {
		FeatureClassPtrs fcps;

		public extract_features_visitor(FeatureClassPtrs fcps) {
			this.fcps = fcps;
		}

		void extract(sp_sentence_type s) {
			for (FeatureClass it : fcps) {
				it.extract_features(s);
			}
		}
	}

	public FeatureClassPtrs() {
		add(new NLogP());

		add(new RightBranch());

		add(new Heavy());

		add(new CoPar(false));

		add(new CoLenPar());

		add(new Word(1));
		add(new Word(2));

		add(new WProj());

		add(new Rule());
		add(new Rule(0, 1));
		add(new Rule(0, 0, true));
		add(new Rule(0, 0, false, true));
		add(new Rule(0, 0, false, false, annotation_level.lexical));
		add(new Rule(0, 0, false, false, annotation_level.none,
				annotation_level.lexical));
		add(new Rule(0, 0, false, false, annotation_level.lexical,
				annotation_level.lexical));

		add(new NGram(1, 1, false, true));
		add(new NGram(2, 1, true, true));
		add(new NGram(3, 1, true, true));
		add(new NGram(2, 1, false, false, annotation_level.lexical));
		add(new NGram(2, 1, false, false, annotation_level.none,
				annotation_level.lexical));

		add(new NGramTree(2, NGramTree.lexicalize_type.none, true));
		add(new NGramTree(2, NGramTree.lexicalize_type.all, true));
		add(new NGramTree(3, NGramTree.lexicalize_type.functional, true));

		add(new HeadTree(true, false, 0, HeadTree.head_type.syntactic));
		add(new HeadTree(true, false, 0, HeadTree.head_type.semantic));
		add(new HeadTree(true, true, 0, HeadTree.head_type.semantic));

		add(new Heads(2, false, false, Heads.head_type_type.syntactic));
		add(new Heads(2, true, true, Heads.head_type_type.syntactic));
		add(new Heads(2, true, true, Heads.head_type_type.semantic));
		add(new Heads(3, false, false));

		long maxwidth = 2, maxsumwidth = 2;

		for (int binflag = 0; binflag < 2; binflag++)
			for (long nleftprec = 0; nleftprec <= maxwidth; nleftprec++)
				for (long nleftsucc = 0; nleftsucc <= maxwidth; nleftsucc++)
					for (long nrightprec = 0; nrightprec <= maxwidth; nrightprec++)
						for (long nrightsucc = 0; nrightsucc <= maxwidth; nrightsucc++)
							if (nleftprec + nleftsucc + nrightprec + nrightsucc <= maxsumwidth)
								add(new Edges((binflag == 0) ? false : true,
										nleftprec, nleftsucc, nrightprec,
										nrightsucc));

		for (int binflag = 0; binflag < 2; binflag++)
			for (long nleftprec = 0; nleftprec <= maxwidth; nleftprec++)
				for (long nleftsucc = 0; nleftsucc <= maxwidth; nleftsucc++)
					for (long nrightprec = 0; nrightprec <= maxwidth; nrightprec++)
						for (long nrightsucc = 0; nrightsucc <= maxwidth; nrightsucc++)
							if (nleftprec + nleftsucc + nrightprec + nrightsucc <= maxsumwidth)
								add(new WordEdges(
										(binflag == 0) ? false : true,
										nleftprec, nleftsucc, nrightprec,
										nrightsucc));
	}

	public int extract_features(String parsefile, String goldfile,
			boolean downcase_flag) {
		extract_features_visitor efv = new extract_features_visitor(this);
		int nsentences = 0;

		try {
			BufferedReader parseReader = new BufferedReader(new FileReader(
					new File(parsefile)));
			BufferedReader goldReader = new BufferedReader(new FileReader(
					new File(goldfile)));

			String line = goldReader.readLine().trim();
			nsentences = Integer.parseInt(line);

			sp_sentence_type sentence = new sp_sentence_type();
//			int count = 0;
//			FeatureExtractor fe = FeatureExtractorFactory.createFeatureExtractor(false);
			
			for (int i = 0; i < nsentences; i++) {
				if (!sentence.read(parseReader, goldReader, downcase_flag)) {
					System.err.println("## Reading sentence tree " + i
							+ " failed.");
					return 0;
				}

//				if(i == 100)
//					break;
				
				efv.extract(sentence);
				
//				for(int k = 0; k < sentence.parses.size(); k++) {
//					fe.getfcp().extract_unpruned_feature_vector(sentence.parses.get(k));
//				}
//				
//				if(count++ % 50 == 0)
//					System.out.println("finished " + (count) + "/" + nsentences + ".");
			}
			
//			PrintWriter os = new PrintWriter(new File("./testout/chariniak.features"));
			
//			this.prune_and_renumber(Integer.MIN_VALUE, os);
//			fe.save_features("./testout/unpruned.features");
			
//			os.close();
			
			parseReader.close();
			goldReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return nsentences;
	}

	public long prune_and_renumber(int mincount, PrintWriter os) {
		long nextid = 0;
		for (FeatureClass it : this) {
			nextid = it.prune_and_renumber(mincount, nextid, os);
		}
		return nextid;
	}
	
	public void print_features(PrintWriter os) {
		for(FeatureClass it : this) {
			it.print_features(os);
		}
	}

	public long read_feature_ids(BufferedReader is) throws IOException {
		HashMap<String, FeatureClass> fcident_fcp = new HashMap<String, FeatureClass>();
		long id = -1, maxid = 0;
		String fcident = "";
		
		for (FeatureClass it : this) {
			fcident_fcp.put(it.identifier(), it);
		}
		
		String line = null;
		int lineno = 0;
		while((line = is.readLine()) != null) {
			lineno++;
			if(line.trim().equals(""))
				continue;
			Pattern pattern = Pattern.compile("([0-9]+)[\t ]([a-zA-Z0-9:]+)[\t ](.+)");
			java.util.regex.Matcher matcher = pattern.matcher(line);
//			String tokens[] = line.split("\t");
//			id = Long.parseLong(tokens[0].trim());
//			fcident = tokens[1].trim();
			
			String fs = null;//tokens[2].trim();
			if(matcher.find()) {
				id = Long.parseLong(matcher.group(1).trim());
				fcident = matcher.group(2).trim();
				fs = matcher.group(3).trim();
				
//				System.out.println(id + fcident + fs);
			} else {
				System.err.println("## Reading feature file failed, error at line " + lineno);
				System.exit(0);
			}
				
			
			FeatureClass it = fcident_fcp.get(fcident);
			
			if(it == null) {
				System.err.println("## Error: can't find feature identifier" + fcident + " in feature list.\n");
				System.exit(0);
			} else {
				it.read_feature(fs, id);
			}
			
			if(id > maxid)
				maxid = id;
		}
		
		return maxid;
	}
	
	public FeatureVector extract_unpruned_feature_vector(sp_parse_type sp) {
		FeatureVector fv = null;
		
		for(FeatureClass it : this)
			fv = it.feature_values(sp, fv, false);
		
		return fv;
	}
	
	public FeatureVector extract_pruned_feature_vector(sp_parse_type sp) {
		FeatureVector fv = null;
		
		for(FeatureClass it : this)
			fv = it.feature_values(sp, fv, true);
		
		return fv;
	}
	
	public Vector<HashMap<Long, Double>> extract_feature_vector(sp_sentence_type s) {
		Vector<HashMap<Long, Double>> p_i_v = new Vector<HashMap<Long, Double>>();
		for(int i = 0; i < s.nparses(); i++)
			p_i_v.add(new HashMap<Long, Double>());
		
		for (FeatureClass it : this) {
			it.feature_values(s, p_i_v);
		}
		
		return p_i_v;
	}
	
	public void load_features(String feature_file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(feature_file)));
			
//			long id_number = -1;
			
			this.read_feature_ids(reader);
			
//			System.err.println("# maxid = " + id_number);
			
			reader.close();
		} catch (IOException e) {
			System.err.println("load features from file " + feature_file + " wrong!");
			e.printStackTrace();
		}
	}
	
	static final String goldPath = "./resourcefiles/finalgoldparses.txt";
	static final String parsePath = "./resourcefiles/finalparses.txt";

	static boolean judge_hash_equal(HashMap<Long, Double> h1, HashMap<Long, Double> h2) {
		for (Map.Entry<Long, Double> entry : h1.entrySet()) {
			Long k = entry.getKey();
			Double v = entry.getValue();
			
			Double v2 = h2.get(k);
			if(v2 == null)
				return false;
			if(v.doubleValue() != v2.doubleValue())
				return false;
		}
		
		for (Map.Entry<Long, Double> entry : h2.entrySet()) {
			Long k = entry.getKey();
			Double v = entry.getValue();
			
			Double v2 = h1.get(k);
			if(v2 == null)
				return false;
			if(v.doubleValue() != v2.doubleValue())
				return false;
		}
		
		return true;
	}
	
	static void print_err_hash(HashMap<Long, Double> h, PrintWriter writer) {
//		ArrayList<Long> keys = new ArrayList<Long>();
		Long[] keys = new Long[h.size()];
		h.keySet().toArray(keys);
//		for(Long key : h.keySet()) {
//			keys.add(key);
//		}
		Arrays.sort(keys);
		
		for(int i = 0; i < keys.length; i++) {
			writer.print(keys[i] + "=" + h.get(keys[i]) + " ");
		}
		writer.println();
	}
	
	static void print_err_hash(HashMap<Long, Double> h) {
//		ArrayList<Long> keys = new ArrayList<Long>();
		Long[] keys = new Long[h.size()];
		h.keySet().toArray(keys);
//		for(Long key : h.keySet()) {
//			keys.add(key);
//		}
		Arrays.sort(keys);
		
		for(int i = 0; i < keys.length; i++) {
			System.out.print(keys[i] + "=" + h.get(keys[i]) + " ");
		}
		System.out.println();
	}
	/**
	 * The main entrance of 
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
//		try {
//			System.setErr(new PrintStream(new File(OutputToFile.filePath)));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}

		FeatureClassPtrs fcp = new FeatureClassPtrs();

		long startTime = System.currentTimeMillis();

		fcp.extract_features(parsePath, goldPath, true);
		PrintWriter os = new PrintWriter(new File(OutputToFile.filePath));
		long id_number = -1; 
		id_number = fcp.prune_and_renumber(5, os);
//
		int count = 0;
//		
//		try {
////			fcp.load_features("./testout/features");
////			fcp.load_features("./testout/short_test_features.txt");
//			fcp.load_features("./testout/chariniak.features");
//			
////			BufferedReader treader = new BufferedReader(new FileReader(new File("./testout/section23.nbest")));
////			BufferedReader treader = new BufferedReader(new FileReader(new File("./testout/short_section23.test.txt")));
//			BufferedReader treader = new BufferedReader(new FileReader(new File("./resourcefiles/finalparses.txt")));
//			PrintWriter cWriter = new PrintWriter(new File("./testout/chariniak.data"));
//			PrintWriter mWriter = new PrintWriter(new File("./testout/my.data"));
//			
//			SPData.sp_sentence_type s = new sp_sentence_type();
//			
//			FeatureExtractor fe = FeatureExtractorFactory.createFeatureExtractor(false);
//			
//			boolean downcase_flag = true;
////			
//			int sentence_count = 0;
//			while(s.read_ec_nbest(treader, downcase_flag)){
//				System.out.println("Processing sentence: " + (sentence_count++));
//				if(sentence_count == 101)
//					break;
//				Vector<HashMap<Long, Double>> fvs = fcp.extract_feature_vector(s);
//				for(int i = 0; i < s.nparses(); i++) {
//					++count;
//					if(count % 50 == 0)
//						System.out.println("Processing parse " + count);
//					
//					HashMap<Long, Double> fv = fvs.get(i);
//					
//					sp_parse_type sp = s.parses.get(i);
//					
//					FeatureVector id_vals = fe.getfcp().extract_unpruned_feature_vector(sp);
//					
//					HashMap<Long, Double> fv2 = new HashMap<Long, Double>();
//					
//					FeatureVector it = id_vals;
//					while(it != null) {
//						Long k = new Long((long)it.index);
//						Double v = new Double(it.value);
//						
//						fv2.put(k, v);
//						
//						it = it.next;
//					}
//					
//					boolean flag = judge_hash_equal(fv, fv2);
//					
//					if(!flag) {
////						System.out.println("The parse " + count + " are not equal!");
////						System.out.println("h1:");
//						print_err_hash(fv, cWriter);
////						System.out.println("h2:");
//						print_err_hash(fv2, mWriter);
//					}
//					
//					TreeSet<Long> fs = new TreeSet<Long>(fv.keySet());
//					
////					for (Long f : fs) {
////						if(f == 0)
////							System.err.print(f + "=" + fv.get(f) + "\t");
////						else 
////							System.err.print(f + "=" + Integer.parseInt((fv.get(f)).toString().split("\\.")[0]) + "\t");
////					}
////					System.err.println();
//				}
//			}
//			
//			fe.save_features("./testout/unpruned.features");
//			
//			treader.close();
//			mWriter.close();
//			cWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.flush();
		os.close();
		System.out.println("# maxid = " + id_number + ", usage "
				+ (((double) (endTime - startTime)) / 1000) + "s");
	}
}
