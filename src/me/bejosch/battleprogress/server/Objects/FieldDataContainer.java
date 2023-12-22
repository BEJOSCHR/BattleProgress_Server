package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Enum.FieldType;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;

public class FieldDataContainer {

	public FieldType type;
	public String[] description_en = new String[4];
	public String[] description_de = new String[4];
	
	public FieldDataContainer(FieldType type) {
		
		this.type = type;
		loadDescription(type);
		
	}

	private void loadDescription(FieldType type) {
		
		this.description_en[0] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_1", "TargetName", type.toString(), "Language", DatabaseData.language_en); 
		this.description_en[1] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_2", "TargetName", type.toString(), "Language", DatabaseData.language_en); 
		this.description_en[2] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_3", "TargetName", type.toString(), "Language", DatabaseData.language_en); 
		this.description_en[3] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_4", "TargetName", type.toString(), "Language", DatabaseData.language_en); 
		
		this.description_de[0] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_1", "TargetName", type.toString(), "Language", DatabaseData.language_de); 
		this.description_de[1] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_2", "TargetName", type.toString(), "Language", DatabaseData.language_de); 
		this.description_de[2] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_3", "TargetName", type.toString(), "Language", DatabaseData.language_de); 
		this.description_de[3] = DatabaseHandler.selectString(DatabaseData.tabellName_description, "Row_4", "TargetName", type.toString(), "Language", DatabaseData.language_de); 
		
	}
	
}
