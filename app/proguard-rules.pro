# ProGuard rules for Chand App
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.chand.app.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
