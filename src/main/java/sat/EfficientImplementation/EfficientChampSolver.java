package sat.EfficientImplementation;

import sat.common.Common;
import sat.common.Logger;
import sat.common.Stopwatch;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.DoubleStream;

import static sat.common.Common.*;

public class EfficientChampSolver extends EfficientAbstractSolver{
    public static void main(String[] args) {
        System.out.println(DoubleStream.iterate(100.0,i->i*0.94).limit(33).average());
    }
    public static final int MINIMAL_SAVING_LEVEL = -1000000;
    boolean firstExecutionInCycle = true;
    double[] saverMinusDeserterLevels;
    static int[] histogram = new int[1001];
    EfficientInstance residualInstance;
    boolean[] valueInFirstCycle;
    Assignment lockedVars;
    Assignment bestAssignment = new Assignment();
    int bestNumberOfUnsatisfiedClauses = Integer.MAX_VALUE;
    @Override
    public Assignment solve(EfficientInstance instance) {
        double startTime=Stopwatch.getInstance().elapsedSeconds();
        EfficientAbstractSolver eemoceSolver = new EfficientEeMoceSolver();
        lockedVars = new Assignment();
        int totalNumOfExecutions = 0;
        int totalNumOfLocks = 0;
        while (bestNumberOfUnsatisfiedClauses!=0 && Stopwatch.getInstance().elapsedSeconds()-startTime<timeLimit) {
//            if (totalNumOfExecutions>=numberOfIterationsPerVariable) break;
            lockedVars.getAssignment().clear();
            residualInstance = new EfficientInstance(instance);
            int unsatisfiedByLocked = 0;
            boolean prioritiesWereChanged = true;
            int numOfExecutionsInCurrentMultyPass = 0;
            saverMinusDeserterLevels = new double[residualInstance.variables.length];
            valueInFirstCycle = new boolean[residualInstance.variables.length];
            firstExecutionInCycle = true;
            while (bestNumberOfUnsatisfiedClauses!=0 && prioritiesWereChanged && Stopwatch.getInstance().elapsedSeconds()-startTime<timeLimit/*&&totalNumOfExecutions<numberOfIterationsPerVariable*/) {
//                if (totalNumOfExecutions>=numberOfIterationsPerVariable) break;
                Assignment currentAssignment = eemoceSolver.solve(residualInstance);
                numOfExecutionsInCurrentMultyPass++;
                totalNumOfExecutions++;
//                System.out.println(totalNumOfExecutions + " executions " + Stopwatch.getInstance().elapsedSeconds() +" sec");
                int numberOfUnsatisfiedClauses = eemoceSolver.getNumberOfUnsatisfied();
                if ((unsatisfiedByLocked+numberOfUnsatisfiedClauses) < bestNumberOfUnsatisfiedClauses) {
                    bestAssignment.getAssignment().putAll(currentAssignment.getAssignment());
                    bestAssignment.getAssignment().putAll(lockedVars.getAssignment());
                    bestNumberOfUnsatisfiedClauses = numberOfUnsatisfiedClauses+unsatisfiedByLocked;
                    if ( Common.recordingLevel== 6) System.out.println("found "+bestAssignment+" with " + bestNumberOfUnsatisfiedClauses);
                    if (numberOfUnsatisfiedClauses == 0) break;
                }
                if (clauseSaversConsensusCycleSize<numberOfIterationsPerVariable) {
                    updateSaversDeserters(currentAssignment);
                }
                firstExecutionInCycle= false;
                if (numOfExecutionsInCurrentMultyPass % clauseSaversConsensusCycleSize == 0) {
                    normalizeSaversDeserters();
//                    System.out.println("saversDeserters="+Arrays.toString(saverMinusDeserterLevels));
                    prioritiesWereChanged = updateCumulativeAssignment(currentAssignment);
                    totalNumOfLocks++;
                    if ( Common.recordingLevel== 6) System.out.println("locked="+lockedVars);
                    unsatisfiedByLocked+=residualInstance.residualizeAndGetNumOfUnsatisfied(lockedVars);
                    Arrays.fill(saverMinusDeserterLevels,0);
                    firstExecutionInCycle=true;
                }
            }
        }
//        System.out.println("numOfExecutions="+totalNumOfExecutions);
//        System.out.println("numOfLocks="+totalNumOfLocks);
        Logger.low("RunStat: numOfExecutions="+totalNumOfExecutions);
        Logger.low("RunStat: champTime="+(Stopwatch.getInstance().elapsedSeconds()-startTime));

        return bestAssignment;

    }


