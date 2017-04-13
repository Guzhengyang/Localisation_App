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

/**
 * Created by l-avaratha on 17/02/2017
 */

public class PredictionFactory {
    public final static String PREDICTION_STANDARD = "standard_prediction";
    public final static String PREDICTION_INSIDE = "inside_prediction";
    public final static String PREDICTION_RP = "rp_prediction";
    public final static String PREDICTION_EAR = "ear_prediction";
    private final static String ZONE_H2O_2_A_IN = "TwoIn";
    private final static String ZONE_H2O_2_B_IN = "TwoIn";
    private final static String ZONE_H2O_3_A_IN = "ThreeIn";
    private final static String ZONE_H2O_4_A_IN = "FourIn";
    private final static String ZONE_H2O_4_B_IN = "FourIn";
    private final static String ZONE_H2O_5_A_IN = "FiveIn";
    private final static String ZONE_H2O_6_A_IN = "SixIn";
    private final static String ZONE_H2O_7_A_IN = "SevenIn";
    private final static String ZONE_H2O_8_A_IN = "EightIn";
    private final static String ZONE_H2O_8_A_IN_THATCHAM = "EightInThatcham";
    private final static String ZONE_H2O_2_A_OUT = "TwoOut";
    private final static String ZONE_H2O_2_B_OUT = "TwoOut";
    private final static String ZONE_H2O_3_A_OUT = "ThreeOut";
    private final static String ZONE_H2O_4_A_OUT = "FourOut";
    private final static String ZONE_H2O_4_B_OUT = "FourOut";
    private final static String ZONE_H2O_5_A_OUT = "FiveOut";
    private final static String ZONE_H2O_6_A_OUT = "SixOut";
    private final static String ZONE_H2O_7_A_OUT = "SevenOut";
    private final static String ZONE_H2O_8_A_OUT = "EightOut";
    private final static String ZONE_H2O_8_A_OUT_THATCHAM = "EightOutThatcham";
    private final static String COORD_H2O_2_A_IN = "TwoIn";
    private final static String COORD_H2O_2_B_IN = "TwoIn";
    private final static String COORD_H2O_3_A_IN = "ThreeIn";
    private final static String COORD_H2O_4_A_IN = "FourIn";
    private final static String COORD_H2O_4_B_IN = "FourIn";
    private final static String COORD_H2O_5_A_IN = "FiveIn";
    private final static String COORD_H2O_6_A_IN = "SixIn";
    private final static String COORD_H2O_7_A_IN = "SevenIn";
    private final static String COORD_H2O_8_A_IN = "EightIn";
    private final static String COORD_H2O_8_A_IN_THATCHAM = "EightInThatcham";
    private final static String COORD_H2O_2_A_OUT = "TwoOut";
    private final static String COORD_H2O_2_B_OUT = "TwoOut";
    private final static String COORD_H2O_3_A_OUT = "ThreeOut";
    private final static String COORD_H2O_4_A_OUT = "FourOut";
    private final static String COORD_H2O_4_B_OUT = "FourOut";
    private final static String COORD_H2O_5_A_OUT = "FiveOut";
    private final static String COORD_H2O_6_A_OUT = "SixOut";
    private final static String COORD_H2O_7_A_OUT = "SevenOut";
    private final static String COORD_H2O_8_A_OUT = "EightOut";
    private final static String COORD_H2O_8_A_OUT_THATCHAM = "EightOutThatcham";

