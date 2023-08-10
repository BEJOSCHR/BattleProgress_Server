package me.bejosch.battleprogress.server.Funktions;

import java.util.ArrayList;
import java.util.List;

public class Funktions {
	
//==========================================================================================================
	/**
	 * Convert an String Pattern into an String ArrayList
	 * @param Input - String[] - The String Pattern shold be converted
	 * @return Output - ArrayList(String) - The converted ArrayList
	 */
	public static List<String> ArrayFromPattern(String[] Input) {
		List<String> Output = new ArrayList<String>();
		for(String Inhalt : Input) {
			Output.add(Inhalt);
		}
		return Output;
	}
	
	public static String doubleWriteNumber(int number) {
		
		if(number < 10) {
			return "0"+number;
		}else {
			return ""+number;
		}
		
	}
	
}
