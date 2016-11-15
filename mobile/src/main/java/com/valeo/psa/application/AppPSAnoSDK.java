package com.valeo.psa.application;

import android.app.Application;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.io.File;

/**
 * Created by l-avaratha on 19/09/2016
 */
public class AppPSAnoSDK extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SdkPreferencesHelper.initializeInstance(this);
        createLogsDir();
        createConfigDir();
    }

    private void createLogsDir() {
        File dir = new File("sdcard/InBlueRssi/");
        //if the folder doesn't exist
        if (!dir.exists()) {
            if (dir.mkdir()) {
                PSALogs.d("make", "dir Success");
            } else {
                PSALogs.d("make", "dir Failed");
            }
        }
    }

    private void createConfigDir() {
        File dir = new File("sdcard/InBlueConfig/");
        //if the folder doesn't exist
        if (!dir.exists()) {
            if (dir.mkdir()) {
                PSALogs.d("make", "dir Success");
            } else {
                PSALogs.d("make", "dir Failed");
            }
        }
    }
}
