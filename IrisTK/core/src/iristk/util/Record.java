package iristk.util;

import iristk.xml.event.RecordType;
import static iristk.util.Converters.*;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * A Record is essentially a map of key-value pairs (much like any java {@link java.util.Map}). However, it supports a convenient way of adding and accessing values deep in the hierarchy by using colon (:) notation.
 * <p> For example you can add a value like this: {@code myRecord.put("foo:bar", 5)}. If the key "foo" does not already contain a nested Record, it will create one. It will then put the value 5 under the key "bar". 
 * <p> You can also access values like this  {@code myRecord.get("foo:bar")}. If it is not possible to traverse "foo" and then "bar" in the nested hierarchy, the method will return null.
 * <p> There are also convenience functions for getting a value of the right type. For example, {@code myRecord.getInt("foo:bar")} will try to convert the value to an Integer. If this fails the method will return null.
 * <p> Using kleen stars (*), it is also possible to search the hierarchy using the {@code has()} method. Thus, {@code myRecord.has("*:bar")} will return true.   
 *  */
public class Record {

	private HashMap<String, Object> map = new HashMap<String,Object>();
	//private List<RecordListener> listeners = new ArrayList<RecordListener>();
	
	public Record() {
	}
	
	public Record(RecordType recordType) {
		putAll(recordType);
	}
	
	public Record(Map map) {
		this();
		putAll(map);
	}
	
	public Record(Object... init) {
		if (init.length % 2 == 1)
			throw new IllegalArgumentException("Can only initialize Record with even number of arguments");
		for (int i = 0; i < init.length; i += 2) {
			put(init[i].toString(), init[i+1]);
		}
	}
	
	public Record(Record initRecord, Object... init) {
		if (init.length % 2 == 1)
			throw new IllegalArgumentException("Can only initialize Record with even number of arguments");
		putAll(initRecord);
		for (int i = 0; i < init.length; i += 2) {
			put(init[i].toString(), init[i+1]);
		}
	}
	
	public void putAll(Record record) {
		map.putAll(record.map);
		notifyListeners();
	}
	
	public void putAll(Map map) {
		for (Object key : map.keySet()) {
			this.map.put(key.toString(), map.get(key));
		}
		notifyListeners();
	}
	
	private void notifyListeners() {
	//	for (RecordListener listener : listeners) {
	//		listener.recordChanged();
	//	}
	}

	public void putAll(RecordType recordType) {
		for (Object object : recordType.getRecordOrListOrString()) {
			Pair<String, Object> pair = parseValue(object);
			put(pair.getFirst(), pair.getSecond());
		}
		notifyListeners();
	}

	public static Pair<String, Object> parseValue(Object element) {
		if (element instanceof iristk.xml.event.Bool) {
			iristk.xml.event.Bool o = (iristk.xml.event.Bool) element;
			return new Pair<String, Object>(o.getName(), o.isValue());
		} else if (element instanceof iristk.xml.event.String) {
			iristk.xml.event.String o = (iristk.xml.event.String) element;
			return new Pair<String, Object>(o.getName(), o.getValue());
		} else if (element instanceof iristk.xml.event.Float) {
			iristk.xml.event.Float o = (iristk.xml.event.Float) element;
			return new Pair<String, Object>(o.getName(), o.getValue());
		} else if (element instanceof iristk.xml.event.Int) {
			iristk.xml.event.Int o = (iristk.xml.event.Int) element;
			return new Pair<String, Object>(o.getName(), o.getValue());
		} else if (element instanceof iristk.xml.event.Record) {
			iristk.xml.event.Record o = (iristk.xml.event.Record) element;
			return new Pair<String, Object>(o.getName(), new Record(o));
		} else if (element instanceof iristk.xml.event.List) {
			iristk.xml.event.List o = (iristk.xml.event.List) element;
			ArrayList<Object> list = new ArrayList<Object>();
			for (Object object : o.getRecordOrListOrString()) {
				Pair<String, Object> pair = parseValue(object);
				list.add(pair.getSecond());
			}
			return new Pair<String, Object>(o.getName(), list);
		} else if (element instanceof iristk.xml.event.Xml) {
			iristk.xml.event.Xml o = (iristk.xml.event.Xml) element;
			return new Pair<String, Object>(o.getName(), o);
		} 
		return null;
	}
	
	private Object getDynamic(String field) {
		Object sub = map.get(field);
		while (sub != null && sub instanceof DynamicValue) {
			sub = ((DynamicValue)sub).getValue();
		}
		return sub;
	}

