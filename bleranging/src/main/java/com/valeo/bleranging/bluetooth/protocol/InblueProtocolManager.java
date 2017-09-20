package com.valeo.bleranging.bluetooth.protocol;

import android.content.Context;

import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.model.Antenna;

/**
 * Created by nhaan on 27/08/2015.
 * Temporary class providing methods to handle 'New' Inblue BLE protocol
 */
public class InblueProtocolManager {

    /**
     * Single helper instance.
     */
    private static InblueProtocolManager sSingleInstance = null;

    /**
     * Private constructor.
     */
    private InblueProtocolManager(final Context context) {
        PacketOne.initializePacketOne(context);
        PacketTwo.initializePacketTwo();
        PacketThree.initializePacketThree();
        PacketFour.initializePacketFour();
        PacketLog.initializePacketLog();
    }

    /**
     * Initialize the helper instance.
     */
    public static void initializeInstance(final Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new InblueProtocolManager(context);
        }
    }

    /**
     * @return the single helper instance.
     */
    public static InblueProtocolManager getInstance() {
        return sSingleInstance;
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

    public PacketOne getPacketOne() {
        return PacketOne.getInstance();
    }

    public PacketTwo getPacketTwo() {
        return PacketTwo.getInstance();
    }

    public PacketThree getPacketThree() {
        return PacketThree.getInstance();
    }

    public PacketFour getPacketFour() {
        return PacketFour.getInstance();
    }

    public PacketLog getPacketLog() {
        return PacketLog.getInstance();
    }

}
