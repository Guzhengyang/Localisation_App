package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.RkeListener;
import com.valeo.psa.R;
import com.valeo.psa.view.ReverseProgressBar;

import static com.valeo.bleranging.BleRangingHelper.RKE_USE_TIMEOUT;

/**
 * Created by l-avaratha on 09/03/2017
 */
public class RkeFragment extends Fragment implements RkeListener {
    private final static int FIVE_MINUTES_IN_MILLI = 300000;
    private final static int MINUTE_IN_MILLI = 60000;
    private final static int SECOND_IN_MILLI = 1000;
    private final Handler mHandler = new Handler();
    private TextView car_door_status;
    private TextView rke_loading_progress_bar;
    private ImageButton vehicle_locked;
    private ImageButton driver_s_door_unlocked;
    private ImageButton vehicle_unlocked;
    private ImageButton start_button;
    private Animation pulseAnimation;
    private Animation pulseAnimation2;
    private ImageView start_button_first_wave;
    private ImageView start_button_second_wave;
    private RelativeLayout content_start_car_dialog;
    private ReverseProgressBar start_car_timeout;
    private TextView car_start_countdown_min_sec;
    private CountDownTimer countDownTimer = null;
    private int progressMin;
    private int progressSec;
    private CarDoorStatus carDoorStatus;
    private RkeFragmentActionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.rke_fragment, container, false);
        setView(rootView);
        setOnClickListeners();
        final Typeface lightTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt.otf");
        car_door_status.setTypeface(lightTypeFace, Typeface.NORMAL);
        return rootView;
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        if (mContext instanceof Activity) {
            mListener = (RkeFragmentActionListener) mContext;
        }
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        car_door_status = (TextView) rootView.findViewById(R.id.car_door_status);
        vehicle_locked = (ImageButton) rootView.findViewById(R.id.vehicle_locked);
        driver_s_door_unlocked = (ImageButton) rootView.findViewById(R.id.driver_s_door_unlocked);
        vehicle_unlocked = (ImageButton) rootView.findViewById(R.id.vehicle_unlocked);
        start_button = (ImageButton) rootView.findViewById(R.id.start_button);
        start_button_first_wave = (ImageView) rootView.findViewById(R.id.start_button_first_wave);
        start_button_second_wave = (ImageView) rootView.findViewById(R.id.start_button_second_wave);

        content_start_car_dialog = (RelativeLayout) rootView.findViewById(R.id.content_start_car_dialog);
        start_car_timeout = (ReverseProgressBar) rootView.findViewById(R.id.start_car_timeout);
        car_start_countdown_min_sec = (TextView) rootView.findViewById(R.id.car_start_countdown_min_sec);
        rke_loading_progress_bar = (TextView) rootView.findViewById(R.id.rke_loading_progress_bar);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pulseAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
        pulseAnimation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
    }

    /**
     * Set OnClickListeners
     */
    private void setOnClickListeners() {
        start_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (carDoorStatus != null && carDoorStatus == CarDoorStatus.UNLOCKED) {
                    if (countDownTimer == null) { // prevent from launching two countDownTimer
                        // Change toolbar to start car mode
                        switchToolbarStartCar(true);
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
                                switchToolbarStartCar(false);
                                countDownTimer = null;
                            }
                        }.start();
                    }
                }
                return false;
            }
        });
        vehicle_locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener.isRKEButtonClickable()) {
                    rke_loading_progress_bar.setVisibility(View.VISIBLE);
                    carDoorStatus = CarDoorStatus.LOCKED;
                    vehicle_locked.setBackgroundResource(R.mipmap.slider_button);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    vehicle_unlocked.setBackgroundResource(0);
                    startButtonAnimation(false);
                    PSALogs.d("performLock", "RKE LOCK");
                    mListener.performRKELockAction(true); //lockVehicle
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rke_loading_progress_bar.setVisibility(View.GONE);
                        }
                    }, RKE_USE_TIMEOUT);
                }
            }
        });
        driver_s_door_unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener.isRKEButtonClickable()) {
                    rke_loading_progress_bar.setVisibility(View.VISIBLE);
                    carDoorStatus = CarDoorStatus.DRIVER_DOOR_OPEN;
                    driver_s_door_unlocked.setBackgroundResource(R.mipmap.slider_button);
                    vehicle_locked.setBackgroundResource(0);
                    vehicle_unlocked.setBackgroundResource(0);
                    start_button.setBackgroundResource(0);
                    startButtonAnimation(false);
                    PSALogs.d("performLock", "RKE UNLOCK 1");
                    mListener.performRKELockAction(false); //unlockVehicle
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rke_loading_progress_bar.setVisibility(View.GONE);
                        }
                    }, RKE_USE_TIMEOUT);
                }
            }
        });
        vehicle_unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener.isRKEButtonClickable()) {
                    rke_loading_progress_bar.setVisibility(View.VISIBLE);
                    carDoorStatus = CarDoorStatus.UNLOCKED;
                    vehicle_unlocked.setBackgroundResource(R.mipmap.slider_button);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    vehicle_locked.setBackgroundResource(0);
                    startButtonAnimation(true);
                    PSALogs.d("performLock", "RKE UNLOCK 2");
                    mListener.performRKELockAction(false); //unlockVehicle
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rke_loading_progress_bar.setVisibility(View.GONE);
                        }
                    }, RKE_USE_TIMEOUT);
