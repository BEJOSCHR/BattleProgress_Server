package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import me.bejosch.battleprogress.server.Data.ConsoleData;
import me.bejosch.battleprogress.server.Data.ServerGameData;
import me.bejosch.battleprogress.server.Enum.GameActionType;
import me.bejosch.battleprogress.server.Enum.GameStatus;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Handler.ServerGameHandler;
import me.bejosch.battleprogress.server.Handler.ServerGroupHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Main.ServerConnection;

public class ServerGame {

	private int id;
	private GameType type;
	private Map map;
	
	private ServerPlayer host = null; //IS SET TO NULL FOR NONE CUSTOM GAME
	
	private GameStatus status = GameStatus.LOBBY;
	private int roundNumber = 1;
	private boolean isRunning = false;
	
	private Timer playerReadyTimer = null;
	private Timer sendTaskWaitTimer = null;
	private Timer execTaskWaitTimer = null;
	
	private List<ServerPlayer> playerList = new ArrayList<ServerPlayer>();
	private List<ServerBuilding> buildings = new ArrayList<ServerBuilding>();
	private List<ServerTroup> troups = new ArrayList<ServerTroup>();
	
	private int currentFirstPlayerPos = 0;
	private CopyOnWriteArrayList<GameAction> actionLog = new CopyOnWriteArrayList<GameAction>(); //ALL ACTIONS ARE LOGGED IN HERE
	//HAS TO BE COPYONWRITE BECAUSE ASYNC ADD FROM THE CONNECTIONS
	
	private int blockedActionsForThisRound = 0;
	private List<Field> usedFieldsForThisRound = new ArrayList<Field>();
	
	private int chatMessageNumber = 1;
	
	private List<ServerBuilding> toRemoveBuildings = new ArrayList<>();
	private List<ServerTroup> toRemoveTroups = new ArrayList<>();
	
