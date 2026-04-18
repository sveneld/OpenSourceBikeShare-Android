# Add project specific ProGuard rules here.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Moshi
-keep class com.bikeshare.app.data.api.dto.** { *; }
-keepclassmembers class com.bikeshare.app.data.api.dto.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep data classes for serialization
-keep class com.bikeshare.app.domain.model.** { *; }

# osmdroid
-dontwarn org.osmdroid.**

# EncryptedSharedPreferences / Security Crypto
-keep class androidx.security.crypto.** { *; }

# Sentry
-dontwarn io.sentry.**

# ML Kit + Google Code Scanner (reflection-based component registrars)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-keep class com.google.android.gms.vision.** { *; }
-keep class com.google.firebase.components.** { *; }
-keepclassmembers class com.google.mlkit.** {
    <init>(...);
}
