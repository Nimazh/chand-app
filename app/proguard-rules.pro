# ProGuard rules for Chand App
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.chand.app.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Crashlytics references this optional Android 15 class behind an API-level guard.
# API 34 compile stubs do not contain it; R8 otherwise rejects the release build.
-dontwarn android.os.ProfilingResult
