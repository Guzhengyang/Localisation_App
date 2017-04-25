package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;
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
        coordPrediction = PredictionFactory.getPredictionCoord(mContext);
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_STANDARD);
//        insidePrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_INSIDE);
        rpPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_RP);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            coordPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);
//            insidePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            insidePrediction.predict(N_VOTE_VERY_LONG);
            rpPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return (coordPrediction != null
                && coordPrediction.isPredictRawFileRead()
                && standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
//                && insidePrediction != null
                && rpPrediction != null
//                && insidePrediction.isPredictRawFileRead()
                && rpPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null));
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            coordPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
//                insidePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SLOW, lockStatus);
            rpPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
            standardPrediction.predict(N_VOTE_SHORT);
//            insidePrediction.predict(N_VOTE_VERY_LONG);
            rpPrediction.predict(N_VOTE_LONG);
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
            rpPrediction.calculatePredictionRP(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = "";
            result += SdkPreferencesHelper.getInstance().getOpeningStrategy() + "\n";
            result += standardPrediction.printDebug(FULL_LOC);
            result += rpPrediction.printDebug(RP_LOC);
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

    public double getDist2Car() {
        if (isInitialized()) {
            return coordPrediction.getDist2Car();
        }
        return 0f;
    }

    private String getInsidePrediction() {
        if (isInitialized()) {
            return insidePrediction.getPrediction();
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
