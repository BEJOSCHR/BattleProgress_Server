package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import me.bejosch.battleprogress.server.Data.ConsoleData;
import me.bejosch.battleprogress.server.Data.ServerGameData;
import me.bejosch.battleprogress.server.Enum.GameActionType;
import me.bejosch.battleprogress.server.Enum.GameFinishCause;
import me.bejosch.battleprogress.server.Enum.GameStatus;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Handler.ServerGameHandler;
import me.bejosch.battleprogress.server.Handler.ServerGroupHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class ServerGame {

	private int id;
	private GameType type;
	private String statusDescription = "";
	private Map map;
	
	private ServerPlayer host = null; //IS SET TO NULL FOR NONE CUSTOM GAME
	
	private GameStatus status = GameStatus.LOBBY;
	private int roundNumber = 1;
	private boolean isRunning = false, isRoundChange = false;
	
	private HashMap<ServerPlayer, Integer> disconnectedPlayer = new HashMap<>();
	private List<ServerPlayer> awaitReconnect = new ArrayList<ServerPlayer>();
	private CopyOnWriteArrayList<ServerPlayer> spectator = new CopyOnWriteArrayList<ServerPlayer>();

	private Timer reconnectTimer = null;
	private Timer playerReadyTimer = null;
	private Timer sendTaskWaitTimer = null;
	private Timer execTaskWaitTimer = null;
	private Timer pingUpdateTimer = null;
	
	private LinkedList<ServerPlayer> playerList = new LinkedList<ServerPlayer>();
	private List<ServerBuilding> buildings = new ArrayList<ServerBuilding>();
	private List<ServerTroup> troups = new ArrayList<ServerTroup>();
	private List<ServerResearch> research = new ArrayList<ServerResearch>();
	
	private int currentFirstPlayerPos = 0;
	private CopyOnWriteArrayList<GameAction> actionLog = new CopyOnWriteArrayList<GameAction>(); //ALL ACTIONS ARE LOGGED IN HERE
	//HAS TO BE COPYONWRITE BECAUSE ASYNC ADD FROM THE CONNECTIONS
	private int executeID = 1; //Counted up for every execution
	
	private int blockedActionsForThisRound = 0;
	private List<Field> usedFieldsForThisRound = new ArrayList<Field>();
	
	private int chatMessageNumber = 1;
	
	private List<ServerBuilding> toRemoveBuildings = new ArrayList<>();
	private List<ServerTroup> toRemoveTroups = new ArrayList<>();
	
	public ServerGame(GameType type, Map map) {
		// NORMAL / RANKED GAME - Created by the queue manager
		
		this.id = ServerGameHandler.getNewGameId();
		this.type = type;
		this.statusDescription = "Ingame ("+this.type.toString().replace("_", " ")+")";
		this.map = map;
		
		ServerGameData.runningGames.add(this);
		ConsoleOutput.printMessageInConsole("CREATED GAME "+getId()+" ["+getType()+"] ON MAP '"+map.name+"'", true);
		ConsoleData.lastCreatedGameID = this.getId();
		checkAllAccept();
	}
	public ServerGame(ServerPlayer host, Map map) {
		//CUSTOM GAME
		
		this.host = host;
		
		this.id = ServerGameHandler.getNewGameId();
		this.type = GameType.Custom_1v1;
		this.statusDescription = "Ingame ("+this.type.toString().replace("_", " ")+")";
		this.map = map;
		
		ServerGameData.runningGames.add(this);
		ConsoleOutput.printMessageInConsole("CREATED GAME "+getId()+" ["+getType()+"] ON MAP '"+map.name+"' WITH HOST '"+host.getProfile().getName()+"'", true);
		ConsoleData.lastCreatedGameID = this.getId();
	}
	
	private void checkAllAccept() {
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				
				if(type == GameType.Normal_2v2) { //CUSTOMS AUSGESCHLOSSEN
					//2v2
					if(getPlayerCount() == 4) {
						//ALL JOINED
						startQueuedGame();
					}else {
						//MISSING
						deleteGame();
					}
				}else {
					//1v1
					if(getPlayerCount() == 2) {
						//ALL JOINED
						startQueuedGame();
					}else {
						//MISSING
						deleteGame();
					}
				}
				
			}
		}, 1000*ServerGameData.gameAcceptWaitingDuration);
		
	}
	
	//START (queued) GAME
	public void startQueuedGame() {
		
		ConsoleOutput.printMessageInConsole("STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		ConsoleOutput.printMessageInConsole(getId(), "STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		
		this.isRunning = true;
		this.status = GameStatus.INGAME;
		
		sendGameDataToPlayer();
		startPlayerReadyTimer();
		
	}
	
	//START (custom) GAME MANUEL
	public void startCustomGame() {
		
		ConsoleOutput.printMessageInConsole("STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		ConsoleOutput.printMessageInConsole(getId(), "STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		
		this.isRunning = true;
		this.status = GameStatus.INGAME;
		
		sendGameDataToPlayer();
		startPlayerReadyTimer();
		
	}
	
	//SEND GAME DATA TO PLAYER
	public void sendGameDataToPlayer() {
		
		//GameID ; GameModus ; PID1 ; PID2 ; PID3 ; PID4 ; MapName ; MapData
		String data = getGameData();
		
		for(ServerPlayer player : this.getPlayerList()) {
			player.resetCurrentRoundLog();
			player.resetRoundReady();
			player.resetSendAllTasks();
			player.resetExecAllTasks();
			player.getProfile().getConnection().sendData(620, data);
			player.getProfile().setCurrentActivity(this.statusDescription);
		}
		
		//START PING UPDATE TIMER
		startPingUpdateTimer();
		
	}
	
	public String getGameData() {
		
		//GameID ; GameModus ; PID1 ; PID2 ; PID3 ; PID4 ; MapName ; MapData
		String data = null;
		if(GameType.isModus1v1(type)) {
			//1v1
			data = id+";"+type+";"+playerList.get(0).getId()+";"+playerList.get(1).getId()+";"+"-1"+";"+"-1"+";"+map.name+";"+map.convertFieldListIntoStringSyntax();
		}else {
			//2v2
			data = id+";"+type+";"+playerList.get(0).getId()+";"+playerList.get(1).getId()+";"+playerList.get(2).getId()+";"+playerList.get(3).getId()+";"+map.name+";"+map.convertFieldListIntoStringSyntax();
		}
		return data;
		
	}
	
	//CANCLE/DELETE GAME START
	public void deleteGame() {
		
		//STOP ALL TIMERS
		stopPlayerReadyTimer();
		stopSendWaitTimer();
		stopExecWaitTimer();
		stopPingUpdateTimer();
		
		if(this.isRunning() == false) {
			//IN ACCEPT PHASE - Try rejoin Queues
			List<ServerGroup> waitingGroups = new ArrayList<>();
			List<ServerGroup> handledGroups = new ArrayList<>();
			for(ServerPlayer player : this.getPlayerList()) {
				ServerGroup group = ServerGroupHandler.getGroupByPlayer(player);
				if(handledGroups.contains(group) == false) {
					if(group.getPlayers().size() == 2) {
						//2er GROUP
						if(waitingGroups.contains(group)) {
							//OTHER PLAYER ALSO ACCEPTED - rejoin Queue
							group.joinQueue(this.getType());
							waitingGroups.remove(group);
							handledGroups.add(group);
						}else {
							//ONLY ONE OF 2 PLAYER ACCEPTED YET - wait for other or dont rejoin
							waitingGroups.add(group);
						}
					}else {
						//NOT HANDLED YET
						group.joinQueue(this.getType());
						handledGroups.add(group);
					}
				}
			}
		}
		
		for(ServerPlayer player : this.getPlayerList()) {
			//REMOVE PLAYER FROM GAME
			player.removeFromGame();
			player.getProfile().setCurrentActivity("Online");
		}
		
		if(this.isRunning() == false) {
			switch(this.getType()) {
			case Custom_1v1:
				ConsoleOutput.printMessageInConsole("DELETED GAME "+getId()+" ["+getType()+"] BECAUSE IT WAS MANUEL ABORTED", true);
				ConsoleOutput.printMessageInConsole(getId(), "DELETED GAME "+getId()+" ["+getType()+"] BECAUSE IT WAS MANUEL ABORTED", true);
				break;
			case Custom_2v2:
				ConsoleOutput.printMessageInConsole("DELETED GAME "+getId()+" ["+getType()+"] BECAUSE IT WAS MANUEL ABORTED", true);
				ConsoleOutput.printMessageInConsole(getId(), "DELETED GAME "+getId()+" ["+getType()+"] BECAUSE IT WAS MANUEL ABORTED", true);
				break;
			case Normal_1v1:
				ConsoleOutput.printMessageInConsole("DELETED GAME "+getId()+" ["+getType()+"] BECAUSE OF AN MISSING ACCEPT! ("+getPlayerCount()+"/2)", true);
				ConsoleOutput.printMessageInConsole(getId(), "DELETED GAME "+getId()+" ["+getType()+"] BECAUSE OF AN MISSING ACCEPT! ("+getPlayerCount()+"/2)", true);
				break;
			case Normal_2v2:
				ConsoleOutput.printMessageInConsole("DELETED GAME "+getId()+" ["+getType()+"] BECAUSE OF AN MISSING ACCEPT! ("+getPlayerCount()+"/4)", true);
				ConsoleOutput.printMessageInConsole(getId(), "DELETED GAME "+getId()+" ["+getType()+"] BECAUSE OF AN MISSING ACCEPT! ("+getPlayerCount()+"/4)", true);
				break;
			case Ranked_1v1:
				ConsoleOutput.printMessageInConsole("DELETED GAME "+getId()+" ["+getType()+"] BECAUSE OF AN MISSING ACCEPT! ("+getPlayerCount()+"/2)", true);
				ConsoleOutput.printMessageInConsole(getId(), "DELETED GAME "+getId()+" ["+getType()+"] BECAUSE OF AN MISSING ACCEPT! ("+getPlayerCount()+"/2)", true);
				break;
			default:
				break;
			}
		}else {
			ConsoleOutput.printMessageInConsole("DELETED GAME "+getId()+" ["+getType()+"] BECAUSE EVERYBODY HAS LEFT ("+getPlayerCount()+")", true);
			ConsoleOutput.printMessageInConsole(getId(), "DELETED GAME "+getId()+" ["+getType()+"] BECAUSE EVERYBODY HAS LEFT ("+getPlayerCount()+")", true);
		}
		
		//REMOVE FROM SESSION SCREEN
		if(ConsoleData.focusedGameID == this.getId()) {
			ConsoleData.focusedGameID = -1;
			ConsoleOutput.printBlankLineInConsole();
			ConsoleOutput.printMessageInConsole("Terminated game session for game ["+this.getId()+"]", true);
			ConsoleOutput.printBlankLineInConsole();
		}
		
		this.getPlayerList().clear();
		ServerGameData.runningGames.remove(thisGet());
		
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//PLAYER
	public void addPlayer(ServerPlayer player) {
		
		//FINAL ADD AFTER SYNC
		this.playerList.add(player);
		player.addToGame(this, this.getPlayerCount());
		
	}
	public boolean removePlayer(int playerId) {
		
		ServerPlayer player = this.getPlayerById(playerId);
		if(player != null) {
			if(this.isRunning() == false) {
				//UPDATE OTHER PLAYER POS IF GAME IS NOT RUNNING YET
				
				this.playerList.remove(player);
				player.removeFromGame();
				for(ServerPlayer otherPlayer : this.playerList) {
					if(otherPlayer.getPositionInGame() > player.getPositionInGame()) {
						otherPlayer.decreasePositionInGame();
					}
				}
				
			}else if(this.status == GameStatus.INGAME) {
				//GAME IS RUNNING AND NOT FINISHED - CHECK ENDING
				
				disconnectedPlayer.put(player, ServerGameData.reconnectTimeSec);
				if(player.isRoundReady()) { //IF READY, SET UNREADY
					player.resetRoundReady();
					this.playerIsRound_UN_Ready(player.getId());
				}
				startReconnectTimer();
				ConsoleOutput.printMessageInConsole(getId(), "PLAYER ("+player.getId()+":"+player.getProfile().getName()+") DISCONNECTED", true);
				this.sendDataToAllGamePlayer(695, ""+player.getId()+";"+ServerGameData.reconnectTimeSec);
				
			}else {
				//IS RUNNING, BUT NOT INGAME -> FINISHED
				
				this.playerList.remove(player);
				player.removeFromGame();
				
			}
			
			player.getProfile().setCurrentActivity("Online"); //Back in menu aka "online"
			
			return true;
		}
		
		return false;
	}
	public void reconnect_start(ServerPlayer player) {
		
		//SIND INFO ABOUT SYNC IF NOT SEND ALREADY
		if(awaitReconnect.contains(player) == false) {
			player.getProfile().getConnection().sendData(696, ""+player.getId());
		}
		
		if(isRoundChange == true) {
			//Currently round change, so wait with reconnect after finish
			awaitReconnect.add(player);
			ConsoleOutput.printMessageInConsole(getId(), "PLAYER ("+player.getId()+":"+player.getProfile().getName()+") AWAITS RECONNECT", true);
			return;
		}
		
		ConsoleOutput.printMessageInConsole(getId(), "PLAYER ("+player.getId()+":"+player.getProfile().getName()+") IS RECONNECTING...", true);
		
		//START GAME PROGRESS SYNCING - Start with general Data (Will start a ping pong with client over multiple loading steps)
		player.getProfile().getConnection().sendData(698, this.getGameData());
		
	}
	
	public void reconnect_generalDone(ServerPlayer player) {
		
		//699 - roundNumber / executeID
		player.getProfile().getConnection().sendData(699, this.getRoundNumber()+";"+this.executeID);
			
	}
	
	public void reconnect_metaDone(ServerPlayer player) {
		
		//700 - Actions
		for(GameAction action : this.actionLog) {
			player.getProfile().getConnection().sendData(700, action.getData());
		}
			
	}

	public void reconnect_actionDone(ServerPlayer player) {
		
		//697/697 - sync finished
		player.getProfile().setCurrentActivity(this.statusDescription);
		player.getProfile().getConnection().sendData(697, "Reconnect data complete!");
		this.sendDataToAllGamePlayer(696, ""+player.getId()); //THIS GETS NOT SEND TO RECONNECTED PLAYER (not added to list yet)
		
		//Finaly add him to game fully
		for(int i = 0 ; i < this.playerList.size() ; i++) {
			ServerPlayer p = this.playerList.get(i);
			if(p.getId() == player.getId()) {
				
				//ADD BACK TO GAME AT SAME POS
				this.playerList.add(i, player); //INSERT NEW PLAYER AS REPLACEMENT TO OLD ONE AT SAME POSITION
				player.addToGame(this, i+1); //SET THIS GAME IN THE PLAYER
				this.playerList.remove(i+1); //THEN REMOVE OLD PLAYER WHICH GOT PUSHED TO THE RIGHT BY ONE
				disconnectedPlayer.remove(p); //REMOVE OLD PLAYER FROM RECONNECT LIST
				ConsoleOutput.printMessageInConsole(getId(), "PLAYER ("+player.getId()+":"+player.getProfile().getName()+") RECONNECTED", true);
				break;
				
			}
		}
		
	}
	
//==========================================================================================================
	/**
	 * Spectate section
	 */
	public void addSpectator(ServerPlayer spectator) {
		
		if(!this.spectator.contains(spectator)) {
			this.spectator.add(spectator);
		}else {
			ConsoleOutput.printMessageInConsole("Tried adding new spectator, which was already added - Skipping and continue...", true);
		}
			
	}
	public void syncSpectators() {
		
		if(this.spectator.isEmpty()) { return; }
		
		//Just debug if needed
		/*for(ServerPlayer spec : this.spectator) {
			ConsoleOutput.printMessageInConsole("Sending to spec ["+(this.roundNumber-1)+"]: "+spec.getId()+" | "+spec.getProfile().getName(), false);
		}*/
		
		//Send the last round actions to all spectators
		for(GameAction action : this.actionLog) {
			if(action.round == this.roundNumber-1) {
				for(ServerPlayer spec : this.spectator) {
					if(spec.getProfile().getConnection() != null) {
						spec.getProfile().getConnection().sendData(752, action.getData());
					}else {
						this.spectator.remove(spec); //No con = offline = no spec
					}
				}
			}
		}
		
	}
	public void removeSpectator(ServerPlayer spectator) {
		
		this.spectator.remove(spectator);
		
	}
	
//==========================================================================================================
	/**
	 * Starts the timer which checks if a client exceeded its reconnect time
	 */
	private void startReconnectTimer() {
		if(this.reconnectTimer == null) {
			this.reconnectTimer = new Timer();
			this.reconnectTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					
					if(disconnectedPlayer.isEmpty()) { stopReconnectTimer(); }
					
					try {
						for(ServerPlayer player : disconnectedPlayer.keySet()) {
							int sec = disconnectedPlayer.get(player);
							
							if(sec == 1) {
								//NO MORE RECONNECT AWAITING
								finishGame(player.getId(), GameFinishCause.DISCONNECT);
								stopReconnectTimer();
								break;
							}else {
								disconnectedPlayer.replace(player, sec, sec-1);
							}
						}
					}catch(ConcurrentModificationException error) {}
					
				}
			}, 0, 1000);
		}
	}
	private void stopReconnectTimer() {
		if(this.reconnectTimer != null) {
			this.reconnectTimer.cancel();
			this.reconnectTimer = null;
		}
	}
	
	public ServerPlayer getPlayerById(int playerId) {
		for(ServerPlayer player : this.playerList) {
			if(player.getId() == playerId) {
				return player;
			}
		}
		return null;
	}
	public ServerPlayer getPlayerByPos(int playerPosition) {
		for(ServerPlayer player : this.playerList) {
			if(player.getPositionInGame() == playerPosition) {
				return player;
			}
		}
		return null;
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//SENDING
	public void sendDataToAllGamePlayer(int signal, String data) {
		for(ServerPlayer player : this.playerList) {
			if(player != null && player.getProfile() != null && player.getProfile().getConnection() != null) {
				player.getProfile().getConnection().sendData(signal, data);
			}else if(!this.disconnectedPlayer.containsKey(player)) {
				ConsoleOutput.printMessageInConsole("Couldn't send package to player! ("+this.id+"-"+(player!=null?player.getId():"null")+"-"+signal+"|"+data+")", true);
			}
		}
	}
	
//==========================================================================================================
	/**
	 * Starts the timer which checks whether all clients are round ready
	 */
	private void startPlayerReadyTimer() {
		if(this.playerReadyTimer == null) {
			this.playerReadyTimer = new Timer();
			this.playerReadyTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					
					int readyPlayer = 0;
					for(ServerPlayer player : playerList) {
						if(player.isRoundReady()) {
							readyPlayer++;
						}
					}
					if(readyPlayer == getPlayerCount() && disconnectedPlayer.isEmpty()) {
						//ALL READY AND NO ONE DISCONNECTED
						stopPlayerReadyTimer();
						for(ServerPlayer player : playerList) {
							player.resetRoundReady();
						}
						sendDataToAllGamePlayer(652, "All clients are ready!");
						ConsoleOutput.printMessageInConsole(getId(), "All player are ready! ("+readyPlayer+"/"+getPlayerCount()+")", true);
						isRoundChange = true;
						startSendWaitTimer();
					}
					
				}
			}, 0, 1000);
		}
	}
	private void stopPlayerReadyTimer() {
		if(this.playerReadyTimer != null) {
			this.playerReadyTimer.cancel();
			this.playerReadyTimer = null;
		}
	}
		