    /**
     * Create and return a coord prediction
     *
     * @return a coord prediction
     */
    public static PredictionCoord getPredictionCoord(Context mContext, String predictionType) {
        boolean areInside = SdkPreferencesHelper.getInstance().getAreBeaconsInside();
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        String strategy = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        boolean ifRoof = SdkPreferencesHelper.getInstance().isPrintRooftopEnabled();
        boolean ifMiniPrediction = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        if (areInside) {
            switch (carType) {
                case TYPE_2_A:
                    return new PredictionCoord(mContext, COORD_H2O_2_A_IN, rowDataKeySetFactory(TYPE_2_A));
                case TYPE_2_B:
                    return new PredictionCoord(mContext, COORD_H2O_2_B_IN, rowDataKeySetFactory(TYPE_2_B));
                case TYPE_3_A:
                    return new PredictionCoord(mContext, COORD_H2O_3_A_IN, rowDataKeySetFactory(TYPE_3_A));
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionCoord(mContext, COORD_H2O_4_B_IN, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_RP:
                            return new PredictionCoord(mContext, COORD_H2O_4_B_IN, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_EAR:
                            return new PredictionCoord(mContext, COORD_H2O_4_B_IN, rowDataKeySetFactory(TYPE_4_B));
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionCoord(mContext, COORD_H2O_6_A_IN, rowDataKeySetFactory(TYPE_6_A));
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionCoord(mContext, COORD_H2O_8_A_IN_THATCHAM, rowDataKeySetFactory(TYPE_8_A));
                            } else {
                                return new PredictionCoord(mContext, COORD_H2O_8_A_IN, rowDataKeySetFactory(TYPE_8_A));
                            }
                        case PREDICTION_INSIDE:
                            return new PredictionCoord(mContext, COORD_H2O_8_A_IN, rowDataKeySetFactory(TYPE_8_A));
                        case PREDICTION_RP:
                            return new PredictionCoord(mContext, COORD_H2O_8_A_IN, rowDataKeySetFactory(TYPE_8_A));
                    }
                    break;
            }
        } else {
            switch (carType) {
                case TYPE_2_A:
                    return new PredictionCoord(mContext, COORD_H2O_2_A_OUT, rowDataKeySetFactory(TYPE_2_A));
                case TYPE_2_B:
                    return new PredictionCoord(mContext, COORD_H2O_2_B_OUT, rowDataKeySetFactory(TYPE_2_B));
                case TYPE_3_A:
                    return new PredictionCoord(mContext, COORD_H2O_3_A_OUT, rowDataKeySetFactory(TYPE_3_A));
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionCoord(mContext, COORD_H2O_4_B_OUT, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_RP:
                            return new PredictionCoord(mContext, COORD_H2O_4_B_OUT, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_EAR:
                            return new PredictionCoord(mContext, COORD_H2O_4_B_OUT, rowDataKeySetFactory(TYPE_4_B));
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionCoord(mContext, COORD_H2O_6_A_OUT, rowDataKeySetFactory(TYPE_6_A));
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionCoord(mContext, COORD_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                            } else {
                                return new PredictionCoord(mContext, COORD_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                            }
                        case PREDICTION_INSIDE:
                            return new PredictionCoord(mContext, COORD_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                        case PREDICTION_RP:
                            return new PredictionCoord(mContext, COORD_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                    }
                    break;
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
        boolean areInside = SdkPreferencesHelper.getInstance().getAreBeaconsInside();
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        String strategy = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        boolean ifRoof = SdkPreferencesHelper.getInstance().isPrintRooftopEnabled();
        boolean ifMiniPrediction = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        if (areInside) {
            switch (carType) {
                case TYPE_2_A:
                    return new PredictionZone(mContext, ZONE_H2O_2_A_IN, rowDataKeySetFactory(TYPE_2_A));
                case TYPE_2_B:
                    return new PredictionZone(mContext, ZONE_H2O_2_B_IN, rowDataKeySetFactory(TYPE_2_B));
                case TYPE_3_A:
                    return new PredictionZone(mContext, ZONE_H2O_3_A_IN, rowDataKeySetFactory(TYPE_3_A));
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, ZONE_H2O_4_B_IN, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, ZONE_H2O_4_B_IN, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_EAR:
                            return new PredictionZone(mContext, ZONE_H2O_4_B_IN, rowDataKeySetFactory(TYPE_4_B));
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, ZONE_H2O_6_A_IN, rowDataKeySetFactory(TYPE_6_A));
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, ZONE_H2O_8_A_IN_THATCHAM, rowDataKeySetFactory(TYPE_8_A));
                            } else {
                                return new PredictionZone(mContext, ZONE_H2O_8_A_IN, rowDataKeySetFactory(TYPE_8_A));
                            }
                        case PREDICTION_INSIDE:
                            return new PredictionZone(mContext, ZONE_H2O_8_A_IN, rowDataKeySetFactory(TYPE_8_A));
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, ZONE_H2O_8_A_IN, rowDataKeySetFactory(TYPE_8_A));
                    }
                    break;
            }
        } else {
            switch (carType) {
                case TYPE_2_A:
                    return new PredictionZone(mContext, ZONE_H2O_2_A_OUT, rowDataKeySetFactory(TYPE_2_A));
                case TYPE_2_B:
                    return new PredictionZone(mContext, ZONE_H2O_2_B_OUT, rowDataKeySetFactory(TYPE_2_B));
                case TYPE_3_A:
                    return new PredictionZone(mContext, ZONE_H2O_3_A_OUT, rowDataKeySetFactory(TYPE_3_A));
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, ZONE_H2O_4_B_OUT, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, ZONE_H2O_4_B_OUT, rowDataKeySetFactory(TYPE_4_B));
                        case PREDICTION_EAR:
                            return new PredictionZone(mContext, ZONE_H2O_4_B_OUT, rowDataKeySetFactory(TYPE_4_B));
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, ZONE_H2O_6_A_OUT, rowDataKeySetFactory(TYPE_6_A));
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, ZONE_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                            } else {
                                return new PredictionZone(mContext, ZONE_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                            }
                        case PREDICTION_INSIDE:
                            return new PredictionZone(mContext, ZONE_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, ZONE_H2O_8_A_OUT, rowDataKeySetFactory(TYPE_8_A));
                    }
                    break;
            }
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
