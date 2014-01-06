package iristk.speech.util;

import iristk.util.Record;
import iristk.util.Regression;
import iristk.util.Utils;

import java.util.ArrayList;


public class PitchAnalyzer implements PitchHandler {

	private ArrayList<PitchData> pitchList = new ArrayList<PitchData>();
	private boolean running;

	@Override
	public void handleDetectedPitch(PitchData pitchData) {
		if (running) {
			pitchList.add(pitchData);
		}
	}
	
	public void start() {
		pitchList.clear();
		running = true;
	}
	
	public void stop() {
		running = false;
	}
	
	public Record analyze() {
		float pitchMean = 0;
		float pitchSlope = 0;
		float pitchStdev = 0;
		float energyMean = 0;
		Boolean voiced = false;
		
		COMPUTE_PROSODY: {
			
			double endTime = pitchList.get(pitchList.size() - 1).time;
			ArrayList<Float> selPitch = new ArrayList<Float>();
			ArrayList<Float> selEnergy = new ArrayList<Float>();
			int i = pitchList.size() - 1;
			while (selPitch.size() < 20 && i > 0) { // && i > pitchList.size() - 100
				float pitch = (float) pitchList.get(i).pitchZ;
				if (pitch == -1) {
					if (selPitch.size() > 8)
						break;
					else
						selPitch.clear();
				} else {
					ADD_PITCH: {
						if (selPitch.size() > 0 && Math.abs(pitch - selPitch.get(selPitch.size() - 1)) > 0.5) {
							if (selPitch.size() < 5)
								selPitch.clear();
							else
								break ADD_PITCH;
						}
						selPitch.add(pitch);
					}
					selEnergy.add((float) pitchList.get(i).signalPower);
				}
				i--;
			}
			if (selPitch.size() > 8) {
				voiced = true;
				pitchStdev = Utils.stdev(selPitch, Utils.mean(selPitch));
				pitchSlope = - (float) Regression.fromList(selPitch, 0.1f).getSlope();
				pitchMean = Utils.mean(selPitch.subList(Math.max(0, selPitch.size() - 10), selPitch.size()));
				energyMean = Utils.mean(selEnergy);
			}
		}

		Record result = new Record();
		
		result.put("pitchMean", pitchMean);
		result.put("pitchStdev", pitchStdev);
		result.put("voiced", voiced);
		result.put("pitchSlope", pitchSlope);
		result.put("energyMean", energyMean);
		
		return result;
	}

	public void trim(int startFrames, int endFrames) {
		for (int i = 0; i < startFrames; i++)
			pitchList.remove(0);
		for (int i = 0; i < endFrames; i++)
			pitchList.remove(pitchList.size()-1);
	}

}
