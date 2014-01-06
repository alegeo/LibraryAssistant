package iristk.speech.windows;

import iristk.system.IrisUtils;

import java.io.File;
import java.io.IOException;

import net.sf.jni4net.Bridge;

public class WindowsSpeech {

	private static final String PACKAGE = "WindowsSpeech";
	
	private static boolean initialized = false;
	
	public static synchronized void init() {
		if (!initialized ) {
			try {
				IrisUtils.addJavaLibPath(IrisUtils.getPackageLibPath(PACKAGE));
				Bridge.init();
		        Bridge.LoadAndRegisterAssemblyFrom(new File(IrisUtils.getPackageLibPath(PACKAGE), "WindowsSpeech.j4n.dll"));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			initialized = true;
		}
	}

	public static File getLibPath() {
		return IrisUtils.getPackageLibPath(PACKAGE);
	}
	
}
