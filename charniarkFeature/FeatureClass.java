package nncon.charniarkFeature;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import nncon.charniarkFeature.SPData.sp_parse_type;
import nncon.charniarkFeature.SPData.sp_sentence_type;

public abstract class FeatureClass {
	public String identifier_string;

	HashMap<Feature, Long> feature_id;

	public FeatureClass() {
		identifier_string = "";
		feature_id = new HashMap<Feature, Long>();
	}

	String identifier() {
		return identifier_string;
	}

	HashMap<Feature, Long> getFeature_Id() {
		return feature_id;
	}

	FeatureParseValInterface getFeatureParseVal() {
		FeatureParseVal fpv = new FeatureParseVal();
		return fpv;
	}

	IdParseValInterface getIdParseVal() {
		IdParseVal ipv = new IdParseVal(this);

		return ipv;
	}

	static abstract class Feature {
		@Override
		abstract public int hashCode();

		@Override
		abstract public boolean equals(Object obj);
	}

	static public interface ParseValAccessor {
		public void setParse(long parse);

		public double getValueOf(Feature feat);

		public void setValueOf(Feature feat, double value);
	}

	static public interface FeatureParseValInterface extends ParseValAccessor {
		public HashMap<Feature, HashMap<Long, Double>> getF_P_V();
	}

	static public interface IdParseValInterface extends ParseValAccessor {
		public HashMap<Long, HashMap<Long, Double>> getF_P_V();
	}

	static class FeatureParseVal implements
			FeatureClass.FeatureParseValInterface {
		Long parse; // parse which we are currently collecting stats from
		HashMap<Feature, HashMap<Long, Double>> f_p_v;

		public FeatureParseVal() {
			parse = new Long(0);
			f_p_v = new HashMap<Feature, HashMap<Long, Double>>();
		}

		public void setParse(long parse) {
			this.parse = new Long(parse);
		}

		@Override
		public double getValueOf(Feature feat) {
			HashMap<Long, Double> c_v = f_p_v.get(feat);
			if (c_v == null) {
				HashMap<Long, Double> new_c_v = new HashMap<Long, Double>();
				new_c_v.put(parse, (double) 0);
				f_p_v.put(feat, new_c_v);
				return 0;
			} else {
				Double res = c_v.get(parse);
				if (res == null) {
					c_v.put(parse, (double) 0);
					return 0;
				} else
					return res;
			}
		}

		@Override
		public void setValueOf(Feature feat, double value) {
			HashMap<Long, Double> c_v = f_p_v.get(feat);
//			if (c_v == null) {
//				HashMap<Long, Double> new_c_v = new HashMap<Long, Double>();
//				new_c_v.put(parse, value);
//				f_p_v.put(feat, new_c_v);
//			} else {
				c_v.put(parse, value);
//			}
		}

		@Override
		public HashMap<Feature, HashMap<Long, Double>> getF_P_V() {
			return f_p_v;
		}
	}

	static class IdParseVal implements FeatureClass.IdParseValInterface {
		FeatureClass fc;
		Long parse;
		HashMap<Long, HashMap<Long, Double>> f_p_v;
		double ignored;

		public IdParseVal(FeatureClass fc) {
			this.fc = fc;
			f_p_v = new HashMap<Long, HashMap<Long, Double>>();
			ignored = (double) 0;
			parse = new Long(0);
		}

		@Override
		public double getValueOf(Feature feat) {
			HashMap<Feature, Long> feature_id = fc.getFeature_Id();

			Long fid = feature_id.get(feat);
			if (fid == null)
				return ignored;
			else {
				HashMap<Long, Double> p_v = f_p_v.get(fid);
				if (p_v == null) {
					HashMap<Long, Double> np_v = new HashMap<Long, Double>();
					np_v.put(parse, (double) 0);
					f_p_v.put(fid, np_v);

					return 0;
				}

				Double v = p_v.get(parse);
				if (v == null) {
					p_v.put(parse, new Double(0));

					return 0;
				} else
					return v;
			}
		}

		@Override
		public void setValueOf(Feature feat, double value) {
			HashMap<Feature, Long> feature_id = fc.getFeature_Id();

			Long fid = feature_id.get(feat);

			if (fid == null)
				return;

			HashMap<Long, Double> p_v = f_p_v.get(fid);
			if (p_v == null) {
				HashMap<Long, Double> np_v = new HashMap<Long, Double>();
				np_v.put(parse, value);
				f_p_v.put(fid, np_v);
			} else
				p_v.put(parse, value);
		}

