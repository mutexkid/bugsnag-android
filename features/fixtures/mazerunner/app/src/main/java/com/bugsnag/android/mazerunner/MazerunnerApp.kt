package com.bugsnag.android.mazerunner

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.StrictMode

class MazerunnerApp : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MazerunnerApp? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        triggerStartupAnrIfRequired()
        setupNonSdkUsageStrictMode()
    }

    /**
     * Configures the mazerunner app so that it will terminate with an exception if [StrictMode]
     * detects that non-public Android APIs have been used. This is intended to provide an
     * early warning system if Bugsnag is using these features internally.
     *
     * https://developer.android.com/about/versions/11/behavior-changes-all#non-sdk-restrictions
     */
    private fun setupNonSdkUsageStrictMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val policy = StrictMode.VmPolicy.Builder()
                .detectNonSdkApiUsage()
                .penaltyDeath()
                .build()
            StrictMode.setVmPolicy(policy)
        }
    }
}
