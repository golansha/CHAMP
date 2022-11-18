package sat.EfficientImplementation;

import sat.common.Common;
import sat.common.Logger;
import sat.common.Stopwatch;

import static sat.common.Common.numberOfIterationsPerVariable;
import static sat.common.Common.timeLimit;

public class EfficientCclsSolver extends EfficientAbstractSolver {
    EfficientInstance instance;
    CclsMakeBreakCalculator calculator;
    Assignment bestAssignment;
    int maxNumOfIterations;
    int bestAssignmentScore;
    double startTime;
    public EfficientCclsSolver(Assignment assignment){
        bestAssignment = assignment;
    }
    @Override
    public Assignment solve(EfficientInstance instance) {
        startTime = Stopwatch.getInstance().elapsedSeconds();
        this.instance = instance;
//        System.out.println(instance);
        int varLen = instance.variables.length;
        int minimalIntervalBetweenSaves = varLen<2000?0:1000;
        Assignment assignment = new Assignment(bestAssignment);
        calculator = new CclsMakeBreakCalculator(instance,assignment);
        bestAssignmentScore = calculator.getNumOfUnsatisfiedClauses();
        maxNumOfIterations = getMaxNumOfIterations(instance);
        int flipsSinceLastSave = 0;
        int iterationNumber;
        for (iterationNumber = 0; !solverFinished(iterationNumber); iterationNumber++) {
//            System.out.println("Instance "+instance);
//            System.out.println("Calculator "+calculator);
//            calculator.validateCalculator();
            int bestVariable;
            if (Common.random.nextDouble()<Common.probabilityForStochasticIteration){
                bestVariable = calculator.getRandomSatisfyingVariable();
            }
            else {
                bestVariable = calculator.getBestScoreCcmpVariable();
                if (bestVariable == -1){
                    bestVariable = calculator.getRandomSatisfyingVariable();
                }
            }
            boolean value = !assignment.get(bestVariable);
            calculator.marginalizeScores(bestVariable, value);
            assignment.set(bestVariable, value);
//            System.out.println("flipping "+bestVariable);
            int currentAssignmentScore = calculator.getNumOfUnsatisfiedClauses();
            if (++flipsSinceLastSave>minimalIntervalBetweenSaves && currentAssignmentScore < bestAssignmentScore){
                if (bestAssignmentScore-currentAssignmentScore==1){
                    minimalIntervalBetweenSaves=0;
                }
                bestAssignmentScore = currentAssignmentScore;
                bestAssignment = new Assignment(assignment);
                flipsSinceLastSave = 0;
            }

        }
//        System.out.println("ccls assignment: "+bestAssignment);
        Logger.low("RunStat: cclsFlipsPerVar="+(iterationNumber/instance.variables.length));
        Logger.low("RunStat: cclsTime="+(Stopwatch.getInstance().elapsedSeconds()-startTime));
        return bestAssignment;
    }

    protected boolean solverFinished(int iterationNumber){
        return calculator.getNumOfUnsatisfiedClauses()==0 ||
            (iterationNumber % 25 == 0 && Stopwatch.getInstance().elapsedSeconds()-startTime> timeLimit);
    }

    protected int getMaxNumOfIterations(EfficientInstance instance) {
        if (numberOfIterationsPerVariable == -1) {
            return Integer.MAX_VALUE;
        } else {
            return numberOfIterationsPerVariable * instance.variables.length;
        }
    }
    public int getNumberOfUnsatisfied(){
        return bestAssignmentScore;
    }


}