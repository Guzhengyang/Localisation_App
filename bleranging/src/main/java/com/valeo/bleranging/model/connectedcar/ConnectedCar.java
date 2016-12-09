package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.bluetooth.AlgoManager;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by l-avaratha on 05/09/2016
 */
public abstract class ConnectedCar {
    public final static int NUMBER_TRX_FRONT_LEFT = 1;
    public final static int NUMBER_TRX_FRONT_RIGHT = 2;
    public final static int NUMBER_TRX_LEFT = 3;
    public final static int NUMBER_TRX_MIDDLE = 4;
    public final static int NUMBER_TRX_RIGHT = 5;
    public final static int NUMBER_TRX_TRUNK = 6;
    public final static int NUMBER_TRX_REAR_LEFT = 7;
    public final static int NUMBER_TRX_BACK = 8;
    public final static int NUMBER_TRX_REAR_RIGHT = 9;
    final static String TRX_FRONT_LEFT_NAME = "FLeft";
    final static String TRX_FRONT_RIGHT_NAME = "FRight";
    final static String TRX_LEFT_NAME = "Left";
    final static String TRX_MIDDLE_NAME = "Mid";
    final static String TRX_RIGHT_NAME = "Right";
    final static String TRX_TRUNK_NAME = "Trunk";
    final static String TRX_REAR_LEFT_NAME = "RLeft";
    final static String TRX_BACK_NAME = "Back";
    final static String TRX_REAR_RIGHT_NAME = "RRight";
    private final static int RSSI_LOCK_DEFAULT_VALUE = -120;
    private final static int RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE = -50;
    private final static int RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE = -30;
    private final static int RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE = -70;
    private final static int RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE = -80;
    private final static String trxAddressFrontLeft = SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft();
    private final static String trxAddressFrontRight = SdkPreferencesHelper.getInstance().getTrxAddressFrontRight();
    private final static String trxAddressLeft = SdkPreferencesHelper.getInstance().getTrxAddressLeft();
    private final static String trxAddressMiddle = SdkPreferencesHelper.getInstance().getTrxAddressMiddle();
    private final static String trxAddressRight = SdkPreferencesHelper.getInstance().getTrxAddressRight();
    private final static String trxAddressTrunk = SdkPreferencesHelper.getInstance().getTrxAddressTrunk();
    private final static String trxAddressRearLeft = SdkPreferencesHelper.getInstance().getTrxAddressRearLeft();
    private final static String trxAddressBack = SdkPreferencesHelper.getInstance().getTrxAddressBack();
    private final static String trxAddressRearRight = SdkPreferencesHelper.getInstance().getTrxAddressRearRight();
    final LinkedHashMap<Integer, Trx> trxLinkedHMap;
    private final ConnectionNumber connectionNumber;
    int welcomeThreshold;
    int lockThreshold;
    int unlockThreshold;
    int startThreshold;
    int averageDeltaLockThreshold;
    int averageDeltaUnlockThreshold;
    int lockMode;
    int unlockMode;
    int startMode;
    int closeToBeaconThreshold;
    int nearDoorRatioThreshold;
    int nearBackDoorRatioThresholdMin;
    int nearBackDoorRatioThresholdMax;
    int nearDoorThresholdMLorMRMin;
    int nearDoorThresholdMLorMRMax;
    int nearDoorThresholdTLorTRMin;
    int nearDoorThresholdTLorTRMax;
    int nearDoorThresholdMRLorMRR;
    int nearDoorThresholdTRLorTRR;
    int nearDoorThresholdMB;
    int thresholdCloseToCar;
    Trx trxFrontLeft;
    Trx trxFrontRight;
    Trx trxLeft;
    Trx trxMiddle;
    Trx trxRight;
    Trx trxTrunk;
    Trx trxRearLeft;
    Trx trxBack;
    Trx trxRearRight;
    private Context mContext;
    private float linAccThreshold;

    ConnectedCar(Context mContext, ConnectionNumber connectionNumber, boolean isIndoor) {
        this.mContext = mContext;
        this.connectionNumber = connectionNumber;
        this.trxLinkedHMap = new LinkedHashMap<>();
        resetSettings(isIndoor);
    }

