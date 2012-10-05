
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class build_tagger {

	
	public static void save(String datafile, Tagger t) {
		
		File f = new File(datafile);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
			oos.writeObject(t);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void testSentence(Tagger t, String line, Hashtable<String,Hashtable<String,Integer>> confusionMatrix) {
		String[] tokens = line.split("\\s+");
		String[] words = new String[tokens.length];
		String[] correctPOS = new String[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			String tokPOS = tokens[i];
			int seperator = tokPOS.lastIndexOf('/');
			words[i] = tokPOS.substring(0, seperator).toLowerCase();
			correctPOS[i] = tokPOS.substring(seperator + 1, tokPOS.length());
		}
		String[] predictedPOS = t.getTags(words);
		for(int i=0;i<predictedPOS.length;i++) countMatrix(confusionMatrix, correctPOS[i], predictedPOS[i]);
	}
	
	public static Hashtable<String,Hashtable<String,Integer>> confusionMatrix(Tagger t,BufferedReader reader) {
		Hashtable<String,Hashtable<String,Integer>> cMat = new Hashtable<String,Hashtable<String,Integer>>();
		String line;
		try {
			while ((line = reader.readLine()) != null)
				testSentence(t,line,cMat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cMat;
	}
	
	public static void countMatrix(Hashtable<String,Hashtable<String,Integer>> confusionMatrix, String truePOS, String predPOS) {
		Hashtable<String,Integer> tbl = confusionMatrix.get(truePOS);

		if (tbl == null) tbl = new Hashtable<String,Integer>();
		tbl.put(predPOS, (tbl.containsKey(predPOS)?tbl.get(predPOS):0) + 1);
		confusionMatrix.put(truePOS, tbl);
	}
	
	
	public static void printMatrix(Hashtable<String,Hashtable<String,Integer>> confusionMatrix) {
		List<String> posList = new ArrayList<String>(confusionMatrix.keySet());
		System.out.printf("%5s\t","");
		for (String pos:posList) System.out.printf("%5s ", pos);
		System.out.println();
		for (String correctPOS:posList) {
			System.out.printf("%5s\t", correctPOS);
			Hashtable<String,Integer> predTable = confusionMatrix.get(correctPOS);
			for(String predictedPOS:posList) {
				System.out.printf("%5d ", predTable.containsKey(predictedPOS)?predTable.get(predictedPOS):0);
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		int c = 0;
		String sentsTrain = args[c++];
		String sentsTest  = args[c++];
		String modelFile  = args[c++];
		
		File fTrain = new File(sentsTrain);
		File fTest  = new File(sentsTest);
		//File fTest  = new File(sentsTest);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fTrain));
			System.out.printf("Learning tags using data from %s...\n",sentsTrain);
			Tagger t = new Tagger(reader);
			System.out.println("Saving model...");
			save(modelFile,t);
			reader.close();
			reader = new BufferedReader(new FileReader(fTest));
			System.out.printf("Testing model using data from %s...\n",sentsTest);
			printMatrix(confusionMatrix(t,reader));
			
			reader.close();
			
			//System.out.println(t.posTransitions);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
