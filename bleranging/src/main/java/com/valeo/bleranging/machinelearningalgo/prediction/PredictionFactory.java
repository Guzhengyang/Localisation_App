package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;

import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_2_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_2_B;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_3_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_4_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_4_B;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_5_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_6_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_7_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_8_A;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_Clf;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_Px;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.TYPE_Py;

/**
 * Created by l-avaratha on 17/02/2017
 */

public class PredictionFactory {
    public final static String PREDICTION_STANDARD = "standard_prediction";
    public final static String PREDICTION_INSIDE = "inside_prediction";
    public final static String PREDICTION_RP = "rp_prediction";
    public final static String PREDICTION_EAR = "ear_prediction";
    //    models for 2 beacons
    private final static String ZONE_2_A = "Two";

    //    models for 4 beacons
    private final static String ZONE_4_B_START = "FourStart";
    private final static String ZONE_4_B = "Four";
    private final static String ZONE_4_B_THATCHAM = "FourThatcham";
    private final static String ZONE_4_B_RP = "FourRP";

    //    models for 8 beacons
    private final static String ZONE_8_A_NORMAL = "EightNormal";
    private final static String ZONE_8_A_THATCHAM = "EightThatcham";
    private final static String COORD_8_A_PX = "MLP4Px";
    private final static String COORD_8_A_PY = "MLP4Py";
    private final static String ZONE_8_A_RP = "EightRP";
    private final static String COORD_8_A_CLF = "MLP4Clf";


    /**
     * Create and return a coord prediction
     *
     * @return a coord prediction
     */
    public static PredictionCoord getPredictionCoord(Context mContext, String modelType) {
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        if (carType.equalsIgnoreCase(TYPE_8_A)) {
            if (modelType.equalsIgnoreCase(TYPE_Clf)) {
                return new PredictionCoord(mContext, COORD_8_A_CLF, rowDataKeySetFactory(TYPE_8_A));
            } else if (modelType.equalsIgnoreCase(TYPE_Px)) {
                return new PredictionCoord(mContext, COORD_8_A_PX, rowDataKeySetFactory(TYPE_8_A));
            } else if (modelType.equalsIgnoreCase(TYPE_Py)) {
                return new PredictionCoord(mContext, COORD_8_A_PY, rowDataKeySetFactory(TYPE_8_A));
            }
        }
        return null;
    }

    /**
     * Create and return a zone prediction
     *
     * @return a zone prediction
     */
    public static PredictionZone getPredictionZone(Context mContext, String predictionType) {
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        String strategy = SdkPreferencesHelper.getInstance().getOpeningStrategy();
//        boolean ifRoof = SdkPreferencesHelper.getInstance().isPrintRooftopEnabled();
//        boolean ifMiniPrediction = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
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
                    case PREDICTION_STANDARD:
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
                    case PREDICTION_STANDARD:
                        if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                            return new PredictionZone(mContext, ZONE_8_A_THATCHAM, rowDataKeySetFactory(TYPE_8_A), predictionType);
                        } else {
                            return new PredictionZone(mContext, ZONE_8_A_NORMAL, rowDataKeySetFactory(TYPE_8_A), predictionType);
                        }
                    case PREDICTION_RP:
                        return new PredictionZone(mContext, ZONE_8_A_NORMAL, rowDataKeySetFactory(TYPE_8_A), predictionType);
                }
                break;
        }
        return null;
    }

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
