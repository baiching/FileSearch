package com.baiching.filesearch.utils;

import com.sun.jna.platform.win32.User32;
import javafx.application.Platform;

public class HotKeyManager {
    /*
    * Global Hot key(Jna/JNI)
     */
    private static final int MOD_CONTROL = 0x0002; // Ctrl key
    private static final int VK_SPACE = 0x20;
    private static final int HOTKEY_ID = 1;

    public static void registerHotKey(Runnable toggleAction) {
        // Register Ctrl+Space hotkey
        System.out.println("Registering hotkey");
        boolean success = User32.INSTANCE.RegisterHotKey(
                null, // use NULL to associate with current thread
                HOTKEY_ID,
                MOD_CONTROL,
                VK_SPACE
        );
        System.out.println("Heyeeee");
        if (!success) {
            System.err.println("Failed to register hotkey");
            return;
        }
        System.out.println(success);

        // start the thread that will handle the hotkey
        new Thread(() -> {
            User32.MSG msg = new User32.MSG();
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                if (msg.message == User32.WM_HOTKEY) {
                    Platform.runLater(toggleAction);
                }
            }

        }, "HotkeyListener").start();
    }

    // Call this on app shutdown
    public static void unregisterHotKey() {
        com.baiching.filesearch.utils.User32.INSTANCE.UnregisterHotKey(null, HOTKEY_ID);
    }
}
