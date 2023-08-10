package me.bejosch.battleprogress.server.Enum;

import java.awt.Color;

public enum FieldType {

	Gras,
	Water,
	Stone,
	Path,
	Ressource,
	RessourceVerbraucht;
	
	public static FieldType getFieldTypeFromSignal(String number) {
		
		switch(number) {
		case "g":
			return Gras;
		case "w":
			return Water;
		case "s":
			return Stone;
		case "p":
			return Path;
		case "r":
			return Ressource;
		case "v":
			return RessourceVerbraucht;
		}
		return Gras;
		
	}
	
	public static String getShortcutForFieldType(FieldType type) {
		
		switch(type) {
		case Gras:
			return "g";
		case Water:
			return "w";
		case Stone:
			return "s";
		case Path:
			return "p";
		case Ressource:
			return "r";
		case RessourceVerbraucht:
			return "v";
		default:
			break;
		}
		return "g";
		
	}
	
	public static Color getMiniMapColorForFieldType(FieldType type) {
		
		switch(type) {
		case Gras:
			return new Color(34, 139, 34, 100);
		case Water:
			return new Color(0, 0, 238, 100);
		case Stone:
			return new Color(193, 205, 205, 100);
		case Path:
			return new Color(139, 71, 38, 100);
		case Ressource:
			return new Color(255, 215, 0, 100);
		case RessourceVerbraucht:
			return new Color(0, 0, 0, 100);
		default:
			break;
		}
		return new Color(50, 205, 50, 100);
		
	}
	
}
