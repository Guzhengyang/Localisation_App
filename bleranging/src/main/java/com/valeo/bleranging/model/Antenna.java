package com.valeo.bleranging.model;

import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.model.Trx.ANTENNA_ID_1;
import static com.valeo.bleranging.model.Trx.ANTENNA_ID_2;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Antenna {
    public static final int AVERAGE_DEFAULT = 0;
    public static final int AVERAGE_START = 1;
    public static final int AVERAGE_LOCK = 2;
    public static final int AVERAGE_UNLOCK = 3;
    public static final int AVERAGE_WELCOME = 4;
    public static final int AVERAGE_LONG = 5;
    public static final int AVERAGE_SHORT = 6;
    public static final int AVERAGE_DELTA_LOCK = 7;   // use for threshold calculation
    public static final int AVERAGE_DELTA_UNLOCK = 8; // use for threshold calculation
    private final AtomicBoolean isAntennaActive;
    private final AtomicBoolean hasReceivedRssi;
    private final ArrayList<Integer> rssiPente;
    private final ArrayList<Integer> rssiHistoric;
    private final int numberTrx;
    private final int antennaId;
    private int lastOriginalRssi;
    private int lastRssi;
    private int currentOriginalRssi;
    private BLEChannel lastBleChannel;
    private boolean lastIsSmartphoneMovingSlowly;
    private int antennaRssiAverage;
    private int antennaRssiAverageStart;
    private int antennaRssiAverageLock;
    private int antennaRssiAverageUnlock;
    private int antennaRssiAverageWelcome;
    private int antennaRssiAverageLong;
    private int antennaRssiAverageShort;
    //    private int defaultSum = 0;
//    private int startSum = 0;
//    private int lockSum = 0;
//    private int unlockSum = 0;
//    private int welcomeSum = 0;
//    private int longSum = 0;
//    private int shortSum = 0;
    private int ratioMaxMin;
    private int min = 0;
    private int max = -100;
    private int offsetBleChannel38 = 0;
    private boolean hasBeenInitialized = false;

    Antenna(int numberTrx, int antennaId) {
        this.numberTrx = numberTrx;
        this.antennaId = antennaId;
        this.isAntennaActive = new AtomicBoolean(true);
        this.hasReceivedRssi = new AtomicBoolean(false);
        this.rssiHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getRollingAvElement());
        this.rssiPente = new ArrayList<>(10);
    }

    public void init(int historicDefaultValue) {
        this.antennaRssiAverage = historicDefaultValue;
        this.antennaRssiAverageStart = historicDefaultValue;
        this.antennaRssiAverageLock = historicDefaultValue;
        this.antennaRssiAverageUnlock = historicDefaultValue;
        this.antennaRssiAverageWelcome = historicDefaultValue;
        this.antennaRssiAverageLong = historicDefaultValue;
        this.antennaRssiAverageShort = historicDefaultValue;
        initWithHysteresis(historicDefaultValue);
    }

    /**
     * Init with Hysteresis over all saved rssi
     */
    private synchronized void initWithHysteresis(int defaultValue) {
        for (int i = 0; i < SdkPreferencesHelper.getInstance().getRollingAvElement(); i++) {
            rssiHistoric.add(i, defaultValue);
        }
    }

    /**
     * Reset with Hysteresis over all saved rssi
     */
    synchronized void resetWithHysteresis(int defaultValue) {
        for (int i = 0; i < SdkPreferencesHelper.getInstance().getRollingAvElement(); i++) {
            rssiHistoric.set(i, defaultValue);
        }
    }

    private float getEcretageValue(int lastN2Rssi) {
        if (lastN2Rssi >= -170 && lastN2Rssi < -70) {
            return SdkPreferencesHelper.getInstance().getEcretage70_100(SdkPreferencesHelper.getInstance().getConnectedCarType());
        } else if (lastN2Rssi >= -70 && lastN2Rssi < -50) {
            return SdkPreferencesHelper.getInstance().getEcretage50_70(SdkPreferencesHelper.getInstance().getConnectedCarType());
        } else if (lastN2Rssi >= -50 && lastN2Rssi < -30) {
            return SdkPreferencesHelper.getInstance().getEcretage30_50(SdkPreferencesHelper.getInstance().getConnectedCarType());
        } else if (lastN2Rssi >= -30 && lastN2Rssi < +30) {
            return SdkPreferencesHelper.getInstance().getEcretage30_30(SdkPreferencesHelper.getInstance().getConnectedCarType());
        }
        return 0;
    }

    /**
     * Correct the rssi received within BORNE_INF and BORNE_SUP
     *
     * @param rssi       the rssi to correct
     * @param bleChannel the ble channel from which the rssi come
     * @return the corrected rssi
     */
    private int getCorrectedRssi(int rssi, BLEChannel bleChannel) {
        int borneInf = (int) (lastRssi - getEcretageValue(lastRssi));
        int borneSup = (int) (lastRssi + getEcretageValue(lastRssi));
        PSALogs.d("ecretage" + antennaId, numberTrx + " lastRssi:" + lastRssi + " borneInf:" + borneInf + " borneSup:" + borneSup);
        switch (bleChannel) {
            case BLE_CHANNEL_37:
                offsetBleChannel38 = 0;
                break;
            case BLE_CHANNEL_38:
                if (!this.lastBleChannel.equals(bleChannel)) { // different channel, calculate offset
                    offsetBleChannel38 = calculateOffsetChannel(rssi);
                }
                rssi += offsetBleChannel38;
                break;
            case BLE_CHANNEL_39:
                if (!this.lastBleChannel.equals(bleChannel)) { // different channel, calculate offset
                    offsetBleChannel38 = 0;
                }
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
        if (rssi > borneSup) {
            return borneSup;
        } else if (rssi < borneInf) {
            return borneInf;
        } else {
            return rssi;
        }
    }

    /**
     * Calculate an offset for this channel
     * @param rssi the rssi just received from the new channel
     * @return the offset of the new channel
     */
    private int calculateOffsetChannel(int rssi) {
        int offsetBleChannel = antennaRssiAverageWelcome - rssi;
        if (offsetBleChannel > 10) {
            offsetBleChannel = 10;
        } else if (offsetBleChannel < -10) {
            offsetBleChannel = -10;
        }
        return offsetBleChannel;
    }

    /**
     * Calculate a rolling average of the rssi
     */
    private synchronized void rollingAverageRssi(boolean isSmartphoneLaid) {
//        lastRssi = rssi; // Need last rssi for ecretage in next round
//        int historicSize = rssiHistoric.size();
//        int defaultSize = SdkPreferencesHelper.getInstance().getRollingAvElement();
//        int startSize = SdkPreferencesHelper.getInstance().getStartNbElement();
//        int lockSize = SdkPreferencesHelper.getInstance().getLockNbElement();
//        int unlockSize = SdkPreferencesHelper.getInstance().getUnlockNbElement();
//        int welcomeSize = SdkPreferencesHelper.getInstance().getWelcomeNbElement();
//        int longSize = SdkPreferencesHelper.getInstance().getLongNbElement();
//        int shortSize = SdkPreferencesHelper.getInstance().getShortNbElement();
//        PSALogs.e("moy", "historicSize " + historicSize);
//        if (historicSize >= defaultSize) {
//            PSALogs.e("moy", "defaultSum " + defaultSum);
//            defaultSum -= rssiHistoric.get(historicSize - defaultSize);
//            PSALogs.e("moy", "defaultSum " + defaultSum);
//        }
//        if (historicSize >= startSize) {
//            startSum -= rssiHistoric.get(historicSize - startSize);
//        }
//        if (historicSize >= lockSize) {
//            lockSum -= rssiHistoric.get(historicSize - lockSize);
//        }
//        if (historicSize >= unlockSize) {
//            unlockSum -= rssiHistoric.get(historicSize - unlockSize);
//        }
//        if (historicSize >= welcomeSize) {
//            welcomeSum -= rssiHistoric.get(historicSize - welcomeSize);
//        }
//        if (historicSize >= longSize) {
//            longSum -= rssiHistoric.get(historicSize - longSize);
//        }
//        if (historicSize >= shortSize) {
//            shortSum -= rssiHistoric.get(historicSize - shortSize);
//        }
//        defaultSum += rssi;
//        startSum += rssi;
//        lockSum += rssi;
//        unlockSum += rssi;
//        welcomeSum += rssi;
//        longSum += rssi;
//        shortSum += rssi;
//        antennaRssiAverage = defaultSum / defaultSize;
//        antennaRssiAverageStart = startSum / startSize;
//        antennaRssiAverageLock = lockSum / lockSize;
//        antennaRssiAverageUnlock = unlockSum / unlockSize;
//        antennaRssiAverageWelcome = welcomeSum / welcomeSize;
//        antennaRssiAverageLong = longSum / longSize;
//        antennaRssiAverageShort = shortSum / shortSize;
//        // If list full, remove first and add last
//        if (rssiHistoric.size() == defaultSize) {
//            rssiHistoric.remove(0);
//        }
//        this.rssiHistoric.add(rssi);
        antennaRssiAverage = 0;
        antennaRssiAverageStart = 0;
        antennaRssiAverageLock = 0;
        antennaRssiAverageUnlock = 0;
        antennaRssiAverageWelcome = 0;
        antennaRssiAverageLong = 0;
        antennaRssiAverageShort = 0;
        int toIndex = rssiHistoric.size();
        min = 0;
        max = -100;
        if (toIndex != 0) {
            int indexDefault = getFromIndex(AVERAGE_DEFAULT, isSmartphoneLaid);
            int indexStart = getFromIndex(AVERAGE_START, isSmartphoneLaid);
            int indexLock = getFromIndex(AVERAGE_LOCK, isSmartphoneLaid);
            int indexUnlock = getFromIndex(AVERAGE_UNLOCK, isSmartphoneLaid);
            int indexWelcome = getFromIndex(AVERAGE_WELCOME, isSmartphoneLaid);
            int indexLong = getFromIndex(AVERAGE_LONG, isSmartphoneLaid);
            int indexShort = getFromIndex(AVERAGE_SHORT, isSmartphoneLaid);
            int currentHistoricValue;
            for (int i = toIndex - 1; i >= 0; i--) {
                currentHistoricValue = rssiHistoric.get(i);
                min = Math.min(min, currentHistoricValue);
                max = Math.max(max, currentHistoricValue);
                antennaRssiAverage += currentHistoricValue;
                if (i >= indexStart) {
                    antennaRssiAverageStart += currentHistoricValue;
                }
                if (i >= indexLock) {
                    antennaRssiAverageLock += currentHistoricValue;
                }
                if (i >= indexUnlock) {
                    antennaRssiAverageUnlock += currentHistoricValue;
                }
                if (i >= indexWelcome) {
                    antennaRssiAverageWelcome += currentHistoricValue;
                }
                if (i >= indexLong) {
                    antennaRssiAverageLong += currentHistoricValue;
                }
                if (i >= indexShort) {
                    antennaRssiAverageShort += currentHistoricValue;
                }
            }
            antennaRssiAverage /= getStartIndexTabPosition(toIndex, indexDefault);
            antennaRssiAverageStart /= getStartIndexTabPosition(toIndex, indexStart);
            antennaRssiAverageLock /= getStartIndexTabPosition(toIndex, indexLock);
            antennaRssiAverageUnlock /= getStartIndexTabPosition(toIndex, indexUnlock);
            antennaRssiAverageWelcome /= getStartIndexTabPosition(toIndex, indexWelcome);
            antennaRssiAverageLong /= getStartIndexTabPosition(toIndex, indexLong);
            antennaRssiAverageShort /= getStartIndexTabPosition(toIndex, indexShort);
            ratioMaxMin = max - min;
        }
    }

    /**
     * Get a valid start index to calculate the mode dependant average
     * @param toIndex the tab size
     * @param fromIndex the starting index to check
     * @return the valid index, or 1 if the index is invalid
     */
    private int getStartIndexTabPosition(int toIndex, int fromIndex) {
        if ((fromIndex > 0) && (toIndex - fromIndex) > 0) {
            return toIndex - fromIndex;
        } else {
            return toIndex;
        }
    }

    /**
     * Save the received rssi in the antenna historic
     * @param rssi the rssi of the packet received
     * @param bleChannel the ble channel of the packet received
     */
    public synchronized void saveRssi(int rssi, BLEChannel bleChannel, boolean isSmartphoneMovingSlowly) {
        if (!hasBeenInitialized) {
            this.currentOriginalRssi = rssi;
            this.lastOriginalRssi = rssi;
            this.lastRssi = rssi;
            this.lastBleChannel = bleChannel;
            this.lastIsSmartphoneMovingSlowly = isSmartphoneMovingSlowly;
            hasBeenInitialized = true;
        }
        if (rssiPente.size() == 10) {
            rssiPente.remove(0);
        }
        rssi += getTrxRssiEqualizer(numberTrx); // add trx rssi antenna power Equalizer
        if (antennaId == ANTENNA_ID_1) {
            PSALogs.d("ecretage" + antennaId, numberTrx + " newRssi:" + rssi + " lastRssi:" + lastRssi);
        }
        if (antennaId == ANTENNA_ID_2) {
            PSALogs.d("ecretage" + antennaId, numberTrx + " newRssi:" + rssi + " lastRssi:" + lastRssi);
        }
        currentOriginalRssi = rssi;
        rssiPente.add(currentOriginalRssi - lastOriginalRssi);
        lastOriginalRssi = currentOriginalRssi;
        rssi = getCorrectedRssi(rssi, bleChannel); // Correct the rssi value with an ecretage on the last N-2 rssi seen
        lastRssi = rssi;
        if (rssiHistoric.size() == SdkPreferencesHelper.getInstance().getRollingAvElement()) {
            rssiHistoric.remove(0);
        }
        this.rssiHistoric.add(rssi);
        this.lastBleChannel = bleChannel;
        if (lastIsSmartphoneMovingSlowly != isSmartphoneMovingSlowly) {
//            resetWithHysteresis(antennaRssiAverageWelcome); //TODO concurrentModification
            lastIsSmartphoneMovingSlowly = isSmartphoneMovingSlowly;
        }
        rollingAverageRssi(isSmartphoneMovingSlowly);
        hasReceivedRssi.set(true);
        if (antennaId == ANTENNA_ID_1) {
            PSALogs.d("ecretage" + antennaId, numberTrx + " savedRssi:" + lastRssi);
        }
        if (antennaId == ANTENNA_ID_2) {
            PSALogs.d("ecretage" + antennaId, numberTrx + " savedRssi:" + lastRssi);
        }
    }

    /**
     * Retrieve the equalizer value for a trx
     * @param numberTrx the trx id
     * @return the value to equalize the trx
     */
    private int getTrxRssiEqualizer(int numberTrx) {
        switch (numberTrx) {
            case ConnectedCar.NUMBER_TRX_FRONT_LEFT:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerFrontLeft(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_FRONT_RIGHT:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerFrontRight(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_LEFT:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerLeft(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_MIDDLE:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerMiddle(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_RIGHT:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerRight(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_REAR_LEFT:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerRearLeft(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_BACK:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerBack(SdkPreferencesHelper.getInstance().getConnectedCarType());
            case ConnectedCar.NUMBER_TRX_REAR_RIGHT:
                return SdkPreferencesHelper.getInstance().getTrxRssiEqualizerRearRight(SdkPreferencesHelper.getInstance().getConnectedCarType());
            default:
                return 0;
        }
    }

    /**
     * Get the starting index for calculating an average (nb of all element - nb of mode element)
     * @param mode the mode of average
     * @param isSmartphoneLaid a boolean true if the smartphone is not moving, false otherwise
     * @return the index from where to start the average calculation
     */
    private int getFromIndex(int mode, boolean isSmartphoneLaid) {
        // if smartphone doesn't move, take all the tab
        if (isSmartphoneLaid) {
            return 0;
        }
        switch (mode) {
            case AVERAGE_DEFAULT:
                return 0;
            case AVERAGE_START:
                return rssiHistoric.size() - SdkPreferencesHelper.getInstance().getStartNbElement();
            case AVERAGE_LOCK:
                return rssiHistoric.size() - SdkPreferencesHelper.getInstance().getLockNbElement();
            case AVERAGE_UNLOCK:
                return rssiHistoric.size() - SdkPreferencesHelper.getInstance().getUnlockNbElement();
            case AVERAGE_WELCOME:
                return rssiHistoric.size() - SdkPreferencesHelper.getInstance().getWelcomeNbElement();
            case AVERAGE_LONG:
                return rssiHistoric.size() - SdkPreferencesHelper.getInstance().getLongNbElement();
            case AVERAGE_SHORT:
                return rssiHistoric.size() - SdkPreferencesHelper.getInstance().getShortNbElement();
            default:
                return 0;
        }

    }

    /**
     * Compare a new check with the last one, if they are equals the antenna is inactive
     * @return true if the antenna is active (checker are different), false otherwise (checker are equals)
     */
    public boolean isAntennaActive() {
        return isAntennaActive.get();
    }

    /**
     * Compare checker then set isAntennaActive and newChecker
     */
    public void compareCheckerAndSetAntennaActive() {
        // antenna active if antenna has received an rssi during the last 2,5 seconds
        isAntennaActive.set(hasReceivedRssi.get());
        hasReceivedRssi.set(false);
    }

    /**
     * Get the antenna rssi average for a mode
     * @param mode the mode of average
     * @return the average rssi for a mode
     */
    public int getAntennaRssiAverage(int mode) {
        switch (mode) {
            case AVERAGE_DEFAULT:
                return antennaRssiAverage;
            case AVERAGE_START:
                return antennaRssiAverageStart;
            case AVERAGE_LOCK:
                return antennaRssiAverageLock;
            case AVERAGE_UNLOCK:
                return antennaRssiAverageUnlock;
            case AVERAGE_WELCOME:
                return antennaRssiAverageWelcome;
            case AVERAGE_LONG:
                return antennaRssiAverageLong;
            case AVERAGE_SHORT:
                return antennaRssiAverageShort;
            default:
                return antennaRssiAverage;
        }
    }

    public int getRatioMaxMin() {
        return ratioMaxMin;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getOffsetBleChannel38() {
        return offsetBleChannel38;
    }

    public int getCurrentOriginalRssi() {
        return currentOriginalRssi;
    }

    public enum BLEChannel {
        BLE_CHANNEL_37, BLE_CHANNEL_38, BLE_CHANNEL_39, UNKNOWN
    }

}
