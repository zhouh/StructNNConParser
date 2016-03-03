package nncon;

import java.util.List;

/**
 * @author Christopher Manning
 */
class Example {

  private final List<Integer> feature;
  private final List<Integer> actLabel;  
  private final List<Integer> labelLabel;

  public Example(List<Integer> feature, List<Integer> actLabel, List<Integer> labelLabel) {
    this.feature = feature;
    this.actLabel = actLabel;
    this.labelLabel = labelLabel;
  }

  public List<Integer> getFeature() {
    return feature;
  }

  public List<Integer> getactTypeLabel() {
    return actLabel;
  }
  
  public List<Integer> getSyntaxTypeLabel() {
	    return labelLabel;
  }

}
