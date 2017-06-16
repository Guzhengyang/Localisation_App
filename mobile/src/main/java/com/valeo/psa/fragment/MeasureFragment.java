package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.Context;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.valeo.bleranging.listeners.MeasureListener;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.SoundUtils;
import com.valeo.psa.R;


/**
 * Created by l-avaratha on 09/03/2017
 */

public class MeasureFragment extends Fragment implements MeasureListener {
    private EditText measurement_index;
    private MeasureFragmentActionListener mListener;
    private Button start_measurement;
    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.measure_fragment, container, false);
        setView(rootView);
        setOnClickListeners();
        return rootView;
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        if (mContext instanceof Activity) {
            mListener = (MeasureFragmentActionListener) mContext;
        }
    }

    private void setView(View rootView) {
        measurement_index = (EditText) rootView.findViewById(R.id.measurement_index);
        measurement_index.setText("0");
        start_measurement = (Button) rootView.findViewById(R.id.start_measurement);
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Set OnClickListeners
     */
    private void setOnClickListeners() {
        start_measurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null && isAdded()) {
                            start_measurement.setEnabled(false);
                            measurement_index.setEnabled(false);
                            if (measurement_index.getText().toString().equals("0")) {
                                mListener.incrementCounter("0");
                                measurement_index.setText(mListener.printCounter());
                            }
                        }
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null && isAdded()) {
                                    SoundUtils.makeNoise(getActivity(), mHandler, ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
                                    mListener.cancelCounter();
                                    mListener.incrementCounter(measurement_index.getText().toString());
                                    measurement_index.setText(mListener.printCounter());
                                    start_measurement.setEnabled(true);
                                    measurement_index.setEnabled(true);
                                }
                            }
                        }, SdkPreferencesHelper.getInstance().getMeasurementInterval());
                    }
                });
            }
        });
    }

    public interface MeasureFragmentActionListener {
        void incrementCounter(String counterValue);

        void cancelCounter();

        String printCounter();
    }
}
