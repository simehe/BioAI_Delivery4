import java.util.ArrayList;

public class ProblemInitiatior {
	
	public int MACHINES;
	public int JOBS;
	public Integer[][] JSSP;
	
	public void initiate(ArrayList<String> input){
		String[] readInput = input.get(0).trim().split("\\s+");
		JOBS = Integer.parseInt(readInput[0]);
		MACHINES = Integer.parseInt(readInput[1]);
		Integer[][] JSSP = new Integer[JOBS][MACHINES*2];
		for (int i = 1; i < input.size(); i++) {
			readInput = input.get(i).trim().split("\\s+");
			for (int j = 0; j < readInput.length; j++) {
				JSSP[i-1][j] = Integer.parseInt(readInput[j]);
			}
			
		}
		this.JSSP = JSSP;	
	}
	
	public static void main(String[] args) {
		ProblemReader reader = new ProblemReader();
		reader.readFile();
		ProblemInitiatior initiator = new ProblemInitiatior();
		initiator.initiate(reader.returnInput());
		System.out.println("hello");
	}
	

}
