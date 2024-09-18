package me.bejosch.battleprogress.server.Data;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Objects.ServerGame;

public class ServerGameData {

	public static final int gameIdLength = 8;
	public static final int gameAcceptWaitingDuration = 10; //SEK
	
	public static List<ServerGame> runningGames = new ArrayList<ServerGame>();
	public static List<ServerGame> oldFinishedGames = new ArrayList<ServerGame>(); //KEPT FOR REVIEW - POSSIBE?
	
	public static final int reconnectTimeSec = 5*60;
	
}