//==========================================================================================================
	/**
	 * Adds a ready player for this round
	 */
	public void playerIsRoundReady(int playerID) {
		
		sendDataToAllGamePlayer(650, ""+playerID);
		
	}
//==========================================================================================================
	/**
	 * Removes a ready player for this round
	 */
	public void playerIsRound_UN_Ready(int playerID) {
		
		sendDataToAllGamePlayer(651, ""+playerID);
		
	}
//==========================================================================================================
	/**
	 * Starts the timer which checks whether all clients has send all their tasks
	 */
	private void startSendWaitTimer() {
		if(this.sendTaskWaitTimer == null) {
			this.sendTaskWaitTimer = new Timer();
			this.sendTaskWaitTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					
					int readyPlayer = 0;
					for(ServerPlayer player : playerList) {
						if(player.hasSendAllTask()) {
							readyPlayer++;
						}
					}
					if(readyPlayer == getPlayerCount()) {
						stopSendWaitTimer();
						int totalTasks = 0;
						for(ServerPlayer player : playerList) {
							player.resetSendAllTasks();
							totalTasks += player.getCurrendRoundLogs().size();
						}
						sendDataToAllGamePlayer(653, "All tasks transfered!");
						ConsoleOutput.printMessageInConsole(getId(), "All tasks transfered! (Total: "+totalTasks+")", true);
						executeRoundTasks();
						handleRemoveActionsOnRoundChange();
						startExecWaitTimer();
					}
					
				}
			}, 0, 1000);
		}
	}
	private void stopSendWaitTimer() {
		if(this.sendTaskWaitTimer != null) {
			this.sendTaskWaitTimer.cancel();
			this.sendTaskWaitTimer = null;
		}
	}
	