    public void updateSaversDeserters(Assignment assignment) {
        Map<Integer,Boolean>[] variables = residualInstance.variables;
        for (int i = 0; i< variables.length; i++){
            if (firstExecutionInCycle){
                valueInFirstCycle[i] = assignment.get(i);
            }
            else{
                if (valueInFirstCycle[i]!=assignment.get(i)){
                    saverMinusDeserterLevels[i] = MINIMAL_SAVING_LEVEL;
                }
            }
        }
        Map<Integer,Boolean>[] clauses = residualInstance.clauses;
        for (int i = 0; i< clauses.length; i++){
            updateSaversDeserters(assignment,clauses[i]);
        }
    }

    public void normalizeSaversDeserters(){
        for (int i = 0 ;i< saverMinusDeserterLevels.length; i++){
            if (!residualInstance.variables[i].isEmpty()) {
                saverMinusDeserterLevels[i] = saverMinusDeserterLevels[i] / (double) (residualInstance.variables[i].size() * clauseSaversConsensusCycleSize);
            }
        }
    }
    private void updateSaversDeserters(Assignment assignment, Map<Integer, Boolean> clause) {
        int saver = -1;
        for (Map.Entry<Integer,Boolean> variableEntry: clause.entrySet()){
            if (variableEntry.getValue() == assignment.get(variableEntry.getKey())) {
                if (saver!= -1) return;
                else saver = variableEntry.getKey();
            }
        }
        if (saver!=-1) {
            saverMinusDeserterLevels[saver]++;
//            System.out.println(saver + " saved " + clause);
        }
        else for (int deserter : clause.keySet()){
            saverMinusDeserterLevels[deserter]--;
//            System.out.println(deserter + " deserted " + clause);
        }
    }

    private boolean updateCumulativeAssignment(Assignment assignment) {
        // Initializing the new assignment
        Assignment newAssignment = new Assignment(lockedVars);

        // calculating the cutoff to get proportion
        int numOfUnlocked = saverMinusDeserterLevels.length-lockedVars.getAssignment().size();
        int topSavers = (int)Math.ceil(numOfUnlocked*clauseSaversTopFraction);

        double topSavingLevel = Math.max(DELTA,kthLargest(topSavers));
        // Updating the new assignment
        for (int i=0; i<saverMinusDeserterLevels.length; i++){
            if (saverMinusDeserterLevels[i]>=topSavingLevel){
                newAssignment.set(i,valueInFirstCycle[i]);
            }
        }

        // Verifying the new assigment and updating the cumulative assigment
        boolean assignmentChanged = !newAssignment.equals(lockedVars);
        lockedVars.putAll(newAssignment);
        return assignmentChanged;
    }

    private double kthLargest(int topSavers) {
        if (saverMinusDeserterLevels.length<100){
            return Arrays.stream(saverMinusDeserterLevels).boxed().sorted(Comparator.reverseOrder()).skip(Math.max(topSavers-1,0)).findFirst().orElse(0.0);
        }
        Arrays.fill(histogram,0);
        for (int i = 0; i < saverMinusDeserterLevels.length; i++) {
            histogram[Math.max(0,(int)(saverMinusDeserterLevels[i]*1000))]++;
        }
        double result = 0;
        int totalElements = 0;
        for (int i = 999; i>=0; i--){
            totalElements+=histogram[i];
            if(totalElements>=topSavers) return (i)/1000.0;
        }
        return 0;

    }

    @Override
    public int getNumberOfUnsatisfied() {
        return bestNumberOfUnsatisfiedClauses;
    }


}
