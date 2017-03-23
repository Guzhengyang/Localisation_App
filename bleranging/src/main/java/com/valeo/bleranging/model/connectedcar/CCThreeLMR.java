package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCThreeLMR extends ConnectedCar {

    public CCThreeLMR(Context mContext) {
        super(mContext);
        trxLinkedHMap = new ConnectedCarFactory.TrxLinkHMapBuilder()
                .left()
                .middle()
                .right()
                .build();
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = PredictionFactory.getPrediction(mContext, PredictionFactory.PREDICTION_STANDARD);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null);
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SLOW, lockStatus);
            }
            standardPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePrediction() {
        if (isInitialized()) {
            standardPrediction.calculatePredictionDefault(SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard(),
                    THRESHOLD_PROB_UNLOCK2LOCK);
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            return standardPrediction.printDebug(SIMPLE_LOC);
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            return standardPrediction.getPrediction();
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    @Override
    public PointF getPredictionCoord() {
        return null;
    }
}
