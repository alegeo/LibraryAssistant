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
    public partial class Phoneme_ {
        
        public static global::java.lang.Class _class {
            get {
                return global::iristk.speech.windows.@__Phoneme.staticClass;
            }
        }
    }
    #endregion
    
    #region Component Designer generated code 
    [global::net.sf.jni4net.attributes.JavaProxyAttribute(typeof(global::iristk.speech.windows.Phoneme), typeof(global::iristk.speech.windows.Phoneme_))]
    [global::net.sf.jni4net.attributes.ClrWrapperAttribute(typeof(global::iristk.speech.windows.Phoneme), typeof(global::iristk.speech.windows.Phoneme_))]
    internal sealed partial class @__Phoneme : global::java.lang.Object {
        
        internal new static global::java.lang.Class staticClass;
        
        private @__Phoneme(global::net.sf.jni4net.jni.JNIEnv @__env) : 
                base(@__env) {
        }
        
        private static void InitJNI(global::net.sf.jni4net.jni.JNIEnv @__env, java.lang.Class @__class) {
            global::iristk.speech.windows.@__Phoneme.staticClass = @__class;
        }
        
        private static global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> @__Init(global::net.sf.jni4net.jni.JNIEnv @__env, global::java.lang.Class @__class) {
            global::System.Type @__type = typeof(__Phoneme);
            global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> methods = new global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod>();
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getDuration", "getDuration0", "()F"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getLabel", "getLabel1", "()Ljava/lang/String;"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "__ctorPhoneme0", "__ctorPhoneme0", "(Lnet/sf/jni4net/inj/IClrProxy;)V"));
            return methods;
        }
        
        private static float getDuration0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()F
            // ()F
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            float @__return = default(float);
            try {
            global::iristk.speech.windows.Phoneme @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.Phoneme>(@__env, @__obj);
            @__return = ((float)(@__real.getDuration()));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static global::net.sf.jni4net.utils.JniHandle getLabel1(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()Ljava/lang/String;
            // ()LSystem/String;
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            global::net.sf.jni4net.utils.JniHandle @__return = default(global::net.sf.jni4net.utils.JniHandle);
            try {
            global::iristk.speech.windows.Phoneme @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::iristk.speech.windows.Phoneme>(@__env, @__obj);
            @__return = global::net.sf.jni4net.utils.Convertor.StrongC2JString(@__env, @__real.getLabel());
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void @__ctorPhoneme0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__class, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::iristk.speech.windows.Phoneme @__real = new global::iristk.speech.windows.Phoneme();
            global::net.sf.jni4net.utils.Convertor.InitProxy(@__env, @__obj, @__real);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        new internal sealed class ContructionHelper : global::net.sf.jni4net.utils.IConstructionHelper {
            
            public global::net.sf.jni4net.jni.IJvmProxy CreateProxy(global::net.sf.jni4net.jni.JNIEnv @__env) {
                return new global::iristk.speech.windows.@__Phoneme(@__env);
            }
        }
    }
    #endregion
}
