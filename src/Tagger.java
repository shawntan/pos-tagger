
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;


public class Tagger {
	
	public Hashtable<String, Hashtable<String,Integer>> posTransitions;
	public Hashtable<String, Hashtable<String,Integer>> posTokCount;
	public Tagger(BufferedReader reader) throws IOException {
		posTransitions = new Hashtable<String, Hashtable<String,Integer>>();
		posTokCount    = new Hashtable<String, Hashtable<String,Integer>>();
		learn(reader);
	}
	
	public void add(Hashtable<String, Hashtable<String,Integer>> table,
					String w1, String w2) {
		Hashtable<String,Integer> w2count;
		if((w2count = table.get(w1)) == null) {
			table.put(w1,w2count = new Hashtable<String,Integer>());
			w2count.put(w2, 0);
		}
		Integer c;
		w2count.put(w2,((c = w2count.get(w2))==null?0:c)+1);
	}
	
	
	
	private void learn(BufferedReader reader) throws IOException {
		String line;
		while((line = reader.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			String prevPos = null,currPos = null;
			for(String tokPOS:tokens) {
				String[] arrTokPos = tokPOS.split("\\/");
				String token = arrTokPos[0];
				currPos      = arrTokPos[arrTokPos.length-1];
				if(prevPos != null) add(posTransitions,prevPos,currPos);
				add(posTokCount,currPos,token.toLowerCase());
				prevPos = currPos;
			}
		}
	}
	
}
