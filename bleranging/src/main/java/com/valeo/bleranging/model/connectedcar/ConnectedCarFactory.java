package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class ConnectedCarFactory {
    public final static String BASE_1 = "Base_1";
    public final static String BASE_2 = "Base_2";
    public final static String BASE_3 = "Base_3";
    public final static String BASE_4 = "Base_4";
    public final static String TYPE_4_A = "fourLMRB";
    public final static String TYPE_4_B = "fourLMRT";
    public final static String TYPE_5_A = "fiveLMRTB";
    public final static String TYPE_7_A = "sevenFlFrLMRRlRr";
    public final static String TYPE_8_A = "eightFlFrLMRTRlRr";
    public final static String ALGO_STANDARD = "ALGO_STANDARD";
    public final static String MACHINE_LEARNING = "MACHINE_LEARNING";
    public final static String DOUBLE_ALGO = "DOUBLE_ALGO";
    public final static String MODEL_RF = "RANDOM_FOREST";
    public final static String MODEL_LOGISTIC = "LOGISTIC";

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName, boolean isIndoor) {
        if (carName.equalsIgnoreCase(TYPE_4_A)) {
            return new CCFourLMRB(mContext, isIndoor);
        } else if (carName.equalsIgnoreCase(TYPE_4_B)) {
            return new CCFourLMRT(mContext, isIndoor);
        } else if (carName.equalsIgnoreCase(TYPE_5_A)) {
            return new CCFiveLMRTB(mContext, isIndoor);
        } else if (carName.equalsIgnoreCase(TYPE_7_A)) {
            return new CCSevenFlFrLMRRlRr(mContext, isIndoor);
        } else if (carName.equalsIgnoreCase(TYPE_8_A)) {
            return new CCEightFlFrLMRTRlRr(mContext, isIndoor);
        } else {
            return new CCFourLMRB(mContext, isIndoor);
        }
    }
}
