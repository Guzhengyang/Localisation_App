package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCSixFlFrMTRlRr extends ConnectedCar {

    public CCSixFlFrMTRlRr(Context mContext) {
        super(mContext, ConnectionNumber.EIGHT_CONNECTION);
        Trx trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, TRX_FRONT_LEFT_NAME);
        Trx trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, TRX_FRONT_RIGHT_NAME);
        Trx trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        Trx trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        Trx trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME);
        Trx trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxLinkedHMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxLinkedHMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            for (Trx trx : trxLinkedHMap.values()) {
                if (trx.getTrxNumber() == NUMBER_TRX_MIDDLE) {
                    trx.init(historicDefaultValueCentral);
                } else {
                    trx.init(historicDefaultValuePeriph);
                }
            }
        }
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = PredictionFactory.getPrediction(mContext, PredictionFactory.PREDICTION_STANDARD);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);
        }
    }

    @Override
    public boolean isInitialized() {
        return standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null);
    }

    @Override
    public double[] getRssiForRangingPrediction() {
        rssi = new double[6];
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT);
        rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT);
        rssi[4] = getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT);
        rssi[5] = getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT);
        return checkForRssiNonNull(rssi);
    }

    @Override
    public void setRssi(double[] rssi) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard());
            }
            standardPrediction.predict(N_VOTE_SHORT);
        }
    }

    @Override
    public void calculatePrediction() {
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionFull(SdkPreferencesHelper.getInstance().getThresholdProbStandard(), THRESHOLD_PROB_UNLOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionFull(SdkPreferencesHelper.getInstance().getThresholdProbStandard(), THRESHOLD_PROB_UNLOCK, PASSIVE_ENTRY_ORIENTED);
            }
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = SdkPreferencesHelper.getInstance().getOpeningOrientation() + "\n";
            result += standardPrediction.printDebug(FULL_LOC);
            return result;
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            return standardPrediction.getPrediction();
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
