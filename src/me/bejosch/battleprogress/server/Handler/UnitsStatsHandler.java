package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;

import me.bejosch.battleprogress.server.Data.DatabaseData;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Objects.ClientConnection;
import me.bejosch.battleprogress.server.Objects.UnitStatsContainer;

public class UnitsStatsHandler {

	public static List<UnitStatsContainer> buildings = new ArrayList<UnitStatsContainer>();
	public static List<UnitStatsContainer> troups_air = new ArrayList<UnitStatsContainer>();
	public static List<UnitStatsContainer> troups_land = new ArrayList<UnitStatsContainer>();
	
	public static void updateUnitsList(boolean withMessages) {
		
		buildings.clear();
		troups_air.clear();
		troups_land.clear();
		
		//BUILDINGS
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Airport") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Barracks") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Garage") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Converter") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Headquarter") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Hospital") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Laboratory") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Artillery") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Turret") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Mine") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Reactor") );
		buildings.add( new UnitStatsContainer(DatabaseData.tabellName_buildings, "Workshop") );
		//...
		
		//TROUPS AIR
		troups_air.add( new UnitStatsContainer(DatabaseData.tabellName_troupAir, "Light Heli") );
		troups_air.add( new UnitStatsContainer(DatabaseData.tabellName_troupAir, "Medium Heli") );
		troups_air.add( new UnitStatsContainer(DatabaseData.tabellName_troupAir, "Heavy Heli") );
		//...
		
		//TROUPS LAND
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Commander") );
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Light Tank") );
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Medium Tank") );
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Heavy Tank") );
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Light Soldier") );
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Medium Soldier") );
		troups_land.add( new UnitStatsContainer(DatabaseData.tabellName_troupLand, "Heavy Soldier") );
		//...
		
		if(withMessages == true) {
			ConsoleOutput.printMessageInConsole("Loaded "+buildings.size()+" buildings, "+troups_air.size()+" air troups and "+troups_land.size()+" land troups", true);
		}
		
	}
	
	public static void updateUnitsForClient(ClientConnection client) {
		
		//110 Name, K�rzel, Kosten, Leben, EnergieVerbrauch, EnergieProduktion, MaterialProduktion, Schaden, ViewDistance, MoveDistance, ActionDistance, Heal, Repair, Research
		
		for(UnitStatsContainer container : buildings) {
			sendUnitContainer(container, client);
		}
		for(UnitStatsContainer container : troups_air) {
			sendUnitContainer(container, client);
		}
		for(UnitStatsContainer container : troups_land) {
			sendUnitContainer(container, client);
		}
		
	}
	
	public static void sendUnitContainer(UnitStatsContainer container, ClientConnection client) {
		
		String descriptionData_en = container.description_en[0]+";"+container.description_en[1]+";"+container.description_en[2]+";"+container.description_en[3];
		String descriptionData_de = container.description_de[0]+";"+container.description_de[1]+";"+container.description_de[2]+";"+container.description_de[3];
		String data = container.name+";"+container.kürzel+";"+container.kosten+";"+container.leben+";"+container.energieVerbrauch+";"+container.energieProduktion+";"+container.materialProduktion+";"+container.schaden+";"+container.viewDistance+";"+container.moveDistance+";"+container.actionDistance+";"+container.heal+";"+container.repair+";"+container.research+";"+descriptionData_en+";"+descriptionData_de;
		client.sendData(110, data);
				
	}
	
	public static UnitStatsContainer getContainer_Troup(String name) {
		for(UnitStatsContainer container : troups_land) {
			if(container.name.equalsIgnoreCase(name)) {
				return container;
			}
		}
		for(UnitStatsContainer container : troups_air) {
			if(container.name.equalsIgnoreCase(name)) {
				return container;
			}
		}
		return null;
	}
	public static UnitStatsContainer getContainer_Building(String name) {
		for(UnitStatsContainer container : buildings) {
			if(container.name.equalsIgnoreCase(name)) {
				return container;
			}
		}
		return null;
	}
	
}
