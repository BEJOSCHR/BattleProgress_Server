package me.bejosch.battleprogress.server.Objects;

import java.util.ArrayList;
import java.util.List;

public class ServerReplay {

	//DATA ABOIT THE PLAYERS
	public List<GameAction> actions = new ArrayList<GameAction>();
	
	public ServerReplay(List<GameAction> actions) {
		
		this.actions = actions;
		
	}
	
}
