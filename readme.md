# CMF Watch Google Maps Navigation Bridge 🧭⌚

A lightweight, privacy-focused Android companion app that brings turn-by-turn Google Maps navigation alerts directly to the **CMF Watch Pro 2** (and other RTOS-based wearables).

---

## 🌟 Why This Exists
The CMF Watch Pro 2 runs a proprietary RTOS that lacks native Google Maps support. Native Google Maps notifications mirrored directly to the watch suffer from:
- Truncated road names and broken layout formatting.
- Missing or malformed navigation arrows due to font rendering limitations on RTOS displays.
- Visual clutter from non-essential landmark descriptions.

**MapsToCMF** intercepts active Google Maps navigation broadcasts, formats instructions into glanceable badges (`[LEFT]`, `[RIGHT]`, `[ROUNDABOUT 2]`, `[STRAIGHT]`), and re-dispatches clean notifications designed specifically for the watch screen.

---

## 🔒 Privacy & Security Guarantee
- **No Internet Access:** This app does **NOT** request the `android.permission.INTERNET` permission. Android physically prevents the app from opening network sockets or transmitting any data off your device.
- **Strict Package Filtering:** Only notifications originating strictly from `com.google.android.apps.maps` are processed. Sensitive notifications (SMS, OTPs, banking alerts, personal chats) are immediately ignored.
- **100% Offline & Open Source:** All parsing runs locally on your device.

---

## 🚀 Installation & Setup

1. **Download the APK:** Grab `MapsToCMF.apk` from the [Releases](https://github.com/deveshtanwar/cmf-watch-google-maps-navigation-support/releases) tab.
2. **Install on Phone:** Open the downloaded APK and install it (allow *Install unknown apps* if prompted).
3. **Grant Notification Access:** Open the app and tap **Check Notification Access**, then enable access for **Maps to CMF**.
4. **Enable in CMF Watch App:**
    - Open the **CMF Watch** app on your phone.
    - Go to **Device** $\rightarrow$ **Notifications**.
    - Enable notifications for **Maps to CMF**.
5. **Navigate:** Start navigation in Google Maps; direction badges will appear on your wrist.

---

## 🛠️ Built With
- Kotlin
- Android `NotificationListenerService`
- Android `Notification.BigTextStyle`