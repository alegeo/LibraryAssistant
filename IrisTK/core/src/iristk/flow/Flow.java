package iristk.flow;

import iristk.util.InMemoryCompiler;
import iristk.util.RandomList.RandomModel;
import iristk.util.RandomMap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Flow {
	
	private RandomMap randomMap = new RandomMap();

	protected State getInitialState() {
		return null;
	}
		
	public abstract Object getVariable(String name);
	
	public abstract void setVariable(String name, Object value);
	
	protected int random(int seed, int n) {
		return randomMap.next(seed, n);
	}
	
	protected int random(int seed, int n, RandomModel model) {
		return randomMap.next(seed, n, model);
	}
	
	protected int randomInterval(int min, int max) {
		return randomMap.getInt(1 + max - min) + min;
	}
	
	protected void log(String string) {
		System.out.println(string);
	}
	
	protected List newList(Object... init) {
		List result = new ArrayList();
		for (Object item : init) {
			result.add(item);
		}
		return result;
	}
	
	protected static boolean eq(Object s1, Object s2) {
		return (s1 == s2 || (s1 != null && s2 != null && s1.equals(s2)));
	}
	
	/*
	protected Record parameters(Object... args) {
		Record params = new Record();
		int start = 0;
		if (args.length  % 2 == 1 && args[0] instanceof FlowEvent) {
			start = 1;
			params.putAll((FlowEvent)args[0]);
		}
		for (int i = start; i < args.length; i += 2) {
			if (i < args.length - 1) {
				params.put(args[i].toString(), args[i + 1]);
			}
		}
		return params;
	}
	*/
	
	protected String choice(int index, String... strings) {
		int i = randomMap.next(index, strings.length);
		int iter = 0;
		while (strings[i].length() == 0 && iter < 1000) {
			i = randomMap.next(index, strings.length);
			iter++;
		}
		if (iter >= 1000) {
			System.err.println("Could not find a non-empty choice element");
		}
		return strings[i];
	}
	
	protected String item(Boolean cond, Double prob, String string) {
		if (cond == null || cond) {
			if (prob == null || randomMap.getFloat() < prob) {
				return string;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	
	protected String concat(Object... objects) {
		if (objects.length == 1)
			return objects[0].toString();
		StringBuilder result = new StringBuilder();
		for (Object object : objects) {
			String string = object.toString();
			if (string.length() > 0) {
				if (result.length() > 0)
					result.append(" ");
				result.append(string);
			}
		}
		return result.toString();
	}

	/*
	public static Flow read(String file, String srcFolder, String binFolder) throws FlowCompilerException {
		try {
			File binDir = new File(binFolder);
			if (!binDir.exists())
				binDir.mkdirs();
			URLClassLoader classLoader = new URLClassLoader(new URL[] {binDir.toURI().toURL()}); 
			File flowFile = new File(file);
			flowFile.lastModified();
			FlowCompiler compiler = new FlowCompiler(flowFile);
			File modFile = new File(binFolder + "/" + compiler.getFlowName().replaceAll("\\.", "/") + ".mod");
			try {
				if (modFile.exists()) {
					long lastMod = Long.parseLong(Utils.readTextFile(modFile));
					if (lastMod == flowFile.lastModified()) {
						Class<Flow> flowClass = (Class<Flow>) classLoader.loadClass(compiler.getFlowName());
						return flowClass.newInstance();
					}
				}
			} catch (ClassNotFoundException e1) {
			} catch (SecurityException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (IOException e) {
			} 
			System.out.println("Compiling " + compiler.getFlowName());
			File srcFile = compiler.compile(new File(srcFolder));
			JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
			if (javaCompiler == null) {
				throw new FlowCompilerException("Could not find Java Compiler");
			}
			if (javaCompiler.run(null, null, null, srcFile.getPath(), "-d", binDir.getAbsolutePath()) == 0) {
				Utils.writeTextFile(modFile, "" + flowFile.lastModified());
				Class<Flow> flowClass = (Class<Flow>) classLoader.loadClass(compiler.getFlowName());
			    return flowClass.newInstance();
			} else {
				throw new FlowCompilerException("Could not compile to Jave byte code");
			}
		} catch (InstantiationException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new FlowCompilerException(e.getMessage());
	    } catch (IllegalAccessException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (IOException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}

	public static Flow read(String flowFile) throws FlowCompilerException {
		String path = System.getProperty("java.io.tmpdir") + File.separator + "iristk" + File.separator + "flow";
		return read(flowFile, path, path);
	} 
	 */
	
	public static Flow compile(File flowFile) throws FlowCompilerException {
		return compile(new FlowCompiler(flowFile));
	}
	
	public static Flow compile(iristk.xml.flow.Flow flowXml) throws FlowCompilerException {
		return compile(new FlowCompiler(flowXml));
	}
	
	private static Flow compile(FlowCompiler compiler) throws FlowCompilerException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			compiler.useUniqueNames(true);
			compiler.compileToStream(out);
			return (Flow) InMemoryCompiler.newInstance(compiler.getFlowName(), new String(out.toByteArray()));
		} catch (ClassNotFoundException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (InstantiationException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}

}

