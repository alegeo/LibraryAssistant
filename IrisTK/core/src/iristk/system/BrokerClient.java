package iristk.system;

import iristk.util.NameFilter;
import iristk.util.ParsedInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BrokerClient {

	private Socket socket;
	private ParsedInputStream inFromServer;
	private DataOutputStream outToServer;
	private EventListener callback;
	private ClientThread clientThread;
	private String system;
	private String serverHost;
	private int serverPort;
	private boolean connected = false;
	private NameFilter brokerSubscribes = NameFilter.NONE;

	public BrokerClient(String system, String serverHost, int serverPort, EventListener callback) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.system = system;
		this.callback = callback;
	}
	
	public void connect() throws IOException {
		socket = new Socket(serverHost, serverPort);
		inFromServer = new ParsedInputStream(socket.getInputStream());
		outToServer = new DataOutputStream(socket.getOutputStream());
		sendToServer("CONNECT " + system + "\n");
		this.clientThread = new ClientThread();
		clientThread.start();
		for (int i = 0; i < 100; i++) {
			if (connected)
				return;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		throw new IOException("Connection to broker refused");
	}
	
	private class ClientThread extends Thread {

		@Override
		public void run() {
			try {
				while (true) {
					String line = inFromServer.readLine();
					if (line.startsWith("EVENT")) {
						String cols[] = line.split(" ");
						String label = cols[1];
						int length = Integer.parseInt(cols[2]);
						byte[] bytes = new byte[length];
						int pos = 0;
						do {
							pos = inFromServer.read(bytes, pos, length - pos) + pos;
						} while (pos < length);
						try {
							callback.onEvent(new Event(bytes));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (line.startsWith("CONNECTED")) {
						connected = true;
					} else if (line.startsWith("SUBSCRIBE")) {
						if (line.trim().length() == 9) {
							brokerSubscribes = NameFilter.NONE;	
						} else {
							String pattern = line.substring(10).trim();
							brokerSubscribes = NameFilter.compile(pattern);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send(Event event) throws IOException {
		if (brokerSubscribes.accepts(event.getName())) {
			byte[] bytes = event.getBytes();
			String header = "EVENT " + event.getName() + " " + bytes.length + "\n";
			sendToServer(header, bytes);
		}
	}
	
	public void subscribe(NameFilter filter) throws IOException {
		if (filter == null) 
			filter = NameFilter.NONE;
		sendToServer("SUBSCRIBE " + filter.toString() + "\n");
	}

	public void close() throws IOException {
		sendToServer("CLOSE\n");
	}
	
	private void sendToServer(String message) throws IOException {
		sendToServer(message, null);
	}

	private void sendToServer(String header, byte payload[]) throws IOException {
		synchronized ( outToServer ) {
			if ( header != null && header.length() > 0 ) {
				outToServer.writeBytes(header);
			}
			if ( payload != null && payload.length > 0 ) {
				outToServer.write(payload);
			}
		}
	}
}
