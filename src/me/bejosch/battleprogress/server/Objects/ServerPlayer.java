package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Data.ServerPlayerData;
import me.bejosch.battleprogress.server.Handler.ProfileHandler;

public class ServerPlayer {

	private PlayerProfile profile;
	
	private ServerGroup group = null;
	private ServerGame game = null;
	private int positionInGame = -1;
	
	//INGAME VALUES
	private List<GameAction> currendRoundLogs = new ArrayList<GameAction>();
	private boolean playerIsRoundReady = false;
	private boolean hasSendAllTask = false;
	private boolean hasExecAllTask = false;
	private int progressPoints = 0;
	
	public ServerPlayer(int id) {
		
		this.profile = ProfileHandler.getPlayerProfile(id);
		
		ServerPlayerData.onlinePlayer.add(this);
	}
	public ServerPlayer(PlayerProfile playerProfile) {
		
		this.profile = playerProfile;
		
		ServerPlayerData.onlinePlayer.add(this);
	}
	
	public void setGroup(ServerGroup group) {
		this.group = group;
	}
	
	public void setGame(ServerGame game) {
		this.game = game;
	}
	public void addToGame(ServerGame game, int positionIngame) {
		this.game = game;
		this.positionInGame = positionIngame;
	}
	public void removeFromGame() {
		this.game = null;
		this.positionInGame = -1;
		this.resetCurrentRoundLog();
		this.resetRoundReady();
		this.resetSendAllTasks();
		this.resetExecAllTasks();
	}
	public void setPositionInGame(int positionInGame) {
		this.positionInGame = positionInGame;
	}
	public void decreasePositionInGame() {
		this.positionInGame--; //IF A PLAYER LEAVE THE OTHER GETS ON LESS POSITION
	}
	public int getPositionInGame() {
		return positionInGame;
	}
	
	
	public String getInfoAsString() {
		return "["+this.profile.getId()+"] - Name: "+this.profile.getName()+" - InGame: "+this.isIngame()+" ("+(isIngame() ? game.getId() : -1 )+") - Online: "+this.profile.getOnlineTimeInMin()+" min - Send: "+this.profile.getConnection().sendedDataList.size();
	}
	
	public boolean isIngroup() {
		return group != null;
	}
	public boolean isIngame() {
		return game != null;
	}
	public int getId() {
		return this.profile.getId();
	}
	public ServerGroup getGroup() {
		return group;
	}
	public ServerGame getGame() {
		return game;
	}
	public PlayerProfile getProfile() {
		return profile;
	}
	
	//INGAME VALUES
	public List<GameAction> getCurrendRoundLogs() {
		return this.currendRoundLogs;
	}
	public void addCurrentRoundLog( GameAction action) {
		this.currendRoundLogs.add(action);
	}
	public void resetCurrentRoundLog() {
		this.currendRoundLogs.clear();
	}
	public void roundReady() {
		this.playerIsRoundReady = true;
	}
	public void resetRoundReady() {
		this.playerIsRoundReady = false;
	}
	public boolean isRoundReady() {
		return this.playerIsRoundReady;
	}
	public void sendAllTasks() {
		this.hasSendAllTask = true;
	}
	public void resetSendAllTasks() {
		this.hasSendAllTask = false;
	}
	public boolean hasSendAllTask() {
		return this.hasSendAllTask;
	}
	public void execAllTasks() {
		this.hasExecAllTask = true;
	}
	public void resetExecAllTasks() {
		this.hasExecAllTask = false;
	}
	public boolean hasExecAllTask() {
		return this.hasExecAllTask;
	}
	
	public int getProgressPoints() {
		return progressPoints;
	}
	public void setProgressPoints(int progressPoints) {
		this.progressPoints = progressPoints;
	}
	public void addProgressPoints(int progressPoints) {
		this.progressPoints += progressPoints;
	}
	
}
