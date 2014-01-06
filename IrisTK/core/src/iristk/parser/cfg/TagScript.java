/*
* Copyright 2009-2010 Gabriel Skantze.
* All Rights Reserved.  Use is subject to license terms.
*
* See the file "license.terms" for information on usage and
* redistribution of this file, and for a DISCLAIMER OF ALL
* WARRANTIES.
*
*/
package iristk.parser.cfg;

import iristk.util.Record;

import java.util.HashMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class TagScript {

	private final Function function;
	private final Context jsContext;
	private final Scriptable jsScope;
	private static int tagCounter = 0;
	private static HashMap<String,TagScript> cache = new HashMap<String,TagScript>();
	
	public static TagScript newTagScript(String id, String tags) {
		if (!tags.endsWith(";"))
			tags = tags += ";";
		if (cache.containsKey(id)) {
			return cache.get(id);
		} else {
			TagScript script = new TagScript(id, tags);
			cache.put(id, script);
			return script;
		}
	}
	
	private TagScript(String id, String tags) {
		jsContext = Context.enter();
		jsScope = new ImporterTopLevel(jsContext);
		function = jsContext.compileFunction(jsScope, "function sem(rules, out) {" + tags + "return out}", id, 1, null);
		Context.exit();
	}

	public Object eval(Object rules, Object out) {
		 return function.call(jsContext, jsScope, jsScope, new Object[]{rules, out});
	}
	
	public static Object eval(String tagScript, RuleEdge ruleEdge, Object out) {
		if (tagScript.contains("=")) {
			TagScript script = TagScript.newTagScript("tag" + tagCounter++, tagScript);
			NativeObject rules = new NativeObject();
			for (RuleEdge re : ruleEdge.getSubRules()) {
				ScriptableObject.putProperty(rules, re.getRuleId(), re.getNativeSem());
			}
			if (out == null)
				out = new NativeObject();
			return script.eval(rules, out);
		} else {
			tagScript = tagScript.trim();
			try {
				return Integer.parseInt(tagScript);
			} catch (NumberFormatException e) {
				return tagScript;
			}
		}
	}
	
	public static Object jsObjectToSem(Object obj) {
		if (obj == null)
			return null;
		//System.out.println(obj);
		else if (obj instanceof NativeObject) {
			Record record = new Record();
			NativeObject no = (NativeObject) obj;
			for (Object pid : ScriptableObject.getPropertyIds(no)) {
				Object prop = ScriptableObject.getProperty(no,pid.toString());
				record.put(pid.toString(), jsObjectToSem(prop));
			}
			return record;
		} else {
			if (obj instanceof Number) {
				// TODO: this is actually Double, regardless of number type, we only create integers now
				return ((Number)obj).intValue();
			} else if (obj instanceof Boolean) {
				return obj;
			} else {
				return obj.toString();
			}
		}
	}
	
	public static Record nativeSemToRecord(Object nativeSem) {
		Object sem = jsObjectToSem(nativeSem);
		if (sem != null && sem instanceof Record)
			return (Record) sem;
		else
			return new Record();
	}

	public static void main(String[] args) {
		TagScript script = TagScript.newTagScript("53", "\"kalle\"");
		NativeObject rec = new NativeObject();
		ScriptableObject.putProperty(rec, "a", "b");
		NativeObject rules = new NativeObject();
		ScriptableObject.putProperty(rules, "kalle", rec);
		Object o = script.eval(rules, null);
		if (o instanceof NativeObject) {
			NativeObject no = (NativeObject) o; 
			System.out.println(ScriptableObject.getProperty(no, "_value"));
		} else {
			System.out.println(o);
		}
	}

	
}
