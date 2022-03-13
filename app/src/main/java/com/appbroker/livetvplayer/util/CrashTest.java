package com.appbroker.livetvplayer.util;

public class CrashTest {

    public static void crash(){
        throw new RuntimeException("Test Crash"); // Force a crash
    }
}