//==========================================================================================================
	/**
	 * Simulates all tasks so the server keeps up with the current game state
	 */
	private void executeRoundTasks() {
		
		int currentActivePlayerPos = currentFirstPlayerPos;
		List<GameActionType> actionsOrder = getActionOrder();
		
		for(GameActionType currentActionType : actionsOrder) {
			while(true) {
				ServerPlayer currentActivePlayer = playerList.get(currentActivePlayerPos);
				List<GameAction> actionsToExecute = getActionsByPlayerAndType(currentActionType, currentActivePlayer);
//				ConsoleOutput.printMessageInConsole(getId(), "	Executing "+currentActivePlayer.getId()+" (Pos: "+currentActivePlayerPos+") - "+currentActionType+" actions (Total: "+actionsToExecute.size()+")", true);
				
				if(actionsToExecute.isEmpty() == false) {
					switch(currentActionType) {
					case ATTACK:
						for(GameAction action : actionsToExecute) {
							execAttack(action);
						}
						break;
					case HEAL:
						for(GameAction action : actionsToExecute) {
							execHeal(action);
						}
						break;
					case BUILD: //SINGLE TARGET ACTION
						for(GameAction action : actionsToExecute) {
							Field field = new Field(null, action.x, action.y);
							if(checkListContainsFieldCoordinate(this.usedFieldsForThisRound, field)) {
								//BLOCKED
								blockedActionsForThisRound++;
								ConsoleOutput.printMessageInConsole(getId(), "	Action Build "+action.text+" was BLOCKED on "+action.x+":"+action.y+" by "+action.playerId, true);
							}else {
								execBuild(action);
								this.usedFieldsForThisRound.add(field);
							}
						}
						break;
					case PRODUCE: //SINGLE TARGET ACTION
						for(GameAction action : actionsToExecute) {
							Field field = new Field(null, action.newX, action.newY);
							if(checkListContainsFieldCoordinate(this.usedFieldsForThisRound, field)) {
								//BLOCKED
								blockedActionsForThisRound++;
								ConsoleOutput.printMessageInConsole(getId(), "	Action Produce "+action.text+" was BLOCKED on "+action.newX+":"+action.newY+" by "+action.playerId, true);
							}else {
								execProduce(action);
								this.usedFieldsForThisRound.add(field);
							}
						}
						break;
					case UPGRADE:
						for(GameAction action : actionsToExecute) {
							execUpgrade(action);
						}
						break;
					case MOVE: //SINGLE TARGET ACTION
						for(GameAction action : actionsToExecute) {
							Field field = new Field(null, action.newX, action.newY);
							if(checkListContainsFieldCoordinate(this.usedFieldsForThisRound, field)) {
								//BLOCKED
								blockedActionsForThisRound++;
								ConsoleOutput.printMessageInConsole(getId(), "	Action Move from "+action.x+":"+action.y+" was BLOCKED on "+action.newX+":"+action.newY+" by "+action.playerId, true);
							}else {
								execMove(action);
								this.usedFieldsForThisRound.add(field);
							}
						}
						break;
					case REMOVE:
						for(GameAction action : actionsToExecute) {
							execRemove(action);
						}
						break;
					default:
						break;
					}
				}
				
				currentActivePlayerPos = getNextExecutePlayerPos(currentActivePlayerPos);
				if(currentActivePlayerPos == currentFirstPlayerPos) { break; }
			}
		}
		
		//UPDATE FIRSTPLAYERPOS FOR NEXT ROUND
		this.currentFirstPlayerPos = getNextExecutePlayerPos(this.currentFirstPlayerPos);
		
	}
	
	private List<GameAction> getActionsByPlayerAndType(GameActionType type, ServerPlayer player) {
		
		List<GameAction> actions = new ArrayList<GameAction>();
		
		for(GameAction action : player.getCurrendRoundLogs()) {
			if(action.type == type) {
				actions.add(action);
			}
		}
		
		return actions;
		
	}
	
	private int getNextExecutePlayerPos(int lastPlayerPos) {
		if(GameType.isModus1v1(this.type)) {
			//1v1
			switch(lastPlayerPos) {
			case 0: return 1;
			case 1: return 0;
			}
		}else {
			//2v2
			switch(lastPlayerPos) {
			case 0: return 1;
			case 1: return 2;
			case 2: return 3;
			case 3: return 0;
			}
		}
		return -1;
	}
	
	private List<GameActionType> getActionOrder() {
		List<GameActionType> actionsOrder = new ArrayList<GameActionType>();
		actionsOrder.add(GameActionType.ATTACK);
		actionsOrder.add(GameActionType.HEAL);
		actionsOrder.add(GameActionType.BUILD);
		actionsOrder.add(GameActionType.PRODUCE);
		actionsOrder.add(GameActionType.UPGRADE);
		actionsOrder.add(GameActionType.MOVE);
		actionsOrder.add(GameActionType.REMOVE);
		return actionsOrder;
	}
	
	private boolean checkListContainsFieldCoordinate(List<Field> list, Field target) {
		
		for(Field field : list) {
			if(field.X == target.X && field.Y == target.Y) {
				//HIT
				return true;
			}
		}
		return false;
		
	}
	
