package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import me.bejosch.battleprogress.server.Data.ServerQueueData;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Main.ServerConnection;
import me.bejosch.battleprogress.server.Objects.ServerGame;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;
import me.bejosch.battleprogress.server.Objects.ServerQueue;

public class ServerQueueHandler {
	
	private static Timer queueWaitTimer = null;
	
	public static void checkNewQueueAdd(ServerQueue newQueue) {
		
		if(ServerQueueData.matchingQueues == true) {
			//ONE IS GETTING CHECKED ALREADY
			ServerQueueData.initQueuesQueue.add(newQueue);
		}else {
			//CHECK THIS ONE
			newQueueAdded(newQueue);
		}
		
	}
	
	private static void newQueueAdded(ServerQueue newQueue) {
		
		ServerQueueData.matchingQueues = true;
		
		GameType type = newQueue.getType();
		
		List<ServerQueue> possibleQueues = getQueuesByType(type);
		possibleQueues.remove(newQueue); //NOT THE NEW ONE
		
		if(possibleQueues.isEmpty() == false) {
			switch(type) {
			case Normal_1v1:
				//NEED 1 MORE
				ServerQueue matchedQueue_1 = possibleQueues.get(0);
				ServerGame newGame_1 = new ServerGame(type, MapHandler.getRandomMap(type));
				List<ServerPlayer> involvedPlayer_1 = new ArrayList<ServerPlayer>();
				involvedPlayer_1.addAll(newQueue.getQueuedPlayer());
				involvedPlayer_1.addAll(matchedQueue_1.getQueuedPlayer());
				for(ServerPlayer player : involvedPlayer_1) {
					player.getProfile().getConnection().sendData(405, ServerConnection.getNewPacketId(), newGame_1.getId()+""); //SEND GAME ACCEPT REQUEST
				}
				newQueue.removeQueue(false);
				matchedQueue_1.removeQueue(false);
				break;
			case Normal_2v2:
				if(newQueue.getQueuedPlayerCount() == 1) {
					//NEED 3 MORE 1er QUEUEs - ONLY 1+1vs1+1 not 1+1vs2
					ServerQueue matchedQueue_2_1 = null;
					ServerQueue matchedQueue_2_2 = null;
					ServerQueue matchedQueue_2_3 = null;
					for(ServerQueue queue : possibleQueues) {
						if(queue.getQueuedPlayerCount() == 1) { //ADD RANKED REQUIERMENT
							if(matchedQueue_2_1 == null) { matchedQueue_2_1 = queue; }
							else if(matchedQueue_2_2 == null) { matchedQueue_2_2 = queue; }
							else {
								matchedQueue_2_3 = queue;
								break;
							}
						}
					}
					if(matchedQueue_2_1 != null && matchedQueue_2_2 != null && matchedQueue_2_3 != null) {
						//FOUND ENOUGH
						ServerGame newGame_2 = new ServerGame(type, MapHandler.getRandomMap(type));
						List<ServerPlayer> involvedPlayer_2 = new ArrayList<ServerPlayer>();
						involvedPlayer_2.addAll(newQueue.getQueuedPlayer());
						involvedPlayer_2.addAll(matchedQueue_2_1.getQueuedPlayer());
						involvedPlayer_2.addAll(matchedQueue_2_2.getQueuedPlayer());
						involvedPlayer_2.addAll(matchedQueue_2_3.getQueuedPlayer());
						for(ServerPlayer player : involvedPlayer_2) {
							player.getProfile().getConnection().sendData(405, ServerConnection.getNewPacketId(), newGame_2.getId()+""); //SEND GAME ACCEPT REQUEST
						}
						newQueue.removeQueue(false);
						matchedQueue_2_1.removeQueue(false);
						matchedQueue_2_2.removeQueue(false);
						matchedQueue_2_3.removeQueue(false);
					}
				}else {
					//NEED 1 MORE 2er QUEUE
					ServerQueue matchedQueue_2 = null;
					for(ServerQueue queue : possibleQueues) {
						if(queue.getQueuedPlayerCount() == 2) { //ADD RANKED REQUIERMENT
							matchedQueue_2 = queue;
							break;
						}
					}
					if(matchedQueue_2 != null) {
						//FOUND ENOUGH
						ServerGame newGame_2 = new ServerGame(type, MapHandler.getRandomMap(type));
						List<ServerPlayer> involvedPlayer_2 = new ArrayList<ServerPlayer>();
						involvedPlayer_2.addAll(newQueue.getQueuedPlayer());
						involvedPlayer_2.addAll(matchedQueue_2.getQueuedPlayer());
						for(ServerPlayer player : involvedPlayer_2) {
							player.getProfile().getConnection().sendData(405, ServerConnection.getNewPacketId(), newGame_2.getId()+""); //SEND GAME ACCEPT REQUEST
						}
						newQueue.removeQueue(false);
						matchedQueue_2.removeQueue(false);
					}
				}
				break;
			case Ranked_1v1:
				//NEED 1 MORE
				ServerQueue matchedQueue_3 = possibleQueues.get(0);
				ServerGame newGame_3 = new ServerGame(type, MapHandler.getRandomMap(type));
				List<ServerPlayer> involvedPlayer_3 = new ArrayList<ServerPlayer>();
				involvedPlayer_3.addAll(newQueue.getQueuedPlayer());
				involvedPlayer_3.addAll(matchedQueue_3.getQueuedPlayer());
				for(ServerPlayer player : involvedPlayer_3) {
					player.getProfile().getConnection().sendData(405, ServerConnection.getNewPacketId(), newGame_3.getId()+""); //SEND GAME ACCEPT REQUEST
				}
				newQueue.removeQueue(false);
				matchedQueue_3.removeQueue(false);
				break;
			default:
				break;
			}
		}
		
		//Check next queue if one is waiting
		if(ServerQueueData.initQueuesQueue.isEmpty() == false) {
			ServerQueue queue = ServerQueueData.initQueuesQueue.get(0);
			ServerQueueData.initQueuesQueue.remove(queue);
			newQueueAdded(queue); //REKURSION
		}else {
			ServerQueueData.matchingQueues = false;
		}
		
	}
	