	public ServerGame(GameType type, Map map) {
		// NORMAL / RANKED GAME - Created by the queue manager
		
		this.id = ServerGameHandler.getNewGameId();
		this.type = type;
		this.map = map;
		
		ServerGameData.runningGames.add(this);
		ConsoleOutput.printMessageInConsole("CREATED GAME "+getId()+" ["+getType()+"] ON MAP '"+map.name+"'", true);
		checkAllAccept();
	}
	public ServerGame(ServerPlayer host, Map map) {
		//CUSTOM GAME
		
		this.host = host;
		
		this.id = ServerGameHandler.getNewGameId();
		this.type = GameType.Custom_1v1;
		this.map = map;
		
		ServerGameData.runningGames.add(this);
		ConsoleOutput.printMessageInConsole("CREATED GAME "+getId()+" ["+getType()+"] ON MAP '"+map.name+"' WITH HOST '"+host.getProfile().getName()+"'", true);
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
	
	//START (queued) GAMEL
	public void startQueuedGame() {
		
		ConsoleOutput.printMessageInConsole("STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		ConsoleOutput.printMessageInConsole(getId(), "STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		
		this.isRunning = true;
		
		sendGameDataToPlayer();
		startPlayerReadyTimer();
		
	}
	
	//START (custom) GAME MANUEL
	public void startCustomGame() {
		
		ConsoleOutput.printMessageInConsole("STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		ConsoleOutput.printMessageInConsole(getId(), "STARTED GAME "+getId()+" ["+getType()+"] WITH "+getPlayerCount()+" PLAYER!", true);
		
		this.isRunning = true;
		
		sendGameDataToPlayer();
		startPlayerReadyTimer();
		
	}
	
	//SEND GAME DATA TO PLAYER
	public void sendGameDataToPlayer() {
		
		//GameID ; GameModus ; PID1 ; PID2 ; PID3 ; PID4 ; MapName ; MapData
		String data = null;
		if(GameType.isModus1v1(type)) {
			//1v1
			data = id+";"+type+";"+playerList.get(0).getId()+";"+playerList.get(1).getId()+";"+"-1"+";"+"-1"+";"+map.name+";"+map.convertFieldListIntoStringSyntax();
		}else {
			//2v2
			data = id+";"+type+";"+playerList.get(0).getId()+";"+playerList.get(1).getId()+";"+playerList.get(2).getId()+";"+playerList.get(3).getId()+";"+map.name+";"+map.convertFieldListIntoStringSyntax();
		}
		
		for(ServerPlayer player : this.getPlayerList()) {
			player.resetCurrentRoundLog();
			player.resetRoundReady();
			player.resetSendAllTasks();
			player.resetExecAllTasks();
			player.getProfile().getConnection().sendData(620, ServerConnection.getNewPacketId(), data);
		}
		
	}
	
	//CANCLE/DELETE GAME START
	public void deleteGame() {
		
		//STOP ALL TIMERS
		stopPlayerReadyTimer();
		stopSendWaitTimer();
		stopExecWaitTimer();
		
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
		}else {
			//SEND INGAME ABORT
			//TODO SEND PLAYER ABORT/LEAVE PACKET (so they switch to main menu
		}
		
		for(ServerPlayer player : this.getPlayerList()) {
			//REMOVE PLAYER FROM GAME
			player.removeFromGame();
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
			int positionOfPlayer = player.getPositionInGame();
			this.playerList.remove(player);
			player.removeFromGame();
			
			//UPDATE OTHER PLAYER POS IF GAME IS NOT RUNNING YET
			if(this.isRunning() == false) {
				for(ServerPlayer otherPlayer : this.playerList) {
					if(otherPlayer.getPositionInGame() > positionOfPlayer) {
						otherPlayer.decreasePositionInGame();
					}
				}
			}
			
			//CHECK ENDING
			if(this.getPlayerCount() == 2) {
				if(this.getType() == GameType.Normal_2v2 || this.getType() == GameType.Custom_2v2) {
					//2v2
					//TODO CHECK FOR 2v2 IF A WHOLE TEAM HAS LEFT!!! -> Check via game position number of the players
					//TODO IF YES THE REMAING TEAM WINS
				}
			}else if(this.getPlayerCount() == 1) {
				//TODO LAST PLAYER WINS
				//TODO
				ConsoleOutput.printMessageInConsole("ONLY ONE PLAYER ("+this.playerList.get(0).getProfile().getName()+") IS LEFT IN GAME "+getId()+" ["+getType()+"] ("+getPlayerCount()+")", true);
				ConsoleOutput.printMessageInConsole(getId(), "ONLY ONE PLAYER ("+this.playerList.get(0).getProfile().getName()+") IS LEFT IN GAME "+getId()+" ["+getType()+"] ("+getPlayerCount()+")", true);
			}else if(this.getPlayerCount() == 0) {
				//NO PLAYER
				this.deleteGame();
			}
			
			return true;
		}
		
		return false;
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
	public void sendDataToAllGamePlayer(int signal, int id, String data) {
		for(ServerPlayer player : this.playerList) {
			player.getProfile().getConnection().sendData(signal, id, data);
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
					if(readyPlayer == getPlayerCount()) {
						stopPlayerReadyTimer();
						for(ServerPlayer player : playerList) {
							player.resetRoundReady();
						}
						sendDataToAllGamePlayer(652, ServerConnection.getNewPacketId(), "All clients are ready!");
						ConsoleOutput.printMessageInConsole(getId(), "All player are ready! ("+readyPlayer+"/"+getPlayerCount()+")", true);
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
		
		sendDataToAllGamePlayer(650, ServerConnection.getNewPacketId(), ""+playerID);
		
	}
//==========================================================================================================
	/**
	 * Removes a ready player for this round
	 */
	public void playerIsRound_UN_Ready(int playerID) {
		
		sendDataToAllGamePlayer(651, ServerConnection.getNewPacketId(), ""+playerID);
		
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
						sendDataToAllGamePlayer(653, ServerConnection.getNewPacketId(), "All tasks transfered!");
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
						sendDataToAllGamePlayer(654, ServerConnection.getNewPacketId(), "All tasks executed!");
						ConsoleOutput.printMessageInConsole(getId(), "All tasks executed! (Blocked: "+blockedActionsForThisRound+")", true);
						usedFieldsForThisRound.clear();
						blockedActionsForThisRound = 0;
						roundNumber++;
						ConsoleOutput.printMessageInConsole(getId(), "# NEXT ROUND (ID: "+id+" ; Round: "+roundNumber+")", true);
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
		sendDataToAllGamePlayer(660, ServerConnection.getNewPacketId(), chatMessageNumber+";"+chatMessage);
		this.actionLog.add(new GameAction(senderID, GameActionType.CHATMESSAGE, this.roundNumber, chatMessage, chatMessageNumber));
		
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
		sendDataToAllGamePlayer(661, ServerConnection.getNewPacketId(), data);
		this.actionLog.add(new GameAction(pingerID, GameActionType.FIELDPING, this.roundNumber, x, y, -1, -1, -1));
		
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//ACTIONS
	public void addBuild(ServerPlayer player, String name, int x, int y) {
		player.addCurrentRoundLog(new GameAction(player.getId(), GameActionType.BUILD, this.roundNumber, name, x, y, -1, -1, -1));
		ConsoleOutput.printMessageInConsole(getId(), "	ADD Build "+name+" on "+x+":"+y+" by "+player.getId(), true);
	}
	private void execBuild(GameAction action) {
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
		
		for(ServerBuilding building : this.toRemoveBuildings) {
			this.buildings.remove(building);
			this.actionLog.add(new GameAction(building.playerId, GameActionType.DEATH, this.roundNumber, building.X, building.Y, -1, -1, -1));
			ConsoleOutput.printMessageInConsole(getId(), "	Death-B on "+building.X+":"+building.Y+" by "+building.playerId, true);
			deaths++;
		}
		this.toRemoveBuildings.clear();
		
		for(ServerTroup troup : this.toRemoveTroups) {
			this.troups.remove(troup);
			this.actionLog.add(new GameAction(troup.playerId, GameActionType.DEATH, this.roundNumber, troup.X, troup.Y, -1, -1, -1));
			ConsoleOutput.printMessageInConsole(getId(), "	Death-T on "+troup.X+":"+troup.Y+" by "+troup.playerId, true);
			deaths++;
		}
		this.toRemoveTroups.clear();
		
		ConsoleOutput.printMessageInConsole(getId(), "	Deaths executed (Total: "+deaths+")", true);
		
		
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
			return "["+this.id+"] "+this.type+" - Running: "+this.isRunning+" - PlayerCount: "+this.getPlayerCount()+" - RoundNumber: "+this.roundNumber+" - Actions: "+this.actionLog.size()+" - Map: "+this.map.name+" - Host: "+this.host.getProfile().getName();
		}else {
			return "["+this.id+"] "+this.type+" - Running: "+this.isRunning+" - PlayerCount: "+this.getPlayerCount()+" - RoundNumber: "+this.roundNumber+" - Actions: "+this.actionLog.size()+" - Map: "+this.map.name;
		}
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
	public boolean isRunning() {
		return isRunning;
	}
	public GameStatus getStatus() {
		return status;
	}
	public int getPlayerCount() {
		return this.playerList.size();
	}
	public List<ServerBuilding> getBuildings() {
		return buildings;
	}
	public List<ServerTroup> getTroups() {
		return troups;
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
