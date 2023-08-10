package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Handler.DatabaseHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class UpgradeDataContainer {

	public String upgradeType = null;
	
	public int researchCost = 9999;
	public int effectValue = 0; //THE AMOUNT BY THAT IT UPGRADES
	
	public UpgradeDataContainer(String databaseTabelle, String upgradeType) {
		
		loadDataFromDB(databaseTabelle, upgradeType);
		
	}
	
	private void loadDataFromDB(String databaseTabelle, String upgradeType) {
		
		this.upgradeType = upgradeType;
		this.researchCost = DatabaseHandler.selectInt(databaseTabelle, "Cost", "Type", upgradeType);
		this.effectValue = DatabaseHandler.selectInt(databaseTabelle, "Value", "Type", upgradeType);
		
		if(this.researchCost == -1 || this.effectValue == -1) {
			//LOADING FAILED
			ConsoleOutput.printMessageInConsole("Couldn't load data for upgrade '"+upgradeType+"'!", true);	
		}
		
	}
	
}