    public void resetSettings(boolean isIndoor) {
        String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        this.linAccThreshold = SdkPreferencesHelper.getInstance().getCorrectionLinAcc();
        if (isIndoor) {
            this.welcomeThreshold = SdkPreferencesHelper.getInstance().getIndoorWelcomeThreshold(connectedCarType);
            this.lockThreshold = SdkPreferencesHelper.getInstance().getIndoorLockThreshold(connectedCarType);
            this.unlockThreshold = SdkPreferencesHelper.getInstance().getIndoorUnlockThreshold(connectedCarType);
            this.startThreshold = SdkPreferencesHelper.getInstance().getIndoorStartThreshold(connectedCarType);
            this.averageDeltaLockThreshold = SdkPreferencesHelper.getInstance().getIndoorAverageDeltaLockThreshold(connectedCarType);
            this.averageDeltaUnlockThreshold = SdkPreferencesHelper.getInstance().getIndoorAverageDeltaUnlockThreshold(connectedCarType);
            this.closeToBeaconThreshold = SdkPreferencesHelper.getInstance().getIndoorCloseToBeaconThreshold(connectedCarType);
            this.nearDoorRatioThreshold = SdkPreferencesHelper.getInstance().getIndoorNearDoorRatioThreshold(connectedCarType);
            this.nearBackDoorRatioThresholdMin = SdkPreferencesHelper.getInstance().getIndoorNearBackDoorRatioThresholdMin(connectedCarType);
            this.nearBackDoorRatioThresholdMax = SdkPreferencesHelper.getInstance().getIndoorNearBackDoorRatioThresholdMax(connectedCarType);
            this.nearDoorThresholdMLorMRMin = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdMLorMRMin(connectedCarType);
            this.nearDoorThresholdMLorMRMax = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdMLorMRMax(connectedCarType);
            this.nearDoorThresholdTLorTRMin = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdTLorTRMin(connectedCarType);
            this.nearDoorThresholdTLorTRMax = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdTLorTRMax(connectedCarType);
            this.nearDoorThresholdMRLorMRR = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdMRLorMRR(connectedCarType);
            this.nearDoorThresholdTRLorTRR = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdTRLorTRR(connectedCarType);
            this.nearDoorThresholdMB = SdkPreferencesHelper.getInstance().getIndoorNearDoorThresholdMB(connectedCarType);
            this.thresholdCloseToCar = SdkPreferencesHelper.getInstance().getIndoorRatioCloseToCarThreshold(connectedCarType);
        } else {
            this.welcomeThreshold = SdkPreferencesHelper.getInstance().getOutsideWelcomeThreshold(connectedCarType);
            this.lockThreshold = SdkPreferencesHelper.getInstance().getOutsideLockThreshold(connectedCarType);
            this.unlockThreshold = SdkPreferencesHelper.getInstance().getOutsideUnlockThreshold(connectedCarType);
            this.startThreshold = SdkPreferencesHelper.getInstance().getOutsideStartThreshold(connectedCarType);
            this.averageDeltaLockThreshold = SdkPreferencesHelper.getInstance().getOutsideAverageDeltaLockThreshold(connectedCarType);
            this.averageDeltaUnlockThreshold = SdkPreferencesHelper.getInstance().getOutsideAverageDeltaUnlockThreshold(connectedCarType);
            this.closeToBeaconThreshold = SdkPreferencesHelper.getInstance().getOutsideCloseToBeaconThreshold(connectedCarType);
            this.nearDoorRatioThreshold = SdkPreferencesHelper.getInstance().getOutsideNearDoorRatioThreshold(connectedCarType);
            this.nearBackDoorRatioThresholdMin = SdkPreferencesHelper.getInstance().getOutsideNearBackDoorRatioThresholdMin(connectedCarType);
            this.nearBackDoorRatioThresholdMax = SdkPreferencesHelper.getInstance().getOutsideNearBackDoorRatioThresholdMax(connectedCarType);
            this.nearDoorThresholdMLorMRMin = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdMLorMRMin(connectedCarType);
            this.nearDoorThresholdMLorMRMax = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdMLorMRMax(connectedCarType);
            this.nearDoorThresholdTLorTRMin = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdTLorTRMin(connectedCarType);
            this.nearDoorThresholdTLorTRMax = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdTLorTRMax(connectedCarType);
            this.nearDoorThresholdMRLorMRR = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdMRLorMRR(connectedCarType);
            this.nearDoorThresholdTRLorTRR = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdTRLorTRR(connectedCarType);
            this.nearDoorThresholdMB = SdkPreferencesHelper.getInstance().getOutsideNearDoorThresholdMB(connectedCarType);
            this.thresholdCloseToCar = SdkPreferencesHelper.getInstance().getOutsideRatioCloseToCarThreshold(connectedCarType);
        }
        this.lockMode = SdkPreferencesHelper.getInstance().getLockMode(connectedCarType);
        this.unlockMode = SdkPreferencesHelper.getInstance().getUnlockMode(connectedCarType);
        this.startMode = SdkPreferencesHelper.getInstance().getStartMode(connectedCarType);
    }

