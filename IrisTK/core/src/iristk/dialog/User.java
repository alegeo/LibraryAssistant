package iristk.dialog;

import iristk.util.Record;

public class User extends Record {

	public User(Object id, Object location) {
		put("id", id.toString());
		put("location", location.toString());
	}
	
	public String id() {
		return getString("id");
	}
	
	public String location() {
		return getString("location");
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
}
