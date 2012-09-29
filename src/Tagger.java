import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class Tagger {
	
	private Hashtable<String, Hashtable<String,Integer>> posTransitions;
	private Hashtable<String, Hashtable<String,Integer>> posTokCount;
	private Hashtable<String, Integer> posCount;
	private Hashtable<String, Integer> prevPosCount;
	
	public Tagger(BufferedReader reader) throws IOException {
		posTransitions	= new Hashtable<String, Hashtable<String,Integer>>();
		posTokCount		= new Hashtable<String, Hashtable<String,Integer>>();
		posCount		= new Hashtable<String, Integer>();
		prevPosCount	= new Hashtable<String, Integer>();
		learn(reader);
	}
	
	public void add(
			Hashtable<String, Hashtable<String,Integer>> table,
			Hashtable<String, Integer> occCount,
			String w1, String w2) {
		Hashtable<String,Integer> w2count;
		if((w2count = table.get(w1)) == null) {
			table.put(w1,w2count = new Hashtable<String,Integer>());
			w2count.put(w2, 0);
		}
		Integer c;
		w2count.put(w2,((c = w2count.get(w2))==null?0:c)+1);
		occCount.put(w2,((c = occCount.get(w2))==null?0:c)+1);
	}
	
	private void learn(BufferedReader reader) throws IOException {
		String line;
		while((line = reader.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			String prevPos = "^", currPos;
			for(String tokPOS:tokens) {
				int seperator = tokPOS.lastIndexOf('/');
				String token = tokPOS.substring(0, seperator);
				currPos      = tokPOS.substring(seperator+1,tokPOS.length());
				add(posTransitions,	prevPosCount,	prevPos, currPos);
				add(posTokCount,	posCount,		currPos, token.toLowerCase());
				prevPos = currPos;
			}
		}
	}
	
	private float transProb(String pos1, String pos2) {
		int c  = posTransitions.get(pos1).get(pos2);
		int pc = prevPosCount.get(pos1);
		return ((float)c/pc);
	}
	public static void main(String[] args) {
		String sentsTrain = args[0];
		//String sentsTest  = args[2];
		File fTrain = new File(sentsTrain);
		//File fTest  = new File(sentsTest);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fTrain));
			Tagger t = new Tagger(reader);
			reader.close();
			//System.out.println(t.posTransitions);
			//System.out.println(t.posTokCount);
			//System.out.println(t.prevPosCount);
			System.out.println(t.transProb("POS","NN"));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
