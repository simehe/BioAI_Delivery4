import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ProblemReader {
	
	private static final String FILENAME = "/Users/simenhellem/Documents/1.txt";
	private ArrayList<String> input = new ArrayList<String>();

	public void readFile() {

		BufferedReader br = null;
		FileReader fr = null;

		try {

			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);
			String sCurrentLine;
			br = new BufferedReader(new FileReader(FILENAME));
			while ((sCurrentLine = br.readLine()) != null) {
				input.add(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public ArrayList<String> returnInput(){
		return input;
	}

	public static void main(String[] args) {
		ProblemReader test = new ProblemReader();
		test.readFile();
	}
}
	
	


