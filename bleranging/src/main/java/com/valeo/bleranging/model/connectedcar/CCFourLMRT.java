package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCFourLMRT extends ConnectedCar {

    public CCFourLMRT(Context mContext) {
        super(mContext);
        trxLinkedHMap = new ConnectedCarFactory.TrxLinkHMapBuilder()
                .left()
                .middle()
                .right()
                .trunk()
                .build();
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_STANDARD);
        rpPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_RP);
//        earPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_EAR);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            rpPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            earPrediction.init(rssi, 0);
            standardPrediction.predict(N_VOTE_SHORT);
            rpPrediction.predict(N_VOTE_LONG);
//            earPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return standardPrediction != null
//                && earPrediction != null
                && rpPrediction != null
                && standardPrediction.isPredictRawFileRead()
//                && earPrediction.isPredictRawFileRead()
                && rpPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null);
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            standardPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
//                earPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_EAR, comValid, lockStatus);
            rpPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
            standardPrediction.predict(N_VOTE_SHORT);
//            earPrediction.predict(N_VOTE_LONG);
            rpPrediction.predict(N_VOTE_LONG);
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
//            earPrediction.calculatePredictionEar(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
            rpPrediction.calculatePredictionRP(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        String result = SdkPreferencesHelper.getInstance().getOpeningStrategy() + "\n";
        if (isInitialized()) {
            return standardPrediction.printDebug(STANDARD_LOC) + rpPrediction.printDebug(RP_LOC);
        }
        return result;
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) { // if smartphone com activated and near ear
                if (lastModelUsed.equals(STANDARD_LOC)) {
                    comValid = true;
                    mHandlerComValidTimeOut.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            comValid = false;
                            lastModelUsed = EAR_HELD_LOC;
                        }
                    }, 2000);
                    return BleRangingHelper.PREDICTION_UNKNOWN;
                }
                lastModelUsed = EAR_HELD_LOC;
                return earPrediction.getPrediction();
            } else {
                lastModelUsed = STANDARD_LOC;
                return standardPrediction.getPrediction();
            }
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    @Override
    public PointF getPredictionCoord() {
        return null;
    }

    @Override
    public double getDist2Car() {
        return 0;
    }
}
