package com.valeo.bleranging.model;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.machinelearningalgo.prediction.BasePrediction;
import com.valeo.bleranging.machinelearningalgo.prediction.Coord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionZone;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.CalculusUtils;
import com.valeo.bleranging.utils.PSALogs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord.INDEX_KALMAN;
import static com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord.INDEX_RAW;
import static com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord.INDEX_THRESHOLD;
import static com.valeo.bleranging.persistence.Constants.N_VOTE_LONG;
import static com.valeo.bleranging.persistence.Constants.N_VOTE_SHORT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_COORD;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_EAR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RP;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_STD;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_TEST;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.persistence.Constants.THRESHOLD_PROB_LOCK2UNLOCK;
import static com.valeo.bleranging.persistence.Constants.THRESHOLD_PROB_UNLOCK2LOCK;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_4_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_8_A;
import static com.valeo.bleranging.utils.CalculusUtils.initMatrix;

/**
 * Created by l-avaratha on 12/07/2017
 */

public class MultiPrediction {
    private static final double THRESHOLD_DIST = 0.1;
    private final LinkedHashMap<String, BasePrediction> predictionLinkedHMap;

    public MultiPrediction(LinkedHashMap<String, BasePrediction> predictionLinkedHMap) {
        this.predictionLinkedHMap = predictionLinkedHMap;
        initMatrix();
    }

    /**
     * Create predictions
     */
    public void readPredictionsRawFiles(Context context) {
        predictionLinkedHMap.put(PREDICTION_COORD, PredictionFactory.getPredictionCoord(context));
        predictionLinkedHMap.put(PREDICTION_STD, PredictionFactory.getPredictionZone(context, PREDICTION_STD));
        predictionLinkedHMap.put(PREDICTION_TEST, PredictionFactory.getPredictionZone(context, PREDICTION_TEST));
        predictionLinkedHMap.put(PREDICTION_EAR, PredictionFactory.getPredictionZone(context, PREDICTION_EAR));
        predictionLinkedHMap.put(PREDICTION_RP, PredictionFactory.getPredictionZone(context, PREDICTION_RP));
        PSALogs.d("init2", "readPredictionsRawFiles OK");
    }

    public void initPredictions(final double[] rssiTab, final int offset) {
        final int nVote = 5;
        for (String predictionType : predictionLinkedHMap.keySet()) {
            initPredictions(predictionType, rssiTab, offset, nVote);
        }
        PSALogs.d("init2", "initPredictions OK");
    }

    /**
     * Initialize predictions
     */
    private void initPredictions(final String predictionType, final double[] rssiTab,
                                 final int offset, final int nVote) {
        if (isInitialized(predictionType)) {
            if (predictionLinkedHMap.get(predictionType) instanceof PredictionZone) {
                ((PredictionZone) predictionLinkedHMap.get(predictionType)).init(rssiTab, offset);
                ((PredictionZone) predictionLinkedHMap.get(predictionType)).predict(nVote);
            } else if (predictionLinkedHMap.get(predictionType) instanceof PredictionCoord) {
                ((PredictionCoord) predictionLinkedHMap.get(predictionType)).init(rssiTab, offset);
            }
        }
    }

