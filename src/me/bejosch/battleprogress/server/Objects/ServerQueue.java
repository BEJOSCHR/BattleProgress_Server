 package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.bejosch.battleprogress.server.Data.ServerQueueData;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Handler.ServerGroupHandler;
import me.bejosch.battleprogress.server.Handler.ServerQueueHandler;
import me.bejosch.battleprogress.server.Main.ServerConnection;

public class ServerQueue {

	private int queueID;
	
	private GameType type;
	private List<ServerPlayer> queuedPlayer = new ArrayList<ServerPlayer>();
	
	private int waitingLevel = 0;
	private int waitedSecondes = 0;
	
	private Timer timer;
	
	public ServerQueue(GameType type, int allreadyWaitedTime, List<ServerPlayer> players) {
		
		this.queueID = ServerQueueHandler.getNewQueueId();
		this.type = type;
		this.waitedSecondes = allreadyWaitedTime;
		this.queuedPlayer = players;
		
		for(ServerPlayer player : this.queuedPlayer) {
			player.getProfile().getConnection().sendData(400, ServerConnection.getNewPacketId(), type+";"+waitedSecondes);
		}
		
		if(allreadyWaitedTime >= ServerQueueData.waitLevel_3) {
			ServerQueueData.waitingQueues_LV3.add(this);
		}else if(allreadyWaitedTime >= ServerQueueData.waitLevel_2) {
			ServerQueueData.waitingQueues_LV2.add(this);
		}else if(allreadyWaitedTime >= ServerQueueData.waitLevel_1) {
			ServerQueueData.waitingQueues_LV1.add(this);
		}else {
			ServerQueueData.waitingQueues_LV0.add(this);
		}
		
		//ADD WITH DELAY, because activegroup variable has to be initialised in group
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				
				ServerQueueHandler.checkNewQueueAdd(thisGet());
				
			}
		}, 1000*3);

	}
	
	private ServerQueue thisGet() {
		return this;
	}
	
	public void updateWaitTime() {
		
		this.waitedSecondes++;
		if(waitedSecondes == ServerQueueData.waitLevel_1) {
			ServerQueueData.waitingQueues_LV0.remove(this);
			ServerQueueData.waitingQueues_LV1.add(this);
			this.waitingLevel = 1;
			ServerQueueHandler.queueLevelUpdate(this, 1);
		}else if(waitedSecondes == ServerQueueData.waitLevel_2) {
			ServerQueueData.waitingQueues_LV1.remove(this);
			ServerQueueData.waitingQueues_LV2.add(this);
			this.waitingLevel = 2;
			ServerQueueHandler.queueLevelUpdate(this, 2);
		}else if(waitedSecondes == ServerQueueData.waitLevel_3) {
			ServerQueueData.waitingQueues_LV2.remove(this);
			ServerQueueData.waitingQueues_LV3.add(this);
			this.waitingLevel = 3;
			ServerQueueHandler.queueLevelUpdate(this, 3);
		}
		
	}
	
	public void removeQueue(boolean calledFromGroup) {
		
		this.timer.cancel();
		
		if(calledFromGroup == false) { ServerGroupHandler.getGroupByQueue(this).leaveQueue(true, false); }
		ServerQueueHandler.removeQueue(this);
		
		for(ServerPlayer player : this.queuedPlayer) {
			player.getProfile().getConnection().sendData(401, ServerConnection.getNewPacketId(), "Leave Queue");
		}
		
	}
	
	public String getInfoAsString() {
		return "["+this.queueID+"] "+this.type+" - "+(queuedPlayer.size() >= 1 ? queuedPlayer.get(0).getProfile().getName() : "null")+", "+(queuedPlayer.size() >= 2 ? queuedPlayer.get(1).getProfile().getName() : "null")+" - PlayerCount: "+this.queuedPlayer.size()+" - WaitedLevel: "+this.waitingLevel+" - WaitedTime: "+this.waitedSecondes;
	}
	
	public int getID() {
		return queueID;
	}
	public GameType getType() {
		return type;
	}
	public List<ServerPlayer> getQueuedPlayer() {
		return queuedPlayer;
	}
	public int getQueuedPlayerCount() {
		return queuedPlayer.size();
	}
	public int getWaitingLevel() {
		return waitingLevel;
	}
	public int getWaitedSecondes() {
		return waitedSecondes;
	}
	
}
