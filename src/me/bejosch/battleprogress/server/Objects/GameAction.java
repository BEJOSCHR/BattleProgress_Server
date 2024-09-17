package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Enum.GameActionType;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class GameAction {

	public int playerId;
	public GameActionType type;
	public int round;
	public int x, y, newX, newY, amount;
	public String text; 
	
	public int executeID = -1;
	
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
	
	public void setExecuteID(int id) {
		if(this.executeID != -1) { ConsoleOutput.printMessageInConsole("Set executeID for game action which has already been executed! Continue... ("+this.getData()+")", true); }
		this.executeID = id;
	}
	
	public String getData() {
		
		//playerID;type;round;x;y;newX;newY;amount;text;executeID
		return this.playerId+";"+this.type+";"+this.round+";"+this.x+";"+this.y+";"+this.newX+";"+this.newY+";"+this.amount+";"+this.text+";"+this.executeID;
		
	}
	
}
