package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.model.Trx;

import java.util.List;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCFiveLMRTB extends ConnectedCar {
    private static final String SPACE_ONE = "  ";
    private static final String SPACE_TWO = "      ";

    public CCFiveLMRTB(Context mContext) {
        super(mContext, ConnectionNumber.FIVE_CONNECTION);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        trxBack = new Trx(NUMBER_TRX_BACK, TRX_BACK_NAME);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxTrunk.setEnabled(true);
        trxBack.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
        trxLinkedHMap.put(NUMBER_TRX_BACK, trxBack);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_TRUNK).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_BACK).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public boolean startStrategy() {
        return false;
    }

    @Override
    public boolean isInStartArea(int threshold) {
        return false;
    }

    @Override
    public List<Integer> unlockStrategy() {
        //TODO ADD TRUNK
        return null;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        return false;
    }

    @Override
    public boolean lockStrategy() {
        return false;
    }

    @Override
    public boolean isInLockArea(int threshold) {
        return false;
    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newlockStatus) {
        return false;
    }

    @Override
    public SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        return createFirstFooterDebugData(spannableStringBuilder, SPACE_ONE, SPACE_TWO);
    }

    @Override
    public SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder, boolean smartphoneIsInPocket, boolean smartphoneIsMovingSlowly, int totalAverage, boolean rearmLock, boolean rearmUnlock) {
        return spannableStringBuilder;
    }
}
