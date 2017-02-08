package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.machinelearningalgo.Prediction;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCFourLMRT extends ConnectedCar {

    public CCFourLMRT(Context mContext) {
        super(mContext, ConnectionNumber.FOUR_CONNECTION);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxTrunk.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_TRUNK).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public void initPredictions() {
        if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(THATCHAM_ORIENTED)) {
            standardPrediction = new Prediction(mContext, R.raw.classes_standard_thatcham,
                    R.raw.rf_standard_thatcham, R.raw.sample_standard_thatcham);
        } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
            standardPrediction = new Prediction(mContext, R.raw.classes_standard_entry,
                    R.raw.rf_standard_entry, R.raw.sample_standard_entry);
        }
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        standardPrediction.predict(N_VOTE_SHORT);

        this.rpPrediction = new Prediction(mContext, R.raw.classes_rp,
                R.raw.rf_rp, R.raw.sample_rp);
        rpPrediction.init(rssi, 0); //TODO create other offsets
        rpPrediction.predict(N_VOTE_LONG);

        this.earPrediction = new Prediction(mContext, R.raw.classes_ear,
                R.raw.rf_ear, R.raw.sample_ear);
        earPrediction.init(rssi, 0); //TODO create other offsets
        earPrediction.predict(N_VOTE_LONG);
    }

    @Override
    public boolean isInitialized() {
        return standardPrediction != null && earPrediction != null && rpPrediction != null;
    }

    @Override
    public double[] getRssiForRangingPrediction() {
        rssi = new double[4];
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
        rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
        rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        return checkForRssiNonNull(rssi);
    }

    @Override
    public void setRssi(double[] rssi) {
        if (isInitialized()) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard());
                earPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_EAR, comValid);
                rpPrediction.setRssi(i, rssi[i], 0, SdkPreferencesHelper.getInstance().getThresholdDistAwayStandard());
            }
            standardPrediction.predict(N_VOTE_SHORT);
            earPrediction.predict(N_VOTE_LONG);
            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePrediction() {
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(), THRESHOLD_PROB_UNLOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(), THRESHOLD_PROB_UNLOCK, PASSIVE_ENTRY_ORIENTED);
            }
            earPrediction.calculatePredictionEar(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
            rpPrediction.calculatePredictionRP(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        String result = SdkPreferencesHelper.getInstance().getOpeningOrientation() + "\n";
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
                result = earPrediction.printDebug(EAR_HELD_LOC);
            } else {
                result = standardPrediction.printDebug(STANDARD_LOC);
            }
            result += rpPrediction.printDebug(RP_LOC);
        }
        return result;
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) { // if smartphone com activated and near ear
                if (lastModelUsed.equals(STANDARD_LOC)) {
                    comValid = true;
                    mHandlerComValidTimeOut.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            comValid = false;
                            lastModelUsed = EAR_HELD_LOC;
                        }
                    }, 2000);
                    return BleRangingHelper.PREDICTION_UNKNOWN;
                }
                lastModelUsed = EAR_HELD_LOC;
                return earPrediction.getPrediction();
            } else {
                lastModelUsed = STANDARD_LOC;
                return standardPrediction.getPrediction();
            }
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }
}
