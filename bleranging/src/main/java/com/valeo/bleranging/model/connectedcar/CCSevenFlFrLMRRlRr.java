package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.model.Trx;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCSevenFlFrLMRRlRr extends ConnectedCar {

    public CCSevenFlFrLMRRlRr(Context mContext) {
        super(mContext, ConnectionNumber.SEVEN_CONNECTION);
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, TRX_FRONT_LEFT_NAME);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, TRX_FRONT_RIGHT_NAME);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME);
        trxFrontLeft.setEnabled(true);
        trxFrontRight.setEnabled(true);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxLinkedHMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_FRONT_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_FRONT_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_REAR_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_REAR_RIGHT).init(historicDefaultValuePeriph);
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
        return null;
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        return null;
    }
}
