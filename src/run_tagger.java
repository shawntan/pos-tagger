import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;


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
		t.setSmoother(t.new Smoother() {
			public double tokenProbability(int posCount, int tokenCountPOS) {
				return ((double) tokenCountPOS + 1)
						/ (posCount + getUniqueWordCount());
			}

			public double transitionProbability(int prevPOS, int currPOS) {
				return ((double) currPOS + 1)
						/ (prevPOS + getUniqueWordCount());
			}
		});
		System.out.println(
				Arrays.toString(
						t.testSentence(
								"For/IN six/CD years/NNS ,/, T./NNP " +
								"Marshall/NNP Hahn/NNP Jr./NNP has/VBZ " +
								"made/VBN corporate/JJ acquisitions/NNS " +
								"in/IN the/DT George/NNP Bush/NNP mode/NN " +
								":/: kind/JJ and/CC gentle/JJ ./.")));

	}

}
