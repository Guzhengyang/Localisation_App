package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.machinelearningalgo.Prediction;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCEightFlFrLMRTRlRr extends ConnectedCar {

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
    public void initPredictions() {
        standardPrediction = new Prediction(mContext, R.raw.classes_full,
                R.raw.rf_full, R.raw.sample_full);
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        standardPrediction.predict(N_VOTE_SHORT);

        insidePrediction = new Prediction(mContext, R.raw.classes_inside,
                R.raw.rf_inside, R.raw.sample_inside);
        insidePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        insidePrediction.predict(N_VOTE_LONG);
    }

    @Override
    public boolean isInitialized() {
        return standardPrediction != null && insidePrediction != null;
    }

    @Override
    public double[] getRssiForRangingPrediction() {
        rssi = new double[8];
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
        rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
        rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        rssi[4] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT);
        rssi[5] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT);
        rssi[6] = getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT);
        rssi[7] = getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT);
        return checkForRssiNonNull(rssi);
    }

    @Override
    public void setRssi(double[] rssi) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard());
                insidePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard());
            }
            standardPrediction.predict(N_VOTE_SHORT);
            insidePrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePrediction() {
        if (isInitialized()) {
            standardPrediction.calculatePredictionFull(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
            insidePrediction.calculatePredictionInside(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = standardPrediction.printDebug(FULL_LOC);
            if (standardPrediction.getPrediction().equals(PREDICTION_START)) {
                result += insidePrediction.printDebug(INSIDE_LOC);
            }
            return result;
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = standardPrediction.getPrediction();
            if (result.equalsIgnoreCase(PREDICTION_START)) {
                return getInsidePrediction();
            }
            return result;
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    private String getInsidePrediction() {
        if (isInitialized()) {
            return insidePrediction.getPrediction();
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
