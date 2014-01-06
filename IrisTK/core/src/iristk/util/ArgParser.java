package iristk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ArgParser {

	HashMap<String,String> args = new HashMap<String,String>();
	HashMap<String,String> descs = new HashMap<String,String>();
	HashMap<String,String> arguments = new HashMap<String,String>();
	HashSet<String> required = new HashSet<String>();
	ArrayList<String> argList = new ArrayList<String>();
	
	public void addArg(String name, String desc, boolean required) {
		addArg(name, desc, null, required);
	}
	
	public void addArg(String name, String desc, String argument, boolean required) {
		this.descs.put(name, desc);
		this.argList.add(name);
		if (required)
			this.required.add(name);
		if (argument != null)
			this.arguments.put(name, argument);
	}

	public String get(String string) {
		return args.get(string);
	}

	public boolean has(String string) {
		return args.containsKey(string);
	}

	public boolean parse(String[] cmdargs) {
		for (int i = 0; i < cmdargs.length; i++) {
			String carg = cmdargs[i];
			if (carg.startsWith("-")) {
				String cargn = carg.substring(1);
				if (arguments.containsKey(cargn)) {
					if (descs.containsKey(cargn) && i < cmdargs.length - 1) {
						args.put(cargn, cmdargs[i+1]);
						i++;
					}
				} else {
					if (descs.containsKey(cargn)) {
						args.put(cargn, "true");
					} 
				}
			}
		}
		for (String arg : required) {
			if (!args.containsKey(arg))
				return false;
		}
		return true;
	}

	public String help() {
		StringBuilder help = new StringBuilder();
		for (String arg : argList) {
			String use = "-" + arg;
			if (arguments.containsKey(arg))
				use += " [" + arguments.get(arg) + "]";
			for (int i = use.length(); i < 20; i++) {
				use += " ";
			}
			help.append("  " + use + descs.get(arg));
			if (required.contains(arg)) 
				help.append(" (required)");
			else 
				help.append(" (optional)");
			help.append("\n");
		}
		return help.toString();
	}

}
