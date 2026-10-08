# LifeRhythm

*Understand your day. Understand yourself.*

LifeRhythm is a private, offline Android app that estimates when you probably slept, using only the pattern of when you use your phone.

> **Honest by design.** Phone inactivity does not prove sleep. Every number in the app is an estimate, shown with a confidence level, and is never a medical measurement.

## How it works

1. Reads app open/close and unlock events from Android's **Usage Access** (UsageStatsManager). Notification-only screen wake-ups are ignored.
2. Groups them into phone sessions and finds the quiet stretches between them.
3. The longest quiet stretch inside your night window (default 9 PM to 9 AM) is the estimated sleep.
4. A brief pickup in the middle of the night (checking the time) is logged as an **awakening** and does not end the sleep.
5. A background job (WorkManager, every ~6 hours) saves each night on the device, because Android only keeps a few days of usage history.

## Privacy

- No internet permission. Nothing leaves your phone.
- Only permission: Usage Access, granted by you in Android Settings.
- No ads, no analytics, no accounts. Export or delete your data from Settings.

## Install on your phone

1. Push this project to your GitHub repo. The **Build APK** workflow runs automatically.
2. When it finishes, open the repo's **Releases** page on your phone and download app-debug.apk.
   (Alternatively: **Actions** tab, latest run, **Artifacts**, **LifeRhythm-debug-apk**.)
3. Open the file and allow "Install unknown apps" when asked.
4. Open LifeRhythm, then tap **Open Usage Access settings** and switch LifeRhythm on.
5. Recent nights are calculated right away from the last few days of phone data.

## Build locally

JDK 17 and Gradle 8.7+: gradle assembleDebug

## Roadmap

- Manual correction ("Is this right?") and Room database
- Health Connect steps and activity
- Trends, "You vs You", observed patterns
