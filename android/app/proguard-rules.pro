# Keep Retrofit service methods used by dynamic proxies.
-keep interface pg.marketpg.app.MarketService { *; }

# Preserve Gson-annotated DTO fields when R8 optimizes the release APK.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve generic signatures and annotations required by Retrofit and Gson.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.stream.** { *; }

# Gson serializes Kotlin DTOs reflectively. Preserve the original backing-field
# names for every app model so release/R8 builds still send JSON keys such as
# "email", "password", and diagnostic "report" (not obfuscated names).
-keepclassmembers class pg.marketpg.app.** {
    <fields>;
}
