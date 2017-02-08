package com.valeo.psa.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by l-avaratha on 07/02/2017
 */

public class PreferenceUtils {

    public static boolean saveSharedPreferencesToFile(Context context, File dst, String sharedPreferencesName) {
        boolean res = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
                PSALogs.d("map save", entry.getKey() + " " + entry.getValue());
            }
            output.writeObject(sharedPreferences.getAll());
            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    public static boolean loadSharedPreferencesFromFileDesciptor(Context context, String filename) {
        try {
            return loadSharedPreferencesFromInputStream(context, new FileInputStream(new File(filename)), SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean loadSharedPreferencesFromFile(Context context, File src, String sharedPreferencesName) {
        try {
            return loadSharedPreferencesFromInputStream(context, new FileInputStream(src), sharedPreferencesName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean loadSharedPreferencesFromInputStream(Context context, FileInputStream src, String sharedPreferencesName) {
        boolean res = false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(src);
            SharedPreferences.Editor prefEdit = sharedPreferences.edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                if (v instanceof Boolean) {
                    prefEdit.putBoolean(key, (Boolean) v);
                    PSALogs.d("map load", key + " " + v.toString() + " bool");
                } else if (v instanceof Float) {
                    prefEdit.putFloat(key, (Float) v);
                    PSALogs.d("map load", key + " " + v.toString() + " float");
                } else if (v instanceof Integer) {
                    prefEdit.putInt(key, (Integer) v);
                    PSALogs.d("map load", key + " " + v.toString() + " int");
                } else if (v instanceof Long) {
                    prefEdit.putLong(key, (Long) v);
                    PSALogs.d("map load", key + " " + v.toString() + " long");
                } else if (v instanceof String) {
                    prefEdit.putString(key, ((String) v));
                    PSALogs.d("map load", key + " " + v.toString() + " string");
//                    if (findPreference(key) != null) {
//                        PSALogs.d("map change", findPreference(key).getSummary() + " to " + v.toString());
//                        findPreference(key).setSummary(v.toString());
//                    }
                }
            }
            prefEdit.apply();
            prefEdit.commit();

            res = true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

}
