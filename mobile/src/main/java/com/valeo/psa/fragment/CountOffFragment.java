package com.valeo.psa.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import java.util.Locale;

/**
 * Created by l-avaratha on 09/05/2017
 */

public class CountOffFragment extends Fragment {
    private TextView count_off_number;
    private boolean count_off_launched = false;
    private BleRangingHelper mBleRangingHelper;
    private Handler mHandler;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("frag", "8 " + mBleRangingHelper.isSmartphoneFrozen());
            if (mBleRangingHelper.isSmartphoneFrozen()) {
                PSALogs.d("frag", "9");
                count_off_launched = true;
                new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(final long millisUntilFinished) {
                        if (mBleRangingHelper.isSmartphoneFrozen()) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        count_off_number.setText(String.format(Locale.FRANCE,
                                                getString(R.string.count_off_remaining_time),
                                                millisUntilFinished / 1000));
                                    }
                                });
                            }
                        } else {
                            count_off_launched = false;
                            PSALogs.d("frag", "11");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        count_off_number.setText(R.string.smartphone_moved);
                                    }
                                });
                            }
                            mHandler.postDelayed(runnable, 1800);
                            cancel();
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    count_off_number.setText(String.format(Locale.FRANCE,
                                            getString(R.string.count_off_remaining_time),
                                            0));
                                    Snackbar.make(getActivity().getWindow().getDecorView(),
                                            R.string.calibration_done, Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                        SdkPreferencesHelper.getInstance().setIsCalibrated(true);
                    }
                }.start();
            } else {
                PSALogs.d("frag", "10 " + count_off_launched);
                if (!count_off_launched) {
                    mHandler.postDelayed(this, 1000);
                }
            }
        }
    };

    public CountOffFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.count_off_fragment, container, false);
        count_off_number = (TextView) rootView.findViewById(R.id.count_off_number);
        Bundle bundle = getArguments();
        mBleRangingHelper = (BleRangingHelper) bundle.get("bleRangingHelper");
        if (mBleRangingHelper != null) {
            mHandler = new Handler();
            mHandler.post(runnable);
        }
        return rootView;
    }
}
