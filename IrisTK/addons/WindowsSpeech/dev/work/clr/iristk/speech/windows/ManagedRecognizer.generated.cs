//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
//     Runtime Version:4.0.30319.1008
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace iristk.speech.windows {
    
    
    #region Component Designer generated code 
    public partial class ManagedRecognizer_ {
        
        public static global::java.lang.Class _class {
            get {
                return global::iristk.speech.windows.@__ManagedRecognizer.staticClass;
            }
        }
    }
    #endregion
    
    #region Component Designer generated code 
    [global::net.sf.jni4net.attributes.JavaProxyAttribute(typeof(global::iristk.speech.windows.ManagedRecognizer), typeof(global::iristk.speech.windows.ManagedRecognizer_))]
    [global::net.sf.jni4net.attributes.ClrWrapperAttribute(typeof(global::iristk.speech.windows.ManagedRecognizer), typeof(global::iristk.speech.windows.ManagedRecognizer_))]
    internal sealed partial class @__ManagedRecognizer : global::java.lang.Object {
        
        internal new static global::java.lang.Class staticClass;
        
        private @__ManagedRecognizer(global::net.sf.jni4net.jni.JNIEnv @__env) : 
                base(@__env) {
        }
        
        private static void InitJNI(global::net.sf.jni4net.jni.JNIEnv @__env, java.lang.Class @__class) {
            global::iristk.speech.windows.@__ManagedRecognizer.staticClass = @__class;
        }
        
        private static global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> @__Init(global::net.sf.jni4net.jni.JNIEnv @__env, global::java.lang.Class @__class) {
            global::System.Type @__type = typeof(__ManagedRecognizer);
            global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> methods = new global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod>();
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getRecognizerSetting", "getRecognizerSetting0", "(Ljava/lang/String;)I"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setRecognizerSetting", "setRecognizerSetting1", "(Ljava/lang/String;I)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setMaxAlternates", "setMaxAlternates2", "(I)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "startRecognizing", "startRecognizing3", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "saveAudio", "saveAudio4", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "registerListener", "registerListener5", "(Liristk/speech/windows/IResultListener;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "init", "init6", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getAudioLevel", "getAudioLevel7", "()I"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setInputToAudioStream", "setInputToAudioStream8", "(II)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getAudioStream", "getAudioStream9", "()Liristk/speech/windows/SpeechAudioStream;"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setInputToDefaultAudioDevice", "setInputToDefaultAudioDevice10", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setInputToWaveFile", "setInputToWaveFile11", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "deactivateGrammar", "deactivateGrammar12", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "activateGrammar", "activateGrammar13", "(Ljava/lang/String;F)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "activateGrammar", "activateGrammar14", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "loadDictationGrammar", "loadDictationGrammar15", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "loadGrammarFromPath", "loadGrammarFromPath16", "(Ljava/lang/String;Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "loadGrammarFromString", "loadGrammarFromString17", "(Ljava/lang/String;Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "recognizeCancel", "recognizeCancel18", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setNoSpeechTimeout", "setNoSpeechTimeout19", "(I)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setEndSilTimeout", "setEndSilTimeout20", "(I)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setMaxSpeechTimeout", "setMaxSpeechTimeout21", "(I)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "recognize", "recognize22", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "__ctorManagedRecognizer0", "__ctorManagedRecognizer0", "(Lnet/sf/jni4net/inj/IClrProxy;)V"));
            return methods;
        }
        
        private static int getRecognizerSetting0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name) {
            // (Ljava/lang/String;)I
            // (LSystem/String;)I
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            int @__return = default(int);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__return = ((int)(@__real.getRecognizerSetting(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name))));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void setRecognizerSetting1(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name, int value) {
            // (Ljava/lang/String;I)V
            // (LSystem/String;I)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setRecognizerSetting(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name), value);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void setMaxAlternates2(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, int max) {
            // (I)V
            // (I)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setMaxAlternates(max);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void startRecognizing3(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.startRecognizing();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void saveAudio4(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle folderPath) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.saveAudio(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, folderPath));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void registerListener5(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle listener) {
            // (Liristk/speech/windows/IResultListener;)V
            // (Liristk/speech/windows/IResultListener;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.registerListener(global::net.sf.jni4net.utils.Convertor.FullJ2C<global::iristk.speech.windows.IResultListener>(@__env, listener));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void init6(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle lang) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.init(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, lang));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static int getAudioLevel7(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()I
            // ()I
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            int @__return = default(int);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__return = ((int)(@__real.getAudioLevel()));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void setInputToAudioStream8(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, int sampleRate, int bufferSize) {
            // (II)V
            // (II)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setInputToAudioStream(sampleRate, bufferSize);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static global::net.sf.jni4net.utils.JniHandle getAudioStream9(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()Liristk/speech/windows/SpeechAudioStream;
            // ()Liristk/speech/windows/SpeechAudioStream;
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            global::net.sf.jni4net.utils.JniHandle @__return = default(global::net.sf.jni4net.utils.JniHandle);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__return = global::net.sf.jni4net.utils.Convertor.StrongC2Jp<global::iristk.speech.windows.SpeechAudioStream>(@__env, @__real.getAudioStream());
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void setInputToDefaultAudioDevice10(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setInputToDefaultAudioDevice();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void setInputToWaveFile11(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle path) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setInputToWaveFile(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, path));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void deactivateGrammar12(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.deactivateGrammar(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void activateGrammar13(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name, float weight) {
            // (Ljava/lang/String;F)V
            // (LSystem/String;F)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.activateGrammar(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name), weight);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void activateGrammar14(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.activateGrammar(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void loadDictationGrammar15(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.loadDictationGrammar(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void loadGrammarFromPath16(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name, global::net.sf.jni4net.utils.JniLocalHandle path) {
            // (Ljava/lang/String;Ljava/lang/String;)V
            // (LSystem/String;LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.loadGrammarFromPath(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name), global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, path));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void loadGrammarFromString17(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name, global::net.sf.jni4net.utils.JniLocalHandle grammar) {
            // (Ljava/lang/String;Ljava/lang/String;)V
            // (LSystem/String;LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.loadGrammarFromString(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name), global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, grammar));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void recognizeCancel18(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.recognizeCancel();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void setNoSpeechTimeout19(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, int msec) {
            // (I)V
            // (I)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setNoSpeechTimeout(msec);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void setEndSilTimeout20(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, int msec) {
            // (I)V
            // (I)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setEndSilTimeout(msec);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void setMaxSpeechTimeout21(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, int msec) {
            // (I)V
            // (I)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.setMaxSpeechTimeout(msec);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void recognize22(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedRecognizer>(@__env, @__obj);
            @__real.recognize();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void @__ctorManagedRecognizer0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__class, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedRecognizer @__real = new global::iristk.speech.windows.ManagedRecognizer();
            global::net.sf.jni4net.utils.Convertor.InitProxy(@__env, @__obj, @__real);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        new internal sealed class ContructionHelper : global::net.sf.jni4net.utils.IConstructionHelper {
            
            public global::net.sf.jni4net.jni.IJvmProxy CreateProxy(global::net.sf.jni4net.jni.JNIEnv @__env) {
                return new global::iristk.speech.windows.@__ManagedRecognizer(@__env);
            }
        }
    }
    #endregion
}