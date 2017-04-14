package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.PointF;

import com.valeo.bleranging.BleRangingHelper;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCFourLMRB extends ConnectedCar {


    public CCFourLMRB(Context mContext) {
        super(mContext);
        trxLinkedHMap = new ConnectedCarFactory.TrxLinkHMapBuilder()
                .left()
                .middle()
                .right()
                .back()
                .build();
    }

    @Override
    public void readPredictionsRawFiles() {

    }

    @Override
    public void initPredictions() {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void setRssi(double[] rssi, boolean lockStatus) {

    }

    @Override
    public void calculatePrediction(float[] orientation) {

    }

    @Override
    public String printDebug(boolean smartphoneIsInPocket) {
        return "";
    }

    @Override
    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        return BleRangingHelper.PREDICTION_UNKNOWN;
    }

    @Override
    public PointF getPredictionCoord() {
        return null;
    }

    @Override
    public double getDist2Car() {
        return 0;
    }
}
