package iristk.system;

import iristk.util.NameFilter;
import iristk.util.XmlMarshaller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IrisSystem implements EventListener {
	
	ArrayList<IrisModule> modules = new ArrayList<IrisModule>();
	private BrokerClient brokerClient;

	private String systemName;
	
	static int sessionId = (int) (System.currentTimeMillis() / 60000) - (40 * 365 * 24 * 60);
	public static XmlMarshaller<iristk.xml.event.Event> eventMarshaller = new XmlMarshaller<iristk.xml.event.Event>("iristk.xml.event");
	
	private long timestampOffset = 0;
	private ArrayList<IrisMonitor> monitors = new ArrayList<IrisMonitor>();
	private boolean running = false;
	private Integer brokerPort = 1932;
	private String brokerHost = "localhost";
	private ArrayBlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(1000);
	
	static int idCounter = 0;
	
	public IrisSystem(String name) throws Exception {
		this.systemName = name;
	}
	
	public void addMonitor(IrisMonitor monitor) {
		this.monitors.add(monitor);
	}
		
	public void addModule(String name, IrisModule module) throws InitializationException {
		module.setName(name);
		module.setSystem(this);
		modules.add(module);
		System.out.println("Initializing module " + module.getName());
		module.init();
	}
	
	// An event from the broker
	@Override
	public void onEvent(Event event) {
		distributeInternal(event);
	}
	 	
	public void connectToBroker() throws IOException {
		connectToBroker(brokerHost, brokerPort);
	}
	
	public void connectToBroker(String host, int port) throws IOException {
		brokerClient = new BrokerClient(systemName, host, port, this);
		brokerClient.connect();
	}

	private void distributeExternal(Event event) {
		if (brokerClient != null) {
			try {
				brokerClient.send(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	private void distributeInternal(Event event) {
		this.eventQueue.add(event);
		for (IrisModule module : modules) {
			if (module.subscribes.accepts(event.getName())) {
				module.invokeEvent(event);
			}
		}
	}
	
	private String generateId(String name) {
		return name + "." + IrisSystem.sessionId + "." + idCounter++;
	}
	
	public String getTimestamp() {
		return getTimestamp(System.currentTimeMillis());
	}
	
	public String getTimestamp(long time) {
		return new Timestamp(time - timestampOffset).toString();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void send(Event event, String sender) {
		event.setSender(sender);
		event.setTime(getTimestamp());
		event.setId(generateId(sender));
		monitorEvent(sender, event);
		distributeInternal(event);
		distributeExternal(event);
	}
	
	public void monitorEvent(String sender, Event event) {
		for (IrisMonitor monitor : monitors) {
			monitor.monitorEvent(sender, event);
		}
	}

	public void monitorState(String sender, String[] states) {
		for (IrisMonitor monitor : monitors) {
			monitor.monitorState(sender, states);
		}
	}
	
	public void setBrokerHost(String host) {
		this.brokerHost = host;
	}
	
	public void setBrokerPort(Integer port) {
		this.brokerPort = port;
	}
	
	public void setTimestampOffset(long offset) {
		this.timestampOffset = offset;
	}

	public void start() {
		updateSubscriptions();
		for (IrisModule module : modules) {
			module.start();
		}
		running  = true;
		send(new Event("monitor.system.start"), systemName);
		while (running) {
			try {
				Event message = eventQueue.poll(5, TimeUnit.MILLISECONDS);
				// TODO: handle system messages
			} catch (InterruptedException e) {
				break;
			}
		}
		for (IrisModule module : modules) {
			System.out.println("Stopping module " + module.getName());
			module.stop();
		}
	}
	
	public void stop() {
		running = false;
	}

	public void updateSubscriptions() {
		if (brokerClient != null) {
			NameFilter filter = NameFilter.NONE;
			for (IrisModule module : modules) {
				filter = filter.combine(module.subscribes);
			}
			try {
				brokerClient.subscribe(filter);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<IrisModule> getModules() {
		return modules;
	}

}
