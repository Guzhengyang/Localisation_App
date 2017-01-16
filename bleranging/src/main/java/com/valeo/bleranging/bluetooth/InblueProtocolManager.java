package com.valeo.bleranging.bluetooth;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_BACK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FRONT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_TRUNK;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.BASE_1;

/**
 * Created by nhaan on 27/08/2015.
 * Temporary class providing methods to handle 'New' Inblue BLE protocol
 */
public class InblueProtocolManager {
    public final static int MAX_BLE_TRAME_BYTE = 6;
    private int packetOneCounter = 0;
    private boolean isStartRequested = false;
    private boolean isWelcomeRequested = false;
    private boolean isLockedFromTrx = false;
    private boolean isLockedToSend = false;
    private boolean isThatcham = false;
    private boolean isInRemoteParkingArea = false;
    private String carBase;

    public InblueProtocolManager() {
    }

    public boolean isStartRequested() {
        return isStartRequested;
    }

    public boolean isWelcomeRequested() {
        return isWelcomeRequested;
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

    public void setInRemoteParkingArea(boolean inRemoteParkingArea) {
        isInRemoteParkingArea = inRemoteParkingArea;
    }

    public void setIsStartRequested(boolean isStartRequested) {
        this.isStartRequested = isStartRequested;
    }

    public void setIsWelcomeRequested(boolean isWelcomeRequested) {
        this.isWelcomeRequested = isWelcomeRequested;
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

    /**
     * Construct the packet One to send
     *
     * @param mAlgoManager the algoManager
     * @return the packet one payload containing six bytes
     */
    public byte[] getPacketOnePayload(final AlgoManager mAlgoManager) {
        boolean isRKE = mAlgoManager.getIsRKE();
        byte[] payload = new byte[MAX_BLE_TRAME_BYTE];
        payload[0] = (byte) ((packetOneCounter >> 8) & 0xFF);
        payload[1] = (byte) (packetOneCounter & 0xFF);
        payload[2] = (0x01);
        payload[3] = getPayloadThirdByte();
        payload[4] = getPayloadFourthByte(isRKE, mAlgoManager.getRangingPositionPrediction());
        payload[5] = getPayloadFifthByte(isRKE, mAlgoManager.getRangingPositionPrediction());
        packetOneCounter++;
        if (packetOneCounter > 65534) { // packetOneCounter > FF FE
            packetOneCounter = 0;
        }
        return payload;
    }

    /**
     * Set car type in payload third byte
     *
     * @return the payload third byte
     */
    private byte getPayloadThirdByte() {
        byte payloadThree = (byte) 0;
        switch (SdkPreferencesHelper.getInstance().getConnectedCarType()) {
            case ConnectedCarFactory.TYPE_4_A:
                payloadThree |= 0x01;
                break;
            case ConnectedCarFactory.TYPE_5_A:
                payloadThree |= 0x02;
                break;
            case ConnectedCarFactory.TYPE_7_A:
                payloadThree |= 0x03;
                break;
            case ConnectedCarFactory.TYPE_8_A:
                payloadThree |= 0x04;
                break;
            default:
                payloadThree |= 0x00;
                break;
        }
        if (isInRemoteParkingArea) {
            payloadThree |= 0x10;
        }
        return payloadThree;
    }

    /**
     * Set jlr protocol in payload fourth byte
     * @param isRKE true if the action is RKE, flase otherwise
     * @param prediction the algo prediction
     * @return the payload fourth byte
     */
    private byte getPayloadFourthByte(boolean isRKE, String prediction) {
        byte payloadFour = (byte) 0;
        switch (prediction) {
            case PREDICTION_LOCK:
                payloadFour |= 0x06;
                break;
            case PREDICTION_TRUNK:
                payloadFour |= 0x04;
                break;
            case PREDICTION_START:
                payloadFour |= 0x01;
                break;
            case PREDICTION_LEFT:
                payloadFour |= 0x03;
                break;
            case PREDICTION_RIGHT:
                payloadFour |= 0x02;
                break;
            case PREDICTION_BACK:
                payloadFour |= 0x05;
                break;
            case PREDICTION_FRONT:
                payloadFour |= 0x09;
                break;
            default:
                break;
        }
        if (isRKE) {
            payloadFour |= isLockedToSend ? 0x08 : 0x07;
        }
        return payloadFour;
    }

    /**
     * Set valeo commands in payload fifth byte
     *
     * @return the payload fifth byte
     */
    private byte getPayloadFifthByte(boolean isRKE, String prediction) {
        byte payloadFive = (byte) 0;
        payloadFive |= isThatcham ? 0x08 : 0x00;
        if (isWelcomeRequested) {
            payloadFive |= 0x40;
        }
        if (isStartRequested) {
            payloadFive |= 0x04;
        } else if (prediction.equalsIgnoreCase(PREDICTION_BACK)) {
            payloadFive |= 0x80;
        }
        switch (carBase) {
            case BASE_1:
                payloadFive |= 0x30; // Full PSU, lock and unlock activated 0011 0000
                break;
            case ConnectedCarFactory.BASE_2:
                if (isLockedToSend && !isLockedFromTrx) {
                    payloadFive |= 0x01; // WAL, lock command sent when car is unlocked
                } else {
                    payloadFive |= 0x00;
                }
                payloadFive |= 0x20; // Unlock PSU activated 0010 0000
                break;
            case ConnectedCarFactory.BASE_3:
                payloadFive |= isLockedToSend ? 0x01 : 0x02;
                // WAL & UIR, PSU deactivated 0000 0000
                break;
            case ConnectedCarFactory.BASE_4:
                if (!isLockedToSend && isLockedFromTrx) {
                    payloadFive |= 0x02; // UIR, unlock command sent when car is locked
                } else {
                    payloadFive |= 0x00;
                }
                payloadFive |= 0x10; // Lock PSU activated 0001 0000
                break;
        }
        if (isRKE) {
            payloadFive |= isLockedToSend ? 0x01 : 0x02;
        }
        return payloadFive;
    }
}
