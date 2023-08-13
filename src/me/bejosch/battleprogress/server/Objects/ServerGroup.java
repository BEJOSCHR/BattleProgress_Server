package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;

import me.bejosch.battleprogress.server.Data.ServerGroupData;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Handler.ServerGroupHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class ServerGroup {

	private int groupID;
	
	private ArrayList<ServerPlayer> players = new ArrayList<ServerPlayer>();
	private boolean isCustomGame = false;
	
	private int waitedTime = 0;
	private ServerQueue activeQueue = null;
	
	public ServerGroup(ServerPlayer owner) {
		
		this.groupID = ServerGroupHandler.getNewGroupId();
		
		players.add(owner);
		owner.setGroup(this);
		
		ServerGroupData.activeGroups.add(this);
		
	}
	
	public void joinQueue(GameType queueType) {
		
		if(this.activeQueue == null) {
			this.activeQueue = new ServerQueue(queueType, waitedTime, players);
			ConsoleOutput.printMessageInConsole("Group "+this.groupID+" joined a queue! ("+this.activeQueue.getID()+")", true);
		}else {
			ConsoleOutput.printMessageInConsole("Group "+this.groupID+" has already a queue! ("+this.activeQueue.getID()+")", true);
		}
		
	}
	public void leaveQueue(boolean calledFromQueue, boolean userLeave) {
		
		if(this.activeQueue != null) {
			if(userLeave == true) { //USER LEFT QUEUE - RESET TIME
				this.waitedTime = 0;
			}else { //PROGRAM LEAVE - SAVE WAITED TIME
				this.addWaitedTime(this.activeQueue.getWaitedSecondes());
			}
			if(calledFromQueue == false) { this.activeQueue.removeQueue(true); }
			ConsoleOutput.printMessageInConsole("Group "+this.groupID+" left the queue! ("+this.activeQueue.getID()+")", true);
			this.activeQueue = null;
		}else {
			ConsoleOutput.printMessageInConsole("Group "+this.groupID+" has no queue to remove!", true);
		}
		
	}
	
	public void sendDataToAllGroupPlayer(int signal, String data) {
		
		for(ServerPlayer player : this.players) {
			player.getProfile().getConnection().sendData(signal, data);
		}
		
	}
	
	public void invitePlayer(ServerPlayer player) {
		
		if(players.isEmpty() == false && players.contains(player) == false) {
			//PLAYER AT POSITION 0 (SO FIRST ONE) IS HOST
			player.getProfile().getConnection().sendData(300, ""+this.players.get(0).getId());
		}
		
	}
	
	public void addPlayer(ServerPlayer player) {
		
		if(this.isCustomGame() == false && this.getPlayers().size() != 1) {
			//NO CUSTOM GAME AND ALREADY 2 PLAYER
			return;
		}
		
		player.getGroup().removePlayer(player, true);
		players.add(player);
		player.setGroup(this);
		for(ServerPlayer gamePlayer : players) {
			sendDataToAllGroupPlayer(301, ""+gamePlayer.getId());
		}
		
	}
	
	public void kickPlayer(ServerPlayer player) {
		
		if(players.contains(player)) {
			sendDataToAllGroupPlayer(303, ""+player.getId());
			players.remove(player);
			
			//NEW GROUP
			new ServerGroup(player);
		}
		
	}
	public void removePlayer(ServerPlayer player, boolean playerDisconnect) {
		
		if(players.contains(player)) {
			players.remove(player);
			if(players.isEmpty()) {
				//NO ONE LEFT IN THIS GROUP
				this.removeGroup();
			}else {
				player.getProfile().getConnection().sendData(304, ""+player.getId()); //SEPERATE SENDING BECAUSE OF INFINIT LOOPS
				sendDataToAllGroupPlayer(304, ""+player.getId());
			}
			
			if(playerDisconnect == false) {
				//NEW GROUP
				new ServerGroup(player);
			}
			
		}
		
	}
	
	public void changeToCustomGame() {
		
		this.isCustomGame = true;
		
	}
	public void changeToNoCustomGame() {
		
		this.isCustomGame = false;
		
	}
	
	public void addWaitedTime(int waitedTime) {
		
		this.waitedTime += waitedTime;
		
	}
	
	public void removeGroup() {
		
		if(this.activeQueue != null) {
			this.leaveQueue(false, false);
		}
		
		for(ServerPlayer player : this.players) {
			player.setGroup(null);
		}
		
		ServerGroupData.activeGroups.remove(this);
		
	}
	
	public String getInfoAsString() {
		return "["+this.groupID+"] "+(players.size() >= 1 ? players.get(0).getProfile().getName() : "null")+", "+(players.size() >= 2 ? players.get(1).getProfile().getName() : "null")+", "+(players.size() >= 3 ? players.get(2).getProfile().getName() : "null")+", "+(players.size() >= 4 ? players.get(3).getProfile().getName() : "null")+" - Queue: ["+(this.activeQueue != null ? activeQueue.getInfoAsString() : "null")+"] - PlayerCount: "+this.players.size()+" - WaitedTime: "+this.waitedTime;
	}
	
	public ServerQueue getActiveQueue() {
		return activeQueue;
	}
	public int getID() {
		return groupID;
	}
	public ArrayList<ServerPlayer> getPlayers() {
		return players;
	}
	public boolean isCustomGame() {
		return isCustomGame;
	}
	
}
