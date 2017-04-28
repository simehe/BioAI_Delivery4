import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class BA {
	
	public static int ITERATIONS = 500;
	public static int POPULATION_SIZE = 30;
	public static double FINAL_TEMP = 0.1;
	public static double COOLING_RATE = 0.97;
	public int M = 10;
	public int E = 5;
	public int NEP = 15;
	public int NSP = 10;
	public ProblemInitiatior INIT;
	public Integer[][] positions;
	public ArrayList<ArrayList<Integer>> critical = new ArrayList<ArrayList<Integer>>();
	public Integer[] scores;
	public Integer[] allTimeB;
	public int[] bestTabu = new int[3];
	public int allTimeBest;
	
	
	public BA(ProblemInitiatior init){
		this.INIT = init;
		this.positions = new Integer[POPULATION_SIZE][INIT.MACHINES*INIT.JOBS];
		this.scores = new Integer[POPULATION_SIZE];
		for (int i = 0; i < POPULATION_SIZE; i++) {
			ArrayList<Integer> crit = new ArrayList<Integer>();
			this.critical.add(crit);
		}
	}
	
	public void main(){
		initiate();
		setLocalAndBest();
		int iteration = 0;
		while(iteration < ITERATIONS){
			Integer[] sortedList = new Integer[POPULATION_SIZE];
			sortedList = sortList(sortedList);
			neighbourSearch(sortedList);
			generateNew(sortedList);
			update();
			double randomNumber = Math.random();
			if(randomNumber < 0.1){
				for (int i = 0; i < bestTabu.length; i++) {
					sa(this.bestTabu[i]);	
				}
			}
			iteration++;
			updateOverallandLocal();
			//System.out.println(Arrays.toString(this.bestTabu));
			System.out.println(iteration + " Best so far: " + this.scores[sortedList[0]]);
		}
		
	}
	
	public void update(){
		
	}
	
	public void updateOverallandLocal(){
		for (int i = 0; i < POPULATION_SIZE; i++) {
			scores[i] = setOperationOrder2(i);
			if(scores[i] < allTimeBest){
				allTimeBest = scores[i];
			}
		}
	}
	
	public Integer[] crossOver(Integer[] newPop, int index){
		Random rn = new Random();
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		int firstInt = rn.nextInt(this.critical.get(index).size());
		int secondInt = rn.nextInt(this.critical.get(index).size());
		while(firstInt >= secondInt){
			firstInt = rn.nextInt(this.critical.get(index).size());
			secondInt = rn.nextInt(this.critical.get(index).size());
		}
		
		for (int i = firstInt; i <= secondInt; i++) {
			int number = this.critical.get(index).get(i);
			numbers.add(number);
		}for (int i = 0; i < numbers.size(); i++) {
			int placeholder = 0;
			for (int j = 0; j < newPop.length; j++) {
				if (newPop[i] == positions[index][numbers.get(i)]){
					placeholder = newPop[numbers.get(j)];
					newPop[numbers.get(j)] = positions[index][numbers.get(i)];
					newPop[i] = placeholder;
				}
			}
		}return newPop;
	}
	
	public void generateNew(Integer[] sortedList){
		int best = Integer.MAX_VALUE;
		int indexRem = 0;
		for (int i = 1 + M + E ; i < POPULATION_SIZE; i++) {
			Integer[] newPop = new Integer[INIT.MACHINES*INIT.JOBS];
			for (int j = 0; j < newPop.length; j++) {
				newPop[j] = j;
			}
			newPop = shuffleArray(newPop);
			newPop = crossOver(newPop, sortedList[0]);
			positions[sortedList[i]] = newPop;
			scores[sortedList[i]] = setOperationOrder(newPop);
			if(scores[sortedList[i]] < best){
				best = scores[sortedList[i]];
				indexRem = sortedList[i];
			}
		}this.bestTabu[2] = indexRem;
		
	}
	
	public void sa(int individual){
		int makespan = setOperationOrder2(individual);
		double temperature = makespan - allTimeBest;
		while(temperature > this.FINAL_TEMP){
			Integer[] newIndivid = new Integer[positions[individual].length];
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
	
	public Integer[] mIE(Integer[] particle){
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
	
	public void generateNewCross(int start){
		
	}
	
	public void neighbourSearch(Integer[] sortedList){
		double randomVar = Math.random();
		if(randomVar < 1){
			multipleSearch(sortedList, NEP);
			multipleSearch(sortedList, NSP);
			//Find the best from each or something
			
		}
	}
	
	public void multipleSearch(Integer[] sortedList, int mode){
		int start = 1;
		int modeNumber = E;
		int bestIndex = 0;
		int bestScore = Integer.MAX_VALUE;
		int insertBest = 0;
		if(mode == NSP){
			start = 1 + E;
			modeNumber = M;
			insertBest = 1;
		}for (int i = start; i < start + modeNumber; i++) {
			//Integer[] best = new Integer[positions[sortedList[i]].length];
			for (int j = 0; j < mode; j++) {
				Integer[] newPop = copyList(positions[sortedList[i]]);
				double randomNumber = Math.random();
				if(randomNumber < 0.4){
					newPop = swap(newPop);
				}else if(randomNumber < 0.8){
					newPop = insert(newPop);
				}else if(randomNumber < 0.9){
					newPop = inversion(newPop);
				}else{
					newPop = longDistance(newPop);
				}
				int score = setOperationOrder(newPop);
				if(score < scores[sortedList[i]]){
					scores[sortedList[i]] = score;
					positions[sortedList[i]] = copyList(newPop);
				}
			}
			if(scores[sortedList[i]] < bestScore){
				bestIndex = i;
				bestScore = scores[sortedList[i]];
			}
		}this.bestTabu[insertBest] = bestIndex;
	}
	
	public Integer[] swap(Integer[] individual){
		Random rn = new Random();
		int indexOne = 0;
		int indexTwo = 0;
		while (indexOne == indexTwo){
			indexOne = rn.nextInt(individual.length);
			indexTwo = rn.nextInt(individual.length);
		}int first = individual[indexOne];
		int second = individual[indexTwo];
		individual[indexOne] = second;
		individual[indexTwo] = first;
		return individual;
	}
	
	//Can be optimized
	public Integer[] insert(Integer[] individual){
		Random rn = new Random();
		int indexOne = 0;
		int indexTwo = 0;
		while (indexOne == indexTwo){
			indexOne = rn.nextInt(individual.length);
			indexTwo = rn.nextInt(individual.length);
		}
		int first = individual[indexOne];
		ArrayList<Integer> placeholder = new ArrayList<Integer>(Arrays.asList(individual));
		placeholder.remove(indexOne);
		if(indexOne > indexTwo){
			placeholder.add(indexTwo, first);
		}else{
			placeholder.add(indexTwo-1, first);
		}individual = placeholder.toArray(new Integer[placeholder.size()]);
		return individual;
	}
	
	public Integer[] inversion(Integer[] individual){
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
		Integer[] holder = new Integer[indexTwo-indexOne];
		for (int i = 0; i < holder.length; i++) {
			holder[i] = individual[indexOne+i];
		}
		for (int i = 0; i < holder.length; i++) {
			individual[indexOne+i] = holder[holder.length - 1 - i];
		}
		return individual;
			
	}
	public Integer[] longDistance(Integer[] individual){
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
		ArrayList<Integer> placeholder = new ArrayList<Integer>(Arrays.asList(individual));
		ArrayList<Integer> rev = new ArrayList<Integer>();
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
		individual = placeholder.toArray(new Integer[placeholder.size()]);
		return individual;
	}
	
	
	public Integer[] sortList(Integer[] list){
		int x = 0;
		for (int i = 0; i < list.length; i++) {
			list[i] = x;
			x++;
		}
		Arrays.sort(list, (a, b) -> Integer.compare(scores[a], scores[b]));
		return list;
	}
	
	public void setLocalAndBest(){
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < POPULATION_SIZE; i++) {
			this.scores[i] = setOperationOrder2(i);
			if(this.scores[i] < min){
				min = this.scores[i];
				allTimeBest = min;
				allTimeB = copyList(positions[i]);
			}
		}
	}
	
	public void initiate(){
		for (int i = 0; i < POPULATION_SIZE; i++) {
			for (int j = 0; j < INIT.MACHINES * INIT.JOBS ; j++) {
				this.positions[i][j] = j;
			}
			positions[i] = shuffleArray(positions[i]);
		}
	}
	
	private Integer[] shuffleArray(Integer[] array)
	{
	    int index, temp;
	    Random random = new Random();
	    for (int i = array.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	        temp = array[index];
	        array[index] = array[i];
	        array[i] = temp;
	    }
	    return array;
	}
	
	public Integer[] copyList(Integer[] list){
		Integer[] newList = new Integer[list.length];
		for (int i = 0; i < newList.length; i++) {
			newList[i] = list[i];
		}
		return newList;
	}
	
	
	// ALL DECODE METHODS // 
	
		public int setOperationOrder(Integer[] individual){
			Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
			Integer[][] sortedHolders = new Integer[INIT.MACHINES * INIT.JOBS][2];
			for (int i = 0; i < individual.length; i++) {
				sortedPositions[i] = (individual[i] % INIT.JOBS) +1;
			}int[] max = decode2(sortedPositions);	
			return max[0];
		}
		
		public int setOperationOrder2(int individ){
			Integer[] individual = positions[individ];
			Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
			Integer[][] sortedHolders = new Integer[INIT.MACHINES * INIT.JOBS][2];
			for (int i = 0; i < individual.length; i++) {
				sortedPositions[i] = (individual[i] % INIT.JOBS) +1;
			}
			//System.out.println(Arrays.toString(sortedPositions));
			int[] max = decode2(sortedPositions);
			this.critical.get(individ).clear();
			for (int i = 1; i < max.length; i++) {
				this.critical.get(individ).add(max[i]);
			}
			return max[0];
		}
		
		// DIFFERENT DECODERS //
		
		public int[] decode(Integer[] jobs){
			ArrayList<Integer> jobsList = new ArrayList<Integer>();
			for (int i = 0; i < jobs.length; i++) {
				jobsList.add(jobs[i]);
			}
			//Integer[][] machinesSchedule = new Integer[INIT.MACHINES][]
			int[] makespan = new int[INIT.MACHINES];
			int[] jobCounter = new int[INIT.JOBS];
			int[] jobSchedule = new int[INIT.JOBS];
			int[] machineCounter = new int[INIT.MACHINES];
			ArrayList<ArrayList<Integer>> machineControl = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < INIT.MACHINES; i++) {
				ArrayList<Integer> indexControl = new ArrayList<Integer>();
				machineControl.add(indexControl);
			}
			ArrayList<Integer> indexControl = new ArrayList<Integer>();
			for (int i = 0; i < jobsList.size(); i++) {
				indexControl.add(i);
			}
			while(jobsList.size() > 0){
				Boolean jobTaken = true;
				int jobChosen = 0;
				int[] jobRestriction = new int[INIT.JOBS];
				while(jobTaken){
					int job = jobsList.get(jobChosen) - 1;
					int machine = INIT.JSSP[job][jobSchedule[job]*2];
					int time = INIT.JSSP[job][1+jobSchedule[job]*2];
					if(jobCounter[job] >= 0 && machineCounter[machine] >= 0 && jobRestriction[job] == 0){
						jobTaken = false;
						jobCounter[job] -= time;
						machineCounter[machine] -= time;
						makespan[machine] += time;
						jobSchedule[job] += 1;
						jobsList.remove(jobChosen);
						machineControl.get(machine).add(indexControl.get(jobChosen));
						indexControl.remove(jobChosen);
						
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
					}
					jobRestriction[job]++;
				}
			}
			int[] ret = returnMax(makespan);
			int[] retur = new int[1 + machineControl.get(ret[1]).size()];
			retur[0] = ret[0];
			for (int i = 1; i < retur.length; i++) {
				retur[i] = machineControl.get(ret[1]).get(i-1);
			}
			return retur;
			
		}
		
		public int[] decode2(Integer[] jobs){
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
			ArrayList<ArrayList<Integer>> machineControl = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < INIT.MACHINES; i++) {
				ArrayList<Integer> indexControl = new ArrayList<Integer>();
				machineControl.add(indexControl);
			}
			ArrayList<Integer> indexControl = new ArrayList<Integer>();
			for (int i = 0; i < jobsList.size(); i++) {
				indexControl.add(i);
			}
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
				machineControl.get(machine).add(indexControl.get(useIndex));
				indexControl.remove(useIndex);
				if(makespan[machine] > max){
					max = makespan[machine];
				}
			}
			int[] ret = returnMax(makespan);
			int[] retur = new int[1 + machineControl.get(ret[1]).size()];
			retur[0] = ret[0];
			for (int i = 1; i < retur.length; i++) {
				retur[i] = machineControl.get(ret[1]).get(i-1);
			}
			return retur;
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
		
		public int[] returnMax(int[] makespan){
			int max = 0;
			int index = 0;
			for (int i = 0; i < makespan.length; i++) {
				if (makespan[i] > max){
					max = makespan[i];
					index = i;
				}
			}int[] ret = new int[2];
			ret[0] = max;
			ret[1] = index;
			return ret;
		}
		
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
		
		
		public static void main(String[] args) {
			ProblemReader reader = new ProblemReader();
			reader.readFile();
			ProblemInitiatior initiator = new ProblemInitiatior();
			initiator.initiate(reader.returnInput());
			BA ba = new BA(initiator);
			ba.main();
		
		}
		
}

