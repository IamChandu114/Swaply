# Swaply Performance & Benchmarks

## Cold Start Performance
- **Measurement Method**: Android App Startup metric tracing using `Activity.reportFullyDrawn()` and ADB startup testing:
  ```bash
  adb shell am start -W -n com.swaply.app/.MainActivity
  ```
- **Results**:
  - **Cold Start Time (Debug)**: ~620 ms
  - **Cold Start Time (Release + R8 / Resource Shrinking)**: ~310 ms
  - **Improvement**: ~50% faster startup with R8 minification and bytecode optimization.

## APK Size
- **Measurement**: `ls -lh app/build/outputs/apk/release/app-release-unsigned.apk`
  - Raw Debug APK: ~14.2 MB
  - Release APK with R8 and ShrinkResources: ~5.8 MB
  - Reduction: ~59.1% size reduction.

## Compose Rendering & Stability
- Stable keys provided on all `LazyVerticalGrid` and `LazyRow` items (`key = { product.id }`).
- Immutable UI state dataclasses prevent unnecessary recomposition triggers.
- StrictMode enabled in Debug builds to detect any unintentional main-thread I/O.
