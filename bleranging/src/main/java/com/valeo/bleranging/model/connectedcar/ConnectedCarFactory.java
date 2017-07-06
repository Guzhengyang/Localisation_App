package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.model.Trx;

import java.util.LinkedHashMap;

import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_BACK;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_FRONT_LEFT;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_FRONT_RIGHT;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_LEFT;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_MIDDLE;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_REAR_LEFT;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_REAR_RIGHT;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_RIGHT;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_TRUNK;
import static com.valeo.bleranging.persistence.Constants.TRX_BACK_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_FRONT_LEFT_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_FRONT_RIGHT_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_LEFT_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_MIDDLE_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_REAR_LEFT_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_REAR_RIGHT_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_RIGHT_NAME;
import static com.valeo.bleranging.persistence.Constants.TRX_TRUNK_NAME;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_3_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_4_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_4_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_5_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_6_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_7_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_8_A;

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
    public final static String TYPE_TEST = "test";
    public final static String TYPE_Px = "MLP4Px";
    public final static String TYPE_Py = "MLP4Py";
    public final static int NUMBER_TRX_FRONT_LEFT = 1;
    public final static int NUMBER_TRX_FRONT_RIGHT = 2;
    public final static int NUMBER_TRX_LEFT = 3;
    public final static int NUMBER_TRX_MIDDLE = 4;
    public final static int NUMBER_TRX_RIGHT = 5;
    public final static int NUMBER_TRX_TRUNK = 6;
    public final static int NUMBER_TRX_REAR_LEFT = 7;
    public final static int NUMBER_TRX_BACK = 8;
    public final static int NUMBER_TRX_REAR_RIGHT = 9;
    private final static String TRX_FRONT_LEFT_NAME = "FLeft";
    private final static String TRX_FRONT_RIGHT_NAME = "FRight";
    private final static String TRX_LEFT_NAME = "Left";
    private final static String TRX_MIDDLE_NAME = "Middle";
    private final static String TRX_RIGHT_NAME = "Right";
    private final static String TRX_TRUNK_NAME = "Trunk";
    private final static String TRX_REAR_LEFT_NAME = "RLeft";
    private final static String TRX_BACK_NAME = "Back";
    private final static String TRX_REAR_RIGHT_NAME = "RRight";

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        return new CCEightFlFrLMRTRlRr(mContext);
    }

    public static class TrxLinkHMapBuilder {
        private final LinkedHashMap<Integer, Trx> trxLinked;

        TrxLinkHMapBuilder() {
            trxLinked = new LinkedHashMap<>();
        }

        public TrxLinkHMapBuilder left() {
            trxLinked.put(NUMBER_TRX_LEFT, new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME));
            return this;
        }

        public TrxLinkHMapBuilder middle() {
            trxLinked.put(NUMBER_TRX_MIDDLE, new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME));
            return this;
        }

        public TrxLinkHMapBuilder right() {
            trxLinked.put(NUMBER_TRX_RIGHT, new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME));
            return this;
        }

        public TrxLinkHMapBuilder frontLeft() {
            trxLinked.put(NUMBER_TRX_FRONT_LEFT, new Trx(NUMBER_TRX_FRONT_LEFT, TRX_FRONT_LEFT_NAME));
            return this;
        }

        public TrxLinkHMapBuilder frontRight() {
            trxLinked.put(NUMBER_TRX_FRONT_RIGHT, new Trx(NUMBER_TRX_FRONT_RIGHT, TRX_FRONT_RIGHT_NAME));
            return this;
        }

        public TrxLinkHMapBuilder rearleft() {
            trxLinked.put(NUMBER_TRX_REAR_LEFT, new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME));
            return this;
        }

        public TrxLinkHMapBuilder rearRight() {
            trxLinked.put(NUMBER_TRX_REAR_RIGHT, new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME));
            return this;
        }

        public TrxLinkHMapBuilder trunk() {
            trxLinked.put(NUMBER_TRX_TRUNK, new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME));
            return this;
        }

        public TrxLinkHMapBuilder back() {
            trxLinked.put(NUMBER_TRX_BACK, new Trx(NUMBER_TRX_BACK, TRX_BACK_NAME));
            return this;
        }

        public LinkedHashMap<Integer, Trx> build() {
            return trxLinked;
        }
    }
}
