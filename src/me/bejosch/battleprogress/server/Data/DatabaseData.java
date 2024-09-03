package me.bejosch.battleprogress.server.Data;

import java.sql.Connection;
import java.util.Timer;

public class DatabaseData {

	public static final String DBname = "BattleProgress";
	public static final String paras = "serverTimezone=UTC&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false";
	public static final String url = "jdbc:mysql://localhost:3306/"+DBname+"?"+paras;
	public static final String user = "battleprogress";
	public static final String pw = "dJwos8!7h29hI-is34f";
	
	public static Connection con = null;
	public static Timer keepConnectionTimer = null;
	
	public static final String tabellName_troupLand = "Unit_Troup_Land";
	public static final String tabellName_troupAir = "Unit_Troup_Air";
	public static final String tabellName_buildings = "Unit_Buildings";
	
	public static final String tabellName_description = "Descriptions";
	public static final String tabellName_dictionaryInfo = "DictionaryInfo";
	public static final String tabellName_language = "Language";
	
	public static final String tabellName_upgrades = "Upgrades";
	public static final String tabellName_profile = "Profile";
	public static final String tabellName_stats = "Stats";
	public static final String tabellName_friendlist = "Friends";
	public static final String tabellName_friendRequests = "FriendRequests";
	
	public static final String language_en = "EN";
	public static final String language_de = "DE";
	
}
