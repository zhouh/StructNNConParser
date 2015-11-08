package nncon.charniarkFeature;

public class NLogP extends FeatureClass {
	static public class NLogPFeature extends FeatureClass.Feature {
		Long feature;
		
		public NLogPFeature() {
			this(0);
		}
		
		public NLogPFeature(int feature) {
			this.feature = new Long(feature);
		}
		
		@Override
		public String toString(){
			return "" + feature;
		}
		
		@Override
		public int hashCode() {
			return feature.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			return ((NLogPFeature)obj).feature.equals(feature);
		}
	}
	
	public NLogP() {
		super();
		identifier_string = "NLogP";
	}
	
	@Override
	public void parse_featurecount(FeatureClass fc, SPData.sp_parse_type parse, FeatureClass.ParseValAccessor feat_count) {
		feat_count.setValueOf(new NLogPFeature(0), feat_count.getValueOf(new NLogPFeature(0)) - parse.logprob);
	}

	@Override
	public void read_feature(String fs, long id) {
		NLogPFeature feature = new NLogPFeature(Integer.parseInt(fs));
		
		feature_id.put(feature, id);
	}
}
