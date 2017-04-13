package com.valeo.psa.application;

import android.support.multidex.MultiDexApplication;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 19/09/2016
 */
public class AppPSAnoSDK extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SdkPreferencesHelper.initializeInstance(this);
    }
}
