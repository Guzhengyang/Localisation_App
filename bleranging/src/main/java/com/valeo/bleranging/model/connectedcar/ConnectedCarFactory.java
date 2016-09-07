package com.valeo.bleranging.model.connectedcar;

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
    public static ConnectedCar getConnectedCar(String carName) {
        if (carName.equalsIgnoreCase(TYPE_3_A)) {
            return new CCThreeLMR(ConnectedCar.ConnectionNumber.THREE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_3_B)) {
            return new CCThreeLRB(ConnectedCar.ConnectionNumber.THREE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_4_A)) {
            return new CCFourLMRB(ConnectedCar.ConnectionNumber.FOUR_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_4_B)) {
            return new CCFourLRRlRr(ConnectedCar.ConnectionNumber.FOUR_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_5_A)) {
            return new CCFiveLMRRlRr(ConnectedCar.ConnectionNumber.FIVE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_5_B)) {
            return new CCFiveFlFrLRB(ConnectedCar.ConnectionNumber.FIVE_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_6_A)) {
            return new CCSixLMRRlBRr(ConnectedCar.ConnectionNumber.SIX_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_6_B)) {
            return new CCSixFlFrLRRlRr(ConnectedCar.ConnectionNumber.SIX_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_7_A)) {
            return new CCSevenFlFrLMRRlRr(ConnectedCar.ConnectionNumber.SEVEN_CONNECTION);
        } else if (carName.equalsIgnoreCase(TYPE_7_B)) {
            return new CCSevenFlFrLRRlBRr(ConnectedCar.ConnectionNumber.SEVEN_CONNECTION);
        } else {
            return null;
        }
    }
}
