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
public class Phoneme extends system.Object {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected Phoneme(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("()V")
    public Phoneme() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        iristk.speech.windows.Phoneme.__ctorPhoneme0(this);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    private native static void __ctorPhoneme0(net.sf.jni4net.inj.IClrProxy thiz);
    
    @net.sf.jni4net.attributes.ClrMethod("()F")
    public native float getDuration();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String getLabel();
    
    public static system.Type typeof() {
        return iristk.speech.windows.Phoneme.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        iristk.speech.windows.Phoneme.staticType = staticType;
    }
    //</generated-proxy>
}