package sat.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Stopwatch {
	private static final double NANO = 1.0 / 1000000000.0;
	private ThreadMXBean threadMXBean = null;
	private long start;
	private static Stopwatch theInstance = new Stopwatch();
	public static Stopwatch getInstance(){
		return theInstance;
	}
	public Stopwatch() {
		threadMXBean = ManagementFactory.getThreadMXBean();
		start = threadMXBean.getCurrentThreadCpuTime();
	}

	public double elapsedSeconds() {
		long now = threadMXBean.getCurrentThreadCpuTime();
		return (now - start) * NANO;
	}
	public double elapsedNanoSeconds() {
		long now = threadMXBean.getCurrentThreadCpuTime();
		return (now - start);
	}
}
