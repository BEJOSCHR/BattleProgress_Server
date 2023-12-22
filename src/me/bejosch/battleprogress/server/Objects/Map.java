package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Data.MapData;
import me.bejosch.battleprogress.server.Data.ServerData;
import me.bejosch.battleprogress.server.Enum.FieldType;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class Map {
	
	public String name;
	public Field[][] fieldList;
	
	public boolean loadedSuccessfully = true; //ONLY FALSE IF ERROR OCCURRED
	
//==========================================================================================================
	/**
	 * Creates a new Map object witch representing the game area later will be played on
	 * @param mapData - String - The data of an other map witch following a syntax
	 */
	public Map(String name, String fieldData) {
		
		this.name = name;
		this.fieldList = new Field[ServerData.mapWidth][ServerData.mapHight];
		fillFieldList();
		readOutFieldData(fieldData);
		
		if(loadedSuccessfully == true) {
			MapData.loadedMaps.add(this);
		}
		return;
	}
	
//==========================================================================================================
	/**
	 * First preset fill fieldList with Grass
	 */
	public void fillFieldList() {
		
		for(int x = 0 ; x < ServerData.mapWidth ; x++) {
			for(int y = 0 ; y < ServerData.mapHight ; y++) {
				fieldList[x][y] = new Field(FieldType.Flatland, x, y);
			}
		}
		
	}
	
//==========================================================================================================
	/**
	 * Switches the type of the given Field
	 * @param X - int - The X-Coordinate of the Field
	 * @param Y - int - The Y-Coordinate of the Field
	 * @param newType - {@link FieldType} - The new Type of the Field
	 */
	public void switchFieldType(int X, int Y, FieldType newType) {
		
		fieldList[X][Y].changeType(newType);
		
	}
	
//==========================================================================================================
	/**
	 * Load the field data out of the String
	 * @param fieldData - String - The data of the fields
	 */
	public void readOutFieldData(String fieldData) {
		
		try {
			String[] data = fieldData.split("-");
			for(String field : data) {
				String[] fieldValues = field.split(":");
				FieldType type = FieldType.getFieldTypeFromSignal(fieldValues[0]);
				int X = Integer.parseInt(fieldValues[1]);
				int Y = Integer.parseInt(fieldValues[2]);
				fieldList[X][Y] = new Field(type, X, Y);
			}
		}catch(NullPointerException | IndexOutOfBoundsException error) {
//			ConsoleOutput.printMessageInConsole("An error occurred while decoding map "+this.name+"! [FieldData: "+fieldData+"]", true);
			ConsoleOutput.printMessageInConsole("An error occurred while decoding map "+this.name+"!", true);
			this.loadedSuccessfully = false;
		}
		
	}
	
//==========================================================================================================
	/**
	 * Converts the fieldList with a syntax into a String
	 * @return String - The string with the field data
	 */
	public String convertFieldListIntoStringSyntax() {
		
		String output = "";
		
		for(int x = 0 ; x < ServerData.mapWidth ; x++) {
			for(int y = 0 ; y < ServerData.mapHight ; y++) {
				
				Field field = fieldList[x][y];
				
				if(field.type != FieldType.Flatland) {
					if(output.length() > 2) { //NICHT DAS ERSTE FELD
						output = output+"-"+FieldType.getShortcutForFieldType(field.type)+":"+field.X+":"+field.Y;
					}else { //DAS ERSTE FELD OHNE -
						output = FieldType.getShortcutForFieldType(field.type)+":"+field.X+":"+field.Y;
					}
				}
				
			}
		}
		
		return output;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the field on the coordinates or null
	 * @param X - int - X coordiante
	 * @param Y - int - Y coordiante
	 * @return Field(object) if on this coordiantes have a field or null if not
	 */
	public Field getFieldByCoordinate(int X, int Y) {
		
		return fieldList[X][Y];
		
	}
	
}
