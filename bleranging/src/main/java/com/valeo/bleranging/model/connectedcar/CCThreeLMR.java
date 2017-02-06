package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.machinelearningalgo.Prediction;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCThreeLMR extends ConnectedCar {

    public CCThreeLMR(Context mContext) {
        super(mContext, ConnectionNumber.THREE_CONNECTION);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public void initPredictions() {
        standardPrediction = new Prediction(mContext, R.raw.classes_simple,
                R.raw.rf_simple, R.raw.sample_simple);
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        standardPrediction.predict(N_VOTE_LONG);
    }

    @Override
    public double[] getRssiForRangingPrediction() {
        rssi = new double[3];
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
        rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
        return checkForRssiNonNull(rssi);
    }

    @Override
    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SLOW);
        }
        standardPrediction.predict(N_VOTE_LONG);
    }

    @Override
    public void calculatePrediction() {
        standardPrediction.calculatePredictionSimple(THRESHOLD_PROB, THRESHOLD_PROB_UNLOCK, THRESHOLD_PROB_LOCK);
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        return standardPrediction.printDebug(SIMPLE_LOC);
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        return standardPrediction.getPrediction();
    }
}