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
public class Voice extends system.Object {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected Voice(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("(LSystem/String;LSystem/String;LSystem/String;)V")
    public Voice(java.lang.String name, java.lang.String lang, java.lang.String gender) {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        iristk.speech.windows.Voice.__ctorVoice0(this, name, lang, gender);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")
    private native static void __ctorVoice0(net.sf.jni4net.inj.IClrProxy thiz, java.lang.String name, java.lang.String lang, java.lang.String gender);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String getName();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String getLang();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String getGender();
    
    public static system.Type typeof() {
        return iristk.speech.windows.Voice.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        iristk.speech.windows.Voice.staticType = staticType;
    }
    //</generated-proxy>
}