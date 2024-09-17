package me.bejosch.battleprogress.server.Data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Objects.ServerGame;

public class ServerGameData {

	//HQ SPAWNS
	public static final int mapWidth = 60;
	public static final int mapHight = 60;
	private static final int HQwallDistance = 14;
	
	public static final Point HQ_1_1vs1 = new Point(HQwallDistance, HQwallDistance), HQ_2_1vs1 = new Point(mapWidth-HQwallDistance+1, mapHight-HQwallDistance+1);
							  //TEAM 1 - Links														//TEAM 2 - Rechts
	public static final Point HQ_1_2vs2 = new Point(HQwallDistance, HQwallDistance), 			HQ_3_2vs2 = new Point(mapWidth-HQwallDistance+1, mapHight-HQwallDistance+1), 
							  HQ_2_2vs2 = new Point(HQwallDistance, mapHight-HQwallDistance+1) , 	HQ_4_2vs2 = new Point(mapWidth-HQwallDistance+1, HQwallDistance);
	
	
	public static final int gameIdLength = 8;
	public static final int gameAcceptWaitingDuration = 10; //SEK
	
	public static List<ServerGame> runningGames = new ArrayList<ServerGame>();
	public static List<ServerGame> oldFinishedGames = new ArrayList<ServerGame>(); //KEPT FOR REVIEW - POSSIBE?
	
	public static final int reconnectTimeSec = 5*60;
	
}
