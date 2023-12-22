package me.bejosch.battleprogress.server.Objects;

import me.bejosch.battleprogress.server.Enum.FieldType;

public class Field {

	public FieldType type = FieldType.Flatland;
	public int X = 0;
	public int Y = 0;
	
	
//==========================================================================================================
	/**
	 * Creates a new Map object witch representing the game area later will be played on
	 * @param type - {@link FieldType} - The type of the field
	 * @param X - int - The X coordinate of the field
	 * @param Y - int - The Y coordinate of the field
	 */
	public Field(FieldType type, int X, int Y) {
		
		this.type = type;
		this.X = X;
		this.Y = Y;
		
	}
	
//==========================================================================================================
	/**
	 * Changes the type of the field
	 * @param newType - {@link FieldType} - The type this field should be changed to
	 */
	public void changeType(FieldType newType) {
		
		this.type = newType;
		
	}
	
}
