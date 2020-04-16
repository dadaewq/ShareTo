package com.mihotel.shareto;

import android.app.Application;
import android.os.StrictMode;

@SuppressWarnings("WeakerAccess")
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

    }

}
