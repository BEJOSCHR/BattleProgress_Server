package me.bejosch.battleprogress.server.Handler;

import me.bejosch.battleprogress.server.Data.ServerPlayerData;
import me.bejosch.battleprogress.server.Objects.ClientConnectionThread;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;

public class ServerPlayerHandler {

	public static ServerPlayer getOnlinePlayerByID(int playerID) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getId() == playerID) {
				return player;
			}
		}
		return null;
		
	}
	public static ServerPlayer getOnlinePlayerByName(String playerName) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getProfile().getName().equalsIgnoreCase(playerName)) {
				return player;
			}
		}
		return null;
		
	}
	public static ServerPlayer getOnlinePlayerByConnection(ClientConnectionThread clientConnectionThread) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getProfile().getConnection() == clientConnectionThread) {
				return player;
			}
		}
		return null;
		
	}
	
}
