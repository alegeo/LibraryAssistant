// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------

package iristk.speech.windows;

@net.sf.jni4net.attributes.ClrType
public class Voices extends system.Object {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected Voices(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("()V")
    public Voices() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        iristk.speech.windows.Voices.__ctorVoices0(this);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    private native static void __ctorVoices0(net.sf.jni4net.inj.IClrProxy thiz);
    
    @net.sf.jni4net.attributes.ClrMethod("(I)Liristk/speech/windows/Voice;")
    public native iristk.speech.windows.Voice getVoice(int index);
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int getLength();
    
    public static system.Type typeof() {
        return iristk.speech.windows.Voices.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        iristk.speech.windows.Voices.staticType = staticType;
    }
    //</generated-proxy>
}
