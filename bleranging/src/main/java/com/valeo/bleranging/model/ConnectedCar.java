package com.valeo.bleranging.model;

import android.content.Context;

import com.valeo.bleranging.machinelearningalgo.prediction.BasePrediction;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.LinkedHashMap;

import static com.valeo.bleranging.utils.CheckUtils.checkForRssiNonNull;
import static com.valeo.bleranging.utils.JsonUtils.createTrxList;
import static com.valeo.bleranging.utils.JsonUtils.getConnectedCarJsonContent;
import static com.valeo.bleranging.utils.JsonUtils.getStoredData;

/**
 * Created by l-avaratha on 05/09/2016
 */
public class ConnectedCar {
    /**
     * Manage group of trx
     */
    private final MultiTrx mMultiTrx;
    /**
     * Manage group of prediction
     */
    private final MultiPrediction mMultiPrediction;
    /**
     * The car registration plate number
     */
    private String regPlate;

    /**
     * Constructor
     *
     * @param trxLinked        the list of trx on the car
     * @param predictionLinked the list of prediction active
     */
    private ConnectedCar(final LinkedHashMap<Integer, Trx> trxLinked,
                 LinkedHashMap<String, BasePrediction> predictionLinked) {
        this.mMultiTrx = new MultiTrx(trxLinked);
        this.mMultiPrediction = new MultiPrediction(predictionLinked);
    }

    /**
     * Return a connected car
     *
     * @param mContext the context
     * @param carName  the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        getStoredData(mContext);
        String[] carData = getConnectedCarJsonContent(carName);
        if (carData != null && carData.length >= 3) {
            return new ConnectedCar(createTrxList(carData[0], Integer.valueOf(carData[2])),
                    new LinkedHashMap<String, BasePrediction>());
        }
        return null;
    }

    /**
     * Get the trx manager
     *
     * @return trx manager
     */
    public MultiTrx getMultiTrx() {
        return mMultiTrx;
    }

    /**
     * Get the prediction manager
     * @return the prediction manager
     */
    public MultiPrediction getMultiPrediction() {
        return mMultiPrediction;
    }

    /**
     * Check if predictions raw file are read and gathered rssi tab is not null
     *
     * @return true if prediction were initialized, false otherwise
     */
    public boolean isInitialized() {
        return checkForRssiNonNull(mMultiTrx.getRssiTab()) != null
                && mMultiPrediction.isInitialized();
    }

    /**
     * Initialize predictions
     */
    public void initPredictions() {
        mMultiPrediction.initPredictions(mMultiTrx.getRssiTab(), SdkPreferencesHelper.getInstance().getOffsetSmartphone());
    }

    /**
     * Get the connected car registration plate
     * @return the registration plate number
     */
    public String getRegPlate() {
        return regPlate;
    }

    /**
     * Set the connected car registration plate
     * @param regPlate the connected car registration plate
     */
    public void setRegPlate(final String regPlate) {
        this.regPlate = regPlate;
    }
}
