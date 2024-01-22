package me.bejosch.battleprogress.server.Objects;

public class ServerResearch {

	private int playerID;
	private String upgradeName;
	private int roundNumber;
	
	public ServerResearch(int playerID, String upgradeName, int roundNumber) {
		
		this.playerID = playerID;
		this.upgradeName = upgradeName;
		this.roundNumber = roundNumber;
		
	}
	
	public int getPlayerID() {
		return playerID;
	}
	public String getUpgradeName() {
		return upgradeName;
	}
	public int getRoundNumber() {
		return roundNumber;
	}
	
}
