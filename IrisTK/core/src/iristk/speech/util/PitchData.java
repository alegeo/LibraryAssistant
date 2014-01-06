package iristk.speech.util;

import java.util.Locale;

public class PitchData {

	public double pitchHz;
	public double pitchZ;
	public double pitchCent;
	public double conf;
	public double time;
	double[] yinBuffer;
	public double signalPower;

	PitchData(double pitchHz, double signalPower, double conf, double time) {
		this.pitchHz = pitchHz;
		if (pitchHz > -1) 
			this.pitchCent = pitchHzToCent(pitchHz);
		else
			this.pitchCent = -1;
		this.conf = conf;
		this.time = time;
		this.signalPower = signalPower;
		this.pitchZ = -1;
	}
	
	PitchData(double pitchHz, double signalPower, double conf, double time, double[] yinBuffer) {
		this(pitchHz, signalPower, conf, time);
		this.yinBuffer = yinBuffer;
	}
	
	PitchData(double pitchHz, double signalPower, double conf, double time, double pitchZ) {
		this(pitchHz, signalPower, conf, time);
		this.pitchZ = pitchZ;
	}

	private static final double CENT_CONST = 1731.2340490667560888319096172f;
	
    public static double pitchCentToHz(double pitchCent) {
    	return (Math.exp(pitchCent / CENT_CONST) * 110);
	}
    
    public static double pitchHzToCent(double pitchHz) {
    	return (CENT_CONST * Math.log(pitchHz / 110));
	}
    
    @Override
    public String toString() {
    	return String.format(Locale.US,  "hz: %.2f, pitchZ: %.2f, power: %.2f, conf: %.2f, time: %.2f", pitchHz, pitchZ, signalPower, conf, time);
    }
	
	
}
