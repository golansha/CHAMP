package sat.EfficientImplementation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static sat.common.Common.random;

public class CclsMakeBreakCalculator {
    RandomElementExtractionSet<Integer> unsatisfiedClauses = new RandomElementExtractionSet<>();
    Map<Integer,Boolean>[] clauses, variables;
    Map<Integer,RandomElementExtractionSet<Integer>> makeBreakScoreLevels = new HashMap<>();
    int[] configurationChanged;
    int[] makeLevel, breakLevel;
    int[] numOfSatisfiers;
    Assignment assignment;
    int maxScore = Integer.MIN_VALUE;
    int minScore = Integer.MAX_VALUE;
    public CclsMakeBreakCalculator(EfficientInstance instance, Assignment assignment) {
        clauses = instance.clauses;
        variables = instance.variables;
        numOfSatisfiers = new int[clauses.length];
        makeLevel = new int[variables.length];
        breakLevel = new int[variables.length];
        configurationChanged = new int[variables.length];
        Arrays.fill(configurationChanged,1);
        int makeBreakThreshold = clauses.length>1000000?10000:1000;
        for (int i = -makeBreakThreshold; i<makeBreakThreshold;i++){
            makeBreakScoreLevels.put(i, new RandomElementExtractionSet());
        }
        calculateAllScores(assignment);
        this.assignment = assignment;
    }
    public int getRandomUnsatisfiedClause(){
        return unsatisfiedClauses.randomElement();
    }
    public void calculateAllScores(Assignment assignment){
        for (int i = 0; i<clauses.length; i++){
            for (Map.Entry<Integer,Boolean> variableEntry: clauses[i].entrySet()){
                if (assignment.get(variableEntry.getKey())==variableEntry.getValue()){
                    numOfSatisfiers[i]++;
                }
            }
            if (numOfSatisfiers[i]==0 && !clauses[i].isEmpty()){
                unsatisfiedClauses.add(i);
                for (int variable : clauses[i].keySet()){
                    makeLevel[variable]++;
                }
            }
            if (numOfSatisfiers[i] == 1){
                for (Map.Entry<Integer,Boolean> variableEntry: clauses[i].entrySet()){
                    if (assignment.get(variableEntry.getKey())==variableEntry.getValue()){
                        breakLevel[variableEntry.getKey()]++;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i< variables.length; i++){
            if (variables[i].isEmpty()) continue;
            addVariableToScoreLevels(i);
        }
    }
    void addVariableToScoreLevels(int i){
        int score = makeLevel[i]-breakLevel[i];
        if(makeLevel[i]>0) {
            maxScore=Math.max(score,maxScore);
            minScore=Math.min(score,minScore);
            makeBreakScoreLevels.get(score).add(i);
        }
    }
    void removeVariableFromScoreLevels(int i){
        int score = makeLevel[i]-breakLevel[i];
        makeBreakScoreLevels.get(score).remove(i);
        if (score == maxScore){
            while (makeBreakScoreLevels.get(maxScore).isEmpty()&& maxScore>minScore) maxScore--;
            if (makeBreakScoreLevels.get(maxScore).isEmpty()){
                maxScore=Integer.MIN_VALUE;
                minScore=Integer.MAX_VALUE;
            }
        }
        if (score == minScore){
            while (makeBreakScoreLevels.get(minScore).isEmpty()&& minScore<maxScore) minScore++;
            if (makeBreakScoreLevels.get(maxScore).isEmpty()){
                maxScore=Integer.MIN_VALUE;
                minScore=Integer.MAX_VALUE;
            }
        }
    }

    void addToMakeLevel(int varIndex, int toIncrement){
        removeVariableFromScoreLevels(varIndex);
        makeLevel[varIndex]+=toIncrement;
        addVariableToScoreLevels(varIndex);
    }
    void addToBreakLevel(int varIndex, int toIncrement){
        removeVariableFromScoreLevels(varIndex);
        breakLevel[varIndex]+=toIncrement;
        addVariableToScoreLevels(varIndex);
    }

    public void marginalizeScores(int variable, boolean value) {
        for (Map.Entry<Integer, Boolean> clauseEntry : variables[variable].entrySet()) {
            int clauseIndex = clauseEntry.getKey();
            if (value == clauseEntry.getValue()) {
                if (numOfSatisfiers[clauseIndex] == 0) {
                    for (Map.Entry<Integer, Boolean> neighborEntry : clauses[clauseIndex].entrySet()) {
                        int neighborIndex = neighborEntry.getKey();
                        addToMakeLevel(neighborIndex, -1);
                    }
                    addToBreakLevel(variable, 1);
                    unsatisfiedClauses.remove(clauseIndex);
                }
                if (numOfSatisfiers[clauseIndex] == 1) {
                    for (Map.Entry<Integer, Boolean> neighborEntry : clauses[clauseIndex].entrySet()) {
                        int neighborIndex = neighborEntry.getKey();
                        if (neighborEntry.getValue() == assignment.get(neighborIndex)) {
                            addToBreakLevel(neighborIndex, -1);
                        }
                    }
                }
                numOfSatisfiers[clauseIndex]++;
            }
            else {
                if (numOfSatisfiers[clauseIndex] == 1) {
                    for (Map.Entry<Integer, Boolean> neighborEntry : clauses[clauseIndex].entrySet()) {
                        int neighborIndex = neighborEntry.getKey();
                        addToMakeLevel(neighborIndex, 1);
                    }
                    addToBreakLevel(variable, -1);
                    unsatisfiedClauses.add(clauseIndex);
                }
                if (numOfSatisfiers[clauseIndex] == 2) {
                    for (Map.Entry<Integer, Boolean> neighborEntry : clauses[clauseIndex].entrySet()) {
                        int neighborIndex = neighborEntry.getKey();
                        if (neighborIndex==variable)continue;
                        if (neighborEntry.getValue() == assignment.get(neighborIndex)) {
                            addToBreakLevel(neighborIndex, 1);
                        }
                    }
                }
                numOfSatisfiers[clauseIndex]--;
            }

        }
        removeVariableFromScoreLevels(variable);
    }
    public int getRandomSatisfyingVariable(){
        Map<Integer,Boolean> unsatisfiedClause = clauses[unsatisfiedClauses.randomElement()];
        int varIndex = random.nextInt(unsatisfiedClause.size());
        int index =0;
        for (int i : unsatisfiedClause.keySet()) {
            if (index++==varIndex) return i;
        }
        throw new RuntimeException();
    }

    public int getBestScoreCcmpVariable() {
        if (makeBreakScoreLevels.get(maxScore).isEmpty()) return -1;
        return makeBreakScoreLevels.get(maxScore).randomElement();
    }
    public int getNumOfUnsatisfiedClauses(){
        return unsatisfiedClauses.size();
    }
/*    public void validateCalculator(){
        Set validatedUnsatisfied = new HashSet<>();
        int [] validatedMake = new int[variables.length];
        int [] validatedBreak = new int[variables.length];
        for (int i = 0; i<clauses.length; i++){
            if (clauses[i].entrySet().stream().noneMatch(e-> e.getValue() == assignment.get(e.getKey()))){
                validatedUnsatisfied.add(i);
                assert unsatisfiedClauses.contains(i);
                clauses[i].entrySet().stream().forEach(e -> validatedMake[e.getKey()]++);
            }
            if (clauses[i].entrySet().stream().filter(e-> e.getValue() == assignment.get(e.getKey())).count()==1){
                int breakVar = clauses[i].entrySet().stream().filter(e-> e.getValue() == assignment.get(e.getKey())).mapToInt(e->e.getKey()).findFirst().orElse(-1);
                validatedBreak[breakVar]++;
            }


        }
        assert validatedUnsatisfied.containsAll(unsatisfiedClauses.getContent());
        assert Arrays.compare(validatedMake,makeLevel)==0: Arrays.toString(validatedMake)+"!="+Arrays.toString(makeLevel);
        assert Arrays.compare(validatedBreak,breakLevel)==0 : Arrays.toString(validatedBreak)+"!="+Arrays.toString(breakLevel);
        int validatedMinScore = Integer.MAX_VALUE;
        int validatedMaxScore = Integer.MIN_VALUE;
        for (int i=-100; i<100; i++){
            if (!makeBreakScoreLevels.get(i).isEmpty()){
                validatedMaxScore = Math.max(validatedMaxScore,i);
                validatedMinScore = Math.min(validatedMinScore,i);
            }
            for (int variable : makeBreakScoreLevels.get(i).getContent()){
                assert makeLevel[variable]-breakLevel[variable]==i;
            }
        }
        assert validatedMaxScore == maxScore : validatedMaxScore+"!="+maxScore;
        assert validatedMinScore == minScore;

    }
    @Override
    public String toString(){
        return "assignment="+assignment+" unsatisfied="+unsatisfiedClauses.toString()+" make="+Arrays.toString(makeLevel)
            +" break="+Arrays.toString(breakLevel)+" numOfSatisfiers="+Arrays.toString(numOfSatisfiers)+", minMaxScore=("+minScore+","+maxScore+")";
    }*/
}
