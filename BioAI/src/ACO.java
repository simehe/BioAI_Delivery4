import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.DoubleStream;

public class ACO {
    // CONSTANTS AND VARIABLES //
    public static int ITERATIONS = 300;
    public static int POPULATION_SIZE = 30;
    public double INIT_PHEROMONE = 2;
    public double EVAP_CONST = 2;
    public double ALFA = 2;
    public double BETA = 2;

    public ProblemInitiatior INIT;
    public int GlobalBest = Integer.MAX_VALUE;

    public ArrayList<Integer> globBest;
    HashMap<Integer, Node> graph;
    public Double [][] pherMatrix;


    public ACO(ProblemInitiatior init) {
        this.INIT = init;
        this.globBest = new ArrayList<>(init.JOBS * init.MACHINES);
        this.graph = initGraph(init);
        this.pherMatrix = new Double[init.JOBS * init.MACHINES+1][init.JOBS * init.MACHINES+1];
        for (int i = 0; i < pherMatrix.length; i++) {
            for (int j = 0; j < pherMatrix[i].length; j++) {
                pherMatrix[i][j]=INIT_PHEROMONE;
            }
        }
    }

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

    private int getOpTime(ProblemInitiatior init, int i) {
        int row_job = i/init.MACHINES;
        int col = (i % init.MACHINES)*2 + 1;
        return init.JSSP[row_job][col];
    }

    public void runACO() {
        for (int iteration = 0; iteration < this.ITERATIONS; iteration++) {
            ArrayList<ArrayList<Integer>> tabu_k = new ArrayList<>();
            for (int ant_i = 0; ant_i < this.POPULATION_SIZE; ant_i++) {
                ArrayList<Integer> notVisited = new ArrayList<>();
                ArrayList<Integer> possNodes ;
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
                    int nextOpp = getNextOpp(possNodes, currentIndex);//aka State Transition Rule
                    notVisited.remove(new Integer(nextOpp));
                    tabu.add(nextOpp);
                }
                tabu_k.add(tabu);
            }// All ants found a solution

            for (int ant_i = 0; ant_i < this.POPULATION_SIZE; ant_i++) {//Check if new best solution is found
                int score = setOperationOrder(tabu_k.get(ant_i), false);
                if(score < GlobalBest){
                    GlobalBest = score;
                    globBest = tabu_k.get(ant_i);
                }
            }//Check if new best solution is found

            pheromoneUpdate(globBest);
        }// Finished with all iterations

        System.out.println(GlobalBest);
        setOperationOrder(globBest, true);
    }

    public void pheromoneUpdate(ArrayList<Integer> globBest) {
    }

    public int setOperationOrder(ArrayList<Integer> individual, boolean finished){
        Integer[] sortedPositions = new Integer[INIT.MACHINES * INIT.JOBS];
        //Convert from Arraylist to Integer []
        int max=0;
        if (finished){
            max = decodeFinish2(sortedPositions);
        }
        else {
            max = decode2(sortedPositions);
        }
        return max;
    }
    public Integer[] copyList(Integer[] list){
        Integer[] newList = new Integer[list.length];
        for (int i = 0; i < newList.length; i++) {
            newList[i] = list[i];
        }
        return newList;
    }

    private int getNextOpp(ArrayList<Integer> possNodes, int current) {//aka State Transition Rule
        double [] transValue = new double[possNodes.size()];
        double [] probTrans = new double[possNodes.size()];
        int chosenInt=possNodes.get(0);
        for (int i = 0; i < possNodes.size(); i++) {
            double pheromoneIJ = this.pherMatrix[current + 1][possNodes.get(i) + 1];
            double heuristicIJ = 1/(double)this.graph.get(possNodes.get(i)).op_time;
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

    public ArrayList<Integer> nodesToIntegers(ArrayList<Node> nodes) {
        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {// convert from nodes to Integers
            Integer nodeInt=nodes.get(i).id;
            integers.add(nodeInt);
        }
        return integers;
    }

    //COPIED FROM PSO:
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
    public void printGant(ArrayList<ArrayList<ArrayList<Integer>>> gant) throws IOException{
        String workingDirect = "writeToGant.py";
        String arguments = gant.toString();
        String path = "C:\\Users\\Martin\\Documents\\1(10)Semester\\AI2\\BioAI_Delivery4\\BioAI\\" + workingDirect;
        String path2 = System.getProperty("user.dir") + workingDirect;
        System.out.println(path);
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
    //END COPY

    public static void main(String[] args) {
        ProblemReader reader = new ProblemReader();
        reader.readFile();
        ProblemInitiatior initiator = new ProblemInitiatior();
        initiator.initiate(reader.returnInput());
        ACO aco = new ACO(initiator);
        aco.runACO();

    }
}



