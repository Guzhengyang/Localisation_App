package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

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
    public void initPredictions() {
        standardPrediction = new Prediction(mContext, R.raw.classes_start,
                R.raw.rf_start, R.raw.sample_start);
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone() + START_OFFSET);
        standardPrediction.predict(N_VOTE_LONG);
    }

    @Override
    public double[] getRssiForRangingPrediction() {
        return new double[0];
    }

    @Override
    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone() + START_OFFSET, THRESHOLD_DIST_AWAY_SLOW);
        }
        standardPrediction.predict(N_VOTE_LONG);
    }

    @Override
    public void calculatePrediction() {
        standardPrediction.calculatePredictionStart(THRESHOLD_PROB);
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        return standardPrediction.printDebug(START_LOC);
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        return standardPrediction.getPrediction();
    }
}
