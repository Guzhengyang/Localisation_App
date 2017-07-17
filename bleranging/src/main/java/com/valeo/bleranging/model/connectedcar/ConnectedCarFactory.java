package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.machinelearningalgo.prediction.BasePrediction;

import java.util.LinkedHashMap;

import static com.valeo.bleranging.utils.JsonUtils.createTrxList;
import static com.valeo.bleranging.utils.JsonUtils.getConnectedCarJsonContent;
import static com.valeo.bleranging.utils.JsonUtils.getStoredData;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class ConnectedCarFactory {

    /**
     * Return a connected car
     *
     * @param carName the car name
     * @return a connected car with the specified number of connection
     */
    public static ConnectedCar getConnectedCar(Context mContext, String carName) {
        getStoredData(mContext);
        String[] carData = getConnectedCarJsonContent(carName);
        if (carData != null && carData.length >= 3) {
            return new ConnectedCar(mContext, createTrxList(carData[0], Integer.valueOf(carData[2])),
                    new LinkedHashMap<String, BasePrediction>());
        }
        return null;
    }
}
