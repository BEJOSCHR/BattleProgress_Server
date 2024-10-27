package me.bejosch.battleprogress.server.Data;

import java.util.concurrent.CopyOnWriteArrayList;

import me.bejosch.battleprogress.server.Objects.ServerPlayer;

public class ServerPlayerData {

	public static CopyOnWriteArrayList<ServerPlayer> onlinePlayer = new CopyOnWriteArrayList<ServerPlayer>();
	
}
