package me.bejosch.battleprogress.server.Handler;

import org.apache.mina.core.session.IoSession;

import me.bejosch.battleprogress.server.Data.ServerPlayerData;
import me.bejosch.battleprogress.server.Objects.ClientConnection;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;

public class ServerPlayerHandler {

	public static ServerPlayer getOnlinePlayer(int playerID) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getId() == playerID) {
				return player;
			}
		}
		return null;
		
	}
	public static ServerPlayer getOnlinePlayer(String playerName) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getProfile().getName().equalsIgnoreCase(playerName)) {
				return player;
			}
		}
		return null;
		
	}
	public static ServerPlayer getOnlinePlayer(ClientConnection clientConnectionThread) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getProfile().getConnection() == clientConnectionThread) {
				return player;
			}
		}
		return null;
		
	}
	public static ServerPlayer getOnlinePlayer(IoSession session) {
		
		for(ServerPlayer player : ServerPlayerData.onlinePlayer) {
			if(player.getProfile().getConnection().session == session) {
				return player;
			}
		}
		return null;
		
	}
	
}
