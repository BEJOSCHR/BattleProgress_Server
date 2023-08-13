package me.bejosch.battleprogress.server.Main;

import me.bejosch.battleprogress.server.Connection.MinaServer;
import me.bejosch.battleprogress.server.Data.StandardData;
import me.bejosch.battleprogress.server.Handler.DatabaseHandler;
import me.bejosch.battleprogress.server.Handler.FileHandler;
import me.bejosch.battleprogress.server.Handler.MapHandler;
import me.bejosch.battleprogress.server.Handler.ServerQueueHandler;
import me.bejosch.battleprogress.server.Handler.UnitsStatsHandler;
import me.bejosch.battleprogress.server.Handler.UpgradeDataHandler;

public class BattleProgress_StartMain_Server {

//==========================================================================================================
	/**
	 * With this methode everything starts ;D
	 */
	public static void main(String[] args) {
		
		int max = 8;
		
		ConsoleOutput.printMessageInConsole("Starting BattleProgress Server...", true);
		StandardData.timeSinceStartup = System.currentTimeMillis();
		
		ConsoleOutput.printMessageInConsole("1/"+max+" init files", true);
		FileHandler.firstWrite();
		
		ConsoleOutput.printMessageInConsole("2/"+max+" load Maps from files", true);
		MapHandler.loadMaps();
		
		ConsoleOutput.printMessageInConsole("3/"+max+" start DB", true);
		DatabaseHandler.connect();
		
		ConsoleOutput.printMessageInConsole("4/"+max+" load Units from DB", true);
		UnitsStatsHandler.updateUnitsList(true);
		
		ConsoleOutput.printMessageInConsole("5/"+max+" load Upgrades from DB", true);
		UpgradeDataHandler.updateUpgradeList(true);
		
		ConsoleOutput.printMessageInConsole("6/"+max+" start queue waiting timer", true);
		ServerQueueHandler.startQueueWaitTimer();
		
		ConsoleOutput.printMessageInConsole("7/"+max+" start input scanner", true);
		ConsoleOutput.startUserInputScanner();
		ConsoleOutput.printMessageInConsole("Type '/help' for console commands!", true);
		
		ConsoleOutput.printMessageInConsole("8/"+max+" init server socket", true);
		MinaServer.initConnection();
		
	}
	
}
