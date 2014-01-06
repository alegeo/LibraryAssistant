package iristk.audio;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class WaveformPanel extends JPanel {

	private Sound sound;
	private Integer lastHeight = 0;
	private Integer lastWidth = 0;
	private Image waveformImg;
	private SoundPlayer player;

	private Integer playPosition = null;
	private Thread playThread;
	private long startPlayTime;

	public WaveformPanel(Sound sound) {
		setSound(sound);
	}
	
	public void setSound(Sound sound) {
		this.sound = sound;
	}
	
	public Sound getSound() {
		return sound;
	}
	
	public void play() {
		play(null);
	}
	
	public void play(Integer start) {
		play(start, null, null);
	}
	
	public interface PlayCallback {
		public void playingDone();
	}
	
	public void play(Integer start, final Integer end, final PlayCallback callback) {
		if (player == null) {
			player = new SoundPlayer(sound.getAudioFormat());
		}
		int startTime = start == null ? (playPosition == null ? 0 : playPosition) : start;
		startPlayTime = System.currentTimeMillis() - startTime;
		player.playAsync(sound, startTime);
		playThread = 
		new Thread() {
			@Override
			public void run() {
				while(player.isPlaying()) {
					//playPosition = player.getPlayPosition();
					playPosition = (int) (System.currentTimeMillis() - startPlayTime);
					repaint();
					if (end != null && playPosition > end) {
						new Thread() {
							@Override
							public void run() {
								pause();
								if (callback != null) {
									callback.playingDone();
								}
							}
						}.start();
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				playPosition = null;
				repaint();
			};
		};
		playThread.start();
	}
	

	public void pause() {
		if (player != null) {
			int pausePosition  = (int) (System.currentTimeMillis() - startPlayTime);
			player.stop();
			try {
				playThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			playPosition = pausePosition;
		}
	}

	public boolean isPlaying() {
		return player != null && player.isPlaying();
	}
	
	public Integer getPlayPosition() {
		return playPosition;
	}
	
	public void stop() {
		if (player != null) {
			player.stop();
			try {
				playThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	} 
	
	private void paintWaveform(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		int height = getHeight() / sound.getAudioFormat().getChannels();
		for (int channel = 0; channel < sound.getAudioFormat().getChannels(); channel++) {
			int midY = height / 2 + channel * height;
			g.setColor(Color.black);
			g.drawLine(0, midY, getWidth(), midY);
			g.setColor(Color.blue);
			float maxValue = (float) (Math.pow(2, sound.getAudioFormat().getSampleSizeInBits()) / 2);
			int ly = midY;
			int lx = 0;
			int n = getWidth() * 5;
			for (int i = 0; i < n; i++) {
				int x = (getWidth() * i) / n;
				int pos = (int)((float) sound.getSampleLength() * (float) i / n);
				int y =  midY - (int) (((height / 2) * (float)sound.getSample(pos, channel) / maxValue));
				g.drawLine(lx, ly, x, y);
				lx = x;
				ly = y;
			}
		}
	}
	
	protected void paintPlayMarker(Graphics g) {
		if (playPosition != null) {
			int x = (int) (getWidth() * ((float)playPosition/(float)1000) / sound.getSecondsLength());
			g.setColor(Color.red);
			g.drawLine(x, 0, x, getHeight());
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (lastWidth != getWidth() || lastHeight != getHeight()) {
			waveformImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			paintWaveform(waveformImg.getGraphics());
			lastWidth = getWidth();
			lastHeight = getHeight();
		}
		g.drawImage(waveformImg, 0, 0, null);
		paintPlayMarker(g);
	}
	
	public static void main(String[] args) {
		try {
			JFrame window = new JFrame();
			window.add(new WaveformPanel(new Sound(new File("C:\\Dropbox\\KTH\\IURO\\feedback\\william_fb\\free_dialogue\\which_ehm_hesitation_01.wav"))));
			window.setPreferredSize(new Dimension(800,400));
			window.pack();
			window.setVisible(true);
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
