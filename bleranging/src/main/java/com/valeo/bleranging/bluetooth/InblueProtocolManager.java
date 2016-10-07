package com.valeo.bleranging.bluetooth;

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
    private int packetOneCounter = 0;
    private boolean isStartRequested = false;
    private boolean isLockedFromTrx = false;
    private boolean isLockedToSend = false;
    private boolean isThatcham = false;
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

    /**
     * Construct the packet One to send
     *
     * @param isRKE                 the rke status
     * @param isUnlockStrategyValid the list of unlock position valid
     * @param isStartStrategyValid  true if in start area, false otherwise
     * @param isLockStrategyValid   true if in lock area, false otherwise
     * @return the packet one payload containing six bytes
     */
    public byte[] getPacketOnePayload(boolean isRKE, List<Integer> isUnlockStrategyValid, boolean isStartStrategyValid, boolean isLockStrategyValid) {
        byte[] payload = new byte[6];
        payload[0] = (byte) ((packetOneCounter>>8)&0xFF);
        payload[1] = (byte) ((packetOneCounter)&0xFF);
        payload[2] = (0x01);
        payload[3] = getPayloadThirdByte();
        payload[4] = getPayloadFourthByte(isRKE, isUnlockStrategyValid, isStartStrategyValid, isLockStrategyValid);
        payload[5] = getPayloadFifthByte(isRKE);
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
            case ConnectedCarFactory.TYPE_3_A:
                payloadThree |= 0x01;
                break;
            case ConnectedCarFactory.TYPE_3_B:
                payloadThree |= 0x02;
                break;
            case ConnectedCarFactory.TYPE_4_A:
                payloadThree |= 0x03;
                break;
            case ConnectedCarFactory.TYPE_4_B:
                payloadThree |= 0x04;
                break;
            case ConnectedCarFactory.TYPE_5_A:
                payloadThree |= 0x05;
                break;
            case ConnectedCarFactory.TYPE_5_B:
                payloadThree |= 0x06;
                break;
            case ConnectedCarFactory.TYPE_6_A:
                payloadThree |= 0x07;
                break;
            case ConnectedCarFactory.TYPE_6_B:
                payloadThree |= 0x08;
                break;
            case ConnectedCarFactory.TYPE_7_A:
                payloadThree |= 0x09;
                break;
            case ConnectedCarFactory.TYPE_7_B:
                payloadThree |= 0x10;
                break;
            default:
                payloadThree |= 0x00;
                break;
        }
        return payloadThree;
    }

    /**
     * Set jlr protocol in payload fourth byte
     *
     * @return the payload fourth byte
     */
    private byte getPayloadFourthByte(boolean isRKE, List<Integer> isUnlockStrategyValid, boolean isStartStrategyValid, boolean isLockStrategyValid) {
        byte payloadFour = (byte) 0;
        if (isStartStrategyValid) {
            payloadFour |= 0x01;
        } else if (!isLockStrategyValid && isUnlockStrategyValid != null) {
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
        } else if (isLockStrategyValid && isUnlockStrategyValid == null) {
            payloadFour |= 0x06;
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
    private byte getPayloadFifthByte(boolean isRKE) {
        byte payloadFive = (byte) 0;
        payloadFive |= isStartRequested ? 0x04 : 0x00;
        payloadFive |= isThatcham ? 0x08 : 0x00;
        switch (carBase) {
            case BASE_1:
                payloadFive |= 0x00;
                payloadFive |= (0x10);
                break;
            case ConnectedCarFactory.BASE_2:
                if (isLockedToSend && !isLockedFromTrx) {
                    payloadFive |= 0x01;
                } else {
                    payloadFive |= 0x00;
                }
                if (!isLockedFromTrx) { // no psu_lock, so if unlock force thatcham to 0, so psu deactivated
                    payloadFive &= 0xF7;
                }
                payloadFive |= (0x20);
                break;
            case ConnectedCarFactory.BASE_3:
                payloadFive |= isLockedToSend ? 0x01 : 0x02;
                payloadFive &= 0xF7;
                payloadFive |= (0x40);
                break;
            case ConnectedCarFactory.BASE_4:
                if (!isLockedToSend && isLockedFromTrx) {
                    payloadFive |= 0x02;
                } else {
                    payloadFive |= 0x00;
                }
                if (isLockedFromTrx) { // no psu_unlock, so if lock force thatcham to 0, so psu deactivated
                    payloadFive &= 0xF7;
                }
                payloadFive |= (0x80);
                break;
        }
        if (isRKE) {
            payloadFive |= isLockedToSend ? 0x01 : 0x02;
        }
        return payloadFive;
    }
}
