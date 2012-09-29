import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Tagger {

	final static private String DATAFILE = "tagger.dat";
	private Hashtable<String, Hashtable<String, Integer>> posTransitions;
	private Hashtable<String, Hashtable<String, Integer>> posTokCount;
	private Hashtable<String, Integer> posCount;
	private Hashtable<String, Integer> prevPosCount;

	private Smoother smoother;

	private Set<String> vocabulary;
	private Set<String> POS;

	public Tagger(BufferedReader reader) throws IOException {
		this.posTransitions = new Hashtable<String, Hashtable<String, Integer>>();
		this.posTokCount = new Hashtable<String, Hashtable<String, Integer>>();
		this.posCount = new Hashtable<String, Integer>();
		this.prevPosCount = new Hashtable<String, Integer>();
		this.vocabulary = new HashSet<String>();
		learn(reader);
	}



	public void setSmoother(Tagger.Smoother smoother) {
		this.smoother = smoother;
	}

	public void add(Hashtable<String, Hashtable<String, Integer>> table,
			Hashtable<String, Integer> occCount, String w1, String w2) {
		Hashtable<String, Integer> w2count;
		if ((w2count = table.get(w1)) == null) {
			table.put(w1, w2count = new Hashtable<String, Integer>());
			w2count.put(w2, 0);
		}
		Integer c;
		w2count.put(w2, ((c = w2count.get(w2)) == null ? 0 : c) + 1);
		occCount.put(w1, ((c = occCount.get(w1)) == null ? 0 : c) + 1);
	}

	private void learn(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			String prevPos = "^", currPos;
			for (String tokPOS : tokens) {
				int seperator = tokPOS.lastIndexOf('/');
				String token = tokPOS.substring(0, seperator).toLowerCase();
				currPos = tokPOS.substring(seperator + 1, tokPOS.length());
				add(posTransitions, prevPosCount, prevPos, currPos);
				add(posTokCount, posCount, currPos, token);
				vocabulary.add(token);
				prevPos = currPos;
			}
		}
		POS = posCount.keySet();
	}

	private float tokProbGivenPOS(String pos, String tok) {
		int pc, c;
		pc = posCount.get(pos);
		Hashtable<String, Integer> cTable = posTokCount.get(pos);
		c = cTable.containsKey(tok) ? cTable.get(tok) : 0;
		return smoother.tokenProbability(pc, c);
	}

	private String[] viterbi(String[] words) {
		Hashtable<String, String>[] t	= new Hashtable[words.length + 1];
		Hashtable<String, Float>[] V	= new Hashtable[words.length + 1];
		V[0] = new Hashtable<String, Float>();
		V[0].put("^", 1.0f);
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].toLowerCase();
			V[i + 1] = new Hashtable<String, Float>();
			t[i + 1] = new Hashtable<String, String>();
			for (String currPOS : POS) {
				float maxProb = 0;
				String maxPOS = null;
				for (String prevPOS : V[i].keySet()) {
					Integer prevCount = prevPosCount.get(prevPOS);
					Integer tranCount = posTransitions.get(prevPOS).get(currPOS);
					if (tranCount == null || prevCount == null) continue;
					float tranProb = tranCount / (float) prevCount;
					float obsvProb = tokProbGivenPOS(currPOS, words[i]);
					float joinProb = V[i].get(prevPOS) * tranProb * obsvProb;
					if (joinProb > maxProb) {
						maxProb = joinProb;
						maxPOS = prevPOS;
					}
				}
				if (maxPOS != null) {
					V[i + 1].put(currPOS, maxProb);
					t[i + 1].put(currPOS, maxPOS);
				}
			}
		}
		String[] poss = new String[words.length];
		String lastPOS = null;
		float probPOS = 0;
		for(String pos: V[words.length].keySet()) {
			float prob = V[words.length].get(pos);
			if (prob > probPOS) {
				probPOS = prob;
				lastPOS = pos;
			}
		}
		String prevPOS = lastPOS;
		for(int i=words.length-1;i>=0;i--){
			poss[i] = prevPOS;
			prevPOS = t[i+1].get(prevPOS);
		}
		return poss;
	}
	
	
	public boolean[] testSentence(String line) {
		String[] tokens = line.split("\\s+");
		String[] words = new String[tokens.length];
		String[] correctPOS = new String[tokens.length];
		String prevPos = "^", currPos;
		for (int i=0;i<tokens.length;i++) {
			String tokPOS = tokens[i];
			int seperator = tokPOS.lastIndexOf('/');
			words[i] = tokPOS.substring(0, seperator).toLowerCase();
			correctPOS[i] = tokPOS.substring(seperator + 1, tokPOS.length());
		}
		String[] predictedPOS = viterbi(words);
		boolean[] correct = new boolean[tokens.length];
		for(int i=0;i<correct.length;i++) correct[i] = predictedPOS[i].equals(correctPOS[i]);
		return correct;
	}

	public abstract class Smoother {
		public float transitionProbability(int prevPOS, int currPOS) {
			return 0;
		}

		public float tokenProbability(int posCount, int tokenCountPOS) {
			return 0;
		}

		public int getUniquePOSCount() {
			return posTransitions.size();
		}

		public int getUniqueWordCount() {
			return vocabulary.size();
		}
	}

	public static void main(String[] args) {
		String sentsTrain = args[0];
		// String sentsTest = args[2];
		File fTrain = new File(sentsTrain);
		// File fTest = new File(sentsTest);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fTrain));
			Tagger t = new Tagger(reader);
			t.setSmoother(t.new Smoother() {
				public float tokenProbability(int posCount, int tokenCountPOS) {
					return ((float) tokenCountPOS + 1)
							/ (posCount + getUniqueWordCount());
				}
			});
			reader.close();
			//t.viterbi("Trinity have said they plan to begin delivery in the first quarter of next year ."
			System.out.println(
				Arrays.toString(
					t.testSentence(
			"PRECIOUS/NNP METALS/NNPS :/: Futures/NNP prices/NNS eased/VBD as/RB increased/VBN stability/NN and/CC strength/NN came/VBD into/IN the/DT securities/NNS markets/NNS ./."
					)
				)
			);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