//==========================================================================================================
	/**
	 * Starts the timer which checks whether all clients has executed all their tasks
	 */
	private void startExecWaitTimer() {
		if(this.execTaskWaitTimer == null) {
			this.execTaskWaitTimer = new Timer();
			this.execTaskWaitTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					
					int readyPlayer = 0;
					for(ServerPlayer player : playerList) {
						if(player.hasExecAllTask()) {
							readyPlayer++;
						}
					}
					if(readyPlayer == getPlayerCount()) {
						stopExecWaitTimer();
						for(ServerPlayer player : playerList) {
							player.resetExecAllTasks();
							player.resetCurrentRoundLog(); //RESET ROUND LOGS AS WELL
						}
						sendDataToAllGamePlayer(654, "All tasks executed!");
						ConsoleOutput.printMessageInConsole(getId(), "All tasks executed! (Blocked: "+blockedActionsForThisRound+")", true);
						usedFieldsForThisRound.clear();
						blockedActionsForThisRound = 0;
						//Add round end action (does nothing but ensures that at least one action per round exists for simulating round changes), also displays round changes for spectate and replay
						GameAction roundEndAction = new GameAction(-1, GameActionType.ROUND_END, roundNumber, "Round "+roundNumber+" finished", -1);
						roundEndAction.setExecuteID(executeID++);
						actionLog.add(roundEndAction);
						roundNumber++;
						ConsoleOutput.printMessageInConsole(getId(), "# NEXT ROUND (ID: "+id+" ; Round: "+roundNumber+")", true);
						isRoundChange = false;
						//SPECTATOR SYNC
						syncSpectators();
						//RECONNECT WAITING ONES
						for(ServerPlayer p : awaitReconnect) {
							reconnect_start(p);
						}
						awaitReconnect.clear();
						startPlayerReadyTimer();
					}
					
				}
			}, 0, 1000);
		}
	}
	private void stopExecWaitTimer() {
		if(this.execTaskWaitTimer != null) {
			this.execTaskWaitTimer.cancel();
			this.execTaskWaitTimer = null;
		}
	}
	
