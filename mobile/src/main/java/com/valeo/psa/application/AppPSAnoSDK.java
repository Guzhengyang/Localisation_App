package com.valeo.psa.application;

import android.app.Application;
import android.util.Log;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by l-avaratha on 19/09/2016.
 */
public class AppPSAnoSDK extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SdkPreferencesHelper.initializeInstance(this);
        File dir = new File("sdcard/InBlueRssi/");
        //if the folder doesn't exist
        if (!dir.exists()) {
            dir.mkdir();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk", Locale.FRANCE);
        String timestampLog = sdf.format(new Date());
        SdkPreferencesHelper.getInstance().setLogFileName("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        Log.d("LogFileName", SdkPreferencesHelper.getInstance().getLogFileName());
        File logFile = new File("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        if (!logFile.exists()) {
            try {
                //Create file
                logFile.createNewFile();
                //Write 1st row with column names
                //BufferedWriter for performance, true to set append to file flag
                String ColNames = "TIMESTAMP;RSSI LEFT;RSSI MIDDLE1;RSSI MIDDLE2;RSSI RIGHT;RSSI BACK;RSSI FRONTLEFT;RSSI FRONTRIGHT;RSSI REARLEFT;RSSI REARRIGHT;Z AZIMUTH;X PITCH;Y ROLL;IN POCKET;IS LAID;IS PEPS;IS LOCK STATUS CHANGED TIMER;REARM LOCK;REARM UNLOCK;REARM WELCOME;IS LOCK;WELCOME FLAG;LOCK FLAG;START FLAG;LEFT AREA FLAG; RIGHT AREA FLAG; BACK AREA FLAG;WALK AWAY FLAG;STEADY FLAG;APPROACH FLAG; LEFT TURN FLAG; FULL TURN FLAG; RIGHT TURN FLAG;RECORD FLAG;PREDICTION;LOCK FROM TRX;LOCK TO SEND;START ALLOWED;";
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(ColNames);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
