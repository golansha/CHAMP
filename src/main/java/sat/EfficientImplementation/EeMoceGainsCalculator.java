package sat.EfficientImplementation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class EeMoceGainsCalculator {
    double[] pows;
    double[] gains;
    Map<Integer,Boolean>[] clauses, variables;
    GainLevels gainLevels;
    int[]clauseLengths;
    boolean[] clauseExists;
    boolean[] variableExists;
    int numOfClauses;
    int numOfUnsatisfiedClauses = 0;
    int [] numOfPositiveClauses, numOfNegativeClauses;
    Set<Integer> pures = new HashSet<>();

    public EeMoceGainsCalculator(EfficientInstance instance){
        pows = IntStream.range(0, instance.maximalClauseLength+1).mapToDouble(i -> Math.pow(2,-i)).toArray();
        clauses = instance.clauses;
        variables = instance.variables;
        numOfClauses = 0;
        variableExists = new boolean[variables.length];
        clauseLengths = new int[clauses.length];
        clauseExists = new boolean[clauses.length];
        gains = new double[variables.length];
        gainLevels = new GainLevels(instance.maximalClauseLength);
        numOfNegativeClauses = new int[variables.length];
        numOfPositiveClauses = new int[variables.length];
        calculateAllGains();
    }
    public void calculateAllGains(){
        for (int i = 0; i < clauses.length; i++) {
            clauseLengths[i]=clauses[i].size();
            clauseExists[i] = clauseLengths[i] != 0;
            if (clauseExists[i]) numOfClauses++;
        }
        for (int i = 0; i<variables.length; i++){
            variableExists[i]=!variables[i].isEmpty();
            if (variables[i].isEmpty()) continue;
            for (Map.Entry<Integer,Boolean> clauseEntry: variables[i].entrySet()){
                assert(clauseLengths[clauseEntry.getKey()]==clauses[clauseEntry.getKey()].size());
                (clauseEntry.getValue()?numOfPositiveClauses:numOfNegativeClauses)[i]++;
                gains[i] += pows[clauseLengths[clauseEntry.getKey()]]*(clauseEntry.getValue()?1:-1);
            }
            gainLevels.add(i,Math.abs(gains[i]));
            if (isPure(i)) pures.add(i);
        }
    }
    public void marginalizeGains(int variable, boolean value){
        for (Map.Entry<Integer,Boolean> clauseEntry: variables[variable].entrySet()){
            int clauseIndex = clauseEntry.getKey();
            if (!clauseExists[clauseIndex]) continue;
            boolean clauseSatisfied = value == clauseEntry.getValue();
            int varSatisfactionCoefficient = (clauseSatisfied)?-1:1;
            Map<Integer, Boolean> clause = clauses[clauseIndex];
            double clauseLengthCoefficient = pows[clauseLengths[clauseIndex]];
            for (Map.Entry<Integer,Boolean> neighborEntry: clause.entrySet()){
                int neighborIndex = neighborEntry.getKey();
                if (!variableExists[neighborIndex]) continue;
                if (neighborIndex == variable) continue;
                if (clauseSatisfied){
                    (neighborEntry.getValue()?numOfPositiveClauses:numOfNegativeClauses)[neighborIndex]--;
                    if (isPure(neighborIndex)) pures.add(neighborIndex);
                }
                int neighborSatisfactionCoefficient = neighborEntry.getValue()?1:-1;
                gainLevels.remove(neighborIndex,Math.abs(gains[neighborIndex]));
                gains[neighborIndex]+= clauseLengthCoefficient*varSatisfactionCoefficient*neighborSatisfactionCoefficient;
                gainLevels.add(neighborIndex,Math.abs(gains[neighborIndex]));
            }
            if (clauseSatisfied) {
                clauseExists[clauseIndex] = false;
                numOfClauses--;
            }
            else{
                clauseLengths[clauseIndex]--;
                if (clauseLengths[clauseIndex]==0){
                    numOfClauses--;
                    numOfUnsatisfiedClauses++;
                }
            }

        }
        gains[variable]=0;
        variableExists[variable]=false;
        numOfNegativeClauses[variable]=0;
        numOfPositiveClauses[variable]=0;
    }
    public int getBestVariable(){
        return gainLevels.pollFirst();
    }
    public void removeVariable(int variable){
        gainLevels.remove(variable,Math.abs(gains[variable]));
    }
    @Override
    public String toString(){
        return "gains="+Arrays.toString(gains)+"; levels="+gainLevels+"; "+ Arrays.toString(variableExists)+"; "+Arrays.toString(clauseExists);
    }

    public void reset(EfficientInstance instance) {
        numOfClauses = 0;
        numOfUnsatisfiedClauses = 0;
        Arrays.fill(clauseExists,true);
        Arrays.fill(variableExists,true);
        gainLevels.clear();
        pures.clear();
        clauses = instance.clauses;
        variables = instance.variables;
        calculateAllGains();

    }
    private boolean isPure(int i){
        return (numOfPositiveClauses[i]+numOfNegativeClauses[i]!=0) &&
            (numOfNegativeClauses[i] == 0 || numOfPositiveClauses[i] == 0);
    }
    public int getPure(){
        if (pures.isEmpty()) return -1;
        int pure = pures.iterator().next();
        pures.remove(pure);
        return pure;
    }


}
