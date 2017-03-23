package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valeo.psa.R;
import com.valeo.psa.view.ReverseProgressBar;

/**
 * Created by l-avaratha on 09/03/2017
 */
public class StartFragment extends Fragment {
    private final static int FIVE_MINUTES_IN_MILLI = 300000;
    private final static int MINUTE_IN_MILLI = 60000;
    private final static int SECOND_IN_MILLI = 1000;
    private ReverseProgressBar start_car_timeout;
    private TextView car_start_countdown_min_sec;
    private CountDownTimer countDownTimer = null;
    private int progressMin;
    private int progressSec;
    private StartFragmentActionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.start_fragment, container, false);
        setView(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        if (mContext instanceof Activity) {
            mListener = (StartFragmentActionListener) mContext;
        }
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        final TextView start_fragment_title = (TextView) toolbar.findViewById(R.id.start_fragment_title);
        start_fragment_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                mListener.startButtonActionsFinished();
            }
        });
        start_car_timeout = (ReverseProgressBar) rootView.findViewById(R.id.start_car_timeout);
        car_start_countdown_min_sec = (TextView) rootView.findViewById(R.id.car_start_countdown_min_sec);
    }

    public void startButtonActions() {
        if (countDownTimer == null) { // prevent from launching two countDownTimer
            /** CountDownTimer starts with 5 minutes and every onTick is 1 second */
            countDownTimer = new CountDownTimer(FIVE_MINUTES_IN_MILLI, SECOND_IN_MILLI) {
                public void onTick(long millisUntilFinished) {
                    int timePassed = (int) (millisUntilFinished / SECOND_IN_MILLI);
                    updateStartCarTimeoutBar(timePassed);
                    progressMin = (int) (millisUntilFinished / MINUTE_IN_MILLI);
                    progressSec = timePassed % 60; // ignore minutes
                    updateStartCarTimeout(progressMin, progressSec);
                }

                public void onFinish() {
                    // If time up, return to Remote Key Activity
                    // Change toolbar to normal mode
                    countDownTimer = null;
                    mListener.startButtonActionsFinished();
                }
            }.start();
        }
    }

    /**
     * Set the progress on the ProgressBar
     *
     * @param timeout the progress value
     */
    private void updateStartCarTimeoutBar(int timeout) {
        start_car_timeout.setProgress(timeout);
    }

    /**
     * Set the current remaining progress time in minutes and seconds
     *
     * @param progressMin the progress remaining minutes
     * @param progressSec the progress remaining seconds
     */
    private void updateStartCarTimeout(int progressMin, int progressSec) {
        car_start_countdown_min_sec.setText(String.format(
                getString(R.string.car_start_countdown_min_sec), progressMin, progressSec));
    }

    public interface StartFragmentActionListener {
        void startButtonActionsFinished();
    }
}