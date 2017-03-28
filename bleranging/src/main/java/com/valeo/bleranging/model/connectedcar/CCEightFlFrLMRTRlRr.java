package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCEightFlFrLMRTRlRr extends ConnectedCar {

    public CCEightFlFrLMRTRlRr(Context mContext) {
        super(mContext);
        trxLinkedHMap = new ConnectedCarFactory.TrxLinkHMapBuilder()
                .left()
                .middle()
                .right()
                .trunk()
                .frontLeft()
                .frontRight()
                .rearleft()
                .rearRight()
                .build();
    }

    @Override
    public void readPredictionsRawFiles() {
        coordPrediction = new PredictionCoord(mContext, R.raw.mlp);
        standardPrediction = PredictionFactory.getPrediction(mContext, PredictionFactory.PREDICTION_STANDARD);
//        insidePrediction = PredictionFactory.getPrediction(mContext, PredictionFactory.PREDICTION_INSIDE);
//        rpPrediction = PredictionFactory.getPrediction(mContext, PredictionFactory.PREDICTION_RP);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            coordPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);
//            insidePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            insidePrediction.predict(N_VOTE_VERY_LONG);
//            rpPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            rpPrediction.predict(N_VOTE_VERY_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return (coordPrediction != null
                && coordPrediction.isPredictRawFileRead()
                && standardPrediction != null
//                && insidePrediction != null
//                && rpPrediction != null
                && standardPrediction.isPredictRawFileRead()
//                && insidePrediction.isPredictRawFileRead()
//                && rpPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null));
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                coordPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone());
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard(), lockStatus);
//                insidePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SLOW, lockStatus);
//                rpPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard(), lockStatus);
            }
            standardPrediction.predict(N_VOTE_SHORT);
//            insidePrediction.predict(N_VOTE_VERY_LONG);
//            rpPrediction.predict(N_VOTE_VERY_LONG);
        }
    }

    @Override
    public void calculatePrediction(float[] orientation) {
        if (isInitialized()) {
            coordPrediction.calculatePredictionCoord(orientation);
            if (SdkPreferencesHelper.getInstance().getOpeningStrategy().equalsIgnoreCase(THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                        THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningStrategy().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                        THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, PASSIVE_ENTRY_ORIENTED);
            }
//            insidePrediction.calculatePredictionInside(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
//            rpPrediction.calculatePredictionRP(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = "";
//            result += coordPrediction.printDebug() + "\n";
            result += SdkPreferencesHelper.getInstance().getOpeningStrategy() + "\n";
            result += standardPrediction.printDebug(FULL_LOC);
            return result;
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = standardPrediction.getPrediction();
            if (SdkPreferencesHelper.getInstance().isPrintInsideEnabled()
                    && result.equalsIgnoreCase(PREDICTION_START)) {
                return getInsidePrediction();
            }
            return result;
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    public PointF getPredictionCoord() {
        if (isInitialized()) {
            return coordPrediction.getPredictionCoord();
        }
        return null;
    }

    private String getInsidePrediction() {
        if (isInitialized()) {
            return insidePrediction.getPrediction();
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