	public Object get(String field) {
		if (field.contains(".")) {
			System.err.println("Warning: records do not support dots: " + field);
			field = field.replace(".", ":");
		}
		if (field.contains(":")) {
			int i = field.indexOf(":");
			String subf = field.substring(0, i);
			String rest = field.substring(i + 1);
			Object sub = getDynamic(subf);
			if (sub != null && sub instanceof Record) {
				return ((Record) sub).get(rest);
			} else {
				return null;
			}
		} else {
			return getDynamic(field);
		}
	}
	
	public void put(String field, Object value) {
		if (field != null) {
			if (field.contains(".")) {
				System.err.println("Warning: records do not support dots: " + field);
				field = field.replace(".", ":");
			}
			if (field.contains(":")) {
				int i = field.indexOf(":");
				String f = field.substring(0, i);
				String rest = field.substring(i + 1);
				Record subRec = getRecord(f);
				if (subRec != null) {
					subRec.put(rest, value);
				} else {
					Record record = new Record();
					map.put(f, record);
					record.put(rest, value);
				}
			} else {
				map.put(field, value);
			}
			notifyListeners();
		}
	}
	
	public void put(String field) {
		this.put(field, true);
	}
	
	public boolean has(String field) {
		if (field.contains(".")) {
			System.err.println("Warning: records do not support dots: " + field);
			field = field.replace(".", ":");
		}
		if (field.contains(":")) {
			int i = field.indexOf(":");
			String subField = field.substring(0, i);
			String rest = field.substring(i + 1);
			if (subField.equals("*")) {
				for (String key : map.keySet()) {
					Object sub = getDynamic(key);
					if (sub instanceof Record) {
						if (((Record) sub).has(rest)) return true;
					}
				}
				return false;
			} else {
				Object sub = get(subField);
				if (sub instanceof Record) {
					return ((Record) sub).has(rest);
				} else {
					return false;
				}
			}
		} else {
			return field.equals("*") || map.containsKey(field);
		}
	}
	/*
	public boolean eq(String field, Object... values) {
		Object comp = get(field);
		if (comp == null)
			return false;
		for (Object value : values) {
			if (value != null && comp.equals(value))
				return true;
		}
		return false;
	}
	
	public boolean regex(String field, String... values) {
		String comp = getString(field);
		if (comp == null)
			return false;
		for (String value : values) {
			if (value != null && comp.matches(value))
				return true;
		}
		return false;
	}
	*/
	public Object call(String field, String methodName, Object... args) {
		try {
			Object object = get(field);
			for (Method method : object.getClass().getMethods()) {
				if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length) {
					return method.invoke(object, args);
				}
			}
			System.err.println("Could not find a method named " + methodName + " which takes " + args.length + " arguments");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 
	
	public Object get(String field, Object def) {
		Object value = get(field);
		if (value != null)
			return value;
		else
			return def;
	}

	public String getString(String field, String def) {
		return makeString(get(field), def);
	}
	
	public String getString(String field) {
		return makeString(get(field));
	}

	public Float getFloat(String field, Float def) {
		return makeFloat(get(field), def);
	}

	public Float getFloat(String field) {
		return makeFloat(get(field));
	}

	public Boolean getBool(String field, Boolean def) {
		return makeBool(get(field), def);
	}

	public Boolean getBool(String field) {
		return makeBool(get(field));
	}

	public Integer getInt(String field, Integer def) {
		return makeInt(get(field), def);
	}

	public Integer getInt(String field) {
		return makeInt(get(field));
	}
	
	public Record getRecord(String field) {
		return makeRecord(get(field));
	}
	
	public List getList(String field) {
		return makeList(get(field));
	}
	
	public Set<String> keySet() {
		return map.keySet();
	}
	
	public int size() {
		return map.size();
	}
	
	public Collection<Object> values() {
		return map.values();
	}
	
	//TODO: should support nested keys
	public void remove(Object key) {
		remove(key.toString());
	}
	
	public void remove(String key) {
		notifyListeners();
		map.remove(key);
	}
	
	public String getKey(int i) {
		Iterator<String> keys = keySet().iterator();
		String result = null;
		for (int j = 0; j <= i; j++) {
			if (keys.hasNext()) 
				result = keys.next();
		}
		return result;
	}

	public void toXmlRecord(RecordType recordType) {
		for (String field : this.keySet()) {
			Object value = makeXmlValue(field, this.get(field));
			if (value != null)
				recordType.getRecordOrListOrString().add(value);
		}
	}

	/**
	 * Converts the Record to an object that can be serialized to XML using the iristk.xml.event namespace.
	 */
	private static Object makeXmlValue(String field, Object value) {
		if (value instanceof String) {
			iristk.xml.event.String object = new iristk.xml.event.String();
			object.setValue((String) value);
			if (field != null)
				object.setName(field);
			return object;
		} else if (value instanceof Integer) {
			iristk.xml.event.Int object = new iristk.xml.event.Int();
			object.setValue((Integer) value);
			if (field != null)
				object.setName(field);
			return object;
		} else if (value instanceof Float) {
			iristk.xml.event.Float object = new iristk.xml.event.Float();
			object.setValue((Float) value);
			if (field != null)
				object.setName(field);
			return object;
		} else if (value instanceof Boolean) {
			iristk.xml.event.Bool object = new iristk.xml.event.Bool();
			object.setValue((Boolean) value);
			if (field != null)
				object.setName(field);
			return object;
		} else if (value instanceof Record) {
			iristk.xml.event.Record object = new iristk.xml.event.Record();
			((Record) value).toXmlRecord(object);
			if (field != null)
				object.setName(field);
			return object;
		} else if (value instanceof List) {
			iristk.xml.event.List xmlList = new iristk.xml.event.List();
			for (Object c : (List) value) {
				Object xc = makeXmlValue(null, c);
				if (xc != null)
					xmlList.getRecordOrListOrString().add(xc);
			}
			if (field != null)
				xmlList.setName(field);
			return xmlList;
		} else if (value instanceof Node) {
			iristk.xml.event.Xml object = new iristk.xml.event.Xml();
			NodeList list = ((Node) value).getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				object.getContent().add(list.item(i));
			}
			if (field != null)
				object.setName(field);
			return object;
		} 
		return null;
	}
	
