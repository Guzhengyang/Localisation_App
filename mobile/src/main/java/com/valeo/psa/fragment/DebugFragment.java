package com.valeo.psa.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valeo.bleranging.listeners.DebugListener;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.psa.R;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_ACCESS;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_BACK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FRONT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_INSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_INTERNAL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_NEAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_ROOF;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_FL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_FR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_RL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_RR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_THATCHAM;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_TRUNK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_WELCOME;

/**
 * Created by l-avaratha on 09/03/2017
 */
public class DebugFragment extends Fragment implements DebugListener {
    private ImageView signalReceived;
    private LayerDrawable layerDrawable;
    private GradientDrawable welcome_area;
    private GradientDrawable start_area_fl;
    private GradientDrawable start_area_fr;
    private GradientDrawable start_area_rl;
    private GradientDrawable start_area_rr;
    private GradientDrawable trunk_area;
    private GradientDrawable lock_area;
    private GradientDrawable rooftop;
    private GradientDrawable unlock_area_left;
    private GradientDrawable unlock_area_right;
    private GradientDrawable unlock_area_back;
    private GradientDrawable unlock_area_front;
    private GradientDrawable thatcham_area;
    private GradientDrawable remote_parking_area;
    private TextView debug_info;
    private Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        bundle = getArguments();
        View rootView = inflater.inflate(R.layout.debug_fragment, container, false);
        setView(rootView);
        return rootView;
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        signalReceived = (ImageView) rootView.findViewById(R.id.signalReceived);
        debug_info = (TextView) rootView.findViewById(R.id.debug_info);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (bundle != null) {
            updateCarDrawable(bundle.getBoolean("lockStatus"));
            applyNewDrawable();
        }
    }

    @Override
    public void lightUpArea(String area) {
        switch (area) {
            case PREDICTION_WELCOME:
                welcome_area.setColor(Color.WHITE);
                break;
            case PREDICTION_EXTERNAL:
            case PREDICTION_LOCK:
                lock_area.setColor(Color.RED);
                break;
            case PREDICTION_ROOF:
                rooftop.setStroke(7, Color.RED);
                break;
            case PREDICTION_ACCESS:
                unlock_area_left.setColor(Color.GREEN);
                unlock_area_right.setColor(Color.GREEN);
                unlock_area_front.setColor(Color.GREEN);
                unlock_area_back.setColor(Color.GREEN);
                break;
            case PREDICTION_LEFT:
                unlock_area_left.setColor(Color.GREEN);
                break;
            case PREDICTION_RIGHT:
                unlock_area_right.setColor(Color.GREEN);
                break;
            case PREDICTION_FRONT:
                unlock_area_front.setColor(Color.GREEN);
                break;
            case PREDICTION_BACK:
                unlock_area_back.setColor(Color.GREEN);
                break;
            case PREDICTION_INTERNAL:
                start_area_fl.setColor(Color.CYAN);
                start_area_fr.setColor(Color.CYAN);
                start_area_rl.setColor(Color.CYAN);
                start_area_rr.setColor(Color.CYAN);
                if (trunk_area != null) {
                    trunk_area.setColor(Color.CYAN);
                }
                break;
            case PREDICTION_START:
                start_area_fl.setColor(Color.CYAN);
                start_area_fr.setColor(Color.CYAN);
                start_area_rl.setColor(Color.CYAN);
                start_area_rr.setColor(Color.CYAN);
                break;
            case PREDICTION_START_FL:
                start_area_fl.setColor(Color.CYAN);
                break;
            case PREDICTION_START_FR:
                start_area_fr.setColor(Color.CYAN);
                break;
            case PREDICTION_START_RL:
                start_area_rl.setColor(Color.CYAN);
                break;
            case PREDICTION_START_RR:
                start_area_rr.setColor(Color.CYAN);
                break;
            case PREDICTION_TRUNK:
                if (trunk_area != null) {
                    trunk_area.setColor(Color.MAGENTA);
                }
                break;
            case PREDICTION_THATCHAM:
                thatcham_area.setColor(Color.YELLOW);
                break;
            case PREDICTION_NEAR:
                remote_parking_area.setColor(Color.CYAN);
                break;
            case PREDICTION_FAR:
                remote_parking_area.setColor(Color.RED);
                break;
            case PREDICTION_INSIDE:
                start_area_fl.setColor(Color.CYAN);
                start_area_fr.setColor(Color.CYAN);
                start_area_rl.setColor(Color.CYAN);
                start_area_rr.setColor(Color.CYAN);
                if (trunk_area != null) {
                    trunk_area.setColor(Color.CYAN);
                }
                break;
            case PREDICTION_OUTSIDE:
                lock_area.setColor(Color.RED);
                unlock_area_left.setColor(Color.RED);
                unlock_area_back.setColor(Color.RED);
                unlock_area_right.setColor(Color.RED);
                unlock_area_front.setColor(Color.RED);
                break;
        }
    }

    @Override
    public void darkenArea(String area) {
        switch (area) {
            case PREDICTION_WELCOME:
                welcome_area.setColor(Color.BLACK);
                break;
            case PREDICTION_EXTERNAL:
            case PREDICTION_LOCK:
                lock_area.setColor(Color.BLACK);
                break;
            case PREDICTION_ROOF:
                rooftop.setStroke(0, Color.TRANSPARENT);
                break;
            case PREDICTION_ACCESS:
                unlock_area_left.setColor(Color.BLACK);
                unlock_area_right.setColor(Color.BLACK);
                unlock_area_front.setColor(Color.BLACK);
                unlock_area_back.setColor(Color.BLACK);
                break;
            case PREDICTION_LEFT:
                unlock_area_left.setColor(Color.BLACK);
                break;
            case PREDICTION_RIGHT:
                unlock_area_right.setColor(Color.BLACK);
                break;
            case PREDICTION_FRONT:
                unlock_area_front.setColor(Color.BLACK);
                break;
            case PREDICTION_BACK:
                unlock_area_back.setColor(Color.BLACK);
                break;
            case PREDICTION_INTERNAL:
                start_area_fl.setColor(Color.BLACK);
                start_area_fr.setColor(Color.BLACK);
                start_area_rl.setColor(Color.BLACK);
                start_area_rr.setColor(Color.BLACK);
                if (trunk_area != null) {
                    trunk_area.setColor(Color.BLACK);
                }
                break;
            case PREDICTION_START:
                start_area_fl.setColor(Color.BLACK);
                start_area_fr.setColor(Color.BLACK);
                start_area_rl.setColor(Color.BLACK);
                start_area_rr.setColor(Color.BLACK);
                break;
            case PREDICTION_START_FL:
                start_area_fl.setColor(Color.BLACK);
                break;
            case PREDICTION_START_FR:
                start_area_fr.setColor(Color.BLACK);
                break;
            case PREDICTION_START_RL:
                start_area_rl.setColor(Color.BLACK);
                break;
            case PREDICTION_START_RR:
                start_area_rr.setColor(Color.BLACK);
                break;
            case PREDICTION_TRUNK:
                if (trunk_area != null) {
                    trunk_area.setColor(Color.BLACK);
                }
                break;
            case PREDICTION_THATCHAM:
                thatcham_area.setColor(Color.BLACK);
                break;
            case PREDICTION_NEAR:
            case PREDICTION_FAR:
                remote_parking_area.setColor(Color.BLACK);
                break;
        }
    }

    @Override
    public void applyNewDrawable() {
        signalReceived.setImageDrawable(layerDrawable);
    }

    @Override
    public void printDebugInfo(final SpannedString spannedString) {
        debug_info.setText(spannedString);
    }

    @Override
    public void updateCarDrawable(final boolean isLocked) {
        layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rssi_localization);
        Drawable carDrawable;
        switch (SdkPreferencesHelper.getInstance().getConnectedCarType()) {
            case ConnectedCarFactory.TYPE_2_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_2_a_close, R.drawable.car_2_a_open);
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_2_B:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_2_b_close, R.drawable.car_2_b_open);
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_3_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_3_a_close, R.drawable.car_3_a_open);
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_4_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_4_a_close, R.drawable.car_4_a_open);
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_4_B:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_4_b_close, R.drawable.car_4_b_open);
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_5_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_5_close, R.drawable.car_5_open);
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_6_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_6_close, R.drawable.car_6_open);
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_7_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_7_close, R.drawable.car_7_open);
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_8_A:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_8_close, R.drawable.car_8_open);
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            default:
                carDrawable = chooseCarDrawable(isLocked, R.drawable.car_8_close, R.drawable.car_8_open);
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
        }
        welcome_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.welcome_area);
        start_area_fl = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_fl);
        start_area_fr = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_fr);
        start_area_rl = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_rl);
        start_area_rr = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_rr);
        lock_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.lock_area);
        rooftop = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rooftop);
        unlock_area_left = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_left);
        unlock_area_right = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_right);
        unlock_area_back = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_back);
        unlock_area_front = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_front);
        thatcham_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.thatcham_area);
        remote_parking_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.remote_parking_area);
        layerDrawable.setDrawableByLayerId(R.id.car_drawable, carDrawable);
    }

    private Drawable chooseCarDrawable(boolean isLocked, int isCloseChoice, int isOpenChoice) {
        if (isLocked) {
            return ContextCompat.getDrawable(getActivity(), isCloseChoice);
        } else {
            return ContextCompat.getDrawable(getActivity(), isOpenChoice);
        }
    }
}
