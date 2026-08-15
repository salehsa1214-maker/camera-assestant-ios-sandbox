# --- SSHJ + BouncyCastle (reflection-heavy: JCE provider registration, algorithm lookup) ---
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.crypto.** { *; }
-dontwarn org.bouncycastle.**
-keep class net.schmizz.** { *; }
-keep class com.hierynomus.** { *; }
-dontwarn net.schmizz.**
-dontwarn com.hierynomus.**

# SLF4J -> Android bridge is resolved reflectively.
-keep class org.slf4j.** { *; }
-keep class uk.uuid.slf4j.** { *; }
-dontwarn org.slf4j.**

# --- MediaPipe Tasks Vision (JNI callbacks into Java classes by name) ---
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**
# Protobuf lite used by MediaPipe.
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# --- LiteRT / TensorFlow Lite (MobileCLIP2-S0 semantic expert, JNI) ---
-keep class org.tensorflow.** { *; }
-keep class com.google.ai.edge.litert.** { *; }
-dontwarn org.tensorflow.**

# --- ML Kit face detection ships consumer rules; silence transitive warnings only ---
-dontwarn com.google.mlkit.**

# --- kotlinx-serialization: keep generated serializers for app model classes ---
-keepclassmembers @kotlinx.serialization.Serializable class app.dyrecto.** {
    static **$Companion Companion;
}
-keepclasseswithmembers class app.dyrecto.**$$serializer { *; }
-keepclassmembers class app.dyrecto.** {
    *** Companion;
}

# Compile-time-only references: annotation-processor classes shaded inside MediaPipe's
# autovalue jar, and eddsa's optional sun.security probe. Never used at runtime on Android.
-dontwarn javax.lang.model.**
-dontwarn sun.security.x509.X509Key

# Desugar/javax noise from sshj's optional deps.
-dontwarn javax.el.**
-dontwarn org.ietf.jgss.**
-dontwarn java.awt.**
