package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.RkeListener;
import com.valeo.psa.R;

import static android.view.DragEvent.ACTION_DRAG_LOCATION;
import static android.widget.RelativeLayout.ALIGN_START;
import static android.widget.RelativeLayout.ALIGN_TOP;
import static com.valeo.bleranging.BleRangingHelper.RKE_USE_TIMEOUT;

/**
 * Created by l-avaratha on 09/03/2017
 */
public class RkeFragment extends Fragment implements RkeListener {
    private final Handler mHandler = new Handler();
    private TextView car_door_status;
    private TextView rke_loading_progress_bar;
    private ImageView circle_selector;
    private ImageButton vehicle_locked;
    private ImageButton driver_s_door_unlocked;
    private ImageButton vehicle_unlocked;
    private ImageButton start_button;
    private Animation pulseAnimation;
    private Animation pulseAnimation2;
    private ImageView start_button_first_wave;
    private ImageView start_button_second_wave;
    private CarDoorStatus carDoorStatus;
    private RkeFragmentActionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.rke_fragment, container, false);
        setView(rootView);
        setOnClickListeners();
        setOnTouchListeners();
        setOnDragListeners();
        final Typeface lightTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt.otf");
        car_door_status.setTypeface(lightTypeFace, Typeface.NORMAL);
        return rootView;
    }

    private void setOnTouchListeners() {
        circle_selector.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    PSALogs.d("DragDrop", "ACTION_DOWN");
                    final ClipData dragData = ClipData.newPlainText((CharSequence) view.getTag(), (CharSequence) view.getTag());
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PSALogs.d("DragDrop", "startDragAndDrop");
                        view.startDragAndDrop(dragData, shadowBuilder, view, 0);
                    } else {
                        PSALogs.d("DragDrop", "startDrag");
                        view.startDrag(dragData, shadowBuilder, view, 0);
                    }
                    view.setVisibility(View.INVISIBLE);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    PSALogs.d("DragDrop", "ACTION_MOVE");
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void placeOver(View dragView, View view) {
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dragView.getLayoutParams();
        layoutParams.removeRule(ALIGN_TOP);
        layoutParams.removeRule(ALIGN_START);
        layoutParams.addRule(ALIGN_TOP, ((FrameLayout) view.getParent()).getId());
        layoutParams.addRule(ALIGN_START, ((FrameLayout) view.getParent()).getId());
        dragView.setLayoutParams(layoutParams);
    }

    private void rkeActions(View view) {
        switch (view.getId()) {
            case R.id.vehicle_locked:
                rkeAction(CarDoorStatus.LOCKED, vehicle_locked, false, true);
                break;
            case R.id.driver_s_door_unlocked:
                rkeAction(CarDoorStatus.DRIVER_DOOR_OPEN, driver_s_door_unlocked, false, false);
                break;
            case R.id.vehicle_unlocked:
                rkeAction(CarDoorStatus.UNLOCKED, vehicle_unlocked, true, false);
                break;
            default:
                break;
        }
    }

    private void setOnDragListeners() {
        vehicle_locked.setOnDragListener(new MyDragListener());
        vehicle_unlocked.setOnDragListener(new MyDragListener());
        driver_s_door_unlocked.setOnDragListener(new MyDragListener());
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
        circle_selector = (ImageView) rootView.findViewById(R.id.circle_selector);
        vehicle_locked = (ImageButton) rootView.findViewById(R.id.vehicle_locked);
        driver_s_door_unlocked = (ImageButton) rootView.findViewById(R.id.driver_s_door_unlocked);
        vehicle_unlocked = (ImageButton) rootView.findViewById(R.id.vehicle_unlocked);
        start_button = (ImageButton) rootView.findViewById(R.id.start_button);
        start_button_first_wave = (ImageView) rootView.findViewById(R.id.start_button_first_wave);
        start_button_second_wave = (ImageView) rootView.findViewById(R.id.start_button_second_wave);
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
                    mListener.startButtonActions();
                }
                return false;
            }
        });
        vehicle_locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rkeAction(CarDoorStatus.LOCKED, vehicle_locked, false, true);
            }
        });
        driver_s_door_unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rkeAction(CarDoorStatus.DRIVER_DOOR_OPEN, driver_s_door_unlocked, false, false);
            }
        });
        vehicle_unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rkeAction(CarDoorStatus.UNLOCKED, vehicle_unlocked, true, false);
