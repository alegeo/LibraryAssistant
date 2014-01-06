package iristk.speech.nuance9;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.jna.Memory;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import iristk.audio.AudioSource;
import iristk.audio.AudioUtil;
import iristk.audio.PortAudioSource;
import iristk.speech.RecognitionResult;
import iristk.speech.Recognizer;
import iristk.speech.RecognitionResult.ResultType;
import iristk.speech.nuance9.SWIep.SWIepAudioSamples;
import iristk.speech.nuance9.SWIrec.SWIrecAudioSamples;
import iristk.speech.RecognizerException;
import iristk.speech.RecognizerListener;
import iristk.speech.RecognizerListeners;
import iristk.util.BlockingByteQueue;
import iristk.util.Language;

public class NuanceRecognizer implements Recognizer {

	private AudioSource audioSource;
	private BaseRecognizer recognizer;
	private boolean running = false;
	private int bufferSize;
	BlockingByteQueue speechBytes = new BlockingByteQueue();
	private boolean cancel = false;
	private int endSilTimeout;
	private ListeningThread listeningThread;
	RecognizerListeners listeners = new RecognizerListeners();

	public NuanceRecognizer() throws RecognizerException {
		this(new PortAudioSource(8000, 1));
	}
	
	public NuanceRecognizer(AudioSource audioSource) throws RecognizerException {
		this.audioSource = audioSource;
		try {
			recognizer = new BaseRecognizer();
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		}
	}
	
	//public void setAudioSource(AudioSource audioSource) {
	//	this.audioSource = audioSource;
	//}
	
	public void makeWords(boolean cond) {
		recognizer.makeWords(cond);
	}
	
	@Override
	public void loadGrammar(String name, URI uri) throws RecognizerException {
		try {
			recognizer.loadGrammar(name, uri);
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		}
	}
	
	@Override
	public void loadGrammar(String name, String grammarString) throws RecognizerException {
		try {
			recognizer.loadGrammar(name, grammarString);
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		}
	}
	
	@Override
	public void activateGrammar(String name, float weight) throws RecognizerException {
		try {
			recognizer.activateGrammar(name, (int)weight);
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		}
	}
	
	@Override
	public void deactivateGrammar(String name) throws RecognizerException {
		try {
			recognizer.deactivateGrammar(name);
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		}
	}

	@Override
	public boolean stopListen() throws RecognizerException {
		try {
			if (running) {
				recognizer.stopRecognize();
				cancel = true;
				listeningThread.join();
				return true;
			} else {
				return false;
			}
		} catch (InterruptedException e) {
			throw new RecognizerException(e.getMessage());
		}
	}

	@Override
	public void setNoSpeechTimeout(int msec) throws RecognizerException {
		recognizer.setEpParameter("timeout", new Integer(msec).toString());
	}
	
	@Override
	public void setEndSilTimeout(int msec) throws RecognizerException {
		endSilTimeout = msec;
		recognizer.setRecParameter("incompletetimeout", new Integer(msec).toString());
		recognizer.setRecParameter("completetimeout", new Integer(msec).toString());
	}

	@Override
	public void setMaxSpeechTimeout(int msec) {
		recognizer.setRecParameter("maxspeechtimeout", new Integer(msec).toString());
	}
	
	@Override
	public void startListen() throws RecognizerException {
		// Added this stop, don't know if it is needed
		stopListen();
		listeningThread = new ListeningThread();
		listeningThread.start();
	}

	private class ListeningThread extends Thread {
		
		public ListeningThread() {
			super("ListeningThread");
		}
		
		@Override
		public void run() {
			try {
				listen(audioSource);
			} catch (NuanceException e) {
				e.printStackTrace();
			}
		}
	}

