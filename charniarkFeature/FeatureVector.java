package nncon.charniarkFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FeatureVector implements Comparable {

	public int index;
	public double value;
	public FeatureVector next;

	public FeatureVector(int i, double v, FeatureVector n) {
		index = i;
		value = v;
		next = n;
	}

//	public FeatureVector add(String feat, double val, Alphabet dataAlphabet) {
//		int num = dataAlphabet.lookupIndex(feat);  
//		if (num >= 0)
//			return new FeatureVector(num, val, this);
//		return this;
//	}
//	public FeatureVector add(String feat, double val, Alphabet dataAlphabet,boolean addIfNotFound) {
//		int num = dataAlphabet.lookupIndex(feat,addIfNotFound);  //���� û�оͲ��� ���ض�Ӧkey��value
//		if (num >= 0)
//			return new FeatureVector(num, val, this);
//		return this;
//	}
	
//	/**
//	 * Add the feature vector query the MultiHashAlphabet
//	 * 
//	 * @param featureID
//	 * @param actionID
//	 * @param feat
//	 * @param val
//	 * @param dataAlphabet
//	 * @param addIfNotFound
//	 * @return
//	 */
//	public FeatureVector add(int featureID, int actionID, long feat, double val, 
//			MultiHashAlphabet dataAlphabet,boolean addIfNotFound) {
//		
//		int num = dataAlphabet.lookupIndex(featureID,actionID,feat,addIfNotFound);  
//		if (num >= 0)
//			return new FeatureVector(num, val, this);
//		return this;
//	}
	
//	/**
//	 * Add a list of feature vector in batch
//	 * -2 is the return none value of TIntIntHashMap when initialized
//	 * 
//	 * @param featureID
//	 * @param actions
//	 * @param feat
//	 * @param val
//	 * @param dataAlphabet
//	 * @return
//	 */
//	public FeatureVector batchAdd(int featureID, ArrayList<ConAction> actions, long feat, double val, 
//			MultiHashAlphabet dataAlphabet){
//		
//		TIntIntHashMap actionWeightIndexMap=dataAlphabet.lookupIndexBatch(featureID, feat);
//		int weightindex;
//		
//		for(ConAction action:actions){
//			weightindex=actionWeightIndexMap.get(action.code());
//			if(weightindex!=-2) 
//				add(weightindex,val);
//		}
//		return this;
//	}
	
	public void add(int i1, double v1) {

		FeatureVector new_node = new FeatureVector(this.index, this.value,
				this.next);

		this.index = i1;
		this.value = v1;
		this.next = new_node;

	}

	public static FeatureVector cat(FeatureVector fv1, FeatureVector fv2) {
		FeatureVector result = new FeatureVector(-1, -1.0, null);
		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			result = new FeatureVector(curr.index, curr.value, result);
		}
		for (FeatureVector curr = fv2; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			result = new FeatureVector(curr.index, curr.value, result);
		}
		return result;

	}

	// input: x->y->z->(-1)
	// output: z->y->x->(-1)
	public static FeatureVector inverse(FeatureVector fv) {
		FeatureVector result = new FeatureVector(-1, -1.0, null);
		for (FeatureVector curr = fv; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			result = new FeatureVector(curr.index, curr.value, result);
		}
		return result;
	}

	// fv1 - fv2
	//�������������ͬ��index���� ������ֻ�����ǰ���ֱ����Ϊ -curr.value
	public static FeatureVector getDistVector(FeatureVector fv1,
			FeatureVector fv2) {
		FeatureVector result = new FeatureVector(-1, -1.0, null);
		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			result = new FeatureVector(curr.index, curr.value, result);
		}
		for (FeatureVector curr = fv2; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			result = new FeatureVector(curr.index, -curr.value, result);
		}
		return result;
	}

	// fv1 - fv2 --> remove from the new vector every feature 'x' that is
	// positive in one and negative in the other, in oppose to having the
	// feature twice once with +1 value and then with -1 value.
	public static FeatureVector getDistVectorNoOverlap(FeatureVector fv1,
			FeatureVector fv2) {
		FeatureVector result = new FeatureVector(-1, -1.0, null);

		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();

		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			hm.put(curr.index, hm.get(curr.index) + curr.value);
		}
		for (FeatureVector curr = fv2; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			hm.put(curr.index, hm.get(curr.index) - curr.value);
		}

		Integer[] keys = (Integer[])(hm.keySet().toArray());

		for (int i = 0; i < keys.length; i++) {
			double val = hm.get(keys[i]);
			if ( val != 0.0 ){
				result = new FeatureVector(keys[i], val, result);
			}
		}

		return result;	
	}

	public static double dotProduct(FeatureVector fv1, FeatureVector fv2) {
		double result = 0.0;
		HashMap<Integer, Double> hm1 = new HashMap<Integer, Double>();
		HashMap<Integer, Double> hm2 = new HashMap<Integer, Double>();

		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			hm1.put(curr.index, hm1.get(curr.index) + curr.value);
		}
		for (FeatureVector curr = fv2; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			hm2.put(curr.index, hm2.get(curr.index) + curr.value);
		}

		Integer[] keys = (Integer[])(hm1.keySet().toArray());

		for (int i = 0; i < keys.length; i++) {
			double v1 = hm1.get(keys[i]);
			double v2 = hm2.get(keys[i]);
			result += v1 * v2;
		}

		return result;

	}

	public static double oneNorm(FeatureVector fv1) {
		double sum = 0.0;
		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			sum += curr.value;
		}
		return sum;
	}

	public static int size(FeatureVector fv1) {
		int sum = 0;
		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			sum++;
		}
		return sum;
	}

	public static double twoNorm(FeatureVector fv1) {
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		
		double sum = 0.0;
		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			hm.put(curr.index, hm.get(curr.index) + curr.value);
		}
		Integer[] keys = (Integer[])(hm.keySet().toArray());

		for (int i = 0; i < keys.length; i++)
			sum += Math.pow(hm.get(keys[i]), 2.0);

		return Math.sqrt(sum);
	}

	public static FeatureVector twoNormalize(FeatureVector fv1) {
		return normalize(fv1, twoNorm(fv1));
	}

	public static FeatureVector oneNormalize(FeatureVector fv1) {
		return normalize(fv1, oneNorm(fv1));
	}

	public static FeatureVector normalize(FeatureVector fv1, double norm) {
		FeatureVector result = new FeatureVector(-1, -1.0, null);
		for (FeatureVector curr = fv1; curr.next != null; curr = curr.next) {
			if (curr.index < 0)
				continue;
			result = new FeatureVector(curr.index, curr.value / norm, result);
		}
		return result;
	}

	public String toString() {
		if (next == null)
			return "" + index + ":" + value;
		return index + ":" + value + " " + next.toString();
	}

	//��this �������FeatureVector ��������index>0��Ԫ�� ���� ��С���� ���� ����� thisָ���FeatureVector ������ĵ�һ��
	public void sort() {
		ArrayList features = new ArrayList();

		for (FeatureVector curr = this; curr != null; curr = curr.next)
			if (curr.index >= 0)
				features.add(curr);//������ �������

		Object[] feats = features.toArray();    //���� ��һ�����˳�� ���Ԫ�� ����Ӧ���Ǹ���

		Arrays.sort(feats);  //���� ����

		FeatureVector fv = new FeatureVector(-1, -1.0, null);
		for (int i = feats.length - 1; i >= 0; i--) {
			FeatureVector tmp = (FeatureVector) feats[i];          //��feats�е�Ԫ�� ����嵽tmp��
			fv = new FeatureVector(tmp.index, tmp.value, fv);
		}

		this.index = fv.index;
		this.value = fv.value;
		this.next = fv.next;

	}

	public int compareTo(Object o) {
		FeatureVector fv = (FeatureVector) o;
		if (index < fv.index)
			return -1;
		if (index > fv.index)
			return 1;
		return 0;
	}

	public double dotProdoct(double[] weights) {
		double score = 0.0;
		for (FeatureVector curr = this; curr != null; curr = curr.next) {
			if (curr.index >= 0)
				score += weights[curr.index] * curr.value;
		}
		return score;
	}

}
