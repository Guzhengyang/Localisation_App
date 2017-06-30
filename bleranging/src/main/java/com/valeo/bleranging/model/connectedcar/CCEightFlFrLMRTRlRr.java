package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.Coord;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import org.ejml.simple.SimpleMatrix;
import com.valeo.bleranging.utils.CalculUtils;

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
        trxLinkedHMap = new ConnectedCarFactory.TrxLinkHMapBuilder()
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
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_STANDARD);
        pxPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Px);
        pyPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Py);
//        testPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_TEST);
//        rpPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_RP);
    }

    @Override
    public void initPredictions() {
        if (isInitialized()) {
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);
            pxPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            pyPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            coord_x = pxPrediction.getPredictionCoord();
            coord_y = pyPrediction.getPredictionCoord();
//            testPrediction.initTest(rssi);
//            testPrediction.predictTest();
            coord = new Coord(pxPrediction.getPredictionCoord(), pyPrediction.getPredictionCoord());
//            rpPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public boolean isInitialized() {
        return (standardPrediction != null
                && standardPrediction.isPredictRawFileRead()
                && pxPrediction != null
                && pxPrediction.isPredictRawFileRead()
                && pyPrediction != null
                && pyPrediction.isPredictRawFileRead()
//                && testPrediction != null
//                && testPrediction.isPredictRawFileRead()
//                && rpPrediction != null
//                && rpPrediction.isPredictRawFileRead()
                && (checkForRssiNonNull(rssi) != null));
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {
        if (isInitialized()) {
            standardPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
            standardPrediction.predict(N_VOTE_SHORT);
            pxPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            pyPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//            testPrediction.setRssiTest(rssi);
//            testPrediction.predictTest();
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
            pxPrediction.calculatePredictionCoord();
            pyPrediction.calculatePredictionCoord();
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
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    public String getPredictionPositionTest() {
        if (testPrediction != null && testPrediction.isPredictRawFileRead()) {
            String result = testPrediction.getPrediction();
            return result;
        }
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    public PointF getPredictionCoord() {
        if (pxPrediction != null && pxPrediction.isPredictRawFileRead() &&
                pyPrediction != null && pyPrediction.isPredictRawFileRead() && coord != null) {
            return new PointF((float) coord.getCoord_x(), (float) coord.getCoord_y());
        }
        return null;
    }

    public double getDist2Car() {
        if (pxPrediction != null && pxPrediction.isPredictRawFileRead() &&
                pyPrediction != null && pyPrediction.isPredictRawFileRead() && coord != null) {
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
