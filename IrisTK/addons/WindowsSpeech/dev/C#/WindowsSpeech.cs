using System;
using System.IO;
using System.Collections.Generic;
using System.Threading;
using System.Speech.Recognition;
using System.Speech.Recognition.SrgsGrammar;
using System.Speech.Synthesis;
using System.Speech.AudioFormat;
using System.Collections;
using System.Globalization;
using System.Xml;

namespace iristk.speech.windows
{
    public interface IResultListener {

        void speechDetected(int audioLevel);

        void recognizeCompleted(Result result);

        void recognizeHypothesis(Result result);

    }

    public class Result : ArrayList {

        public bool cancelled;
        public bool timeout;
        public double pitch;
        public int length;
        public string grammar;

        public int size() {
            return base.Count;
        }

        public Hypothesis getHypothesis(int index) {
            return (Hypothesis)base[index];
        }

        public double getPitch()
        {
            return pitch;
        }

        public string getGrammar()
        {
            return grammar;
        }

        public bool isCancelled() {
            return cancelled;
        }

        public bool isTimeout()
        {
            return timeout;
        }

        public int getLength()
        {
            return length;
        }

    }

    public class Hypothesis : ArrayList
    {

        public String text;

        public float confidence;

        public SemanticStruct semantics;

        public String getText()
        {
            return text;
        }

        public float getConfidence()
        {
            return confidence;
        }

        public int size()
        {
            return base.Count;
        }

        public SemanticStruct getSemantics()
        {
            return semantics;
        }

        public Word getWord(int index)
        {
            return (Word)base[index];
        }

    }

    public class Word {
        public String text;
        public float confidence;

        public String getText() {
            return text;
        }

        public float getConfidence() {
            return confidence;
        }
    }

    public class SemanticStruct
    {
        public Object value;
        public float confidence;
        private Dictionary<string, SemanticStruct>  dict = new Dictionary<string, SemanticStruct>();

        public Object getValue()
        {
            return value;
        }

        public float getConfidence()
        {
            return confidence;
        }

        public int getKeysCount()
        {
            return dict.Count;
        }

        public String getKey(int i)
        {
            int c = 0;
            foreach (KeyValuePair<string, SemanticStruct> kvp in dict)
            {
                if (c == i)
                    return kvp.Key;
                c++;
            }
            return null;
        }

        public SemanticStruct getValue(int i)
        {
            int c = 0;
            foreach (KeyValuePair<string, SemanticStruct> kvp in dict)
            {
                if (c == i)
                    return kvp.Value;
                c++;
            }
            return null;
        }

        internal void Add(string key, SemanticStruct semanticStruct)
        {
            dict.Add(key, semanticStruct);
        }
    }

    public class ManagedRecognizer {

        private IResultListener listener = null;

        private SpeechRecognitionEngine speechRecognitionEngine = null;

        private String saveFolderPath = null;
        private String audioId = null;
        private int maxAudioLevel = 0;
        private SpeechAudioStream audioStream = null;
        
        public int getRecognizerSetting(String name)
        {
            return (int) speechRecognitionEngine.QueryRecognizerSetting(name);
        }

        public void setRecognizerSetting(String name, int value)
        {
            speechRecognitionEngine.UpdateRecognizerSetting(name, value);
        }

        public void setMaxAlternates(int max)
        {
            speechRecognitionEngine.MaxAlternates = max;
        }

        public void startRecognizing()
        {
            speechRecognitionEngine.RecognizeAsync();
        }

        private SemanticStruct createStruct(SemanticValue init)
        {
            SemanticStruct result = new SemanticStruct();
            if (init.Value != null)
            {
                result.value = init.Value;
                result.confidence = init.Confidence;
            }
            foreach (KeyValuePair<String, SemanticValue> child in init)
            {
                result.Add(child.Key, createStruct(child.Value));
            }
            return result;
        }