//              createNotification(NOTIFICATION_ID_1, getString(R.string.notif_unlock_it),
//              R.mipmap.car_all_doors_button, getString(R.string.vehicle_unlocked));
            }
        });
    }

    private void rkeAction(CarDoorStatus mCarDoorStatus, View view, boolean enableStartAnimation,
                           boolean lockCar) {
        if (mListener.isRKEButtonClickable()) {
            rke_loading_progress_bar.setVisibility(View.VISIBLE);
            carDoorStatus = mCarDoorStatus;
            placeOver(circle_selector, view);
            startButtonAnimation(enableStartAnimation);
            mListener.performRKELockAction(lockCar);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rke_loading_progress_bar.setVisibility(View.GONE);
                }
            }, RKE_USE_TIMEOUT);
        }
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

    @Override
    public void updateCarDoorStatus(boolean lockStatus) {
        if (lockStatus) {
            PSALogs.d("NIH rearm", "update lock");
            car_door_status.setText(getString(R.string.vehicle_locked));
            carDoorStatus = CarDoorStatus.LOCKED;
            placeOver(circle_selector, vehicle_locked);
            startButtonAnimation(false);
        } else {
            PSALogs.d("NIH rearm", "update unlock");
            car_door_status.setText(getString(R.string.vehicle_unlocked));
            carDoorStatus = CarDoorStatus.UNLOCKED;
            placeOver(circle_selector, vehicle_unlocked);
            // animation waves start_button
            startButtonAnimation(true);
        }
        if (!circle_selector.isShown()) {
            circle_selector.setVisibility(View.VISIBLE);
        }
        mListener.updateCarDrawable();
    }

    public void resetDisplayAfterDisconnection() {
        circle_selector.setVisibility(View.GONE);
        startButtonAnimation(false);
    }

    private enum CarDoorStatus {
        LOCKED, DRIVER_DOOR_OPEN, UNLOCKED
    }

    public interface RkeFragmentActionListener {
        boolean isRKEButtonClickable();
        void performRKELockAction(boolean lock);
        void updateCarDrawable();
        void startButtonActions();
    }

    class MyDragListener implements View.OnDragListener {
        private boolean containsDraggable;

        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            // Defines a variable to store the action type for the incoming event
            final int action = dragEvent.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    PSALogs.d("DragDrop", "ACTION_DRAG_STARTED " + view.getId());
                    break;
                case ACTION_DRAG_LOCATION:
                    // Ignore the event
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (dragEvent.getResult()) {
                        PSALogs.d("DragDrop", "The drop was handled.");
                    } else {
                        PSALogs.d("DragDrop", "The drop didn't work.");
                        final View dragView = (View) dragEvent.getLocalState();
                        if (dragView != null) {
                            PSALogs.d("DragDrop", "OUTSIDE dragView = " + dragView.toString());
                            dragView.post(new Runnable() {
                                @Override
                                public void run() {
                                    dragView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    PSALogs.d("DragDrop", "ACTION_DRAG_ENTERED " + view.getId());
                    containsDraggable = true;
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    PSALogs.d("DragDrop", "ACTION_DRAG_EXITED " + view.getId());
                    containsDraggable = false;
                    break;
                case DragEvent.ACTION_DROP:
                    PSALogs.d("DragDrop", "ACTION_DROP " + view.getId());
                    final View dragView = (View) dragEvent.getLocalState();
                    if (dragView != null) {
                        if (containsDraggable) {
                            PSALogs.d("DragDrop", "INSIDE dragView = " + dragView.toString());
                            placeOver(dragView, view);
                            rkeActions(view);
                        }
                        dragView.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    PSALogs.d("DragDrop Example", "Unknown action type received by OnDragListener.");
                    break;
            }
            return true;
        }
    }
}
