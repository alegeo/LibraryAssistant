package iristk.speech.nuance9;

import java.io.File;
import java.net.URI;
import iristk.audio.AudioSource;
import iristk.audio.AudioUtil;
import iristk.audio.PortAudioSource;
import iristk.speech.RecognitionResult;
import iristk.speech.Recognizer;
import iristk.speech.RecognizerException;
import iristk.speech.RecognitionResult.ResultType;
import iristk.speech.nuance9.SWIep.SWIepAudioSamples;
import iristk.speech.RecognizerListener;
import iristk.speech.RecognizerListeners;
import iristk.util.BlockingByteQueue;
import iristk.util.Language;

import com.sun.jna.Memory;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class NuanceEndpointer implements Recognizer {

	private boolean running = false;
	private int bufferSize;
	private boolean cont;
	BlockingByteQueue speechBytes = new BlockingByteQueue();
	private BaseRecognizer recognizer;
	private AudioSource audioSource;
	private EndpointerThread epThread;
	public boolean generateEmptyResult = false;
	private RecognizerListeners listeners = new RecognizerListeners();

	public NuanceEndpointer() throws RecognizerException {
		this(new PortAudioSource(8000, 1));
	}
	
	public NuanceEndpointer(AudioSource audioSource) throws RecognizerException {
		this.audioSource = audioSource;
		try {
			recognizer = new BaseRecognizer();
		} catch (NuanceException e) {
			throw new RecognizerException(e.getMessage());
		}
		recognizer.setEpParameter("swiep_mode", "begin_end");
	}
	
	private class EndpointerThread extends Thread {
		@Override
		public void run() {
			try {
				boolean foundSpeech = endpoint(audioSource); 
				if (!foundSpeech) {
					NuanceResult result = new NuanceResult(ResultType.NOSPEECH);
					recognitionResult(result);
				}
			} catch (NuanceException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean endpoint(AudioSource source) throws NuanceException {
		boolean result = false;
		if (!running) {
			running  = true;
			bufferSize = 1600;
			
			byte[] buffer = new byte[bufferSize];
			byte[] buffer16 = new byte[bufferSize * 2];
			
			IntByReference state = new IntByReference();
			IntByReference beginSample = new IntByReference();
			IntByReference endSample = new IntByReference();
			SWIepAudioSamples epSamples = new SWIepAudioSamples();
			epSamples.type = new WString(BaseRecognizer.getEncoding(source.getAudioFormat()));
			epSamples.samples = new Memory(bufferSize);
			epSamples.len = bufferSize;
			boolean first = true;
			boolean speechStart = false;
			cont = true;
			boolean resample = source.getAudioFormat().getFrameRate() != 8000;
			speechBytes.reset();
			byte[] speechBuffer = (resample ? new byte[bufferSize * 2] : new byte[bufferSize]);

			float startPosition = source.getPosition() / source.getAudioFormat().getFrameRate();
			
			source.start();
			
			recognizer.epStart();
			recognizer.epPromptDone();
			
			// Collect new samples from audio source 
			while (cont) {
				if (resample) {
					source.read(buffer16, 0, buffer16.length);
					AudioUtil.resample(buffer16, buffer, 2, 16000, 8000);
					speechBytes.write(buffer16);
				} else {
					source.read(buffer, 0, buffer.length);
					speechBytes.write(buffer, 0, buffer.length);
				}
				epSamples.samples.write(0, buffer, 0, bufferSize);
								
				if (first)
					epSamples.status = SWIrec.SWIrec_SAMPLE_FIRST;
				else
					epSamples.status = SWIrec.SWIrec_SAMPLE_CONTINUE;
				recognizer.epWrite(epSamples, state, beginSample, endSample);
				
				if (state.getValue() == SWIep.SWIep_IN_SPEECH) {
					if (!speechStart) {
						listeners.startOfSpeech(startPosition + (beginSample.getValue() / 8000f));
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
						speechSamples(speechBuffer, 0, speechBuffer.length);
					}
				} else if (state.getValue() == SWIep.SWIep_AFTER_SPEECH) {
					int rest = endSample.getValue() * (resample ? 4 : 2) - speechBytes.getReadPos();
					for (; rest >= speechBuffer.length; rest -= speechBuffer.length) {
						try {
							speechBytes.read(speechBuffer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						speechSamples(speechBuffer, 0, speechBuffer.length);
					}
					listeners.endOfSpeech(startPosition + (endSample.getValue() / 8000f));
					NuanceResult nresult = null;
					if (generateEmptyResult) {
						nresult = new NuanceResult(ResultType.FINAL, RecognitionResult.NOMATCH);
					}
					recognitionResult(nresult);
					
					result = true;
					break;
				} else if (state.getValue() == SWIep.SWIep_TIMEOUT) {
					break;
				} else if (state.getValue() == SWIep.SWIep_MAX_SPEECH) {
					break;
				} 
				first = false;
			}
			recognizer.epStop();

			source.stop();
			running = false;
		}
		return result;
	}

	protected void recognitionResult(RecognitionResult result) {
		listeners.recognitionResult(result);
	}
	
	protected void speechSamples(byte[] samples, int pos, int len) {
		listeners.speechSamples(samples, pos, len);
	}
	
	@Override
	public void startListen() throws RecognizerException {
		epThread = new EndpointerThread();
		epThread.start();
	}

	@Override
	public boolean stopListen() throws RecognizerException {
		if (running) {
			try {
				cont = false;
				epThread.join();
				return true;
			} catch (InterruptedException e) {
				throw new RecognizerException(e.getMessage());
			}
		} else {
			return false;
		}
	}
	
	
	@Override
	public void setEndSilTimeout(int msec) {
		recognizer.setEpParameter("incompletetimeout", new Integer(msec).toString());
	}

	@Override
	public void setMaxSpeechTimeout(int msec) {
		recognizer.setEpParameter("maxspeechtimeout", new Integer(msec).toString());
	}

	@Override
	public void setNoSpeechTimeout(int msec) {
		recognizer.setEpParameter("timeout", new Integer(msec).toString());
	}

	@Override
	public void loadGrammar(String name, URI uri) throws RecognizerException {
	}

	@Override
	public void loadGrammar(String name, String grammarString) throws RecognizerException {
	}

	@Override
	public void activateGrammar(String name, float weight) throws RecognizerException {
	}

	@Override
	public void deactivateGrammar(String name) throws RecognizerException {
	}

	@Override
	public RecognitionResult recognizeFile(File file) throws RecognizerException {
		return null;
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

}
