package iristk.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import com.portaudio.BlockingStream;
import com.portaudio.DeviceInfo;
import com.portaudio.PortAudio;
import com.portaudio.StreamParameters;

public class PortAudioSource implements AudioSource {

	private BlockingStream stream;
	private int channelCount;
	private AudioFormat audioFormat;
	
	public PortAudioSource() {
		this(16000, 1);
	}
	
	public PortAudioSource(int sampleRate, int channelCount) {
		this(new AudioFormat(Encoding.PCM_SIGNED, sampleRate, 16, channelCount, 2, sampleRate, false));
	}
	
	public PortAudioSource(AudioFormat format) {
		this.audioFormat = format;
		this.channelCount = format.getChannels();
		try {
			PortAudioUtil.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int deviceId = PortAudio.getDefaultInputDevice();
		DeviceInfo deviceInfo = PortAudio.getDeviceInfo( deviceId );
		StreamParameters streamParams = new StreamParameters();
		streamParams.channelCount = channelCount;
		streamParams.device = deviceId;
		streamParams.sampleFormat = PortAudio.FORMAT_INT_16;
		//streamParams.suggestedLatency = deviceInfo.defaultLowInputLatency;
		int flags = 0;
		int framesPerBuffer = 256;
		stream = PortAudio.openStream(streamParams, null, (int)format.getSampleRate(), framesPerBuffer, flags);
		double latency = stream.getInfo().inputLatency;
		System.out.println("PortAudio using " + deviceInfo.name + ", Latency: " + latency);
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
	public int read(byte[] buffer, int pos, int len) {
		short[] frames = new short[len/2];
		stream.read(frames, frames.length/channelCount);
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int slen = len / 2;
		for (int i = 0; i < slen; i++) {
			short val = frames[i];
			bb.putShort(pos + i * 2, val);
		}
		return len;
	}
	
	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public static void main(String[] args) {
		PortAudioSource dev = new PortAudioSource();
		dev.start();
		byte[] buffer = new byte[512];
		dev.read(buffer, 0, buffer.length);
		dev.stop();
	}

	@Override
	public void close() {
		stream.close();
	}

	@Override
	public int getPosition() {
		return 0;
	}
}
