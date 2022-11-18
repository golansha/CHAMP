package sat.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// TODO use enum for levels
public class Logger {
	public static final int NONE = 0;
	public static final int LOWEST = 1;
	public static final int LOWER = 2;
	public static final int LOW = 3;
	public static final int MEDIUM = 4;
	public static final int HIGH = 5;
	public static final int FULL = 6;

	private static int recordingLevel = NONE;
	private static int loggingLevel = NONE;

	private static final String DEFAULT_RECORDER_DESTINATION = System.getenv("MAXSAT_HOME") + "/Outputs/statistics2.log";
	private static PrintWriter recorder = null;

	private static void validateLevel(int level) {
		if (level < 0 || level > FULL) {
			throw new IllegalArgumentException("The provided level is not valid. Expected: integer between " + NONE
					+ "to " + FULL + " (including). Provided: " + level + ".");
		}
	}

	public static String getLevelName(int level) {
		if (loggingLevel == NONE) {
			return "NONE";
		} else if (loggingLevel == LOWEST) {
			return "LOWEST";
		} else if (loggingLevel == LOWER) {
			return "LOWER";
		} else if (loggingLevel == LOW) {
			return "LOW";
		} else if (loggingLevel == MEDIUM) {
			return "MEDIUM";
		} else if (loggingLevel == HIGH) {
			return "HIGH";
		} else if (loggingLevel == FULL) {
			return "FULL";
		} else {
			throw new InternalError("The level is set to an erroneous value: " + loggingLevel + ".");
		}
	}

	// NOTE if used, the recorder must be closed at the end of usage! To flush
	// and close the BufferedWriter.
	public static void openRecorder(int recordingLevel) throws IOException {
		openRecorder(recordingLevel, DEFAULT_RECORDER_DESTINATION);
	}

	// NOTE if used, the recorder must be closed at the end of usage! To flush
	// and close the BufferedWriter.
	public static void openRecorder(int recordingLevel, String destinationFilePathAndName) throws IOException {
		validateLevel(recordingLevel);
		Logger.recordingLevel = recordingLevel;
		recorder = new PrintWriter(new BufferedWriter(new FileWriter(destinationFilePathAndName, true))); // true
																											// means
																											// append
	}

	// NOTE if used, the recorder must be closed at the end of usage! To flush
	// and close the BufferedWriter.
	public static void closeRecorder() throws IOException {
		Logger.recordingLevel = NONE;
		recorder.close();
		recorder = null;
	}

	public static String getRecordingLevelName() {
		return getLevelName(recordingLevel);
	}

	public static String getLoggingLevelName() {
		return getLevelName(loggingLevel);
	}

	public static void setLoggingLevel(int loggingLevel) {
		validateLevel(loggingLevel);
		Logger.loggingLevel = loggingLevel;
		Logger.recordingLevel = loggingLevel;
	}

	private static void log(int level, Object message) {
		String msg = "";

		// lazy evaluation due to possible huge objects/messages
		if (loggingLevel >= level || recordingLevel >= level)
			if (message != null)
				msg = message.toString();
		if (loggingLevel >= level)
			System.out.println(msg);
		if (recordingLevel >= level)
			recorder.println(msg);
	}

	public static void lowest(Object message) {
		log(LOWEST, message);
	}

	public static void lower(Object message) {
		log(LOWER, message);
	}

	public static void low(Object message) {
		log(LOW, message);
	}

	public static void medium(Object message) {
		log(MEDIUM, message);
	}

	public static void high(Object message) {
		log(HIGH, message);
	}

	public static void full(Object message) {
		log(FULL, message);
	}

	public static boolean lowest() {
		return loggingLevel >= LOWEST;
	}

	public static boolean lower() {
		return loggingLevel >= LOWER;
	}

	public static boolean low() {
		return loggingLevel >= LOW;
	}

	public static boolean medium() {
		return loggingLevel >= MEDIUM;
	}

	public static boolean high() {
		return loggingLevel >= HIGH;
	}

	public static boolean full() {
		return loggingLevel >= FULL;
	}

	public static void track(String trackingMessage, int trackingCycle, int trackingPeriod) {
		if (trackingCycle % trackingPeriod == 0) {
			System.out.println(trackingMessage + trackingCycle);
		}
	}
}
