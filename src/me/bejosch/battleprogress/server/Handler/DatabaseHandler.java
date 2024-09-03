package me.bejosch.battleprogress.server.Handler;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Funktions.Funktions;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class DatabaseHandler {

	//QUELLE: https://www.youtube.com/watch?v=B928IDexsGk
	
// CONNECT ===============================================================================================================
	public static void connect() {
		
		if(DatabaseData.con == null) {
			
			try {
				DatabaseData.con = DriverManager.getConnection(DatabaseData.url, DatabaseData.user, DatabaseData.pw);
				keepConnectionTimer();
				ConsoleOutput.printMessageInConsole("Succesfully connected to DB '"+DatabaseData.DBname+"'!", true);
			}catch(SQLException error) {
				ConsoleOutput.printMessageInConsole("Start aborted! Database connection needed for unit and upgrade stats...", true);
				ConsoleOutput.printMessageInConsole("Connecting to DB failed!", true);
//				ConsoleOutput.printMessageInConsole("Connecting to DB failed with following error:", true);
				error.printStackTrace();
//				System.exit(-1);
			}
			
		}else {
			ConsoleOutput.printMessageInConsole("Connection to DB was allready established as a connect was requested!", true);
		}
		
	}
	public static void disconnect() {
		
		if(DatabaseData.con != null) {
			
			try {
				DatabaseData.con.close();
				ConsoleOutput.printMessageInConsole("Succesfully disconnected from DB '"+DatabaseData.DBname+"'!", true);
			}catch (SQLException error) {
				ConsoleOutput.printMessageInConsole("Disconnecting from DB failed with following error:", true);
				error.printStackTrace();
			}
			
		}else {
			ConsoleOutput.printMessageInConsole("No connection to DB established as a disconnect was requested!", true);
		}
		
	}
	
	public static String makeStringDBSave(String input) {
		
		String[] badParts = {";", "(", ")", "{", "}", "*", "--", "=", "\"","\'", "\\"};
		String replacment = "#";
		
		for(String badPart : badParts) {
			input = input.replace(badPart, replacment);
		}
		
		return input;
		
	}
	
	private static void keepConnectionTimer() {
		//START PING TIMER TO PREVENT BROKEN PIPE TIMEOUT
		DatabaseData.keepConnectionTimer = new Timer();
		DatabaseData.keepConnectionTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					String query = "SELECT "+"*"+" FROM "+DatabaseData.tabellName_profile+" where "+"Name"+"='"+"FreeID"+"'";
					PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ResultSet rs = stmt.executeQuery();
					rs.close();
				} catch (SQLException error) {
					ConsoleOutput.printMessageInConsole("Pinging the DB failed!", true);
					//error.printStackTrace();
				}
			}
		}, 0, 1000*60*5);
		ConsoleOutput.printMessageInConsole("KeepConnectionTimer started for DB '"+DatabaseData.DBname+"'!", true);
	}
	
// SELECT / GET ===============================================================================================================
	public static String selectString(String tabelle, String target, String keyName, String key) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			String result = rs.getString(target);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			//error.printStackTrace(); //MANCHMAL ABSICHTILICHE FEHLER ABFRAGEN ZUM TESTEN
			return null;
		}
		
	}
	public static String selectString(String tabelle, String target, String keyName, String key, String keyName2, String key2) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"' AND "+keyName2+"='"+key2+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			String result = rs.getString(target);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			//error.printStackTrace(); //MANCHMAL ABSICHTILICHE FEHLER ABFRAGEN ZUM TESTEN
			return null;
		}
		
	}
	public static int selectInt(String tabelle, String target, String keyName, String key) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			int result = rs.getInt(target);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			//error.printStackTrace();
			return -1;
		}
		
	}
	public static int selectInt(String tabelle, String target, String keyName, String key, String keyName2, String key2) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"' AND "+keyName2+"='"+key2+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			int result = rs.getInt(target);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			//error.printStackTrace();
			return -1;
		}
		
	}
	public static double selectDouble(String tabelle, String target, String keyName, String key) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			double result = rs.getDouble(target);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			//error.printStackTrace();
			return -1.0;
		}
		
	}
	public static double selectDouble(String tabelle, String target, String keyName, String key, String keyName2, String key2) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"' AND "+keyName2+"='"+key2+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			double result = rs.getDouble(target);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			//error.printStackTrace();
			return -1.0;
		}
		
	}
	
	public static List<Integer> getAllWhereEqual_Int(String tabelle, String target, String keyName, String key) {
		
		try {
			String query = "SELECT "+target+" FROM "+tabelle+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			
			List<Integer> result = new ArrayList<Integer>();
			
			try {
				do {
					result.add(rs.getInt(target));
				}while(rs.next());
			}catch(SQLException error) {
				//EMPTY RESULT SET
			}
			
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException error) {
			error.printStackTrace();
			return null;
		}
		
	}
	
// CREATE / INSERT ===============================================================================================================
	
	public static void insertNewPlayer(String tabelle, String name, String password) {
		
		try {
			int id = ProfileHandler.getNextFreePlayerID(); //GET CURRENT NEXT FREE ID
			ProfileHandler.useFreePlayerID(); //INCREASE FOR NEW NUMBER
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europa/Berlin"));
			String date = Funktions.doubleWriteNumber(cal.get(Calendar.DAY_OF_MONTH))+"_"+Funktions.doubleWriteNumber(cal.get(Calendar.MONTH))+"_"+Funktions.doubleWriteNumber(cal.get(Calendar.YEAR));
			String query = "INSERT INTO "+tabelle+" (Name,ID,Datum,Password) VALUES ('"+name+"','"+id+"','"+date+"','"+password+"')";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	
	/**
	 * Insert data
	 * @param tabelle - Table name
	 * @param vars - The variables - Like this: (Name,ID,Datum,Password)
	 * @param values - The values - Like this: ('BEJOSCH','1234','01_02_2000','abcba')
	 */
	public static void insertData(String tabelle, String vars, String values) {
		
		try {
			String query = "INSERT INTO "+tabelle+" "+vars+" VALUES "+values;
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	
// UPDATE ===============================================================================================================
	
	public static void updateString(String tabelle, String target, String value, String keyName, String key) {
		
		try {
			String query = "UPDATE "+tabelle+" SET "+target+"="+value+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	public static void updateInt(String tabelle, String target, int value, String keyName, String key) {
		
		try {
			String query = "UPDATE "+tabelle+" SET "+target+"="+value+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	public static void updateDouble(String tabelle, String target, double value, String keyName, String key) {
	
		try {
			String query = "UPDATE "+tabelle+" SET "+target+"="+value+" where "+keyName+"='"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	
// DROP / REMOVE ===============================================================================================================
	
	public static void deleteData(String tabelle, String keyName, String key) {
		
		try {
			String query = "DELETE FROM "+tabelle+" WHERE "+keyName+" = '"+key+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	public static void deleteData(String tabelle, String keyName1, String key1, String keyName2, String key2) {
		
		try {
			String query = "DELETE FROM "+tabelle+" WHERE "+keyName1+" = '"+key1+"' AND "+keyName2+" = '"+key2+"'";
			PreparedStatement stmt = DatabaseData.con.prepareStatement(query);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException error) {
			error.printStackTrace();
		}
		
	}
	
}