//==========================================================================================================
	/**
	 * Starts the timer which checks whether all clients has send all their tasks
	 */
	private void startPingUpdateTimer() {
		if(this.pingUpdateTimer == null) {
			this.pingUpdateTimer = new Timer();
			this.pingUpdateTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					
					sendDataToAllGamePlayer(801, ""+System.currentTimeMillis());
					
				}
			}, 0, 1000*3);
		}
	}
	private void stopPingUpdateTimer() {
		if(this.pingUpdateTimer != null) {
			this.pingUpdateTimer.cancel();
			this.pingUpdateTimer = null;
		}
	}
	
//==========================================================================================================
	/**
	 * Send a chatMessage to all players
	 * @param senderName - String - The name of the sender
	 * @param message - String - The message witch should be sent
	 */
	public void sendChatMessage(int senderID, String senderName, String message) {
		
		String chatMessage = null;
		if(senderName == null) {
			chatMessage = message;
		}else {
			chatMessage = senderName+": "+message;
		}
		
		// chatMessageNumber ; message
		sendDataToAllGamePlayer(660, chatMessageNumber+";"+chatMessage);
		GameAction chatAction = new GameAction(senderID, GameActionType.CHATMESSAGE, this.roundNumber, chatMessage, chatMessageNumber);
		chatAction.setExecuteID(this.executeID++);
		this.actionLog.add(chatAction);
		
		chatMessageNumber++;
		
	}
//==========================================================================================================
	/**
	 * Send a fieldPing to all players (Only visible for allied players of the sender)
	 * @param pingerID - Integer - The ID of the player who pinged the field
	 * @param x - Integer - The x coordinate of the field
	 * @param y - Integer - The y coordinate of the field
	 */
	public void sendFieldPing(int pingerID, int x, int y) {
		
		String data = pingerID+";"+x+";"+y;
		sendDataToAllGamePlayer(661, data);
		GameAction pingAction = new GameAction(pingerID, GameActionType.FIELDPING, this.roundNumber, x, y, -1, -1, -1);
		pingAction.setExecuteID(this.executeID++);
		this.actionLog.add(pingAction);
	}
	
