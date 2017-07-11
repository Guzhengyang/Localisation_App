package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;

import com.valeo.bleranging.machinelearningalgo.prediction.PredictionCoord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionZone;
import com.valeo.bleranging.model.MultiTrx;

import static com.valeo.bleranging.persistence.Constants.PREDICTION_EAR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RP;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_STD;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.persistence.Constants.STANDARD_LOC;

/**
 * Created by l-avaratha on 05/09/2016
 */
public abstract class ConnectedCar {
    final Handler mHandlerComValidTimeOut = new Handler();
    protected MultiTrx mMultiTrx;
    protected Context mContext;
    PredictionCoord coordPrediction;
    PredictionZone standardPrediction;
    PredictionZone testPrediction;
    PredictionZone earPrediction;
    PredictionZone rpPrediction;
    boolean comValid = false;
    String lastModelUsed = STANDARD_LOC;

    ConnectedCar(Context context) {
        this.mContext = context;
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

    public MultiTrx getMultiTrx() {
        return mMultiTrx;
    }

    /**
     * Create predictions
     */
    public void readPredictionsRawFiles() {
        coordPrediction = PredictionFactory.getPredictionCoord(mContext);
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PREDICTION_STD);
        earPrediction = PredictionFactory.getPredictionZone(mContext, PREDICTION_EAR);
        rpPrediction = PredictionFactory.getPredictionZone(mContext, PREDICTION_RP);
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