	public static void queueLevelUpdate(ServerQueue queue, int newLevel) {
		
		//TODO Queue changed priority based on waited time - Check for new games to compare with less limitation
		
	}
	
	public static void startQueueWaitTimer() {
		
		if(queueWaitTimer == null) {
			queueWaitTimer = new Timer();
			queueWaitTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					
					for(ServerQueue queue : getAllQueues()) {
						queue.updateWaitTime();
					}
					
				}
			}, 0, 1000);
		}
		
	}
	public static void stopQueueWaitTimer() {
		
		if(queueWaitTimer != null) {
			queueWaitTimer.cancel();
			queueWaitTimer = null;
		}
		
	}
	
//==========================================================================================================
	//GETTER / SETTER
	
	public static ServerQueue getQueueByID(int queueID) {
		
		for(ServerQueue queue : getAllQueues()) {
			if(queue.getID() == queueID) {
				return queue;
			}
		}
		return null;
		
	}
	
	public static List<ServerQueue> getQueuesByType(GameType type) {
		
		List<ServerQueue> queues = new ArrayList<ServerQueue>();
		for(ServerQueue queue : getAllQueues()) {
			if(queue.getType() == type) {
				queues.add(queue);
			}
		}
		return queues;
		
	}
	
	public static void removeQueue(ServerQueue queue) {
		
		if(ServerQueueData.initQueuesQueue.contains(queue)) { ServerQueueData.initQueuesQueue.remove(queue); }
		
		if(ServerQueueData.waitingQueues_LV0.contains(queue)) { ServerQueueData.waitingQueues_LV0.remove(queue); }
		if(ServerQueueData.waitingQueues_LV1.contains(queue)) { ServerQueueData.waitingQueues_LV1.remove(queue); }
		if(ServerQueueData.waitingQueues_LV2.contains(queue)) { ServerQueueData.waitingQueues_LV2.remove(queue); }
		if(ServerQueueData.waitingQueues_LV3.contains(queue)) { ServerQueueData.waitingQueues_LV3.remove(queue); }
		
	}
	
	public static List<ServerQueue> getAllQueues() {
		
		List<ServerQueue> queues = new ArrayList<ServerQueue>();
		queues.addAll(ServerQueueData.waitingQueues_LV0);
		queues.addAll(ServerQueueData.waitingQueues_LV1);
		queues.addAll(ServerQueueData.waitingQueues_LV2);
		queues.addAll(ServerQueueData.waitingQueues_LV3);
		return queues;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back a new QueueId
	 * @return int - The new QueueId
	 */
	public static int getNewQueueId() {
		
		int id = new Random().nextInt( (getMaxQueueIdCount()-getMinQueueIdCount()) )+getMinQueueIdCount();
		for(ServerQueue queue : getAllQueues()) { //CHECK ID
			if(queue.getID() == id) {
				return getNewQueueId();
			}
		}
		return id;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the min QueueId count
	 * @return int - The min QueueId count
	 */
	private static int getMinQueueIdCount() {
		
		int number = 1;
		for(int i = ServerQueueData.queueIdLength; i > 1 ; i--) {
			number = number*10;
		}
		return number;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the max QueueId count
	 * @return int - The max QueueId count
	 */
	private static int getMaxQueueIdCount() {
		
		int number = 1;
		for(int i = ServerQueueData.queueIdLength+1 ; i > 1 ; i--) {
			number = number*10;
		}
		return number-1;
		
	}
	
}
