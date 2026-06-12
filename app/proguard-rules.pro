# ProGuard / R8 rules for Authenticator

# Hilt
-keepclassmembers class * {
    @dagger.hilt.android.HiltAndroidApp <methods>;
}
-keep class dagger.hilt.android.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManagerHolder { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { <fields>; }
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-dontwarn androidx.room.paging.**

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <fields>; }

# CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
-dontwarn androidx.camera.**

# ML Kit Barcode
-keep class com.google.mlkit.vision.barcode.** { *; }
-keep class com.google.mlkit.vision.common.** { *; }
-dontwarn com.google.mlkit.**

# Biometric
-keep class androidx.biometric.** { *; }

# Kotlin coroutines / serialization
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.atomicfu.**

# Keep serialization constructors for R8 full mode
-keepattributes *Annotation*, Signature, Exception, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations

# Keep all project classes. This app uses Hilt, Room, DataStore and Compose StateFlow
# contracts that are referenced by generated code and reflection at runtime.
# Using a broad keep rule avoids having to manually update ProGuard every time a new
# feature module, ViewModel or contract class is added.
-keep class com.hgr.authenticator.** { *; }
