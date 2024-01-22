package me.bejosch.battleprogress.server.Connection;

import java.net.SocketException;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import me.bejosch.battleprogress.server.Data.ConnectionData;
import me.bejosch.battleprogress.server.Handler.ProfileHandler;
import me.bejosch.battleprogress.server.Handler.ServerPlayerHandler;
import me.bejosch.battleprogress.server.Main.ConsoleOutput;
import me.bejosch.battleprogress.server.Objects.ClientConnection;
import me.bejosch.battleprogress.server.Objects.ServerPlayer;

public class MinaServerEvents extends IoHandlerAdapter {

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
	    if(!(cause instanceof SocketException)) {
	    	cause.printStackTrace();
	    }
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		
		new ClientConnection(session);
		ConsoleOutput.printMessageInConsole("Client connected!", true);
		
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		
		ServerPlayer disconnectedPlayer = ServerPlayerHandler.getOnlinePlayer(session);
		if(disconnectedPlayer != null) {
			ProfileHandler.profileDisconnect(disconnectedPlayer.getProfile());
		}else {
			for(ClientConnection connection : ConnectionData.clientConnectionList) {
				if(connection.session == session) {
					ConnectionData.clientConnectionList.remove(connection);
					ConsoleOutput.printMessageInConsole("Client without login disconnected!", true);
					return;
				}
			}
			ConsoleOutput.printMessageInConsole("Unknown Client disconnected!", true);
		}
		
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		
		String messageRaw = message.toString();
		ServerPlayer player = ServerPlayerHandler.getOnlinePlayer(session);
		if(player != null) {
			player.getProfile().getConnection().handlePackage(messageRaw);
		}else {
			for(ClientConnection connection : ConnectionData.clientConnectionList) {
				if(connection.session == session) {
					connection.handlePackage(messageRaw);
					return;
				}
			}
			ConsoleOutput.printMessageInConsole("Unknown Client for message! ("+messageRaw+")", true);
		}
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {}
	
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {}
	
}
