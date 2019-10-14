# JWPlayer
-keepattributes *longtailvideo*
-keep class com.longtailvideo.**{ *; }
-dontwarn com.longtailvideo.*
-keepclasseswithmembernames class * {native <methods>;}

-keep public class com.applicaster.jwplayerplugin.JWPlayerAdapter {
   public <fields>;
   public <methods>;
   protected <methods>;
}
-keep public class com.applicaster.jwplayerplugin.JWPlayerContainer {
   public <fields>;
   public <methods>;
}

-keepclassmembers class com.longtailvideo.jwplayer.** {
    @android.webkit.JavascriptInterface *;
}

# Block warnings about missing module classes
-dontwarn com.longtailvideo.jwplayer.**
-dontwarn com.google.ads.interactivemedia.**

# Classes get rejected without this when running the app if the app has been run through ProGuard
-keepattributes InnerClasses,EnclosingMethod

# Keep module indicator classes
-keep class com.longtailvideo.jwplayer.modules.** { *; }