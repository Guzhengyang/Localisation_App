package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.machinelearningalgo.Prediction;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCTwoMT extends ConnectedCar {

    public CCTwoMT(Context mContext) {
        super(mContext, ConnectionNumber.TWO_CONNECTION);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        trxMiddle.setEnabled(true);
        trxTrunk.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_TRUNK).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = new Prediction(mContext, R.raw.classes_two_start,
                R.raw.rf_two_start, R.raw.sample_two_start);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone() + START_OFFSET);
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
    public double[] getRssiForRangingPrediction() {
        rssi = new double[2];
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        return checkForRssiNonNull(rssi);
    }

    @Override
    public void setRssi(double[] rssi) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i],
                        SdkPreferencesHelper.getInstance().getOffsetSmartphone() + START_OFFSET, THRESHOLD_DIST_AWAY_SLOW);
            }
            standardPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePrediction() {
        if (isInitialized()) {
            standardPrediction.calculatePredictionStart(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            return standardPrediction.printDebug(START_LOC);
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
