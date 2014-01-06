package iristk.dialog;

import iristk.util.DynamicValue;
import iristk.util.RandomList;
import iristk.util.Record;

import java.util.*;

public class UserModel extends Record {

	public static final User NOBODY = new User("nobody", "nowhere");
	public static final User ALL = new User("all", "all");
	
	private User attending = NOBODY;
	
	public UserModel() {
		put("other", new DynamicValue() {
			@Override
			public Object getValue() {
				return other();
			}
		});
		put("random", new DynamicValue() {
			@Override
			public Object getValue() {
				return random();
			}
		});
		put("current", new DynamicValue() {
			@Override
			public Object getValue() {
				return current();
			}
		});
	}
	
	public List<User> users() {
		ArrayList<User> users = new ArrayList<>();
		for (Object user : values()) {
			if (user instanceof User) {
				users.add((User) user);
			}
		}
		return users;
	}
	
	public void setAttending(Object userId) {
		for (User user : users()) {
			if (user.id().equals(userId)) {
				attending = user;
				return;
			}
		}
		attending = NOBODY;
	}
	
	public void setAttendingAll() {
		attending = ALL;
	}
		
	public User getAttending() {
		return attending;
	}
	
	public boolean isAttending(Object user) {
		if (user instanceof User)
			return attending.id().equals(((User)user).id());
		else
			return attending.id().equals(user);
	}
	
	public boolean isAttendingAll() {
		return attending.id().equals("all");
	}
	
	public boolean isAttendingLocation(Object location) {		
		return attending.getString("location").equals(location);
	}
	
	public User userAt(Object location) {
		for (User user : users()) {
			if (user.get("location").equals(location)) {
				return user;
			}
		}
		return NOBODY;
	}
	
	public boolean hasUserAt(Object location) {
		for (User user : users()) {
			if (user.get("location").equals(location)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasMultiUsers() {
		return users().size() > 1;
	}
	
	public boolean hasUser() {
		return users().size() > 0;
	}
	
	public User current() {
		return attending;
	}
	
	public User other() {
		for (User user : users()) {
			if (user != attending) {
				return user;
			}
		}
		return NOBODY;
	}
	
	public User other(Object than) {
		for (User user : users()) {
			if (!equals(user, than)) {
				return user;
			}
		}
		return NOBODY;
	}

	public User user(Object than) {
		for (User user : users()) {
			if (equals(user, than)) {
				return user;
			}
		}
		return NOBODY;
	}

	public static boolean equals(Object user1, Object user2) {
		if (user1 == null && user2 == null)
			return true;
		else if (user1 == null || user2 == null)
			return false;
		String u1id = (user1 instanceof User ? ((User)user1).id() : user1.toString());
		String u2id = (user2 instanceof User ? ((User)user2).id() : user2.toString());
		return u1id.equals(u2id);
	}
	
	public User random() {
		List<User> users = users();
		RandomList.shuffle(users);
		for (User user : users) {
			return user;
		}
		return NOBODY;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

}
