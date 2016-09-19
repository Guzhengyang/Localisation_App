package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class ConnectedCarFactory {
    public final static String TYPE_3_A = "threeLMR";
    public final static String TYPE_3_B = "threeLRB";
    public final static String TYPE_4_A = "fourLMRB";
    public final static String TYPE_4_B = "fourLRRlRr";
    public final static String TYPE_5_A = "fiveLMRRlRr";
    public final static String TYPE_5_B = "fiveFlFrLRB";
    public final static String TYPE_6_A = "sixLMRRlBRr";
    public final static String TYPE_6_B = "sixFlFrLRRlRr";
    public final static String TYPE_7_A = "sevenFlFrLMRRlRr";
    public final static String TYPE_7_B = "sevenFlFrLRRlBRr";

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        if (carName.equalsIgnoreCase(TYPE_3_A)) {
            return new CCThreeLMR(mContext, ConnectedCar.ConnectionNumber.THREE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_3_B)) {
            return new CCThreeLRB(mContext, ConnectedCar.ConnectionNumber.THREE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_4_A)) {
            return new CCFourLMRB(mContext, ConnectedCar.ConnectionNumber.FOUR_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_4_B)) {
            return new CCFourLRRlRr(mContext, ConnectedCar.ConnectionNumber.FOUR_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_5_A)) {
            return new CCFiveLMRRlRr(mContext, ConnectedCar.ConnectionNumber.FIVE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_5_B)) {
            return new CCFiveFlFrLRB(mContext, ConnectedCar.ConnectionNumber.FIVE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_6_A)) {
            return new CCSixLMRRlBRr(mContext, ConnectedCar.ConnectionNumber.SIX_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_6_B)) {
            return new CCSixFlFrLRRlRr(mContext, ConnectedCar.ConnectionNumber.SIX_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_7_A)) {
            return new CCSevenFlFrLMRRlRr(mContext, ConnectedCar.ConnectionNumber.SEVEN_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_7_B)) {
            return new CCSevenFlFrLRRlBRr(mContext, ConnectedCar.ConnectionNumber.SEVEN_CONNECTION);
        } else {
            return new CCFourLMRB(mContext, ConnectedCar.ConnectionNumber.FOUR_CONNECTION);
        }
    }
}
