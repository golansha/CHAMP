package sat.EfficientImplementation;

public abstract class EfficientAbstractSolver {
    public abstract Assignment solve(EfficientInstance instance);
    public abstract int getNumberOfUnsatisfied();

}
