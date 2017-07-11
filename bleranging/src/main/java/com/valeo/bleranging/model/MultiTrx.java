package com.valeo.bleranging.model;

import com.valeo.bleranging.utils.PSALogs;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static com.valeo.bleranging.utils.CheckUtils.checkForRssiNonNull;

/**
 * Created by l-avaratha on 10/07/2017
 */

public class MultiTrx {
    private LinkedHashMap<Integer, Trx> trxLinkedHMap;
    private double[] mRssiTab;

    public MultiTrx(LinkedHashMap<Integer, Trx> trxLinkedHMap) {
        this.trxLinkedHMap = trxLinkedHMap;
    }

    /**
     * Save an incoming rssi
     *
     * @param trxNumber  the trx that sent the signal
     * @param rssi       the rssi value to save
     * @param bleChannel the ble Channel
     */
    public void saveRssi(int trxNumber, int rssi, byte antennaId, Antenna.BLEChannel bleChannel) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            PSALogs.d("NIH", "trx n°" + trxNumber + " rssi saved");
            trxLinkedHMap.get(trxNumber).saveRssi(rssi, true, antennaId, bleChannel);
        } else {
            PSALogs.d("NIH", "trx n°" + trxNumber + " is NULL, cannot save rssi");
        }
    }

    /**
     * Save an incoming rssi from car
     *
     * @param trxNumber the trx that sent the signal
     * @param rssi      the rssi value to save
     */
    public void saveCarRssi(final int trxNumber, final int rssi) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            trxLinkedHMap.get(trxNumber).saveCarRssi(rssi);
        } else {
            PSALogs.d("NIH", "trx is null, cannot save car rssi");
        }
    }

    /**
     * Save an incoming rssi from car
     *
     * @param trxNumber the trx that sent the signal
     * @param address   the trx mac address to save
     */
    public void saveCarAddress(final int trxNumber, final String address) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            if (trxLinkedHMap.get(trxNumber).getTrxAddress() == null
                    || trxLinkedHMap.get(trxNumber).getTrxAddress().isEmpty()) {
                trxLinkedHMap.get(trxNumber).setTrxAddress(address);
            }
            if (trxLinkedHMap.get(trxNumber).getTrxAddress().equalsIgnoreCase(address)) {
                PSALogs.d("NIH", "trx address match the saved address");
            } else {
                PSALogs.d("NIH", "trx address do not match the saved address");
            }
        } else {
            PSALogs.d("NIH", "trx is null, cannot save car address");
        }
    }

    public LinkedHashMap<Integer, Trx> getTrxLinkedHMap() {
        return trxLinkedHMap;
    }

    public Antenna.BLEChannel getCurrentBLEChannel(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentBLEChannel();
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
    }

    public int getCurrentOriginalRssi(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentOriginalRssi();
        } else {
            return 2;
        }
    }

    public int getCurrentAntennaId(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getAntennaId();
        } else {
            return 0;
        }
    }

    /**
     * Get rssi from beacon to make a prediction
     *
     * @return an array with a rssi from each beacon
     */
    public double[] getRssiForRangingPrediction() {
        if (trxLinkedHMap != null) {
            this.mRssiTab = new double[trxLinkedHMap.size()];
            Iterator<Trx> trxIterator = trxLinkedHMap.values().iterator();
            for (int i = 0; i < trxLinkedHMap.size(); i++) {
                if (trxIterator.hasNext()) {
                    this.mRssiTab[i] = getCurrentOriginalRssi(trxIterator.next().getTrxNumber());
                } else {
                    this.mRssiTab[i] = 1;
                }
            }
        } else {
            PSALogs.d("init2", "getRssiForRangingPrediction trxLinkedHMap is NULL\n");
        }
        return checkForRssiNonNull(this.mRssiTab);
    }

    public boolean isActive(int trxNumber) {
        return trxLinkedHMap.get(trxNumber) != null && trxLinkedHMap.get(trxNumber).isActive();
    }

    public double[] getRssiTab() {
        return mRssiTab;
    }
}
