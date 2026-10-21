# CMF Watch Google Maps Navigation Bridge 🧭⌚

A lightweight, privacy-first Android companion app that brings turn-by-turn Google Maps navigation alerts to **CMF Watch Pro, Watch Pro 2, and Watch Pro 3** (as well as other RTOS-based wearables).

---

## 🧠 Why This Exists & What is RTOS?

Most mainstream smartwatches from Nothing/CMF run an **RTOS (Real-Time Operating System)** rather than a full operating system like Google’s Wear OS or Apple’s watchOS:

* **What is RTOS?** A Real-Time Operating System is an ultra-lightweight OS engineered to execute a fixed set of tasks with deterministic timing using minimal CPU cycles and memory (often under 64 MB of RAM). This architecture is the exact reason CMF watches deliver 10–14 days of battery life compared to 1–2 days on Wear OS.
* **The Trade-Off:** Because RTOS firmware is closed and stripped down, devices like the **CMF Watch Pro, Watch Pro 2, and Watch Pro 3** do not have an app store, cannot run a native Google Maps client, and have no direct navigation SDK support.
* **The Notification Problem:** When Google Maps mirrors navigation alerts over Bluetooth Low Energy (BLE) to an RTOS watch:
   - Long road names and verbose landmark descriptions push trip metrics off the screen.
   - Unicode middle dots (`·`) and special symbols often trigger formatting errors or show as unsupported character boxes (`□□`).
   - Google Maps frequently swaps title and text fields, creating confusing notifications on lightweight watch displays.

**MapsToCMF** acts as a lightweight translation bridge on your Android phone, converting raw navigation broadcasts into glanceable, RTOS-friendly text badges.

---

## 🚀 Key Features

* **Glanceable Enum Badges:** Converts maneuvers into prominent text badges (`[LEFT]`, `[RIGHT]`, `[STRAIGHT]`, `[ROUNDABOUT 2]`, `[U-TURN]`) that display clearly without relying on unsupported font glyphs.
* **Separation of Concerns:** Reserves the watch notification title strictly for `[DIRECTION] + DISTANCE` (e.g., `[RIGHT] 100 m`) and keeps the street name and ETA in the body.
* **Smart Landmark Filtering:** Strips out orientation noise (such as `(on the left)` or `pass by...`) so landmark cues never invert or mislabel turn directions.
* **No Truncation:** Pushes full instruction text and ETA lines using Android's `BigTextStyle`.

---

## 🔒 Privacy & Security Guarantee

Because this application uses Android's `NotificationListenerService`, transparency is paramount:

* **Zero Internet Permissions:** This app completely omits the `android.permission.INTERNET` flag in its manifest. Android physically blocks the app from creating network sockets, communicating with servers, or sending data off your device.
* **Strict Package Filtering:** Every incoming notification is inspected for its package origin. If it does not strictly match `com.google.android.apps.maps`, it is instantly dropped without reading, storing, or caching. Personal messages, bank alerts, and SMS OTPs are completely ignored.
* **100% Offline & Open Source:** The entire pipeline executes locally on your device. You can verify and compile the source code directly from this repository.

---

## 📱 Compatibility

* **CMF Watch Pro**
* **CMF Watch Pro 2**
* **CMF Watch Pro 3**
* Any RTOS-based wearable running companion notification mirroring on Android (Android 8.0 Oreo through Android 15+).

---

## 🛠️ Installation & Setup

1. **Download the APK:** Download `MapsToCMF.apk` from the [Latest Release](https://github.com/deveshtanwar/cmf-watch-google-maps-navigation-support/releases).
2. **Install on Android:** Open the file on your device and tap Install (allow *Install unknown apps* if prompted).
3. **Grant Notification Access:** Open **MapsToCMF** and tap **Check Notification Access**, then switch the toggle to **ON** for *Maps to CMF*.
4. **Enable in Companion App:**
   - Open your official **CMF Watch** app.
   - Go to **Device** $\rightarrow$ **Notifications**.
   - Enable notifications for **Maps to CMF**.
5. **Start Navigation:** Open Google Maps and begin a route. Turn badges will automatically appear on your wrist.

---

## 💻 Tech Stack

* **Language:** Kotlin
* **Framework:** Android SDK (API 26+)
* **Core APIs:** `NotificationListenerService`, `NotificationManager`, `Notification.BigTextStyle`

---

## ⌚ Visual representation

```
╭────────────────────────────────────────╮
│  Navigation                            │
│                                        │
│  [RIGHT] 100 m                         │
│  Turn right onto Grand Avenue          │
│  [12 min | 3.5 km | 5:30 PM]           │
╰────────────────────────────────────────╯
```

---

## ⚠️ Disclaimer of Liability

This project is an independent, open-source community tool and is **not** affiliated with, endorsed by, or sponsored by Nothing, CMF by Nothing, or Google LLC.

By downloading, installing, or modifying this software, you acknowledge and agree that it is provided "as-is" and without warranties of any kind, express or implied. You are voluntarily installing this application at your own discretion and risk. The developer(s) assume no liability or responsibility for any direct, indirect, incidental, or consequential damages, including but not limited to device malfunction, data loss, battery degradation, or operating system instability on your smartphone or smartwatch resulting from the use or inability to use this software.

Please review the source code provided in this repository before installation to ensure your own security and compliance requirements are met.