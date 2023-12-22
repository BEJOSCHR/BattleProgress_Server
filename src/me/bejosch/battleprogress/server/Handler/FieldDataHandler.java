package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Enum.FieldType;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Objects.ClientConnection;
import me.bejosch.battleprogress.server.Objects.FieldDataContainer;

public class FieldDataHandler {

	public static List<FieldDataContainer> fieldData = new ArrayList<FieldDataContainer>();
	
	public static void updateFieldDataList(boolean withMessages) {
		
		fieldData.clear();
		
		for(FieldType type : FieldType.values() ) {
			fieldData.add(new FieldDataContainer(type));
		}
		
		if(withMessages == true) {
			ConsoleOutput.printMessageInConsole("Loaded "+fieldData.size()+" field data", true);
		}
		
	}
	
	public static void updateFieldDataForClient(ClientConnection client) {
		
		//113 type ; description
		
		for(FieldDataContainer container : fieldData) {
			sendFieldDataContainer(container, client);
		}
		
	}
	
	public static void sendFieldDataContainer(FieldDataContainer container, ClientConnection client) {
		
		String descriptionData_en = container.description_en[0]+";"+container.description_en[1]+";"+container.description_en[2]+";"+container.description_en[3];
		String descriptionData_de = container.description_de[0]+";"+container.description_de[1]+";"+container.description_de[2]+";"+container.description_de[3];
		String data = container.type.toString()+";"+descriptionData_en+";"+descriptionData_de;
		client.sendData(113, data);
				
	}
	
}
