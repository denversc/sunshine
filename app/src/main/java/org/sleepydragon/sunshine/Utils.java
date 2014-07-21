package org.sleepydragon.sunshine;

import android.util.Log;

public class Utils {

    public static final String LOG_TAG = "Sunshine";

    private Utils() {
    }

    public static boolean isDebugLogEnabled() {
        return Log.isLoggable(LOG_TAG, Log.DEBUG);
    }
}
