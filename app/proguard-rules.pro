# Proguard rules for Gemini Chat Studio
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepclassmembers class * {
    @org.jetbrains.annotations.* <fields>;
    @org.jetbrains.annotations.* <methods>;
}
-dontwarn com.google.ai.client.generativeai.**
