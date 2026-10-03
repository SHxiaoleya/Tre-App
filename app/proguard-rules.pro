# Proguard rules for TreA
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
    @androidx.room.Database *;
}
