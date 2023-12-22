package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;

public class UnitStatsContainer {

	public String name;
	public String kürzel;
	public int kosten;
	public int leben;
	public int energieVerbrauch;
	public int energieProduktion;
	public int materialProduktion;
	public int schaden;
	public int viewDistance;
	public int moveDistance; //FOR BUILDINGS 0
	public int actionDistance;
	public int heal;
	public int repair;
	public int research;
	
	public String[] description_en = new String[4];
	public String[] description_de = new String[4];
	
	//SERVER
	public UnitStatsContainer(String databaseTabelle, String unitName) {
		
		loadUnitStats(databaseTabelle, unitName);
		
	}

	private void loadUnitStats(String databaseTabelle, String unitName) {
		
		this.name = unitName;
		this.kürzel = DatabaseHandler.selectString(databaseTabelle, "Kürzel", "Name", unitName);
		this.kosten = DatabaseHandler.selectInt(databaseTabelle, "Kosten", "Name", unitName);
		this.leben = DatabaseHandler.selectInt(databaseTabelle, "Leben", "Name", unitName);
		this.energieVerbrauch = DatabaseHandler.selectInt(databaseTabelle, "EnergieVerbrauch", "Name", unitName);
		this.energieProduktion = DatabaseHandler.selectInt(databaseTabelle, "EnergieProduktion", "Name", unitName);
		this.materialProduktion = DatabaseHandler.selectInt(databaseTabelle, "MaterialProduktion", "Name", unitName);
		this.schaden = DatabaseHandler.selectInt(databaseTabelle, "Schaden", "Name", unitName);
		this.viewDistance = DatabaseHandler.selectInt(databaseTabelle, "ViewDistance", "Name", unitName);
		this.moveDistance = DatabaseHandler.selectInt(databaseTabelle, "MoveDistance", "Name", unitName);
		this.actionDistance = DatabaseHandler.selectInt(databaseTabelle, "ActionDistance", "Name", unitName);
		this.heal = DatabaseHandler.selectInt(databaseTabelle, "Heal", "Name", unitName);
		this.repair = DatabaseHandler.selectInt(databaseTabelle, "Repair", "Name", unitName);
		this.research = DatabaseHandler.selectInt(databaseTabelle, "Research", "Name", unitName);
		
		this.loadDescription(unitName);
		
	}
	
	private void loadDescription(String unitName) {
		
		this.description_en[0] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_1", "TargetName", unitName, "Language", DatabaseData.language_en);
		this.description_en[1] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_2", "TargetName", unitName, "Language", DatabaseData.language_en);
		this.description_en[2] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_3", "TargetName", unitName, "Language", DatabaseData.language_en);
		this.description_en[3] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_4", "TargetName", unitName, "Language", DatabaseData.language_en);
		
		this.description_de[0] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_1", "TargetName", unitName, "Language", DatabaseData.language_de);
		this.description_de[1] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_2", "TargetName", unitName, "Language", DatabaseData.language_de);
		this.description_de[2] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_3", "TargetName", unitName, "Language", DatabaseData.language_de);
		this.description_de[3] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_4", "TargetName", unitName, "Language", DatabaseData.language_de);
		
	}
	
}
