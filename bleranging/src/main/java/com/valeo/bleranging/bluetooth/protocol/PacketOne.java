package com.valeo.bleranging.bluetooth.protocol;

import android.content.Context;

import com.valeo.bleranging.machinelearningalgo.AlgoManager;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.JsonUtils;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;

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
 * Created by l-avaratha on 01/08/2017
 */

public class PacketOne {
    private static final int MAX_BLE_TRAME_BYTE = 13;
    private static final int MAC_ADDRESS_SIZE = 6;
    private static final int NUMBER_MAX_TRX = 16;
    private static PacketOne instance;
    private int packetOneCounter = 0;
    private String carBase;
    private boolean isInRemoteParkingArea = false;
    private boolean isStartRequested = false;
    private boolean isWelcomeRequested = false;
    private boolean isLockedFromTrx = false;
    private boolean isLockedToSend = false;
    private boolean isThatcham = false;
    private boolean isAutoMode = false;
    private int currentTrx = 0;

    private PacketOne() {
    }

    static void initializePacketOne(Context context) {
        if (instance == null) {
            instance = new PacketOne();
            JsonUtils.loadMacAddress(context);
        }
    }

    static PacketOne getInstance() {
        return instance;
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
        payload[4] = getPayloadFourthByte(isRKE, connectedCar.getMultiPrediction().getPredictionPosition(mAlgoManager.isSmartphoneInPocket()));
        payload[5] = getPayloadFifthByte(isRKE, connectedCar.getMultiPrediction().getPredictionPosition(mAlgoManager.isSmartphoneInPocket()));
        payload[6] = getPayloadSixthByte();
        PSALogs.d("currentTrx", String.format("%02X ", payload[6]));
        if (!isAutoMode) {
            System.arraycopy(getPayloadSevenToTwelveByte(connectedCar.getRegPlate()), 0, payload, 7, 6);
        }
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

    private byte getPayloadSixthByte() {
        currentTrx++;
        if (isAutoMode) {
            return (byte) 0x20;
        } else if (currentTrx < NUMBER_MAX_TRX) {
            return (byte) currentTrx;
        } else {
            isAutoMode = true;
            return (byte) 0xF0;
        }
    }

    private byte[] getPayloadSevenToTwelveByte(final String regPlate) {
        byte[] payloadSevenToTwelve = new byte[MAC_ADDRESS_SIZE];
        //TODO get regPlate here
        String address = JsonUtils.getMacAddress(regPlate, String.valueOf(currentTrx));
        if (currentTrx >= NUMBER_MAX_TRX) {
            payloadSevenToTwelve[0] = (byte) 0xF0;
        } else {
            String[] split = address.split(":");
            for (int i = 0; i < split.length; i++) {
                payloadSevenToTwelve[i] = TextUtils.fromHexString(split[i])[0];
            }
        }
        PSALogs.d("currentTrx", currentTrx + " " + TextUtils.printAddressBytes(payloadSevenToTwelve) + " !");
        return payloadSevenToTwelve;
    }

    public void restartPacketOneCounter() {
        this.packetOneCounter = 0;
        this.currentTrx = 0;
    }

    public void setCarBase(String carBase) {
        this.carBase = carBase;
    }

    public void setInRemoteParkingArea(boolean inRemoteParkingArea) {
        isInRemoteParkingArea = inRemoteParkingArea;
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
}
