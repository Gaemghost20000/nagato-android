# app/build.gradle Changes Required

## Add Dependencies

In the existing `dependencies {}` block, add:

```gradle
dependencies {
    // EXISTING:
    implementation "androidx.annotation:annotation:1.9.0"
    implementation "androidx.core:core:1.13.1"
    implementation "androidx.drawerlayout:drawerlayout:1.2.0"
    implementation "androidx.preference:preference:1.2.1"
    implementation "androidx.viewpager:viewpager:1.0.0"
    implementation "com.google.android.material:material:1.12.0"
    
    // NEW — TabLayout + ViewPager2:
    implementation "androidx.viewpager2:viewpager2:1.1.0"
    // material:1.12.0 already includes TabLayout, no extra dep needed
    
    // EXISTING: ... rest unchanged
}
```

## Change Application ID

```gradle
android {
    defaultConfig {
        // OLD:
        applicationId "com.termux"
        
        // NEW:
        applicationId "com.nagato.agent"
        
        // Keep everything else same
        minSdkVersion project.properties.minSdkVersion.toInteger()
        targetSdkVersion project.properties.targetSdkVersion.toInteger()
        // etc.
    }
}
```

## Bootstrap Handling

In the `downloadBootstraps` task area (~line 218+):

```gradle
// OPTION A: Comment out download, place local zip in cpp/
task downloadBootstraps() {
    return; // DISABLED: Using local bootstrap
    /*
    doLast {
        // ... original download code ...
    }
    */
}
```

Make sure `bootstrap-aarch64.zip` is placed at:
```
app/src/main/cpp/bootstrap-aarch64.zip
```
