package com.valeo.bleranging.model.connectedcar;

import com.valeo.bleranging.model.Trx;

import java.util.HashMap;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class CCFiveFlFrLRB extends ConnectedCar {

    public CCFiveFlFrLRB(ConnectionNumber connectionNumber) {
        super(connectionNumber);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxMap = new HashMap<>();
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, historicDefaultValuePeriph);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, historicDefaultValuePeriph);
        trxLeft = new Trx(NUMBER_TRX_LEFT, historicDefaultValuePeriph);
        trxRight = new Trx(NUMBER_TRX_RIGHT, historicDefaultValuePeriph);
        trxBack = new Trx(NUMBER_TRX_BACK, historicDefaultValuePeriph);
        trxFrontLeft.setEnabled(true);
        trxFrontRight.setEnabled(true);
        trxLeft.setEnabled(true);
        trxRight.setEnabled(true);
        trxBack.setEnabled(true);
        trxMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxMap.put(NUMBER_TRX_BACK, trxBack);
    }

    @Override
    public boolean startStrategy(boolean newLockStatus, boolean smartphoneIsInPocket) {
        return false;
    }

    @Override
    public boolean isInStartArea(int threshold) {
        return false;
    }

    @Override
    public int unlockStrategy(boolean smartphoneIsInPocket) {
        return 0;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        return false;
    }

    @Override
    public boolean lockStrategy(boolean smartphoneIsInPocket) {
        return false;
    }

    @Override
    public boolean isInLockArea(int threshold) {
        return false;
    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newlockStatus, boolean smartphoneIsInPocket) {
        return false;
    }

    @Override
    public boolean numberOfTrxValid(int mode, boolean trxL, boolean trxM, boolean trxR, boolean trxB, boolean trxFL, boolean trxRL, boolean trxFR, boolean trxRR) {
        return false;
    }
}
