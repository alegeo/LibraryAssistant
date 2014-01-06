package iristk.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import com.portaudio.BlockingStream;
import com.portaudio.DeviceInfo;
import com.portaudio.PortAudio;
import com.portaudio.StreamParameters;

public class PortAudioTarget implements AudioTarget {

	private AudioFormat format;
	private BlockingStream stream;

	public PortAudioTarget(AudioFormat format) {
		this.format = format;
		try {
			PortAudioUtil.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(format);
		int deviceId = PortAudio.getDefaultOutputDevice();
		DeviceInfo deviceInfo = PortAudio.getDeviceInfo( deviceId );
		StreamParameters streamParams = new StreamParameters();
		streamParams.channelCount = format.getChannels();
		streamParams.device = deviceId;
		streamParams.sampleFormat = PortAudio.FORMAT_INT_16;
		//streamParams.suggestedLatency = deviceInfo.defaultLowInputLatency;
		int flags = 0;
		int framesPerBuffer = 256;
		stream = PortAudio.openStream(null, streamParams, (int)format.getSampleRate(), framesPerBuffer, flags);
		double latency = stream.getInfo().outputLatency;
		System.out.println("PortAudio using " + deviceInfo.name + ", Latency: " + latency);
	}

	public PortAudioTarget(int sampleRate, int channelCount) {
		this(new AudioFormat(Encoding.PCM_SIGNED, sampleRate, 16, channelCount, 2, sampleRate, false));
	}

	@Override
	public void start() {
		stream.start();
	}

	@Override
	public void stop() {
		stream.stop();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public int write(byte[] buffer, int pos, int len) {
		int sampleSize = getAudioFormat().getSampleSizeInBits() / 8; 
		short[] sbuffer = new short[len / 2];
		for (int i = 0; i < sbuffer.length; i++) {
			int bufPos = pos + i * sampleSize;
	        sbuffer[i] = AudioUtil.bytesToShort(format, buffer[bufPos], buffer[bufPos + 1]);
		}
		stream.write(sbuffer, sbuffer.length / format.getChannels());
		return len;
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
		stream.close();
	}

}