		@Override
		public void setParse(long parse) {
			this.parse = new Long(parse);
		}

		@Override
		public HashMap<Long, HashMap<Long, Double>> getF_P_V() {
			return f_p_v;
		}
	}

	static class UnprunedIdVal implements FeatureClass.IdParseValInterface {
		FeatureClass fc;
		double ignored;
		HashMap<Long, HashMap<Long, Double>> f_p_v;
		Long parse;
		static long global_id = 0;
		
		public UnprunedIdVal(FeatureClass fc) {
			this.fc = fc;
			ignored = (double)0;
			f_p_v = new HashMap<Long, HashMap<Long, Double>>();
			parse = (long)0;
		}
		
		@Override
		public void setParse(long parse) {
			return ;
		}

		@Override
		public double getValueOf(Feature feat) {
			HashMap<Feature, Long> feature_id = fc.getFeature_Id();

			Long fid = feature_id.get(feat);
			if (fid == null){
				feature_id.put(feat, new Long(global_id));
				global_id++;
				return (double)0;
			}
			else {
				HashMap<Long, Double> p_v = f_p_v.get(fid);
				if (p_v == null) {
					HashMap<Long, Double> np_v = new HashMap<Long, Double>();
					np_v.put(parse, (double) 0);
					f_p_v.put(fid, np_v);

					return 0;
				}

				Double v = p_v.get(parse);
				if (v == null) {
					p_v.put(parse, new Double(0));

					return 0;
				} else
					return v;
			}
		}

		@Override
		public void setValueOf(Feature feat, double value) {
			HashMap<Feature, Long> feature_id = fc.getFeature_Id();

			Long fid = feature_id.get(feat);

			assert(fid != null);

			HashMap<Long, Double> p_v = f_p_v.get(fid);
			if (p_v == null) {
				HashMap<Long, Double> np_v = new HashMap<Long, Double>();
				np_v.put(parse, value);
				f_p_v.put(fid, np_v);
			} else
				p_v.put(parse, value);
		}

		@Override
		public HashMap<Long, HashMap<Long, Double>> getF_P_V() {
			return f_p_v;
		}
		
	}
	static class IdVal implements FeatureClass.IdParseValInterface {
		FeatureClass fc;
		double ignored;
		HashMap<Long, HashMap<Long, Double>> f_p_v;
		Long parse;
		
		public IdVal(FeatureClass fc) {
			this.fc = fc;
			ignored = (double)0;
			f_p_v = new HashMap<Long, HashMap<Long, Double>>();
			parse = (long)0;
		}
		
		@Override
		public void setParse(long parse) {
			return ;
		}

		@Override
		public double getValueOf(Feature feat) {
			HashMap<Feature, Long> feature_id = fc.getFeature_Id();

			Long fid = feature_id.get(feat);
			if (fid == null)
				return ignored;
			else {
				HashMap<Long, Double> p_v = f_p_v.get(fid);
				if (p_v == null) {
					HashMap<Long, Double> np_v = new HashMap<Long, Double>();
					np_v.put(parse, (double) 0);
					f_p_v.put(fid, np_v);

					return 0;
				}

				Double v = p_v.get(parse);
				if (v == null) {
					p_v.put(parse, new Double(0));

					return 0;
				} else
					return v;
			}
		}

		@Override
		public void setValueOf(Feature feat, double value) {
			HashMap<Feature, Long> feature_id = fc.getFeature_Id();

			Long fid = feature_id.get(feat);

			if (fid == null)
				return;

			HashMap<Long, Double> p_v = f_p_v.get(fid);
			if (p_v == null) {
				HashMap<Long, Double> np_v = new HashMap<Long, Double>();
				np_v.put(parse, value);
				f_p_v.put(fid, np_v);
			} else
				p_v.put(parse, value);
		}

		@Override
		public HashMap<Long, HashMap<Long, Double>> getF_P_V() {
			return f_p_v;
		}
		
	}
	static class Pair implements Comparable<Pair> {
		Long id;
		Feature feature;

		public Pair(Long id, Feature feature) {
			this.id = id;
			this.feature = feature;
		}

		public Long first() {
			return id;
		}

		public Feature second() {
			return feature;
		}

		@Override
		public int compareTo(Pair o) {
			if (id < o.id)
				return -1;
			else if (id > o.id)
				return 1;

			return 0;
		}
	};

	void extract_features(sp_sentence_type s) {
		extract_features_helper(this, s);
	}