        private Hypothesis createHypothesis(RecognizedPhrase phrase)
        {
            Hypothesis hyp = new Hypothesis();
            hyp.text = phrase.Text;
            hyp.confidence = phrase.Confidence;
            if (phrase.Semantics != null)
                hyp.semantics = createStruct(phrase.Semantics);
            foreach (RecognizedWordUnit word in phrase.Words)
            {
                Word rword = new Word();
                rword.text = word.Text;
                rword.confidence = word.Confidence;
                hyp.Add(rword);
            }
            return hyp;
        }

        private void processSpeechHypothesizedEventArgs(SpeechHypothesizedEventArgs result)
        {
            if (listener != null)
            {
                Result res = new Result();
                res.cancelled = false;
                res.timeout = false;

                if (result.Result != null)
                {
                    res.Add(createHypothesis(result.Result));
                }

                listener.recognizeHypothesis(res);
            }
        }

        private void processRecognizeCompletedEventArgs(RecognizeCompletedEventArgs result)
        {
            if (listener != null) {
                Result res = new Result();

                res.cancelled = result.Cancelled;
                res.timeout = result.InitialSilenceTimeout;

                if (result.Result != null)
                {
                    if (result.Result.Alternates.Count > 0)
                    {
                        foreach (RecognizedPhrase phrase in result.Result.Alternates)
                        {
                            res.Add(createHypothesis(phrase));
                        }
                    }
                    res.grammar = result.Result.Grammar.ToString();
                    res.length = (int)Math.Round(result.Result.Audio.Duration.TotalMilliseconds);
                    if (audioId != null && saveFolderPath != null)
                    {
                        using (Stream outputStream = new FileStream(saveFolderPath + "\\" + audioId + ".wav", FileMode.Create))
                        {
                            result.Result.Audio.WriteToWaveStream(outputStream);
                            outputStream.Close();
                        }
                    }
                }
               
                listener.recognizeCompleted(res);
            }
        }

        public void saveAudio(String folderPath)
        {
            this.saveFolderPath = folderPath;
        }

        public void registerListener(IResultListener listener) {
            this.listener = listener;
        }

        public void init(String lang) {

            foreach (RecognizerInfo config in SpeechRecognitionEngine.InstalledRecognizers()) {
                //if (config.Culture.Equals(requiredCulture) && config.Id == requiredId) {
                Console.Write("Found lang " + config.Culture.IetfLanguageTag);
                if (lang.Equals(config.Culture.IetfLanguageTag))
                {
                    speechRecognitionEngine = new SpeechRecognitionEngine(config);
                    Console.WriteLine(" ... selected");
                    break;
                }
                else
                {
                    Console.WriteLine();
                }
                //}
            }
            if (speechRecognitionEngine == null)
            {
                Console.WriteLine("Error: Language not found: " + lang);
                return;
            }
            
            speechRecognitionEngine.MaxAlternates = 1;

            speechRecognitionEngine.SpeechDetected +=
                delegate(object sender, SpeechDetectedEventArgs eventArgs) {
                    if (listener != null) {
                        listener.speechDetected(maxAudioLevel);
                    }
                };

            speechRecognitionEngine.RecognizeCompleted +=
                delegate(object sender, RecognizeCompletedEventArgs eventArgs)
                {
                    processRecognizeCompletedEventArgs(eventArgs);
                };

           
            speechRecognitionEngine.SpeechHypothesized +=
                delegate(object sender, SpeechHypothesizedEventArgs eventArgs) {
                    processSpeechHypothesizedEventArgs(eventArgs);
                };

            speechRecognitionEngine.AudioLevelUpdated +=
                delegate(object sender, AudioLevelUpdatedEventArgs eventArgs) {
                    //Console.WriteLine(eventArgs.AudioLevel);
                    maxAudioLevel = Math.Max(maxAudioLevel, eventArgs.AudioLevel);
                };

            //setInputToDefaultAudioDevice();
        }

        public int getAudioLevel()
        {
            return speechRecognitionEngine.AudioLevel;
        }

        public void setInputToAudioStream(int sampleRate, int bufferSize) {
            audioStream = new SpeechAudioStream(bufferSize);
            speechRecognitionEngine.SetInputToAudioStream(audioStream, new SpeechAudioFormatInfo(sampleRate, AudioBitsPerSample.Sixteen, AudioChannel.Mono));
        }

