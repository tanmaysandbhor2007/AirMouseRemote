package com.airmouse.remote.transport.bluetooth

import com.airmouse.remote.model.MouseButton
import com.airmouse.remote.model.TVKey

object HidReportDescriptor {
    const val REPORT_ID_MOUSE: Byte = 1
    const val REPORT_ID_CONSUMER: Byte = 2
    const val REPORT_ID_KEYBOARD: Byte = 3

    // Composite HID Report Descriptor combining Mouse, Consumer Control, and Keyboard
    val DESCRIPTOR = byteArrayOf(
        // ============================================
        // 1. MOUSE (Report ID 1)
        // ============================================
        0x05.toByte(), 0x01.toByte(),       // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x02.toByte(),       // USAGE (Mouse)
        0xA1.toByte(), 0x01.toByte(),       // COLLECTION (Application)
        0x85.toByte(), REPORT_ID_MOUSE,     //   REPORT_ID (1)
        0x09.toByte(), 0x01.toByte(),       //   USAGE (Pointer)
        0xA1.toByte(), 0x00.toByte(),       //   COLLECTION (Physical)
        // Buttons (Left, Right, Middle)
        0x05.toByte(), 0x09.toByte(),       //     USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(),       //     USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x03.toByte(),       //     USAGE_MAXIMUM (Button 3)
        0x15.toByte(), 0x00.toByte(),       //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),       //     LOGICAL_MAXIMUM (1)
        0x95.toByte(), 0x03.toByte(),       //     REPORT_COUNT (3)
        0x75.toByte(), 0x01.toByte(),       //     REPORT_SIZE (1)
        0x81.toByte(), 0x02.toByte(),       //     INPUT (Data,Var,Abs)
        // Padding (5 bits)
        0x95.toByte(), 0x01.toByte(),       //     REPORT_COUNT (1)
        0x75.toByte(), 0x05.toByte(),       //     REPORT_SIZE (5)
        0x81.toByte(), 0x03.toByte(),       //     INPUT (Cnst,Var,Abs)
        // X, Y Delta (-127 to 127)
        0x05.toByte(), 0x01.toByte(),       //     USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(),       //     USAGE (X)
        0x09.toByte(), 0x31.toByte(),       //     USAGE (Y)
        0x15.toByte(), 0x81.toByte(),       //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7F.toByte(),       //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(),       //     REPORT_SIZE (8)
        0x95.toByte(), 0x02.toByte(),       //     REPORT_COUNT (2)
        0x81.toByte(), 0x06.toByte(),       //     INPUT (Data,Var,Rel)
        // Wheel Delta (-127 to 127)
        0x09.toByte(), 0x38.toByte(),       //     USAGE (Wheel)
        0x15.toByte(), 0x81.toByte(),       //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7F.toByte(),       //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(),       //     REPORT_SIZE (8)
        0x95.toByte(), 0x01.toByte(),       //     REPORT_COUNT (1)
        0x81.toByte(), 0x06.toByte(),       //     INPUT (Data,Var,Rel)
        0xC0.toByte(),                      //   END_COLLECTION
        0xC0.toByte(),                      // END_COLLECTION

        // ============================================
        // 2. CONSUMER CONTROL / TV MEDIA (Report ID 2)
        // ============================================
        0x05.toByte(), 0x0C.toByte(),       // USAGE_PAGE (Consumer Devices)
        0x09.toByte(), 0x01.toByte(),       // USAGE (Consumer Control)
        0xA1.toByte(), 0x01.toByte(),       // COLLECTION (Application)
        0x85.toByte(), REPORT_ID_CONSUMER,  //   REPORT_ID (2)
        0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),       //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(),       //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(),       //   REPORT_COUNT (8)
        0x09.toByte(), 0xE9.toByte(),       //   USAGE (Volume Increment) -> bit 0
        0x09.toByte(), 0xEA.toByte(),       //   USAGE (Volume Decrement) -> bit 1
        0x09.toByte(), 0xE2.toByte(),       //   USAGE (Mute)               -> bit 2
        0x09.toByte(), 0xCD.toByte(),       //   USAGE (Play/Pause)          -> bit 3
        0x0A.toByte(), 0x23.toByte(), 0x02.toByte(), // USAGE (Home)        -> bit 4
        0x09.toByte(), 0x40.toByte(),       //   USAGE (Menu)                -> bit 5
        0x09.toByte(), 0xB5.toByte(),       //   USAGE (Scan Next Track)     -> bit 6
        0x09.toByte(), 0xB6.toByte(),       //   USAGE (Scan Previous Track) -> bit 7
        0x81.toByte(), 0x02.toByte(),       //   INPUT (Data,Var,Abs)
        0xC0.toByte(),                      // END_COLLECTION