//==========================================================================================================
	/**
	 * Send a ping update of a player to all other player
	 * @param pingID - Integer - The ID of the player
	 * @param ping - Integer - The ping of the player
	 */
	public void updatePlayerPing(int pingerID, int ping) {
		
		try {
			String data = pingerID+";"+ping;
			sendDataToAllGamePlayer(801, data);
		}catch(IndexOutOfBoundsException | ConcurrentModificationException error) {}
		
	}
	
	/**
	 * Used to sync researched upgrades between players, spectators and the round log
	 * @param playerID - Integer - The ID of the player, who researched the upgrade
	 * @param upgradeName - String - The name of the unlocked upgrade
	 */
	public void updateUpgradeResearch(int playerID, String upgradeName) {
		
		sendDataToAllGamePlayer(610, playerID+";"+upgradeName);
		this.research.add(new ServerResearch(playerID, upgradeName, this.roundNumber));
		GameAction researchAction = new GameAction(playerID, GameActionType.RESEARCH, this.roundNumber, upgradeName, 0);
		researchAction.setExecuteID(this.executeID++);
		this.actionLog.add(researchAction);
		ConsoleOutput.printMessageInConsole(getId(), "	UPGRADE RESEARCH "+upgradeName+" by "+playerID, true);
		
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//ACTIONS
	public void addBuild(ServerPlayer player, String name, int x, int y) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.BUILD, this.roundNumber, name, x, y, -1, -1, -1));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Build "+name+" on "+x+":"+y+" by "+player.getId(), true);
	}
	public void execBuild(GameAction action) {
		action.setExecuteID(this.executeID++);
		ServerBuilding building = new ServerBuilding(action.x, action.y, action.text, action.playerId);
		this.buildings.add(building);
		this.actionLog.add(action);
		ConsoleOutput.printMessageInConsole(getId(), "	EXEC Build "+action.text+" on "+action.x+":"+action.y+" by "+action.playerId, true);
	}
	public void addProduce(ServerPlayer player, String name, int fromX, int fromY, int targetX, int targetY) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.PRODUCE, this.roundNumber, name, fromX, fromY, targetX, targetY, -1));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Produce "+name+" on "+targetX+":"+targetY+" by "+player.getId(), true);
	}
	private void execProduce(GameAction action) {
		action.setExecuteID(this.executeID++);
		ServerTroup troup = new ServerTroup(action.newX, action.newY, action.text, action.playerId);
		this.troups.add(troup);
		this.actionLog.add(action);
		ConsoleOutput.printMessageInConsole(getId(), "	EXEC Produce "+action.text+" on "+action.newX+":"+action.newY+" by "+action.playerId, true);
	}
	public void addAttack(ServerPlayer player, int fromX, int fromY, int targetX, int targetY, int amount) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.ATTACK, this.roundNumber, fromX, fromY, targetX, targetY, amount));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Attack ("+amount+") from "+fromX+":"+fromY+" on "+targetX+":"+targetY+" by "+player.getId(), true);
	}
	private void execAttack(GameAction action) {
		action.setExecuteID(this.executeID++);
		ServerBuilding building = getBuilding(action.newX, action.newY);
		if(building != null) {
			//BUILDING
			building.health -= action.amount;
			this.actionLog.add(action);
			ConsoleOutput.printMessageInConsole(getId(), "	EXEC Attack-B ("+action.amount+") from "+action.x+":"+action.y+" on "+action.newX+":"+action.newY+" by "+action.playerId, true);
			if(building.health <= 0) {
				//DEAD
				this.toRemoveBuildings.add(building);
			}
		}else {
			ServerTroup troup = getTroup(action.newX, action.newY);
			if(troup != null) {
				//TROUP
				troup.health -= action.amount;
				this.actionLog.add(action);
				ConsoleOutput.printMessageInConsole(getId(), "	EXEC Attack-T ("+action.amount+") from "+action.x+":"+action.y+" on "+action.newX+":"+action.newY+" by "+action.playerId, true);
				if(troup.health <= 0) {
					//DEAD
					this.toRemoveTroups.add(troup);
				}
			}else {
				//NONE
				ConsoleOutput.printMessageInConsole("EXEC DAMAGE in game "+this.id+" from "+action.playerId+" found no troup or building at "+action.newX+":"+action.newY+"!", true);
			}
		}
	}
	public void addMove(ServerPlayer player, int oldX, int oldY, int newX, int newY) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.MOVE, this.roundNumber, oldX, oldY, newX, newY, -1));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Move from "+oldX+":"+oldY+" to "+newX+":"+newY+" by "+player.getId(), true);
	}
	private void execMove(GameAction action) {
		action.setExecuteID(this.executeID++);
		ServerTroup troup = getTroup(action.x, action.y);
		troup.X = action.newX;
		troup.Y = action.newY;
		this.actionLog.add(action);
		ConsoleOutput.printMessageInConsole(getId(), "	EXEC Move "+troup.name+" from "+action.x+":"+action.y+" to "+action.newX+":"+action.newY+" by "+action.playerId, true);
	}
	public void addUpgrade(ServerPlayer player, String name, int fromX, int fromY, int targetX, int targetY) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.UPGRADE, this.roundNumber, name, fromX, fromY, targetX, targetY, -1));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Upgrade from "+fromX+":"+fromY+" on "+targetX+":"+targetY+" to "+name+" by "+player.getId(), true);
	}
	private void execUpgrade(GameAction action) {
		action.setExecuteID(this.executeID++);
		//CHECK OLD TROUPS
		ServerTroup troup1 = getTroup(action.x, action.y);
		ServerTroup troup2 = getTroup(action.newX, action.newY);
		if(troup1 == null || troup2 == null) { ConsoleOutput.printMessageInConsole("UPGRADE in game "+this.id+" from "+action.playerId+" found no troup(s) at "+action.x+":"+action.y+" or "+action.newX+":"+action.newY+"!", true); return; }
		//REMOVE OLD TROUPS
		this.troups.remove(troup1);
		this.troups.remove(troup2);
		//ADD NEW TROUP
		ServerTroup troup = new ServerTroup(action.newX, action.newY, action.text, action.playerId);
		this.troups.add(troup);
		this.actionLog.add(action);
		ConsoleOutput.printMessageInConsole(getId(), "	EXEC Upgrade from "+action.x+":"+action.y+" on "+action.newX+":"+action.newY+" to "+action.text+" by "+action.playerId, true);
	}
	public void addHeal(ServerPlayer player, int fromX, int fromY, int targetX, int targetY, int amount) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.HEAL, this.roundNumber, fromX, fromY, targetX, targetY, amount));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Heal ("+amount+") on "+targetX+":"+targetY+" by "+player.getId(), true);
	}
	private void execHeal(GameAction action) {
		action.setExecuteID(this.executeID++);
		ServerBuilding building = getBuilding(action.newX, action.newY);
		if(building != null) {
			//BUILDING
			if(building.health + action.amount > building.startHealth) { action.amount = building.startHealth-building.health; }
			building.health += action.amount;
			this.actionLog.add(action);
			ConsoleOutput.printMessageInConsole(getId(), "	EXEC Heal-B ("+action.amount+") on "+action.newX+":"+action.newY+" by "+action.playerId, true);
		}else {
			ServerTroup troup = getTroup(action.newX, action.newY);
			if(troup != null) {
				//TROUP
				if(troup.health + action.amount > troup.startHealth) { action.amount = troup.startHealth-troup.health; }
				troup.health += action.amount;
				this.actionLog.add(action);
				ConsoleOutput.printMessageInConsole(getId(), "	EXEC Heal-T ("+action.amount+") on "+action.newX+":"+action.newY+" by "+action.playerId, true);
			}else {
				//NONE
				ConsoleOutput.printMessageInConsole("EXEC HEAL in game "+this.id+" from "+action.playerId+" found no troup or building at "+action.newX+":"+action.newY+" to heal!", true);
			}
		}
		
	}
	public void addRemove(ServerPlayer player, int x, int y) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.REMOVE, this.roundNumber, x, y, -1, -1, -1));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Remove from "+x+":"+y+" by "+player.getId(), true);
	}
	private void execRemove(GameAction action) {
		action.setExecuteID(this.executeID++);
		ServerBuilding building = getBuilding(action.x, action.y);
		if(building != null) {
			//BUILDING
			this.buildings.remove(building);
			this.actionLog.add(action);
			ConsoleOutput.printMessageInConsole(getId(), "	EXEC Remove-B from "+action.x+":"+action.y+" by "+action.playerId, true);
		}else {
			ServerTroup troup = getTroup(action.x, action.y);
			if(troup != null) {
				//TROUP
				this.troups.remove(troup);
				this.actionLog.add(action);
				ConsoleOutput.printMessageInConsole(getId(), "	EXEC Remove-T from "+action.x+":"+action.y+" by "+action.playerId, true);
			}else {
				//NONE
				ConsoleOutput.printMessageInConsole("EXEC REMOVE in game "+this.id+" from "+action.playerId+" found no troup or building at "+action.x+":"+action.y+"!", true);
			}
		}
	}
	private void handleRemoveActionsOnRoundChange() {
		
		int deaths = 0;
		int hqDestroyedID = -1;
		
		for(ServerBuilding building : this.toRemoveBuildings) {
			this.buildings.remove(building);
			GameAction deathAction = new GameAction(building.playerId, GameActionType.DEATH, this.roundNumber, building.X, building.Y, -1, -1, -1);
			deathAction.setExecuteID(this.executeID++);
			this.actionLog.add(deathAction);
			ConsoleOutput.printMessageInConsole(getId(), "	Death-B on "+building.X+":"+building.Y+" by "+building.playerId, true);
			deaths++;
			if(building.name.equalsIgnoreCase("Headquarter")) {
				if(hqDestroyedID == -1) {
					//NO HQ DESTROYED YET
					hqDestroyedID = building.playerId;
				}else {
					//SAME ROUND ANOTHER HQ DESTROYED
					hqDestroyedID = -99;
				}
			}
		}
		this.toRemoveBuildings.clear();
		
		for(ServerTroup troup : this.toRemoveTroups) {
			this.troups.remove(troup);
			GameAction deathAction = new GameAction(troup.playerId, GameActionType.DEATH, this.roundNumber, troup.X, troup.Y, -1, -1, -1);
			deathAction.setExecuteID(this.executeID++);
			this.actionLog.add(deathAction);
			ConsoleOutput.printMessageInConsole(getId(), "	Death-T on "+troup.X+":"+troup.Y+" by "+troup.playerId, true);
			deaths++;
		}
		this.toRemoveTroups.clear();
		
		ConsoleOutput.printMessageInConsole(getId(), "	Deaths executed (Total: "+deaths+")", true);
		
		//CHECK FOR HQ DESTROYED
		if(hqDestroyedID == -99) {
			//GAME DRAW
			this.finishGame(-99, GameFinishCause.DRAW);
		}else if(hqDestroyedID != -1) {
			//WINNER WINNER, CHICKEN DINNER
			this.finishGame(hqDestroyedID, GameFinishCause.HQ_DESTROY);
		}
		
	}

