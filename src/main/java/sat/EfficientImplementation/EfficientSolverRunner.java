package sat.EfficientImplementation;

import sat.common.Common;
import sat.common.Logger;
import sat.common.Stopwatch;

import java.io.IOException;

public class EfficientSolverRunner {
    EfficientAbstractSolver solver;
    String instancePath;
    String recorderDestination;
    int recordingLevel;
    EfficientInstance instance;
    public static void main(String[] args) throws IOException
    {
        // Print heap related information
//        long maxBytes = Runtime.getRuntime().maxMemory();
//        System.out.println("Max memory: " + maxBytes / 1024 / 1024 + " MB");
//        long totalBytes = Runtime.getRuntime().totalMemory();
//        System.out.println("Total memory: " + totalBytes / 1024 / 1024 + " MB");

        Stopwatch.getInstance(); // NOTE: this should be the first line -- create stopwatch
        System.out.println("running java "+args[0]+" "+args[1]+" "+args[2]);
        Common.theInstance(args[0]);
        Logger.openRecorder(Common.recordingLevel, args[2]);
        Logger.setLoggingLevel(Common.recordingLevel);
        EfficientSolverRunner solverRunner = new EfficientSolverRunner(args);
        Assignment solution = solverRunner.solve();
        Logger.closeRecorder();
        System.exit(0);
    }
    public EfficientSolverRunner(String[] args) throws IOException {
        assert args.length == 3 :"Usage: SolverRunner <properties> <instance> <statistics log file name>";
        instancePath = args[1];
        String solverName = Common.algorithm;
        recorderDestination = args[2];
        recordingLevel = Common.recordingLevel;
        solver = new EfficientChampCclsSolver();
        instance = new EfficientInstance(instancePath);
    }
    public Assignment solve() throws IOException {
        Assignment result = solver.solve(instance);
        return result;
    }

    public EfficientInstance getInstance(){
        return instance;
    }

}