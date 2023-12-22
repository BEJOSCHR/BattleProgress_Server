package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class UpgradeDataContainer {

	public String upgradeType = null;
	
	public int researchCost = 9999;
	public int effectValue = 0; //THE AMOUNT BY THAT IT UPGRADES
	
	public String[] description_en = new String[4];
	public String[] description_de = new String[4];
	
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
		
		this.description_en[0] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_1", "TargetName", upgradeType, "Language", DatabaseData.language_en);
		this.description_en[1] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_2", "TargetName", upgradeType, "Language", DatabaseData.language_en);
		this.description_en[2] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_3", "TargetName", upgradeType, "Language", DatabaseData.language_en);
		this.description_en[3] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_4", "TargetName", upgradeType, "Language", DatabaseData.language_en);
		
		this.description_de[0] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_1", "TargetName", upgradeType, "Language", DatabaseData.language_de);
		this.description_de[1] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_2", "TargetName", upgradeType, "Language", DatabaseData.language_de);
		this.description_de[2] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_3", "TargetName", upgradeType, "Language", DatabaseData.language_de);
		this.description_de[3] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_4", "TargetName", upgradeType, "Language", DatabaseData.language_de);
		
	}
	
}
