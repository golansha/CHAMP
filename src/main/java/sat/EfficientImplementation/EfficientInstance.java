package sat.EfficientImplementation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static sat.common.Common.random;

public class EfficientInstance {
    // initialization related
    private static final String WEIGHTED_EXTENTION = ".wcnf";
    private static final String HEADER_PREFIX = "p";
    private static final String COMMENT_PREFIX = "c";
    private static final int POSITION_OF_NUMBER_OF_VARIABLES = 2;
    private static final int POSITION_OF_NUMBER_OF_CLAUSES = 3;
    int firstLiteralLocation;
    String name;
    public static final boolean DEFAULT_ASSIGNMENT_VALUE = false;
    Map<Integer,Boolean>[] clauses, variables;

    // metadata
    int maximalClauseLength=0;
    public EfficientInstance(String instancePath) throws IOException {
        name = instancePath;
        firstLiteralLocation = instancePath.endsWith(WEIGHTED_EXTENTION)?1:0;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(instancePath));
        initEmptyClauseVariableArrays(bufferedReader);
        initClausesAndVariables(bufferedReader);
    }
    public EfficientInstance(EfficientInstance other){
        this.name = other.name;
        int numOfClauses = other.clauses.length;
        int numOfVariables = other.variables.length;
        clauses = new Map[numOfClauses];
        for (int i = 0; i< numOfClauses; i++) clauses[i]=new HashMap<>(other.clauses[i]);
        variables = new Map[numOfVariables];
        for (int i = 0; i< numOfVariables; i++) variables[i]=new HashMap<>(other.variables[i]);
        maximalClauseLength = other.maximalClauseLength;
    }
    public void initEmptyClauseVariableArrays(BufferedReader bufferedReader) throws IOException {
        String line;
        for (line = bufferedReader.readLine(); !line.startsWith(HEADER_PREFIX); line = bufferedReader.readLine());
        String [] lineTokens = line.trim().split("\\s+");
        int numOfClauses = Integer.parseInt(lineTokens[POSITION_OF_NUMBER_OF_CLAUSES]);
        int numOfVariables = Integer.parseInt(lineTokens[POSITION_OF_NUMBER_OF_VARIABLES]);
        clauses = new Map[numOfClauses];
        for (int i = 0; i< clauses.length; i++) clauses[i]=new HashMap();
        variables = new Map[numOfVariables];
        for (int i = 0; i< variables.length; i++) variables[i]=new HashMap();
    }

    public void initClausesAndVariables(BufferedReader bufferedReader) throws IOException {
        for (int clauseNumber = 0; clauseNumber < clauses.length; clauseNumber++) {
            String line = bufferedReader.readLine();
            if (line.startsWith(COMMENT_PREFIX)) {clauseNumber--; continue;}
            parseClause(clauseNumber, line);
        }

        for (int i = 0; i<clauses.length; i++){
            maximalClauseLength = Math.max(maximalClauseLength, clauses[i].size());
        }
    }
    public void parseClause(int clauseNumber, String line) {
        String[] tokens = line.trim().split("\\s+");
        for (int literalNumber = firstLiteralLocation; literalNumber < tokens.length - 1; literalNumber++) {
            int literal = Integer.parseInt(tokens[literalNumber]);
            boolean sign = literal > 0;
            int variableNumber = Math.abs(literal) - 1; // NOTE we change the names of the variables!
            if (!addVariableToClause(clauseNumber,variableNumber, sign)) break;
        }
    }
    public boolean addVariableToClause(int clauseNumber, int variableNumber, boolean sign){
        Boolean oldSign = clauses[clauseNumber].put(variableNumber, sign);
        if (oldSign!=null&& oldSign!=sign){
            for(int neighbor : clauses[clauseNumber].keySet()) {
                variables[neighbor].remove(clauseNumber);
            }
            clauses[clauseNumber].clear();
            return false;
        }
        variables[variableNumber].put(clauseNumber, sign);
        return true;
    }
    public int residualizeAndGetNumOfUnsatisfied(int variable, boolean value){
        int result = 0;
        for (Map.Entry<Integer,Boolean> clauseEntry: variables[variable].entrySet()){
            boolean satisfied = (value == clauseEntry.getValue());
            int clauseIndex = clauseEntry.getKey();
            Map<Integer, Boolean> clause = clauses[clauseIndex];
            if (satisfied) {
                for (Map.Entry<Integer, Boolean> neighborEntry : clause.entrySet()) {
                    int neighborIndex = neighborEntry.getKey();
                    if (neighborIndex == variable) continue;
                    Map<Integer,Boolean> neighbor = variables[neighborIndex];
                    neighbor.remove(clauseIndex);
                    if (neighbor.isEmpty()) {
                        residualizeAndGetNumOfUnsatisfied(neighborIndex, DEFAULT_ASSIGNMENT_VALUE);
                    }
                }
                clause.clear();
            }
            else {
                clause.remove(variable);
                if (clause.isEmpty()) result++;
            }
        }
        variables[variable].clear();
        return result;
    }

    // returns numberOfUnsatisfied
    public int residualizeAndGetNumOfUnsatisfied(Assignment assignment){
        int result = 0;
        for (Map.Entry<Integer,Boolean> entry : assignment.getAssignment().entrySet()) {
            result+= residualizeAndGetNumOfUnsatisfied(entry.getKey(), entry.getValue());
        }
        return result;
    }
    public String toString(){
        return Arrays.toString(clauses);
    }
    public Assignment getRandomAssignment(){
        Assignment result = new Assignment();
        for (int i = 0; i<variables.length; i++) {
            result.set(i,random.nextDouble()<0.5);
        }
        return result;
    }
}
