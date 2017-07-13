package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.machinelearningalgo.prediction.BasePrediction;
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

/**
 * Created by l-avaratha on 07/09/2016
 */
public class ConnectedCarFactory {

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        final LinkedHashMap<Integer, Trx> trxLinked = new ConnectedCarFactory.TrxLinkHMapBuilder()
                .left()
                .middle()
                .right()
                .trunk()
                .frontLeft()
                .frontRight()
                .rearleft()
                .rearRight()
                .build();
        return new ConnectedCar(mContext, trxLinked, new LinkedHashMap<String, BasePrediction>());
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
