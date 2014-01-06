package iristk.system;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import iristk.util.StringUtils;
import iristk.xml._package.Package.Classpath.Lib;
import iristk.xml._package.Package.Classpath.Src;
import iristk.xml._package.Package.Run.Program;


public class IrisCmd {

	private static String[] cropArgs(String[] args, int n) {
		if (n > args.length) n = args.length;
		String[] result = new String[args.length - n];
		System.arraycopy(args, n, result, 0, result.length);
		return result;
	}

	public static void main(String[] args) {
		if (args.length == 1 && args[0].equals("install")) {
			install();
			return;
		}
		if (System.getenv("IrisTK") == null) {
			System.out.println("IrisTK not installed");
			install();
			return;
		}
		if (args.length == 0) {
			printUsage();
		} else if (args[0].equals("run")) {
			if (args.length < 2)
				listPrograms();
			else
				runProgram(args[1], cropArgs(args, 2), false);
		} else if (args[0].equals("run32")) {
			runProgram(args[1], cropArgs(args, 2), true);
		} else if (args[0].equals("programs")) {
			listPrograms();
		} else if (args[0].equals("packages")) {
			listPackages();
		} else if (args[0].equals("eclipse")) {
			setupEclipse(cropArgs(args, 1));
		} else if (args[0].equals("cflow")) {
			compileFlow(cropArgs(args, 1));
		} else if (args[0].equals("cleantemp")) {
			cleanTempFolder();
		} else if (args[0].equals("create")) {
			if (args.length >= 3)
				createFromTemplate(args[1], args[2]);
			else
				listTemplates();
		} else {
			System.out.println("Could not recognize command " + args[0]);
		}
	}

