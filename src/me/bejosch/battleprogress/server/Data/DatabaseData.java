package me.bejosch.battleprogress.server.Data;

import java.sql.Connection;
import java.util.Timer;

public class DatabaseData {

	public static final String DBname = "BattleProgress";
	public static final String url = "jdbc:mysql://localhost:3306/"+DBname;
	public static final String user = "BattleProgress";
	public static final String pw = "1991";
	
	public static Connection con = null;
	public static Timer keepConnectionTimer = null;
	
	public static final String tabellName_troupLand = "Unit_Troup_Land";
	public static final String tabellName_troupAir = "Unit_Troup_Air";
	public static final String tabellName_buildings = "Unit_Buildings";
	
	public static final String tabellName_upgrades = "Upgrades";
	public static final String tabellName_profile = "Profile";
	public static final String tabellName_stats = "Stats";
	public static final String tabellName_friendlist = "Friends";
	public static final String tabellName_friendRequests = "FriendRequests";
	
}
