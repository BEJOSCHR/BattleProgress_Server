package me.bejosch.battleprogress.server.Handler;

import java.util.Random;

import me.bejosch.battleprogress.server.Data.ServerGameData;
import me.bejosch.battleprogress.server.Objects.ServerGame;

public class ServerGameHandler {

	public static ServerGame getGameByID(int gameID) {
		
		for(ServerGame game : ServerGameData.runningGames) {
			if(game.getId() == gameID) {
				return game;
			}
		}
		return null;
		
	}
	public static ServerGame getGameByPlayerID(int playerID) {
		
		for(ServerGame game : ServerGameData.runningGames) {
			if(game.getPlayerById(playerID) != null) {
				return game;
			}
		}
		return null;
		
	}
	
	public static ServerGame getOldGame(int gameID) {
		
		for(ServerGame game : ServerGameData.oldFinishedGames) {
			if(game.getId() == gameID) {
				return game;
			}
		}
		return null;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back a new GameId
	 * @return int - The new GameId
	 */
	public static int getNewGameId() {
		
		int id = new Random().nextInt( (getMaxGameIdCount()-getMinGameIdCount()) )+getMinGameIdCount();
		for(ServerGame game : ServerGameData.runningGames) {
			if(game.getId() == id) { //CHECK WHETHER GAME ID IS FREE
				return getNewGameId();
			}
		}
		return id;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the min GameId count
	 * @return int - The min GameId count
	 */
	private static int getMinGameIdCount() {
		
		int number = 1;
		for(int i = ServerGameData.gameIdLength; i > 1 ; i--) {
			number = number*10;
		}
		return number;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the max GameId count
	 * @return int - The max GameId count
	 */
	private static int getMaxGameIdCount() {
		
		int number = 1;
		for(int i = ServerGameData.gameIdLength+1 ; i > 1 ; i--) {
			number = number*10;
		}
		return number-1;
		
	}
	
}
