/*
* Copyright 2009-2010 Gabriel Skantze.
* All Rights Reserved.  Use is subject to license terms.
*
* See the file "license.terms" for information on usage and
* redistribution of this file, and for a DISCLAIMER OF ALL
* WARRANTIES.
*
*/
package iristk.util;

import java.io.*;

public class ProcessThread extends Thread {

	private String cmd;
	private Process process;
	private Integer exitValue = null;
	private ProcessThreadListener listener;

	public ProcessThread(String cmd) {
		this.cmd = cmd;
	}
	
	public void setProcessThreadListener(ProcessThreadListener listener) {
		this.listener = listener;
	}

	public Integer getExitValue() {
		return exitValue;
	}
	
	public boolean isDone() {
		return exitValue != null;
	}
	
	@Override
	public void run() {
		try {
			process = Runtime.getRuntime().exec(cmd);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
			        process.destroy();
			    }
			});
			InputStream inputStream = process.getInputStream();
			BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));
			
			while (exitValue == null) {
				try {
				    while (inputStream.available() > 0) {
				    	String line = inputStreamReader.readLine();
				    	if (listener != null) {
				    		listener.newInputLine(line);
				    	}
				    }
				    // Ask the process for its exitValue.  If the process
				    // is not finished, an IllegalThreadStateException
				    // is thrown.  If it is finished, we fall through and
				    // the variable finished is set to true.
				    exitValue = process.exitValue();
		        } catch (IllegalThreadStateException e) {
		        	Thread.currentThread();
					// Sleep a little to save on CPU cycles
		        	Thread.sleep(100);
		        }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		process.destroy();
		interrupt();
	}
	
	public static interface ProcessThreadListener {
		
		void newInputLine(String line);
		
	}

	public static boolean kill(String image) {
		try {
			Process kill = Runtime.getRuntime().exec("cmd.exe /C TASKKILL /F /IM " + image);
			Integer exitValue = null;
			while (exitValue == null) {
				try {
				  exitValue = kill.exitValue();
		        } catch (IllegalThreadStateException e) {
		        	try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
		        }
			}
			if (exitValue == 0)
				return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
