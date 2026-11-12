#!/usr/bin/env python3
"""
Air Mouse Companion Receiver for PC (Windows / Cross-Platform)
Listens on UDP (default port 8888) and executes cursor movement, clicks, scrolls, and TV/media key events.
Zero external dependencies required on Windows (uses built-in ctypes/Win32 API).
"""

import socket
import sys
import os
import platform

IS_WINDOWS = platform.system() == "Windows"

if IS_WINDOWS:
    import ctypes
    user32 = ctypes.windll.user32
    
    # Win32 Mouse Event Flags
    MOUSEEVENTF_MOVE       = 0x0001
    MOUSEEVENTF_LEFTDOWN   = 0x0002
    MOUSEEVENTF_LEFTUP     = 0x0004
    MOUSEEVENTF_RIGHTDOWN  = 0x0008
    MOUSEEVENTF_RIGHTUP    = 0x0010
    MOUSEEVENTF_MIDDLEDOWN = 0x0020
    MOUSEEVENTF_MIDDLEUP   = 0x0040
    MOUSEEVENTF_WHEEL      = 0x0800

    # Win32 Virtual Key Codes
    VK_MAPPING = {
        "UP": 0x26,
        "DOWN": 0x28,
        "LEFT": 0x25,
        "RIGHT": 0x27,
        "OK": 0x0D,             # Return
        "BACK": 0x1B,           # Escape
        "HOME": 0x5B,           # Windows Key
        "MENU": 0x5D,           # Context Menu
        "VOLUME_UP": 0xAF,
        "VOLUME_DOWN": 0xAE,
        "MUTE": 0xAD,
        "PLAY_PAUSE": 0xB3
    }
else:
    try:
        import pyautogui
    except ImportError:
        print("[!] On non-Windows OS, install pyautogui: pip install pyautogui")
        sys.exit(1)


def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(('10.255.255.255', 1))
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip


def handle_mouse_move(dx: int, dy: int):
    if IS_WINDOWS:
        user32.mouse_event(MOUSEEVENTF_MOVE, dx, dy, 0, 0)
    else:
        pyautogui.moveRel(dx, dy)


def handle_mouse_button(button: str, is_down: bool):
    if IS_WINDOWS:
        if button == "LEFT":
            flag = MOUSEEVENTF_LEFTDOWN if is_down else MOUSEEVENTF_LEFTUP
        elif button == "RIGHT":
            flag = MOUSEEVENTF_RIGHTDOWN if is_down else MOUSEEVENTF_RIGHTUP
        elif button == "MIDDLE":
            flag = MOUSEEVENTF_MIDDLEDOWN if is_down else MOUSEEVENTF_MIDDLEUP
        else:
            return
        user32.mouse_event(flag, 0, 0, 0, 0)
    else:
        btn = button.lower()
        if is_down:
            pyautogui.mouseDown(button=btn)
        else:
            pyautogui.mouseUp(button=btn)


def handle_scroll(v_delta: int):
    if IS_WINDOWS:
        # Standard Win32 WHEEL_DELTA is 120 per notch
        wheel_amount = v_delta * 120
        user32.mouse_event(MOUSEEVENTF_WHEEL, 0, 0, wheel_amount, 0)
    else:
        pyautogui.scroll(v_delta * 10)


def handle_key(key_name: str):
    if IS_WINDOWS:
        vk = VK_MAPPING.get(key_name)
        if vk:
            KEYEVENTF_KEYUP = 0x0002
            user32.keybd_event(vk, 0, 0, 0)
            user32.keybd_event(vk, 0, KEYEVENTF_KEYUP, 0)
    else:
        mapping = {
            "UP": "up", "DOWN": "down", "LEFT": "left", "RIGHT": "right",
            "OK": "enter", "BACK": "esc", "VOLUME_UP": "volumeup",
            "VOLUME_DOWN": "volumedown", "MUTE": "volumemute", "PLAY_PAUSE": "playpause"
        }
        key = mapping.get(key_name)
        if key:
            pyautogui.press(key)


def run_receiver(port: int = 8888):
    host_ip = get_local_ip()
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(("0.0.0.0", port))

    print("=" * 60)
    print("      AIR MOUSE & TV REMOTE - COMPANION RECEIVER")
    print("=" * 60)
    print(f"[*] Receiver listening on UDP port: {port}")
    print(f"[*] Your PC's Local IP Address:    {host_ip}")
    print(f"[*] Enter '{host_ip}' in the Android App Wi-Fi settings!")
    print("=" * 60)
    print("[*] Ready. Waiting for inputs from Android app...\n")

    packet_count = 0
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            msg = data.decode('utf-8', errors='ignore').strip()
            if not msg:
                continue

            if msg == "PING":
                continue

            packet_count += 1
            parts = msg.split(':')
            prefix = parts[0]

            if prefix == "M" and len(parts) >= 3:
                dx = int(parts[1])
                dy = int(parts[2])
                handle_mouse_move(dx, dy)
            elif prefix == "B" and len(parts) >= 3:
                btn = parts[1]
                is_down = parts[2] == "1"
                handle_mouse_button(btn, is_down)
            elif prefix == "S" and len(parts) >= 2:
                v_delta = int(parts[1])
                handle_scroll(v_delta)
            elif prefix == "K" and len(parts) >= 2:
                key_name = parts[1]
                handle_key(key_name)
            elif prefix == "RELEASE":
                if IS_WINDOWS:
                    user32.mouse_event(MOUSEEVENTF_LEFTUP | MOUSEEVENTF_RIGHTUP | MOUSEEVENTF_MIDDLEUP, 0, 0, 0, 0)

            if packet_count % 100 == 0:
                print(f"[+] Processed {packet_count} packets from {addr[0]} (latest: {msg})")

        except KeyboardInterrupt:
            print("\nExiting receiver...")
            break
        except Exception as e:
            print(f"[!] Error: {e}")


if __name__ == "__main__":
    port_arg = int(sys.argv[1]) if len(sys.argv) > 1 else 8888
    run_receiver(port_arg)
