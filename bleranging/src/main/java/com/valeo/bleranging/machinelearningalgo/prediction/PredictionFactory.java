package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

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
    public static PredictionZone getPrediction(Context mContext, String predictionType) {
        boolean areInside = SdkPreferencesHelper.getInstance().getAreBeaconsInside();
        String carType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        String strategy = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        boolean ifRoof = SdkPreferencesHelper.getInstance().isPrintRooftopEnabled();
        boolean ifMiniPrediction = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        if (areInside) {
            switch (carType) {
                case TYPE_2_A:
                    return new PredictionZone(mContext, R.raw.two_a_in);
                case TYPE_2_B:
                    return new PredictionZone(mContext, R.raw.two_b_in);
                case TYPE_3_A:
                    return new PredictionZone(mContext, R.raw.three_in);
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, R.raw.four_in);
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, R.raw.four_in);
                        case PREDICTION_EAR:
                            return new PredictionZone(mContext, R.raw.four_in);
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, R.raw.six_in);
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, R.raw.eight_in_thatcham_new);
                            } else {
                                return new PredictionZone(mContext, R.raw.eight_in_new);
                            }
                        case PREDICTION_INSIDE:
                            return new PredictionZone(mContext, R.raw.eight_in);
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, R.raw.eight_in);
                    }
                    break;
            }
        } else {
            switch (carType) {
                case TYPE_2_A:
                    return new PredictionZone(mContext, R.raw.two_a_out);
                case TYPE_2_B:
                    return new PredictionZone(mContext, R.raw.two_b_out);
                case TYPE_3_A:
                    return new PredictionZone(mContext, R.raw.three_out);
                case TYPE_4_A:
                    break;
                case TYPE_4_B:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, R.raw.four_out_thatcham);
                            } else {
                                return new PredictionZone(mContext, R.raw.four_out);
                            }
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, R.raw.four_out);
                        case PREDICTION_EAR:
                            return new PredictionZone(mContext, R.raw.four_out);
                    }
                    break;
                case TYPE_5_A:
                    break;
                case TYPE_6_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            return new PredictionZone(mContext, R.raw.six_out);
                    }
                    break;
                case TYPE_7_A:
                    break;
                case TYPE_8_A:
                    switch (predictionType) {
                        case PREDICTION_STANDARD:
                            if (strategy.equalsIgnoreCase(ConnectedCar.THATCHAM_ORIENTED)) {
                                return new PredictionZone(mContext, R.raw.eight_out_thatcham);
                            } else {
                                return new PredictionZone(mContext, R.raw.eight_out);
                            }
                        case PREDICTION_INSIDE:
                            return new PredictionZone(mContext, R.raw.eight_out);
                        case PREDICTION_RP:
                            return new PredictionZone(mContext, R.raw.eight_out);
                    }
                    break;
            }
        }
        return null;
    }
}
