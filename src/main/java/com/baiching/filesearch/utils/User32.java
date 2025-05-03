package com.baiching.filesearch.utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;


public interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.load("user32", User32.class);

    // Register hot key
    boolean RegisterHotKey(WinDef.HWND hWnd, int id, int fsModifiers, int vk);

    // Unregister hot key
    boolean UnregisterHotKey(WinDef.HWND hWnd, int id);

    // Windows message constants
    int WM_HOTKEY = 0x0312;
}
