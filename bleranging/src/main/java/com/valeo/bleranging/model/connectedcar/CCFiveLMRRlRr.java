package com.valeo.bleranging.model.connectedcar;

import com.valeo.bleranging.model.Trx;

import java.util.HashMap;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class CCFiveLMRRlRr extends ConnectedCar {

    public CCFiveLMRRlRr(ConnectionNumber connectionNumber) {
        super(connectionNumber);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxMap = new HashMap<>();
        trxLeft = new Trx(NUMBER_TRX_LEFT, historicDefaultValuePeriph);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, historicDefaultValueCentral);
        trxRight = new Trx(NUMBER_TRX_RIGHT, historicDefaultValuePeriph);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, historicDefaultValuePeriph);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, historicDefaultValuePeriph);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
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
