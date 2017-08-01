package com.valeo.bleranging.bluetooth.protocol;

/**
 * Created by l-avaratha on 01/08/2017
 */

public class PacketThree {
    private static PacketThree instance;

    private PacketThree() {
    }

    static void initializePacketThree() {
        if (instance == null) {
            instance = new PacketThree();
        }
    }

    static PacketThree getInstance() {
        return instance;
    }

    public byte[] getPacketThreePayload(final double[] rssi) {
        byte[] payload;
        if (rssi != null) {
            payload = new byte[rssi.length];
            for (int payloadIndex = 0; payloadIndex < rssi.length; payloadIndex++) {
                payload[payloadIndex] = (byte) rssi[payloadIndex];
            }
            return payload;
        }
        return new byte[1];
    }
}
