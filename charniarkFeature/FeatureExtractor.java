package nncon.charniarkFeature;

public interface FeatureExtractor {
	public FeatureVector extract_featureVector(String parse, double logp);
	public void save_features(String path);
	public void load_features(String path);
	public FeatureClassPtrs getfcp();
}
