package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.model.Trx;

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
    public void initPredictions() {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public double[] getRssiForRangingPrediction() {
        return new double[0];
    }

    @Override
    public void setRssi(double[] rssi) {

    }

    @Override
    public void calculatePrediction() {

    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
