package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Handler.UnitsStatsHandler;

public class ServerTroup {

	public int playerId;
	public String name;
	public int X;
	public int Y;
	
	public int startHealth, health;
	
	public ServerTroup(int X, int Y, String name, int playerId) {
		
		this.X = X;
		this.Y = Y;
		this.name = name;
		this.playerId = playerId;
		
		int health = UnitsStatsHandler.getContainer_Troup(name).leben;
		this.startHealth = health;
		this.health = health;
		
	}
	
}