	long prune_and_renumber(long mincount, long nextid, PrintWriter os) {

		return prune_and_renumber_helper(this, mincount, nextid, os);
	}

	FeatureVector feature_values(sp_parse_type sp, FeatureVector fv) {
		return feature_values_helper(this, sp, fv);
	}
	
	FeatureVector feature_values(sp_parse_type sp, FeatureVector fv, boolean is_pruned) {
		if(!is_pruned) {
			return feature_values_helper(this, sp, fv, is_pruned);
		} else {
			return feature_values_helper(this, sp, fv);
		}
	}
	
	void feature_values(sp_sentence_type s, Vector<HashMap<Long, Double>> piv) {
		feature_values_helper(this, s, piv);
	}

	void print_feature_ids(FeatureClass fc, PrintWriter os) {
		print_feature_ids_helper(fc, os);
	}

	abstract public void parse_featurecount(FeatureClass fc,
			SPData.sp_parse_type parse, FeatureClass.ParseValAccessor feat_count);

	abstract public void read_feature(String fs, long id);

	static final Symbol E = new Symbol("_");
	static final Symbol CHILD = new Symbol("*CHILD*");
	static final Symbol ADJ = new Symbol("*ADJ*");
	static final Symbol CONJ = new Symbol("*CONJ*");
	static final Symbol HEAD = new Symbol("*HEAD*");
	static final Symbol LASTADJ = new Symbol("*LASTADJ*");
	static final Symbol LASTCONJ = new Symbol("*LASTCONJ*");
	static final Symbol NONROOT = new Symbol("*NONROOT*");
	static final Symbol POSTHEAD = new Symbol("*POSTHEAD*");
	static final Symbol PREHEAD = new Symbol("*PREHEAD*");

	static final Symbol DT = new Symbol("DT");
	static final Symbol NP = new Symbol("NP");
	static final Symbol ROOT = new Symbol("ROOT");
	static final Symbol S = new Symbol("S");
	static final Symbol SBAR = new Symbol("SBAR");
	static final Symbol SINV = new Symbol("SINV");
	static final Symbol VB = new Symbol("VB");
	static final Symbol VP = new Symbol("VP");

	static final Symbol ZERO = new Symbol("0");
	static final Symbol ONE = new Symbol("1");
	static final Symbol TWO = new Symbol("2");
	static final Symbol FOUR = new Symbol("4");
	static final Symbol FIVE = new Symbol("5");

	void extract_features_helper(FeatureClass fc, SPData.sp_sentence_type s) {
		if (s.nparses() <= 1)
			return;

		FeatureParseValInterface fpv = fc.getFeatureParseVal();
		for (int i = 0; i < s.nparses(); i++) {
			fpv.setParse(i);
			fc.parse_featurecount(fc, s.parses.get(i), fpv);
		}

		/* DEBUG */
		// for(Map.Entry<Feature, HashMap<Long, Double>> entry:
		// fpv.getF_P_V().entrySet()) {
		// HashMap<Long, Double> p_v = entry.getValue();
		//
		// Double v = p_v.get((long)0);

		// for (Map.Entry<Long, Double> in_entry : p_v.entrySet()) {
		// System.err.println('\t' + nfc.identifier_string + '\t' +
		// in_entry.getKey() + '\t' + in_entry.getValue());
		// }

		// if(v != null) {
		// System.err.println('\t' + fc.identifier() + '\t' + entry.getKey() +
		// '\t' + v);
		// }
		// }
		for (java.util.Map.Entry<Feature, HashMap<Long, Double>> entry : fpv
				.getF_P_V().entrySet()) {
			HashMap<Long, Double> p_v = entry.getValue();
			boolean pseudoconstant = true;

			if (p_v.size() != s.nparses()) // does feature occur on every parse
				pseudoconstant = false;
			else { // does feature values are the same in all parses
				assert (!p_v.isEmpty());
				Iterator<java.util.Map.Entry<Long, Double>> it1 = p_v
						.entrySet().iterator();
				double first_value = it1.next().getValue();

				while (it1.hasNext()) {
					double value = it1.next().getValue();

					if (value != first_value) {
						pseudoconstant = false;
						break;
					}
				}
			}

			boolean collect_correct = true;
//			pseudoconstant = false;
			if (pseudoconstant == false) {
				/* MARK */
				if ((collect_correct && p_v.containsKey((long) 0))
						|| (collect_correct && (!p_v.containsKey((long) 0) || p_v
								.size() > 1))) {
					Long v = fc.getFeature_Id().get(entry.getKey());
					if (v == null) {
						fc.getFeature_Id().put(entry.getKey(), (long) 1);
					} else {
						fc.getFeature_Id().put(entry.getKey(), v + 1);
					}
				}
			}
		}
	}

