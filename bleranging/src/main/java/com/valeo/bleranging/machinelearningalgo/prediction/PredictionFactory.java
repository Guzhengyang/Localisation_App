package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import static com.valeo.bleranging.persistence.Constants.COORD_8_A_PX;
import static com.valeo.bleranging.persistence.Constants.COORD_8_A_PY;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RP;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_STD;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_TEST;
import static com.valeo.bleranging.persistence.Constants.THATCHAM_ORIENTED;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_3_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_4_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_4_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_5_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_6_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_7_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_8_A;
import static com.valeo.bleranging.persistence.Constants.ZONE_2_A;
import static com.valeo.bleranging.persistence.Constants.ZONE_4_B_RP;
import static com.valeo.bleranging.persistence.Constants.ZONE_4_B_START;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_NORMAL;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_NORMAL_MINI;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_NORMAL_TEST;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_RP;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_THATCHAM;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_THATCHAM_MINI;
import static com.valeo.bleranging.persistence.Constants.ZONE_8_A_THATCHAM_TEST;

/**
 * Created by l-avaratha on 17/02/2017
 */

public class PredictionFactory {

    /***
     *
     * @param mContext
     * @return Coordinate Prediction
     */
    public static PredictionCoord getPredictionCoord(Context mContext) {
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        if (carType.equalsIgnoreCase(TYPE_8_A)) {
            return new PredictionCoord(mContext, COORD_8_A_PX, COORD_8_A_PY, rowDataKeySetFactory(TYPE_8_A));
        }
        return null;
    }

    /***
     *
     * @param mContext
     * @param predictionType  standard prediction for zones(left, right, lock, start, ...) and rp prediction(near or far)
     * @return
     */
    public static PredictionZone getPredictionZone(Context mContext, String predictionType) {
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        String strategy = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        boolean miniPrediction = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        switch (carType) {
            case TYPE_2_A:
                return new PredictionZone(mContext, ZONE_2_A, rowDataKeySetFactory(TYPE_2_A), predictionType);
            case TYPE_2_B:
                break;
            case TYPE_3_A:
                break;
            case TYPE_4_A:
                break;
            case TYPE_4_B:
                switch (predictionType) {
                    case PREDICTION_STD:
                        return new PredictionZone(mContext, ZONE_4_B_START, rowDataKeySetFactory(TYPE_4_B), predictionType);
                    case PREDICTION_RP:
                        return new PredictionZone(mContext, ZONE_4_B_RP, rowDataKeySetFactory(TYPE_4_B), predictionType);
                }
                break;
            case TYPE_5_A:
                break;
            case TYPE_6_A:
                break;
            case TYPE_7_A:
                break;
            case TYPE_8_A:
                switch (predictionType) {
                    case PREDICTION_STD:
                        if (miniPrediction) {
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, ZONE_8_A_THATCHAM_MINI, rowDataKeySetFactory(TYPE_8_A), predictionType);
                            } else {
                                return new PredictionZone(mContext, ZONE_8_A_NORMAL_MINI, rowDataKeySetFactory(TYPE_8_A), predictionType);
                            }
                        } else {
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, ZONE_8_A_THATCHAM, rowDataKeySetFactory(TYPE_8_A), predictionType);
                            } else {
                                return new PredictionZone(mContext, ZONE_8_A_NORMAL, rowDataKeySetFactory(TYPE_8_A), predictionType);
                            }
                        }
                    case PREDICTION_TEST:
                        if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                            return new PredictionZone(mContext, ZONE_8_A_THATCHAM_TEST, rowDataKeySetFactory(TYPE_8_A), predictionType);
                        } else {
                            return new PredictionZone(mContext, ZONE_8_A_NORMAL_TEST, rowDataKeySetFactory(TYPE_8_A), predictionType);
                        }
                    case PREDICTION_RP:
                        return new PredictionZone(mContext, ZONE_8_A_RP, rowDataKeySetFactory(TYPE_8_A), predictionType);
                }
                break;
        }
        return null;
    }

    /**
     * @param carType type of beacons to be used
     * @return list for entry of ML algo
     */
    private static List<String> rowDataKeySetFactory(String carType) {
        List<String> rowDataKeySet = new ArrayList<>();
        switch (carType) {
            case TYPE_2_A:
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI TRUNK_ORIGIN");
                break;
            case TYPE_2_B:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                break;
            case TYPE_3_A:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                break;
            case TYPE_4_A:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                rowDataKeySet.add("RSSI BACK_ORIGIN");
                break;
            case TYPE_4_B:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                rowDataKeySet.add("RSSI TRUNK_ORIGIN");
                break;
            case TYPE_5_A:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                rowDataKeySet.add("RSSI TRUNK_ORIGIN");
                rowDataKeySet.add("RSSI BACK_ORIGIN");
                break;
            case TYPE_6_A:
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI TRUNK_ORIGIN");
                rowDataKeySet.add("RSSI FRONTLEFT_ORIGIN");
                rowDataKeySet.add("RSSI FRONTRIGHT_ORIGIN");
                rowDataKeySet.add("RSSI REARLEFT_ORIGIN");
                rowDataKeySet.add("RSSI REARRIGHT_ORIGIN");
                break;
            case TYPE_7_A:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                rowDataKeySet.add("RSSI FRONTLEFT_ORIGIN");
                rowDataKeySet.add("RSSI FRONTRIGHT_ORIGIN");
                rowDataKeySet.add("RSSI REARLEFT_ORIGIN");
                rowDataKeySet.add("RSSI REARRIGHT_ORIGIN");
                break;
            case TYPE_8_A:
                rowDataKeySet.add("RSSI LEFT_ORIGIN");
                rowDataKeySet.add("RSSI MIDDLE_ORIGIN");
                rowDataKeySet.add("RSSI RIGHT_ORIGIN");
                rowDataKeySet.add("RSSI TRUNK_ORIGIN");
                rowDataKeySet.add("RSSI FRONTLEFT_ORIGIN");
                rowDataKeySet.add("RSSI FRONTRIGHT_ORIGIN");
                rowDataKeySet.add("RSSI REARLEFT_ORIGIN");
                rowDataKeySet.add("RSSI REARRIGHT_ORIGIN");
                break;
        }
        return rowDataKeySet;
    }
}
