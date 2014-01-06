package iristk.system;

import iristk.util.NameFilter;
import iristk.util.ParsedInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

/*
> CONNECT maptask
< CONNECTED 
< SUBSCRIBE pattern
> SUBSCRIBE pattern

> EVENT speech.start 312
<event></event> 

EVENT sense.audio 312+320
<event name="sense.audio">
	<string name="stream">left</string>
	<binary name="data" part="1"/>
</event>

> CLOSE

 */

public class Broker extends Thread {

	private ServerSocket socket;
	private HashMap<String,BrokerSystem> systems = new HashMap<String,BrokerSystem>();
	private int port;

	public Broker(int port) {
		this.port = port;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("BrokerServer running on " + getIpAddress(InetAddress.getLocalHost()) + ":" + port);
			socket = new ServerSocket(port, 100);
			while (true) {
				try {
					Socket connected = socket.accept();
					//System.out.println("Connection");
					(new ServerClient(connected)).start();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void send(ServerClient sender, String label, byte[] message) {
		for (ServerClient client : systems.get(sender.system)) {
			if (sender != client) {
				if (client.subscribes.accepts(label)) {
					client.send(label, message);
				}
			}
		}
	}

	private synchronized void addClient(ServerClient client) {
		if (!systems.containsKey(client.system))
			systems.put(client.system, new BrokerSystem());
		systems.get(client.system).add(client);
	}

	private synchronized void removeClient(ServerClient client) {
		systems.get(client.system).remove(client);
		if (systems.get(client.system).size() == 0)
			systems.remove(client.system);
		updateSubscriptions(client.system);
	}

	private synchronized void updateSubscriptions(String system) {
		if (systems.containsKey(system)) {
			for (ServerClient fromClient : systems.get(system)) {
				NameFilter filter = NameFilter.NONE;
				for (ServerClient toClient : systems.get(system)) {
					if (fromClient != toClient) {
						filter = filter.combine(toClient.subscribes);
					}
				}
				if (fromClient.latestSubscribeMessage == null || !fromClient.latestSubscribeMessage.equals(filter)) {
					try {
						fromClient.outToClient.writeBytes("SUBSCRIBE " + filter + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					fromClient.latestSubscribeMessage = filter;
				}
			}
		}
		
	}
	
	private static String getIpAddress(InetAddress inetAddress) {
		String result = "";
		for (byte b : inetAddress.getAddress()) {
			int bi = b & 0xFF;
			if (result.length() > 0)
				result += ".";
			result += bi;
		}
		return result;
	}
	
	private class BrokerSystem extends ArrayList<ServerClient> {
		
	}
	
	private class ServerClient extends Thread {

		private Socket socket;
		private ParsedInputStream inFromClient = null;
		private DataOutputStream outToClient = null;
		private String system;
		private NameFilter subscribes = NameFilter.NONE;
		private NameFilter latestSubscribeMessage = null;

		public ServerClient(Socket socket) {
			this.socket = socket;
		}

		public synchronized void send(String label, byte[] message) {
			try {
				outToClient.writeBytes("EVENT " + label + " " + (message.length+1) + "\n");
				outToClient.write(new String(new String(message) + "\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void disconnect() {
			synchronized (Broker.this) {
				removeClient(this);
				try {
					inFromClient.close();
					outToClient.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {

			try {

				inFromClient = new ParsedInputStream(socket.getInputStream());
				outToClient = new DataOutputStream(socket.getOutputStream());

				HANDLE:
					while (true) {
						String line = inFromClient.readLine();
						//System.out.println(line);
						if (line != null) {
							if (line.startsWith("CONNECT")) {
								this.system = line.substring(8).trim();
								//if (hasClient(name)) {
								//	System.out.println("Connection from " + this.name + "@" + getIpAddress(socket.getInetAddress()) + " refused");
								//	disconnect();
								//} else {
									System.out.println("Connection to " + this.system + "@" + getIpAddress(socket.getInetAddress()));
									addClient(this);
									updateSubscriptions(system);
									outToClient.writeBytes("CONNECTED\n");
								//}
							} else if (line.startsWith("EVENT")) {
								String cols[] = line.split(" ");
								String label = cols[1];
								int length = Integer.parseInt(cols[2]);
								byte[] message = new byte[length];
								int pos = 0;
								do {
									pos = inFromClient.read(message, pos, length - pos) + pos;
								} while (pos < length);
								Broker.this.send(this, label, message);
							} else if (line.startsWith("CLOSE")) {
								System.out.println("Disconnection from " + this.system + "@" + getIpAddress(socket.getInetAddress()));
								disconnect();
								break HANDLE;
							} else if (line.startsWith("SUBSCRIBE")) {
								String pattern = line.substring(10).trim();
								this.subscribes = NameFilter.compile(pattern);
								updateSubscriptions(system);
								//subscribe = subscribe.replaceAll(".", "\\.");
								//subscribe = subscribe.replaceAll("*", ".*"); 
							}
						}
					}
			}  catch (SocketException e) {
				System.out.println("Disconnection from " + this.system + "@" + getIpAddress(socket.getInetAddress()));
				removeClient(this);
			}  catch (Exception e) {
				e.printStackTrace();
				removeClient(this);
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Broker(Integer.parseInt(args[0])).start();
		} else {
			new Broker(1932).start();
		}
	}

}
