import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

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
	public Double[] globBest;
	public Double[] localBest = new Double[POPULATION_SIZE];
	public Double[][] locBest;
	public Double[][] speed;
	public Double[][] positions;
	public double weight;
	
	public PSO(ProblemInitiatior init){
		this.INIT = init;
		this.vmax = init.MACHINES * INIT.JOBS * 0.01;
		this.weight = WMAX;
		this.speed = new Double[POPULATION_SIZE][init.MACHINES*init.JOBS];
		this.globBest = new Double[init.MACHINES*init.JOBS];
		this.locBest = new Double[POPULATION_SIZE][init.MACHINES*init.JOBS];
		positions = new Double[POPULATION_SIZE][init.MACHINES*init.JOBS];
	}
	
	public double MPSO(){
		initiate();
		setOperationOrder(0);
		int iteration = 0;
		while(iteration < ITERATIONS){
			for (int i = 0; i < POPULATION_SIZE; i++) {
				double randomNumber = Math.random();
				if(randomNumber < MIE){
					//mIE(i);
				}
			}
			iteration++;
			//updateLocalandBest();
			updateWeight(iteration);
			updateSpeedandPosition();
		}
		System.out.println("Hello");
		return 0.0;
	}
	
	public void updateLocalandBest(){
		
	}
	
	public void updateWeight(int iteration){
		this.weight = WMAX - (iteration * ((WMAX - WMIN)/ITERATIONS));
	
	}
	
	public void updateSpeedandPosition(){
		for (int i = 0; i < POPULATION_SIZE; i++) {
			for (int j = 0; j < speed[i].length; j++) {
				speed[i][j] += weight * speed[i][j] + (this.C1 * Math.random() * (locBest[i][j] - positions[i][j])) +
						(this.C2 * Math.random() * (globBest[j] - positions[i][j]));
				if(speed[i][j] > vmax){
					speed[i][j] = vmax;
				}else if(speed[i][j] < -vmax){
					speed[i][j] = - vmax;
				}
				positions[i][j] += positions[i][j] + speed[i][j];
			}
		}
	}
	
	public void initiate(){
		Random rn = new Random();
		for (int i = 0; i < POPULATION_SIZE; i++) {
			for (int j = 0; j < INIT.MACHINES * INIT.JOBS ; j++) {
				double randomNumber = Math.random();
				this.positions[i][j] = (INIT.MACHINES * INIT.JOBS) * randomNumber;
				double speedInsert = randomNumber * this.vmax * (Math.pow(-1, rn.nextInt(10)));
				this.speed[i][j] = speedInsert;
				/// PLACEHOLDERS ///
				locBest[i][j] = (INIT.MACHINES * INIT.JOBS) * randomNumber;
				globBest[j] = (INIT.MACHINES * INIT.JOBS) * randomNumber;
			}
		}
	}
	
	public void mIE(int particle){
		for (int i = 0; i < POPULATION_SIZE; i++) {
			double randomNumber = Math.random();
			if(randomNumber < 0.4){
				swap(particle);
			}
		}	
	}
	
	public void swap(int individual){
		
	}
	
	public void insert(int individual){
		
	}
	
	public void inversion(int individual){
		
	}
	
	public void longDistance(int individual){
		
	}
	
	public void setOperationOrder(int individual){
		Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
		Double[][] sortedHolders = new Double[INIT.MACHINES * INIT.JOBS][2];
		for (int i = 0; i < positions[individual].length; i++) {
			sortedHolders[i][0] = positions[individual][i];
			sortedHolders[i][1] = i + 0.0;
		}Arrays.sort(sortedHolders, (a, b) -> Double.compare(a[0], b[0]));
		for (int i = 0; i < positions[individual].length; i++) {
			sortedPositions[sortedHolders[i][1].intValue()] = (i % INIT.JOBS) +1;
		}
		System.out.println("hello");
		
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
