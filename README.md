# NextUp — Android Countdown & Jetpack Glance Widget App

NextUp is a modern Android application built with **Kotlin 2.0**, **Jetpack Compose (Material 3 Dynamic Color / Material You)**, **Jetpack Glance AppWidgets**, **Room Database**, **Hilt**, **WorkManager**, and **AlarmManager**.

## Features Included
- **Dashboard & Live Ticker**: Displays active countdowns ordered by nearest date (`DAYS : HOURS : MINS`) with custom card colors or gallery background photos.
- **Pre-loaded Indian Templates**: One-tap templates for **IRCTC AC Tatkal (10:00 AM)**, **IRCTC Non-AC Tatkal (11:00 AM)**, **UPSC CSE Prelims**, **ITR Filing Deadline**, **Diwali**, and **GATE**.
- **Material You Home Screen Widget (Jetpack Glance)**: Supports custom background images, direct in-app widget pinning (`WidgetPinHelper`), and an adaptive battery-saving alarm schedule (hourly updates when >24h away, 1-minute updates when <24h away).
- **Milestone Alerts & Zero-Hour Action Trigger**: Schedules exact local notifications at 7 days, 24 hours, 1 hour, and 00:00:00, and reveals a one-tap launcher button when a countdown hits zero (opening IRCTC Rail Connect `cris.org.in.prs.ima` or the target web portal).

---

## How to Build & Install the APK (`app-debug.apk`)

### Option 1: Open in Android Studio (Recommended for Local Device Testing)
1. Extract `NextUp_Android_Project.zip`.
2. Open **Android Studio Ladybug (2024.2.1)** or newer and select **Open** -> choose the extracted `NextUp` folder.
3. Let Android Studio sync Gradle and download the Android SDK 35 platform dependencies.
4. Connect your Android phone (with **USB Debugging** or **Wireless Debugging** enabled) and click **Run ('app')**, or build the installable APK from the top menu via **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
   - Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Build via Command Line (macOS / Linux / Windows)
If you have the Android SDK (`ANDROID_HOME`) and Gradle 8.9+ installed:
```bash
gradle assembleDebug
```
Then install onto a connected device via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Zero-Setup Cloud APK Build via GitHub Actions (No Local Android Studio Needed)
This project includes a pre-configured GitHub Actions workflow at `.github/workflows/build-apk.yml`:
1. Create a new repository on GitHub and push the extracted `NextUp` folder.
2. Go to the **Actions** tab in your GitHub repository and select **Build NextUp Installable APK**.
3. Once the workflow completes (~2 minutes), download the **`NextUp-debug-apk`** artifact (`app-debug.apk`) directly to your phone and tap to install!
