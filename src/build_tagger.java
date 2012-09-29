
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class build_tagger {

	
	public void save(String datafile, Tagger t) {
		
		File f = new File(datafile);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
			oos.writeObject(t);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
			reader.close();
			//System.out.println(t.posTransitions);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
