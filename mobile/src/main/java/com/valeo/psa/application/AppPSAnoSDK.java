package com.valeo.psa.application;

import android.app.Application;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 19/09/2016
 */
public class AppPSAnoSDK extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SdkPreferencesHelper.initializeInstance(this);
    }
}
