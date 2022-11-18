package sat.EfficientImplementation;

import sat.common.Common;

import static sat.EfficientImplementation.EfficientInstance.DEFAULT_ASSIGNMENT_VALUE;

public class EfficientEeMoceSolver extends EfficientAbstractSolver{
    EfficientInstance instance;
    EeMoceGainsCalculator calculator;
    Assignment assignment = new Assignment();

    @Override
    public Assignment solve(EfficientInstance instance) {
        this.instance = instance;
        assignment.getAssignment().clear();
        if (calculator == null) calculator = new EeMoceGainsCalculator(instance);
        else calculator.reset(instance);
        if ( Common.recordingLevel== 6)System.out.println(instance);
        if ( Common.recordingLevel== 6)System.out.println(calculator);

        for (int iterationNumber = 0; calculator.numOfClauses!=0; iterationNumber++) {
//            double bestGain = instance.validateInstanceAndGetBestGain();
            int bestVariable = calculator.getPure();
            if (bestVariable != -1) { calculator.removeVariable(bestVariable);}
            if (bestVariable == -1) {
                bestVariable = calculator.getBestVariable();
//                assert bestGain == Math.abs(calculator.gains[bestVariable]) :"best gain is "+bestGain+" but got a variable " +bestVariable+" with gain "+calculator.gains[bestVariable];
            }
            boolean value = calculator.gains[bestVariable]>0;
            calculator.marginalizeGains(bestVariable,value);
            assignment.set(bestVariable,value);
//            instance.residualize(bestVariable, value);
//            System.out.println(bestVariable+"->"+value+"; ");
//            System.out.println(instance);
//            System.out.println(calculator);
//            assert calculator.numOfClauses == instance.numOfClauses;
//            assert calculator.numOfUnsatisfiedClauses == instance.numOfUnsatisfiedClauses;
//            assert Arrays.equals(calculator.numOfNegativeClauses,instance.numOfNegativeClauses);
//            assert Arrays.equals(calculator.numOfPositiveClauses,instance.numOfPositiveClauses);
//            assert calculator.pures.equals(instance.pures) : calculator.pures +"!="+instance.pures;

        }
        while (!calculator.gainLevels.isEmpty()){
            int bestVariable = calculator.getBestVariable();
            calculator.marginalizeGains(bestVariable,DEFAULT_ASSIGNMENT_VALUE);
            assignment.set(bestVariable,DEFAULT_ASSIGNMENT_VALUE);

        }
        if ( Common.recordingLevel== 6) System.out.println("The Assignment is "+assignment+"\nThe instance is"+instance+"\n");
        return assignment;
    }

    public int getNumberOfUnsatisfied(){
        return calculator.numOfUnsatisfiedClauses;
    }


}
