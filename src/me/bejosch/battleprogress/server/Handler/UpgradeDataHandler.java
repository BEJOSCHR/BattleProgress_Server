package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Main.ServerConnection;
import me.bejosch.battleprogress.server.Objects.ClientConnectionThread;
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
		
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "Converter") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterEfficiency1") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterEfficiency2") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterProduction1") );
		upgrades.add( new UpgradeDataContainer(DatabaseData.tabellName_upgrades, "ConverterProduction2") );
		
		if(withMessages == true) {
			ConsoleOutput.printMessageInConsole("Loaded "+upgrades.size()+" upgrades", true);
		}
		
	}
	
	public static void updateUpgradesForClient(ClientConnectionThread client) {
		
		//111 UpgradeType, cost, effectValue
		
		for(UpgradeDataContainer container : upgrades) {
			sendUpgradeDataContainer(container, client);
		}
		
	}
	
	public static void sendUpgradeDataContainer(UpgradeDataContainer container, ClientConnectionThread client) {
		
		String data = container.upgradeType+";"+container.researchCost+";"+container.effectValue;
		client.sendData(111, ServerConnection.getNewPacketId(), data);
				
	}
	
}
