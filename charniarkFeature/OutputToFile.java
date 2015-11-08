package nncon.charniarkFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputToFile {
	static final String filePath = "./testout/final-features.out";
	public static void output(String content) {
		FileWriter outWriter;
		try {
			outWriter = new FileWriter(new File(filePath), true);
			outWriter.write(content + "\n");
			outWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
