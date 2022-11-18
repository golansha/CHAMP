package sat.EfficientImplementation;

import sat.common.Common;
import sat.common.Logger;
import sat.common.Stopwatch;

import static sat.common.Common.*;

public class EfficientChampCclsSolver extends EfficientAbstractSolver {
    EfficientInstance instance;
    EfficientAbstractSolver cclsSolver;

    @Override
    public Assignment solve(EfficientInstance instance) {
        double startTime = Stopwatch.getInstance().elapsedSeconds();
        this.instance = instance;
        timeLimit = calculateTime(instance);
        Assignment initialAssignment;
        if (algorithm.equalsIgnoreCase("randccls")){
            double startTimeRandomAssignment =Stopwatch.getInstance().elapsedSeconds();
            initialAssignment = instance.getRandomAssignment();
            double endTimeRandomAssignment = Stopwatch.getInstance().elapsedSeconds();
            timeLimit-=(endTimeRandomAssignment-startTimeRandomAssignment);
 //           System.out.println("random assignment: "+initialAssignment);
        }
        else{
            double champTimeLimit = multiPassBudget/100.0*timeLimit;
            double cclsTimeLimit = timeLimit - champTimeLimit;
            timeLimit = champTimeLimit;
            EfficientAbstractSolver champSolver = new EfficientChampSolver();
            initialAssignment = champSolver.solve(instance);
//            System.out.println("champ assignment: "+initialAssignment);
            timeLimit=cclsTimeLimit;
        }
        cclsSolver = new EfficientCclsSolver(initialAssignment);
        Assignment result = cclsSolver.solve(instance);
        double endTime = Stopwatch.getInstance().elapsedSeconds();
        Logger.low("RunStat: time="+(endTime-startTime));
        Logger.low("properties=" + Common.theInstance()
            + ", numberOfVariables=" + instance.variables.length
            + ", numberOfClauses=" + instance.clauses.length
            + ", numberOfSatisfiedClauses=" + (instance.clauses.length - getNumberOfUnsatisfied())
            + ", numberOfUnsatisfiedClauses=" + getNumberOfUnsatisfied()
            + ", instanceName=" + instance.name);
        return result;

    }


    private Assignment runChamp(EfficientInstance instance) {
        return null;
    }

    private double calculateTime(EfficientInstance instance) {
        return Math.max(1.0,1.0*instance.maximalClauseLength*instance.clauses.length*8/100000);
    }

    @Override
    public int getNumberOfUnsatisfied() {
        return cclsSolver.getNumberOfUnsatisfied();
    }
}