    public void updateThresholdValues(boolean isIndoor, boolean smartphoneIsInPocket, boolean smartphoneComIsActivated) {
        this.welcomeThreshold = TrxUtils.getCurrentThreshold(Antenna.AVERAGE_WELCOME, isIndoor, smartphoneIsInPocket, smartphoneComIsActivated);
        this.lockThreshold = TrxUtils.getCurrentThreshold(Antenna.AVERAGE_LOCK, isIndoor, smartphoneIsInPocket, smartphoneComIsActivated);
        this.unlockThreshold = TrxUtils.getCurrentThreshold(Antenna.AVERAGE_UNLOCK, isIndoor, smartphoneIsInPocket, smartphoneComIsActivated);
        this.startThreshold = TrxUtils.getCurrentThreshold(Antenna.AVERAGE_START, isIndoor, smartphoneIsInPocket, smartphoneComIsActivated);
        this.averageDeltaLockThreshold = TrxUtils.getCurrentThreshold(Antenna.AVERAGE_DELTA_LOCK, isIndoor, smartphoneIsInPocket, smartphoneComIsActivated);
        this.averageDeltaUnlockThreshold = TrxUtils.getCurrentThreshold(Antenna.AVERAGE_DELTA_UNLOCK, isIndoor, smartphoneIsInPocket, smartphoneComIsActivated);
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     * @param newLockStatus the lock status that determines which values to set
     */
    public void initializeTrx(boolean newLockStatus) {
        if (newLockStatus) {
            initializeTrx(RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE);
        } else {
            initializeTrx(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
        }
    }

    /**
     * Reset rssi historic with new value
     * @param newLockStatus         the lock status
     * @param isUnlockStrategyValid the unlock strategy result
     */
    public synchronized void resetWithHysteresis(boolean newLockStatus, List<Integer> isUnlockStrategyValid) {
        if (!newLockStatus && isUnlockStrategyValid != null) { // just perform an unlock
            for (Integer trxNumber : isUnlockStrategyValid) {
                switch (trxNumber) {
                    case ConnectedCar.NUMBER_TRX_LEFT:
                        resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                        break;
                    case ConnectedCar.NUMBER_TRX_RIGHT:
                        resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE);
                        break;
                    case ConnectedCar.NUMBER_TRX_BACK:
                        resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                        break;
                    default:
                        resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                                RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                        break;
                }
            }
        } else { // just perform a lock
            resetTrxWithHysteresis(RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE,
                    RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE,
                    RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE);
        }
    }

    /**
     * Reset Trxs with these values
     *
     * @param valueMiddle     the middle trx new value
     * @param valueLeft       the left trx new value
     * @param valueRight      the right trx new value
     * @param valueBack       the back trx new value
     * @param valueFrontLeft  the front left trx new value
     * @param valueFrontRight the front right trx new value
     * @param valueRearLeft   the rear left trx new value
     * @param valueRearRight  the rear right trx new value
     */
    private synchronized void resetTrxWithHysteresis(int valueMiddle, int valueLeft, int valueRight,
                                                     int valueBack, int valueTrunk, int valueFrontLeft, int valueFrontRight,
                                                     int valueRearLeft, int valueRearRight) {
        if(trxFrontLeft != null) {
            trxFrontLeft.resetWithHysteresis(valueFrontLeft);
        }
        if (trxFrontRight != null) {
            trxFrontRight.resetWithHysteresis(valueFrontRight);
        }
        if (trxLeft != null) {
            trxLeft.resetWithHysteresis(valueLeft);
        }
        if(trxMiddle != null) {
            trxMiddle.resetWithHysteresis(valueMiddle);
        }
        if(trxRight != null) {
            trxRight.resetWithHysteresis(valueRight);
        }
        if (trxTrunk != null) {
            trxTrunk.resetWithHysteresis(valueTrunk);
        }
        if (trxRearLeft != null) {
            trxRearLeft.resetWithHysteresis(valueRearLeft);
        }
        if(trxBack != null) {
            trxBack.resetWithHysteresis(valueBack);
        }
        if (trxRearRight != null) {
            trxRearRight.resetWithHysteresis(valueRearRight);
        }
    }

    /**
     * Save the current ble channel
     * @param trxNumber                the trx that sent the signal
     * @param bleChannel               the ble channel used to sent
     */
    public void saveBleChannel(int trxNumber, Antenna.BLEChannel bleChannel) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            trxLinkedHMap.get(trxNumber).saveBleChannel(bleChannel);
        }
    }

    /**
     * Save an incoming rssi
     * @param trxNumber                the trx that sent the signal
     * @param rssi                     the rssi value to save
     * @param smartphoneIsMovingSlowly the boolean that determines if the smartphone is moving or not
     */
    public void saveRssi(int trxNumber, int rssi, boolean smartphoneIsMovingSlowly) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            trxLinkedHMap.get(trxNumber).saveRssi(rssi, smartphoneIsMovingSlowly, true);
        }
    }

    private int getRssiAverage(int trxNumber, int averageMode) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getAntennaRssiAverage(averageMode);
        } else {
            return 0;
        }
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
            return 0;
        }
    }

    public int getCurrentModifiedRssi(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentModifiedRssi();
        } else {
            return 0;
        }
    }

    /**
     * Check all trx antenna to see if they are active
     * @param smartphoneIsMovingSlowly true is smartphone is moving slowly, false otherwise
     */
    public void compareCheckerAndSetAntennaActive(boolean smartphoneIsMovingSlowly) {
        for (Trx trx : trxLinkedHMap.values()) {
            trx.compareCheckerAndSetAntennaActive();
            if (trx.isEnabled() && !trx.isActive()) {
                trx.saveRssi(getCurrentDisconnectedRssi(trx.getTrxNumber()), smartphoneIsMovingSlowly, false);
            }
        }
    }

    private int getCurrentDisconnectedRssi(int trxNumber) {
        if (trxLinkedHMap.get(NUMBER_TRX_MIDDLE) != null) {
            return trxLinkedHMap.get(NUMBER_TRX_MIDDLE).getCurrentModifiedRssi() +
                    (trxLinkedHMap.get(NUMBER_TRX_MIDDLE).getCurrentModifiedRssi() - trxLinkedHMap.get(trxNumber).getCurrentModifiedRssi());
        }
        return trxLinkedHMap.get(NUMBER_TRX_MIDDLE).getCurrentModifiedRssi();
    }

    // GET AVERAGES AND RATIOS

    /**
     * Calculate all the trx average
     * @param averageMode the average mode
     * @return the average of all active trx or 0 if there is none
     */
    public int getAllTrxAverage(int averageMode) {
        int totalAverage = 0;
        int numberOfAntenna = 0;
        for (Trx trx : trxLinkedHMap.values()) {
            if (trx.isEnabled()) {
                totalAverage += (trx.getAntennaRssiAverage(averageMode));
                numberOfAntenna++;
            }
        }
        if (numberOfAntenna == 0) {
            // If all trx are down restart the app
//            Intent i = mContext.getPackageManager()
//                    .getLaunchIntentForPackage(mContext.getPackageName() );
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            int mPendingIntentId = 123456;
//            PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, i, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager mgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//            System.exit(0);
            return 0;
        }
        totalAverage /= numberOfAntenna;
        return totalAverage;
    }

    /**
     * Calculate Next to Door delta rssi
     *
     * @param mode the average mode of calculation
     * @param trx1 the first trx
     * @param trx2 the second trx
     * @return the delta between both trx average rssi
     */
    int getRatioBetweenTwoTrx(int mode, int trx1, int trx2) {
        if (trxLinkedHMap != null) {
            Trx trxOne = trxLinkedHMap.get(trx1);
            Trx trxTwo = trxLinkedHMap.get(trx2);
            if (trxOne != null && trxTwo != null) {
                if (trxOne.isEnabled() && trxTwo.isEnabled()) {
                    int trxAverageRssi1 = trxOne.getAntennaRssiAverage(mode);
                    int trxAverageRssi2 = trxTwo.getAntennaRssiAverage(mode);
                    return trxAverageRssi1 - trxAverageRssi2;
                }
                return 0;
            }
            return 0;
        } else {
            return 0;
        }
    }

