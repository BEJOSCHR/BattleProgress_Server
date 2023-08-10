package me.bejosch.battleprogress.server.Main;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
		//IDENTIFICATION SECURE
		
		//SERVERCONNECTION:
		//https://www.youtube.com/watch?v=l4_JIIrMhIQ
		
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
		ServerConnection.initialiseServerSocket();
		
	}

//==========================================================================================================
	/**
	 * The SSLContext with the battleprogress.identification identification key
	 * @return SSLContext - The context
	 */
	public static SSLContext getSSLCOntext() {
		
		SSLContext sslContext = null;
		
		try {
			
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream keystoreStream = BattleProgress_StartMain_Server.class.getResourceAsStream("Identification/battleprogress.identification");
			//ConsoleOutput.printMessageInConsole("Identification Datei: "+keystoreStream, true);
			keystore.load(keystoreStream, "benno2001".toCharArray());
			trustManagerFactory.init(keystore);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, "benno2001".toCharArray());
			KeyManager[] keyManagers =kmf.getKeyManagers();
			TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(keyManagers, trustManagers, null);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		}
		
		return sslContext;
		
	}
	
}