//                    createNotification(NOTIFICATION_ID_1, getString(R.string.notif_unlock_it),
//                            R.mipmap.car_all_doors_button, getString(R.string.vehicle_unlocked));
                }
            }
        });
    }

    private void startButtonAnimation(boolean isAnimated) {
        if (isAnimated) {
            start_button_first_wave.setVisibility(View.VISIBLE);
            start_button_second_wave.setVisibility(View.VISIBLE);
            start_button_first_wave.setAnimation(pulseAnimation);
            start_button_second_wave.setAnimation(pulseAnimation2);
        } else {
            start_button_first_wave.setVisibility(View.INVISIBLE);
            start_button_second_wave.setVisibility(View.INVISIBLE);
            start_button_first_wave.setAnimation(null);
            start_button_second_wave.setAnimation(null);
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
                getString(R.string.car_start_countdown_min_sec),
                progressMin, progressSec));
    }

    /**
     * Switch between toolbar's (normal and start car mode)
     *
     * @param mainToStart boolean to determine which toolbar to inflate
     */
    private void switchToolbarStartCar(boolean mainToStart) {
        if (mainToStart) {
            content_start_car_dialog.setVisibility(View.VISIBLE);
            mListener.showBleStatus(false);
            mListener.switchToolbarStartCar(true);
        } else {
            content_start_car_dialog.setVisibility(View.GONE);
            mListener.showBleStatus(true);
            mListener.switchToolbarStartCar(false);
        }
    }

    public void cancelCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void updateCarDoorStatus(boolean lockStatus) {
        if (lockStatus) {
            PSALogs.d("NIH rearm", "update lock");
            car_door_status.setText(getString(R.string.vehicle_locked));
            carDoorStatus = CarDoorStatus.LOCKED;
            vehicle_locked.setBackgroundResource(R.mipmap.slider_button);
            driver_s_door_unlocked.setBackgroundResource(0);
            vehicle_unlocked.setBackgroundResource(0);
            startButtonAnimation(false);
        } else {
            PSALogs.d("NIH rearm", "update unlock");
            car_door_status.setText(getString(R.string.vehicle_unlocked));
            carDoorStatus = CarDoorStatus.UNLOCKED;
            vehicle_unlocked.setBackgroundResource(R.mipmap.slider_button);
            driver_s_door_unlocked.setBackgroundResource(0);
            vehicle_locked.setBackgroundResource(0);
            // animation waves start_button
            startButtonAnimation(true);
        }
        mListener.updateCarDrawable();
    }

    private enum CarDoorStatus {
        LOCKED, DRIVER_DOOR_OPEN, UNLOCKED
    }

    public interface RkeFragmentActionListener {
        boolean isRKEButtonClickable();

        void performRKELockAction(boolean lock);

        void showBleStatus(boolean show);

        void updateCarDrawable();

        void switchToolbarStartCar(boolean mainToStart);
    }
}
