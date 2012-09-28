import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class build_tagger {

	public static void main(String[] args) {
		String sentsTrain = args[0];
		//String sentsTest  = args[2];
		File fTrain = new File(sentsTrain);
		//File fTest  = new File(sentsTest);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fTrain));
			Tagger t = new Tagger(reader);
			reader.close();
			System.out.println(t.posTransitions);
			System.out.println(t.posTokCount);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
