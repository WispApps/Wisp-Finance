# Wisp Finance release hardening.
# Room and the encrypted backup format persist these model names and fields.
-keep class com.wisp.finance.data.local.entity.** { *; }
-keep enum com.wisp.finance.domain.model.** { *; }
-keep class com.wisp.finance.security.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# OkHttp may reference optional platform classes that are not part of the APK.
-dontwarn okhttp3.**
-dontwarn okio.**
