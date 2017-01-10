package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.model.Trx;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCEightFlFrLMRTRlRr extends ConnectedCar {
    private static final String SPACE_ONE = " ";
    private static final String SPACE_TWO = "   ";

    public CCEightFlFrLMRTRlRr(Context mContext) {
        super(mContext, ConnectionNumber.EIGHT_CONNECTION);
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, TRX_FRONT_LEFT_NAME);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, TRX_FRONT_RIGHT_NAME);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME);
        trxFrontLeft.setEnabled(true);
        trxFrontRight.setEnabled(true);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxTrunk.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
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
            trxLinkedHMap.get(NUMBER_TRX_TRUNK).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_REAR_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_REAR_RIGHT).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newLockStatus) {
        return (totalAverage >= -100) && newLockStatus;
    }

    @Override
    public SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        return createFirstFooterDebugData(spannableStringBuilder, SPACE_ONE, SPACE_TWO);
    }
}
