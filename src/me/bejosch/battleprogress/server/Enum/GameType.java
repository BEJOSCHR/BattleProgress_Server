package me.bejosch.battleprogress.server.Enum;

public enum GameType {

	Ranked_1v1,
	Normal_1v1,
	Normal_2v2,
	Custom_1v1,
	Custom_2v2;
	
	public static boolean isModus1v1(GameType modus) {
		
		if(modus == GameType.Custom_1v1 || modus == GameType.Normal_1v1 || modus == GameType.Ranked_1v1) {
			return true;
		}else {
			return false;
		}
		
	}
	
}
