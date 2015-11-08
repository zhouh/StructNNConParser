package nncon.charniarkFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import nncon.charniarkFeature.SPData.sp_parse_type;

public class FeatureExtractorFactory {
	static class PrunedFeatureExtractor implements FeatureExtractor {
		FeatureClassPtrs fcp;
		boolean downcase_flag;
		
		public PrunedFeatureExtractor() {
			this(true);
		}
		
		public PrunedFeatureExtractor(boolean downcase_flag) {
			fcp = new FeatureClassPtrs();
			this.downcase_flag = downcase_flag;
		}

		@Override
		public FeatureVector extract_featureVector(String parse, double logp) {
			sp_parse_type sp = new sp_parse_type();
			
			sp.read_ec_nbest(logp, parse, downcase_flag);
			
			FeatureVector fv = fcp.extract_pruned_feature_vector(sp);
			
			return fv;
		}

		@Override
		public void save_features(String path) {
			try {
				PrintWriter os = new PrintWriter(new File(path));
				
				fcp.print_features(os);
				
				os.close();
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
		}

		@Override
		public void load_features(String path) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
				
				long id_number = Long.MIN_VALUE;
				
				id_number = fcp.read_feature_ids(reader);
				
				System.err.println("# maxid = " + id_number);
				
				reader.close();
			} catch (IOException e) {
				System.err.println("load features from file " + path + " wrong!");
				e.printStackTrace();
			}
		}

		@Override
		public FeatureClassPtrs getfcp() {
			return fcp;
		}
	}

	static class UnprunedFeatureExtractor implements FeatureExtractor {
		FeatureClassPtrs fcp;
		boolean downcase_flag;
		
		public UnprunedFeatureExtractor() {
			this(true);
		}
		
		public UnprunedFeatureExtractor(boolean downcase_flag) {
			fcp = new FeatureClassPtrs();
			this.downcase_flag = downcase_flag;
		}

		@Override
		public FeatureVector extract_featureVector(String parse, double logp) {
			sp_parse_type sp = new sp_parse_type();
			
			sp.read_ec_nbest(logp, parse, downcase_flag);
			
			FeatureVector fv = fcp.extract_unpruned_feature_vector(sp);
			
			return fv;
		}

		@Override
		public void save_features(String path) {
			try {
				PrintWriter os = new PrintWriter(new File(path));
				
				fcp.print_features(os);
				
				os.close();
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
		}

		@Override
		public void load_features(String path) {
			System.err.println("For dynamicly insert feature mode, there is no load_features.");
			System.exit(0);
		}

		@Override
		public FeatureClassPtrs getfcp() {
			return fcp;
		}
	}
	
	static public FeatureExtractor createFeatureExtractor(boolean isPruned) {
		if(isPruned)
			return createFeatureExtractor(true, true);
		else 
			return createFeatureExtractor(false, true);
	}
	
	static public FeatureExtractor createFeatureExtractor(boolean isPruned, boolean downcase_flag) {
		if(isPruned) {
			return new PrunedFeatureExtractor(downcase_flag);
		} else {
			return new UnprunedFeatureExtractor(downcase_flag);
		}
	}
	
	static final String goldPath = "./resourcefiles/goldparses.txt";
	static final String parsePath = "./resourcefiles/parses.txt";
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		FeatureExtractor fe = FeatureExtractorFactory.createFeatureExtractor(true);
		
		fe.load_features("G:/自然语言处理/最近工作/reranking/rerank codes/Brown/nlparser/reranking-parser/second-stage/models/ec50spfinal/features");
//		fe.load_features("./testout/short_test_features.txt");
		
		fe.save_features("./testout/features.test");
//		fe.save_features("./testout/short_test_features.out");
		
		long endTime = System.currentTimeMillis();
		
		System.err.println("# Usage: "+ (((double) (endTime - startTime)) / 1000) + "s");
	}

}
