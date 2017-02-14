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
    public final static String TYPE_2_A = "twoMT";
    public final static String TYPE_2_B = "twoLR";
    public final static String TYPE_3_A = "threeLMR";
    public final static String TYPE_4_A = "fourLMRB";
    public final static String TYPE_4_B = "fourLMRT";
    public final static String TYPE_5_A = "fiveLMRTB";
    public final static String TYPE_6_A = "sixFlFrMTRlRr";
    public final static String TYPE_7_A = "sevenFlFrLMRRlRr";
    public final static String TYPE_8_A = "eightFlFrLMRTRlRr";

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        if (carName.equalsIgnoreCase(TYPE_2_A)) {
            return new CCTwoMT(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_2_B)) {
            return new CCTwoLR(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_3_A)) {
            return new CCThreeLMR(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_4_A)) {
            return new CCFourLMRB(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_4_B)) {
            return new CCFourLMRT(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_5_A)) {
            return new CCFiveLMRTB(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_6_A)) {
            return new CCSixFlFrMTRlRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_7_A)) {
            return new CCSevenFlFrLMRRlRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_8_A)) {
            return new CCEightFlFrLMRTRlRr(mContext);
        } else {
            return new CCFourLMRB(mContext);
        }
    }
}
