import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;


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
	
	
	public static void main(String[] args) {
		int c = 0;
		String modelFile = args[c++];
		Tagger t = load(modelFile);
		


	}

}