        public SpeechAudioStream getAudioStream()
        {
            return audioStream;
        }

        public void setInputToDefaultAudioDevice()
        {
            speechRecognitionEngine.SetInputToDefaultAudioDevice();
        }

        public void setInputToWaveFile(string path)
        {
            speechRecognitionEngine.SetInputToWaveFile(path);
        }

        private Dictionary<String, Grammar> grammars = new Dictionary<string, Grammar>();

        /*
        private Dictionary<String, float> grammarWeights = new Dictionary<string, float>();

        public void saveGrammarWeights()
        {
            foreach (String name in grammars.Keys) {
                grammarWeights[name] = grammars[name].Weight;
            }
        }

        public void restoreGrammarWeights()
        {
            foreach (String name in grammars.Keys)
            {
                grammars[name].Weight = grammarWeights[name];
            }
        }

        public void setGrammarPriority(String name, int priority)
        {
            grammars[name].Priority = priority;
        }

        public void setGrammarWeight(String name, float weight)
        {
            grammars[name].Weight = weight;
        }
         * */

        public void deactivateGrammar(String name)
        {
            speechRecognitionEngine.UnloadGrammar(grammars[name]);
        }

        public void activateGrammar(String name, float weight)
        {
            grammars[name].Weight = weight;
            activateGrammar(name);
        }

        public void activateGrammar(String name)
        {
            speechRecognitionEngine.LoadGrammar(grammars[name]);
        }

        public void loadDictationGrammar(String name)
        {
            grammars[name] = new DictationGrammar();
        }

        public void loadGrammarFromPath(String name, String path)
        {
            grammars[name] = new Grammar(path);
        }

        public void loadGrammarFromString(String name, String grammar)
        {
            byte[] bytes = new byte[grammar.Length * sizeof(char)];
            System.Buffer.BlockCopy(grammar.ToCharArray(), 0, bytes, 0, bytes.Length);
            grammars[name] = new Grammar(new SrgsDocument(XmlReader.Create(new MemoryStream(bytes))));
        }

        public void recognizeCancel()
        {
            speechRecognitionEngine.RecognizeAsyncCancel();
        }

	    public void setNoSpeechTimeout(int msec) {
            speechRecognitionEngine.InitialSilenceTimeout = new TimeSpan(0, 0, 0, 0, msec);
	    }

        public void setEndSilTimeout(int msec)
        {
            speechRecognitionEngine.EndSilenceTimeout = new TimeSpan(0, 0, 0, 0, msec);
	    }

	    public void setMaxSpeechTimeout(int msec) {
            speechRecognitionEngine.BabbleTimeout = new TimeSpan(0, 0, 0, 0, msec);
	    }

        public void recognize() {
           // this.audioId = id;
            this.maxAudioLevel = 0;
            //speechRecognitionEngine.EndSilenceTimeoutAmbiguous = new TimeSpan(0, 0, 0, 0, endSilence);
            speechRecognitionEngine.RecognizeAsync();
        }
    }

    public class Phoneme
    {
        public float duration;
        public string label;

        public float getDuration()
        {
            return duration;
        }

        public string getLabel()
        {
            return label;
        }

    }

    public class Phonemes : ArrayList
    {
        public Phoneme getPhoneme(int index) {
            return (Phoneme)base[index];
        }

        public int getLength()
        {
            return base.Count;
        }
    }

    public class Voices : ArrayList
    {
        public Voice getVoice(int index)
        {
            return (Voice)base[index];
        }

        public int getLength()
        {
            return base.Count;
        }
    }

    public class Voice
    {
        private String name;
        private String lang;
        private String gender;

        public Voice(String name, String lang, String gender)
        {
            this.name = name;
            this.lang = lang;
            this.gender = gender;
        }

        public String getName()
        {
            return name;
        }

        public String getLang()
        {
            return lang;
        }

        public String getGender()
        {
            return gender;
        }
    }

    public class ManagedSynthesizer
    {

        SpeechSynthesizer synth;
        SpeechAudioFormatInfo format;
        Phonemes phonemes;

