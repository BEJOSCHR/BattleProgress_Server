package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Objects.ClientConnection;
import me.bejosch.battleprogress.server.Objects.DictionaryInfoDescription;

public class DictionaryInfoHandler {

	public static List<DictionaryInfoDescription> dictionaryInfos = new ArrayList<DictionaryInfoDescription>();
	
	public static void updateDictionaryInfoList(boolean withMessages) {
		
		dictionaryInfos.clear();
		
		dictionaryInfos.add(new DictionaryInfoDescription("Refund"));
		dictionaryInfos.add(new DictionaryInfoDescription("Field depletion"));
		dictionaryInfos.add(new DictionaryInfoDescription("Energy"));
		dictionaryInfos.add(new DictionaryInfoDescription("Material"));
		dictionaryInfos.add(new DictionaryInfoDescription("ResearchPoints"));
		
		if(withMessages == true) {
			ConsoleOutput.printMessageInConsole("Loaded "+dictionaryInfos.size()+" dictionaryInfoDescriptions", true);
		}
		
	}
	
	public static void updateDictionaryInfoForClient(ClientConnection client) {
		
		//112 titel, description_en, description_de
		
		for(DictionaryInfoDescription info : dictionaryInfos) {
			sendDictionaryInfoContainer(info, client);
		}
		
	}
	
	public static void sendDictionaryInfoContainer(DictionaryInfoDescription info, ClientConnection client) {
		
		String descriptionData_en = info.description_en;
		String descriptionData_de = info.description_de;
		
		String data = info.titel+";"+descriptionData_en+";"+descriptionData_de;
		client.sendData(112, data);
				
	}
	
}
