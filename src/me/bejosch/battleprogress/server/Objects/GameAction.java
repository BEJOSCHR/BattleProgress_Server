package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Enum.GameActionType;

public class GameAction {

	public int playerId;
	public GameActionType type;
	public int round;
	public int x, y, newX, newY, amount;
	public String text; 
	
	public GameAction(int playerId, GameActionType type, int round, String name, int x, int y, int newX, int newY, int amount) {
		
		this.playerId = playerId;
		this.type = type;
		this.round = round;
		
		this.text = name;
		
		this.x = x;
		this.y = y;
		this.newX = newX;
		this.newY = newY;
		this.amount = amount;
		
	}
	
	public GameAction(int playerId, GameActionType type, int round, int x, int y, int newX, int newY, int amount) {
		
		this.playerId = playerId;
		this.type = type;
		this.round = round;
		
		this.x = x;
		this.y = y;
		this.newX = newX;
		this.newY = newY;
		this.amount = amount;
		
	}
	
	public GameAction(int playerId, GameActionType type, int round, String text, int amount) {
		
		this.playerId = playerId;
		this.type = type;
		this.round = round;
		
		this.amount = amount; //CHATMESSAGE NUMBER
		this.text = text;
		
	}
	
}
