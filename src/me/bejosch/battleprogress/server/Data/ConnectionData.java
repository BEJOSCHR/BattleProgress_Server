package me.bejosch.battleprogress.server.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.bejosch.battleprogress.server.Objects.ClientConnection;

public class ConnectionData {

	public static final int PORT = 8998;
	public static final String ENCODING = "UTF-8";
	public static final int BUFFER_SIZE = 32768;
	public static final int PACKAGEIDLENGTH = 8;
	
	public static List<ClientConnection> clientConnectionList = new ArrayList<ClientConnection>();
	
	
	public static long getNewPacketId() {
		
		//ALL NUMBERS BETWEEN 10000000 AND 100000000 COUNT
		long min = (long) Math.pow(10, PACKAGEIDLENGTH-1);
		long max = (long) Math.pow(10, PACKAGEIDLENGTH);
		return new Random().nextInt( (int) (max-min) ) + min;
		
	}
	
}
