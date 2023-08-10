package me.bejosch.battleprogress.server.Handler;

import java.util.Random;

import me.bejosch.battleprogress.server.Data.ServerGroupData;
import me.bejosch.battleprogress.server.Objects.ClientConnectionThread;
import me.bejosch.battleprogress.server.Objects.ServerGroup;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;
import me.bejosch.battleprogress.server.Objects.ServerQueue;

public class ServerGroupHandler {

	public static ServerGroup getGroupByPlayer(ServerPlayer player) {
		
		for(ServerGroup group : ServerGroupData.activeGroups) {
			if(group.getPlayers().contains(player)) {
				return group;
			}
		}
		
		return null;
	}
	
	public static ServerGroup getGroupByPlayerID(int playerID) {
		
		for(ServerGroup group : ServerGroupData.activeGroups) {
			for(ServerPlayer player : group.getPlayers()) {
				if(player.getId() == playerID) {
					return group;
				}
			}
		}
		
		return null;
	}
	
	public static ServerGroup getGroupByClientConnection(ClientConnectionThread clientConnectionThread) {
		
		for(ServerGroup group : ServerGroupData.activeGroups) {
			for(ServerPlayer player : group.getPlayers()) {
				if(player.getProfile().getConnection() == clientConnectionThread) {
					return group;
				}
			}
		}
		
		return null;
	}
	
	public static ServerGroup getGroupByQueue(ServerQueue queue) {
		
		for(ServerGroup group : ServerGroupData.activeGroups) {
			if(group.getActiveQueue() != null && group.getActiveQueue().getID() == queue.getID()) {
				return group;
			}
		}
		
		return null;
	}
	
//==========================================================================================================
	/**
	 * Gives back a new GroupId
	 * @return int - The new GroupId
	 */
	public static int getNewGroupId() {
		
		int id = new Random().nextInt( (getMaxGroupIdCount()-getMinGroupIdCount()) )+getMinGroupIdCount();
		for(ServerGroup group : ServerGroupData.activeGroups) { //CHECK ID
			if(group.getID() == id) {
				return getNewGroupId();
			}
		}
		return id;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the min GroupId count
	 * @return int - The min GroupId count
	 */
	private static int getMinGroupIdCount() {
		
		int number = 1;
		for(int i = ServerGroupData.groupIdLength; i > 1 ; i--) {
			number = number*10;
		}
		return number;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the max GroupId count
	 * @return int - The max GroupId count
	 */
	private static int getMaxGroupIdCount() {
		
		int number = 1;
		for(int i = ServerGroupData.groupIdLength+1 ; i > 1 ; i--) {
			number = number*10;
		}
		return number-1;
		
	}
	
}
