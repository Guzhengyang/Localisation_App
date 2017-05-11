package com.valeo.psa.fragment;

import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.SoundUtils;
import com.valeo.psa.R;
import com.valeo.psa.interfaces.CountOffFragmentListener;

/**
 * Created by l-avaratha on 09/05/2017
 */

public class CountOffFragment extends Fragment {
    private TextView count_off_title;
    private TextView count_off_value;
    private boolean count_off_launched = false;
    private CountOffFragmentListener countOffFragmentListener;
    private Handler mHandler;
    private int mCounter;
    private final Runnable checkIfFrozenRunnable = new Runnable() {
        @Override
        public void run() {
            if (!count_off_launched) {
                if (countOffFragmentListener.isConnected() && countOffFragmentListener.isFrozen()) {
                    count_off_launched = true;
                    mCounter = 10;
                    mHandler.postDelayed(timerRunner, 1000);
                } else if (!countOffFragmentListener.isConnected()) {
                    setCountOffText(R.string.waiting_for_connection, R.string.empty_string);
                    mHandler.postDelayed(this, 1000);
                } else if (!countOffFragmentListener.isFrozen()) {
                    setCountOffText(R.string.smartphone_moved, R.string.empty_string);
                    mHandler.postDelayed(this, 1000);
                }
            } else {
                PSALogs.d("frag", "count_off already launched");
            }
        }
    };
    private Animation fade_in;
    private Animation fade_out;
    private final Runnable timerRunner = new Runnable() {
        @Override
        public void run() {
            if (!countOffFragmentListener.isConnected()) {
                setCountOffText(R.string.waiting_for_connection, R.string.empty_string);
                mHandler.removeCallbacksAndMessages(null);
                count_off_launched = false;
                mCounter = 10;
                mHandler.postDelayed(checkIfFrozenRunnable, 1500);
                return;
            }
            if (countOffFragmentListener.isFrozen()) {
                count_off_value.startAnimation(fade_out);
                setCountOffText(R.string.count_off_remaining_time_empty, String.valueOf(mCounter--));
                count_off_value.startAnimation(fade_in);
                if (mCounter == -1) {
                    setCountOffText(R.string.empty_string, R.string.calibration_done);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            countOffFragmentListener.setSmartphoneOffset();
                            countOffFragmentListener.dismissDialog();
                            SoundUtils.makeNoise(getContext(), mHandler,
                                    ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 100);
                        }
                    }, 3000);
                    return;
                }
                mHandler.postDelayed(this, 1000);
            } else {
                setCountOffText(R.string.smartphone_moved, R.string.empty_string);
                mHandler.removeCallbacksAndMessages(null);
                count_off_launched = false;
                mCounter = 10;
                mHandler.postDelayed(checkIfFrozenRunnable, 1500);
            }
        }
    };

    public CountOffFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.count_off_fragment, container, false);
        count_off_title = (TextView) rootView.findViewById(R.id.count_off_title);
        count_off_value = (TextView) rootView.findViewById(R.id.count_off_value);
        mHandler = new Handler();
        mHandler.post(checkIfFrozenRunnable);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fade_in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            countOffFragmentListener = (CountOffFragmentListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement CountOffFragmentListener");
        }
    }

    private void setCountOffText(final int resIdTitle, final int resIdValue) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    count_off_title.setText(resIdTitle);
                    count_off_value.setText(resIdValue);
                }
            });
        }
    }

    private void setCountOffText(final int resIdTitle, final String stringValue) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    count_off_title.setText(resIdTitle);
                    count_off_value.setText(stringValue);
                }
            });
        }
    }
}
