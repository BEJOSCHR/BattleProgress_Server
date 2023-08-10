package me.bejosch.battleprogress.server.Handler;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Data.ProfileData;
import me.bejosch.battleprogress.server.Data.ServerPlayerData;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Main.ServerConnection;
import me.bejosch.battleprogress.server.Objects.ClientConnectionThread;
import me.bejosch.battleprogress.server.Objects.PlayerProfile;
import me.bejosch.battleprogress.server.Objects.ServerGame;
import me.bejosch.battleprogress.server.Objects.ServerGroup;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;

public class ProfileHandler {

// PLAYER ID ==================================================================================================
	private static int nextFreePlayerID = -1;
	
	public static int getNextFreePlayerID() {
		
		if(nextFreePlayerID == -1) {
			//NEED LOAD
			nextFreePlayerID = DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "ID", "Name", "FreeID");
		}
		
		return nextFreePlayerID;
		
	}
	
	public static void useFreePlayerID() {
		
		nextFreePlayerID++;
		DatabaseHandler.updateInt(DatabaseData.tabellName_profile, "ID", nextFreePlayerID, "Name", "FreeID");
		
	}
	
// PROFILES ==================================================================================================
	
	public static void profileConnects(PlayerProfile profile) {
		//TRIGGERED IF AN PROFILE GETS ONLINE
		
		ProfileData.onlineProfiles.add(profile);
		ConsoleOutput.printMessageInConsole("Client logged in! - (ID:"+profile.getId()+") (Name:"+profile.getName()+") (FriendList: "+profile.getFriendlist().size()+")", true);
		
		//SEND FriendList - ONLINE
		for(PlayerProfile friendProfile : profile.getFriendlist()) {
			if(friendProfile.getOnlineTimeInMin() != -1) {
				//ONLINE
				profile.getConnection().sendData(130, ServerConnection.getNewPacketId(), ""+friendProfile.getId()); //SEND CLIENT ONLINE FRIEND
				friendProfile.getConnection().sendData(130, ServerConnection.getNewPacketId(), ""+profile.getId()); //SEND FRIEND THAT CLIENT IS ON
				
				//UPDATE REFERENCE OF FRIEND TO THIS
				for(PlayerProfile friendFriendProfile : friendProfile.getFriendlist()) {
					if(friendFriendProfile.getId() == profile.getId()) {
						//OFFLINE FRIENDLISTLOAD REPRESENT IN FRIENDS FRIENDLIST
						friendProfile.getFriendlist().remove(friendFriendProfile);
						break;
					}
				}
				//ADD THIS PROFILE AS ONLINE REFERENCE TO FRIENDS FRIENDLIST
				friendProfile.getFriendlist().add(profile);
				
			}else {
				//OFFLINE
				profile.getConnection().sendData(131, ServerConnection.getNewPacketId(), ""+friendProfile.getId()); //SEND CLIENT OFFLINE FRIEND
			}
		}
		
	}

	public static void profileDisconnect(PlayerProfile profile) {
		//TRIGGERED IF AN PROFILE GETS OFFLINE
		
		if(profile.isMarkedForDisconnect()) { return; }
		
		ConsoleOutput.printMessageInConsole("Client disconnected! - (ID:"+profile.getId()+") (Name:"+profile.getName()+") (Send: "+profile.getConnection().sendedDataList.size()+")", true);
		profile.setMarkedForDisconnect(true);
		ServerPlayer player = ServerPlayerHandler.getOnlinePlayerByName(profile.getName());
		ServerGroup group = ServerGroupHandler.getGroupByPlayer(player);
		if(group != null) {
			group.removePlayer(player, true);
		}
		ServerGame game = ServerGameHandler.getGameByPlayerID(profile.getId());
		if(game != null) {
			game.removePlayer(profile.getId());
		}
		ServerPlayerData.onlinePlayer.remove(player);
		ProfileData.onlineProfiles.remove(profile);
		
		//SEND FriendList - OFFLINE
		for(PlayerProfile friendProfile : profile.getFriendlist()) {
			if(friendProfile.getOnlineTimeInMin() != -1) {
				//ONLINE
				friendProfile.getConnection().sendData(131, ServerConnection.getNewPacketId(), ""+profile.getId()); //SEND FRIEND THAT CLIENT IS OFF
				//SET REFERENCE OF FRIEND TO THIS TO OFFLINE
				for(PlayerProfile friendFriendProfile : friendProfile.getFriendlist()) {
					if(friendFriendProfile.getId() == profile.getId()) {
						//SET TO OFFLINE
						friendFriendProfile.setConnection(null);
						friendFriendProfile.setOnlineTimestamp(-1);
						break;
					}
				}
			}
		}
		
	}
	
	//GET
	//ONLINE
	public static PlayerProfile getPlayerProfileByClientConnection(ClientConnectionThread connection) {
		
		for(PlayerProfile profile : ProfileData.onlineProfiles) {
			if(profile.getConnection() == connection) {
				//FOUND
				return profile;
			}
		}
		
		return null;
	}
	public static PlayerProfile getPlayerProfileByName(String name) {
		
		for(PlayerProfile profile : ProfileData.onlineProfiles) {
			if(profile.getName().equalsIgnoreCase(name)) {
				//FOUND
				return profile;
			}
		}
		
		return null;
	}
	public static PlayerProfile getPlayerProfileByID(int ID) {
		
		for(PlayerProfile profile : ProfileData.onlineProfiles) {
			if(profile.getId() == ID) {
				//FOUND
				return profile;
			}
		}
		
		return null;
	}
	
	//OFFLINE
	public static PlayerProfile getOfflineProfile(int playerID) {
		
		if(DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Name", "ID", ""+playerID) != null) {
			//PROFILE FOUND
			return new PlayerProfile(null, playerID, true);
		}else {
			//NO PROFILE
			return null;
		}
		
	}
	
	public static PlayerProfile getOfflineProfile(String name) {
		
		if(DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "ID", "Name", name) != -1) {
			//PROFILE FOUND
			return new PlayerProfile(null, name, true);
		}else {
			//NO PROFILE
			return null;
		}
		
	}
	
	//CHECK
	public static boolean isNameUsed(String name) {
		
		if(DatabaseHandler.selectInt(DatabaseData.tabellName_profile, "ID", "Name", name) == -1) {
			//UNKNOWN NAME SO NO ID
			return false;
		}else {
			//KNOWN NAME WITH ID
			return true;
		}
		
	}

	//REGISTER / LOGIN
	public static void createNewProfile(String name, String password, ClientConnectionThread clientConnectionThread) {
		
		if(isNameUsed(name) == false) {
			//NAME IS FREE
			DatabaseHandler.insertNewPlayer(DatabaseData.tabellName_profile, name, password);
			PlayerProfile profile = new PlayerProfile(clientConnectionThread, name, false);
			ServerPlayer player = new ServerPlayer(profile);
			new ServerGroup(player);
			clientConnectionThread.sendData(200, ServerConnection.getNewPacketId(), profile.convertThisPlayerProfileToString()+";Successfully registered!");
		}else {
			clientConnectionThread.sendData(201, ServerConnection.getNewPacketId(), "Username already used!");
		}
		
	}

	public static void checkProfileLogin(String name, String password, ClientConnectionThread clientConnectionThread) {
		
		if(isNameUsed(name) == true) {
			//NAME IS VALID
			if(getPlayerProfileByName(name) == null) {
				//NAME IS NOT ONLINE
				String realName = DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Name", "Name", name);
				if(DatabaseHandler.selectString(DatabaseData.tabellName_profile, "Password", "Name", name).equals(password)) {
					//PASSWORD CORRECT
					PlayerProfile profile = new PlayerProfile(clientConnectionThread, realName, false);
					ServerPlayer player = new ServerPlayer(profile);
					new ServerGroup(player);
					clientConnectionThread.sendData(200, ServerConnection.getNewPacketId(), profile.convertThisPlayerProfileToString()+";Successfully logged in!");
				}else {
					clientConnectionThread.sendData(201, ServerConnection.getNewPacketId(), "Incorrect password!");
				}
			}else {
				clientConnectionThread.sendData(201, ServerConnection.getNewPacketId(), "Already logged in!");
			}
		}else {
			clientConnectionThread.sendData(201, ServerConnection.getNewPacketId(), "Unknown username!");
		}
		
	}
	
}
