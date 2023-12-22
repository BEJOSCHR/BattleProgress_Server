package me.bejosch.battleprogress.server.Enum;

import java.awt.Color;

public enum FieldType {

	Flatland,
	Ocean,
	Mountain,
	Path,
	Ressource,
	Consumed;
	
	public static FieldType getFieldTypeFromSignal(String number) {
		
		switch(number) {
		case "g":
			return Flatland;
		case "w":
			return Ocean;
		case "s":
			return Mountain;
		case "p":
			return Path;
		case "r":
			return Ressource;
		case "v":
			return Consumed;
		}
		return Flatland;
		
	}
	
	public static String getShortcutForFieldType(FieldType type) {
		
		switch(type) {
		case Flatland:
			return "g";
		case Ocean:
			return "w";
		case Mountain:
			return "s";
		case Path:
			return "p";
		case Ressource:
			return "r";
		case Consumed:
			return "v";
		default:
			break;
		}
		return "g";
		
	}
	
	public static Color getMiniMapColorForFieldType(FieldType type) {
		
		switch(type) {
		case Flatland:
			return new Color(34, 139, 34, 100);
		case Ocean:
			return new Color(0, 0, 238, 100);
		case Mountain:
			return new Color(193, 205, 205, 100);
		case Path:
			return new Color(139, 71, 38, 100);
		case Ressource:
			return new Color(255, 215, 0, 100);
		case Consumed:
			return new Color(0, 0, 0, 100);
		default:
			break;
		}
		return new Color(50, 205, 50, 100);
		
	}
	
}
