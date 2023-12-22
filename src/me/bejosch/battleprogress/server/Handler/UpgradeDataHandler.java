package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Objects.ClientConnection;
import me.bejosch.battleprogress.server.Objects.UpgradeDataContainer;

public class UpgradeDataHandler {

	public static List<UpgradeDataContainer> upgrades = new ArrayList<UpgradeDataContainer>();
	
	public static void updateUpgradeList(boolean withMessages) {
		
		upgrades.clear();
		
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "MineProduction1") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "MineProduction2") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "MineProduction3") );
		
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ReactorProduction1") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ReactorProduction2") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ReactorProduction3") );
		
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "UnlockConverter") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterEfficiency1") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterEfficiency2") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterProduction1") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterProduction2") );
		
		if(withMessages == true) {
			ConsoleOutput.printMessageInConsole("Loaded "+upgrades.size()+" upgrades", true);
		}
		
	}
	
	public static void updateUpgradesForClient(ClientConnection client) {
		
		//111 UpgradeType, cost, effectValue
		
		for(UpgradeDataContainer container : upgrades) {
			sendUpgradeDataContainer(container, client);
		}
		
	}
	
	public static void sendUpgradeDataContainer(UpgradeDataContainer container, ClientConnection client) {
		
		String descriptionData_en = container.description_en[0]+";"+container.description_en[1]+";"+container.description_en[2]+";"+container.description_en[3];
		String descriptionData_de = container.description_de[0]+";"+container.description_de[1]+";"+container.description_de[2]+";"+container.description_de[3];
		
		String data = container.upgradeType+";"+container.researchCost+";"+container.effectValue+";"+descriptionData_en+";"+descriptionData_de;
		client.sendData(111, data);
				
	}
	
}
