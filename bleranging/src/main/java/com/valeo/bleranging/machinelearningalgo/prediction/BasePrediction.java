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
    final List<EasyPredictModelWrapper> modelWrappers = new ArrayList<>(); // ML model list
    final RowData rowData; // sample vector for ML model
    private final List<String> rowDataKeySet; // name list for sample vector
    double[] rssi_offset; // rssi after adding smartphone offset
    double[] rssi_modified; // rssi used for algo entry
    boolean binomial; // whether the model is binomial
    private boolean arePredictRawFileRead = false; // whether the ML model files are read

    /***
     * used for zone prediction
     * @param context the context
     * @param modelClassName java model class name
     * @param rowDataKeySet the key set list of row data
     */
    BasePrediction(Context context, String modelClassName, List<String> rowDataKeySet) {
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit(context).execute(modelClassName);
    }

    /***
     * used for coord prediction
     * @param context the context
     * @param modelClassNameX java model class name for coord_x
     * @param modelClassNameY java model class name for coord_y
     * @param rowDataKeySet the key set list of row data
     */
    BasePrediction(Context context, String modelClassNameX, String modelClassNameY, List<String> rowDataKeySet) {
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit(context).execute(modelClassNameX, modelClassNameY);
    }

    /***
     * update rowData object for ML prediction
     * @param rssi modified rssi vector
     */
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
        return rssi_modified;
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

    /***
     * read ML model
     */
    private class AsyncPredictionInit extends AsyncTask<String, Void, Void> {
        private final Context context;
        private String message = "";

        public AsyncPredictionInit(Context context) {
            this.context = context;
        }

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
                    message = "Init " + genModelClassName + " Model fail";
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
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
