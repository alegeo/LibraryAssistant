package iristk.audio;

import iristk.system.IrisUtils;
import iristk.util.BlockingByteQueue;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

public class ASIOAudioSource implements AudioSource {

	private int bufferSize = 1600;
	private int nChannels;

	private String driverName = null;
	private AsioDriver driver;

	private Set<AsioChannel> asio_channels;
	private int sourcePos = 0;
	private AudioFormat audioFormat;

	byte[] channelBuffer;
	byte[] byteBuffer;
	float[] floatBuffer;

	BlockingByteQueue byteQueue = new BlockingByteQueue();
	private boolean running = false;

	static {
		IrisUtils.addCoreLibPath();
	}

	public ASIOAudioSource(int nChannels, int sampleRate) {
		this("", nChannels, sampleRate);
	}
	
	public ASIOAudioSource(String name, int nChannels, int sampleRate) {
		this.nChannels = nChannels;
		this.audioFormat = new AudioFormat(sampleRate, 16, nChannels, true, false);

		List<String> driverNameList = AsioDriver.getDriverNames();

		// finding the driver name that contains the name provided by the
		// constructor
		String dName = null;
		for (int i = 0; i < driverNameList.size(); i++) {
			if (driverNameList.get(i).contains(name)) {
				dName = driverNameList.get(i);
				break;
			}
		}
		if (dName == null)
			System.out.println("failed to find a driver name that contains:"
					+ name);
		else {
			driverName = dName;
		}
		driver = AsioDriver.getDriver(driverName);
		driver.addAsioDriverListener(new AsioListener());
		// adding the channels
		asio_channels = new HashSet<AsioChannel>();
		for (int i = 0; i < nChannels; i++) {
			asio_channels.add(driver.getChannelInput(i));
		}

		driver.createBuffers(asio_channels);
		
		bufferSize = driver.getBufferPreferredSize() * 2;

		channelBuffer = new byte[bufferSize];
		byteBuffer = new byte[nChannels * bufferSize];
		floatBuffer = new float[bufferSize / 2];

	}

	public static List<String> getDriverNames() {
		return AsioDriver.getDriverNames();
	}

	private class AsioListener implements AsioDriverListener {

		@Override
		public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> a_channels) {
			// System.out.println("reading input");
			Iterator<AsioChannel> it = a_channels.iterator();

			for (int c = 0; c < nChannels; c++) {
				AsioChannel ch = it.next();

				ch.read(floatBuffer);

				AudioUtil.floatsToBytes(audioFormat, floatBuffer, 0, floatBuffer.length, channelBuffer, 0);

				for (int i = 0; i < floatBuffer.length; i++) {
					byteBuffer[(i * nChannels + c) * 2] = channelBuffer[i * 2];
					byteBuffer[(i * nChannels + c) * 2 + 1] = channelBuffer[i * 2 + 1];
				}

			}

			byteQueue.write(byteBuffer);

		}

		@Override
		public void sampleRateDidChange(double sampleRate) {

		}

		@Override
		public void resetRequest() {

		}

		@Override
		public void resyncRequest() {

		}

		@Override
		public void bufferSizeChanged(int bufferSize) {
			throw new RuntimeException("buffer size changed: " + bufferSize);
		}

		@Override
		public void latenciesChanged(int inputLatency, int outputLatency) {
		}

	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public String getDriverName() {
		return driverName;
	}

	@Override
	public void start() {
		running = true;
		byteQueue.reset();
		driver.start();
	}

	@Override
	public void stop() {
		driver.stop();
		running  = false;
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		try {
			sourcePos += len / audioFormat.getFrameSize();
			return byteQueue.read(buffer, pos, len);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	@Override
	public void close() {
		if (running)
			stop();
		driver.disposeBuffers();
		driver.shutdownAndUnloadDriver();
	}
	
	public static void main(String[] args) {
		AudioSourceReader reader = new AudioSourceReader(new ASIOAudioSource("Microcone", 8, 48000));
		reader.addAudioTarget(new FileAudioTarget(reader.getAudioSource().getAudioFormat(), new File("c:/microcone")));
		reader.run(32000);
	}

	@Override
	public int getPosition() {
		return sourcePos;
	}

}
