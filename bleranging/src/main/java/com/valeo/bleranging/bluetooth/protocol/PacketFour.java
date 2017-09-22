package com.valeo.bleranging.bluetooth.protocol;

import android.graphics.PointF;

import java.util.List;

/**
 * Created by l-avaratha on 01/08/2017
 */

public class PacketFour {
    private static PacketFour instance;

    private PacketFour() {
    }

    static void initializePacketFour() {
        if (instance == null) {
            instance = new PacketFour();
        }
    }

    static PacketFour getInstance() {
        return instance;
    }

    public byte[] getPacketFourPayload(List<PointF> predictionCoord, List<Double> distances) {
        byte[] payloadSix = new byte[3];
        if (predictionCoord != null && predictionCoord.size() > 0 && predictionCoord.get(0) != null) {
            payloadSix[0] = (byte) (predictionCoord.get(0).x * 10);
            payloadSix[1] = (byte) (predictionCoord.get(0).y * 10);
        }
        if (distances != null && distances.size() > 0 && distances.get(0) != null) {
            payloadSix[2] = (byte) (distances.get(0) * 10);
        }
        return payloadSix;
    }
}
