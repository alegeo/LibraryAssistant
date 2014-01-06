package iristk.audio;

import iristk.system.IrisUtils;
import com.portaudio.PortAudio;

public class PortAudioUtil {

	private static Boolean initialized = false;
	
	public static synchronized void initialize() throws Exception {
		if (!initialized) {
			IrisUtils.addCoreLibPath();
			IrisUtils.loadPackageLib64(IrisUtils.CORE_PACKAGE, "portaudio_x64.dll");
			IrisUtils.loadPackageLib64(IrisUtils.CORE_PACKAGE, "jportaudio_x64.dll");
			IrisUtils.loadPackageLib32(IrisUtils.CORE_PACKAGE, "portaudio_x86.dll");
			IrisUtils.loadPackageLib32(IrisUtils.CORE_PACKAGE, "jportaudio_x86.dll");
			PortAudio.initialize();
			initialized = true;
		}
	}

}
