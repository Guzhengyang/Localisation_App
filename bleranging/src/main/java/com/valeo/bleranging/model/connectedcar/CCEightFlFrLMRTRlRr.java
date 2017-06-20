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
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_STANDARD);
//        pxPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Px);
//        pyPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Py);
///       squarePrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Clf);
//        rpPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_RP);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);
//            squarePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            rpPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return (standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
//                && squarePrediction != null
//                && squarePrediction.isPredictRawFileRead()
//                && rpPrediction != null
//                && rpPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null));
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            standardPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
            standardPrediction.predict(N_VOTE_SHORT);
            //            squarePrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            //            rpPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
//            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePrediction(float[] orientation) {
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getOpeningStrategy().equalsIgnoreCase(THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                        THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningStrategy().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                        THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, PASSIVE_ENTRY_ORIENTED);
            }
            //            squarePrediction.calculatePredictionCoord(orientation);
//            rpPrediction.calculatePredictionRP(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = "";
            result += SdkPreferencesHelper.getInstance().getOpeningStrategy() + "\n";
            result += standardPrediction.printDebug(FULL_LOC);
//            result += rpPrediction.printDebug(RP_LOC);
            return result;
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (standardPrediction != null && standardPrediction.isPredictRawFileRead()) {
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
        if (squarePrediction != null && squarePrediction.isPredictRawFileRead()) {
            return squarePrediction.getPredictionCoord();
        }
        return null;
    }

    public double getDist2Car() {
        if (squarePrediction != null && squarePrediction.isPredictRawFileRead()) {
            return squarePrediction.getDist2Car();
        }
        return 0f;
    }

    private String getInsidePrediction() {
        if (insidePrediction != null && insidePrediction.isPredictRawFileRead()) {
            return insidePrediction.getPrediction();
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