        // ============================================
        // 3. KEYBOARD / TV D-PAD (Report ID 3)
        // ============================================
        0x05.toByte(), 0x01.toByte(),       // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x06.toByte(),       // USAGE (Keyboard)
        0xA1.toByte(), 0x01.toByte(),       // COLLECTION (Application)
        0x85.toByte(), REPORT_ID_KEYBOARD,  //   REPORT_ID (3)
        // Modifiers (8 bits)
        0x05.toByte(), 0x07.toByte(),       //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0xE0.toByte(),       //   USAGE_MINIMUM (Keyboard LeftControl)
        0x29.toByte(), 0xE7.toByte(),       //   USAGE_MAXIMUM (Keyboard Right GUI)
        0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),       //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(),       //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(),       //   REPORT_COUNT (8)
        0x81.toByte(), 0x02.toByte(),       //   INPUT (Data,Var,Abs)
        // Reserved byte
        0x95.toByte(), 0x01.toByte(),       //   REPORT_COUNT (1)
        0x75.toByte(), 0x08.toByte(),       //   REPORT_SIZE (8)
        0x81.toByte(), 0x03.toByte(),       //   INPUT (Cnst,Var,Abs)
        // Key Array (6 bytes)
        0x95.toByte(), 0x06.toByte(),       //   REPORT_COUNT (6)
        0x75.toByte(), 0x08.toByte(),       //   REPORT_SIZE (8)
        0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x65.toByte(),       //   LOGICAL_MAXIMUM (101)
        0x05.toByte(), 0x07.toByte(),       //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0x00.toByte(),       //   USAGE_MINIMUM (Reserved)
        0x29.toByte(), 0x65.toByte(),       //   USAGE_MAXIMUM (Keyboard Application)
        0x81.toByte(), 0x00.toByte(),       //   INPUT (Data,Ary,Abs)
        0xC0.toByte()                       // END_COLLECTION
    )

    // Helper: Build Mouse Report (4 bytes: [buttons, dx, dy, wheel])
    fun createMouseReport(buttons: Byte, dx: Int, dy: Int, wheel: Int = 0): ByteArray {
        val clampedX = dx.coerceIn(-127, 127).toByte()
        val clampedY = dy.coerceIn(-127, 127).toByte()
        val clampedWheel = wheel.coerceIn(-127, 127).toByte()
        return byteArrayOf(buttons, clampedX, clampedY, clampedWheel)
    }

    // Consumer Control Bits
    const val CONSUMER_VOL_UP: Byte   = (1 shl 0).toByte()
    const val CONSUMER_VOL_DOWN: Byte = (1 shl 1).toByte()
    const val CONSUMER_MUTE: Byte     = (1 shl 2).toByte()
    const val CONSUMER_PLAY: Byte     = (1 shl 3).toByte()
    const val CONSUMER_HOME: Byte     = (1 shl 4).toByte()
    const val CONSUMER_MENU: Byte     = (1 shl 5).toByte()

    // Keyboard HID Keycodes
    const val KEY_NONE: Byte   = 0x00
    const val KEY_ENTER: Byte  = 0x28 // Return / Enter / OK
    const val KEY_ESCAPE: Byte = 0x29 // Escape / Back
    const val KEY_RIGHT: Byte  = 0x4F // Arrow Right
    const val KEY_LEFT: Byte   = 0x50 // Arrow Left
    const val KEY_DOWN: Byte   = 0x51 // Arrow Down
    const val KEY_UP: Byte     = 0x52 // Arrow Up
    const val KEY_MENU: Byte   = 0x65 // Menu / Application key

    fun mapTVKeyToConsumer(key: TVKey): Byte? = when (key) {
        TVKey.VOLUME_UP   -> CONSUMER_VOL_UP
        TVKey.VOLUME_DOWN -> CONSUMER_VOL_DOWN
        TVKey.MUTE        -> CONSUMER_MUTE
        TVKey.PLAY_PAUSE  -> CONSUMER_PLAY
        TVKey.HOME        -> CONSUMER_HOME
        TVKey.MENU        -> CONSUMER_MENU
        else              -> null
    }

    fun mapTVKeyToKeyboard(key: TVKey): Byte? = when (key) {
        TVKey.UP    -> KEY_UP
        TVKey.DOWN  -> KEY_DOWN
        TVKey.LEFT  -> KEY_LEFT
        TVKey.RIGHT -> KEY_RIGHT
        TVKey.OK    -> KEY_ENTER
        TVKey.BACK  -> KEY_ESCAPE
        TVKey.MENU  -> KEY_MENU
        else        -> null
    }
}