        public ManagedSynthesizer()
        {
            synth = new SpeechSynthesizer();
            format = new SpeechAudioFormatInfo(16000, AudioBitsPerSample.Sixteen, AudioChannel.Mono);
            synth.PhonemeReached += delegate(object sender, PhonemeReachedEventArgs eventArgs)
            {
                Phoneme phon = new Phoneme();
                phon.label = eventArgs.Phoneme;
                phon.duration = (float)eventArgs.Duration.TotalSeconds;
                phonemes.Add(phon);
            };
        }

        public void setVoice(String name)
        {
            synth.SelectVoice(name);
        }

        public Phonemes synthesize(String ssml, String path)
        {
            phonemes = new Phonemes();
            synth.SetOutputToWaveFile(path, format);
            synth.SpeakSsml(ssml);
            synth.SetOutputToNull();
            return phonemes;
        }

        public Phonemes transcribe(String ssml)
        {
            phonemes = new Phonemes();
            synth.SetOutputToNull();
            synth.SpeakSsml(ssml);
            return phonemes;
        }

        public Voices getVoices()
        {
            Voices voices = new Voices();
            foreach (InstalledVoice voice in synth.GetInstalledVoices())
            {
                if (voice.Enabled)
                {
                    voices.Add(new Voice(voice.VoiceInfo.Name, voice.VoiceInfo.Culture.Name, voice.VoiceInfo.Gender.ToString()));
                }
            }
            return voices;
        }

        public void printVoices()
        {
            foreach (InstalledVoice voice in synth.GetInstalledVoices())
            {
                VoiceInfo info = voice.VoiceInfo;
                Console.WriteLine(" Name:          " + info.Name);
                Console.WriteLine(" Culture:       " + info.Culture);
                Console.WriteLine(" Age:           " + info.Age);
                Console.WriteLine(" Gender:        " + info.Gender);
                Console.WriteLine(" Description:   " + info.Description);
                Console.WriteLine(" ID:            " + info.Id);
                Console.WriteLine(" Enabled:       " + voice.Enabled);
                Console.WriteLine();
            }
        }

    }

    public class SpeechAudioStream : Stream
    {
        private AutoResetEvent _writeEvent;
        private List<byte> _buffer;
        private int _buffersize;
        private int _readposition;
        private int _writeposition;
        private bool _reset;

        public SpeechAudioStream(int bufferSize)
        {
            _writeEvent = new AutoResetEvent(false);
            _buffersize = bufferSize;
            _buffer = new List<byte>(_buffersize);
            for (int i = 0; i < _buffersize; i++)
                _buffer.Add(new byte());
            _readposition = 0;
            _writeposition = 0;
        }

        public void Clear()
        {
            _readposition = 0;
            _writeposition = 0;
        }

        public override bool CanRead
        {
            get { return true; }
        }

        public override bool CanSeek
        {
            get { return false; }
        }

        public override bool CanWrite
        {
            get { return true; }
        }

        public override long Length
        {
            get { return -1L; }
        }

        public override long Position
        {
            get { return 0L; }
            set { }
        }

        public override long Seek(long offset, SeekOrigin origin)
        {
            return 0L;
        }

        public override void SetLength(long value)
        {

        }

        public override int Read(byte[] buffer, int offset, int count)
        {
            int i = 0;
            while (i < count && _writeEvent != null)
            {
                if (!_reset && _readposition >= _writeposition)
                {
                    _writeEvent.WaitOne(100, true);
                    continue;
                }
                buffer[i] = _buffer[_readposition + offset];
                _readposition++;
                if (_readposition == _buffersize)
                {
                    _readposition = 0;
                    _reset = false;
                }
                i++;
            }

            return count;
        }

        public override void Write(byte[] buffer, int offset, int count)
        {
            for (int i = offset; i < offset + count; i++)
            {
                _buffer[_writeposition] = buffer[i];
                _writeposition++;
                if (_writeposition == _buffersize)
                {
                    _writeposition = 0;
                    _reset = true;
                }
            }
            _writeEvent.Set();

        }

        public override void Close()
        {
            _writeEvent.Close();
            _writeEvent = null;
            base.Close();
        }

        public override void Flush()
        {

        }
    }

}

