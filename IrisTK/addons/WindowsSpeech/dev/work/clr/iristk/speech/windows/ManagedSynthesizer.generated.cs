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
    public partial class ManagedSynthesizer_ {
        
        public static global::java.lang.Class _class {
            get {
                return global::iristk.speech.windows.@__ManagedSynthesizer.staticClass;
            }
        }
    }
    #endregion
    
    #region Component Designer generated code 
    [global::net.sf.jni4net.attributes.JavaProxyAttribute(typeof(global::iristk.speech.windows.ManagedSynthesizer), typeof(global::iristk.speech.windows.ManagedSynthesizer_))]
    [global::net.sf.jni4net.attributes.ClrWrapperAttribute(typeof(global::iristk.speech.windows.ManagedSynthesizer), typeof(global::iristk.speech.windows.ManagedSynthesizer_))]
    internal sealed partial class @__ManagedSynthesizer : global::java.lang.Object {
        
        internal new static global::java.lang.Class staticClass;
        
        private @__ManagedSynthesizer(global::net.sf.jni4net.jni.JNIEnv @__env) : 
                base(@__env) {
        }
        
        private static void InitJNI(global::net.sf.jni4net.jni.JNIEnv @__env, java.lang.Class @__class) {
            global::iristk.speech.windows.@__ManagedSynthesizer.staticClass = @__class;
        }
        
        private static global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> @__Init(global::net.sf.jni4net.jni.JNIEnv @__env, global::java.lang.Class @__class) {
            global::System.Type @__type = typeof(__ManagedSynthesizer);
            global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> methods = new global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod>();
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setVoice", "setVoice0", "(Ljava/lang/String;)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "synthesize", "synthesize1", "(Ljava/lang/String;Ljava/lang/String;)Liristk/speech/windows/Phonemes;"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "transcribe", "transcribe2", "(Ljava/lang/String;)Liristk/speech/windows/Phonemes;"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getVoices", "getVoices3", "()Liristk/speech/windows/Voices;"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "printVoices", "printVoices4", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "__ctorManagedSynthesizer0", "__ctorManagedSynthesizer0", "(Lnet/sf/jni4net/inj/IClrProxy;)V"));
            return methods;
        }
        
        private static void setVoice0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle name) {
            // (Ljava/lang/String;)V
            // (LSystem/String;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedSynthesizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedSynthesizer>(@__env, @__obj);
            @__real.setVoice(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, name));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static global::net.sf.jni4net.utils.JniHandle synthesize1(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle ssml, global::net.sf.jni4net.utils.JniLocalHandle path) {
            // (Ljava/lang/String;Ljava/lang/String;)Liristk/speech/windows/Phonemes;
            // (LSystem/String;LSystem/String;)Liristk/speech/windows/Phonemes;
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            global::net.sf.jni4net.utils.JniHandle @__return = default(global::net.sf.jni4net.utils.JniHandle);
            try {
            global::iristk.speech.windows.ManagedSynthesizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedSynthesizer>(@__env, @__obj);
            @__return = global::net.sf.jni4net.utils.Convertor.StrongC2Jp<global::iristk.speech.windows.Phonemes>(@__env, @__real.synthesize(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, ssml), global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, path)));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static global::net.sf.jni4net.utils.JniHandle transcribe2(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle ssml) {
            // (Ljava/lang/String;)Liristk/speech/windows/Phonemes;
            // (LSystem/String;)Liristk/speech/windows/Phonemes;
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            global::net.sf.jni4net.utils.JniHandle @__return = default(global::net.sf.jni4net.utils.JniHandle);
            try {
            global::iristk.speech.windows.ManagedSynthesizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedSynthesizer>(@__env, @__obj);
            @__return = global::net.sf.jni4net.utils.Convertor.StrongC2Jp<global::iristk.speech.windows.Phonemes>(@__env, @__real.transcribe(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, ssml)));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static global::net.sf.jni4net.utils.JniHandle getVoices3(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()Liristk/speech/windows/Voices;
            // ()Liristk/speech/windows/Voices;
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            global::net.sf.jni4net.utils.JniHandle @__return = default(global::net.sf.jni4net.utils.JniHandle);
            try {
            global::iristk.speech.windows.ManagedSynthesizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedSynthesizer>(@__env, @__obj);
            @__return = global::net.sf.jni4net.utils.Convertor.StrongC2Jp<global::iristk.speech.windows.Voices>(@__env, @__real.getVoices());
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void printVoices4(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedSynthesizer @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.ManagedSynthesizer>(@__env, @__obj);
            @__real.printVoices();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void @__ctorManagedSynthesizer0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__class, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.ManagedSynthesizer @__real = new global::iristk.speech.windows.ManagedSynthesizer();
            global::net.sf.jni4net.utils.Convertor.InitProxy(@__env, @__obj, @__real);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        new internal sealed class ContructionHelper : global::net.sf.jni4net.utils.IConstructionHelper {
            
            public global::net.sf.jni4net.jni.IJvmProxy CreateProxy(global::net.sf.jni4net.jni.JNIEnv @__env) {
                return new global::iristk.speech.windows.@__ManagedSynthesizer(@__env);
            }
        }
    }
    #endregion
}