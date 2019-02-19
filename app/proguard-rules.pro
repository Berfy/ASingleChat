#进行除算法指令,字段,类合并的所有优化;
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
#执行优化2次
-optimizationpasses 2
#优化时允许访问并修改有修饰符的类和类的成员.
-allowaccessmodification
-dontoptimize
-ignorewarnings
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-keepattributes *JavascriptInterface*,*Annotation*, Exceptions, Signature, Deprecated, SourceFile, SourceDir, LineNumberTable, LocalVariableTable, LocalVariableTypeTable, Synthetic, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault, InnerClasses,ProtoContract,ProtoMember

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keep public abstract interface com.asqw.android.Listener{
public protected <methods>;
}
# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#内部类
-keepclassmembers class * implements java.io.Serializable$* { *; }

-keep class **.R$* {
 *;
}

-keepclassmembers class * {
    void *(**On*Event);
}

#model
-keep class cn.zcgames.sdk.mqttsdk.contacts.model.** { *; }
-keep class cn.zcgames.sdk.mqttsdk.home.model.** { *; }
-keep class cn.zcgames.sdk.mqttsdk.message.model.** { *; }
-keep class cn.zcgames.sdk.mqttsdk.personal.model.** { *; }
-keep class cn.berfy.sdk.http.model.** { *; }
-keep class cn.berfy.service.im.model.** { *; }
-keep class cn.berfy.sdk.mvpbase.model.** { *; }
