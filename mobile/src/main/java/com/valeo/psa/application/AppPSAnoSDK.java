package com.valeo.psa.application;

import android.app.Application;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by l-avaratha on 19/09/2016
 */
public class AppPSAnoSDK extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SdkPreferencesHelper.initializeInstance(this);
        File dir = new File("sdcard/InBlueRssi/");
        //if the folder doesn't exist
        if (!dir.exists()) {
            if (dir.mkdir()) {
                PSALogs.d("make", "dir Success");
            } else {
                PSALogs.d("make", "dir Failed");
                return;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk", Locale.FRANCE);
        String timestampLog = sdf.format(new Date());
        SdkPreferencesHelper.getInstance().setLogFileName("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        PSALogs.d("LogFileName", SdkPreferencesHelper.getInstance().getLogFileName());
        File logFile = new File("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        if (!logFile.exists()) {
            try {
                //Create file
                if (logFile.createNewFile()) {
                    PSALogs.d("make", "file Success");
                    //Write 1st row with column names
                    //BufferedWriter for performance, true to set append to file flag
                    String ColNames = "TIMESTAMP;RSSI LEFT;RSSI MIDDLE1;RSSI MIDDLE2;RSSI RIGHT;"
                            + "RSSI TRUNK;RSSI FRONTLEFT;RSSI FRONTRIGHT;RSSI REARLEFT;RSSI REARRIGHT;"
                            + "RSSI BACK;Z AZIMUTH;X PITCH;Y ROLL;IN POCKET;IS LAID;IS LOCK STATUS CHANGED TIMER;"
                            + "IS START BLOCKED;IS START FORCED;IS FROZEN;IS LOCK;REARM LOCK;REARM UNLOCK;"
                            + "REARM WELCOME;WELCOME FLAG;LOCK FLAG;START FLAG;LEFT AREA FLAG;RIGHT AREA FLAG;"
                            + "BACK AREA FLAG;WALK AWAY FLAG;APPROACH FLAG;LEFT TURN FLAG;FULL TURN FLAG;"
                            + "RIGHT TURN FLAG;RECORD FLAG;PREDICTION;LOCK FROM TRX;LOCK TO SEND;START ALLOWED;IS THATCHAM;";
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(ColNames);
                    buf.newLine();
                    buf.close();
                } else {
                    PSALogs.d("make", "file Failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
