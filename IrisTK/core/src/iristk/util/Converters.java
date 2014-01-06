package iristk.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Converters {

	public static String makeString(Object object) {
		if (object == null)
			return null;
		else
			return object.toString();
	}

	public static String makeString(Object object, String def) {
		if (object == null)
			return def;
		else
			return object.toString();
	}	

	public static boolean makeBool(Object object) {
		if (object == null)
			return false;
		else if (object instanceof Boolean) 
			return (Boolean)object;
		else if (object instanceof Float) 
			return ((Float)object) != 0;
		else if (object instanceof Integer) 
			return ((Integer)object) != 0;
		else if (object instanceof Double) 
			return ((Double)object) != 0;
		else if (object instanceof String) 
			return !((String)object).equalsIgnoreCase("false");
		else if (object instanceof Collection) 
			return ((Collection)object).size() > 0;
			else return true;
	}

	public static boolean makeBool(Object object, boolean def) {
		if (object == null)
			return def;
		else
			return makeBool(object);
	}

	public static List makeList(Object object) {
		if (object == null)
			return null;
		else if (object instanceof List)
			return (List)object;
		else if (object instanceof Collection)
			return new ArrayList((Collection)object);
		else 
			return Arrays.asList(object);
	}

	public static Record makeRecord(Object object) {
		if (object == null)
			return null;
		else if (object instanceof Record)
			return (Record)object;
		else if (object instanceof Map)
			return new Record((Map)object);
		else 
			return null;
	}

	public static Float makeFloat(Object object, Float def) {
		Float f = makeFloat(object);
		if (f == null)
			return def;
		else
			return f;
	}

	public static Float makeFloat(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number)object).floatValue();
		} else if (object instanceof String) {
			try {
				return Float.parseFloat((String)object);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	public static Double makeDouble(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number)object).doubleValue();
		} else if (object instanceof String) {
			try {
				return Double.parseDouble((String)object);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
	
	public static Integer makeInt(Object object, Integer def) {
		Integer i = makeInt(object);
		if (i == null)
			return def;
		else
			return i;
	}

	public static Integer makeInt(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number)object).intValue();
		} else if (object instanceof String) {
			try {
				return Integer.parseInt((String)object);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

}
