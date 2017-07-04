package com.valeo.bleranging.utils;

import hex.genmodel.easy.EasyPredictModelWrapper;

/**
 * Created by l-avaratha on 30/06/2017
 */

public class CheckUtils {
    public static boolean comparePrediction(EasyPredictModelWrapper modelWrapper,
                                            int calculatedPrediction, String expectedPrediction) {
        return modelWrapper.getResponseDomainValues()[calculatedPrediction].equals(expectedPrediction);
    }

    public static boolean compareDistribution(double[] distribution,
                                              int temp_prediction, double threshold_prob) {
        return distribution[temp_prediction] > threshold_prob;
    }

    public static boolean ifNoDecision2Lock(double[] distribution, int index_lock,
                                            double threshold_prob_unlock2lock) {
        return distribution == null || distribution[index_lock] <= threshold_prob_unlock2lock;
    }

    public static boolean if2Lock(double[] rssi, double threshold_rssi_lock) {
        boolean result = true;
        if (rssi != null) {
            for (int i = 0; i < rssi.length; i++) {
                if (rssi[i] > threshold_rssi_lock) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Check if the array of rssi contains only non null value
     *
     * @param mRssi the array of rssi to check
     * @return null if a value is equal to 0, the entire array otherwise
     */
    public static double[] checkForRssiNonNull(double[] mRssi) {
        if (mRssi == null) {
            return null;
        }
        for (Double elem : mRssi) {
            if (elem == 0) {
                return null;
            }
        }
        return mRssi;
    }
}
