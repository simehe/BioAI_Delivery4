import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ProblemReader {
	
	private String FILENAME = "/Users/simenhellem/Documents/";
	private ArrayList<String> input = new ArrayList<String>();
	public ArrayList<Integer> optimal = new ArrayList<Integer>();
	public int mode;

	public ProblemReader(int mode){
		FILENAME += mode + ".txt";
		this.mode = mode;
		optimal.add(55);
		optimal.add(930);
		optimal.add(1165);
		optimal.add(1005);
		optimal.add(1235);
		optimal.add(943);
	}
	public void readFile() {
		BufferedReader br = null;
		FileReader fr = null;
		

		try {

			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);
			String sCurrentLine;
			br = new BufferedReader(new FileReader(FILENAME));
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.length() > 1){
					input.add(sCurrentLine);
				}
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
	
	public double calculateOptimal(int results){
		double diff = results - this.optimal.get(mode-1);
		double success = diff/(this.optimal.get(mode-1));
		return success;
	}

	public static void main(String[] args) {
		ProblemReader test = new ProblemReader(2);
		test.readFile();
		System.out.println("Testing");
	}
	
	
}
	
	


