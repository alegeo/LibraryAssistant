package iristk.system;

import iristk.util.XmlMarshaller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.bind.JAXBException;

import iristk.xml._package.Package;
import iristk.xml._package.Package.Classpath.Dll;
import iristk.xml._package.Package.Classpath.Lib;
import iristk.xml._package.Package.Classpath.Src;

public class IrisUtils {
	
	public static final String CORE_PACKAGE = "core";
	
	private static HashMap<String,Package> packages;
	private static HashMap<String,File> packagePaths;
	private static HashSet<String> javaLibPaths = new HashSet<>();
	private static String iristkPath = null;
	
	private static void readPackages() {
		if (packages == null) {
			packages = new HashMap<>();
			packagePaths = new HashMap<>();
			final XmlMarshaller<Package> packageReader = new XmlMarshaller<>("iristk.xml._package");
			
			try {
				Files.walkFileTree(FileSystems.getDefault().getPath(getIristkPath().getAbsolutePath()), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						File packFile = new File(dir.toString(), "package.xml");
						if (packFile.exists()) {
							try {
								Package pack = packageReader.unmarshal(packFile);
								packages.put(pack.getName(), pack);
								packagePaths.put(pack.getName(), packFile.getParentFile());
							} catch (JAXBException e) {
								e.printStackTrace();
							}
							return FileVisitResult.SKIP_SUBTREE;
						} else {
							return FileVisitResult.CONTINUE;
						}
					}
				});
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static Collection<Package> getPackages() {
		readPackages();
		return packages.values();
	}
	
	public static Collection<String> getPackageNames() {
		readPackages();
		return packages.keySet();
	}

	public static Package getPackage(String pname) {
		readPackages();
		return packages.get(pname);
	}
	
	public static File getPackagePath(String packageName) {
		readPackages();
		return packagePaths.get(packageName);
	}
	
	public static File getPackageLibPath(String packageName) {
		readPackages();
		return new File(packagePaths.get(packageName), "lib");
	}
	
	public static void setIristkPath(String path) {
		iristkPath  = path;
	}
	
	public static File getIristkPath() {
		if (iristkPath == null)
			iristkPath = System.getenv("IrisTK");
		if (iristkPath == null)
			throw new RuntimeException("Environment variable 'iristk' not set");
		else {
			File iristkPathF = new File(iristkPath);
			if (!iristkPathF.exists()) 
				throw new RuntimeException("Directory " + iristkPath + " does not exist");
			else
				return iristkPathF;
		}
	}
	
	public static File getIristkPath(String path) {
		File pathF = new File(getIristkPath(), path);
		if (!pathF.exists()) 
			throw new RuntimeException("Directory " + pathF.getAbsolutePath() + " does not exist");
		else
			return pathF;
	}
	
	public static void loadPackageLib64(String packageName, String dll) {
		if (System.getProperty("sun.arch.data.model").equals("64")) {
			System.load(new File(getPackageLibPath(packageName), dll).getAbsolutePath());
		}
	}

	public static void loadPackageLib32(String packageName, String dll) {
		if (!System.getProperty("sun.arch.data.model").equals("64")) {
			System.load(new File(getPackageLibPath(packageName), dll).getAbsolutePath());
		}
	}
	
	public static void loadPackageDlls(String packageName) {
		Package pack = getPackage(packageName);
		if (pack.getClasspath() != null) {
			for (Object entry : pack.getClasspath().getLibOrSrcOrDll()) {
				if (entry instanceof Dll) {
					Dll dll = (Dll)entry;
					if (dll.getArch() == null || dll.getArch().equals(System.getProperty("sun.arch.data.model"))) {
						System.load(new File(getPackagePath(packageName), dll.getPath()).getAbsolutePath());
					}
				}
			}
		}
	}
	
	public static String getClasspath() {
		String classpath = "";
		for (Package pack : getPackages()) {
			if (pack.getClasspath() != null) {
				for (Object entry : pack.getClasspath().getLibOrSrcOrDll()) {
					if (entry instanceof Lib) {
						classpath += new File(getPackagePath(pack.getName()), ((Lib)entry).getPath()).getAbsolutePath() + ";";
					} else if (entry instanceof Src) {
						classpath += new File(getPackagePath(pack.getName()), ((Src)entry).getOutput()).getAbsolutePath() + ";";
					}
				}
			}
		}
		return classpath;
	}
	
	public static void addCoreLibPath() {
		addJavaLibPath(IrisUtils.getPackageLibPath(CORE_PACKAGE));
	}
	
	public static void addJavaLibPath(File path) {
		if (!javaLibPaths.contains(path.getAbsolutePath())) {
			try {
				System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" + path.getAbsolutePath());
				Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
				javaLibPaths.add(path.getAbsolutePath());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public static File getTempDir() {
		return new File(System.getProperty("java.io.tmpdir") + File.separator + "iristk");
	}
	
	public static File getTempDir(String name) {
		return new File(System.getProperty("java.io.tmpdir") + File.separator + "iristk" + File.separator + name);
	}

}
