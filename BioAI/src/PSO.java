import java.util.ArrayList;

public class PSO {
	
	public static double C1 = 2.0;
	public static double C2 = 2.0;
	public static double WMAX = 1.4;
	public static double WMIN = 0.4;
	public static double MIE = 0.01;
	public static double FINAL_TEMP = 0.1;
	public static double COOLING_RATE = 0.97;
	public static int RUNS = 10;
	public static int ITERATIONS = 300;
	public static int POPULATION_SIZE = 30;
	public double vmax;
	public ProblemInitiatior INIT;
	public double GlobalBest = 0;
	public Double[] localBest = new Double[30];
	public ArrayList<ArrayList<Double>> positions;
	public double weight;
	
	public PSO(ProblemInitiatior init){
		this.INIT = init;
		this.vmax = init.MACHINES * INIT.JOBS * 0.01;
		this.weight = WMAX;
		positions = new ArrayList<ArrayList<Double>>();
	}
	
	public double MPSO(){
		initiate();
		int iteration = 0;
		while(iteration < ITERATIONS){
			for (int i = 0; i < POPULATION_SIZE; i++) {
				double randomNumber = Math.random();
				if(randomNumber < MIE){
					mIE(i);
				}
			}
			iteration++;
		}
		System.out.println("Hello");
		return 0.0;
	}
	
	public void updateLocalandBest(){
		
	}
	
	public void updateWeight(){
		
	}
	
	public void updatePosition(){
		
	}
	
	public void initiate(){
		for (int i = 0; i < POPULATION_SIZE; i++) {
			ArrayList<Double> particle = new ArrayList<Double>();
			for (int j = 0; j < INIT.MACHINES * INIT.JOBS ; j++) {
				double randomNumber = Math.random();
				particle.add((INIT.MACHINES * INIT.JOBS) * randomNumber);
			}
		}
	}
	
	public void mIE(int particle){
		
	}
	
	public void setOperationOrder(){
		
	}
	
	public void decode(){
		
	}
	
	public static void main(String[] args) {
		ProblemReader reader = new ProblemReader();
		reader.readFile();
		ProblemInitiatior initiator = new ProblemInitiatior();
		initiator.initiate(reader.returnInput());
		PSO pso = new PSO(initiator);
		pso.MPSO();
	
	}

}
