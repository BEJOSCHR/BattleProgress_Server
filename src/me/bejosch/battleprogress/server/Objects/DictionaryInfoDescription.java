package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;

public class DictionaryInfoDescription {

	public String titel;
	
	public String description_en;
	public String description_de;
	
	public DictionaryInfoDescription(String titel) {
		
		this.titel = titel;
		loadDescriptions(titel);
		
	}
	
	private void loadDescriptions(String titel) {
		
		this.description_en = DatabaseHandler.selectString(DatabaseData.tabellName_dictionaryInfo, "Description", "Titel", titel, "Language", DatabaseData.language_en);
		this.description_de = DatabaseHandler.selectString(DatabaseData.tabellName_dictionaryInfo, "Description", "Titel", titel, "Language", DatabaseData.language_de);
		
	}
	
}
