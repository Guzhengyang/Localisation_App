package com.valeo.bleranging.model.connectedcar;

import android.content.Context;

import com.valeo.bleranging.machinelearningalgo.prediction.BasePrediction;
import com.valeo.bleranging.model.MultiPrediction;
import com.valeo.bleranging.model.MultiTrx;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.LinkedHashMap;

import static com.valeo.bleranging.utils.CheckUtils.checkForRssiNonNull;

/**
 * Created by l-avaratha on 05/09/2016
 */
public class ConnectedCar {
    private final MultiTrx mMultiTrx;
    private final MultiPrediction mMultiPrediction;
    private final Context mContext;

    ConnectedCar(final Context context, final LinkedHashMap<Integer, Trx> trxLinked,
                 LinkedHashMap<String, BasePrediction> predictionLinked) {
        this.mContext = context;
        this.mMultiTrx = new MultiTrx(trxLinked);
        this.mMultiPrediction = new MultiPrediction(predictionLinked);
    }

    public MultiTrx getMultiTrx() {
        return mMultiTrx;
    }

    public MultiPrediction getMultiPrediction() {
        return mMultiPrediction;
    }

    /**
     * Check if predictions has been initialized
     *
     * @return true if prediction were initialized, false otherwise
     */
    public boolean isInitialized() {
        return checkForRssiNonNull(mMultiTrx.getRssiTab()) != null
                && mMultiPrediction.isInitialized();
    }

    public void initPredictions() {
        mMultiPrediction.initPredictions(mMultiTrx.getRssiTab(), SdkPreferencesHelper.getInstance().getOffsetSmartphone());
    }
}
