package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.SoundUtils;
import com.valeo.psa.R;


/**
 * Created by l-avaratha on 09/03/2017
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class MeasureFragment extends Fragment {
    private EditText measurement_index;
    private MeasureFragmentActionListener mListener;
    private Button start_measurement;
    private Button flash_button;
    private Handler mMainHandler;
    private Handler mHandler;
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean mFlashAvailable;
    private boolean mFlashEnabled;
    private final CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(@NonNull String cameraId) {
            super.onTorchModeUnavailable(cameraId);
            if (cameraId.equals(mCameraId)) {
                PSALogs.d("camera", "onTorchModeUnavailable");
                mFlashAvailable = false;
                mFlashEnabled = false;
            }
        }

        @Override
        public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            PSALogs.d("camera", "onTorchModeChanged " + enabled);
            mFlashAvailable = true;
            mFlashEnabled = enabled;
        }
    };
    private byte counterByte = 0;
    private byte savedCounterByte = 0;

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
        flash_button = (Button) rootView.findViewById(R.id.flash_button);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Set OnClickListeners
     */
    private void setOnClickListeners() {
        start_measurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            start_measurement.setEnabled(false);
                            measurement_index.setEnabled(false);
                            start_measurement.setText(R.string.measuring);
                            start_measurement.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                            if (measurement_index.getText().toString().equals("0")) {
                                incrementCounter("0");
                                measurement_index.setText(getCounter());
                            }
                            enableCounter();
                        }
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isAdded()) {
                                    SoundUtils.makeNoise(getActivity(), mMainHandler, ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
                                    incrementCounter(measurement_index.getText().toString());
                                    measurement_index.setText(getCounter());
                                    start_measurement.setText(R.string.start_measure);
                                    start_measurement.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                                    cancelCounter();
                                    start_measurement.setEnabled(true);
                                    measurement_index.setEnabled(true);
                                }
                            }
                        }, (SdkPreferencesHelper.getInstance().getMeasurementInterval() * 1000));
                    }
                });
            }
        });
        flash_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PSALogs.d("camera", "flash toggle");
                blink(SdkPreferencesHelper.getInstance().getFlashFrequency());
            }
        });
    }

    private void blink(int nanoSecondPeriod) {
        String myString = "0101010101";
        for (int i = 0; i < myString.length(); i++) {
            if (myString.charAt(i) == '0') {
                PSALogs.d("torch", "Flash ON");
                long timestamp = System.nanoTime();
                setTorchOn();
                long timestampEnd = System.nanoTime();
                PSALogs.d("torch", (timestampEnd - timestamp) + " ns");
            } else {
                long timestamp = System.nanoTime();
                setTorchOff();
                long timestampEnd = System.nanoTime();
                PSALogs.d("torch", "Flash OFF");
                PSALogs.d("torch", (timestampEnd - timestamp) + " ns");
            }
            try {
                Thread.sleep(0, nanoSecondPeriod);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraManager != null) {
            mCameraManager.unregisterTorchCallback(mTorchCallback);
            PSALogs.d("camera", "unregisterTorchCallback.");
            mCameraManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
    }

    private void initialize() {
        if (isAdded()) {
            mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = getCameraId();
            } catch (Throwable e) {
                PSALogs.e("camera", "Couldn't initialize \n" + e.toString());
                return;
            }
            if (mCameraId != null) {
                ensureHandler();
                mCameraManager.registerTorchCallback(mTorchCallback, mHandler);
                PSALogs.d("camera", "registerTorchCallback.");
            }
            PSALogs.d("camera", "initialized.");
        }
    }

    private synchronized void ensureHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread("TorchRessources", Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            mHandler = new Handler(thread.getLooper());
            PSALogs.d("camera", "handler created");
        }
    }

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                PSALogs.d("camera", "camera id " + id);
                return id;
            }
        }
        return null;
    }

    private boolean setTorchOn() {
        if (mFlashAvailable) {
            try {
                if (mFlashEnabled) {
                    PSALogs.d("camera", "Flash already enabled");
                }
                if (mCameraManager == null) {
                    PSALogs.d("camera", "mCameraManager NULL");
                }
                if (!mFlashEnabled && mCameraManager != null) {
                    mCameraManager.setTorchMode(mCameraId, true);
                    PSALogs.d("camera", "flash ON.");
                    return true;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        PSALogs.d("camera", "flash UNAVAILABLE.");
        return false;
    }

    private boolean setTorchOff() {
        if (mFlashAvailable) {
            try {
                if (mFlashEnabled && mCameraManager != null) {
                    mCameraManager.setTorchMode(mCameraId, false);
                    PSALogs.d("camera", "flash OFF.");
                    return true;
                } else {
                    PSALogs.d("camera", "flash already disabled.");
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        PSALogs.d("camera", "flash UNAVAILABLE.");
        return false;
    }

    private void incrementCounter(String counterValue) {
        savedCounterByte = (byte) (Byte.valueOf(counterValue) + 1);
    }

    private void cancelCounter() {
        counterByte = 0;
    }

    private void enableCounter() {
        counterByte = savedCounterByte;
    }

    private String getCounter() {
        return String.valueOf(savedCounterByte);
    }

    public byte getMeasureCounterByte() {
        return savedCounterByte;
    }

    public interface MeasureFragmentActionListener {
    }
}