//------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//GAME FINISH
	public void requestSurrender(ServerPlayer requester) {
		for(ServerPlayer player : this.getAlliedPlayers(requester)) {
			if(player.getId() != requester.getId()) {
				//ALLIED, NOT THE REQUESTER
				player.getProfile().getConnection().sendData(691, "Requested surrender");
			}
		}
	}
	
	
	public void finishGame(int loserID, GameFinishCause cause) {
		
		ConsoleOutput.printMessageInConsole("GAME "+this.id+" FINISHED! Cause: "+cause, true);
		ConsoleOutput.printMessageInConsole(this.getId(), "GAME "+this.id+" FINISHED! Cause: "+cause+" - Loser: "+loserID, true);
		
		this.status = GameStatus.FINISHED;
		
		this.stopExecWaitTimer();
		this.stopPlayerReadyTimer();
		this.stopSendWaitTimer();
		this.stopPingUpdateTimer();
		this.stopReconnectTimer();
		
		ServerGameData.runningGames.remove(this);
		ServerGameData.oldFinishedGames.add(this);
		
		if(cause == GameFinishCause.DRAW) {
			//NO WINNER / LOSER
			
			//TODO SEND 690 GAME FINISH WITH ALL DATA
			
		}else {
			//WITH WINNER / LOSER
			List<ServerPlayer> winner = getEnemyPlayers(loserID);
			List<ServerPlayer> loser = getAlliedPlayers(loserID);
			
			//TODO SEND 690 GAME FINISH WITH ALL DATA
			
			//TODO CALCULATE RP (ONLY IF RANKED) AND LEVEL GAINS/LOSES AND SEND IT TO EACH PLAYER WITH HIS DATA
			
		}
		
		if(ConsoleData.focusedGameID == this.id) {
			ConsoleData.focusedGameID = -1;
			ConsoleOutput.printMessageInConsole("Terminated game session for game ["+this.id+"]", true);
			ConsoleOutput.printBlankLineInConsole();
		}
		
	}
	
	
