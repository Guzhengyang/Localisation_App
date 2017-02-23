package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCSixFlFrMTRlRr extends ConnectedCar {

    public CCSixFlFrMTRlRr(Context mContext) {
        super(mContext);
        trxLinkedHMap = new ConnectedCarFactory.TrxLinkHMapBuilder()
                .middle()
                .trunk()
                .frontLeft()
                .frontRight()
                .rearleft()
                .rearRight()
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
            standardPrediction.predict(N_VOTE_SHORT);
        }
    }

    @Override
    public boolean isInitialized() {
        return standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null);
    }

    @Override
    public void setRssi(double[] rssi) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard());
            }
            standardPrediction.predict(N_VOTE_SHORT);
        }
    }

    @Override
    public void calculatePrediction() {
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionFull(SdkPreferencesHelper.getInstance().getThresholdProbStandard(), THRESHOLD_PROB_UNLOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionFull(SdkPreferencesHelper.getInstance().getThresholdProbStandard(), THRESHOLD_PROB_UNLOCK, PASSIVE_ENTRY_ORIENTED);
            }
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = SdkPreferencesHelper.getInstance().getOpeningOrientation() + "\n";
            result += standardPrediction.printDebug(FULL_LOC);
            return result;
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
}
