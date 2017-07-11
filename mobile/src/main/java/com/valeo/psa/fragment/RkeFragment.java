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
import android.widget.TextView;

import com.valeo.bleranging.listeners.RkeListener;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import static android.view.DragEvent.ACTION_DRAG_LOCATION;
import static com.valeo.bleranging.persistence.Constants.RKE_USE_TIMEOUT;

/**
 * Created by l-avaratha on 09/03/2017
 */
public class RkeFragment extends Fragment implements RkeListener {
    private final Handler mHandler = new Handler();
    private boolean isDragging = false;
    private boolean updateAfterDragStops = false;
    private boolean savedLockStatus = false;
    private TextView car_door_status;
    private TextView rke_loading_progress_bar;
    private ImageButton circle_selector_driver_s_door_unlocked;
    private ImageButton circle_selector_unlocked;
    private ImageButton circle_selector_locked;
    private FrameLayout frame_vehicle_locked;
    private FrameLayout frame_driver_s_door_unlocked;
    private FrameLayout frame_vehicle_unlocked;
    private ImageView vehicle_locked;
    private ImageView driver_s_door_unlocked;
    private ImageView vehicle_unlocked;
    private ImageButton start_button;
    private Animation pulseAnimation;
    private Animation pulseAnimation2;
    private ImageView start_button_first_wave;
    private ImageView start_button_second_wave;
    private CarDoorStatus carDoorStatus;
    private RkeFragmentActionListener mListener;
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                PSALogs.d("DragDrop", "ACTION_DOWN");
                if (mListener != null && mListener.isRKEButtonClickable()) {
                    PSALogs.d("DragDrop", "isRKEButtonClickable = " + mListener.isRKEButtonClickable() + " before startDrag");
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
                } else {
                    PSALogs.d("DragDrop", "isRKEButtonClickable is FALSE before startDrag");
                }
                return true;
            } else {
                return false;
            }
        }
    };

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
        circle_selector_locked.setOnTouchListener(mOnTouchListener);
        circle_selector_unlocked.setOnTouchListener(mOnTouchListener);
        circle_selector_driver_s_door_unlocked.setOnTouchListener(mOnTouchListener);
    }

    private void hideOldSelection() {
        // Hide everyone
        circle_selector_driver_s_door_unlocked.setVisibility(View.INVISIBLE);
        driver_s_door_unlocked.setVisibility(View.INVISIBLE);
        circle_selector_unlocked.setVisibility(View.INVISIBLE);
        vehicle_unlocked.setVisibility(View.INVISIBLE);
        circle_selector_locked.setVisibility(View.INVISIBLE);
        vehicle_locked.setVisibility(View.INVISIBLE);
    }

    private void placeSelectorOver(View view) {
        PSALogs.d("DragDrop", "placeSelectorOver " + view.getId());
        hideOldSelection();
        // Show the selected view
        ((FrameLayout) view).getChildAt(0).setVisibility(View.VISIBLE);
        ((FrameLayout) view).getChildAt(1).setVisibility(View.VISIBLE);
    }

    private void rkeActions(View view) {
        if (((FrameLayout) view).getChildAt(1) != null) {
            switch (((FrameLayout) view).getChildAt(1).getId()) {
                case R.id.vehicle_locked:
                    rkeAction(CarDoorStatus.LOCKED, frame_vehicle_locked, false, true);
                    break;
                case R.id.driver_s_door_unlocked:
                    rkeAction(CarDoorStatus.DRIVER_DOOR_OPEN, frame_driver_s_door_unlocked, false, false);
                    break;
                case R.id.vehicle_unlocked:
                    rkeAction(CarDoorStatus.UNLOCKED, frame_vehicle_unlocked, true, false);
                    break;
                default:
                    break;
            }
        }
    }

    private void setOnDragListeners() {
        frame_vehicle_locked.setOnDragListener(new MyDragListener());
        frame_vehicle_unlocked.setOnDragListener(new MyDragListener());
        frame_driver_s_door_unlocked.setOnDragListener(new MyDragListener());
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
        circle_selector_driver_s_door_unlocked = (ImageButton) rootView.findViewById(R.id.circle_selector_driver_s_door_unlocked);
        circle_selector_locked = (ImageButton) rootView.findViewById(R.id.circle_selector_locked);
        circle_selector_unlocked = (ImageButton) rootView.findViewById(R.id.circle_selector_unlocked);
        frame_vehicle_locked = (FrameLayout) rootView.findViewById(R.id.frame_layout_vehicule_locked);
        frame_driver_s_door_unlocked = (FrameLayout) rootView.findViewById(R.id.frame_layout_driver_s_door_unlocked);
        frame_vehicle_unlocked = (FrameLayout) rootView.findViewById(R.id.frame_layout_vehicule_unlocked);
        vehicle_locked = (ImageView) rootView.findViewById(R.id.vehicle_locked);
        driver_s_door_unlocked = (ImageView) rootView.findViewById(R.id.driver_s_door_unlocked);
        vehicle_unlocked = (ImageView) rootView.findViewById(R.id.vehicle_unlocked);
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
//        vehicle_locked.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rkeAction(CarDoorStatus.LOCKED, vehicle_locked, false, true);
//            }
//        });
//        driver_s_door_unlocked.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rkeAction(CarDoorStatus.DRIVER_DOOR_OPEN, driver_s_door_unlocked, false, false);
//            }
//        });
//        vehicle_unlocked.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rkeAction(CarDoorStatus.UNLOCKED, vehicle_unlocked, true, false);
////              createNotification(NOTIFICATION_ID_1, getString(R.string.notif_unlock_it),
////              R.mipmap.car_all_doors_button, getString(R.string.vehicle_unlocked));
//            }
//        });
        vehicle_locked.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        driver_s_door_unlocked.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        vehicle_unlocked.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    private void rkeAction(CarDoorStatus mCarDoorStatus, View view, boolean enableStartAnimation,
                           boolean lockCar) {
        PSALogs.d("DragDrop", "rkeAction");
        if (mListener.isRKEButtonClickable()) {
            PSALogs.d("DragDrop", "isRKEButtonClickable is TRUE");
            rke_loading_progress_bar.setVisibility(View.VISIBLE);
            carDoorStatus = mCarDoorStatus;
            placeSelectorOver(view);
            startButtonAnimation(enableStartAnimation);
            mListener.performRKELockAction(lockCar);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rke_loading_progress_bar.setVisibility(View.GONE);
                }
            }, RKE_USE_TIMEOUT);
        } else {
            PSALogs.d("DragDrop", "isRKEButtonClickable is FALSE");
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
        PSALogs.d("DragDrop", "updateCarDoorStatus isDragging " + isDragging);
        if (isDragging) {
            updateAfterDragStops = true;
            savedLockStatus = lockStatus;
            PSALogs.d("DragDrop", "call updateCarDoorStatus when user was dragging, to apply savedLockStatus " + savedLockStatus);
        } else {
            if (isAdded()) {
                if (lockStatus) {
                    PSALogs.d("NIH rearm", "update lock");
                    car_door_status.setText(getString(R.string.vehicle_locked));
                    carDoorStatus = CarDoorStatus.LOCKED;
                    placeSelectorOver(frame_vehicle_locked);
                    startButtonAnimation(false);
                } else {
                    PSALogs.d("NIH rearm", "update unlock");
                    car_door_status.setText(getString(R.string.vehicle_unlocked));
                    carDoorStatus = CarDoorStatus.UNLOCKED;
                    placeSelectorOver(frame_vehicle_unlocked);
                    // animation waves start_button
                    startButtonAnimation(true);
                }
            }
        }
        mListener.updateCarDrawable();
    }

    public void resetDisplayAfterDisconnection() {
        hideOldSelection();
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

    private class MyDragListener implements View.OnDragListener {
        private boolean containsDraggable;

        @Override
        public boolean onDrag(final View view, DragEvent dragEvent) {
            // Defines a variable to store the action type for the incoming event
            final int action = dragEvent.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    PSALogs.d("DragDrop", "ACTION_DRAG_STARTED " + view.getId());
                    isDragging = true;
                    break;
                case ACTION_DRAG_LOCATION:
                    // Ignore the event
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    isDragging = false;
                    if (dragEvent.getResult()) {
                        PSALogs.d("DragDrop", "The drop was handled.");
                    } else {
                        PSALogs.d("DragDrop", "The drop didn't work.");
                        final View dragView = (View) dragEvent.getLocalState();
                        if (dragView != null) {
                            PSALogs.d("DragDrop", "OUTSIDE dragView = " + dragView.toString());
                            if (updateAfterDragStops) {
                                PSALogs.d("DragDrop", "updateCarDoorStatus After Drag Stops");
                                dragView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateAfterDragStops = false;
                                        updateCarDoorStatus(savedLockStatus);
                                    }
                                });
                            } else {
                                dragView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        PSALogs.d("DragDrop", "redraw invisible buttons");
                                        dragView.setVisibility(View.VISIBLE);
                                        if (((FrameLayout) dragView.getParent()).getChildAt(1) != null) {
                                            ((FrameLayout) dragView.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    PSALogs.d("DragDrop", "ACTION_DRAG_ENTERED " + view.getId());
                    containsDraggable = true;
                    if (((FrameLayout) view).getChildAt(1) != null) {
                        ((FrameLayout) view).getChildAt(1).setVisibility(View.VISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    PSALogs.d("DragDrop", "ACTION_DRAG_EXITED " + view.getId());
                    containsDraggable = false;
                    if (((FrameLayout) view).getChildAt(1) != null) {
                        ((FrameLayout) view).getChildAt(1).setVisibility(View.INVISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    PSALogs.d("DragDrop", "ACTION_DROP " + view.getId());
                    final View dragView = (View) dragEvent.getLocalState();
                    if (!updateAfterDragStops) { // if no auto relock happened
                        if (dragView != null) {
                            if (containsDraggable) {
                                PSALogs.d("DragDrop", "INSIDE dragView = " + dragView.toString());
                                placeSelectorOver(view);
                                rkeActions(view);
                            } else {
                                dragView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else { // Ignore command if user was dragging and auto relock happened
                        PSALogs.d("DragDrop", "ACTION_DROP updateCarDoorStatus");
                        updateAfterDragStops = false;
                        isDragging = false;
                        updateCarDoorStatus(savedLockStatus);
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
