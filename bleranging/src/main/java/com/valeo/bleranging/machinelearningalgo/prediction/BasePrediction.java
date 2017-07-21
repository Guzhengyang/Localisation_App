package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.valeo.bleranging.utils.PSALogs;

import java.util.ArrayList;
import java.util.List;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;


/**
 * Created by l-avaratha on 12/01/2017
 */

public class BasePrediction {
    final List<EasyPredictModelWrapper> modelWrappers = new ArrayList<>();
    final RowData rowData;
    final Context mContext;
    private final List<String> rowDataKeySet;
    double[] rssi_offset; //    rssi after adding smartphone offset
    double[] modified_rssi; //    rssi used for algo entry
    boolean binomial; // whether the model is binomial
    private boolean arePredictRawFileRead = false;

    BasePrediction(Context context, String modelClassName, List<String> rowDataKeySet) {
        this.mContext = context;
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit().execute(modelClassName);
    }

    BasePrediction(Context context, String modelClassNameX, String modelClassNameY, List<String> rowDataKeySet) {
        this.mContext = context;
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit().execute(modelClassNameX, modelClassNameY);
    }

    void constructRowData(double[] rssi) {
        if (rssi == null) {
            return;
        }
        int i = 0;
        for (String elem : rowDataKeySet) {
            if (i < rssi.length) {
                rowData.put(elem, String.valueOf(rssi[i]));
                i++;
            }
        }
    }

    public String printDebug() {
        return "";
    }

    public double[] getModifiedRssi() {
        return modified_rssi;
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

    private class AsyncPredictionInit extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... elements) {
            for (String genModelClassName : elements) {
                hex.genmodel.GenModel rawModel;
                try {
                    rawModel = (hex.genmodel.GenModel) Class.forName(genModelClassName).newInstance();
                    modelWrappers.add(new EasyPredictModelWrapper(rawModel));
                    if (rawModel.isClassifier()) {
                        binomial = rawModel.getNumResponseClasses() == 2;
                    }
                    PSALogs.d("read", genModelClassName + " OK");
                } catch (Exception e) {
                    PSALogs.d("read", e.toString() + " KO");
                }
            }
            arePredictRawFileRead = modelWrappers.size() != 0;
            PSALogs.d("read", "arePredictRawFileRead : " + arePredictRawFileRead);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!arePredictRawFileRead) {
                Toast.makeText(mContext, "Init Model Fail", Toast.LENGTH_LONG).show();
            }
        }
    }
}
