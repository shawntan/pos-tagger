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
	final static private String START = "^";
	
	private Hashtable<String, Hashtable<String, Integer>>	countPos1Pos2;
	private Hashtable<String, Integer>						countPos1;
	private Hashtable<String, Hashtable<String, Integer>>	countPos2Pos1;
	private Hashtable<String, Integer>						countPos2;
	private Hashtable<String, Hashtable<String, Integer>>	countPosTok;
	private Hashtable<String, Integer>						countPos;
	private Hashtable<String, Hashtable<String, Integer>>	countTokPos;
	private Hashtable<String, Integer>						countTok;

	private Smoother smootherPosWord;
	private Smoother smootherPosPos;

	public Tagger(BufferedReader reader) throws IOException {
		this.countPos1Pos2		= new Hashtable<String, Hashtable<String, Integer>>();
		this.countPos2Pos1		= new Hashtable<String, Hashtable<String, Integer>>();
		this.countPosTok		= new Hashtable<String, Hashtable<String, Integer>>();
		this.countTokPos		= new Hashtable<String, Hashtable<String, Integer>>();
		this.countPos			= new Hashtable<String, Integer>();
		this.countTok			= new Hashtable<String, Integer>();
		this.countPos1			= new Hashtable<String, Integer>();
		this.countPos2			= new Hashtable<String, Integer>();
		learn(reader);
	}

	public void setSmootherPosWord(Tagger.Smoother smoother) {
		this.smootherPosWord = smoother;
		this.smootherPosWord.countCtx 		= this.countPos;
		this.smootherPosWord.countCtxCur	= this.countPosTok;
		this.smootherPosWord.countCur 		= this.countTok;
		this.smootherPosWord.countCurCtx	= this.countTokPos;
		this.smootherPosWord.vocab = new HashSet<String>(this.countTok.keySet());
	}
	public void setSmootherPosPos(Tagger.Smoother smoother) {
		this.smootherPosPos = smoother;	
		this.smootherPosPos.countCtx		= this.countPos1;
		this.smootherPosPos.countCtxCur		= this.countPos1Pos2;
		this.smootherPosPos.countCur		= this.countPos2;
		this.smootherPosPos.countCurCtx		= this.countPos2Pos1;
		this.smootherPosPos.vocab = new HashSet<String>(this.countPos.keySet());
	}
	
	public void add(Hashtable<String, Hashtable<String, Integer>> table,
			Hashtable<String, Integer> occCount, String w1, String w2) {
		Hashtable<String, Integer> w2count;
		if ((w2count = table.get(w1)) == null) {
			table.put(w1, w2count = new Hashtable<String, Integer>());
			w2count.put(w2, 0);
		}
		Integer c;
		w2count.put(w2,  ((c = w2count.get(w2)) == null ? 0 : c) + 1);
		occCount.put(w1, ((c = occCount.get(w1)) == null ? 0 : c) + 1);
	}

	private void learn(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			String prevPos = START, currPos;
			for (String tokPOS : tokens) {
				int seperator = tokPOS.lastIndexOf('/');
				String token = filterToken(tokPOS.substring(0, seperator));
				currPos = tokPOS.substring(seperator + 1, tokPOS.length());
				add(countPos1Pos2, countPos1, prevPos, currPos);
				add(countPos2Pos1, countPos2, currPos, prevPos);
				add(countPosTok, countPos, currPos, token);
				add(countTokPos, countTok, token, currPos);
				prevPos = currPos;
			}
		}
		setSmootherPosPos(new Smoother() {});
		setSmootherPosWord(new Smoother() {});
	}

	private double tokProbGivenPOS(String pos, String tok) {
		Hashtable<String,Integer> ht;
		if((ht = countPosTok.get(pos))!=null && ht.containsKey(tok))
			return smootherPosWord.alpha(pos, tok);
		else return smootherPosWord.gamma(pos)*smootherPosWord.pSmooth(pos, tok);
	}
	
	private double posTransitions(String pos1, String pos2) {
		Hashtable<String,Integer> ht;
		if((ht = countPos1Pos2.get(pos1))!=null && ht.containsKey(pos2))
			return smootherPosPos.alpha(pos1, pos2);
		else return smootherPosPos.gamma(pos1)*smootherPosPos.pSmooth(pos1, pos2);
	}
	
	private String filterToken(String token) {
		token = token.toLowerCase();
		token = token.replaceAll("[0-9]", "#");
		return token;
	}
	public String[] getTags(String[] words) {
		Set<String> POS = countPos.keySet();
		Hashtable<String, String>[] t =	new Hashtable[words.length + 1];
		Hashtable<String,Double>[] V =	new Hashtable[words.length + 1];
		V[0] = new Hashtable<String, Double>();
		V[0].put("^", 0.0);
		for (int i = 0; i < words.length; i++) {
			words[i] = filterToken(words[i]);
			V[i + 1] = new Hashtable<String, Double>();
			t[i + 1] = new Hashtable<String, String>();
			for (String currPOS : POS) {
				if (currPOS.equals(START)) continue;
				double maxProb = Double.NEGATIVE_INFINITY;
				String maxPOS = null;
				double obsvProb = Math.log(tokProbGivenPOS(currPOS, words[i]));
				Set<String> posList = i==0?V[i].keySet():POS;
				for (String prevPOS : posList) {
					double tranProb = Math.log(posTransitions(prevPOS,currPOS));
					double prevProb = V[i].get(prevPOS);
					double joinProb = prevProb + tranProb + obsvProb;
					if(DEBUG) System.out.printf(
							"%d\t%5s->%s\t%.10f\t%.10f\t%.10f\t%.10f\n" ,
							i,prevPOS,currPOS,tranProb,obsvProb,prevProb,joinProb);
					if (joinProb > maxProb){
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
	
	public abstract class Smoother implements Serializable {
		Hashtable<String,Hashtable<String,Integer>> countCtxCur;
		Hashtable<String,Integer> 					countCtx;
		Hashtable<String,Hashtable<String,Integer>> countCurCtx;
		Hashtable<String,Integer>					countCur;
		Set<String> vocab;
		
		public double alpha(String ctx, String cur) {
			return (countCtxCur.get(ctx).get(cur) + 1)/
					(double)(countCtx.get(ctx) + vocab.size());
		}
		
		public double gamma(String ctx) {
			return 1;
		}
		
		public double pSmooth(String ctx, String w) {
			return 1/(double)(countCtx.get(ctx) + vocab.size());
		}
	}
}
