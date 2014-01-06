package iristk.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

public class WlanController {

	static interface WlanAPI extends StdCallLibrary {

		WlanAPI INSTANCE = (WlanAPI) Native.loadLibrary("wlanapi", WlanAPI.class);
		
		int WLAN_API_VERSION_2_0 = 2;
		
		int WlanOpenHandle(int dwClientVersion, Pointer pReserved, IntByReference pdwNegotiatedVersion, PointerByReference phClientHandle); 
		
		int WlanEnumInterfaces(Pointer hClientHandle, Pointer pReserved, PointerByReference ppInterfaceList);
		
	}
	
	public WlanController() {
		
		IntByReference pdwNegotiatedVersion = new IntByReference();
		PointerByReference phClientHandle = new PointerByReference();
		
		System.out.println(WlanAPI.INSTANCE.WlanOpenHandle(WlanAPI.WLAN_API_VERSION_2_0, null, pdwNegotiatedVersion, phClientHandle));
		
	}
	
	public static void main(String[] args) {
		new WlanController();
	}
	
	
}
