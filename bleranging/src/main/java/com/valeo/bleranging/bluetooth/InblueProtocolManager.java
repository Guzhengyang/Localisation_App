package com.valeo.bleranging.bluetooth;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;

/**
 * Created by nhaan on 27/08/2015.
 * Temporary class providing methods to handle 'New' Inblue BLE protocol
 */
public class InblueProtocolManager {

    private int packetOneCounter;
    private boolean isStartRequested;
    private boolean isLockedFromTrx;
    private boolean isLockedToSend;
    private boolean isThatcham;
    private String carBase;

    public InblueProtocolManager() {
    }

    public boolean isStartRequested() {
        return isStartRequested;
    }

    public boolean isLockedToSend() {
        return isLockedToSend;
    }

    public boolean isLockedFromTrx() {
        return isLockedFromTrx;
    }

    public boolean isThatcham() {
        return isThatcham;
    }

    public void setThatcham(boolean thatcham) {
        this.isThatcham = thatcham;
    }

    public void setIsStartRequested(boolean isStartRequested) {
        this.isStartRequested = isStartRequested;
    }

    public void setIsLockedFromTrx(boolean isLockedFromTrx) {
        this.isLockedFromTrx = isLockedFromTrx;
    }

    public void setIsLockedToSend(boolean isLockedToSend) {
        this.isLockedToSend = isLockedToSend;
    }

    public void restartPacketOneCounter() {
        this.packetOneCounter = 0;
    }

    public void setCarBase(String carBase) {
        this.carBase = carBase;
    }

    public byte[] getPacketOnePayload(){
        byte[] payload = new byte[6];
        payload[0] = (byte) ((packetOneCounter>>8)&0xFF);
        payload[1] = (byte) ((packetOneCounter)&0xFF);
        payload[2] = (0x01);
        switch (carBase) {
            case ConnectedCarFactory.BASE_1:
                payload[4] = (0x01);
                break;
            case ConnectedCarFactory.BASE_2:
                payload[4] = (0x02);
                break;
            case ConnectedCarFactory.BASE_3:
                payload[4] = (0x04);
                break;
        }
        payload[5] = (byte) (isLockedToSend?0x01:0x02);
        payload[5] |= isStartRequested?0x04:0x00;
        payload[5] |= isThatcham ? 0x08 : 0x00;
        packetOneCounter++;
        return payload;
    }

}
