package me.bejosch.battleprogress.server.Main;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import me.bejosch.battleprogress.server.Data.ConsoleData;
import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Data.ServerDaten;
import me.bejosch.battleprogress.server.Data.ServerGameData;
import me.bejosch.battleprogress.server.Data.ServerGroupData;
import me.bejosch.battleprogress.server.Data.ServerPlayerData;
import me.bejosch.battleprogress.server.Data.StandardData;
import me.bejosch.battleprogress.server.Funktions.Funktions;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;
import me.bejosch.battleprogress.server.Handler.ServerGameHandler;
import me.bejosch.battleprogress.server.Handler.ServerPlayerHandler;
import me.bejosch.battleprogress.server.Handler.ServerQueueHandler;
import me.bejosch.battleprogress.server.Handler.UnitsStatsHandler;
import me.bejosch.battleprogress.server.Handler.UpgradeDataHandler;
import me.bejosch.battleprogress.server.Objects.ClientConnectionThread;
import me.bejosch.battleprogress.server.Objects.ServerGame;
import me.bejosch.battleprogress.server.Objects.ServerGroup;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;

public class ConsoleOutput {

	public static Timer ConsoleInputScanner = new Timer();
	
//==========================================================================================================
	/**
	 * Print simple Message in the console
	 * @param gameID - int - (-1) if not game related, else the gameID
	 * @param text - String - The message to print
	 * @param prefix - boolean - Enable/Disable Prefix
	 */
	public static void printMessageInConsole(String text, boolean prefix) { printMessageInConsole(-1, text, prefix); }
	public static void printMessageInConsole(int gameID, String text, boolean prefix) {
		
		if(gameID == ConsoleData.focusedGameID) {
			//RIGHT MODE
			if(prefix == true) {
				System.out.println(ServerDaten.MessagePrefix+text);
			}else {
				System.out.println(text);
			}
		}
		
	}
//==========================================================================================================
	/**
	 * Print an empty line
	 */
	public static void printBlankLineInConsole() { printBlankLineInConsole(-1); }
	public static void printBlankLineInConsole(int gameID) {
		if(gameID == ConsoleData.focusedGameID) {
			System.out.println("");
		}
	}
	
//==========================================================================================================
	/**
	 * Start Scanner which checks for new user console input
	 * @see userInputCommand
	 */
	public static void startUserInputScanner() {
		
		ConsoleInputScanner.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				
				try {
					@SuppressWarnings("resource") //DARF NICHT GESCHLOSSEN WERDEN!
					Scanner consoleInput = new Scanner(System.in);
					
					if(consoleInput.hasNextLine()) {
						
						if(ConsoleData.focusedGameID != -1) {
							//GAME SESSION
							int oldID = ConsoleData.focusedGameID;
							ConsoleData.focusedGameID = -1; //LEAVE SESSION
							printBlankLineInConsole();
							if(oldID == 0) {
								//PACKETS
								printMessageInConsole("Terminated packets session", true);
							}else {
								//GAME
								printMessageInConsole("Terminated game session for game ["+oldID+"]", true);
							}
							printBlankLineInConsole();
						}else {
							
							String input = consoleInput.nextLine();
							List<String> inputs = Funktions.ArrayFromPattern(input.split(" "));
							String keyWord = inputs.get(0);
							
							switch(keyWord) { 
							case "/help":
								sendCommand_help(inputs);
								break;
							case "/game":
								sendCommand_game(inputs);
								break;
							case "/games":
								sendCommand_games(inputs);
								break;
							case "/player":
								sendCommand_player(inputs);
								break;
							case "/players":
								sendCommand_players(inputs);
								break;
							case "/groups":
								sendCommand_groups(inputs);
								break;
							case "/packets":
								sendCommand_packets(inputs);
								break;
							case "/overview":
								sendCommand_overview(inputs);
								break;
							case "/update":
								sendCommand_update(inputs);
								break;
							case "/stop":
								sendCommand_stop(inputs);
								break;
							default:
								printMessageInConsole("Unknown input! Use '/help' for details...", true);
								break;
							}
							
						}
						
					}
				}catch(Exception error) {
					error.printStackTrace();
					printMessageInConsole("Can't handle this input! [Error]", true);
				}
				
			}
		}, 0, 60);
		
	}


	protected static void sendCommand_help(List<String> inputs) {
		
		printMessageInConsole("Choose one of these commands:", true);
		printMessageInConsole("'/game [id] ' - Join the game session so you see the log of the game", true);
		printMessageInConsole("'/games [quantity] ' - Shows the list of running games", true);
		printMessageInConsole("'/player [id|name] ' - Gives info about the player", true);
		printMessageInConsole("'/players [quantity] ' - Shows the list of online player", true);
		printMessageInConsole("'/groups [quantity] ' - Shows the list of active groups", true);
		printMessageInConsole("'/packets ' - Join the packet session so you see the traffic of packets", true);
		printMessageInConsole("'/overview ' - Gives a general overview about everything interesting", true);
		printMessageInConsole("'/update [units|upgrades] ' - Reloads the units or the upgrades from the DB", true);
		printMessageInConsole("'/stop ' - Stoppes the whole server", true);
		
	}
	
	protected static void sendCommand_game(List<String> inputs) {
		
		if(inputs.size() >= 2) {
			try {
				int id = Integer.parseInt(inputs.get(1));
				if(ServerGameHandler.getGameByID(id) != null) {
					//KNOW GAME
					printBlankLineInConsole();
					printMessageInConsole("Joined game session for game ["+id+"]", true);
					printBlankLineInConsole();
					ConsoleData.focusedGameID = id;
				}else {
					//NO GAME
					printMessageInConsole("There is no running game with id '"+id+"' found!", true);
				}
			}catch(NumberFormatException error) {
				printMessageInConsole("/game [ID]", true);
			}
		}else {
			printMessageInConsole("/game [ID]", true);
		}
		
	}
	
	protected static void sendCommand_games(List<String> inputs) {
		
		if(ServerGameData.runningGames.isEmpty()) {
			//EMPTY
			printMessageInConsole("There are no running games at the moment!", true);
			return;
		}
		
		if(inputs.size() >= 2) {
			//HAS NUMBER
			try {
				int quantity = Integer.parseInt(inputs.get(1));
				int limit = ( ServerGameData.runningGames.size() >= quantity ? quantity : ServerGameData.runningGames.size() );
				printMessageInConsole("Showing "+limit+"/"+ServerGameData.runningGames.size()+" games running:", true);
				for(int i = 1 ; i <= limit ; i++) {
					ServerGame game = ServerGameData.runningGames.get(i-1);
					printMessageInConsole(i+". "+game.getInfoAsString(), true);
				}
			}catch(NumberFormatException error) {
				printMessageInConsole("/games or /games [quantity]", true);
			}
		}else { 
			//NO NUMBER
			int quantity = 10;
			int limit = ( ServerGameData.runningGames.size() >= quantity ? quantity : ServerGameData.runningGames.size() );
			printMessageInConsole("Showing "+limit+"/"+ServerGameData.runningGames.size()+" games running:", true);
			for(int i = 1 ; i <= limit ; i++) {
				ServerGame game = ServerGameData.runningGames.get(i-1);
				printMessageInConsole(i+". "+game.getInfoAsString(), true);
			}
		}
		
	}

	protected static void sendCommand_player(List<String> inputs) {
		
		if(inputs.size() >= 2) {
			try {
				//ID
				int id = Integer.parseInt(inputs.get(1));
				ServerPlayer player = ServerPlayerHandler.getOnlinePlayerByID(id);
				if(player != null) {
					player.getProfile().getConnection().sendData(998, ServerConnection.getNewPacketId(), ""+System.currentTimeMillis());
				}else {
					printMessageInConsole("There is no player with ID '"+id+"' online!", true);
				}
			}catch(NumberFormatException error) {
				//NAME
				String name = inputs.get(1);
				ServerPlayer player = ServerPlayerHandler.getOnlinePlayerByName(name);
				if(player != null) {
					player.getProfile().getConnection().sendData(998, ServerConnection.getNewPacketId(), ""+System.currentTimeMillis());
				}else {
					printMessageInConsole("There is no player with Name '"+name+"' online!", true);
				}
			}
		}else {
			printMessageInConsole("/player [ID|Name]", true);
		}
		
	}
	public static void sendPlayerInfo(ServerPlayer player, int ping) {
		
		printMessageInConsole(player.getInfoAsString()+" - Ping: "+ping+" ms", true);
		
	}
	
	protected static void sendCommand_players(List<String> inputs) {
		
		if(ServerPlayerData.onlinePlayer.isEmpty()) {
			//EMPTY
			printMessageInConsole("There are no online player at the moment!", true);
			return;
		}
		
		if(inputs.size() >= 2) {
			//HAS NUMBER
			try {
				int quantity = Integer.parseInt(inputs.get(1));
				int limit = ( ServerPlayerData.onlinePlayer.size() >= quantity ? quantity : ServerPlayerData.onlinePlayer.size() );
				printMessageInConsole("Showing "+limit+"/"+ServerPlayerData.onlinePlayer.size()+" online player:", true);
				for(int i = 1 ; i <= limit ; i++) {
					ServerPlayer player = ServerPlayerData.onlinePlayer.get(i-1);
					printMessageInConsole(i+". "+player.getInfoAsString(), true);
				}
			}catch(NumberFormatException error) {
				printMessageInConsole("/players or /players [quantity]", true);
			}
		}else { 
			//NO NUMBER
			int quantity = 10;
			int limit = ( ServerPlayerData.onlinePlayer.size() >= quantity ? quantity : ServerPlayerData.onlinePlayer.size() );
			printMessageInConsole("Showing "+limit+"/"+ServerPlayerData.onlinePlayer.size()+" online player:", true);
			for(int i = 1 ; i <= limit ; i++) {
				ServerPlayer player = ServerPlayerData.onlinePlayer.get(i-1);
				printMessageInConsole(i+". "+player.getInfoAsString(), true);
			}
		}
		
	}
	
	protected static void sendCommand_groups(List<String> inputs) {
		
		if(ServerGroupData.activeGroups.isEmpty()) {
			//EMPTY
			printMessageInConsole("There are no active groups at the moment!", true);
			return;
		}
		
		if(inputs.size() >= 2) {
			//HAS NUMBER
			try {
				int quantity = Integer.parseInt(inputs.get(1));
				int limit = ( ServerGroupData.activeGroups.size() >= quantity ? quantity : ServerGroupData.activeGroups.size() );
				printMessageInConsole("Showing "+limit+"/"+ServerGroupData.activeGroups.size()+" active groups:", true);
				for(int i = 1 ; i <= limit ; i++) {
					ServerGroup group = ServerGroupData.activeGroups.get(i-1);
					printMessageInConsole(i+". "+group.getInfoAsString(), true);
				}
			}catch(NumberFormatException error) {
				printMessageInConsole("/groups or /groups [quantity]", true);
			}
		}else { 
			//NO NUMBER
			int quantity = 10;
			int limit = ( ServerGroupData.activeGroups.size() >= quantity ? quantity : ServerGroupData.activeGroups.size() );
			printMessageInConsole("Showing "+limit+"/"+ServerGroupData.activeGroups.size()+" active groups:", true);
			for(int i = 1 ; i <= limit ; i++) {
				ServerGroup group = ServerGroupData.activeGroups.get(i-1);
				printMessageInConsole(i+". "+group.getInfoAsString(), true);
			}
		}
		
	}
	
	protected static void sendCommand_packets(List<String> inputs) {
		
		printBlankLineInConsole();
		printMessageInConsole("Joined packets session", true);
		printBlankLineInConsole();
		ConsoleData.focusedGameID = 0;
		
	}
	
	protected static void sendCommand_overview(List<String> inputs) {
		
		printMessageInConsole("Running BattleProgress-Server on Version "+StandardData.version+" since "+(System.currentTimeMillis()-StandardData.timeSinceStartup)/1000/60+" min", true);
		printMessageInConsole("Running games: "+ServerGameData.runningGames.size(), true);
		printMessageInConsole("Online player: "+ServerPlayerData.onlinePlayer.size(), true);
		printMessageInConsole("Active Groups: "+ServerGroupData.activeGroups.size(), true);
		printMessageInConsole("Loaded units: (B:"+UnitsStatsHandler.buildings.size()+"|L:"+UnitsStatsHandler.troups_land.size()+"|A:"+UnitsStatsHandler.troups_air.size()+")", true);
		printMessageInConsole("Loaded upgrades: "+UpgradeDataHandler.upgrades.size(), true);
		
	}
	
	protected static void sendCommand_update(List<String> inputs) {
		
		if(inputs.size() >= 2) {
			//UNITS OR UPGRADES
			if(inputs.get(1).equalsIgnoreCase("units")) {
				//UNITS
				ConsoleOutput.printMessageInConsole("Updating units...", true);
				UnitsStatsHandler.updateUnitsList(true);
			}else if(inputs.get(1).equalsIgnoreCase("upgrades")) {
				//UPGRADES
				ConsoleOutput.printMessageInConsole("Updating upgrades...", true);
				UpgradeDataHandler.updateUpgradeList(true);
			}else {
				printMessageInConsole("/update [units|upgrades]", true);
			}
		}else {
			printMessageInConsole("/update [units|upgrades]", true);
		}
		
	}
	
	protected static void sendCommand_stop(List<String> inputs) {
		
		ConsoleOutput.printMessageInConsole("Stopping server after "+(System.currentTimeMillis()-StandardData.timeSinceStartup)/1000/60+" min runtime...", true);
		//CLIENT CONNECTIONS
		for(ClientConnectionThread clientSocket : ServerConnection.clientConnectionList) {
			try {
				clientSocket.socket.close();
			} catch (IOException e) { }
		}
		//QUEUE WAITINGTIMER
		ServerQueueHandler.stopQueueWaitTimer();
		ConsoleOutput.printMessageInConsole("QueueWaitingTimer stopped!", true);
		//DB CONNECTION TIMER
		DatabaseData.keepConnectionTimer.cancel();
		ConsoleOutput.printMessageInConsole("KeepConnectionTimer stopped for DB '"+DatabaseData.DBname+"'!", true);
		DatabaseHandler.disconnect();
		ConsoleOutput.printMessageInConsole("Stopped!", true);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 2000);
		
	}
	
	
}
