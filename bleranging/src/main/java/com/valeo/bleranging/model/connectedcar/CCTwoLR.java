package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCTwoLR extends ConnectedCar {

    public CCTwoLR(Context mContext) {
        super(mContext, ConnectionNumber.TWO_CONNECTION);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxLeft.setEnabled(true);
        trxRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = PredictionFactory.getPrediction(mContext, PredictionFactory.PREDICTION_STANDARD);
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
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
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
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
