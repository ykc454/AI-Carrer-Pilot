# Project-specific R8 rules.
#
# IMPORTANT: avoid blanket "-keep class some.library.** { *; }" rules.
# Every kept class is excluded from shrinking, obfuscation and optimisation,
# which is what Play Console's "DEX code optimisation" score measures.
# Firebase, Room, Hilt/Dagger, Coroutines, Gson, Retrofit and OkHttp all ship
# their own consumer R8 rules inside their AARs/JARs, so nothing is needed here.

# Keep line numbers so Play Console / Crashlytics stack traces are readable
# (use the mapping.txt uploaded with the bundle to de-obfuscate).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------- Gson models (fields are read reflectively) ----------
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.nextgendevs.aicareerpilot.data.model.news.** { <fields>; <init>(...); }

# ---------- Retrofit service interfaces ----------
-keep,allowobfuscation interface com.nextgendevs.aicareerpilot.data.remote.** { *; }

# ---------- PDFBox-Android optional deps ----------
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn java.awt.Shape
