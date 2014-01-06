package iristk.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

	public static String readTextFile(File file) throws IOException {
		byte[] buffer = new byte[(int) file.length()];
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
		is.read(buffer);
		is.close();
		return new String(buffer);
	}
	
	public static void writeTextFile(File file, String string) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(string);
		bw.close();
	}
	
	public static float mean(List<Float> data) {
		float mean = 0;
		for (Float pd : data) {
			mean += pd / data.size();
		}
		return mean;
	}

	public static float median(List<Float> data) {
		ArrayList<Float> sorted = new ArrayList<Float>(data);
		Collections.sort(sorted);
		if (sorted.size() % 2 == 1) {
			return sorted.get(sorted.size() / 2);
		} else {
			int mid = sorted.size() / 2 - 1;
			return (sorted.get(mid) + sorted.get(mid+1)) / 2;
		}
	}
	
	public static float stdev(List<Float> data, double mean) {
		float variance = 0;
		for (Float pd : data) {
			variance += Math.pow((pd - mean), 2) / data.size();
		}
		return (float) Math.sqrt(variance);
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

	/*
	public static File resourceToFile(URL config) throws IOException {
		try {
			// See if we can read directly from the resource
			if (!config.getProtocol().equals("jar")) {
				File file = new File(config.toURI());
				if (file.exists()) {
					return file;
				}
			}
		} catch (URISyntaxException e) {
		}
		// That didn't work. Try to copy the resource to a temporary directory.
		File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "iristk" + File.separator + "nuance9" + File.separator + "config.xml");
		new File(tempFile.getParent()).mkdirs();
		InputStream is = config.openStream();
		FileOutputStream fos = new FileOutputStream(tempFile);
		byte[] buf = new byte[256];
		int read = 0;
		while ((read = is.read(buf)) > 0) {
			fos.write(buf, 0, read);
		}
		fos.close();
		is.close();
		return tempFile;
	}
	*/
	
	public static String listToString(List list, String glue) {
		String result = "";
		boolean first = true;
		for (Object item : list) {
			if (!first)
				result += glue;
			result += item.toString();
			first = false;
		}
		return result;
	}

	public static String listToString(List list) {
		return listToString(list, " ");
	}

}
