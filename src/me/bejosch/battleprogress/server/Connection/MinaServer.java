package me.bejosch.battleprogress.server.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import me.bejosch.battleprogress.server.Data.ConnectionData;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;

public class MinaServer {
	
	private static NioSocketAcceptor acceptor;
	
	public static void initConnection() {
		
		try {
			acceptor = new NioSocketAcceptor();
//			acceptor.getFilterChain().addLast("logger", new LoggingFilter()); //USED TO GET DEBUG LOG IN CONSOLE
			acceptor.getFilterChain().addLast("codec",  new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName(ConnectionData.ENCODING)))); 
			acceptor.setHandler(new MinaServerEvents());
			acceptor.bind(new InetSocketAddress(ConnectionData.PORT));
			ConsoleOutput.printMessageInConsole("ServerConnection is now running!", true);
		} catch(IOException error) {
			ConsoleOutput.printMessageInConsole("Starting up interrupted:", true);
			ConsoleOutput.printMessageInConsole("There is allready a running server on this port!", true);
			ConsoleOutput.printMessageInConsole(">Prozess stopped<", true);
			System.exit(0);
		} catch(Exception error) {
			error.printStackTrace();
			ConsoleOutput.printMessageInConsole("Something went wrong during connection startup!", true);
			ConsoleOutput.printMessageInConsole(">Prozess stopped<", true);
			System.exit(0);
		}
		
	}
	
	public static void closeConnection() {
		
		if(acceptor != null) {
			acceptor.unbind();
			acceptor.dispose();
			acceptor = null;
		}
		
	}
}
