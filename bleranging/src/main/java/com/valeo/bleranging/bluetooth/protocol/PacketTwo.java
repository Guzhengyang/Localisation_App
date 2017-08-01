package com.valeo.bleranging.bluetooth.protocol;

import static com.valeo.bleranging.persistence.Constants.PREDICTION_ACCESS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_BACK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FRONT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LEFT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LOCK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RIGHT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_ROOF;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_TRUNK;

/**
 * Created by l-avaratha on 01/08/2017
 */

public class PacketTwo {
    private static PacketTwo instance;

    private PacketTwo() {
    }

    static void initializePacketTwo() {
        if (instance == null) {
            instance = new PacketTwo();
        }
    }

    static PacketTwo getInstance() {
        return instance;
    }

    public byte[] getPacketTwoPayload(final String[] classes, final double[] distribution) {
        byte[] payload = new byte[8];
        if (distribution != null && classes != null) {
            for (int payloadIndex = 0; payloadIndex < classes.length; payloadIndex++) {
                payload[payloadIndex] = getPayloadProba(encodeClasses(classes[payloadIndex]), distribution[payloadIndex]);
            }
        }
        return payload;
    }

    private byte getPayloadProba(byte classes, double proba) {
        byte payloadProb = classes;
        payloadProb = (byte) (payloadProb << 4);
        payloadProb |= ((byte) (proba * 10) & 0x0F);
        return payloadProb;
    }

    private byte encodeClasses(String classes) {
        switch (classes) {
            case PREDICTION_START:
                return 0x01;
            case PREDICTION_LOCK:
                return 0x02;
            case PREDICTION_TRUNK:
                return 0x03;
            case PREDICTION_LEFT:
                return 0x04;
            case PREDICTION_RIGHT:
                return 0x05;
            case PREDICTION_BACK:
                return 0x06;
            case PREDICTION_FRONT:
                return 0x07;
            case PREDICTION_INTERNAL:
                return 0x08;
            case PREDICTION_EXTERNAL:
                return 0x09;
            case PREDICTION_ACCESS:
                return 0x0A;
            case PREDICTION_INSIDE:
                return 0x0B;
            case PREDICTION_OUTSIDE:
                return 0x0C;
            case PREDICTION_ROOF:
                return 0x0D;
            default:
                return 0x00;
        }
    }
}