	void print_features(PrintWriter os) {
		print_feature_ids_helper(os);
	}
	
	long prune_and_renumber_helper(FeatureClass fc, long mincount, long nextid,
			PrintWriter os) {
		Vector<Feature> fs = new Vector<NLogP.Feature>();

		for (java.util.Map.Entry<Feature, Long> entry : fc.getFeature_Id()
				.entrySet()) {
			Feature f = entry.getKey();
			Long v = entry.getValue();

			// os.append("it->second " + v + '\n');
			if (v >= mincount)
				fs.add(f);
		}

		fc.getFeature_Id().clear();

		for (Feature it : fs) {
			fc.getFeature_Id().put(it, nextid);
			nextid++;
		}

		print_feature_ids_helper(fc, os);

		return nextid;
	}

	void print_feature_ids_helper(PrintWriter os) {
		Vector<Pair> idfps = new Vector<Pair>();

		idfps.setSize(this.getFeature_Id().size());

		int index = 0;
		for (Entry<Feature, Long> entry : this.getFeature_Id().entrySet()) {
			idfps.set(index, new Pair(entry.getValue(), entry.getKey()));
			index++;
		}

		java.util.Collections.sort(idfps);

		for (Pair pair : idfps) {
			os.print(pair.first() + "\t");
			os.print(this.identifier() + "\t");
			os.print(pair.second().toString() + "\n");
		}
	}
	
	void print_feature_ids_helper(FeatureClass fc, PrintWriter os) {
		Vector<Pair> idfps = new Vector<Pair>();

		idfps.setSize(fc.getFeature_Id().size());

		int index = 0;
		for (Entry<Feature, Long> entry : fc.getFeature_Id().entrySet()) {
			idfps.set(index, new Pair(entry.getValue(), entry.getKey()));
			index++;
		}

		java.util.Collections.sort(idfps);

		for (Pair pair : idfps) {
			os.print(pair.first() + "\t");
			os.print(fc.identifier() + "\t");
			os.print(pair.second().toString() + "\n");
		}
	}

	public FeatureVector parse_unpruned_fidvals(FeatureClass fc, sp_parse_type p, FeatureVector fv) {
		IdParseValInterface fpv = new UnprunedIdVal(fc);
		
		fc.parse_featurecount(fc, p, fpv);
		
		for(Map.Entry<Long, HashMap<Long, Double>> entry : fpv.getF_P_V().entrySet()) {
			Long feat = entry.getKey();
			int id = (int)(feat.longValue());
			
			HashMap<Long, Double> parse_val = entry.getValue();
			double val = parse_val.get((long)0).doubleValue();
			
			if(fv == null) {
				fv = new FeatureVector(id, val, null);
			} else {
				fv.add(id, val);
			}
		}
		
		return fv;
	}
	
	public FeatureVector parse_fidvals(FeatureClass fc, sp_parse_type p, FeatureVector fv) {
		IdParseValInterface fpv = new IdVal(fc);
		
		fc.parse_featurecount(fc, p, fpv);
		
		for(Map.Entry<Long, HashMap<Long, Double>> entry : fpv.getF_P_V().entrySet()) {
			Long feat = entry.getKey();
			int id = (int)(feat.longValue());
			
			HashMap<Long, Double> parse_val = entry.getValue();
			double val = parse_val.get((long)0).doubleValue();
			
			if(fv == null) {
				fv = new FeatureVector(id, val, null);
			} else {
				fv.add(id, val);
			}
		}
		
		return fv;
	}
	
