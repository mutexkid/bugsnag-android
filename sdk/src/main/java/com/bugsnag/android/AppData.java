package com.bugsnag.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Information about the running Android app which doesn't change over time,
 * including app name, version and release stage.
 * <p/>
 * App information in this class is cached during construction for faster
 * subsequent lookups and to reduce GC overhead.
 */
class AppData extends AppDataSummary {

    private static final Long startTime = SystemClock.elapsedRealtime();

    @Nullable
    final String appName;

    @NonNull
    private final Long duration;

    @Nullable
    private final Boolean inForeground;

    @Nullable
    private final String activeScreen;

    @NonNull
    private final Long memoryUsage;

    @Nullable
    private final Boolean lowMemory;

    AppData(@NonNull Context appContext, @NonNull Configuration config) {
        super(appContext, config);
        appName = getAppName(appContext);
        duration = getDuration();
        inForeground = isInForeground(appContext);
        activeScreen = getActiveScreen(appContext);
        memoryUsage = getMemoryUsage();
        lowMemory = isLowMemory(appContext);
    }

    @Override
    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();

        serialiseMinimalAppData(writer);

        // TODO serialise missing fields from Apiary, migrate metadata values

//        "app": {
//            "id": "com.bugsnag.android.example.debug",
//                "version": "1.1.3",
//                "versionCode": 12,
//                "bundleVersion": "1.0.2",
//                "codeBundleId": "1.0-1234",
//                "buildUUID": "BE5BA3D0-971C-4418-9ECF-E2D1ABCB66BE",
//                "releaseStage": "staging",
//                "type": "rails",
//                "dsymUUIDs": [
//            "e6173678256785afd940392abee"
//        ],
//            "duration": 1275,
//                "durationInForeground": 983,
//                "inForeground": true
//        },


        writer.name("name").value(appName);
        writer.name("packageName").value(packageName);
        writer.name("versionName").value(versionName);
        writer.name("buildUUID").value(config.getBuildUUID());


        writer.name("duration").value(duration);
        writer.name("inForeground").value(inForeground);
        writer.name("activeScreen").value(activeScreen);
        writer.name("memoryUsage").value(memoryUsage);
        writer.name("lowMemory").value(lowMemory);
        writer.endObject();
    }



    /**
     * The name of the running Android app, from android:label in
     * AndroidManifest.xml
     */
    @Nullable
    private static String getAppName(@NonNull Context appContext) {
        try {
            PackageManager packageManager = appContext.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(appContext.getPackageName(), 0);

            return (String) packageManager.getApplicationLabel(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.warn("Could not get app name");
        }
        return null;
    }

    @Nullable
    String getActiveScreenClass() {
        if (activeScreen != null) {
            return activeScreen.substring(activeScreen.lastIndexOf('.') + 1);
        } else {
            return null;
        }
    }

    /**
     * Get the actual memory used by the VM (which may not be the total used
     * by the app in the case of NDK usage).
     */
    @NonNull
    private static Long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Check if the device is currently running low on memory.
     */
    @Nullable
    private static Boolean isLowMemory(@NonNull Context appContext) {
        try {
            ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);

            return memInfo.lowMemory;
        } catch (Exception e) {
            Logger.warn("Could not check lowMemory status");
        }
        return null;
    }

    /**
     * Get the name of the top-most activity. Requires the GET_TASKS permission,
     * which defaults to true in Android 5.0+.
     */
    @Nullable
    private static String getActiveScreen(@NonNull Context appContext) {
        try {
            ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo runningTask = tasks.get(0);
            return runningTask.topActivity.getClassName();
        } catch (Exception e) {
            Logger.warn("Could not get active screen information, we recommend granting the 'android.permission.GET_TASKS' permission");
        }
        return null;
    }

    /**
     * Get the name of the top-most activity. Requires the GET_TASKS permission,
     * which defaults to true in Android 5.0+.
     */
    @Nullable
    private static Boolean isInForeground(@NonNull Context appContext) {
        try {
            ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            if (tasks.isEmpty()) {
                return false;
            }

            ActivityManager.RunningTaskInfo runningTask = tasks.get(0);
            return runningTask.topActivity.getPackageName().equalsIgnoreCase(appContext.getPackageName());
        } catch (Exception e) {
            Logger.warn("Could not check if app is in the foreground, we recommend granting the 'android.permission.GET_TASKS' permission");
        }

        return null;
    }

    /**
     * Get the time in milliseconds since Bugsnag was initialized, which is a
     * good approximation for how long the app has been running.
     */
    @NonNull
    private static Long getDuration() {
        return SystemClock.elapsedRealtime() - startTime;
    }
}
