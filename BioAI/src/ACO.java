import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;

public class ACO {
    //ACO CONSTANTS
    public static int ITERATIONS = 300;
    public static int POPULATION_SIZE = 36;
    public double INIT_PHEROMONE = 1;
    public double EVAP_CONST = 0.02;
    public double ALFA = 20;
    public double BETA = 2;
    public double Q;

    // ACO VARIABLES
    public ProblemInitiatior INIT;
    public int GlobalBest = Integer.MAX_VALUE;
    public ArrayList<Integer> globBest;
    HashMap<Integer, Node> graph;
    public Double [][] pherMatrix;

    //SA CONSTANTS
    public static double FINAL_TEMP = 0.1;
    public static double SA_RANDOM = 0.1;
    public static double COOLING_RATE = 0.97;
    public static double UPPER_RANDOM = 0.5;
    public static double LOWER_RANDOM = 0.01;

    //CONSTRUCTOR
    public ACO(ProblemInitiatior init) {
        this.INIT = init;
        this.globBest = new ArrayList<>();
        this.graph = initGraph(init);
        this.pherMatrix = new Double[init.JOBS * init.MACHINES+1][init.JOBS * init.MACHINES+1];
        for (int i = 0; i < pherMatrix.length; i++) {
            for (int j = 0; j < pherMatrix[i].length; j++) {
                pherMatrix[i][j]=INIT_PHEROMONE;
            }
        }
        int sum = 0;
        for (int i = 0; i < init.JSSP.length ; i++) {
            for (int d : init.JSSP[i]) sum += d;
        }
        this.Q = (double) sum/(init.MACHINES*init.JOBS)*10;
    }

    //MAIN RUNTIME METHOD
    public void runACO() {
        for (int iteration = 0; iteration < this.ITERATIONS; iteration++) {
            ArrayList<ArrayList<Integer>> tabu_k = new ArrayList<>();
            for (int ant_i = 0; ant_i < this.POPULATION_SIZE; ant_i++) {
                ArrayList<Integer> notVisited = new ArrayList<>();
                ArrayList<Integer> possNodes ;
                ArrayList<Integer> tempSol;
                ArrayList<Integer> tabu = new ArrayList<>();
                for (int i = 0; i < this.graph.size() - 1; i++) {
                    notVisited.add(i);
                }
                int currentIndex;
                while (notVisited.size() > 0){
                    possNodes = getPossNodes(tabu); // possibility of error in this method

                    if(tabu.size() == 0 ){
                        currentIndex = -1;
                    }
                    else {
                        currentIndex = tabu.get(tabu.size() - 1 );
                    }
                    int nextOpp = getNextOpp(possNodes, currentIndex, iteration);//aka State Transition Rule
                    notVisited.remove(new Integer(nextOpp));
                    tabu.add(nextOpp);
                }
                double randomNumber = Math.random();
                if(randomNumber < SA_RANDOM){
                    tempSol = sa(arrayListToInteger(tabu));
                    tabu = toLegalSoultion(tempSol);
                }
                tabu_k.add(tabu);
            }// All ants found a solution

            for (int ant_i = 0; ant_i < this.POPULATION_SIZE; ant_i++) {//Check if new best solution is found
                int score = setOperationOrder(arrayListToInteger(tabu_k.get(ant_i)));
                if(score < GlobalBest){
                    GlobalBest = score;
                    globBest = tabu_k.get(ant_i);
                }
            }//Check if new best solution is found
            pheromoneUpdate(globBest, GlobalBest);

            System.out.println("Iteration number "+ iteration +" Best so far is: " + GlobalBest);

        }// Finished with all iterations

        System.out.println(GlobalBest);
        setOperationOrderFinish(arrayListToInteger( globBest));
    }

    //INITIALISING METHODS //
    public HashMap<Integer,Node> initGraph(ProblemInitiatior init) {
        HashMap<Integer, Node> graph = new HashMap<Integer, Node>();
        ArrayList<Node> nodes = new ArrayList<>();

        Node startNode = new Node(-1,0);

        for (int i = 0; i < init.JOBS * init.MACHINES ; i++) {
            int opTime = getOpTime(init, i);
            Node tempNode = new Node(i,opTime);
            nodes.add(tempNode);
        }

        //Special case for startNode
        ArrayList<Node> possList = new ArrayList<>();
        for (int i = 0; i < init.JOBS; i++) {
            int index = i*init.MACHINES;
            possList.add(nodes.get(index));
        }
        startNode.updatePossList(possList);
        //End startNode

        for (int i = 0; i < init.JOBS * init.MACHINES ; i++) {//Add all possible nodes
            possList = new ArrayList<>();
            if ((i + 1) % init.MACHINES != 0 ){// if not last operation in job{
                possList.add(nodes.get(i + 1));
            }
            int jobIndex = i/init.MACHINES;
            for (int j = 0; j < init.JOBS * init.MACHINES; j++) {
                if ( j < jobIndex * init.MACHINES || j >= (jobIndex + 1) * init.MACHINES){ // if node is not in same job/column
                    possList.add(nodes.get(j));
                }
            }
            nodes.get(i).updatePossList(possList);
        }
        // make final graph
        graph.put(-1,startNode);
        for (int i = 0; i < init.JOBS * init.MACHINES; i++) {
            graph.put(i,nodes.get(i));
        }

        return graph;
    }

