package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;

import com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionZone;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.utils.PSALogs;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.utils.CheckUtils.checkForRssiNonNull;

/**
 * Created by l-avaratha on 05/09/2016
 */
public abstract class ConnectedCar {
    public final static String THATCHAM_ORIENTED = "thatcham_oriented";
    public final static String PASSIVE_ENTRY_ORIENTED = "passive_entry_oriented";
    final static String SIMPLE_LOC = "Simple Localisation:";
    final static String STANDARD_LOC = "Standard Localisation:";
    final static String TEST_LOC = "Test Localisation:";
    final static String EAR_HELD_LOC = "Ear held Localisation:";
    final static String RP_LOC = "RP Localisation:";
    final static String START_LOC = "Start Localisation:";
    final static String FULL_LOC = "Full Localisation:";
    final static int N_VOTE_LONG = 5;
    final static int N_VOTE_SHORT = 3;
    final static double THRESHOLD_PROB_UNLOCK2LOCK = 0.5;
    final static double THRESHOLD_PROB_LOCK2UNLOCK = 0.9;
    final Handler mHandlerComValidTimeOut = new Handler();
    protected LinkedHashMap<Integer, Trx> trxLinkedHMap;
    //    protected HashMap<String, BasePrediction> predictionHMap;
    protected Context mContext;
    protected double[] rssi;
    PredictionCoord pxPrediction;
    PredictionCoord pyPrediction;
    PredictionZone standardPrediction;
    PredictionZone testPrediction;
    PredictionZone earPrediction;
    PredictionZone rpPrediction;
    boolean comValid = false;
    String lastModelUsed = STANDARD_LOC;

    ConnectedCar(Context context) {
        this.mContext = context;
//        initPredictionHashMap();
    }

//    private void initPredictionHashMap() {
//        predictionHMap = new HashMap<>();
//        predictionHMap.put("pxPrediction", pxPrediction);
//        predictionHMap.put("pyPrediction", pyPrediction);
//        predictionHMap.put("standardPrediction", standardPrediction);
//        predictionHMap.put("earPrediction", earPrediction);
//        predictionHMap.put("rpPrediction", rpPrediction);
//    }

    /**
     * Save an incoming rssi
     *
     * @param trxNumber  the trx that sent the signal
     * @param rssi       the rssi value to save
     * @param bleChannel the ble Channel
     */
    public void saveRssi(int trxNumber, int rssi, byte antennaId, Antenna.BLEChannel bleChannel) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            PSALogs.d("NIH", "trx n°" + trxNumber + " rssi saved");
            trxLinkedHMap.get(trxNumber).saveRssi(rssi, true, antennaId, bleChannel);
        } else {
            PSALogs.d("NIH", "trx n°" + trxNumber + " is NULL, cannot save rssi");
        }
    }

    /**
     * Save an incoming rssi from car
     *
     * @param trxNumber the trx that sent the signal
     * @param rssi      the rssi value to save
     */
    public void saveCarRssi(final int trxNumber, final int rssi) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            trxLinkedHMap.get(trxNumber).saveCarRssi(rssi);
        } else {
            PSALogs.d("NIH", "trx is null, cannot save car rssi");
        }
    }

    /**
     * Save an incoming rssi from car
     *
     * @param trxNumber the trx that sent the signal
     * @param address   the trx mac address to save
     */
    public void saveCarAddress(final int trxNumber, final String address) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            if (trxLinkedHMap.get(trxNumber).getTrxAddress() == null
                    || trxLinkedHMap.get(trxNumber).getTrxAddress().isEmpty()) {
                trxLinkedHMap.get(trxNumber).setTrxAddress(address);
            }
            if (trxLinkedHMap.get(trxNumber).getTrxAddress().equalsIgnoreCase(address)) {
                PSALogs.d("NIH", "trx address match the saved address");
            } else {
                PSALogs.d("NIH", "trx address do not match the saved address");
            }
        } else {
            PSALogs.d("NIH", "trx is null, cannot save car address");
        }
    }

    public LinkedHashMap<Integer, Trx> getTrxLinkedHMap() {
        return trxLinkedHMap;
    }

    public Antenna.BLEChannel getCurrentBLEChannel(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentBLEChannel();
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
    }

    public int getCurrentOriginalRssi(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentOriginalRssi();
        } else {
            return 2;
        }
    }

    public int getCurrentAntennaId(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getAntennaId();
        } else {
            return 0;
        }
    }

    /**
     * Get a prediction of proximity with the car
     *
     * @return the proximity prediction
     */
    public String getPredictionProximity() {
        if (rpPrediction != null) {
            return rpPrediction.getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }

    public double[] getStandardDistribution() {
        if (standardPrediction != null) {
            return standardPrediction.getDistribution();
        }
        return null;
    }

    public String[] getStandardClasses() {
        if (standardPrediction != null) {
            return standardPrediction.getClasses();
        }
        return null;
    }

    public double[] getStandardRssi() {
        if (standardPrediction != null) {
            return standardPrediction.getRssi();
        }
        return null;
    }

    /**
     * Get rssi from beacon to make a prediction
     *
     * @return an array with a rssi from each beacon
     */
    public double[] getRssiForRangingPrediction() {
        if (trxLinkedHMap != null) {
            rssi = new double[trxLinkedHMap.size()];
            Iterator<Trx> trxIterator = trxLinkedHMap.values().iterator();
            for (int i = 0; i < trxLinkedHMap.size(); i++) {
                if (trxIterator.hasNext()) {
                    rssi[i] = getCurrentOriginalRssi(trxIterator.next().getTrxNumber());
                } else {
                    rssi[i] = 1;
                }
            }
        } else {
            PSALogs.d("init2", "getRssiForRangingPrediction trxLinkedHMap is NULL\n");
        }
        return checkForRssiNonNull(rssi);
    }

    public boolean isActive(int trxNumber) {
        return trxLinkedHMap.get(trxNumber) != null && trxLinkedHMap.get(trxNumber).isActive();
    }

    /**
     * Create predictions
     */
    public void readPredictionsRawFiles() {
        pxPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Px);
        pyPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Py);
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_STANDARD);
        earPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_EAR);
        rpPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_RP);
//        initPredictionHashMap();
    }

    /**
     * Initialize predictions
     */
    public abstract void initPredictions();

    /**
     * Check if predictions has been initialized
     *
     * @return true if prediction were initialized, false otherwise
     */
    public abstract boolean isInitialized();

    /**
     * Set the rssi into the machine learning algorithm
     *
     * @param rssi the array containing rssi from beacons
     */
    public abstract void setRssi(double[] rssi, boolean lockStatus);

    /**
     * Calculate a prediction using machine learning
     */
    public abstract void calculatePrediction(float[] orientation);

    public abstract void calculatePredictionTest(Double threshold);

    public abstract String getPredictionPositionTest();

    /**
     * Print debug info
     *
     * @param smartphoneIsInPocket true if the smartphone is supposedly in the pocket, false otherwise
     * @return the debug information
     */
    public abstract String printDebug(boolean smartphoneIsInPocket);

    /**
     * Get a prediction of position regarding the car
     *
     * @param smartphoneIsInPocket true if the smartphone is supposedly in the pocket, false otherwise
     * @return the position prediction
     */
    public abstract String getPredictionPosition(boolean smartphoneIsInPocket);

    /**
     * Get a coord prediction regarding the car
     *
     * @return the position prediction
     */
    public abstract PointF getPredictionCoord();

    public abstract double getDist2Car();
}
