# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.AndroidEntryPoint class *

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Kotlinx Serialisation
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.ghostkey.**$$serializer { *; }
-keepclassmembers class com.ghostkey.** {
    *** Companion;
}
-keepclasseswithmembers class com.ghostkey.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# MediaPipe
-keep class com.google.mediapipe.** { *; }

# Ktor
-keep class io.ktor.** { *; }
