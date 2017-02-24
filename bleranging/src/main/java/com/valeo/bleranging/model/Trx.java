package com.valeo.bleranging.model;

import android.util.SparseIntArray;

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
    public void saveRssi(int rssi, boolean isRssiReceived, byte antennaId) {
        if (antennaList != null) {
            Antenna tmpAntenna = getAntennaById(antennaId);
            if (tmpAntenna != null) {
                tmpAntenna.saveRssi(rssi, isRssiReceived);
            }
        }
    }

    /**
     * Get the current original rssi
     *
     * @return the current original rssi value.
     */
    public int getCurrentOriginalRssi() { //TODO change to int[]
        int[] currentOriginRssi = new int[antennaList.size()];
        for (int i = 0; i < antennaList.size(); i++) {
            currentOriginRssi[i] = antennaList.get(i).getCurrentOriginalRssi();
        }
        return currentOriginRssi[0];
    }

    public Antenna.BLEChannel getCurrentBLEChannel() {
        return antennaList.get(0).getCurrentBLEChannel();
    }

    public void saveBleChannel(Antenna.BLEChannel bleChannel) {
        for (Antenna antenna : antennaList) {
            antenna.saveBleChannel(bleChannel);
        }
    }

    public int getTrxNumber() {
        return trxNumber;
    }

    public String getTrxName() {
        return trxName;
    }
}
