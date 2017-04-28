import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	public int GlobalBest = 0;
	public Double[] globBest;
	public int[] localBest = new int[POPULATION_SIZE];
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
		setLocalAndBest();
		int iteration = 0;
		while(iteration < ITERATIONS){
			for (int i = 0; i < POPULATION_SIZE; i++) {
				double randomNumber = Math.random();
				if(randomNumber < MIE){
					SA(i);
				}
			}
			iteration++;
			System.out.println(iteration + " Best so far is: " + GlobalBest);
			//System.out.println(Arrays.toString(globBest));
			updateLocalandBest();
			updateWeight(iteration);
			updateSpeedandPosition();
		}
		System.out.println(GlobalBest);
		setOperationOrderFinish(globBest);
		return 0.0;
	}
	
	//INITIALISING METHODS //
	public void setLocalAndBest(){
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < POPULATION_SIZE; i++) {
			this.localBest[i] = setOperationOrder(i);
			this.locBest[i] = copyList(positions[i]);
			if(this.localBest[i] < min){
				min = this.localBest[i];
				GlobalBest = min;
				globBest = copyList(positions[i]);
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
	
	public Double[] copyList(Double[] list){
		Double[] newList = new Double[list.length];
		for (int i = 0; i < newList.length; i++) {
			newList[i] = list[i];
		}
		return newList;
	}
	
	
	// RUNTIME METHODS // 
	public void updateLocalandBest(){
		for (int i = 0; i < POPULATION_SIZE; i++) {
			int score = setOperationOrder(i);
			if(score < this.localBest[i]){
				this.localBest[i] = score;
				this.locBest[i] = copyList(positions[i]);
			}
			if(score < GlobalBest){
				GlobalBest = score;
				globBest = copyList(positions[i]);
			}
		}	
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
				positions[i][j] += speed[i][j];
			}
		}
	}
	
	
	public Double[] mIE(Double[] particle){
		double randomNumber = Math.random();
		if(randomNumber < 0.4){
			particle = swap(particle);
		}else if(randomNumber < 0.8){
			particle = insert(particle);
		}else if(randomNumber < 0.9){
			particle = inversion(particle);
			
		}else{
			particle = longDistance(particle);
		
		}
		
		return particle;	
	}
	
	//ALL LOCAL SEARCH METHODS //
	
	public Double[] swap(Double[] individual){
		Random rn = new Random();
		int indexOne = 0;
		int indexTwo = 0;
		while (indexOne == indexTwo){
			indexOne = rn.nextInt(individual.length);
			indexTwo = rn.nextInt(individual.length);
		}double first = individual[indexOne];
		double second = individual[indexTwo];
		individual[indexOne] = second;
		individual[indexTwo] = first;
		return individual;
	}
	
	//Can be optimized
	public Double[] insert(Double[] individual){
		Random rn = new Random();
		int indexOne = 0;
		int indexTwo = 0;
		while (indexOne == indexTwo){
			indexOne = rn.nextInt(individual.length);
			indexTwo = rn.nextInt(individual.length);
		}
		double first = individual[indexOne];
		ArrayList<Double> placeholder = new ArrayList<Double>(Arrays.asList(individual));
		placeholder.remove(indexOne);
		if(indexOne > indexTwo){
			placeholder.add(indexTwo, first);
		}else{
			placeholder.add(indexTwo-1, first);
		}individual = placeholder.toArray(new Double[placeholder.size()]);
		return individual;
	}
	
	public Double[] inversion(Double[] individual){
		Random rn = new Random();
		int indexOne = 0;
		int indexTwo = 0;
		while (indexOne == indexTwo){
			indexOne = rn.nextInt(individual.length);
			indexTwo = rn.nextInt(individual.length);
		}
		if(indexOne > indexTwo){
			int keep = indexTwo;
			indexTwo = indexOne;
			indexOne = keep;
		}
		Double[] holder = new Double[indexTwo-indexOne];
		for (int i = 0; i < holder.length; i++) {
			holder[i] = individual[indexOne+i];
		}
		for (int i = 0; i < holder.length; i++) {
			individual[indexOne+i] = holder[holder.length - 1 - i];
		}
		return individual;
			
	}
	public Double[] longDistance(Double[] individual){
		Random rn = new Random();
		int indexOne = 0;
		int indexTwo = 0;
		int indexThree = rn.nextInt(individual.length);
		while (indexOne >= indexTwo){
			indexOne = rn.nextInt(individual.length);
			indexTwo = rn.nextInt(individual.length);
		}while(indexThree > indexOne && indexThree < indexTwo){
			indexThree = rn.nextInt(individual.length);
		}
		ArrayList<Double> placeholder = new ArrayList<Double>(Arrays.asList(individual));
		ArrayList<Double> rev = new ArrayList<Double>();
		for (int i = indexOne; i <= indexTwo; i++) {
			rev.add(placeholder.get(indexOne));
			placeholder.remove(indexOne);
			
		}
		if(indexThree >= indexTwo){
			indexThree -= (indexTwo-indexOne);
		}
		int size = rev.size();
		for (int i = indexThree; i < size+indexThree; i++) {
			placeholder.add(i, rev.get(0));
			rev.remove(0);
		}
		individual = placeholder.toArray(new Double[placeholder.size()]);
		return individual;
	}
	
	// THE SA ALGORITHM //
	
	public void SA(int individual){
		int makespan = setOperationOrder(individual);
		double temperature = makespan - GlobalBest;
		while(temperature > this.FINAL_TEMP){
			Double[] newIndivid = new Double[positions[individual].length];
			for (int i = 0; i < positions[individual].length; i++) {
				newIndivid[i] = positions[individual][i];
			}
			newIndivid = mIE(newIndivid);
			int newMakespan = setOperationOrder(newIndivid);
			int delta = newMakespan - makespan;
			double rand = 0;
			if(delta > 0){
				rand = Math.random();
				if(rand < Math.min(0, Math.exp(-delta/temperature)));{
					makespan = newMakespan;
					positions[individual] = newIndivid;
				}
			}else{
				makespan = newMakespan;
				positions[individual] = newIndivid;
			}
			temperature = COOLING_RATE * temperature;
		}
	}
	
	// ALL DECODE METHODS // 
	
	public int setOperationOrder(int individual){
		Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
		Double[][] sortedHolders = new Double[INIT.MACHINES * INIT.JOBS][2];
		for (int i = 0; i < positions[individual].length; i++) {
			sortedHolders[i][0] = positions[individual][i];
			sortedHolders[i][1] = i + 0.0;
		}Arrays.sort(sortedHolders, (a, b) -> Double.compare(a[0], b[0]));
		for (int i = 0; i < positions[individual].length; i++) {
			sortedPositions[sortedHolders[i][1].intValue()] = (i % INIT.JOBS) +1;
		}int max = decode2(sortedPositions);	
		return max;
	}
	
	public int setOperationOrder(Double[] individual){
		Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
		Double[][] sortedHolders = new Double[INIT.MACHINES * INIT.JOBS][2];
		for (int i = 0; i < individual.length; i++) {
			sortedHolders[i][0] = individual[i];
			sortedHolders[i][1] = i + 0.0;
		}Arrays.sort(sortedHolders, (a, b) -> Double.compare(a[0], b[0]));
		for (int i = 0; i < individual.length; i++) {
			sortedPositions[sortedHolders[i][1].intValue()] = (i % INIT.JOBS) +1;
		}int max = decode2(sortedPositions);	
		return max;
	}
	
	// DIFFERENT DECODERS //
	
	public int decode(Integer[] jobs){
		ArrayList<Integer> jobsList = new ArrayList<Integer>();
		for (int i = 0; i < jobs.length; i++) {
			jobsList.add(jobs[i]);
		}
		//Integer[][] machinesSchedule = new Integer[INIT.MACHINES][]
		int[] makespan = new int[INIT.MACHINES];
		int[] jobCounter = new int[INIT.JOBS];
		int[] jobSchedule = new int[INIT.JOBS];
		int[] machineCounter = new int[INIT.MACHINES];
		int idleTime = 0;
		while(jobsList.size() > 0){
			Boolean jobTaken = true;
			int jobChosen = 0;
			int[] jobRestriction = new int[INIT.JOBS];
			while(jobTaken){
				int job = jobsList.get(jobChosen) -1;
				int machine = INIT.JSSP[job][jobSchedule[job]*2];
				int time = INIT.JSSP[job][1+jobSchedule[job]*2];
				if(jobCounter[job] >= 0 && machineCounter[machine] >= 0 && jobRestriction[job] == 0){
					jobTaken = false;
					jobCounter[job] -= time;
					machineCounter[machine] -= time;
					makespan[machine] += time;
					jobSchedule[job] += 1;
					jobsList.remove(jobChosen);
				}
				else{
					jobChosen++;
				}
				if(jobChosen == jobsList.size()){ // || checkMin(jobRestriction) > 1){
					for (int i = 0; i < machineCounter.length; i++) {
						if (machineCounter[i] >= 0){
							makespan[i] += 1;
						}
						machineCounter[i] = Math.min(0, machineCounter[i] + 1);
					}for (int i = 0; i < jobCounter.length; i++) {
						jobCounter[i] = Math.min(0, jobCounter[i] + 1);
					}jobTaken = false;
					idleTime++;
				}
				jobRestriction[job]++;
			}
		}
		return returnMax(makespan);
	}
	
	//NEED TO MAKE A BETTER DECODER
	
	// HELPER METHODS //
	public int checkMin(int[] jobs){
		int min = 1;
		for (int i = 0; i < jobs.length; i++) {
			if(jobs[i] < min){
				min = jobs[i];
			}
		}return min;
	}
	
	public int returnMax(int[] makespan){
		int max = 0;
		for (int i = 0; i < makespan.length; i++) {
			if (makespan[i] > max){
				max = makespan[i];
			}
		}
		return max;
	}
	
	// PRINTING THE FINAL SOLUTION //
	
	public void printGant(ArrayList<ArrayList<ArrayList<Integer>>> gant) throws IOException{
		String workingDirect = "writeToGant.py";
		String arguments = gant.toString();
		String path = "/Users/simenhellem/Documents/" + workingDirect;
		System.out.println(path);
        String[] cmd = {
                "/usr/local/bin/python",
                path,
                arguments,
                
        };
        String line;
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try{
        	while ((line = input.readLine()) != null) {
        		System.out.println(line);
        	}
        	
        }catch(Exception e){System.out.println(e);}
        input.close();
    }
	
	public int setOperationOrderFinish(Double[] individual){
		Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
		Double[][] sortedHolders = new Double[INIT.MACHINES * INIT.JOBS][2];
		for (int i = 0; i < individual.length; i++) {
			sortedHolders[i][0] = individual[i];
			sortedHolders[i][1] = i + 0.0;
		}Arrays.sort(sortedHolders, (a, b) -> Double.compare(a[0], b[0]));
		for (int i = 0; i < individual.length; i++) {
			sortedPositions[sortedHolders[i][1].intValue()] = (i % INIT.JOBS) +1;
		}int max = decodeFinish2(sortedPositions);	
		return max;
	}
	
	public int decodeFinish(Integer[] jobs){
		System.out.println(jobs.length);
		ArrayList<Integer> jobsList = new ArrayList<Integer>();
		ArrayList<ArrayList<ArrayList<Integer>>> gant = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> gantJob = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for (int i = 0; i < INIT.MACHINES; i++) {
			ArrayList<ArrayList<Integer>> machineList = new ArrayList<ArrayList<Integer>>();
			gant.add(machineList);
		}for (int i = 0; i < INIT.JOBS; i++) {
			ArrayList<ArrayList<Integer>> jList = new ArrayList<ArrayList<Integer>>();
			gantJob.add(jList);
		}
		for (int i = 0; i < jobs.length; i++) {
			jobsList.add(jobs[i]);
		}
		System.out.println("Lenght of joblist: " + jobsList.size());
		//Integer[][] machinesSchedule = new Integer[INIT.MACHINES][]
		int[] makespan = new int[INIT.MACHINES];
		int[] jobCounter = new int[INIT.JOBS];
		int[] jobSchedule = new int[INIT.JOBS];
		int[] machineCounter = new int[INIT.MACHINES];
		int idleTime = 0;
		while(jobsList.size() > 0){
			Boolean jobTaken = true;
			int jobChosen = 0;
			int[] jobRestriction = new int[INIT.JOBS];
			while(jobTaken){
				int job = jobsList.get(jobChosen) - 1;
				int machine = INIT.JSSP[job][jobSchedule[job]*2];
				int time = INIT.JSSP[job][1+jobSchedule[job]*2];
				if(jobCounter[job] >= 0 && machineCounter[machine] >= 0 && jobRestriction[job] == 0){
					ArrayList<Integer> addedTask = new ArrayList<Integer>();
					ArrayList<Integer> jTask = new ArrayList<Integer>();
					addedTask.add(job);
					addedTask.add(makespan[machine]);
					addedTask.add(time);
					jTask.add(machine);
					jTask.add(makespan[machine]);
					jTask.add(time);
					jobTaken = false;
					if (time == 0){
						System.out.println("FOund it");
					}
					jobCounter[job] -= time;
					machineCounter[machine] -= time;
					makespan[machine] += time;
					jobSchedule[job] += 1;
					jobsList.remove(jobChosen);
					gant.get(machine).add(addedTask);
					gantJob.get(job).add(jTask);
					
				}
				else{
					jobChosen++;
				}
				//Think this is the right way to do it
				if(jobChosen == jobsList.size() && jobsList.size() != 0){ //checkMin(jobRestriction) > 1){
					for (int i = 0; i < machineCounter.length; i++) {
						if (machineCounter[i] >= 0){
							makespan[i] += 1;
						}
						machineCounter[i] += 1;
						machineCounter[i] = Math.min(0, machineCounter[i]);
					}for (int i = 0; i < jobCounter.length; i++) {
						jobCounter[i] += 1;
						jobCounter[i] = Math.min(0, jobCounter[i]);
					}jobTaken = false;
					idleTime++;
				}
				jobRestriction[job]++;
			}
		}
		for (int i = 0; i < INIT.JOBS; i++) {
			Collections.sort(gantJob.get(i), (a, b) -> Integer.compare(a.get(1), b.get(1)));
		}
		System.out.println(gantJob);
		System.out.println(gant);
		testMachineJob(gant, gantJob);
		try{
			System.out.println("hello");
			printGant(gant);
		}catch(IOException o){
			System.out.println(o);
		}
		return returnMax(makespan);
	}
	
	public void testMachineJob(ArrayList<ArrayList<ArrayList<Integer>>> gant, ArrayList<ArrayList<ArrayList<Integer>>> gantJob){
		int maxM = 0;
		int maxJ = 0;
		int[] testM = new int[INIT.MACHINES];
		int[] testJ = new int[INIT.JOBS];
		for (int i = 0; i < INIT.MACHINES; i++) {
			testM[i] -= gant.get(i).get(0).get(1);
			int previous = 0;
			for (int j = 0; j < gant.get(i).size(); j++) {
				testM[i] += gant.get(i).get(j).get(1) - previous;
				previous = gant.get(i).get(j).get(1);
				if(testM[i] < 0){
					System.out.println("You did something wrong with machines, machine:  " + i + " Added: " + j);
				}testM[i] = 0;
				testM[i] -= gant.get(i).get(j).get(2);
				int mxM = gant.get(i).get(j).get(1) + gant.get(i).get(j).get(2);
				if (mxM > maxM){
					maxM = mxM;
				}
			}
		}
		for (int i = 0; i < gantJob.size(); i++) {
			testJ[i] -= gantJob.get(i).get(0).get(1);
			int previous = 0;
			for (int j = 0; j < gantJob.get(i).size(); j++) {
				testJ[i] += gantJob.get(i).get(j).get(1) - previous;
				previous = gantJob.get(i).get(j).get(1);
				if(testJ[i] < 0){
					System.out.println("You did something wrong with jobs, job: " + i + "Added: " + j);
				}testJ[i] = 0;
				testJ[i] -= gantJob.get(i).get(j).get(2);
				int mxJ = gantJob.get(i).get(j).get(1) + gantJob.get(i).get(j).get(2);
				if (mxJ > maxJ){
					maxJ = mxJ;
				}
			}
		}System.out.println(maxM);
		System.out.println(maxJ);
	}
	
	public int decode2(Integer[] jobs){
		ArrayList<Integer> jobsList = new ArrayList<Integer>();
		for (int i = 0; i < jobs.length; i++) {
			jobsList.add(jobs[i]);
		}
		int max = 0;
		int[] jobCounter = new int[INIT.JOBS];
		int[] jobSchedule = new int[INIT.JOBS];
		int[] makespan = new int[INIT.MACHINES];
		int[] machineCounter = new int[INIT.MACHINES];
		int[] makespanHolder = new int[INIT.JOBS];
		while (jobsList.size() > 0){
			int min = Integer.MAX_VALUE;
			int index = 0;
			int chosenMachine = 0;
			int[] jobRestriction = new int[INIT.JOBS];
			for (int i = 0; i < jobsList.size(); i++) {
				int job = jobsList.get(i) -1;
				if(jobRestriction[job] == 0){
					jobRestriction[job]++;
					int machine = INIT.JSSP[job][jobSchedule[job]*2];
					int time = INIT.JSSP[job][1+jobSchedule[job]*2];
					int startTime = Math.max(Math.abs(jobCounter[job]), Math.abs(machineCounter[machine]));
					int addedTime = startTime - Math.abs(machineCounter[machine]);
					startTime = Math.max(makespanHolder[job], makespan[machine]); //makespan[machine] + addedTime;
					int endTime = startTime + time;
					if (endTime < min){
						min = endTime;
						chosenMachine = machine;
						index = i;
					}
				}	
			}
			int useIndex = index;
			jobRestriction = new int[INIT.JOBS];
			for (int i = 0; i < jobsList.size(); i++) {
				int job = jobsList.get(i)-1;
				int machine = INIT.JSSP[job][jobSchedule[job]*2];
				if(jobRestriction[job] == 0 && machine == chosenMachine){
					jobRestriction[job]++;
					int time = INIT.JSSP[job][1+jobSchedule[job]*2];
					int startTimePos = Math.max(Math.abs(jobCounter[job]), Math.abs(machineCounter[machine]));
					int addedTime = startTimePos - Math.abs(machineCounter[machine]);
					startTimePos = Math.max(makespanHolder[job], makespan[machine]);//makespan[machine] + addedTime;
					if(startTimePos < min){
						min = startTimePos;
						useIndex = i;	
					}
				}
			}int job = jobsList.get(useIndex)-1;
			int machine = INIT.JSSP[job][jobSchedule[job]*2];
			int time = INIT.JSSP[job][1+jobSchedule[job]*2];
			jobCounter[job] = 0;
			machineCounter[machine] = 0;
			jobCounter[job] -= time;
			machineCounter[machine] -= time;
			makespanHolder[job] = time + min;
			makespan[machine] = time + min;
			jobSchedule[job] += 1;
			jobsList.remove(useIndex);
			if(makespan[machine] > max){
				max = makespan[machine];
			}
		}
			
		return max;
	}
	
	public int decodeFinish2(Integer[] jobs){
		ArrayList<Integer> jobsList = new ArrayList<Integer>();
		for (int i = 0; i < jobs.length; i++) {
			jobsList.add(jobs[i]);
		}
		ArrayList<ArrayList<ArrayList<Integer>>> gant = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for (int i = 0; i < INIT.MACHINES; i++) {
			ArrayList<ArrayList<Integer>> machineList = new ArrayList<ArrayList<Integer>>();
			gant.add(machineList);
		}
		int max = 0;
		int[] jobCounter = new int[INIT.JOBS];
		int[] jobSchedule = new int[INIT.JOBS];
		int[] makespan = new int[INIT.MACHINES];
		int[] makespanHolder = new int[INIT.JOBS];
		int[] machineCounter = new int[INIT.MACHINES];
		while (jobsList.size() > 0){
			int min = Integer.MAX_VALUE;
			int index = 0;
			int chosenMachine = 0;
			int[] jobRestriction = new int[INIT.JOBS];
			for (int i = 0; i < jobsList.size(); i++) {
				int job = jobsList.get(i) -1;
				if(jobRestriction[job] == 0){
					jobRestriction[job]++;
					int machine = INIT.JSSP[job][jobSchedule[job]*2];
					int time = INIT.JSSP[job][1+jobSchedule[job]*2];
					int startTime = Math.max(Math.abs(jobCounter[job]), Math.abs(machineCounter[machine]));
					int addedTime = startTime - Math.abs(machineCounter[machine]);
					startTime = Math.max(makespan[machine], makespanHolder[job]);  //makespan[machine] + addedTime;
					int endTime = startTime + time;
					if (endTime < min){
						min = endTime;
						chosenMachine = machine;
						index = i;
					}
				}	
			}
			int useIndex = index;
			jobRestriction = new int[INIT.JOBS];
			for (int i = 0; i < jobsList.size(); i++) {
				int job = jobsList.get(i)-1;
				int machine = INIT.JSSP[job][jobSchedule[job]*2];
				if(jobRestriction[job] == 0 && machine == chosenMachine){
					jobRestriction[job]++;
					int time = INIT.JSSP[job][1+jobSchedule[job]*2];
					int startTimePos = Math.max(Math.abs(jobCounter[job]), Math.abs(machineCounter[machine]));
					int addedTime = startTimePos - Math.abs(machineCounter[machine]);
					startTimePos = Math.max(makespan[machine], makespanHolder[job]);//makespan[machine] + addedTime;
					if(startTimePos < min){
						min = startTimePos;
						useIndex = i;	
					}
				}
			}int job = jobsList.get(useIndex)-1;
			int machine = INIT.JSSP[job][jobSchedule[job]*2];
			int time = INIT.JSSP[job][1+jobSchedule[job]*2];
			ArrayList<Integer> addedTask = new ArrayList<Integer>();
			jobCounter[job] = 0;
			machineCounter[machine] = 0;
			jobCounter[job] -= time;
			machineCounter[machine] -= time;
			addedTask.add(job);
			makespanHolder[job] = min + time;
			makespan[machine] = min;
			addedTask.add(makespan[machine]);
			makespan[machine] += time;
			addedTask.add(time);
			jobSchedule[job] += 1;
			jobsList.remove(useIndex);
			gant.get(machine).add(addedTask);
			if(makespan[machine] > max){
				max = makespan[machine];
			}
		}
		System.out.println(max);
		System.out.println(gant);
		try{
			System.out.println("hello");
			printGant(gant);
		}catch(IOException o){
			System.out.println(o);
		}
			
		return max;
	}
		
	
	public static void main(String[] args) {
		ProblemReader reader = new ProblemReader();
		reader.readFile();
		ProblemInitiatior initiator = new ProblemInitiatior();
		initiator.initiate(reader.returnInput());
		PSO pso = new PSO(initiator);
		pso.MPSO();
		/*
		Double[] tester = new Double[6];
		tester[0] = 1.4;
		tester[1] = 2.3;
		tester[2] = 0.8;
		tester[3] = 3.6;
		tester[4] = 4.2;
		tester[5] = 5.1;
		tester = pso.longDistance(tester);
		System.out.println("hello");
		*/
	
	}

}
