import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.management.RuntimeErrorException;

public class Tagger implements Serializable {

	final static private boolean DEBUG = false;
	private Hashtable<String, Hashtable<String, Integer>> posTransitions;
	private Hashtable<String, Hashtable<String, Integer>> posTokCount;
	private Hashtable<String, Integer> posCount;
	private Hashtable<String, Integer> prevPosCount;

	private Smoother smoother;

	private Set<String> vocabulary;

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
	}

	private double tokProbGivenPOS(String pos, String tok) {
		int pc, c;
		pc = posCount.get(pos);
		Hashtable<String, Integer> cTable = posTokCount.get(pos);
		if(cTable==null) c = 0;
		else c = cTable.containsKey(tok) ? cTable.get(tok) : 0;
		return smoother.tokenProbability(pc, c);
	}
	private double posTransitions(String pos1, String pos2) {
		int prevPOS,currPOS;
		prevPOS = prevPosCount.get(pos1);
		Hashtable<String, Integer> cTable = posTransitions.get(pos1);
		if(cTable==null) currPOS = 0;
		else currPOS = cTable.containsKey(pos2) ? cTable.get(pos2) : 0;
		return smoother.transitionProbability(prevPOS, currPOS);
	}
	
	private String[] viterbi(String[] words) {
		Set<String> POS = posCount.keySet();
		Hashtable<String, String>[] t =	new Hashtable[words.length + 1];
		Hashtable<String,Double>[] V =	new Hashtable[words.length + 1];
		V[0] = new Hashtable<String, Double>();
		V[0].put("^", 0.0);
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].toLowerCase();
			V[i + 1] = new Hashtable<String, Double>();
			t[i + 1] = new Hashtable<String, String>();
			for (String currPOS : POS) {
				if (currPOS.equals("^")) continue;
				double maxProb = Double.NEGATIVE_INFINITY;
				String maxPOS = null;
				double obsvProb = Math.log(tokProbGivenPOS(currPOS, words[i]));
				Set<String> posList = i==0?V[i].keySet():POS;
				for (String prevPOS : posList) {
					double tranProb = Math.log(posTransitions(prevPOS,currPOS));
					double prevProb = V[i].get(prevPOS);
					double joinProb = prevProb + tranProb + obsvProb;
					if(DEBUG) System.out.printf(
							"%d\t%5s->%s\t%.10f\t%.10f\t%.10f\t%.10f\n",
							i,prevPOS,currPOS,tranProb,obsvProb,prevProb,joinProb);
					if (joinProb > maxProb) {
						maxProb = joinProb;
						maxPOS  = prevPOS;
					}
				}
				V[i+1].put(currPOS, maxProb);
				t[i+1].put(currPOS, maxPOS);
			}
		}
		String[] poss = new String[words.length];
		String lastPOS = null;
		double probPOS = Double.NEGATIVE_INFINITY;
		for (String pos : V[words.length].keySet()) {
			double prob = V[words.length].get(pos);
			if (prob > probPOS) {
				probPOS = prob;
				lastPOS = pos;
			}
		}
		String prevPOS = lastPOS;
		for (int i = words.length - 1; i >= 0; i--) {
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
		for (int i = 0; i < tokens.length; i++) {
			String tokPOS = tokens[i];
			int seperator = tokPOS.lastIndexOf('/');
			words[i] = tokPOS.substring(0, seperator).toLowerCase();
			correctPOS[i] = tokPOS.substring(seperator + 1, tokPOS.length());
		}
		String[] predictedPOS = viterbi(words);
		boolean[] correct = new boolean[tokens.length];
		for (int i = 0; i < correct.length; i++)
			correct[i] = predictedPOS[i].equals(correctPOS[i]);
		return correct;
	}

	public abstract class Smoother {
		public double transitionProbability(int prevPOS, int currPOS) {
			return 0;
		}

		public double tokenProbability(int posCount, int tokenCountPOS) {
			return 0;
		}

		public int getUniquePOSCount() {
			return posTransitions.size();
		}

		public int getUniqueWordCount() {
			return vocabulary.size();
		}
	}


}
