package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import me.bejosch.battleprogress.server.Data.ConnectionData;
import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Enum.GameActionType;
import me.bejosch.battleprogress.server.Enum.GameFinishCause;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;
import me.bejosch.battleprogress.server.Handler.DictionaryInfoHandler;
import me.bejosch.battleprogress.server.Handler.FieldDataHandler;
import me.bejosch.battleprogress.server.Handler.ProfileHandler;
import me.bejosch.battleprogress.server.Handler.ServerGameHandler;
import me.bejosch.battleprogress.server.Handler.ServerPlayerHandler;
import me.bejosch.battleprogress.server.Handler.UnitsStatsHandler;
import me.bejosch.battleprogress.server.Handler.UpgradeDataHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class ClientConnection {

	public IoSession session;
	public boolean securityIdentificationAccepted = false;
	
	public List<String> sendedDataList = new ArrayList<String>();
	
//==========================================================================================================
	public ClientConnection(IoSession session) {
		
		ConnectionData.clientConnectionList.add(this);
		this.session = session;
		
	}
	
//==========================================================================================================
	public void handlePackage(String message) {
		
		if(message == null) { return; }
		try {
			if(message.equalsIgnoreCase("Establish new connection") == false) { 
				int signal = Integer.parseInt(message.substring(0, 3).trim());
				int id = Integer.parseInt(message.substring(4, ConnectionData.PACKAGEIDLENGTH+4).trim());
				String answer = null;
				if(message.length() > ConnectionData.PACKAGEIDLENGTH+1+4) {
					answer = message.substring(ConnectionData.PACKAGEIDLENGTH+1+4, message.length()).trim();
				}
				recieveDataFromClient(signal, id, answer);
			}
		}catch(NullPointerException | NumberFormatException | IndexOutOfBoundsException error) {
			//IGNORE DATA WITH WRONG SYNTAX
			ConsoleOutput.printMessageInConsole(-1, "Wrong syntax data received! ["+message+"] ("+error.getLocalizedMessage()+")", true);
//			error.printStackTrace();
		}
		
	}
	
//==========================================================================================================
	/**
	 * Send given data to this client
	 * @param data - String - The Data which should be sent
	 */
	public void sendData(int signal, String data) {
		
		long id = ConnectionData.getNewPacketId();
		session.write(signal+"-"+id+"-"+data);
		
		if(signal != 997 && signal != 801) {
			sendedDataList.add(signal+"-"+id+"-"+data);
		}
		
	}
	
//==========================================================================================================
	/**
	 * Handle the Data
	 * @param signal - int - The signal infront of the data
	 * @param id - int - The id of the data
	 * @param data - String - The data which has been send
	 */
	public void recieveDataFromClient(int signal, int id, String data) {
			
		int[] signalBlackList = {103, 105, 106, 112, 997, 801};
		List<Integer> noConsoleOutput = new ArrayList<>();
		for(int i : signalBlackList) {noConsoleOutput.add(i);}
		
		ServerPlayer player = ServerPlayerHandler.getOnlinePlayer(this);
		ServerGame game = null;
		if(player != null) { game = ServerGameHandler.getGameByPlayerID(player.getId()); }
		
		if(!noConsoleOutput.contains(signal)) {
			if(player != null) {
				ConsoleOutput.printMessageInConsole(0, "["+this.getAddress()+"]: [signal:"+signal+" ; id:"+id+" ; data:"+data+"] - From user '"+player.getProfile().getName()+"'", false);
			}else {
				ConsoleOutput.printMessageInConsole(0, "["+this.getAddress()+"]: [signal:"+signal+" ; id:"+id+" ; data:"+data+"] - From user '<Unknown>'", false);
			}
		}
		
		String[] content = null;
		if(data != null) {
			content = data.split(";");
		}
		
		switch(signal) {
//========================================================================
		//CONNECT
		case 100:
			sendData(100, "Server Answer");
			break;
//========================================================================
		//Units update request
		case 110:
			UnitsStatsHandler.updateUnitsForClient(this);
			break;
//========================================================================
		//Upgrade update request
		case 111:
			UpgradeDataHandler.updateUpgradesForClient(this);
			break;
//========================================================================
		//DictionaryInfo update request
		case 112:
			DictionaryInfoHandler.updateDictionaryInfoForClient(this);
			break;
//========================================================================
		//FieldData update request
		case 113:
			FieldDataHandler.updateFieldDataForClient(this);
			break;
//========================================================================
		//Player Data Request
		case 120:
			//PlayerID
			int playerID1 = Integer.parseInt(data);
			ServerPlayer player1 = ServerPlayerHandler.getOnlinePlayer(playerID1);
			if(player1 != null) {
				//ONLINE PLAYER FOUND
				this.sendData(120, player1.getProfile().convertThisPlayerProfileToString());
			}else {
				//NOT ONLINE PLAYER
				PlayerProfile profile1 = ProfileHandler.getOfflineProfile(playerID1);
				if(profile1 != null) {
					//FOUND OFFLINE PROFILE
					this.sendData(120, profile1.convertThisPlayerProfileToString());
				}else {
					//NO PROFILE AT ALL
					ConsoleOutput.printMessageInConsole("Couln't find DATA for Player "+playerID1+" (Requested from "+player.getId()+":"+player.getProfile().getName()+")", true);
				}
			}
			break;
//========================================================================
		//Player Stats Request
		case 121:
			//PlayerID
//			int playerID2 = Integer.parseInt(data);
			//TODO
			
			break;
//========================================================================
		//Send all Friendrequests
		case 135:
			for(int requestingPlayerID : DatabaseHandler.getAllWhereEqual_Int(DatabaseData.tabellName_friendRequests, "ID", "RequestID", player.getId()+"")) {
				//GETTING ALL PLAYERIDs WHICH HAS SEND A REQUEST TO THIS CLIENTID
				PlayerProfile requestingProfile = ProfileHandler.getOfflineProfile(requestingPlayerID);
				player.getProfile().getConnection().sendData(135, requestingProfile.getId()+";"+requestingProfile.getName());
			}
			break;
//========================================================================
		//Check player friendrequest
		case 136:
			//PlayerName
			String username = data;
			PlayerProfile onlineFoundProfile = ProfileHandler.getPlayerProfile(username);
			if(onlineFoundProfile != null) {
				//FOUND ONLINE
				if(DatabaseHandler.getAllWhereEqual_Int(DatabaseData.tabellName_friendRequests, "ID", "RequestID", onlineFoundProfile.getId()+"").contains(player.getId())) {
					//ALREADY SEND REQUEST
					player.getProfile().getConnection().sendData(136, "true"+";"+"You already send a friendrequest to "+username);
					return;
				}
				onlineFoundProfile.getConnection().sendData(135, player.getId()+";"+player.getProfile().getName());
				DatabaseHandler.insertData(DatabaseData.tabellName_friendRequests, "(ID, RequestID)", "('"+player.getId()+"','"+onlineFoundProfile.getId()+"')");
				player.getProfile().getConnection().sendData(136, "true"+";"+username+" was send a friendrequest");
			}else {
				//TRY OFFLINE
				PlayerProfile offlineFoundProfile = ProfileHandler.getOfflineProfile(username);
				if(offlineFoundProfile != null) { 
					//FOUND OFFLINE
					if(DatabaseHandler.getAllWhereEqual_Int(DatabaseData.tabellName_friendRequests, "ID", "RequestID", offlineFoundProfile.getId()+"").contains(player.getId())) {
						//ALREADY SEND REQUEST
						player.getProfile().getConnection().sendData(136, "true"+";"+"You already send a friendrequest to "+username);
						return;
					}
					DatabaseHandler.insertData(DatabaseData.tabellName_friendRequests, "(ID, RequestID)", "('"+player.getId()+"','"+offlineFoundProfile.getId()+"')");
					player.getProfile().getConnection().sendData(136, "true"+";"+username+" was send a friendrequest, but he is currently offline!");
				}else {
					//NONE EXISTENT USERNAME
					player.getProfile().getConnection().sendData(136, "false"+";"+"This username is not registered!");
				}
			}
			break;
//========================================================================
		//Friendrequest Accepted
		case 137:
			int acceptedID = Integer.parseInt(data);
			DatabaseHandler.insertData(DatabaseData.tabellName_friendlist, "(ID, FriendID)", "('"+player.getId()+"','"+acceptedID+"')");
			DatabaseHandler.insertData(DatabaseData.tabellName_friendlist, "(ID, FriendID)", "('"+acceptedID+"','"+player.getId()+"')");
			DatabaseHandler.deleteData(DatabaseData.tabellName_friendRequests, "ID", ""+acceptedID, "RequestID", ""+player.getId());
			DatabaseHandler.deleteData(DatabaseData.tabellName_friendRequests, "ID", ""+player.getId(), "RequestID", ""+acceptedID);
			ServerPlayer onlineFriend = ServerPlayerHandler.getOnlinePlayer(acceptedID);
			if(onlineFriend != null) {
				//FRIEND IS ONLINE
				onlineFriend.getProfile().getConnection().sendData(130, ""+player.getId());
				player.getProfile().getConnection().sendData(130, ""+acceptedID);
			}else {
				//FRIEND IS OFFLINE
				player.getProfile().getConnection().sendData(131, ""+acceptedID);
			}
			break;
//========================================================================
		//Friendrequest DECLINED
		case 138:
			int declinedID = Integer.parseInt(data);
			DatabaseHandler.deleteData(DatabaseData.tabellName_friendRequests, "ID", ""+declinedID, "RequestID", ""+player.getId());
			DatabaseHandler.deleteData(DatabaseData.tabellName_friendRequests, "ID", ""+player.getId(), "RequestID", ""+declinedID);
			break;
//========================================================================
		//FriendRemove
		case 139:
			int removedID = Integer.parseInt(data);
			ServerPlayer onlineFriend2 = ServerPlayerHandler.getOnlinePlayer(removedID);
			
			//REMOVE
			DatabaseHandler.deleteData(DatabaseData.tabellName_friendlist, "ID", ""+player.getId(), "FriendID", ""+removedID);
			DatabaseHandler.deleteData(DatabaseData.tabellName_friendlist, "ID", ""+removedID, "FriendID", ""+player.getId());
			player.getProfile().getFriendlist().remove(onlineFriend2.getProfile());
			onlineFriend2.getProfile().getFriendlist().remove(player.getProfile());
			
			//SEND
			if(onlineFriend2 != null) {
				//FRIEND IS ONLINE
				onlineFriend2.getProfile().getConnection().sendData(139, ""+player.getId());
			}
			player.getProfile().getConnection().sendData(139, ""+removedID);
			break;
//========================================================================
		//Chat Message Menu
		case 140:
			//ReceiverID ; Message
			int receiverID = Integer.parseInt(content[0]);
			String message = DatabaseHandler.makeStringDBSave(content[1]);
			PlayerProfile receiverProfile = ProfileHandler.getPlayerProfile(receiverID);
			if(receiverProfile != null) {
				receiverProfile.getConnection().sendData(140, player.getId()+";"+message);
			}
			break;
//========================================================================
		//Register new Profil
		case 200:
			// UserName ; Password
			String userName_1 = content[0];
			String passowrd_1 = content[1];
			ProfileHandler.createNewProfile(userName_1, passowrd_1, this);
			break;
//========================================================================
		//Login in Profil
		case 201:
			// UserName ; Password
			String userName_2 = content[0];
			String passowrd_2 = content[1];
			ProfileHandler.checkProfileLogin(userName_2, passowrd_2, this);
			break;
//========================================================================
		//GROUP INVITE
		case 300:
			//PlayerID (der eingeladen werden soll)
			int invitedPlayerID = Integer.parseInt(data);
			ServerPlayer invitedPlayer = ServerPlayerHandler.getOnlinePlayer(invitedPlayerID);
			if(invitedPlayer != null) {
				//SEND GROUP INVITE
				player.getGroup().invitePlayer(invitedPlayer);
			}else {
				ConsoleOutput.printMessageInConsole("Couln't find invited Player "+invitedPlayerID+" (From "+player.getId()+":"+player.getProfile().getName()+")", true);
			}
			break;
//========================================================================
		//ACCEPT GROUP INVITE
		case 301:
			//PlayerID (der ihn eingeladen hat)
			int invitingPlayerID = Integer.parseInt(data);
			ServerPlayer invitingPlayer = ServerPlayerHandler.getOnlinePlayer(invitingPlayerID);
			if(invitingPlayer != null) {
				//ADD PLAYER TO GROUP
				invitingPlayer.getGroup().addPlayer(player);
			}else {
				ConsoleOutput.printMessageInConsole("Couln't find inviting Player (Accept) "+invitingPlayerID+" (From "+player.getId()+":"+player.getProfile().getName()+")", true);
			}
			break;
//========================================================================
		//DECLINE GROUP INVITE
		case 302:
			//PlayerID (der ihn eingeladen hat)
			int invitingPlayerID1 = Integer.parseInt(data);
			ServerPlayer invitingPlayer1 = ServerPlayerHandler.getOnlinePlayer(invitingPlayerID1);
			if(invitingPlayer1 != null) {
				//SEND INFO TO INVITING PLAYER
				invitingPlayer1.getProfile().getConnection().sendData(302, ""+player.getId());
			}else {
				ConsoleOutput.printMessageInConsole("Couln't find inviting Player (Decline) "+invitingPlayerID1+" (From "+player.getId()+":"+player.getProfile().getName()+")", true);
			}
			break;
//========================================================================
		//KICK PLAYER FROM GROUP
		case 303:
			//PlayerID (der gekickt werden soll) 
			int kickedPlayerID = Integer.parseInt(data);
			ServerPlayer kickedPlayer = ServerPlayerHandler.getOnlinePlayer(kickedPlayerID);
			if(kickedPlayer != null) {
				//SEND INFO TO INVITING PLAYER
				kickedPlayer.getGroup().kickPlayer(kickedPlayer);
			}else {
				ConsoleOutput.printMessageInConsole("Couln't find kicked Player "+kickedPlayerID+" (From "+player.getId()+":"+player.getProfile().getName()+")", true);
			}
			break;
//========================================================================
		//LEAVE GROUP
		case 304:
			player.getGroup().removePlayer(player, false);
			break;
//========================================================================
		//JOIN QUEUE
		case 400:
			//QueueType
			GameType queueType = GameType.valueOf(data);
			player.getGroup().joinQueue(queueType);
			break;
//========================================================================
		//LEAVE QUEUE
		case 401:
			player.getGroup().leaveQueue(false, true);
			break;
//========================================================================
		//GAME ACCEPT
		case 405:
			//GameID
			int gameID = Integer.parseInt(data);
			ServerGame acceptGame = ServerGameHandler.getGameByID(gameID);
			acceptGame.addPlayer(player);
			break;
//========================================================================
		//Task - ATTACK
		case 600:
			// startX ; startY ; goalX ; goalY ; Count
			int startX_1 = Integer.parseInt(content[0]);
			int startY_1 = Integer.parseInt(content[1]);
			int goalX_1 = Integer.parseInt(content[2]);
			int goalY_1 = Integer.parseInt(content[3]);
			int count_1 = Integer.parseInt(content[4]);
			
			game.sendDataToAllGamePlayer(600, player.getId()+";"+startX_1+";"+startY_1+";"+goalX_1+";"+goalY_1+";"+count_1);
			game.addAttack(player, startX_1, startY_1, goalX_1, goalY_1, count_1);
			
			break;
//========================================================================
		//Task - HEAL / REPAIR
		case 601:
			
			// startX ; startY ; goalX ; goalY ; Count
			int startX_2 = Integer.parseInt(content[0]);
			int startY_2 = Integer.parseInt(content[1]);
			int goalX_2 = Integer.parseInt(content[2]);
			int goalY_2 = Integer.parseInt(content[3]);
			int count_2 = Integer.parseInt(content[4]);
			
			game.sendDataToAllGamePlayer(601, player.getId()+";"+startX_2+";"+startY_2+";"+goalX_2+";"+goalY_2+";"+count_2);
			game.addHeal(player, startX_2, startY_2, goalX_2, goalY_2, count_2);
			
			break;
//========================================================================
		//Task - BUILD
		case 602:
			
			// buildingName ; goalX ; goalY
			String buildingName = content[0];
			int goalX_3 = Integer.parseInt(content[1]);
			int goalY_3 = Integer.parseInt(content[2]);
			
			game.sendDataToAllGamePlayer(602, player.getId()+";"+buildingName+";"+goalX_3+";"+goalY_3);
			game.addBuild(player, buildingName, goalX_3, goalY_3);
			
			break;
//========================================================================
		//Task - PRODUCE
		case 603:
			
			// troupName ; startX ; startY ; goalX ; goalY
			String troupName = content[0];
			int startX_4 = Integer.parseInt(content[1]);
			int startY_4 = Integer.parseInt(content[2]);
			int goalX_4 = Integer.parseInt(content[3]);
			int goalY_4 = Integer.parseInt(content[4]);
			
			game.sendDataToAllGamePlayer(603, player.getId()+";"+troupName+";"+startX_4+";"+startY_4+";"+goalX_4+";"+goalY_4);
			game.addProduce(player, troupName, startX_4, startY_4, goalX_4, goalY_4);
			
			break;
//========================================================================
		//Task - MOVE
		case 604:
			
			// startX ; startY ; goalX ; goalY
			int startX_5 = Integer.parseInt(content[0]);
			int startY_5 = Integer.parseInt(content[1]);
			int goalX_5 = Integer.parseInt(content[2]);
			int goalY_5 = Integer.parseInt(content[3]);
			
			game.sendDataToAllGamePlayer(604, player.getId()+";"+startX_5+";"+startY_5+";"+goalX_5+";"+goalY_5);
			game.addMove(player, startX_5, startY_5, goalX_5, goalY_5);
			
			break;
//========================================================================
		//Task - REMOVE
		case 605:
			
			// goalX ; goalY
			int goalX_6 = Integer.parseInt(content[0]);
			int goalY_6 = Integer.parseInt(content[1]);
			
			game.sendDataToAllGamePlayer(605, player.getId()+";"+goalX_6+";"+goalY_6);
			game.addRemove(player, goalX_6, goalY_6);
			
			break;
//========================================================================
		//Task - UPGRADE
		case 606:
			
			// upgradeTroupName ; startX ; startY ; goalX ; goalY
			String troupName_7 = content[0];
			int startX_7 = Integer.parseInt(content[1]);
			int startY_7 = Integer.parseInt(content[2]);
			int goalX_7 = Integer.parseInt(content[3]);
			int goalY_7 = Integer.parseInt(content[4]);
			
			game.sendDataToAllGamePlayer(606, player.getId()+";"+troupName_7+";"+startX_7+";"+startY_7+";"+goalX_7+";"+goalY_7);
			game.addUpgrade(player, troupName_7, startX_7, startY_7, goalX_7, goalY_7);
			
			break;
//========================================================================
		//RESEARCH UNLOCK SYNC
		case 610:
			//researchName
			String researchName = data;
			game.updateUpgradeResearch(player.getId(), researchName);
			break;
//========================================================================
		// 620 - GAME START
//========================================================================
		//Create special Building (Headquarter)
		case 621:
			// buildingName ; X ; Y
			String buildingName_2 = content[0];
			int X1 = Integer.parseInt(content[1]);
			int Y1 = Integer.parseInt(content[2]);
			int roundNumber = (buildingName_2.equals("Headquarter") ? 0 : game.getRoundNumber()); //HQ always build at round 0
			game.execBuild(new GameAction(player.getId(), GameActionType.BUILD, roundNumber, buildingName_2, X1, Y1, -1, -1, -1));
			game.sendDataToAllGamePlayer(621, player.getId()+";"+buildingName_2+";"+X1+";"+Y1);
			break;
//========================================================================
		//Client is ready for this round
		case 650:
			player.roundReady();
			game.playerIsRoundReady(player.getId());
			break;
//========================================================================
		//Client is UN ready for this round
		case 651:
			player.resetRoundReady();
			game.playerIsRound_UN_Ready(player.getId());
			break;
//========================================================================
		//All ready - Only client receive
		case 652:
//========================================================================
		//Client has send all his tasks
		case 653:
			player.sendAllTasks();
			break;
//========================================================================
		//Client has executed all tasks
		case 654:
			player.execAllTasks();
			break;
//========================================================================
		//CHAT MESSAGE
		case 660:
			// message
			game.sendChatMessage(player.getId(), player.getProfile().getName(), DatabaseHandler.makeStringDBSave(data));
			break;
//========================================================================
		//FIELD PING
		case 661:
			// x ; y
			int fieldX = Integer.parseInt(content[0]);
			int fieldY = Integer.parseInt(content[1]);
			game.sendFieldPing(player.getId(), fieldX, fieldY);
			break;
//========================================================================
		//GAME SURRENDER
		case 690:
			// -
			game.finishGame(player.getId(), GameFinishCause.SURRENDER);
			break;
//========================================================================
		//GAME SURRENDER REQUEST
		case 691:
			// -
			game.requestSurrender(player);
			break;
//========================================================================
		//GAME DISCONNECT INFO (ONLY SEND)
		case 695:
			// -
			break;
//========================================================================
		//GAME RECONNECT INFO (ONLY SEND)
		case 696:
			// -
			break;
//========================================================================
		//GAME SYNC FINISHED (ONLY SEND)
		case 697:
			// -
			break;
//========================================================================
		//GAME SYNC DATA - General Game Data (Done confirmation)
		case 698:
			// GameID
			ServerGameHandler.getGameByID(Integer.parseInt(data)).reconnect_generalDone(player);
			break;
		//GAME SYNC DATA - RoundNumber (Done confirmation)
		case 699:
			// GameID
			ServerGameHandler.getGameByID(Integer.parseInt(data)).reconnect_metaDone(player);
			break;
		//GAME SYNC DATA - RoundNumber (Done confirmation)
		case 700:
			// GameID
			ServerGameHandler.getGameByID(Integer.parseInt(data)).reconnect_actionDone(player);
			break;
//========================================================================
		//CLIENT GAME PING
		case 801:
			// SendTimeStamp
			long sendTimestamp = Long.parseLong(data);
			int ping = (int) ( (System.currentTimeMillis()-sendTimestamp)/2.0);
			game.updatePlayerPing(player.getId(), ping);
			break;
//========================================================================
		//CheckConnection Answer
		case 997:
			sendData(997, "This is a ping test packet from the server as an answer to the client, it is used to calculate the ping in ms!");
			break;	
//========================================================================
		//ClientPingAnswer
		case 998:
			long startTimestamp = Long.parseLong(data);
			int ms = (int) ((System.currentTimeMillis()-startTimestamp)/2);
			ConsoleOutput.sendPlayerInfo(player, ms);
			break;	
		default:
			ServerPlayer errorPlayer = ServerPlayerHandler.getOnlinePlayer(this);
			if(errorPlayer == null) {
				ConsoleOutput.printMessageInConsole(0, "["+this.getAddress()+"]: INVALID SIGNAL [signal:"+signal+" ; id:"+id+" ; data:"+data+"] - From user '<Unknown>'", false);
			}else {
				ConsoleOutput.printMessageInConsole(0, "["+this.getAddress()+"]: INVALID SIGNAL [signal:"+signal+" ; id:"+id+" ; data:"+data+"] - From user '"+player.getProfile().getName()+"'", false);
			}
			break;
		}
			
	}

	private String getAddress() {
		return this.session.getRemoteAddress().toString();
	}
	
}
