package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.machinelearningalgo.prediction.PredictionFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCEightFlFrLMRTRlRr extends ConnectedCar {

    private static final int MAX_X = 11;
    private static final int MAX_Y = 10;
    private static final double THRESHOLD_DIST = 0.25;
    private static double X1 = 3, X2 = 7, Y1 = 4, Y2 = 6;
    private double coord_x, coord_y;

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
    }

    @Override
    public void readPredictionsRawFiles() {
        standardPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_STANDARD);
        pxPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Px);
        pyPrediction = PredictionFactory.getPredictionCoord(mContext, ConnectedCarFactory.TYPE_Py);
        testPrediction = PredictionFactory.getPredictionZone(mContext, PredictionFactory.PREDICTION_TEST);
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
            testPrediction.initTest(rssi);
            testPrediction.predictTest();
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
                && testPrediction != null
                && testPrediction.isPredictRawFileRead()
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
            testPrediction.setRssiTest(rssi);
            testPrediction.predictTest();
//            rpPrediction.setRssi(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone(), lockStatus);
//            rpPrediction.predict(N_VOTE_LONG);
        }
    }

    @Override
    public void calculatePredictionTest(Double threshold) {
        PSALogs.d("abstract", "calculatePredictionTest overrided OK !");
        if (threshold >= 0 && threshold <= 1) {
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
            correctCoord(pxPrediction.getPredictionCoord(), pyPrediction.getPredictionCoord(), THRESHOLD_DIST);
            testPrediction.calculatePredictionTest();
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
            result += testPrediction.printDebugTest(TEST_LOC);
//            result += rpPrediction.printDebug(RP_LOC);
            return result;
        }
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (standardPrediction != null && standardPrediction.isPredictRawFileRead()) {
            String result = standardPrediction.getPrediction();
            return result;
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
                pyPrediction != null && pyPrediction.isPredictRawFileRead()) {
            return new PointF((float) coord_x, (float) coord_y);
        }
        return null;
    }

    public double getDist2Car() {
        if (pxPrediction != null && pxPrediction.isPredictRawFileRead() &&
                pyPrediction != null && pyPrediction.isPredictRawFileRead()) {
            return calculateDist2Car();
        }
        return 0f;
    }

    private double calculateDist2Car() {
        double dist2car;
        if ((coord_x < X1) & (coord_y < Y1)) {
            dist2car = Math.sqrt((coord_x - X1) * (coord_x - X1) + (coord_y - Y1) * (coord_y - Y1));
        } else if ((coord_x < X1) & (coord_y > Y2)) {
            dist2car = Math.sqrt((coord_x - X1) * (coord_x - X1) + (coord_y - Y2) * (coord_y - Y2));
        } else if ((coord_x > X2) & (coord_y < Y1)) {
            dist2car = Math.sqrt((coord_x - X2) * (coord_x - X2) + (coord_y - Y1) * (coord_y - Y1));
        } else if ((coord_x > X2) & (coord_y > Y2)) {
            dist2car = Math.sqrt((coord_x - X2) * (coord_x - X2) + (coord_y - Y2) * (coord_y - Y2));
        } else if ((coord_x < X1) & (coord_y > Y1) & (coord_y < Y2)) {
            dist2car = X1 - coord_x;
        } else if ((coord_x > X2) & (coord_y > Y1) & (coord_y < Y2)) {
            dist2car = coord_x - X2;
        } else if ((coord_y < Y1) & (coord_x > X1) & (coord_x < X2)) {
            dist2car = Y1 - coord_y;
        } else if ((coord_y > Y2) & (coord_x > X1) & (coord_x < X2)) {
            dist2car = coord_y - Y2;
        } else {
            dist2car = -1;
        }
        return dist2car;
    }

    private void correctCoord(double coord_x_new, double coord_y_new, double threshold_dist) {
        double deltaX = coord_x_new - coord_x;
        double deltaY = coord_y_new - coord_y;
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (dist > threshold_dist) {
            double ratio = threshold_dist / dist;
            coord_x = coord_x + deltaX * ratio;
            coord_y = coord_y + deltaY * ratio;
        } else {
            coord_x = coord_x_new;
            coord_y = coord_y_new;
        }
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
}
