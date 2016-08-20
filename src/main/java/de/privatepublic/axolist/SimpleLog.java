package de.privatepublic.axolist;

public class SimpleLog {
	
	public static void info(Object...msgs) {
		printLogMessage(false, msgs);
	}
	
	public static void error(Object...msgs) {
		printLogMessage(true, msgs);
	}
	
	private static void printLogMessage(boolean isError, Object...msgs) {
		StringBuilder sb = new StringBuilder("- ");
		boolean isFirst = true;
		for (Object msg:msgs) {
			if (!isFirst) { sb.append(' '); }
			sb.append(msg);
			isFirst = false;
		}
		if (isError) {
			System.err.println(sb);
		}
		else {
			System.out.println(sb);
		}
	}
	
}