	private static void install() {
		try {
			if (!new File(new File(System.getProperty("user.dir")), "iristk.exe").exists()) {
				throw new Exception("Your must be in the IrisTK root folder when installing");
			}
			System.out.println("Installing IrisTK...");
			setEnv("IrisTK", System.getProperty("user.dir"));
			String path = getEnv("PATH");
			if (path == null || !path.contains("%IrisTK%")) {
				setEnv("PATH", path + ";%IrisTK%");
			}
			IrisUtils.setIristkPath(System.getProperty("user.dir"));
			setupEclipse(null);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static String getEnv(String name) throws Exception {
		String value = getCmd("reg query HKCU\\Environment /v " + name).trim();
		for (String line : value.split("\n")) {
			Matcher m = Pattern.compile(name + " +[^ ]+ +(.*)", Pattern.CASE_INSENSITIVE).matcher(line.trim());
			if (m.matches())
				return m.group(1);
		}
		return null;
	}

	private static void setEnv(String name, String value) throws Exception {
		//value = value.replace("%", "~");
		//String result = getCmd("reg add HKCU\\Environment /f /v " + name + " /t " + type + " /d \"" + value + "\"").trim();
		String result = getCmd("setx " + name + " \"" + value + "\"").trim();
		//if (!result.contains("successfully")) {
		if (!result.contains("SUCCESS")) {
			throw new Exception(result);
		}
	}

	private static void setupEclipse(String[] buildPackages) {
		try {
			PrintWriter pw = new PrintWriter(new File(IrisUtils.getIristkPath(), ".classpath")); 
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<classpath>");
			pw.println("<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>");
			String root = IrisUtils.getIristkPath().getAbsolutePath() + "\\";
			for (iristk.xml._package.Package pack : IrisUtils.getPackages()) {
				if (pack.getClasspath() != null) {
					String path = IrisUtils.getPackagePath(pack.getName()).getAbsolutePath().replace(root, "").replace("\\", "/") + "/";
					for (Object entry : pack.getClasspath().getLibOrSrcOrDll()) {
						if (entry instanceof Lib) {
							pw.println("<classpathentry kind=\"lib\" path=\"" + path + ((Lib)entry).getPath() + "\"/>");
						} else if (entry instanceof Src) {
							boolean build = true;
							if (buildPackages != null && buildPackages.length > 0) {
								build = false;
								for (String buildPackage : buildPackages) {
									if (buildPackage.equalsIgnoreCase(pack.getName())) {
										build = true;
										break;
									}
								}
							} 
							if (build) {
								pw.println("<classpathentry kind=\"src\" output=\"" + path + ((Src)entry).getOutput() + "\" path=\"" + path + ((Src)entry).getPath() + "\"/>");
							} else {
								pw.println("<classpathentry kind=\"lib\" path=\"" + path + ((Src)entry).getOutput() + "\"/>");
							}
						}
					}
				}
			}
			pw.println("<classpathentry kind=\"output\" path=\"core/bin\"/>");
			pw.println("</classpath>");
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void compileFlow(String[] args) {
		try {
			runJava(false, new File(System.getProperty("user.dir")), "iristk.flow.FlowCompiler", args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private static void compileSchema(String[] args) {
		try {
			runJava(false, new File(System.getProperty("user.dir")), "iristk.flow.TemplateSchemaCompiler", args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println();
		System.out.println("iristk run [program] [args]    Runs a program");
		System.out.println("iristk run32 [program] [args]  Runs a program using 32 bit Java");
		System.out.println("iristk programs                Lists available programs");
		System.out.println("iristk packages                Lists available packages");
		System.out.println("iristk cleantemp               Cleans the iristk temp folder");
		System.out.println("iristk eclipse                 Sets up the eclipse classpath");
		System.out.println("iristk cflow                   Compile flow");
		System.out.println("iristk create                  Create an iristk package");
	}

	private static void listPrograms() {
		System.out.println("Available programs:\n");
		for (String pname : IrisUtils.getPackageNames()) {
			if (IrisUtils.getPackage(pname).getRun() != null) {
				for (Program program : IrisUtils.getPackage(pname).getRun().getProgram()) {
					System.out.println(program.getName() + " (" + pname + ")");
				}
			}
		}
	}

	private static void listPackages() {
		for (String pname : IrisUtils.getPackageNames()) {
			System.out.println(pname);
		}
	}

	private static void runProgram(String progName, String[] args, boolean force32) {
		Program program = findProgram(progName);
		if (program != null) {
			File progDir = IrisUtils.getPackagePath(program.getName());
			try {
				runJava(force32, progDir, program.getClazz(), args);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		} else {
			System.out.println("No program with the name '" + progName + "' found");
		}
	}

	private static Program findProgram(String progName) {
		for (String pname : IrisUtils.getPackageNames()) {
			if (IrisUtils.getPackage(pname).getRun() != null) {
				for (Program prog : IrisUtils.getPackage(pname).getRun().getProgram()) {
					if (prog.getName().equals(progName)) {
						return prog;
					}
				}
			}
		}
		return null;
	}
	
	/*
	private static void runJava(String javaVersion, File appDir, String mainClass, String[] args) throws Exception {
		for (Method method : Class.forName(mainClass).getMethods()) {
			if (method.getName().equals("main") && Modifier.isStatic(method.getModifiers())) {
				method.invoke(null, new Object[] {args});
				return;
			}
		}
	}
	 */

	private static String getJavaCmd(boolean force32) throws Exception {
		if (force32 && System.getProperty("sun.arch.data.model").equals("64")) {
			File javaDir = new File(System.getenv("ProgramFiles(x86)"),  "Java");
			if (javaDir.exists()) {
				String[] versions = new String[]{"jdk1.7", "jre7"};
				for (String v : versions) {
					for (File f : javaDir.listFiles()) {
						if (f.getName().startsWith(v)) {
							File javaexe = new File(new File(f.getPath(), "bin"), "java.exe");
							if (javaexe.exists()) {
								return javaexe.getAbsolutePath();
							}
						}
					}
				}
			}
			throw new Exception("Could not find Java 32-bit runtime");
		} else {
			File javaexe = new File(new File(new File(System.getProperty("java.home")), "bin"), "java.exe");
			if (javaexe.exists()) {
				return javaexe.getAbsolutePath();
			}
			throw new Exception("Could not find Java runtime");
		}
	}

	public static void cleanTempFolder() {
		try {
			FileUtils.cleanDirectory(IrisUtils.getTempDir());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createFromTemplate(String templateName, String name) {
		try {
			ZipFile zipFile = new ZipFile(new File(IrisUtils.getPackageLibPath("core"), "templates.zip"));
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			boolean found = false;
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().startsWith(templateName + "/")) {
					found = true;
					String entryName = entry.getName().replaceFirst(templateName + "/", name + "/");
					entryName = entryName.replace("$name$", name);
					entryName = entryName.replace("$Name$", name.substring(0, 1).toUpperCase() + name.substring(1));
					entryName = entryName.replace("/_", "/");
					if(entry.isDirectory()) {
						System.err.println("Creating directory: " + entryName);
						(new File(entryName)).mkdirs();
					} else {
						System.err.println("Creating file: " + entryName);
						InputStream in = zipFile.getInputStream(entry);
						Scanner s = new Scanner(in).useDelimiter("\\A");
						String content = s.hasNext() ? s.next() : "";
						content = content.replace("$name$", name);
						content = content.replace("$Name$", name.substring(0, 1).toUpperCase() + name.substring(1));
						OutputStream out = new BufferedOutputStream(new FileOutputStream(entryName));
						out.write(content.getBytes());
						in.close();
						out.close();
					}
				}
			}
			zipFile.close();
			if (!found) {
				System.out.println("Couldn't find template '" + templateName + "'");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}
	}
	
	public static void listTemplates() {
		try {
			System.out.println("Usage: iristk create [template] [name]");
			System.out.println("Available templates:");
			ZipFile zipFile = new ZipFile(new File(IrisUtils.getPackageLibPath("core"), "templates.zip"));
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			HashSet<String> templates = new HashSet<>();
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String templ = entry.getName().replaceFirst("/.*", "");
				if (!templates.contains(templ)) {
					System.out.println(templ);
					templates.add(templ);
				}
			}
			zipFile.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}
	}

	private static void runJava(boolean force32, File appDir, String mainClass, String[] args) throws Exception {
		String javaCmd = getJavaCmd(force32);
		String cmd = "\"" + javaCmd + "\" -cp \"" + IrisUtils.getClasspath() + "\" " + mainClass + " " + StringUtils.join(args, " ");
		runCmd(cmd);
	}

	private static void runCmd(String cmd) throws Exception {
		final Process proc = Runtime.getRuntime().exec(cmd);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				proc.destroy();
			}
		});
		StreamPrinter errorGobbler = new StreamPrinter(proc.getErrorStream(), System.err);
		StreamPrinter outputGobbler = new StreamPrinter(proc.getInputStream(), System.out);
		errorGobbler.start();
		outputGobbler.start();
		proc.waitFor();
	}

	private static String getCmd(String cmd) throws Exception {
		final Process proc = Runtime.getRuntime().exec(cmd);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				proc.destroy();
			}
		});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamPrinter errorGobbler = new StreamPrinter(proc.getErrorStream(), System.err);
		StreamPrinter outputGobbler = new StreamPrinter(proc.getInputStream(), out);
		errorGobbler.start();
		outputGobbler.start();
		proc.waitFor();
		return new String(out.toByteArray());
	}

	private static class StreamPrinter extends Thread {
		InputStream is;
		PrintStream out;

		StreamPrinter(InputStream is, OutputStream out) {
			this.is = is;
			this.out = new PrintStream(out);
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
					out.println(line);    
			} catch (IOException ioe) {
				ioe.printStackTrace();  
			}
		}
	}

}