	void sentence_parsefidvals(FeatureClass fc, sp_sentence_type s,
			FeatureClass.IdParseValInterface fid_parse_val,
			Vector<HashMap<Long, Double>> parse_fid_val) {
		assert (parse_fid_val.size() == s.nparses());

		IdParseValInterface fpv = fid_parse_val;

		fpv.getF_P_V().clear();

		for (int i = 0; i < s.nparses(); i++) {
			fpv.setParse((long) i);
			fc.parse_featurecount(fc, s.parses.get(i), fpv);
		}

		/* ??? */
		for (Map.Entry<Long, HashMap<Long, Double>> entry : fpv.getF_P_V()
				.entrySet()) {
			Long feat = entry.getKey();
			HashMap<Long, Double> parse_val = entry.getValue();

			boolean absolute_counts = true;

			if (absolute_counts) {
				for (int i = 0; i < s.nparses(); i++) {
					Double val = parse_val.get(new Long(i));
//
//					if (val == null)
//						val = new Double(0.0);
					
					if(val != null && val != 0.0)
						parse_fid_val.get(i).put(feat, val);
				}
			} else {
				/* TODO MARK */
				HashMap<Double, Long> val_gain = new HashMap<Double, Long>();

				for (long i = 0; i < s.nparses(); i++) {
					Double val = parse_val.get(new Long(i));

					if (val == null)
						val = new Double(0.0);

					Long c_v = val_gain.get(val);
					Long c_v_1 = val_gain.get(val - 1);
					if (c_v == null) {
						val_gain.put(val, (long) 2);
					} else {
						val_gain.put(val, c_v + 2);
					}
					if (c_v_1 == null) {
						val_gain.put(val - 1, (long) 1);
					} else {
						val_gain.put(val - 1, c_v_1 + 1);
					}
				}

				double highest_gain_val = -Double.MAX_VALUE;
				long highest_gain = Long.MIN_VALUE;
				for (Map.Entry<Double, Long> dlEntry : val_gain.entrySet()) {
					if (dlEntry.getValue() > highest_gain)
						highest_gain_val = dlEntry.getKey();
				}

				for (int i = 0; i < s.nparses(); i++) {
					Double old_val = parse_val.get((long) i);
					if (old_val == null)
						old_val = new Double(0.0);

					double val = old_val - highest_gain_val;

					if (val != 0) {
						parse_fid_val.get(i).put(feat, val);
					}
				}
			}
		}
	}

	FeatureVector feature_values_helper(FeatureClass fc, sp_parse_type sp, FeatureVector fv, boolean is_pruned) {
		if(is_pruned)
			return parse_fidvals(fc, sp, fv);
		else 
			return parse_unpruned_fidvals(fc, sp, fv);
	}
	
	FeatureVector feature_values_helper(FeatureClass fc, sp_parse_type sp, FeatureVector fv) {
		return parse_fidvals(fc, sp, fv);
	}
	
	void feature_values_helper(FeatureClass fc, sp_sentence_type s,
			Vector<HashMap<Long, Double>> p_i_v) {
		assert (p_i_v.size() == s.nparses());

		IdParseValInterface i_p_v =  new IdParseVal(fc);//.getIdParseVal();

		sentence_parsefidvals(fc, s, i_p_v, p_i_v);
	}

	static public Symbol endmarker() {
		return E;
	}

	static public Symbol childmarker() {
		return CHILD;
	}

	static public Symbol adjunctmarker() {
		return ADJ;
	}

	static public Symbol conjunctmarker() {
		return CONJ;
	}

	static public Symbol headmarker() {
		return HEAD;
	}

	static public Symbol lastadjunctmarker() {
		return LASTADJ;
	}

	static public Symbol lastconjunctmarker() {
		return LASTCONJ;
	}

	static public Symbol nonrootmarker() {
		return NONROOT;
	}

	static public Symbol postheadmarker() {
		return POSTHEAD;
	}

	static public Symbol preheadmarker() {
		return PREHEAD;
	}

	static public Symbol DT() {
		return DT;
	}

	static public Symbol NP() {
		return NP;
	}

	static public Symbol ROOT() {
		return ROOT;
	}

	static public Symbol S() {
		return S;
	}

	static public Symbol SBAR() {
		return SBAR;
	}

	static public Symbol SINV() {
		return SINV;
	}

	static public Symbol VB() {
		return VB;
	}

	static public Symbol VP() {
		return VP;
	}

	static public int quantize(int v) {
		assert (v >= 0);
		switch (v) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
		case 4:
			return 4;
		default:
			return 5;
		}
	}

	public Symbol symbol_quantize(int v) {
		assert (v >= 0);
		switch (v) {
		case 0:
			return ZERO;
		case 1:
			return ONE;
		case 2:
			return TWO;
		case 3:
		case 4:
			return FOUR;
		default:
			return FIVE;
		}
	}

	static public boolean is_bounding_node(SPTreeNode node) {
		return (node != null
				&& (node.label.cat.equalTo(NP()) || node.label.cat
						.equalTo(ROOT())) || node.label.cat.equalTo(S()) || node.label.cat
					.equalTo(SBAR())) ? true : false;
	}
}
