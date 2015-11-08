package nncon.charniarkFeature;

import java.util.HashMap;

public class MyTest {
	public static void main(String[] args) {
		HashMap<Long, Double> h = new HashMap<Long, Double>();
		
		h.put(new Long(1), new Double(30));
		
		System.out.println(h.get(new Long(1)));
	}
}
