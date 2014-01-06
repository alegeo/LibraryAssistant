@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "c:\dropbox\iristk\addons\windowsspeech\dev\jni4net.j-0.8.6.0.jar"; "jvm\iristk/speech/windows\IResultListener.java" "jvm\iristk/speech/windows\IResultListener_.java" "jvm\iristk/speech/windows\Result.java" "jvm\iristk/speech/windows\Hypothesis.java" "jvm\iristk/speech/windows\Word.java" "jvm\iristk/speech/windows\SemanticStruct.java" "jvm\iristk/speech/windows\ManagedRecognizer.java" "jvm\iristk/speech/windows\Phoneme.java" "jvm\iristk/speech/windows\Phonemes.java" "jvm\iristk/speech/windows\Voices.java" "jvm\iristk/speech/windows\Voice.java" "jvm\iristk/speech/windows\ManagedSynthesizer.java" "jvm\iristk/speech/windows\SpeechAudioStream.java" 
IF %ERRORLEVEL% NEQ 0 goto end


echo WindowsSpeech.j4n.jar 
jar cvf WindowsSpeech.j4n.jar  -C target\classes "iristk\speech\windows\IResultListener.class"  -C target\classes "iristk\speech\windows\IResultListener_.class"  -C target\classes "iristk\speech\windows\__IResultListener.class"  -C target\classes "iristk\speech\windows\Result.class"  -C target\classes "iristk\speech\windows\Hypothesis.class"  -C target\classes "iristk\speech\windows\Word.class"  -C target\classes "iristk\speech\windows\SemanticStruct.class"  -C target\classes "iristk\speech\windows\ManagedRecognizer.class"  -C target\classes "iristk\speech\windows\Phoneme.class"  -C target\classes "iristk\speech\windows\Phonemes.class"  -C target\classes "iristk\speech\windows\Voices.class"  -C target\classes "iristk\speech\windows\Voice.class"  -C target\classes "iristk\speech\windows\ManagedSynthesizer.class"  -C target\classes "iristk\speech\windows\SpeechAudioStream.class"  > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo WindowsSpeech.j4n.dll 
csc /nologo /warn:0 /t:library /out:WindowsSpeech.j4n.dll /recurse:clr\*.cs  /reference:"C:\dropbox\iristk\addons\WindowsSpeech\dev\work\WindowsSpeech.dll" /reference:"C:\dropbox\iristk\addons\WindowsSpeech\dev\jni4net.n-0.8.6.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
