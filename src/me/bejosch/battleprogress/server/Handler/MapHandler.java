package me.bejosch.battleprogress.server.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.bejosch.battleprogress.server.Data.FileData;
import me.bejosch.battleprogress.server.Data.MapData;
import me.bejosch.battleprogress.server.Enum.GameType;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Objects.Map;

public class MapHandler {

	public static void loadMaps() {
		
		List<String> mapList = getMapsList();
		
		for(String name : mapList) {
			String fieldData = FileHandler.readOutData(FileData.file_Maps, name);
			new Map(name, fieldData);
		}
		
		ConsoleOutput.printMessageInConsole("Loaded "+MapData.loadedMaps.size()+"/"+mapList.size()+" map(s)!", true);
		
	}

	public static Map getRandomMap(GameType type) {
		
//		switch(type) {
//		case NORMAL_1v1:
//			break;
//		case NORMAL_2v2:
//			break;
//		case RANKED_1v1:
//			break;
//		default:
//			break;
//		}
		
		int mapPos = new Random().nextInt(MapData.loadedMaps.size());
		Map map = MapData.loadedMaps.get(mapPos);
		
		return map;
	}
	
	public static List<String> getMapsList() {
		
		String mapList = FileHandler.readOutData(FileData.file_Maps, "MapList");
		
		mapList = mapList.replace("[", "");
		mapList = mapList.replace("]", "");
		
		List<String> list = new ArrayList<String>();
		if(mapList.contains(", ")) {
			for(String result : mapList.split(", ")) {
				list.add(result);
			}
		}else {
			list.add(mapList);
		}
		
		return list;
	}
	
}