//    protected int getRatioMaxMin(int trxNumber1, int trxNumber2, int trxNumber3,
//                                 int trxNumber4, int trxNumber5, int trxNumber6, int mode) {
//        if (trxLinkedHMap != null) {
//            int max = Math.max(Math.max(trxLinkedHMap.get(trxNumber1).getAntennaRssiAverage(mode),
//                    trxLinkedHMap.get(trxNumber2).getAntennaRssiAverage(mode)),
//                    trxLinkedHMap.get(trxNumber3).getAntennaRssiAverage(mode));
//            int min = Math.min(Math.min(trxLinkedHMap.get(trxNumber4).getAntennaRssiAverage(mode),
//                    trxLinkedHMap.get(trxNumber5).getAntennaRssiAverage(mode)),
//                    trxLinkedHMap.get(trxNumber6).getAntennaRssiAverage(mode));
//            return max - min;
//        }
//        return 0;
//    }

    int getRatioCloseToCar(int trxNumber, int mode1, int mode2) {
        if (trxLinkedHMap != null) {
            int average = trxLinkedHMap.get(trxNumber).getAntennaRssiAverage(mode1);
            int minimum = getMinAverageRssi(mode2);
//            PSALogs.d("close", "getAntennaRssiAverage = " + average);
//            PSALogs.d("close", "getMinAverageRssi = " + minimum);
//            PSALogs.d("close", "getRatioCloseToCar = " + (average - minimum));
            return (average - minimum);
        }
        return 0;
    }

    /**
     * Find corner trx minimum average
     *
     * @param mode the average mode
     * @return the corner trx minimum average
     */
    private int getMinAverageRssi(int mode) {
        int min = 0;
        if (trxLinkedHMap != null) {
            for (Integer trxNumber : trxLinkedHMap.keySet()) {
                if (trxNumber != NUMBER_TRX_LEFT
                        && trxNumber != NUMBER_TRX_RIGHT && trxNumber != NUMBER_TRX_MIDDLE) {
                    min = Math.min(min, trxLinkedHMap.get(trxNumber).getAntennaRssiAverage(mode));
                }
            }
        }
        return min;
    }

    /**
     * Find three corner lower trx min max, take 80% of it
     *
     * @return the three corner lower trx min max 80% of it, or 15 if it's lower than 15.
     */
    protected int getThreeCornerLowerMaxMinRatio() {
        int trxToIgnore = -1;
        int maxOne = -100;
        if (trxLinkedHMap != null) {
            // Find the trx to ignore in the next loop
            for (Integer trxNumber : trxLinkedHMap.keySet()) {
                if (trxNumber != NUMBER_TRX_LEFT && trxNumber != NUMBER_TRX_RIGHT
                        && trxNumber != NUMBER_TRX_MIDDLE && trxNumber != NUMBER_TRX_TRUNK) {
                    if (maxOne < trxLinkedHMap.get(trxNumber).getRatioMaxMin()) {
                        trxToIgnore = trxNumber;
                        maxOne = trxLinkedHMap.get(trxNumber).getRatioMaxMin();
                    }
                }
            }
//            PSALogs.d("max", "maxOne " + maxOne);
            int maxTwo = -100;
            int minTwo = 0;
            for (Integer trxNumber : trxLinkedHMap.keySet()) {
                if (trxNumber != trxToIgnore // previous loop trx to ignore because it is the maxOne
                        && trxNumber != NUMBER_TRX_LEFT && trxNumber != NUMBER_TRX_RIGHT
                        && trxNumber != NUMBER_TRX_MIDDLE && trxNumber != NUMBER_TRX_TRUNK) {
                    minTwo = Math.min(minTwo, trxLinkedHMap.get(trxNumber).getMin());
                    maxTwo = Math.max(maxTwo, trxLinkedHMap.get(trxNumber).getMax());
                }
            }
            int result = ((maxTwo - minTwo) * 80) / 100;
//            PSALogs.d("max", "maxTwo " + maxTwo + ", minTwo " + minTwo + ", result " + result);
            if (result < 15) {
                return 15;
            } else {
                return result;
            }
        }
        return -1;
    }

    /**
     * Calculate the delta between the average (Long and Short) of trx 's averages
     *
     * @return the delta between the average(L&S) of trx s average
     */
    int getAverageLSDelta() {
        int averageLong = getAllTrxAverage(Antenna.AVERAGE_LONG);
        int averageShort = getAllTrxAverage(Antenna.AVERAGE_SHORT);
        return (averageLong - averageShort);
    }

    // COMPARE UTILS

    /**
     * Calculate two TRX average RSSI and compare their difference with thresold
     *
     * @param mode      the average mode of calculation
     * @param trx1      the first trx
     * @param trx2      the second trx
     * @param threshold the threshold to compare with
     * @param isGreater true to compare with >, false to compare with <
     * @return true if the difference of the two average rssi is greater than the threshold, false otherwise
     */
    boolean compareRatioWithThreshold(int mode, int trx1, int trx2, int threshold, boolean isGreater) {
        return TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(mode, trx1, trx2), threshold, isGreater);
    }

    /**
     * Calculate the TRX RSSI and compare with thresold
     *
     * @param trxNumber   the trx number to compare
     * @param averageMode the mode of average
     * @param threshold   the threshold to compare with
     * @param isGreater   true if compare sign is >, false if it is <
     * @return true if the rssi of each antenna is greater than the threshold, false otherwise
     */
    boolean compareTrxWithThreshold(int trxNumber, int averageMode, int threshold, boolean isGreater) {
        return trxLinkedHMap.get(trxNumber) != null
                && trxLinkedHMap.get(trxNumber).compareTrxWithThreshold(averageMode, threshold, isGreater);
    }

    public boolean isActive(int trxNumber) {
        return trxLinkedHMap.get(trxNumber) != null && trxLinkedHMap.get(trxNumber).isActive();
    }

    public int getCurrentOffset38(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getOffset38();
        } else {
            return 0;
        }
    }

    public int getCurrentOffset39(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getOffset39();
        } else {
            return 0;
        }
    }

    /**
     * Condition to enable Start action
     * @return true if the strategy is verified, false otherwise
     */
    public abstract List<Integer> startStrategy();

    /**
     * Check if we are in start area
     * @return true if we are in start area, false otherwise
     */
    public abstract boolean isInStartArea(int threshold);

    /**
     * Condition to enable unlock action
     * @return true if the strategy is verified, false otherwise
     */
    public abstract List<Integer> unlockStrategy();

    /**
     * Check if we are in unlock area
     * @return true if we are in unlock area, false otherwise
     */
    public abstract boolean isInUnlockArea(int threshold);

    /**
     * Condition to enable lock action
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean lockStrategy();

    /**
     * Check if we are in lock area
     * @return true if we are in lock area, false otherwise
     */
    public abstract boolean isInLockArea(int threshold);

    /**
     * Condition to enable welcome action
     * @param totalAverage  the total average of all antenna rssi
     * @param newlockStatus the lock status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean welcomeStrategy(int totalAverage, boolean newlockStatus);

    /**
     * Check if the number of trx valid is available
     * @param numberOfTrxNeeded the number of trx needed
     * @param trxBoolLinkedHMap the list of boolean to check
     * @return true if the number of trx valid is greater or equal to the number of trx valid needed, false otherwise
     */
    boolean numberOfTrxValid(int numberOfTrxNeeded, LinkedHashMap<Integer, Boolean> trxBoolLinkedHMap) {
        if (trxBoolLinkedHMap != null) {
            int counter = 0;
            for (Integer trxNumber : trxBoolLinkedHMap.keySet()) {
                if (trxBoolLinkedHMap.get(trxNumber)) {
                    counter++;
                }
            }
            return counter >= numberOfTrxNeeded;
        }
        return false;
    }

    /**
     * Create a string of header debug
     * @param spannableStringBuilder the spannable string builder to fill
     * @param bytesToSend              the bytes to send
     * @param bytesReceived            the bytes received
     * @param isFullyConnected         the boolean that determine if the smartphone is connected or not
     * @return the spannable string builder filled with the header
     */
    public SpannableStringBuilder createHeaderDebugData(
            SpannableStringBuilder spannableStringBuilder, final byte[] bytesToSend, final byte[] bytesReceived, boolean isFullyConnected) {
        if (isFullyConnected) {
            if (bytesToSend != null) {
                spannableStringBuilder.append("       Send:       ").append(TextUtils.printBleBytes((bytesToSend))).append("\n");
            }
            if (bytesReceived != null) {
                spannableStringBuilder.append("       Receive: ").append(TextUtils.printBleBytes(bytesReceived)).append("\n");
            }
        } else {
            SpannableString disconnectedSpanString = new SpannableString("Disconnected\n");
            disconnectedSpanString.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, "Disconnected\n".length(), 0);
            spannableStringBuilder.append(disconnectedSpanString);
        }
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        return spannableStringBuilder;
    }

    /**
     * Create a string of footer debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     * @return the spannable string builder filled with the first footer
     */
    public abstract SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder);

    /**
     * Create a string of footer debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     * @return the spannable string builder filled with the first footer
     */
    SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder, String space1, String space2) {
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder
                    .append(space1)
                    .append(TextUtils.colorText(isActive(trx.getTrxNumber()), trx.getTrxName(), Color.WHITE, Color.DKGRAY))
                    .append(space1);
        }
        spannableStringBuilder.append("\n");
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder
                    .append(space2)
                    .append(String.format(Locale.FRANCE, "%1$03d",
                            getCurrentOriginalRssi(trx.getTrxNumber())))
                    .append(space2);
        }
        spannableStringBuilder.append('\n');
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder
                    .append(space2)
                    .append(String.format(Locale.FRANCE, "%1$03d",
                            getRssiAverage(trx.getTrxNumber(), Antenna.AVERAGE_DEFAULT)))
                    .append(space2);
        }
        spannableStringBuilder.append('\n');
        spannableStringBuilder
                .append("                               ").append("Total :").append(" ")
                .append(String.format(Locale.FRANCE, "%1$03d", getAllTrxAverage(Antenna.AVERAGE_DEFAULT)))
                .append("\n");
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        return spannableStringBuilder;
    }

    /**
     * Get the string from the second footer
     *
     * @param spannableStringBuilder   the string builder to fill
     * @param totalAverage             the total average of all trx
     * @param mAlgoManager             the algorithm manager
     * @return the spannable string builder filled with the second footer
     */
    public abstract SpannableStringBuilder createSecondFooterDebugData(
            SpannableStringBuilder spannableStringBuilder, int totalAverage, AlgoManager mAlgoManager);

    SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder, String space2) {
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append("offset channel 38 :\n");
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder
                    .append(space2)
                    .append(String.format(Locale.FRANCE, "%1$03d", getCurrentOffset38(trx.getTrxNumber())))
                    .append(space2);
        }
        spannableStringBuilder.append('\n');
        spannableStringBuilder.append("offset channel 39 :\n");
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder
                    .append(space2)
                    .append(String.format(Locale.FRANCE, "%1$03d", getCurrentOffset39(trx.getTrxNumber())))
                    .append(space2);
        }
        spannableStringBuilder.append('\n');
        return spannableStringBuilder;
    }

    /**
     * Get the string from the third footer
     *
     * @param spannableStringBuilder   the string builder to fill
     * @param mAlgoManager the algorithm manager
     * @return the string builder filled with the third footer data
     */
    public SpannableStringBuilder createThirdFooterDebugData(
            SpannableStringBuilder spannableStringBuilder, AlgoManager mAlgoManager) {
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append("Scanning on channel: ").append(getCurrentBLEChannel(NUMBER_TRX_RIGHT).toString()).append("\n");
        String lAccStringBuilder = "Linear Acceleration < (" + linAccThreshold + "): "
                + String.format(Locale.FRANCE, "%1$.4f", mAlgoManager.getDeltaLinAcc()) + "\n";
        spannableStringBuilder.append(TextUtils.colorText(mAlgoManager.isSmartphoneMovingSlowly(),
                lAccStringBuilder, Color.WHITE, Color.DKGRAY));
        return spannableStringBuilder;
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param historicDefaultValuePeriph  the peripheral trx default value
     * @param historicDefaultValueCentral the central trx default value
     */
    protected abstract void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral);


    /**
     * Color each antenna average with color if comparaisonSign (> or <) threshold, DK_GRAY otherwise
     *
     * @param mode            the average mode to calculate
     * @param color           the color to use if the conditions is checked
     * @param threshold       the threshold to compare with
     * @param comparaisonSign the comparaison sign
     * @return a colored spannablestringbuilder with all the trx's average
     */
    SpannableStringBuilder printModedAverage(int mode, int color, int threshold,
                                             String comparaisonSign, String space) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (Trx trx : trxLinkedHMap.values()) {
            ssb.append(TextUtils.colorAntennaAverage(getRssiAverage(trx.getTrxNumber(), mode),
                    color, threshold, comparaisonSign, space));
        }
        ssb.append("\n");
        return ssb;
    }

    public int getTrxNumber(String address) {
        if (address.equals(trxAddressFrontLeft)) {
            return ConnectedCar.NUMBER_TRX_FRONT_LEFT;
        } else if (address.equals(trxAddressFrontRight)) {
            return ConnectedCar.NUMBER_TRX_FRONT_RIGHT;
        } else if (address.equals(trxAddressLeft)) {
            return ConnectedCar.NUMBER_TRX_LEFT;
        } else if (address.equals(trxAddressMiddle)) {
            return ConnectedCar.NUMBER_TRX_MIDDLE;
        } else if (address.equals(trxAddressRight)) {
            return ConnectedCar.NUMBER_TRX_RIGHT;
        } else if (address.equals(trxAddressTrunk)) {
            return ConnectedCar.NUMBER_TRX_TRUNK;
        } else if (address.equals(trxAddressRearLeft)) {
            return ConnectedCar.NUMBER_TRX_REAR_LEFT;
        } else if (address.equals(trxAddressBack)) {
            return ConnectedCar.NUMBER_TRX_BACK;
        } else if (address.equals(trxAddressRearRight)) {
            return ConnectedCar.NUMBER_TRX_REAR_RIGHT;
        } else {
            return -1;
        }
    }

    public double[] getRssiForRangingPrediction() {
        double[] rssi = new double[8];
        rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
        rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
        rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
        rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        rssi[4] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT);
        rssi[5] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT);
        rssi[6] = getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT);
        rssi[7] = getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT);
        return rssi;
    }

    protected enum ConnectionNumber {
        THREE_CONNECTION, FOUR_CONNECTION, FIVE_CONNECTION,
        SIX_CONNECTION, SEVEN_CONNECTION, EIGHT_CONNECTION
    }
}
