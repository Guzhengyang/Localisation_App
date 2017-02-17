package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import static com.valeo.bleranging.model.connectedcar.ConnectedCar.THATCHAM_ORIENTED;
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

    /**
     * Create and return a prediction
     *
     * @return a prediction
     */
    public static Prediction getPrediction(Context mContext, String predictionType) {
        boolean areInside = SdkPreferencesHelper.getInstance().getAreBeaconsInside();
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        String strategy = SdkPreferencesHelper.getInstance().getOpeningOrientation();
        if (areInside) {
            switch (carType) {
                case TYPE_2_A:
                    return new Prediction(mContext, R.raw.classes_two_start,
                            R.raw.rf_two_start, R.raw.sample_two_start);
                case TYPE_2_B:
                    break;
                case TYPE_3_A:
                    return new Prediction(mContext, R.raw.classes_three,
                            R.raw.rf_three, R.raw.sample_three);
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new Prediction(mContext, R.raw.classes_four_thatcham,
                                        R.raw.rf_four_thatcham, R.raw.sample_four_thatcham);
                            } else {
                                return new Prediction(mContext, R.raw.classes_four_entry,
                                        R.raw.rf_four_entry, R.raw.sample_four_entry);
                            }
                        case PREDICTION_RP:
                            return new Prediction(mContext, R.raw.classes_four_rp,
                                    R.raw.rf_four_rp, R.raw.sample_four_rp);
                        case PREDICTION_EAR:
                            return new Prediction(mContext, R.raw.classes_four_ear,
                                    R.raw.rf_four_ear, R.raw.sample_four_ear);
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new Prediction(mContext, R.raw.classes_eight_thatcham,
                                        R.raw.rf_eight_thatcham, R.raw.sample_eight_thatcham);
                            } else {
                                return new Prediction(mContext, R.raw.classes_eight_entry,
                                        R.raw.rf_eight_entry, R.raw.sample_eight_entry);
                            }
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new Prediction(mContext, R.raw.classes_eight_thatcham,
                                        R.raw.rf_eight_thatcham, R.raw.sample_eight_thatcham);
                            } else {
                                return new Prediction(mContext, R.raw.classes_eight_entry,
                                        R.raw.rf_eight_entry, R.raw.sample_eight_entry);
                            }
                        case PREDICTION_INSIDE:
                            return new Prediction(mContext, R.raw.classes_eight_inside,
                                    R.raw.rf_eight_inside, R.raw.sample_eight_inside);
                        case PREDICTION_RP:
                            return new Prediction(mContext, R.raw.classes_eight_rp,
                                    R.raw.rf_eight_rp, R.raw.sample_eight_rp);
                    }
                    break;
            }
        } else {
            switch (carType) {
                case TYPE_2_A:
                    return new Prediction(mContext, R.raw.classes_two_start,
                            R.raw.rf_two_start, R.raw.sample_two_start);
                case TYPE_2_B:
                    break;
                case TYPE_3_A:
                    return new Prediction(mContext, R.raw.classes_three,
                            R.raw.rf_three, R.raw.sample_three);
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new Prediction(mContext, R.raw.classes_four_thatcham,
                                        R.raw.rf_four_thatcham, R.raw.sample_four_thatcham);
                            } else {
                                return new Prediction(mContext, R.raw.classes_four_entry,
                                        R.raw.rf_four_entry, R.raw.sample_four_entry);
                            }
                        case PREDICTION_RP:
                            return new Prediction(mContext, R.raw.classes_four_rp,
                                    R.raw.rf_four_rp, R.raw.sample_four_rp);
                        case PREDICTION_EAR:
                            return new Prediction(mContext, R.raw.classes_four_ear,
                                    R.raw.rf_four_ear, R.raw.sample_four_ear);
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new Prediction(mContext, R.raw.classes_eight_thatcham,
                                        R.raw.rf_eight_thatcham, R.raw.sample_eight_thatcham);
                            } else {
                                return new Prediction(mContext, R.raw.classes_eight_entry,
                                        R.raw.rf_eight_entry, R.raw.sample_eight_entry);
                            }
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(THATCHAM_ORIENTED)) {
                                return new Prediction(mContext, R.raw.classes_eight_thatcham,
                                        R.raw.rf_eight_thatcham, R.raw.sample_eight_thatcham);
                            } else {
                                return new Prediction(mContext, R.raw.classes_eight_entry,
                                        R.raw.rf_eight_entry, R.raw.sample_eight_entry);
                            }
                        case PREDICTION_INSIDE:
                            return new Prediction(mContext, R.raw.classes_eight_inside,
                                    R.raw.rf_eight_inside, R.raw.sample_eight_inside);
                        case PREDICTION_RP:
                            return new Prediction(mContext, R.raw.classes_eight_rp,
                                    R.raw.rf_eight_rp, R.raw.sample_eight_rp);
                    }
                    break;
            }
        }
        return null;
    }
}