	public void listen(AudioSource source) throws NuanceException {
		if (!running) {
			running  = true;
			
			bufferSize = 1600;
			
			byte[] readBuffer = new byte[bufferSize];
			byte[] readBuffer16 = new byte[bufferSize * 2];
			byte[] writeBuffer = new byte[bufferSize];
			IntByReference state = new IntByReference();
			IntByReference beginSample = new IntByReference();
			IntByReference endSample = new IntByReference();
			SWIepAudioSamples epSamples = new SWIepAudioSamples();
			epSamples.type = new WString(BaseRecognizer.getEncoding(source.getAudioFormat()));
			epSamples.samples = new Memory(bufferSize);
			epSamples.len = bufferSize;
			SWIrecAudioSamples recSamples = new SWIrecAudioSamples();
			recSamples.type = new WString(BaseRecognizer.getEncoding(source.getAudioFormat()));
			recSamples.samples = new Memory(bufferSize);
			boolean first = true;
			boolean speechStart = false;
			cancel = false;
			
			boolean resample = source.getAudioFormat().getFrameRate() != 8000;
			speechBytes.reset();
			byte[] speechBuffer = (resample ? new byte[bufferSize * 2] : new byte[bufferSize]);
			
			recognizer.startRecognize();
			
			recognizer.epStart();
			recognizer.epPromptDone();
			source.start();
			while (!cancel) {
				//TODO: should be possible to resample from any rate
				if (source.getAudioFormat().getFrameRate() == 16000) {
					source.read(readBuffer16, 0, readBuffer16.length);
					speechBytes.write(readBuffer16);
					AudioUtil.resample(readBuffer16, 0, readBuffer16.length, readBuffer, 0, 2, 16000, 8000);
				} else {
					source.read(readBuffer, 0, readBuffer.length);
					speechBytes.write(readBuffer);
				}
				epSamples.samples.write(0, readBuffer, 0, bufferSize);
				if (first)
					epSamples.status = SWIrec.SWIrec_SAMPLE_FIRST;
				else
					epSamples.status = SWIrec.SWIrec_SAMPLE_CONTINUE;
				recognizer.epWrite(epSamples, state, beginSample, endSample);
								
				if (state.getValue() == SWIep.SWIep_IN_SPEECH) {
					if (!speechStart) {
						listeners.startOfSpeech(beginSample.getValue() / source.getAudioFormat().getFrameRate());
						try {
							speechBytes.skip(beginSample.getValue() * (resample ? 4 : 2));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						speechStart = true;
					}
					if (speechBytes.available() >= speechBuffer.length) {
						try {
							speechBytes.read(speechBuffer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						listeners.speechSamples(speechBuffer, 0, speechBuffer.length);
					}
					recognizer.epRead(recSamples, state, bufferSize);
					while (recSamples.len > 0) {
						recognizer.recAudioWrite(recSamples);
						recSamples.samples.read(0, writeBuffer, 0, bufferSize);
						recognizer.epRead(recSamples, state, bufferSize);
					}
					if (!recognizer.isRunning()) {
						listeners.endOfSpeech(((speechBytes.getWritePos() / (resample ? 32f : 16f)) - endSilTimeout) / 1000f);
						listeners.recognitionResult(recognizer.getResult());
						break;
					}
				} else if (state.getValue() == SWIep.SWIep_TIMEOUT) {
					recognizer.stopRecognize();
					listeners.recognitionResult(new NuanceResult(ResultType.NOSPEECH));
					break;
				} else if (state.getValue() == SWIep.SWIep_MAX_SPEECH) {
					recognizer.stopRecognize();
					listeners.recognitionResult(new NuanceResult(ResultType.MAXSPEECH));
					break;
				} else if (state.getValue() == SWIep.SWIep_LOOKING_FOR_SPEECH) {
				} else {
					System.err.println("Nuance unknown result: " + state.getValue());
				}
				first = false;
			}
			source.stop();
			recognizer.epStop();
			
			running = false;
		} 
	}

	@Override
	public RecognitionResult recognizeFile(File file) throws RecognizerException {
		try {
			recognizer.acousticStateReset();
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
			String encoding = BaseRecognizer.getEncoding(audioInputStream.getFormat());
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			int bufferSize = 1024;
			int	nBytesRead = 0;
			int pos = 0;
			byte[] buffer = new byte[bufferSize];
			while (true) {
				nBytesRead = audioInputStream.read(buffer, 0, bufferSize);
				if (nBytesRead == -1) 
					break;
				dataStream.write(buffer, 0, nBytesRead);
				pos += nBytesRead;
			}
			byte[] data = dataStream.toByteArray();	
			audioInputStream.close();	
			return recognizeAudioBytes(encoding, data);
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		} catch (UnsupportedAudioFileException e) {
			throw new RecognizerException(e.getMessage());
		} catch (IOException e) {
			throw new RecognizerException(e.getMessage());
		}
	}
		
	public NuanceResult recognizeAudioBytes(String encoding, byte[] data) throws NuanceException {
		recognizer.recRecognizerStart();
		
		SWIrecAudioSamples samples = new SWIrecAudioSamples();
		samples.type = new WString(encoding);
		samples.samples = new Memory(bufferSize);
		
		for (int i = 0; i < data.length; i += bufferSize) {
			samples.len = bufferSize;
			if (i == 0) {
				samples.status = SWIrec.SWIrec_SAMPLE_FIRST;
			} else if ((data.length - i) <= bufferSize) {
				samples.status = SWIrec.SWIrec_SAMPLE_LAST;
				samples.len = data.length - i;
			} else {
				samples.status = SWIrec.SWIrec_SAMPLE_CONTINUE;
			}
			samples.samples.write(0, data, i, samples.len);
			recognizer.recAudioWrite(samples);
		}

		IntByReference status = new IntByReference();
		IntByReference type = new IntByReference();
		PointerByReference result = new PointerByReference();
		
		do {
			recognizer.recRecognizerCompute(-1, status, type, result);
			//if (debug)
			//	System.out.println("status: " + status.getValue() + ", type: " + type.getValue());
		} while (status.getValue() > 1);
		
		if (status.getValue() == 0)
			return new NuanceResult(result.getValue(), true);
		else
			return new NuanceResult(ResultType.FINAL, RecognitionResult.NOMATCH);
	}

	@Override
	public void setLanguage(Language lang) {
	}

	@Override
	public void addRecognizerListener(RecognizerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void setPartialResults(boolean cond) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	public static void main(String[] args) throws NuanceException {
		JSoundAudioSource mic = new JSoundAudioSource();
		//MultiChannelASIOAudioSource m= new MultiChannelASIOAudioSource("cone",8);
		//m.start();
		//AudioSource mic= m.getChannel(4);
		NuanceRecognizer recognizer = new NuanceRecognizer();
		recognizer.loadGrammar("Digits", "src\\iristk\\app\\chess\\ChessGrammar.xml");
		recognizer.activateGrammar("Digits", 1);
		recognizer.setEndSpeechTimeout(500);
		while (true) {
			System.out.print("Listening: ");
			NuanceResult result = recognizer.recognize(mic);
			System.out.println(result);
		}
	}
	*/

}
