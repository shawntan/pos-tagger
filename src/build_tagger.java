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

	public static void testSentence(Tagger t, String line,
			Hashtable<String, Hashtable<String, Integer>> confusionMatrix) {
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
		for (int i = 0; i < predictedPOS.length; i++)
			countMatrix(confusionMatrix, correctPOS[i], predictedPOS[i]);
	}

	public static Hashtable<String, Hashtable<String, Integer>> confusionMatrix(
			Tagger t, BufferedReader reader) {
		Hashtable<String, Hashtable<String, Integer>> cMat = new Hashtable<String, Hashtable<String, Integer>>();
		String line;
		try {
			while ((line = reader.readLine()) != null)
				testSentence(t, line, cMat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cMat;
	}

	public static void countMatrix(
			Hashtable<String, Hashtable<String, Integer>> confusionMatrix,
			String truePOS, String predPOS) {
		Hashtable<String, Integer> tbl = confusionMatrix.get(truePOS);

		if (tbl == null)
			tbl = new Hashtable<String, Integer>();
		tbl.put(predPOS, (tbl.containsKey(predPOS) ? tbl.get(predPOS) : 0) + 1);
		confusionMatrix.put(truePOS, tbl);
	}

	public static void printMatrix(Hashtable<String, Hashtable<String, Integer>> confusionMatrix) {
		List<String> posList = new ArrayList<String>(confusionMatrix.keySet());
		System.out.printf("%5s\t", "");
		for (String pos : posList)
			System.out.printf("%5s ", pos);
		System.out.println();

		int col=0,row=0;
		int[] tpCounts 		= new int[posList.size()];
		int[] tpfnCounts	= new int[posList.size()];
		int[] tpfpCounts	= new int[posList.size()];

		for (String correctPOS : posList) {
			System.out.printf("%5s\t", correctPOS);
			Hashtable<String, Integer> predTable = confusionMatrix.get(correctPOS);
			int fntpCounts = 0; 
			col=0;
			for (String predictedPOS : posList) {
				int val = 	predTable.containsKey(predictedPOS) ? predTable.get(predictedPOS) : 0;
				System.out.printf("%5d ",val);

				if (col==row) tpCounts[col] = val;
				tpfnCounts[row] += val;
				tpfpCounts[col] += val;

				col++;
			}
			System.out.println("\n");
			row++;
		}
		System.out.printf("%5s\t%5s\t%5s\t%s\n", "POS","Rec.","Prec.","F1");
		int i=0,count=0;
		double precSum=0,recSum=0,f1Sum=0;
		for (String pos : posList) {
			double precision	= ((double)tpCounts[i])/tpfpCounts[i];
			double recall		= ((double)tpCounts[i])/tpfnCounts[i];
			double f1measure	= 2*precision*recall/(recall + precision);
			
			if(!(Double.isNaN(precision) || Double.isNaN(recall))) {
				precSum += precision;
				recSum	+= recall;
				f1Sum	+= f1measure;
				count ++; 
			}

			System.out.printf("%5s\t%5.4f\t%5.4f\t%5.4f\n",pos,precision,recall,f1measure);
			i++;
		}
		System.out.println("\n");
		System.out.printf("%5s\t%5.4f\t%5.4f\t%5.4f\n","",precSum/count,recSum/count,f1Sum/count);
		
	}
	

	private static Tagger.Smoother wbBuilder(Tagger t) {
		Tagger.Smoother wb = t.new Smoother() {
			public double gamma(String ctx) {
				return 1;
			}
			public double alpha(String ctx, String cur) {
				int ctxCount	= countCtx.get(ctx);
				int ctxCurCount	= countCtxCur.get(ctx).get(cur);
				return ((double)ctxCurCount)/(ctxCount + countCtxCur.get(ctx).size());
			}
			public double pSmooth(String ctx, String w) {
				int ctxCount = countCtx.get(ctx);
				int Z = vocab.size() - countCtxCur.get(ctx).size();
				int T = countCtxCur.get(ctx).size();
				double val = Math.log(T) - ( Math.log(Z) +  Math.log(ctxCount + T));
				double unlogged = Math.exp(val);
				return unlogged;
			}
		};
		return wb;
	}
	private static Tagger.Smoother knBuilder(Tagger t) {
		Tagger.Smoother kneserNey = t.new Smoother() {
			double D = 0.75;
			int sumUnique = -1;

			private int countUnique() {
				if (sumUnique == -1) {
					for (Hashtable<String, Integer> curs : countCurCtx.values()) {
						sumUnique += curs.size();
					}
				}
				return sumUnique;
			}

			public double alpha(String ctx, String cur) {
				return ((double) countCtxCur.get(ctx).get(cur) - D)
						/ (countCtx.get(ctx) + vocab.size());
			}

			public double gamma(String ctx) {
				int Cctx = countCtx.get(ctx);
				int Cseen = countCtxCur.get(ctx).size();
				return ((double) D * Cseen / Cctx);
			}

			public double pSmooth(String ctx, String cur) {
				//System.out.println(countCurCtx);
				try {
					return ((double) countCurCtx.get(cur).size()/countUnique());
				}
				catch (Exception e) {
					System.out.println(cur + " " + countCurCtx.get(cur));
					return 0;
				}
				
				
			}
		};
		return kneserNey;
	}
	public static void main(String[] args) {
		int c = 0;
		String sentsTrain = args[c++];
		String sentsTest = args[c++];
		String modelFile = args[c++];

		File fTrain = new File(sentsTrain);
		File fTest = new File(sentsTest);
		// File fTest = new File(sentsTest);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fTrain));
			System.out.printf("Learning tags using data from %s...\n",
					sentsTrain);
			Tagger t = new Tagger(reader);
			System.out.println("Saving model...");
			save(modelFile, t);

			//t.setSmootherPosPos(wbBuilder(t));
			//t.setSmootherPosWord(wbBuilder(t));
			reader.close();
			reader = new BufferedReader(new FileReader(fTest));
			System.out.printf(
					"Testing model using data from %s...\n",
					sentsTest);
			printMatrix(confusionMatrix(t, reader));
			reader.close();
			// System.out.println(t.posTransitions);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
