package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import static com.valeo.bleranging.utils.JsonUtils.createRowDataList;
import static com.valeo.bleranging.utils.JsonUtils.getPredictionCoordJsonContent;
import static com.valeo.bleranging.utils.JsonUtils.getPredictionZoneJsonContent;
import static com.valeo.bleranging.utils.JsonUtils.getStoredData;

/**
 * Created by l-avaratha on 17/02/2017
 */

public class PredictionFactory {

    /***
     *
     * @param mContext
     * @return Coordinate Prediction
     */
    public static PredictionCoord getPredictionCoord(Context mContext) {
        final String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        getStoredData(mContext);
        final String[] predictionData = getPredictionCoordJsonContent(carType);
        if (predictionData != null) {
            return new PredictionCoord(mContext, predictionData[2], predictionData[3], createRowDataList(predictionData[1]));
        }
        return null;
    }

    /***
     *
     * @param mContext
     * @param predictionType  standard prediction for zones(left, right, lock, start, ...) and rp prediction(near or far)
     * @return
     */
    public static PredictionZone getPredictionZone(Context mContext, String predictionType) {
        final String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        final String strategy = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        final boolean miniPrediction = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        getStoredData(mContext);
        final String[] predictionData = getPredictionZoneJsonContent(carType, predictionType, miniPrediction, strategy);
        if (predictionData != null) {
            return new PredictionZone(mContext, predictionData[5], createRowDataList(predictionData[4]), predictionType);
        }
        return null;
    }
}
