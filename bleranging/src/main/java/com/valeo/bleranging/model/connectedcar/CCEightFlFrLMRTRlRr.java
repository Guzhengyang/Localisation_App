package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.machinelearningalgo.prediction.Coord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.model.MultiTrx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import org.ejml.simple.SimpleMatrix;
import com.valeo.bleranging.utils.CalculUtils;

import static com.valeo.bleranging.persistence.Constants.FULL_LOC;
import static com.valeo.bleranging.persistence.Constants.N_VOTE_SHORT;
import static com.valeo.bleranging.persistence.Constants.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_STD;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.persistence.Constants.THATCHAM_ORIENTED;
import static com.valeo.bleranging.persistence.Constants.THRESHOLD_PROB_LOCK2UNLOCK;
import static com.valeo.bleranging.persistence.Constants.THRESHOLD_PROB_UNLOCK2LOCK;
import static com.valeo.bleranging.utils.CheckUtils.checkForRssiNonNull;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCEightFlFrLMRTRlRr extends ConnectedCar {
    private static final int MAX_X = 11;
    private static final int MAX_Y = 10;
    private static final double THRESHOLD_DIST = 0.25;
    private static double X1 = 3, X2 = 7, Y1 = 4, Y2 = 6;
    private double coord_x, coord_y;
    private double dt = 0.105;
    private SimpleMatrix X, P, F, G, H, Q, R, Z, K;
    private Coord coord = null;

    public CCEightFlFrLMRTRlRr(Context mContext) {
        super(mContext);
        mMultiTrx = new MultiTrx(new ConnectedCarFactory.TrxLinkHMapBuilder()
                .left()
                .middle()
                .right()
                .trunk()
                .frontLeft()
                .frontRight()
                .rearleft()
                .rearRight()
                .build();
        initMatrix();
    }

    public void initMatrix() {
        X = new SimpleMatrix(new double[][]{{0}, {0}, {0}, {0}});
        P = new SimpleMatrix(SimpleMatrix.identity(X.numRows()));
        F = new SimpleMatrix(new double[][]{{1, dt, 0, 0}, {0, 1, 0, 1}, {0, 0, 1, dt}, {0, 0, 0, 1}});
        G = new SimpleMatrix(new double[][]{{dt * dt / 2, 0}, {dt, 0}, {0, dt * dt / 2}, {0, dt}});
        H = new SimpleMatrix(new double[][]{{1, 0, 0, 0}, {0, 0, 1, 0}});
        Q = new SimpleMatrix(SimpleMatrix.identity(2));
        R = new SimpleMatrix(SimpleMatrix.identity(H.numRows()));
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PREDICTION_STD);
        coordPrediction = PredictionFactory.getPredictionCoord(mContext);
//        rpPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_RP);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(mMultiTrx.getRssiTab(), SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);
            coordPrediction.init(mMultiTrx.getRssiTab(), SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            coord = new Coord(coordPrediction.getPredictionCoord());
//            rpPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return (standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
                && coordPrediction != null
                && coordPrediction.isPredictRawFileRead()
//                && rpPrediction != null
//                && rpPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(mMultiTrx.getRssiTab()) != null));
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            standardPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
            standardPrediction.predict(N_VOTE_SHORT);
            coordPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            rpPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
//            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePredictionTest(Double threshold) {
        PSALogs.d("abstract", "calculatePredictionTest overrided OK !");
        if (testPrediction != null && threshold >= 0 && threshold <= 1) {
            testPrediction.setThreshold(threshold);
        }
    }

    @Override
    public void calculatePrediction(float[] orientation) {

        if (isInitialized()) {
            if (SdkPreferencesHelper.getInstance().getOpeningStrategy().equalsIgnoreCase(THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                        THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningStrategy().equalsIgnoreCase(PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(SdkPreferencesHelper.getInstance().getThresholdProbStandard(),
                        THRESHOLD_PROB_LOCK2UNLOCK, THRESHOLD_PROB_UNLOCK2LOCK, PASSIVE_ENTRY_ORIENTED);
            }
            coordPrediction.calculatePredictionCoord();
            CalculUtils.correctCoord(coord, coordPrediction.getPredictionCoord(), THRESHOLD_DIST);
//            correctCoordThreshold(pxPrediction.getPredictionCoord(), pyPrediction.getPredictionCoord(), THRESHOLD_DIST);
            correctCoordKalman(pxPrediction.getPredictionCoord(), pyPrediction.getPredictionCoord());
            correctBoundry();
//            testPrediction.calculatePredictionTest();
            CalculUtils.correctCoord(coord, pxPrediction.getPredictionCoord(), pyPrediction.getPredictionCoord(), THRESHOLD_DIST);
//            rpPrediction.calculatePredictionRP(SdkPreferencesHelper.getInstance().getThresholdProbStandard());
        }
    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        if (isInitialized()) {
            String result = "";
            result += SdkPreferencesHelper.getInstance().getOpeningStrategy() + "\n";
            result += "----------------------------------\n";
            result += standardPrediction.printDebug(STANDARD_LOC);
//            result += testPrediction.printDebugTest(TEST_LOC);
//            result += rpPrediction.printDebug(RP_LOC);
            return result;
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (standardPrediction != null && standardPrediction.isPredictRawFileRead()) {
            return standardPrediction.getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }

    public String getPredictionPositionTest() {
        if (testPrediction != null && testPrediction.isPredictRawFileRead()) {
            String result = testPrediction.getPrediction();
            return result;
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    public PointF getPredictionCoord() {
        if (coordPrediction != null && coordPrediction.isPredictRawFileRead() && coord != null) {
            return new PointF((float) coord.getCoord_x(), (float) coord.getCoord_y());
        }
        return null;
    }

    public double getDist2Car() {
        if (coordPrediction != null && coordPrediction.isPredictRawFileRead() && coord != null) {
            return CalculUtils.calculateDist2Car(coord.getCoord_x(), coord.getCoord_y());
        }
        return 0f;
    }

    public void correctBoundry() {
        if (coord_x > MAX_X) {
            coord_x = MAX_X;
        } else if (coord_x < 0) {
            coord_x = 0;
        }
        if (coord_y > MAX_Y) {
            coord_y = MAX_Y;
        } else if (coord_y < 0) {
            coord_y = 0;
        }
    }

    public void correctCoordKalman(double coord_x_new, double coord_y_new) {
        Z = new SimpleMatrix(new double[][]{{coord_x_new}, {coord_y_new}});
        X = F.mult(X);
        P = F.mult(P.mult(F.transpose())).plus(G.mult(Q.mult(G.transpose())));
        K = P.mult(H.transpose()).mult(H.mult(P).mult(H.transpose()).plus(R).invert());
        X = X.plus(K.mult(Z.minus(H.mult(X))));
        P = P.minus(K.mult(H.mult(P)));
        coord_x = X.get(0);
        coord_y = X.get(2);
    }
}
