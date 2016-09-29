package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class ConnectedCarFactory {
    public final static String TYPE_4_A = "fourLMRB";
    public final static String BASE_1 = "Base_1";
    public final static String BASE_2 = "Base_2";
    public final static String BASE_3 = "Base_3";
    public final static String BASE_4 = "Base_4";
    private final static String TYPE_3_A = "threeLMR";
    private final static String TYPE_3_B = "threeLRB";
    private final static String TYPE_4_B = "fourLRRlRr";
    private final static String TYPE_5_A = "fiveLMRRlRr";
    private final static String TYPE_5_B = "fiveFlFrLRB";
    private final static String TYPE_6_A = "sixLMRRlBRr";
    private final static String TYPE_6_B = "sixFlFrLRRlRr";
    private final static String TYPE_7_A = "sevenFlFrLMRRlRr";
    private final static String TYPE_7_B = "sevenFlFrLRRlBRr";

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        if (carName.equalsIgnoreCase(TYPE_3_A)) {
            return new CCThreeLMR(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_3_B)) {
            return new CCThreeLRB(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_4_A)) {
            return new CCFourLMRB(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_4_B)) {
            return new CCFourLRRlRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_5_A)) {
            return new CCFiveLMRRlRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_5_B)) {
            return new CCFiveFlFrLRB(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_6_A)) {
            return new CCSixLMRRlBRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_6_B)) {
            return new CCSixFlFrLRRlRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_7_A)) {
            return new CCSevenFlFrLMRRlRr(mContext);
        } else if (carName.equalsIgnoreCase(TYPE_7_B)) {
            return new CCSevenFlFrLRRlBRr(mContext);
        } else {
            return new CCFourLMRB(mContext);
        }
    }
}
