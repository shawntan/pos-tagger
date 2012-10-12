
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;


public class run_tagger {

	public static Tagger load(String filename) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File(filename)));
			return (Tagger) ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String tagLine(Tagger t, String line) {
		String[] origin = line.split("\\s+");
		String[] tokens = line.split("\\s+");
		StringBuilder b = new StringBuilder();
		String[] pos = t.getTags(tokens);
		for(int i=0;i<tokens.length;i++) {
			b.append(origin[i]);
			b.append('/');
			b.append(pos[i]);
			if(i < tokens.length-1) b.append(' ');
		}
		return b.toString();
	}
	
	public static void main(String[] args){
		int c = 0;
		String testFile		= args[c++];
		String modelFile	= args[c++];
		String outFile		= args[c++];
		Tagger t = load(modelFile);
		
		File fTest = new File(testFile);
		File fOut  = new File(outFile);
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(fTest));
			PrintWriter out = new PrintWriter(fOut);
			String line;
			while ((line = in.readLine()) != null) out.println(tagLine(t,line));
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
