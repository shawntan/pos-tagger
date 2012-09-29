import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

public class Tagger {

	private Hashtable<String, Hashtable<String,Integer>> posTransitions;
	private Hashtable<String, Hashtable<String,Integer>> posTokCount;
	private Hashtable<String, Integer> posCount;
	private Hashtable<String, Integer> prevPosCount;
	private HashSet<String> vocabulary;
	private Smoother smoother;

	public Tagger(BufferedReader reader) throws IOException {
		this.posTransitions = new Hashtable<String, Hashtable<String,Integer>>();
		this.posTokCount	= new Hashtable<String, Hashtable<String,Integer>>();
		this.posCount = new Hashtable<String, Integer>();
		this.prevPosCount = new Hashtable<String, Integer>();
		this.vocabulary = new HashSet<String>();
		learn(reader);
	}

	public void setSmoother(Tagger.Smoother smoother) {
		this.smoother = smoother;
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
		occCount.put(w1,((c = occCount.get(w1))==null?0:c)+1);
	}

	private void learn(BufferedReader reader) throws IOException {
		String line;
		while((line = reader.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			String prevPos = "^", currPos;
			for(String tokPOS:tokens) {
				int seperator = tokPOS.lastIndexOf('/');
				String token = tokPOS.substring(0, seperator).toLowerCase();
				currPos      = tokPOS.substring(seperator+1,tokPOS.length());
				add(posTransitions,	prevPosCount,	prevPos, currPos);
				add(posTokCount,	posCount,		currPos, token);
				vocabulary.add(token);
				prevPos = currPos;
			}
		}
	}
	
	private float tokProbGivenPOS(String pos, String tok) {
		int pc,c;
		pc = posCount.get(pos);
		Hashtable<String,Integer> cTable = posTokCount.get(pos);
		c  = cTable.containsKey(tok)?cTable.get(tok):0;
		System.out.println(pos + "\t" + tok);
		System.out.println(pc + "\t" + c);
		return smoother.tokenProbability(pc, c);
	}
	
	private void viterbi(Iterable<String> words){
		String prevPOS = "^", currPOS;
		for (String curr: words) {
			curr = curr.toLowerCase();
			//System.out.println(posTransitions.get(prevPOS));
			for (String pos: posTransitions.get(prevPOS).keySet()) {
				tokProbGivenPOS(pos, curr);
				//System.out.print(pos + " "+ tokProbGivenPOS(pos, curr) + "\t");
				//System.out.println();
			}
			System.out.println();
			
			
		}
	}
	

	public abstract class Smoother {
		public float transitionProbability(int prevPOS, int currPOS) { return 0; }
		public float tokenProbability(int posCount, int tokenCountPOS) { return 0; }
		public int getUniquePOSCount() {
			return posTransitions.size();
		}
		public int getUniqueWordCount() {
			return vocabulary.size();
		}
	}


	public static void main(String[] args) {
		String sentsTrain = args[0];
		//String sentsTest  = args[2];
		File fTrain = new File(sentsTrain);
		//File fTest  = new File(sentsTest);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fTrain));
			Tagger t = new Tagger(reader);
			t.setSmoother(t.new Smoother() {
				public float tokenProbability(int posCount, int tokenCountPOS) {
					return ((float)tokenCountPOS + 1)/(posCount + getUniqueWordCount());
				}
			});
			reader.close();
			
			t.viterbi(Arrays.asList("The fox sitting on the roof.".split("\\s+")));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
