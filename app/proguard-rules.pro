# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.google.gson.** { *; }

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room
-keep class androidx.room.** { *; }

# Retrofit
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.gemalto.jp2.JP2Decoder

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.awt.Shape

# ---------- Apache POI ----------
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# ---------- XMLBeans ----------
-keep class org.apache.xmlbeans.** { *; }
-dontwarn org.apache.xmlbeans.**

# ---------- OOXML Schemas ----------
-keep class org.openxmlformats.schemas.** { *; }
-dontwarn org.openxmlformats.schemas.**

-keep class schemaorg_apache_xmlbeans.** { *; }
-dontwarn schemaorg_apache_xmlbeans.**

# ---------- Commons Compress ----------
-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**

# ---------- Commons Collections ----------
-keep class org.apache.commons.collections4.** { *; }
-dontwarn org.apache.commons.collections4.**

# ---------- Commons IO ----------
-keep class org.apache.commons.io.** { *; }
-dontwarn org.apache.commons.io.**

# ---------- Retrofit ----------
-keep interface com.nextgendevs.aicareerpilot.data.remote.** { *; }

# ---------- News Models ----------
-keep class com.nextgendevs.aicareerpilot.data.model.news.** { *; }

-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ---------- Log4j ----------
-keep class org.apache.logging.log4j.** { *; }
-dontwarn org.apache.logging.log4j.**

-keep class org.apache.logging.log4j.spi.** { *; }
-keep class org.apache.logging.log4j.message.** { *; }

# Keep ServiceLoader metadata used by Log4j
-keepattributes *Annotation*
-keepnames class org.apache.logging.log4j.**