    // RUNTIME METHODS //
    private int getOpTime(ProblemInitiatior init, int i) {
        int row_job = i/init.MACHINES;
        int col = (i % init.MACHINES)*2 + 1;
        return init.JSSP[row_job][col];
    }
    private int getNextOpp(ArrayList<Integer> possNodes, int current, int iteration) {//aka State Transition Rule
        double doRandomMove = Math.random();
        double randLimit = getRandLimit(iteration);
        if (doRandomMove < randLimit){
            int randIndex = ThreadLocalRandom.current().nextInt(0, possNodes.size());
            int doMove = possNodes.get(randIndex);
            return doMove;
        }
        double [] transValue = new double[possNodes.size()];
        double [] probTrans = new double[possNodes.size()];
        int chosenInt=possNodes.get(0);
        for (int i = 0; i < possNodes.size(); i++) {
            double pheromoneIJ = this.pherMatrix[current + 1][possNodes.get(i) + 1];
            double heuristicIJ = 1; //1/(double)this.graph.get(possNodes.get(i)).op_time;
            double pher_heu_calc =  Math.pow(pheromoneIJ, this.ALFA) * Math.pow(heuristicIJ, this.BETA);
            transValue[i] = pher_heu_calc;
        }
        double transSum = DoubleStream.of(transValue).sum();

        for (int i = 0; i < transValue.length; i++) {
            if (i == 0){
                probTrans[i] = transValue[i]/transSum;
            }
            else {
                probTrans[i] = probTrans[i-1] + transValue[i]/transSum;
            }

        }
        double randomNumb = Math.random();
        for (int i = 0; i < probTrans.length; i++) {
            if (randomNumb <= probTrans[i]){
                chosenInt =  possNodes.get(i);
                return chosenInt;
            }
        }
        return chosenInt;
    }
    private ArrayList<Integer> getPossNodes(ArrayList<Integer> tabu) { // this could be done faster by having a index counter per job.
        ArrayList<Integer> possNodes = new ArrayList<>();
        ArrayList<Integer> startNodes = nodesToIntegers(this.graph.get(-1).possList);
        if (tabu.size() == 0){// If starting node
            possNodes = startNodes;
            return possNodes;
        }
        else {
            int curNodeIndex = tabu.get(tabu.size() - 1);
            possNodes = nodesToIntegers( this.graph.get(curNodeIndex).possList);
            // remove unrelevant nodes
            for (int i = 0; i < tabu.size(); i++) { //remove visited node from possNodes
                possNodes.remove(new Integer(tabu.get(i)));
            }
            //Find list of the first unvisited node in each job
            Integer [] firstUnvisited = new Integer[this.INIT.JOBS];
            for (int i = 0; i < firstUnvisited.length; i++) { // make list with 0
                firstUnvisited[i] = Integer.MAX_VALUE;
            }
            for (int i = 0; i < possNodes.size(); i++) {
                int jobNum = possNodes.get(i)/this.INIT.MACHINES;
                if ( possNodes.get(i) < firstUnvisited[jobNum]){
                    firstUnvisited[jobNum] = possNodes.get(i);
                }
            }
            //END Find list of the first unvisited node in each job
            ArrayList<Integer> newPossNodes = new ArrayList<>();
            for (int i = 0; i < firstUnvisited.length; i++) {
                if (firstUnvisited[i] != Integer.MAX_VALUE){
                    newPossNodes.add(firstUnvisited[i]);
                }
            }
            possNodes=newPossNodes;
        }
        return possNodes;
    }
    public void pheromoneUpdate(ArrayList<Integer> globBest, int makespan) {
        for (int i = 0; i <this.pherMatrix.length ; i++) {
            for (int j = 0; j < this.pherMatrix[i].length; j++) {
                pherMatrix[i][j] = pherMatrix[i][j] * (1 - this.EVAP_CONST);
            }
        }
        double deltaPher = this.Q/makespan;
        double extraPher = this.EVAP_CONST * deltaPher;
        this.pherMatrix[0][globBest.get(0) + 1] += extraPher;
        for (int node_i = 0; node_i < globBest.size() - 1; node_i++) {
            int i = globBest.get( node_i ) + 1;
            int j = globBest.get( node_i + 1) + 1;

            this.pherMatrix[i][j] += extraPher;
        }
    }

