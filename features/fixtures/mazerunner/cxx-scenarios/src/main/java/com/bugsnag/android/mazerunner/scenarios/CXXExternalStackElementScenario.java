package com.bugsnag.android.mazerunner.scenarios;

import com.bugsnag.android.Configuration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CXXExternalStackElementScenario extends Scenario {

    static {
        System.loadLibrary("cxx-scenarios");
    }

    public native void crash(int counter);

    public CXXExternalStackElementScenario(@NonNull Configuration config,
                                           @NonNull Context context,
                                           @Nullable String eventMetadata) {
        super(config, context, eventMetadata);
    }

    @Override
    public void startScenario() {
        super.startScenario();
        crash(34);
    }
}
