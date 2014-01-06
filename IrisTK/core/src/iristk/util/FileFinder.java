package iristk.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileFinder extends SimpleFileVisitor<Path> {
	
	private String name;
	private ArrayList<String> result = new ArrayList<>();
	private boolean findAll;
	
	public static String findFirst(String root, String name) {
		FileFinder finder = new FileFinder();
		try {
			finder.name = name;
			finder.findAll = false;
			Files.walkFileTree(FileSystems.getDefault().getPath(root), finder);
		} catch (IOException e) {
		}
		if (finder.result.size() > 0)
			return finder.result.get(0);
		else
			return null;
	}
	
	public static List<String> findAll(String root, String name, int maxdepth) {
		FileFinder finder = new FileFinder();
		try {
			finder.name = name;
			finder.findAll = true;
			Files.walkFileTree(FileSystems.getDefault().getPath(root), finder);
		} catch (IOException e) {
		}
		return finder.result;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		String fn = file.toString();
		if (fn.endsWith("\\" + name)) {
			result.add(fn);
			if (!findAll)
				return FileVisitResult.TERMINATE;
		} 
		return FileVisitResult.CONTINUE;
	}
	
}