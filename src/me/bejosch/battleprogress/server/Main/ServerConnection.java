package me.bejosch.battleprogress.server.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import me.bejosch.battleprogress.server.Handler.ProfileHandler;
import me.bejosch.battleprogress.server.Objects.ClientConnectionThread;
import me.bejosch.battleprogress.server.Objects.PlayerProfile;

public class ServerConnection {

	public static List<ClientConnectionThread> clientConnectionList = new ArrayList<ClientConnectionThread>();
	public static int PackageIdLength = 8;
	
	private static boolean sendCancleMessage = true;
	
//==========================================================================================================
	/**
	 * Initialise Online Connection
	 * @throws IOException 
	 */
	public static void initialiseServerSocket() {
		
		ServerSocket serverSocket = null;
		try {
			//serverSocket = ( (SSLServerSocketFactory) SSLServerSocketFactory.getDefault() ).createServerSocket(8998);
			serverSocket = BattleProgress_StartMain_Server.getSSLCOntext().getServerSocketFactory().createServerSocket(8998);
		} catch (IOException e1) {
			ConsoleOutput.printMessageInConsole("Starting up interrupted:", true);
			ConsoleOutput.printMessageInConsole("There is allready a running server on this address or port!", true);
			ConsoleOutput.printMessageInConsole(">Prozess stopped<", true);
			System.exit(0);
			return;
		} 
		
		ConsoleOutput.printMessageInConsole("ServerConnection is now running!", true);
		
		while(true) {
			
			try { //IF NEW CLIENT CONNECTS... CREATE NEW PLAYER WITH NEW CLIENT CONNECTION THREAD
				
				Socket playerSocket = serverSocket.accept();
				String message = null;
				
				try{
					BufferedReader testInput = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
					
					while(true) {
						try{
							message = testInput.readLine();
						}catch(SSLHandshakeException error) {
							//HAPPENS ON FIRST CONNECT
							break;
						}
						//PLAYER ERST ERSTELLEN WENN ERSTE VERBINDUNG ZU STANDE GEKOMMEN!
						if(message != null) {
							new ClientConnectionThread(playerSocket);
							ConsoleOutput.printMessageInConsole("Client connected! - "+message, true);
							message = null;
						}
						break;
						
					}
				}catch(SSLException error) {
					//IF WRONG OR NON IDENTIFICATION COMES FROM THE CLIENT
					ConsoleOutput.printMessageInConsole("A client tried to connect, but with a wrong security identification! [SSLExeption] - "+message, true);	
				}
				
			}catch(SocketException error) {
				//CONNECTION CANCLED BY CLIENT
				if(sendCancleMessage == true) {
					ConsoleOutput.printMessageInConsole("Connection cancled by the client! [SocketExeption]", true);
					sendCancleMessage = false;
				}else {
					sendCancleMessage = true;
				}
			}catch (SecurityException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
//==========================================================================================================
	/**
	 * Gives back a {@link ClientConnectionThread} which belongs to the given PlayerProfile if found
	 * @param profile - {@link PlayerProfile} - The PlayerProfile we want the {@link ClientConnectionThread} for
	 * @return {@link ClientConnectionThread} - The {@link ClientConnectionThread} which belongs to the given PlayerProfile if one is found (so he is online)
	 */
	public static ClientConnectionThread getConnectionFromPlayerProfile(PlayerProfile profile) {
		
		for(ClientConnectionThread connectionThread : clientConnectionList) {
			PlayerProfile playerProfile = ProfileHandler.getPlayerProfileByClientConnection(connectionThread);
			if(playerProfile != null) {
				if(playerProfile.getId() == profile.getId()) {
					return connectionThread;
				}
			}
		}
		
		return null;
	}
	
//==========================================================================================================
	/**
	 * Gives back a new PacketId
	 * @return int - The new PacketId
	 */
	public static int getNewPacketId() {
		
		//ALL NUMBERS BETWEEN MIN AND MAX COUNT
		return new Random().nextInt( (getMaxPacketIdCount()-getMinPacketIdCount()) )+getMinPacketIdCount();
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the min PacketId count
	 * @return int - The min PacketId count
	 */
	public static int getMinPacketIdCount() {
		
		int number = 1;
		for(int i = PackageIdLength ; i > 1 ; i--) {
			number = number*10;
		}
		return number;
		
	}
	
//==========================================================================================================
	/**
	 * Gives back the max PacketId count
	 * @return int - The max PacketId count
	 */
	public static int getMaxPacketIdCount() {
		
		int number = 1;
		for(int i = PackageIdLength+1 ; i > 1 ; i--) {
			number = number*10;
		}
		return number-1;
		
	}
	
}