	public void inspect() {
		new RecordViewer(this).setVisible(true);
	}

	//public void addListener(RecordListener listener) {
	//	listeners.add(listener);
	//}
	
	@Override
	public String toString() {
		return map.toString();
	}
	
	public static void main(String[] args) {
		Record r = new Record();
		r.put("a.b.c", "hej");
		System.out.println(r.has("*.*"));
	}

	/**
	 * Converts the Record to a JSON compatible String.
	 */
	public String toJSON() {
		String json = "{";
		for (String key : keySet()) {
			if (json.length() > 1)
				json += ", ";
			Object value;
			if (get(key) instanceof Record) {
				value = ((Record)get(key)).toJSON();
			} else {
				value = "\"" + get(key).toString() + "\"";
			}
			json += "\"" + key + "\": " + value; 
		}
		json += "}";
		return json;
	}
	
	/**
	 * Converts the JSON compatible String to a Record.
	 */
	public static Record fromJSON(String jsonString) {
		Record record = new Record();
		JsonObject jsonObject = JsonObject.readFrom(jsonString);
		parseJSON(jsonObject, record);
		return record;
	}
	
	public static Record fromJSON(Reader reader) throws IOException {
		Record record = new Record();
		JsonObject jsonObject = JsonObject.readFrom(reader);
		parseJSON(jsonObject, record);
		return record;
	}
	
	private static void parseJSON(JsonObject jsonObject, Record record) {
		for(String name : jsonObject.names()) {
			record.put(name, parseJSON(jsonObject.get(name)));
		}
	}
	
	private static Object parseJSON(JsonValue value) {
		if (value.isObject()) {
			Record rec = new Record();
			parseJSON(value.asObject(), rec);
			return rec;
		} else if (value.isNumber()) {
			try {
				return value.asInt();
			} catch (NumberFormatException e) {
				return value.asFloat();
			}
		} else if (value.isBoolean()) {
			return value.asBoolean();
		} else if (value.isArray()) {
			ArrayList<Object> array = new ArrayList<Object>();
			JsonArray ja = value.asArray();
			for (int i = 0; i < ja.size(); i++) {
				array.add(parseJSON(ja.get(i)));
			}
			return array;
		} else {
			return value.asString();
		}
	}

	public String toStringIndent() {
		return toStringIndent(1);
	}
	
	protected String toStringIndent(int level) {
		String result = "";
		for (String key : keySet()) {
			if (result.length() > 0)
				result += "\n";
			for (int i = 0; i < level; i++)
				result += "  ";
			result += key + ": ";
			if (get(key) instanceof Record) {
				result += getRecord(key).toStringIndent(level + 1);
			} else {
				result += get(key);
			}
		}
		//result += "}";
		return result;
	}

}
