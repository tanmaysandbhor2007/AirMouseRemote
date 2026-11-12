# Air Mouse & Universal TV Remote 📱🖱️📺

A modern Android app that turns your smartphone into a **3D motion-sensing Air Mouse** (like an LG Magic Remote or Wiimote) and **Universal TV Remote**, supporting both **PC** and **Smart TV**.

---

## 🌟 Key Features

1. **Dual Connectivity**:
   - **Primary: Bluetooth HID (`BluetoothHidDevice`)**: Emulates a native Bluetooth mouse and multimedia keyboard. **Zero software required** on the target PC, Mac, Linux, Android TV, Google TV, Apple TV, or Fire TV.
   - **Secondary: Wi-Fi (UDP Socket)**: High-speed, ultra-low latency fallback over local Wi-Fi with an instant companion script.

2. **Air Pointer Mode Toggle**:
   - **TV D-Pad Mode (Air Pointer OFF)**: Precision 5-way D-Pad (Up, Down, Left, Right, OK) plus essential TV controls (Home, Back, Menu, Volume +/-, Mute, Play/Pause). Ideal for streaming apps where free cursor movement is inconvenient.
   - **Air Pointer Mode (Air Pointer ON)**: 60Hz gyroscope motion pointing for desktop cursor navigation, presentation clicker usage, or TV freeform pointer.

3. **Motion Engine & UX**:
   - **Hand-Tremor Deadzone Filter**: Eliminates micro-shaking and cursor jitter when resting your hand.
   - **Dynamic Ballistic Acceleration**: Slow wrist motion provides pixel-perfect alignment; fast wrist flicks glide swiftly across multi-monitor setups.
   - **Clutch / Dead-man's Switch**: Support for both **Hold-Thumb-to-Aim** (cursor only moves while touching the screen, letting you reposition your hand freely) and **Toggle Lock**.
   - **Integrated Touchpad Controls**: Large primary Left Click trigger, secondary Right Click, and edge kinetic vertical scroll strip.

---

## 🚀 Getting Started

### 1. Install APK on Phone
The compiled debug APK is located at:
```
C:\Users\tanma\AirMouseRemote\app\build\outputs\apk\debug\app-debug.apk
```
To install via ADB:
```bash
adb install -r C:\Users\tanma\AirMouseRemote\app\build\outputs\apk\debug\app-debug.apk
```
*(Or transfer `app-debug.apk` directly to your phone via USB cable, Google Drive, or local file sharing, and tap to install).*

---

### 2. Using Primary Bluetooth Mode (Zero Setup on PC / TV)
1. Open the app on your phone.
2. Grant Bluetooth permissions when prompted.
3. On your **PC or Smart TV**, open Bluetooth Settings and select **"Add Device"**.
4. Look for **"Air Mouse & TV Remote"** in the list of available Bluetooth devices and click **Pair / Connect**.
5. You're ready!
   - Tap the prominent **Air Pointer Toggle** bar to activate 3D motion pointing.
   - Tap it again to switch to the TV D-Pad.

---

### 3. Using Wi-Fi UDP Mode (For PC without Bluetooth)
1. On your PC, launch the companion receiver (zero external dependencies required on Windows):
   ```bash
   python C:\Users\tanma\AirMouseRemote\companion\pc_receiver.py
   ```
2. The terminal will print your PC's local IP address (e.g., `192.168.1.15`).
3. In the Android app:
   - Tap the top-left pill to switch transport to **Wi-Fi (UDP)**.
   - Tap the **Settings (gear)** icon and enter your PC's IP address.
   - Tap **Save**!

---

## ⚙️ Sensor & Customization Settings

Tap the **Settings (gear)** icon in the app bar to adjust:
- **Gyro Sensitivity**: Adjust pointer speed from 0.5x to 3.0x.
- **Tremor Filter (Deadzone)**: Threshold to cancel small hand vibrations.
- **Hold Thumb to Aim (Clutch)**:
  - Enabled: Aim only while resting your thumb on the center touchpad surface (reposition your arm freely by lifting your thumb).
  - Disabled: Air Pointer remains active continuously while toggled ON.
- **Invert X / Invert Y**: Reverse cursor tracking directions if desired.
