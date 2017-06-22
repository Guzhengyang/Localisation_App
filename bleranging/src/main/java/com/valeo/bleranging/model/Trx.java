package com.valeo.bleranging.model;

import android.util.SparseIntArray;

import com.valeo.bleranging.utils.PSALogs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Trx {
    private final static int ANTENNA_NULL = 0;
    private final static int ANTENNA_A = 4;
    private final static int ANTENNA_B = 8;
    private final int trxNumber;
    private final String trxName;
    private final List<Antenna> antennaList;
    private final SparseIntArray antennaIdArray;
    private String trxAddress;
    private Antenna currentAntenna;
    private int carRssi;

    public Trx(int trxNumber, String trxName) {
        this.trxNumber = trxNumber;
        this.trxName = trxName;
        this.antennaIdArray = initSparseArray();
        this.antennaList = createAntennaList();
    }

    /**
     * Create a list of antenna Id
     *
     * @return a sparseIntArray of antenna id
     */
    private SparseIntArray initSparseArray() {
        SparseIntArray antennaIdArray = new SparseIntArray();
        antennaIdArray.put(0, ANTENNA_NULL);
        antennaIdArray.put(1, ANTENNA_A);
        antennaIdArray.put(2, ANTENNA_B);
        return antennaIdArray;
    }

    private List<Antenna> createAntennaList() {
        List<Antenna> antennaTmpList = new ArrayList<>();
        for (int i = 0; i < antennaIdArray.size(); i++) {
            antennaTmpList.add(new Antenna(trxNumber, antennaIdArray.get(i)));
        }
        return antennaTmpList;
    }

    public void init(int historicDefaultValue) {
        for (Antenna antenna : antennaList) {
            antenna.init(historicDefaultValue);
        }
    }

    /**
     * Compare a new check with the last one, if they are equals the trx antenna is inactive
     *
     * @return true if the trx antenna is active (checker are different), false otherwise (checker are equals)
     */
    public boolean isActive() {
        boolean result = true;
        for (Antenna antenna : antennaList) {
            result = result && antenna.isActive();
        }
        return result;
    }

    /**
     * Retrieve an antenna by its id
     *
     * @param antennaId the antenna id of the antenna to retrieve
     * @return the antenna with its antenna id equals to antennaId, null otherwise
     */
    private Antenna getAntennaById(byte antennaId) {
        if (antennaList != null) {
            for (Antenna antenna : antennaList) {
                if (antenna.getAntennaId() == antennaId) {
                    return antenna;
                }
            }
        }
        return null;
    }

    /**
     * Save the received rssi in the antenna historic
     * @param rssi the rssi of the packet received
     * @param isRssiReceived true if the rssi has been received, false otherwise
     */
    public void saveRssi(int rssi, boolean isRssiReceived,
                         byte antennaId, Antenna.BLEChannel bleChannel) {
        if (antennaList != null) {
            final Antenna tmpAntenna = getAntennaById(antennaId);
            if (tmpAntenna != null) {
                currentAntenna = tmpAntenna;
                tmpAntenna.saveRssi(rssi, isRssiReceived, bleChannel);
                PSALogs.d("antenna", tmpAntenna.toString());
            } else {
                PSALogs.d("NIH", "tmpAntenna is null, cannot save rssi");
            }
        } else {
            PSALogs.d("NIH", "antennaList is null, cannot save rssi");
        }
    }

    /**
     * Save the car rssi and ble channel
     * @param rssi the rssi to save
     */
    public void saveCarRssi(final int rssi) {
        this.carRssi = rssi;
    }

    /**
     * Get the current antenna id
     *
     * @return the antenna id, or 0
     */
    public int getAntennaId() {
        if (currentAntenna != null) {
            return currentAntenna.getAntennaId();
        }
        return 0;
    }

    /**
     * Get the current original rssi
     * @return the current original rssi value, or 0
     */
    public int getCurrentOriginalRssi() { //TODO change to int[]
//        int[] currentOriginRssi = new int[antennaList.size()];
//        for (int i = 0; i < antennaList.size(); i++) {
//            currentOriginRssi[i] = antennaList.get(i).getCurrentOriginalRssi();
//        }
//        return currentOriginRssi[0];
        if (currentAntenna != null) {
            return currentAntenna.getCurrentOriginalRssi();
        }
        return 0;
    }

    /**
     * Get the current ble channel
     * @return the current ble channel, or ble channel unknown
     */
    public Antenna.BLEChannel getCurrentBLEChannel() {
        if (currentAntenna != null) {
            return currentAntenna.getCurrentBLEChannel();
        }
        return Antenna.BLEChannel.UNKNOWN;
    }

    public int getTrxNumber() {
        return trxNumber;
    }

    public String getTrxName() {
        return trxName;
    }

    public int getCarRssi() {
        return carRssi;
    }

    public String getTrxAddress() {
        return trxAddress;
    }

    public void setTrxAddress(String trxAddress) {
        this.trxAddress = trxAddress;
    }
}
