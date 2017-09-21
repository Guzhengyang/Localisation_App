package com.valeo.bleranging.model;

import com.valeo.bleranging.utils.PSALogs;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static com.valeo.bleranging.utils.CheckUtils.checkForRssiNonNull;

/**
 * Created by l-avaratha on 10/07/2017
 */

public class MultiTrx {
    /**
     * The list of enabled trx
     */
    private final LinkedHashMap<Integer, Trx> trxLinkedHMap;
    /**
     * All the trx rssi
     */
    private double[] mRssiTab;

    /**
     * Constructor
     *
     * @param trxLinkedHMap the trx list
     */
    MultiTrx(LinkedHashMap<Integer, Trx> trxLinkedHMap) {
        this.trxLinkedHMap = trxLinkedHMap;
    }

    /**
     * Save an incoming rssi
     *
     * @param trxNumber  the trx that sent the signal
     * @param rssi       the rssi value to save
     * @param bleChannel the ble Channel
     */
    public void saveRssi(final int trxNumber, final int rssi, final byte antennaId, Antenna.BLEChannel bleChannel) {
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

    /**
     * Get the trx, with id trxNumber, current ble channel
     * @param trxNumber the trx id number
     * @return the current ble channel or UNKNOWN
     */
    public Antenna.BLEChannel getCurrentBLEChannel(final int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentBLEChannel();
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
    }

    /**
     * Get the trx, with id trxNumber, current rssi
     * @param trxNumber the trx id number
     * @return the current rssi or 2
     */
    public int getCurrentOriginalRssi(final int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentOriginalRssi();
        } else {
            return 2;
        }
    }

    /**
     * Get the trx, with id trxNumber, current antenna id
     * @param trxNumber the trx id number
     * @return the current antenna id or 0
     */
    public int getCurrentAntennaId(final int trxNumber) {
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

    /**
     * Check if the trx with trx id number is active
     * @param trxNumber the trx id number
     * @return true if the trx is active, false otherwise
     */
    public boolean isActive(final int trxNumber) {
        return trxLinkedHMap.get(trxNumber) != null && trxLinkedHMap.get(trxNumber).isActive();
    }

    /**
     * Get the rssi tab
     *
     * @return the rssi tab
     */
    double[] getRssiTab() {
        return mRssiTab;
    }

    /**
     * Get the trx list
     * @return the list of trx
     */
    public LinkedHashMap<Integer, Trx> getTrxLinkedHMap() {
        return trxLinkedHMap;
    }
}
