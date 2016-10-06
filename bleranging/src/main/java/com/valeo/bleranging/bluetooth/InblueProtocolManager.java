package com.valeo.bleranging.bluetooth;

import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;

import java.util.List;

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

    public byte[] getPacketOnePayload(boolean isRKE, List<Integer> isUnlockStrategyValid, boolean isStartStrategyValid, boolean isLockStrategyValid) {
        byte[] payload = new byte[6];
        payload[0] = (byte) ((packetOneCounter>>8)&0xFF);
        payload[1] = (byte) ((packetOneCounter)&0xFF);
        payload[2] = (0x01);
        payload[4] = (byte) 0;
        if (!isLockStrategyValid && isUnlockStrategyValid != null) {
            for (Integer integer : isUnlockStrategyValid) {
                switch (integer) {
                    case ConnectedCar.NUMBER_TRX_LEFT:
                        payload[4] |= 0x03;
                        break;
                    case ConnectedCar.NUMBER_TRX_RIGHT:
                        payload[4] |= 0x02;
                        break;
                    case ConnectedCar.NUMBER_TRX_BACK:
                        payload[4] |= 0x05;
                        break;
                    default:
                        break;
                }
            }
        } else if (isLockStrategyValid && isUnlockStrategyValid == null) {
            payload[4] |= 0x06;
        } else if (isStartStrategyValid) {
            payload[4] |= 0x01;
        }
        payload[5] = (byte) 0;
        payload[5] |= isStartRequested ? 0x04 : 0x00;
        payload[5] |= isThatcham ? 0x08 : 0x00;
        switch (carBase) {
            case ConnectedCarFactory.BASE_1:
                payload[5] |= 0x00;
                payload[5] |= (0x10);
                break;
            case ConnectedCarFactory.BASE_2:
                if (isLockedToSend && !isLockedFromTrx) {
                    payload[5] |= 0x01;
                } else {
                    payload[5] |= 0x00;
                }
                if (!isLockedFromTrx) { // no psu_lock, so if unlock force thatcham to 0, so psu deactivated
                    payload[5] &= 0xF7;
                }
                payload[5] |= (0x20);
                break;
            case ConnectedCarFactory.BASE_3:
                payload[5] |= isLockedToSend ? 0x01 : 0x02;
                payload[5] &= 0xF7;
                payload[5] |= (0x40);
                break;
            case ConnectedCarFactory.BASE_4:
                if (!isLockedToSend && isLockedFromTrx) {
                    payload[5] |= 0x02;
                } else {
                    payload[5] |= 0x00;
                }
                if (isLockedFromTrx) { // no psu_unlock, so if lock force thatcham to 0, so psu deactivated
                    payload[5] &= 0xF7;
                }
                payload[5] |= (0x80);
                break;
        }
        if (isRKE) {
            payload[5] |= isLockedToSend ? 0x01 : 0x02;
            payload[4] |= isLockedToSend ? 0x08 : 0x07;
        }
        packetOneCounter++;
        return payload;
    }
}