    /**
     * Check if predictions has been initialized
     *
     * @return true if prediction were initialized, false otherwise
     */
    public boolean isInitialized() {
        for (String predictionType : predictionLinkedHMap.keySet()) {
            if (isInitialized(predictionType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInitialized(String predictionType) {
        return predictionLinkedHMap.get(predictionType) != null
                && predictionLinkedHMap.get(predictionType).isPredictRawFileRead();
    }

    /**
     * Set the rssi into the machine learning algorithm
     *
     * @param rssiTab the array containing rssi from beacons
     */
    public void setRssi(double[] rssiTab) {
        for (String predictionType : predictionLinkedHMap.keySet()) {
            final int nVote = getNVote(predictionType);
            setRssi(predictionType, rssiTab,
                    SdkPreferencesHelper.getInstance().getOffsetSmartphone(), nVote);
        }
    }

    private int getNVote(String predictionType) {
        switch (SdkPreferencesHelper.getInstance().getConnectedCarType()) {
            case TYPE_2_A:
                return N_VOTE_LONG;
            case TYPE_2_B:
                return N_VOTE_LONG;
            case TYPE_4_B:
                switch (predictionType) {
                    case PREDICTION_STD:
                        return N_VOTE_SHORT;
                    case PREDICTION_RP:
                        return N_VOTE_LONG;
                    default:
                        return N_VOTE_LONG;
                }
            case TYPE_8_A:
                switch (predictionType) {
                    case PREDICTION_STD:
                        return N_VOTE_LONG;
                    case PREDICTION_RP:
                        return N_VOTE_LONG;
                    default:
                        return N_VOTE_LONG;
                }
            default:
                return N_VOTE_SHORT;
        }
    }

    private void setRssi(final String predictionType, final double[] rssiTab,
                         final int offset, final int nVote) {
        if (isInitialized(predictionType)) {
            if (predictionLinkedHMap.get(predictionType) instanceof PredictionZone) {
                ((PredictionZone) predictionLinkedHMap.get(predictionType)).setRssi(rssiTab, offset);
                ((PredictionZone) predictionLinkedHMap.get(predictionType)).predict(nVote);
            } else if (predictionLinkedHMap.get(predictionType) instanceof PredictionCoord) {
                ((PredictionCoord) predictionLinkedHMap.get(predictionType)).setRssi(rssiTab, offset);
            }
        }
    }

    /**
     * Calculate a prediction using machine learning
     */
    public void calculatePredictionZone() {
        for (String predictionType : predictionLinkedHMap.keySet()) {
            if (isInitialized(predictionType)) {
                if (predictionLinkedHMap.get(predictionType) instanceof PredictionZone) {
                    ((PredictionZone) predictionLinkedHMap.get(predictionType)).calculatePrediction(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                            THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, SdkPreferencesHelper.getInstance().getOpeningStrategy());
                }
            }
        }
    }

    /**
     * Calculate a prediction using machine learning
     */
    public void calculatePredictionCoord() {
        for (String predictionType : predictionLinkedHMap.keySet()) {
            if (isInitialized(predictionType)) {
                if (predictionLinkedHMap.get(predictionType) instanceof PredictionCoord) {
                    final PredictionCoord predictionCoord = (PredictionCoord) predictionLinkedHMap.get(predictionType);
                    if (predictionCoord != null) {
                        final Coord mlCoord = predictionCoord.getMLCoord();
                        for (int i = 0; i < predictionCoord.getCoordsSize(); i++) {
                            if (predictionCoord.getPredictionCoord(i) != null) {
                                if (i == INDEX_KALMAN) {
                                    CalculusUtils.correctCoordKalman(predictionCoord.getPredictionCoord(i), mlCoord);
                                } else if (i == INDEX_THRESHOLD) {
                                    CalculusUtils.correctCoordThreshold(predictionCoord.getPredictionCoord(i), mlCoord, THRESHOLD_DIST);
                                } else if (i == INDEX_RAW) {
                                    predictionCoord.getPredictionCoord(i).setCoord(mlCoord);
                                }
                                CalculusUtils.correctBoundary(predictionCoord.getPredictionCoord(i));
                            }
                        }
                    }
                }
            }
        }
    }

    public void calculatePredictionTest(Double threshold) {
        PSALogs.d("abstract", "calculatePredictionTest override OK !");
        if (predictionLinkedHMap.get(PREDICTION_TEST) != null && threshold >= 0 && threshold <= 1) {
            ((PredictionZone) predictionLinkedHMap.get(PREDICTION_TEST)).setThresholdROC(threshold);
        }
    }

    /**
     * Print debug info
     *
     * @return the debug information
     */
    public String printDebug() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (String predictionType : predictionLinkedHMap.keySet()) {
            final String temp = printDebug(predictionType);
            if (temp.length() > 0) {
                stringBuilder.append(temp);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private String printDebug(final String predictionType) {
        if (isInitialized(predictionType)) {
            return predictionLinkedHMap.get(predictionType).printDebug();
        }
        return "";
    }

    /**
     * Get a prediction of position regarding the car
     *
     * @return the position prediction
     */
    public String getPredictionZone(boolean smartphoneIsInPocket) {
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
            if (isInitialized(PREDICTION_EAR)) {
                return ((PredictionZone) predictionLinkedHMap.get(PREDICTION_EAR)).getPrediction();
            }
        } else if (isInitialized(PREDICTION_STD)) {
            return ((PredictionZone) predictionLinkedHMap.get(PREDICTION_STD)).getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }

    /**
     * Get a coord prediction regarding the car
     *
     * @return the position prediction
     */
    public List<PointF> getPredictionCoord() {
        List<PointF> pointFList = null;
        if (isInitialized(PREDICTION_COORD)) {
            for (int i = 0; i < ((PredictionCoord) predictionLinkedHMap.get(PREDICTION_COORD)).getCoordsSize(); i++) {
                final Coord coordFinal = ((PredictionCoord) predictionLinkedHMap.get(PREDICTION_COORD)).getPredictionCoord(i);
                if (coordFinal != null) {
                    if (pointFList == null) {
                        pointFList = new ArrayList<>();
                    }
                    pointFList.add(new PointF((float) coordFinal.getCoord_x(), (float) coordFinal.getCoord_y()));
                }
            }
        }
        return pointFList;
    }

    public List<String> getPredictionCoord2zone() {
        List<String> zoneList = null;
        if (isInitialized(PREDICTION_COORD)) {
            for (int i = 0; i < ((PredictionCoord) predictionLinkedHMap.get(PREDICTION_COORD)).getCoordsSize(); i++) {
                final Coord coordFinal = ((PredictionCoord) predictionLinkedHMap.get(PREDICTION_COORD)).getPredictionCoord(i);
                if (coordFinal != null) {
                    if (zoneList == null) {
                        zoneList = new ArrayList<>();
                    }
                    zoneList.add(CalculusUtils.coord2zone(coordFinal.getCoord_x(), coordFinal.getCoord_y(), SdkPreferencesHelper.getInstance().getThresholdUnlockLock()));
                }
            }
        }
        return zoneList;
    }

    /**
     * Get a prediction of proximity with the car
     *
     * @return the proximity prediction
     */
    public String getPredictionRP() {
        if (isInitialized(PREDICTION_RP)) {
            return ((PredictionZone) predictionLinkedHMap.get(PREDICTION_RP)).getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }

    public String getPredictionPositionTest() {
        if (isInitialized(PREDICTION_TEST)) {
            return ((PredictionZone) predictionLinkedHMap.get(PREDICTION_TEST)).getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }

    public double[] getStandardDistribution() {
        if (isInitialized(PREDICTION_STD)) {
            return ((PredictionZone) predictionLinkedHMap.get(PREDICTION_STD)).getDistribution();
        }
        return null;
    }

    public String[] getStandardClasses() {
        if (isInitialized(PREDICTION_STD)) {
            return ((PredictionZone) predictionLinkedHMap.get(PREDICTION_STD)).getClasses();
        }
        return null;
    }

    public double[] getStandardRssi() {
        if (isInitialized(PREDICTION_STD)) {
            return predictionLinkedHMap.get(PREDICTION_STD).getModifiedRssi();
        }
        return null;
    }

    public List<Double> getDist2Car() {
        List<Double> doubleList = null;
        if (isInitialized(PREDICTION_COORD)) {
            for (int i = 0; i < ((PredictionCoord) predictionLinkedHMap.get(PREDICTION_COORD)).getCoordsSize(); i++) {
                final Coord coordFinal = ((PredictionCoord) predictionLinkedHMap.get(PREDICTION_COORD)).getPredictionCoord(i);
                if (coordFinal != null) {
                    if (doubleList == null) {
                        doubleList = new ArrayList<>();
                    }
                    doubleList.add(CalculusUtils.calculateDist2Car(coordFinal.getCoord_x(), coordFinal.getCoord_y()));
                }
            }
        }
        return doubleList;
    }
}
