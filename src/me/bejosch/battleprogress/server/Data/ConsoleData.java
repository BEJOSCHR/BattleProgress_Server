package me.bejosch.battleprogress.server.Data;

import java.util.Timer;

public class ConsoleData {

	public static int focusedGameID = -1; // -1 -> KEINE , ELSE ONLY THE ID
	
	public static Timer ConsoleInputScanner = new Timer();

	public static int lastCreatedGameID = -1;
	
}
