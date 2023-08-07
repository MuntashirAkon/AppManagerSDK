package io.github.muntashirakon.AppManager.sdk;

import android.app.Application;

import io.github.muntashirakon.amsdk.AppManagerSdk;

public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppManagerSdk.init(this);
    }
}
