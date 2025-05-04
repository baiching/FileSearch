package com.baiching.filesearch.utils;

import com.baiching.filesearch.HelloApplication;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Platform;
import javafx.stage.Stage;

public class HotKeyManager {
    /*
    * Global Hot key(Jna/JNI)
     */
    private static final int MOD_CONTROL = 0x0002; // Ctrl key
    private static final int VK_SPACE = 0x20;
    private static final int HOTKEY_ID = 1;

    private static final int MOD_ALT = 0x0001;
    private static final int MOD_SHIFT = 0x0004;
    private static final int VK_Z = 0x5A; // 'Z' key


    public static void registerHotKey(Runnable toggleAction) {

        new Thread(() -> {
            int modifiers = MOD_ALT | MOD_SHIFT;
            int key = VK_Z;
            User32.INSTANCE.UnregisterHotKey(null, HOTKEY_ID);
//            boolean success = User32.INSTANCE.RegisterHotKey(null, HOTKEY_ID, MOD_CONTROL, VK_SPACE);
            boolean success = User32.INSTANCE.RegisterHotKey(
                null, // use NULL to associate with current thread
                HOTKEY_ID,
                modifiers,
                key
        );
            if (!success) {
                System.err.println("Hotkey registration failed.");
                return;
            }
            System.out.println("Hotkey registered: Alt + Shift + Z");

            WinUser.MSG msg = new WinUser.MSG();
            while (true) {
                int result = User32.INSTANCE.GetMessage(msg, null, 0, 0);
                if (result == -1) {
                    System.err.println("Error in message loop.");
                    break;
                }

                if (msg.message == WinUser.WM_HOTKEY) {
                    System.out.println("Hotkey pressed!" + msg.message);

                    Platform.runLater(() -> {
                        try {
                            new HelloApplication().start(new Stage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            User32.INSTANCE.UnregisterHotKey(null, HOTKEY_ID);
        }, "HotkeyListener").start();
    }

    // Call this on app shutdown
    public static void unregisterHotKey() {
        User32.INSTANCE.UnregisterHotKey(null, HOTKEY_ID);
    }
}
