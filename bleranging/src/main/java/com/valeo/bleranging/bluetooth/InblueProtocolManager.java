package com.valeo.bleranging.bluetooth;

import android.graphics.PointF;

import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.machinelearningalgo.AlgoManager;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.List;

import static com.valeo.bleranging.persistence.Constants.BASE_1;
import static com.valeo.bleranging.persistence.Constants.BASE_2;
import static com.valeo.bleranging.persistence.Constants.BASE_3;
import static com.valeo.bleranging.persistence.Constants.BASE_4;
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
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_FL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_FR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_RL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_RR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_TRUNK;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_2_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_3_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_4_B;
import static com.valeo.bleranging.persistence.Constants.TYPE_5_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_6_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_7_A;
import static com.valeo.bleranging.persistence.Constants.TYPE_8_A;

/**
 * Created by nhaan on 27/08/2015.
 * Temporary class providing methods to handle 'New' Inblue BLE protocol
 */
public class InblueProtocolManager {
    private final static int MAX_BLE_TRAME_BYTE = 6;
    private int packetOneCounter = 0;
    private boolean isStartRequested = false;
    private boolean isWelcomeRequested = false;
    private boolean isLockedFromTrx = false;
    private boolean isLockedToSend = false;
    private boolean isThatcham = false;
    private boolean isInRemoteParkingArea = false;
    private String carBase;
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

    /**
     * Construct the packet One to send
     *
     * @param mAlgoManager the algoManager
     * @return the packet one payload containing six bytes
     */
    public byte[] getPacketOnePayload(final AlgoManager mAlgoManager, final ConnectedCar connectedCar) {
        boolean isRKE = mAlgoManager.getIsRKE();
        byte[] payload = new byte[MAX_BLE_TRAME_BYTE];
        payload[0] = (byte) ((packetOneCounter >> 8) & 0xFF);
        payload[1] = (byte) (packetOneCounter & 0xFF);
        payload[2] = (0x01);
        payload[3] = getPayloadThirdByte();
        payload[4] = getPayloadFourthByte(isRKE, mAlgoManager.getPredictionPosition(connectedCar));
        payload[5] = getPayloadFifthByte(isRKE, mAlgoManager.getPredictionPosition(connectedCar));
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
        if (SdkPreferencesHelper.getInstance().isCalibrated()) {
            switch (SdkPreferencesHelper.getInstance().getConnectedCarType()) {
                case TYPE_6_A:
                    payloadThree |= 0x08;
                    break;
                case TYPE_2_A:
                    payloadThree |= 0x07;
                    break;
                case TYPE_2_B:
                    payloadThree |= 0x06;
                    break;
                case TYPE_3_A:
                    payloadThree |= 0x05;
                    break;
                case TYPE_4_B:
                    payloadThree |= 0x04;
                    break;
                case TYPE_5_A:
                    payloadThree |= 0x03;
                    break;
                case TYPE_7_A:
                    payloadThree |= 0x02;
                    break;
                case TYPE_8_A:
                    payloadThree |= 0x01;
                    break;
                default:
                    payloadThree |= 0x00;
                    break;
            }
            if (isInRemoteParkingArea) {
                payloadThree |= 0x10;
            }
        }
        return payloadThree;
    }

    /**
     * Set jlr protocol in payload fourth byte
     * @param isRKE true if the action is RKE, false otherwise
     * @param prediction the algo prediction
     * @return the payload fourth byte
     */
    private byte getPayloadFourthByte(boolean isRKE, String prediction) {
        byte payloadFour = (byte) 0;
        if (SdkPreferencesHelper.getInstance().isCalibrated()) {
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
                case PREDICTION_INSIDE:
                    payloadFour |= 0x10;
                    break;
                case PREDICTION_OUTSIDE:
                    payloadFour |= 0x11;
                    break;
                case PREDICTION_START_FL:
                    payloadFour |= 0x12;
                    break;
                case PREDICTION_START_FR:
                    payloadFour |= 0x13;
                    break;
                case PREDICTION_START_RL:
                    payloadFour |= 0x14;
                    break;
                case PREDICTION_START_RR:
                    payloadFour |= 0x15;
                    break;
                case PREDICTION_ROOF:
                    payloadFour |= 0x16;
                    break;
                case PREDICTION_INTERNAL:
                    payloadFour |= 0x17;
                    break;
                case PREDICTION_ACCESS:
                    payloadFour |= 0x18;
                    break;
                case PREDICTION_EXTERNAL:
                    payloadFour |= 0x19;
                    break;
                default:
                    break;
            }
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
        if (SdkPreferencesHelper.getInstance().isCalibrated()) {
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
                case BASE_2:
                    if (isLockedToSend && !isLockedFromTrx) {
                        payloadFive |= 0x01; // WAL, lock command sent when car is unlocked
                    } else {
                        payloadFive |= 0x00;
                    }
                    payloadFive |= 0x20; // Unlock PSU activated 0010 0000
                    break;
                case BASE_3:
                    payloadFive |= isLockedToSend ? 0x01 : 0x02;
                    // WAL & UIR, PSU deactivated 0000 0000
                    break;
                case BASE_4:
                    if (!isLockedToSend && isLockedFromTrx) {
                        payloadFive |= 0x02; // UIR, unlock command sent when car is locked
                    } else {
                        payloadFive |= 0x00;
                    }
                    payloadFive |= 0x10; // Lock PSU activated 0001 0000
                    break;
            }
        }
        if (isRKE) {
            payloadFive |= isLockedToSend ? 0x01 : 0x02;
        }
        return payloadFive;
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

    public byte[] getPacketFourPayload(List<PointF> predictionCoords, List<Double> distances) {
        byte[] payloadSix = new byte[3];
        if (predictionCoords != null && predictionCoords.size() > 0 && predictionCoords.get(0) != null) {
            payloadSix[0] = (byte) (predictionCoords.get(0).x * 10);
            payloadSix[1] = (byte) (predictionCoords.get(0).y * 10);
        }
        if (distances != null && distances.size() > 0 && distances.get(0) != null) {
            payloadSix[2] = (byte) (distances.get(0) * 10);
        }
        return payloadSix;
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

    private byte getPayloadProba(byte classes, double proba) {
        byte payloadProb = classes;
        payloadProb = (byte) (payloadProb << 4);
        payloadProb |= ((byte) (proba * 10) & 0x0F);
        return payloadProb;
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

    /**
     * Get the current advertising channel from beacon scan response
     *
     * @param scanResponse the beacon scan response
     * @return the received ble channel
     */
    public Antenna.BLEChannel getCurrentChannel(BeaconScanResponse scanResponse) {
        if (scanResponse.getAdvertisingChannel() == 0x01) {
            return Antenna.BLEChannel.BLE_CHANNEL_37;
        } else if (scanResponse.getAdvertisingChannel() == 0x02) {
            return Antenna.BLEChannel.BLE_CHANNEL_38;
        } else if (scanResponse.getAdvertisingChannel() == 0x03) {
            return Antenna.BLEChannel.BLE_CHANNEL_39;
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
    }
}
