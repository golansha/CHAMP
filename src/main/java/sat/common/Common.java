package sat.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public final class Common {
	private static final int DEFAULT_RECORDING_LEVEL = 3;
	private static final String DEFAULT_ALGORITHM = "EEMoce";
	private static final String DEFAULT_RMOCE_INPUT = "/io/stdout_parsed.csv";
	private static final boolean DEFAULT_VALIDATE = false;
	private static final boolean DEFAULT_OPTIMIZE_CRITICALS = false;
	private static final boolean DEFAULT_OPTIMIZE_LUCIDS = true;
	private static final boolean DEFAULT_OPTIMIZE_SEMI_LUCIDS = false;
	private static final boolean DEFAULT_KEEP_ASSIGNED_VARIABLES = false;
	private static final String DEFAULT_VARIABLE_TYPE = "Variable";
	private static final Set<String> ALGORITHMS_USING_GAINS = new HashSet<>(Arrays.asList("EEMoce", "Ccls"));
	private static final String DEFAULT_INSTANCE_GENERATOR_TYPE = "RandomInstanceGenerator";
	private static final double DEFAULT_BALANCE_FACTOR = 0.0;
	private static final double DEFAULT_SECONDARY_GAIN_WEIGHT = 0.0;
	private static final long DEFAULT_NUMBER_OF_ITERATIONS_PER_VARIABLE = -1;
	private static final long DEFAULT_TIME_LIMIT = Integer.MAX_VALUE;
	private static final Double DEFAULT_PROBABILITY_FOR_STOCHASTIC_ITERATION = 0.2;
	public static final String DEFAULT_POST_SOLUTION_CONFIGURATION = "None";
	public static final String DEFAULT_SECONDARY_SOLUTION_CONFIGURATION = "None";
	private static final int DEFAULT_SEED = -1;
	private static final String DEFAULT_INSTANCE_SIMPLIFIERS = "InstanceSimplifier";
	private static final int DEFAULT_NUM_ITERATIONS_BETWEEN_SIMPLIFICATIONS = Integer.MAX_VALUE;
	private static final boolean DEFAULT_OPTIMIZE_REDUNDANT_VARIABLES = false;
	private static final boolean DEFAULT_OPTIMIZE_CLAUSE_SAVER_VARIABLES = false;
	private static final boolean DEFAULT_RANDOM_TIE_BREAKING = true;
	private static final boolean DEFAULT_CONSTANT_HIDDEN_ASSIGNMENT = false;
	private static final int DEFAULT_MULTIPASS_BUDGET = 100;
	private static final int DEFAULT_MULTIPASS_SUB_BUDGET = 100;
	private static final double DEFAULT_CLAUSE_SAVERS_TOP_FRACTION = 1;
	private static final double DEFAULT_CLAUSE_SAVERS_MINIMAL_CONSENSUS_LEVEL = 0.0;
	private static final double DEFAULT_CLAUSE_SAVERS_MINIMAL_AVERAGE_LEVEL = 0.0;
	private static final int DEFAULT_CONSENSUS_CYCLE_SIZE = 1;
	private static final double DEFAULT_SEMI_SAVER_WEIGHT = 0;
    private static final boolean DEFAULT_CHAMP_PLUS = true;

	private static Common common;
	public static Common theInstance(String path){
		if (common==null){
			common = new Common(path);
		}
		return common;
	}
	// NOTE: For testing purposes. Consider before using in main code
	public static void resetInstance(){
		common = null;
	}
	public static Common theInstance(){
		return common;
	}
	private Common(String path){
		try {
			this.path = path;
			appProps = new Properties();
			appProps.load(new FileInputStream(System.getenv("MAXSAT_HOME") +"/Java/Moce/config/"+path +".properties"));
			recordingLevel = Integer.parseInt(appProps.getProperty("recordingLevel", DEFAULT_RECORDING_LEVEL+""));
			algorithm = appProps.getProperty("algorithm", DEFAULT_ALGORITHM);
			postSolutionConfiguration = appProps.getProperty("postSolutionConfiguration", DEFAULT_POST_SOLUTION_CONFIGURATION);
			secondarySolutionConfiguration = appProps.getProperty("secondarySolutionConfiguration", DEFAULT_SECONDARY_SOLUTION_CONFIGURATION);
			initUseVariableGains();
			remoceInput = System.getenv("MAXSAT_HOME") + appProps.getProperty("rmoceInput", DEFAULT_RMOCE_INPUT);
			validate = Boolean.parseBoolean(appProps.getProperty("validate", DEFAULT_VALIDATE+""));
			optimizeCriticals = Boolean.parseBoolean(appProps.getProperty("optimizeCriticals", DEFAULT_OPTIMIZE_CRITICALS+""));
			optimizeLucids =Boolean.parseBoolean(appProps.getProperty("optimizeLucids", DEFAULT_OPTIMIZE_LUCIDS+""));
			optimizeSemiLucids =Boolean.parseBoolean(appProps.getProperty("optimizeSemiLucids", DEFAULT_OPTIMIZE_SEMI_LUCIDS+""));
			variableType = appProps.getProperty("variableType", DEFAULT_VARIABLE_TYPE);
			instanceGeneratorType = appProps.getProperty("instanceGeneratorType", DEFAULT_INSTANCE_GENERATOR_TYPE);
			balanceFactor = Double.parseDouble(appProps.getProperty("balanceFactor", DEFAULT_BALANCE_FACTOR+""));
			secondaryGainWeight = Double.parseDouble(appProps.getProperty("secondaryGainWeight", DEFAULT_SECONDARY_GAIN_WEIGHT +""));
			keepAssignedVariables = Boolean.parseBoolean(appProps.getProperty("keepAssignedVariables", DEFAULT_KEEP_ASSIGNED_VARIABLES +""));
			numberOfIterationsPerVariable = Integer.parseInt(appProps.getProperty("numberOfIterationsPerVariable", DEFAULT_NUMBER_OF_ITERATIONS_PER_VARIABLE +""));
			timeLimit = Integer.parseInt(appProps.getProperty("timeLimit", DEFAULT_TIME_LIMIT +""));
			probabilityForStochasticIteration = Double.parseDouble(appProps.getProperty("probabilityForStochasticIteration", DEFAULT_PROBABILITY_FOR_STOCHASTIC_ITERATION+""));
			seed = Integer.parseInt(appProps.getProperty("seed", DEFAULT_SEED +""));
			instanceSimplifiers = appProps.getProperty("instanceSimplifiers", DEFAULT_INSTANCE_SIMPLIFIERS);
			numIterationsBetweenSimplifications = Integer.parseInt(appProps.getProperty("numIterationsBetweenSimplifications", DEFAULT_NUM_ITERATIONS_BETWEEN_SIMPLIFICATIONS +""));
			optimizeRedundantVariables = Boolean.parseBoolean(appProps.getProperty("optimizeRedundantVariables", DEFAULT_OPTIMIZE_REDUNDANT_VARIABLES +""));
			optimizeClauseSaverVariables = Boolean.parseBoolean(appProps.getProperty("optimizeClauseSaverVariables", DEFAULT_OPTIMIZE_CLAUSE_SAVER_VARIABLES +""));
			randomTieBreaking = Boolean.parseBoolean(appProps.getProperty("randomTieBreaking", DEFAULT_RANDOM_TIE_BREAKING +""));
			constantHiddenAssignment = Boolean.parseBoolean(appProps.getProperty("constantHiddenAssignment", DEFAULT_CONSTANT_HIDDEN_ASSIGNMENT +""));
			multiPassBudget = Integer.parseInt(appProps.getProperty("multiPassBudget", DEFAULT_MULTIPASS_BUDGET +""));
			multiPassSubBudget = Integer.parseInt(appProps.getProperty("multiPassSubBudget", DEFAULT_MULTIPASS_SUB_BUDGET +""));
			clauseSaversTopFraction = Double.parseDouble(appProps.getProperty("clauseSaversTopFraction", DEFAULT_CLAUSE_SAVERS_TOP_FRACTION +""));
			clauseSaversMinimalConsensusLevel = Double.parseDouble(appProps.getProperty("clauseSaversMinimalConsensusLevel", DEFAULT_CLAUSE_SAVERS_MINIMAL_CONSENSUS_LEVEL +""));
			clauseSaversMinimalAverageLevel = Double.parseDouble(appProps.getProperty("clauseSaversMinimalAverageLevel", DEFAULT_CLAUSE_SAVERS_MINIMAL_AVERAGE_LEVEL +""));
			clauseSaversConsensusCycleSize = Integer.parseInt(appProps.getProperty("clauseSaversConsensusCycleSize", DEFAULT_CONSENSUS_CYCLE_SIZE +""));
			semiSaverWeight = Double.parseDouble(appProps.getProperty("semiSaverWeight", DEFAULT_SEMI_SAVER_WEIGHT +""));
			champPlus = Boolean.parseBoolean(appProps.getProperty("champPlus", DEFAULT_CHAMP_PLUS +""));

			if (seed != DEFAULT_SEED){
				random = new Random(seed);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final double DELTA = 0.00000001;
	public static final int TOP_GAINS_TO_CONSIDER = 4;
	public static final double HALF = 0.5;
	public static final double HUNDRED = 100.0;
	public static final double THOUSAND = 1000.0;
	public static final double TEN_THOUSAND = 10000.0;
	public static final double HUNDRED_THOUSAND = 100000.0;


	private static final double HASH_MAP_CAPACITY_FACTOR = 0.75;
	public static double secondaryGainWeight;
	public static double balanceFactor;
	public static String instanceGeneratorType;
	public static int recordingLevel;
	public static String remoceInput;
	public static String algorithm;
	public static String postSolutionConfiguration;
	public static String secondarySolutionConfiguration;
	public static String variableType;
	public static boolean useVariableGains;
	public static boolean keepAssignedVariables;
	public static int numberOfIterationsPerVariable;
	public static double timeLimit;
	public static double probabilityForStochasticIteration;
	public static Random random = new Random();
	public static int seed;
	static Properties appProps;
	public static String instanceSimplifiers;
	public static int numIterationsBetweenSimplifications;
	public static String path;
	public static boolean optimizeRedundantVariables;
	public static boolean optimizeClauseSaverVariables;
	public static boolean constantHiddenAssignment;
	public static int multiPassBudget;
	public static int multiPassSubBudget;
	public static double clauseSaversTopFraction;
	public static double clauseSaversMinimalConsensusLevel;
	public static double clauseSaversMinimalAverageLevel;
	public static int clauseSaversConsensusCycleSize;
	public static double semiSaverWeight;
	public static boolean champPlus;

	public static final int calculateInitialHashMapCapacity(int expectedNumberOfPairs) {
		return (int) (expectedNumberOfPairs / HASH_MAP_CAPACITY_FACTOR + 2);
	}

	public static boolean validate;
	public static boolean optimizeCriticals;
	public static boolean optimizeLucids;
	public static boolean optimizeSemiLucids;

	public static boolean optimizeByRandomPerturbation = false;
	public static double probabilityToPerturbeBestTruthValue;
	public static double probabilityToPerturbeBestVariable;

	public static boolean optimizeByAprioriRedundantVariablePriortization = false;

	public static boolean randomTieBreaking = false;

	public static boolean optimizeByAddingWeights = false;

	public static boolean optimizeByStd = false;
	@Override
	public String toString(){
		HashMap<String, String> nonDefaultArgs = new HashMap<>();
		if (recordingLevel != DEFAULT_RECORDING_LEVEL)
			nonDefaultArgs.put("recordingLevel", recordingLevel+"");
		if (!algorithm.equalsIgnoreCase(DEFAULT_ALGORITHM))
			nonDefaultArgs.put("algorithm", algorithm);
		if (validate != (DEFAULT_VALIDATE))
			nonDefaultArgs.put("validate", validate+"");
		if (optimizeCriticals != DEFAULT_OPTIMIZE_CRITICALS)
			nonDefaultArgs.put("optimizeCriticals", optimizeCriticals+"");
		if (keepAssignedVariables != DEFAULT_KEEP_ASSIGNED_VARIABLES)
			nonDefaultArgs.put("keepAssignedVariables", keepAssignedVariables +"");
		if (optimizeLucids != DEFAULT_OPTIMIZE_LUCIDS)
			nonDefaultArgs.put("optimizeLucids", optimizeLucids+"");
		if (optimizeSemiLucids != DEFAULT_OPTIMIZE_SEMI_LUCIDS)
			nonDefaultArgs.put("optimizeSemiLucids", optimizeSemiLucids+"");
		if (optimizeRedundantVariables != DEFAULT_OPTIMIZE_REDUNDANT_VARIABLES)
			nonDefaultArgs.put("optimizeRedundantVariables", optimizeRedundantVariables +"");
		if (optimizeClauseSaverVariables != DEFAULT_OPTIMIZE_CLAUSE_SAVER_VARIABLES)
			nonDefaultArgs.put("optimizeClauseSaverVariables", optimizeClauseSaverVariables +"");
		if (!variableType.equalsIgnoreCase(DEFAULT_VARIABLE_TYPE))
			nonDefaultArgs.put("variableType", variableType);
		if (secondaryGainWeight != DEFAULT_SECONDARY_GAIN_WEIGHT)
			nonDefaultArgs.put("secondaryGainWeight", secondaryGainWeight +"");
		if (numberOfIterationsPerVariable != DEFAULT_NUMBER_OF_ITERATIONS_PER_VARIABLE)
			nonDefaultArgs.put("numberOfIterationsPerVariable", numberOfIterationsPerVariable +"");
		if (timeLimit != DEFAULT_TIME_LIMIT)
			nonDefaultArgs.put("timeLimit", timeLimit +"");
		if (probabilityForStochasticIteration != DEFAULT_PROBABILITY_FOR_STOCHASTIC_ITERATION)
			nonDefaultArgs.put("probabilityForStochasticIteration", probabilityForStochasticIteration+"");
		if(postSolutionConfiguration != DEFAULT_POST_SOLUTION_CONFIGURATION)
			nonDefaultArgs.put("postSolutionConfiguration", postSolutionConfiguration);
		if(secondarySolutionConfiguration != DEFAULT_SECONDARY_SOLUTION_CONFIGURATION)
			nonDefaultArgs.put("secondarySolutionConfiguration", secondarySolutionConfiguration);
		if(!instanceSimplifiers.equals(DEFAULT_INSTANCE_SIMPLIFIERS)){
			nonDefaultArgs.put("instanceSimplifiers", instanceSimplifiers);
		}
		if(numIterationsBetweenSimplifications!=DEFAULT_NUM_ITERATIONS_BETWEEN_SIMPLIFICATIONS){
			nonDefaultArgs.put("numIterationsBetweenSimplifications", numIterationsBetweenSimplifications+"");
		}
		if(randomTieBreaking != DEFAULT_RANDOM_TIE_BREAKING) {
			nonDefaultArgs.put("randomTieBreaking", randomTieBreaking +"");
		}
		if(constantHiddenAssignment != DEFAULT_CONSTANT_HIDDEN_ASSIGNMENT) {
			nonDefaultArgs.put("constantHiddenAssignment", constantHiddenAssignment +"");
		}
		if(multiPassBudget != DEFAULT_MULTIPASS_BUDGET) {
			nonDefaultArgs.put("multiPassBudget", multiPassBudget +"");
		}
		if(multiPassSubBudget != DEFAULT_MULTIPASS_SUB_BUDGET) {
			nonDefaultArgs.put("multiPassSubBudget", multiPassSubBudget +"");
		}
		if(clauseSaversTopFraction != DEFAULT_CLAUSE_SAVERS_TOP_FRACTION) {
			nonDefaultArgs.put("clauseSaversTopFraction", clauseSaversTopFraction +"");
		}
		if(clauseSaversMinimalConsensusLevel != DEFAULT_CLAUSE_SAVERS_MINIMAL_CONSENSUS_LEVEL) {
			nonDefaultArgs.put("clauseSaversMinimalConsensusLevel", clauseSaversMinimalConsensusLevel +"");
		}
		if(clauseSaversMinimalAverageLevel != DEFAULT_CLAUSE_SAVERS_MINIMAL_AVERAGE_LEVEL) {
			nonDefaultArgs.put("clauseSaversMinimalAverageLevel", clauseSaversMinimalAverageLevel +"");
		}
		if(clauseSaversConsensusCycleSize != DEFAULT_CONSENSUS_CYCLE_SIZE) {
			nonDefaultArgs.put("clauseSaversConsensusCycleSize", clauseSaversConsensusCycleSize +"");
		}
		if(semiSaverWeight != DEFAULT_SEMI_SAVER_WEIGHT) {
			nonDefaultArgs.put("semiSaverWeight", semiSaverWeight +"");
		}

		if(seed != DEFAULT_SEED)
			nonDefaultArgs.put("seed", seed+"");
		return path+"\t"+nonDefaultArgs.toString();
	}
	void initUseVariableGains(){
		useVariableGains = ALGORITHMS_USING_GAINS.contains(algorithm);
	}
}
