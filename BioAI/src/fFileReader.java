import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class fFileReader {
	
	private static final String FILENAME = "/Users/simenhellem/Documents/1.txt";

	public void readFile() {

		BufferedReader br = null;
		FileReader fr = null;

		try {

			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);
			String sCurrentLine;
			br = new BufferedReader(new FileReader(FILENAME));
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine.split(" "));
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

	public static void main(String[] args) {
		fFileReader test = new fFileReader();
		test.readFile();
	}
}
	
	


