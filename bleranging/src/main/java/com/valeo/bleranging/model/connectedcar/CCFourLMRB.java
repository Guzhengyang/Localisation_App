package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.model.Trx;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCFourLMRB extends ConnectedCar {


    public CCFourLMRB(Context mContext) {
        super(mContext, ConnectionNumber.FOUR_CONNECTION);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxBack = new Trx(NUMBER_TRX_BACK, TRX_BACK_NAME);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxBack.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_BACK, trxBack);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_BACK).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public void initPredictions() {

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
        return null;
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        return null;
    }
}
