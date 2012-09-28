import java.io.BufferedInputStream;
import java.util.Hashtable;


public class Tagger {
	
	public Hashtable<String, Hashtable<String,Integer>> posTransitions;
	
	public Tagger(BufferedInputStream bis) {
		posTransitions = new Hashtable<String, Hashtable<String,Integer>>();
	}
	
	public void add(String w1, String w2) {
		Hashtable<String,Integer> w2count;
		if((w2count = posTransitions.get(w1)) == null) {
			posTransitions.put(w1,w2count = new Hashtable<String,Integer>());
			w2count.put(w2, 0);
		}
		Integer c;
		w2count.put(w2,((c = w2count.get(w2))==null?0:c)+1);
	}
	
	private void learn(BufferedInputStream bis) {
		
	}
	
}
