package com.valeo.bleranging.bluetooth.protocol;

/**
 * Created by l-avaratha on 01/08/2017
 */

public class PacketLog {
    private static PacketLog instance;
    private byte recordByte = 0;
    private byte welcomeByte = 0;
    private byte lockByte = 0;
    private byte startByte = 0;
    private byte leftAreaByte = 0;
    private byte rightAreaByte = 0;
    private byte backAreaByte = 0;
    private byte walkAwayByte = 0;
    private byte approachByte = 0;
    private byte leftTurnByte = 0;
    private byte rightTurnByte = 0;
    private byte approachSideByte = 0;
    private byte approachRoadByte = 0;

    private PacketLog() {
    }

    static void initializePacketLog() {
        if (instance == null) {
            instance = new PacketLog();
        }
    }

    static PacketLog getInstance() {
        return instance;
    }

    /**
     * Create two bytes with all the bits from the switches
     */
    public void getAdvertisedBytes(byte[] advertisedData) {
        if (advertisedData != null) {
            walkAwayByte = (byte) ((advertisedData[3] & (1 << 6)) >> 6);
            backAreaByte = (byte) ((advertisedData[3] & (1 << 5)) >> 5);
            rightAreaByte = (byte) ((advertisedData[3] & (1 << 4)) >> 4);
            leftAreaByte = (byte) ((advertisedData[3] & (1 << 3)) >> 3);
            startByte = (byte) ((advertisedData[3] & (1 << 2)) >> 2);
            lockByte = (byte) ((advertisedData[3] & (1 << 1)) >> 1);
            welcomeByte = (byte) (advertisedData[3] & 1);
            recordByte = (byte) ((advertisedData[4] & (1 << 7)) >> 7);
            approachRoadByte = (byte) ((advertisedData[4] & 0x070) >> 4);
            approachSideByte = (byte) ((advertisedData[4] & (1 << 3)) >> 3);
            rightTurnByte = (byte) ((advertisedData[4] & (1 << 2)) >> 2);
            leftTurnByte = (byte) ((advertisedData[4] & (1 << 1)) >> 1);
            approachByte = (byte) (advertisedData[4] & 1);
        }
    }

    public byte getRecordByte() {
        return recordByte;
    }

    public byte getWelcomeByte() {
        return welcomeByte;
    }

    public byte getLockByte() {
        return lockByte;
    }

    public byte getStartByte() {
        return startByte;
    }

    public byte getLeftAreaByte() {
        return leftAreaByte;
    }

    public byte getRightAreaByte() {
        return rightAreaByte;
    }

    public byte getBackAreaByte() {
        return backAreaByte;
    }

    public byte getWalkAwayByte() {
        return walkAwayByte;
    }

    public byte getApproachByte() {
        return approachByte;
    }

    public byte getLeftTurnByte() {
        return leftTurnByte;
    }

    public byte getRightTurnByte() {
        return rightTurnByte;
    }

    public byte getApproachSideByte() {
        return approachSideByte;
    }

    public byte getApproachRoadByte() {
        return approachRoadByte;
    }
}
