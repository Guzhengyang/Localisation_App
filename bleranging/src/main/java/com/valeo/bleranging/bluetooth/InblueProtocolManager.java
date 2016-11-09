package com.valeo.bleranging.bluetooth;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.List;

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
     * @param isRKE                 the rke status
     * @param isUnlockStrategyValid the list of unlock position valid
     * @param isInUnlockArea  true if in unlock area, false otherwise
     * @param isInStartArea  true if in start area, false otherwise
     * @param isInLockArea   true if in lock area, false otherwise
     * @return the packet one payload containing six bytes
     */
    public byte[] getPacketOnePayload(boolean isRKE, List<Integer> isUnlockStrategyValid,
                                      boolean isInUnlockArea, List<Integer> isStartStrategyValid,
                                      boolean isInStartArea, boolean isInLockArea) {
        byte[] payload = new byte[MAX_BLE_TRAME_BYTE];
        payload[0] = (byte) ((packetOneCounter>>8)&0xFF);
        payload[1] = (byte) ((packetOneCounter)&0xFF);
        payload[2] = (0x01);
        payload[3] = getPayloadThirdByte();
        payload[4] = getPayloadFourthByte(isRKE, isUnlockStrategyValid, isInUnlockArea,
                isStartStrategyValid, isInStartArea, isInLockArea);
        payload[5] = getPayloadFifthByte(isRKE, isUnlockStrategyValid);
        packetOneCounter++;
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
        return payloadThree;
    }

    /**
     * Set jlr protocol in payload fourth byte
     * @param isRKE true if the action is RKE, flase otherwise
     * @param isUnlockStrategyValid the list of valid unlock area
     * @param isInUnlockArea true if in unlock area; false otherwise
     * @param isInStartArea true if in start area; false otherwise
     * @param isInLockArea true if in lock area; false otherwise
     * @return the payload fourth byte
     */
    private byte getPayloadFourthByte(boolean isRKE, List<Integer> isUnlockStrategyValid, boolean
            isInUnlockArea, List<Integer> isStartStrategyValid, boolean isInStartArea, boolean isInLockArea) {
        byte payloadFour = (byte) 0;
        if (isInLockArea) {
            payloadFour |= 0x06;
        } else if (isStartStrategyValid != null && isInStartArea) {
            for (Integer integer : isStartStrategyValid) {
                switch (integer) {
                    case BleRangingHelper.START_TRUNK_AREA:
                        payloadFour |= 0x04;
                        break;
                    case BleRangingHelper.START_PASSENGER_AREA:
                        payloadFour |= 0x01;
                        break;
                    default:
                        break;
                }
            }
        } else if (isUnlockStrategyValid != null && isInUnlockArea) {
            for (Integer integer : isUnlockStrategyValid) {
                switch (integer) {
                    case ConnectedCar.NUMBER_TRX_LEFT:
                        payloadFour |= 0x03;
                        break;
                    case ConnectedCar.NUMBER_TRX_RIGHT:
                        payloadFour |= 0x02;
                        break;
                    case ConnectedCar.NUMBER_TRX_BACK:
                        payloadFour |= 0x05;
                        break;
                    case ConnectedCar.NUMBER_TRX_FRONT_LEFT:
                        payloadFour |= 0x09;
                        break;
                    case ConnectedCar.NUMBER_TRX_FRONT_RIGHT:
                        payloadFour |= 0x10;
                        break;
                    case ConnectedCar.NUMBER_TRX_REAR_LEFT:
                        payloadFour |= 0x11;
                        break;
                    case ConnectedCar.NUMBER_TRX_REAR_RIGHT:
                        payloadFour |= 0x12;
                        break;
                    default:
                        break;
                }
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
    private byte getPayloadFifthByte(boolean isRKE, List<Integer> isUnlockStrategyValid) {
        byte payloadFive = (byte) 0;
//        payloadFive |= isStartRequested ? 0x04 : 0x00;
        payloadFive |= isThatcham ? 0x08 : 0x00;
//        payloadFive |= isWelcomeRequested ? 0x40 : 0x00;
        if (isWelcomeRequested) {
            payloadFive |= 0x40;
        }
        if (isStartRequested) {
            payloadFive |= 0x04;
        } else if (isUnlockStrategyValid != null
                && isUnlockStrategyValid.contains(ConnectedCar.NUMBER_TRX_BACK)) {
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
                if (!isLockedFromTrx) { // no psu_lock, so if unlock force thatcham to 0, so psu deactivated
                    payloadFive &= 0xF7; // TODO delete workaround after bml flash
                }
                payloadFive |= 0x20; // Unlock PSU activated 0010 0000
                break;
            case ConnectedCarFactory.BASE_3:
                payloadFive |= isLockedToSend ? 0x01 : 0x02;
                payloadFive &= 0xF7; // TODO delete workaround after bml flash
                // WAL & UIR, PSU deactivated 0000 0000
                break;
            case ConnectedCarFactory.BASE_4:
                if (!isLockedToSend && isLockedFromTrx) {
                    payloadFive |= 0x02; // UIR, unlock command sent when car is locked
                } else {
                    payloadFive |= 0x00;
                }
//                if (isLockedFromTrx) { // no psu_unlock, so if lock force thatcham to 0, so psu deactivated
//                    payloadFive &= 0xF7; // TODO delete workaround after bml flash
//                }
                payloadFive |= 0x10; // Lock PSU activated 0001 0000
                break;
        }
        if (isRKE) {
            payloadFive |= isLockedToSend ? 0x01 : 0x02;
        }
        return payloadFive;
    }
}