//------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public ServerBuilding getBuilding(int x, int y) {
		for(ServerBuilding building : this.buildings) {
			if(building.X == x && building.Y == y) {
				return building;
			}
		}
		return null;
	}
	public ServerTroup getTroup(int x, int y) {
		for(ServerTroup troup : this.troups) {
			if(troup.X == x && troup.Y == y) {
				return troup;
			}
		}
		return null;
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//GETTER
	public String getInfoAsString() {
		if(this.type == GameType.Custom_1v1 || this.type == GameType.Custom_2v2) {
			return "["+this.id+"] "+this.type+" - Running: "+this.isRunning+" - PlayerCount: "+this.getPlayerCount()+" ("+this.disconnectedPlayer.size()+") - RoundNumber: "+this.roundNumber+" - Actions: "+this.actionLog.size()+" - Map: "+this.map.name+" - Host: "+this.host.getProfile().getName();
		}else {
			return "["+this.id+"] "+this.type+" - Running: "+this.isRunning+" - PlayerCount: "+this.getPlayerCount()+" ("+this.disconnectedPlayer.size()+") - RoundNumber: "+this.roundNumber+" - Actions: "+this.actionLog.size()+" - Map: "+this.map.name;
		}
	}
	
	public List<ServerPlayer> getAlliedPlayers(ServerPlayer player) { return getAlliedPlayers(player.getId()); }
	public List<ServerPlayer> getAlliedPlayers(int playerID) {
		
		if(this.type != null) {
			List<ServerPlayer> output = new ArrayList<>();
			if(GameType.isModus1v1(this.type)) {
				//1v1
				if(this.playerList.get(0).getId() == playerID) {
					//Player1
					output.add(this.playerList.get(0));
				}else if(this.playerList.get(1).getId() == playerID) {
					//Player2
					output.add(this.playerList.get(1));
				}else {
					ConsoleOutput.printMessageInConsole("Getting allied player found no matching player! (1v1 - ID: "+playerID+")", true);
				}
			}else {
				//2v2
				if(this.playerList.get(0).getId() == playerID) {
					//Player1
					output.add(this.playerList.get(0));
					output.add(this.playerList.get(1));
				}else if(this.playerList.get(1).getId() == playerID) {
					//Player2
					output.add(this.playerList.get(0));
					output.add(this.playerList.get(1));
				}else if(this.playerList.get(2).getId() == playerID) {
					//Player3
					output.add(this.playerList.get(2));
					output.add(this.playerList.get(3));
				}else if(this.playerList.get(3).getId() == playerID) {
					//Player4
					output.add(this.playerList.get(2));
					output.add(this.playerList.get(3));
				}else {
					ConsoleOutput.printMessageInConsole("Getting allied player found no matching player! (2v2 - ID: "+playerID+")", true);
				}
			}
			return output;
		}
		return null;
		
	}
	public List<ServerPlayer> getEnemyPlayers(ServerPlayer player) { return getEnemyPlayers(player.getId()); }
	public List<ServerPlayer> getEnemyPlayers(int playerID) {
		
		if(this.type != null) {
			List<ServerPlayer> output = new ArrayList<>();
			if(GameType.isModus1v1(this.type)) {
				//1v1
				if(this.playerList.get(0).getId() == playerID) {
					//Player1
					output.add(this.playerList.get(1));
				}else if(this.playerList.get(1).getId() == playerID) {
					//Player2
					output.add(this.playerList.get(0));
				}else {
					ConsoleOutput.printMessageInConsole("Getting enemy player found no matching player! (1v1 - ID: "+playerID+")", true);
				}
			}else {
				//2v2
				if(this.playerList.get(0).getId() == playerID) {
					//Player1
					output.add(this.playerList.get(2));
					output.add(this.playerList.get(3));
				}else if(this.playerList.get(1).getId() == playerID) {
					//Player2
					output.add(this.playerList.get(2));
					output.add(this.playerList.get(3));
				}else if(this.playerList.get(2).getId() == playerID) {
					//Player3
					output.add(this.playerList.get(0));
					output.add(this.playerList.get(1));
				}else if(this.playerList.get(3).getId() == playerID) {
					//Player4
					output.add(this.playerList.get(0));
					output.add(this.playerList.get(1));
				}else {
					ConsoleOutput.printMessageInConsole("Getting enemy player found no matching player! (2v2 - ID: "+playerID+")", true);
				}
			}
			return output;
		}
		return null;
		
	}
	
	public ServerPlayer getHost() {
		return host;
	}
	public GameType getType() {
		return type;
	}
	public List<GameAction> getActionLog() {
		return actionLog;
	}
	public int getRoundNumber() {
		return roundNumber;
	}
	public int getExecuteID() {
		return executeID;
	}
	public boolean isRunning() {
		return isRunning;
	}
	public GameStatus getStatus() {
		return status;
	}
	public int getPlayerCount() {
		return this.playerList.size()-this.disconnectedPlayer.size();
	}
	public List<ServerBuilding> getBuildings() {
		return buildings;
	}
	public List<ServerTroup> getTroups() {
		return troups;
	}
	public List<ServerResearch> getResearch() {
		return research;
	}
	public List<ServerPlayer> getPlayerList() {
		return playerList;
	}
	public int getId() {
		return id;
	}
	
	private ServerGame thisGet() {
		return this;
	}
	
}
