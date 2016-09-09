package com.valeo.bleranging.model.connectedcar;

import android.text.SpannableStringBuilder;

import com.valeo.bleranging.model.Trx;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class CCFiveLMRRlRr extends ConnectedCar {

    public CCFiveLMRRlRr(ConnectionNumber connectionNumber) {
        super(connectionNumber);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME, historicDefaultValuePeriph);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME, historicDefaultValueCentral);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME, historicDefaultValuePeriph);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME, historicDefaultValuePeriph);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME, historicDefaultValuePeriph);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxLinkedHMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
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

    @Override
    public SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder, boolean smartphoneIsInPocket, boolean smartphoneIsLaidDownLAcc, int totalAverage, boolean rearmLock, boolean rearmUnlock) {
        return spannableStringBuilder;
    }
}
