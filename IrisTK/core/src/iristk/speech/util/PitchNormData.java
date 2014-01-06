package iristk.speech.util;

//TODO: this should be a Record and stored as XML to file
public class PitchNormData {
	
	private double meanPitch;
	private double stdevPitch;
	
	public PitchNormData(double meanPitch, double stdevPitch) {
		setMeanPitch(meanPitch);
		setStdevPitch(stdevPitch);
	}
	
	public double getMeanPitch() {
		return meanPitch;
	}
	
	public void setMeanPitch(double meanPitch) {
		this.meanPitch = meanPitch;
	}
	
	public double getStdevPitch() {
		return stdevPitch;
	}
	
	public void setStdevPitch(double stdevPitch) {
		this.stdevPitch = stdevPitch;
	}
	
	public static PitchNormData gabriel = new PitchNormData(-475.1, 197);
	
}
