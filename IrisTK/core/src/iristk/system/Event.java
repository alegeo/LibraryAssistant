package iristk.system;

import iristk.util.NameFilter;
import iristk.util.Record;

import javax.xml.bind.JAXBException;

public class Event extends Record {
	
	private String name;
	private String sender;
	private String id;
	private String time;
	
	public Event(String name) {
		setName(name);
	}
	
	public Event(String name, Record parameters) {
		setName(name);
		putAll(parameters);
	}
	
	public Event(String name, Object... params) {
		if (params.length % 2 != 0)
			throw new IllegalArgumentException("Must pass an even number of parameters");
		for (int i = 0; i < params.length; i += 2) {
			put(params[i].toString(), params[i]);
		}
		setName(name);
	}
	
	public Event(byte[] bytes) throws JAXBException {
		this(IrisSystem.eventMarshaller.unmarshal(bytes));
	}

	public Event(iristk.xml.event.Event event) {
		setName(event.getName());
		setId(event.getId());
		setSender(event.getSender());
		setTime(event.getTime());
		putAll(new Record(event));
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public String getTime() {
		return time;
	}
	
	public boolean triggers(String trigger) {
		NameFilter nptrigger = NameFilter.compile(trigger);
		return (nptrigger.accepts(name));
	}
	
	public String toXmlString() {
		try {
			iristk.xml.event.Event event = new iristk.xml.event.Event();
			this.toXmlRecord(event);
			event.setId(getId());
			event.setName(getName());
			event.setTime(getTime());
			event.setSender(getSender());
			return IrisSystem.eventMarshaller.marshal(event);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getBytes() {
		return toXmlString().getBytes();
	}
	
	@Override
	public String toString() {
		return name + " " + super.toString();
	}
	
}
