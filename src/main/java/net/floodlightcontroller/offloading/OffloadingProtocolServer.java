package net.floodlightcontroller.offloading;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import net.floodlightcontroller.util.MACAddress;

class OffloadingProtocolServer implements Runnable {

	protected static Logger log = LoggerFactory.getLogger(OffloadingProtocolServer.class);

	// Message types
	private final String MSG_PING = "ping";

	private final int SERVER_PORT;
	
	private DatagramSocket controllerSocket;
	private final ExecutorService executor;
	private final OffloadingMaster offloadingMaster;

	public OffloadingProtocolServer (OffloadingMaster om, int port, ExecutorService executor) {
		this.offloadingMaster = om; 
		this.SERVER_PORT = port;
		this.executor = executor;
	}
	
	@Override
	public void run() {
		
		try {
			controllerSocket = new DatagramSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true)	{
			
			try {
				final byte[] receiveData = new byte[1024]; // We can probably live with less
				final DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				controllerSocket.receive(receivedPacket);
				
				executor.execute(new ConnectionHandler(receivedPacket));
			}
			catch (IOException e) {
				log.error("controllerSocket.accept() failed: " + SERVER_PORT);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	/** Protocol handlers **/
	
	private void receivePing (final InetAddress AgentAddr) {
		offloadingMaster.receivePing(AgentAddr);
	}
	
	private class ConnectionHandler implements Runnable {
		final DatagramPacket receivedPacket;
		
		public ConnectionHandler(final DatagramPacket dp) {
			receivedPacket = dp;
		}
		
		// Agent message handler
		public void run() {			
			final String msg = new String(receivedPacket.getData()).trim().toLowerCase();
			final String[] fields = msg.split(" ");
			final String msg_type = fields[0];
			final InetAddress AgentAddr = receivedPacket.getAddress();
            
            if (msg_type.equals(MSG_PING)) {
            	receivePing(AgentAddr);
            }
		}
	}

}