    // HELPER
    private double getRandLimit(int iteration) {
        double rand = UPPER_RANDOM * (ITERATIONS - iteration)/ITERATIONS;
        rand = Math.max(rand, LOWER_RANDOM);
        return rand;
    }
    public ArrayList<Integer> nodesToIntegers(ArrayList<Node> nodes)  {
        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {// convert from nodes to Integers
            Integer nodeInt=nodes.get(i).id;
            integers.add(nodeInt);
        }
        return integers;
    }
    public Integer [] arrayListToInteger ( ArrayList<Integer> arrList){
        Integer [] list = new Integer[arrList.size()];
        for (int i = 0; i < arrList.size(); i++) {
            list[i] = new Integer(arrList.get(i));
        }
        return list;
    }
    public ArrayList<Integer> integerToArrayList (Integer [] intList){
        ArrayList<Integer> arrayList= new ArrayList<>();
        for (int i = 0; i < intList.length; i++) {
            arrayList.add(intList[i]);
        }
        return arrayList;
    }


    //ALL LOCAL SEARCH METHODS //
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

    // THE SA ALGORITHM //
    public ArrayList<Integer> sa(Integer[] individual){
        int makespan = setOperationOrder(individual);
        double temperature = makespan - this.GlobalBest + 2;
        if (temperature == 0){
            temperature = 5;
        }
        while(temperature > this.FINAL_TEMP){
            Integer[] newIndivid = new Integer[individual.length];
            for (int i = 0; i < individual.length; i++) {
                newIndivid[i] = individual[i];
            }
            newIndivid = mIE(newIndivid);
            int newMakespan = setOperationOrder(newIndivid);
            int delta = newMakespan - makespan;
            double rand = 0;
            if(delta > 0){
                rand = Math.random();
                if(rand < Math.min(0, Math.exp(-delta/temperature)));{
                    makespan = newMakespan;
                    individual = newIndivid;
                }
            }else{
                makespan = newMakespan;
                individual = newIndivid;
            }
            temperature = COOLING_RATE * temperature;
        }
        return integerToArrayList( individual);
    }

    public ArrayList<Integer> toLegalSoultion(ArrayList<Integer> solA){
        Integer[] sol = arrayListToInteger(solA);
        ArrayList<Integer> legalSol = new ArrayList<>();
        int nodeID;
        int jobIndex;
        int [] counterJobs= new int[INIT.JOBS];
        for (int i = 0; i < sol.length; i++) {
            //jobIndex = (sol[i] % INIT.JOBS);
            jobIndex = sol[i]/INIT.MACHINES;
            nodeID = jobIndex * INIT.MACHINES + counterJobs[jobIndex];
            legalSol.add(nodeID);
            counterJobs[jobIndex] += 1;
        }

        return legalSol;
    }
    // --------------------//
    // ALL DECODE METHODS //
    // --------------------//

    // TRANSLATOR INTO JOBSLIST //
    public int setOperationOrder(Integer[] individual){
        Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
        Integer[] sortedPositionsOLD = new Integer[INIT.MACHINES * INIT.JOBS];
        int jobIndex;
        double  ind;
        double jobs;
        double beforeFloor;
        for (int i = 0; i < individual.length; i++) {
            sortedPositionsOLD[i] = (individual[i] % INIT.JOBS) +1;
            ind =(double)individual[i];
            jobs = (double)INIT.MACHINES;
            beforeFloor=ind/jobs;
            jobIndex=(int)Math.floor(beforeFloor) + 1;

            sortedPositions[i] = (individual[i]/INIT.MACHINES) + 1;
            if (jobIndex != sortedPositions[i]){
                int a=0;
            }
        }
        int [] test = new int [INIT.JOBS];
        int [] testOLD = new int [INIT.JOBS];
        for (int i = 0; i < sortedPositions.length; i++) {
            test[sortedPositions[i] - 1] += 1;
            testOLD[sortedPositionsOLD[i] - 1] +=1;
        }
        int[] max = decode2(sortedPositions);
        return max[0];
    }
    public int setOperationOrderFinish(Integer[] individual){
        Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
        for (int i = 0; i < individual.length; i++) {
            //sortedPositions[i] = (individual[i] % INIT.JOBS) +1;
            sortedPositions[i] = individual[i]/INIT.MACHINES + 1;
        }
        int max = decodeFinish2(sortedPositions);
        return max;
    }

    // DIFFERENT DECODERS (decided by mode)//
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
        System.out.println("The best is: " + max);
        //System.out.println(gant);
        try{
            printGant(gant);
        }catch(IOException o){
            System.out.println(o);
        }

        return max;
    }


    // ---------------- //
    // HELPER METHODS //
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


    // PRINTING THE FINAL SOLUTION //
    public void printGant(ArrayList<ArrayList<ArrayList<Integer>>> gant) throws IOException{
        String workingDirect = "writeToGant.py";
        String arguments = gant.toString();
        String path = "C:\\Users\\Martin\\Documents\\1(10)Semester\\AI2\\BioAI_Delivery4\\BioAI\\" + workingDirect; //ERROR ADDED ON PURPOSE TO PREVENT PRINTING
        //System.out.println(path);
        String[] cmd = {
                "python",
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
        ProblemReader reader = new ProblemReader(3);
        reader.readFile();
        ProblemInitiatior initiator = new ProblemInitiatior();
        initiator.initiate(reader.returnInput());
        ACO aco = new ACO(initiator);
        aco.runACO();

    }
}

