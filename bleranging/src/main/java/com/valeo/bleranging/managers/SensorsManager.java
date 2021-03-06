package com.valeo.bleranging.managers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.CalculusUtils;
import com.valeo.bleranging.utils.CallReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by l-avaratha on 25/11/2016
 */
public class SensorsManager implements SensorEventListener {
    /**
     * Single helper instance.
     */
    private static SensorsManager sSingleInstance = null;
    private final BleRangingListener bleRangingListener;
    private final Handler mIsFrozenTimeOutHandler;
    private final ArrayList<Double> lAccHistoric;
    private final float R[] = new float[9];
    private final float I[] = new float[9];
    private final float orientation[] = new float[3];
    private final CallReceiver callReceiver = new CallReceiver();
    private final BroadcastReceiver bleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bleRangingListener.askBleOn();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
    private double deltaLinAcc = 0;
    private boolean smartphoneIsFrozen = false;
    private final Runnable isFrozenRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsFrozen = true; // smartphone is staying still
            mIsFrozenTimeOutHandler.removeCallbacks(this);
        }
    };
    private boolean isFrozenRunnableAlreadyLaunched = false;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private boolean smartphoneIsInPocket = false;

    /**
     * Private constructor.
     */
    private SensorsManager(final Context context, final BleRangingListener bleRangingListener) {
        this.bleRangingListener = bleRangingListener;
        this.mIsFrozenTimeOutHandler = new Handler();
        this.lAccHistoric = new ArrayList<>();
        SensorManager senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor magnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor senLinAcceleration = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senLinAcceleration, SensorManager.SENSOR_DELAY_UI);
        senSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        context.registerReceiver(callReceiver, new IntentFilter());
        context.registerReceiver(bleStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**
     * Initialize the helper instance.
     * @param context the context
     * @param bleRangingListener the ble ranging listener
     */
    public static void initializeInstance(final Context context, final BleRangingListener bleRangingListener) {
        if (sSingleInstance == null) {
            sSingleInstance = new SensorsManager(context.getApplicationContext(), bleRangingListener);
        }
    }

    /**
     * @return the single helper instance.
     */
    public static SensorsManager getInstance() {
        return sSingleInstance;
    }

    /**
     * Get sensors debug data
     *
     * @return a spannableStringBuilder with sensors debug data
     */
    SpannableStringBuilder createSensorsDebugData() {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(String.format(Locale.FRANCE, "%.3f %.3f %.3f\n", orientation[0], orientation[1], orientation[2]));
        spannableStringBuilder.append(String.format(Locale.FRANCE, "%.3f\n", deltaLinAcc));
        return spannableStringBuilder;
    }

    /**
     * Unregister broadcast receivers
     * @param context the context
     */
    public void closeApp(final Context context) {
        try {
            context.unregisterReceiver(callReceiver);
            context.unregisterReceiver(bleStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the accelerometer, magnetic field and pocket sensors data
     * @param event the event that gives sensors data
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //near
            smartphoneIsInPocket = (event.values[0] == 0);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
            if (lAccHistoric.size() == SdkPreferencesHelper.getInstance().getLinAccSize()) {
                lAccHistoric.remove(0);
            }
            double currentLinAcc = CalculusUtils.getQuadratiqueSum(event.values[0], event.values[1], event.values[2]);
            lAccHistoric.add(currentLinAcc);
            double averageLinAcc = CalculusUtils.getAverage(lAccHistoric);
            deltaLinAcc = Math.abs(currentLinAcc - averageLinAcc);
            if (deltaLinAcc < SdkPreferencesHelper.getInstance().getFrozenThreshold()) {
                if (!isFrozenRunnableAlreadyLaunched) {
                    mIsFrozenTimeOutHandler.postDelayed(isFrozenRunnable, 3000); // wait before apply stillness
                    isFrozenRunnableAlreadyLaunched = true;
                }
            } else {
                smartphoneIsFrozen = false; // smartphone is moving
                mIsFrozenTimeOutHandler.removeCallbacks(isFrozenRunnable);
                isFrozenRunnableAlreadyLaunched = false;
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            Arrays.fill(R, 0);
            Arrays.fill(I, 0);
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(R, orientation); // orientation contains: azimut, pitch and roll
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Get smartphone orientation
     * @return the coord of smartphone orientation
     */
    public float[] getOrientation() {
        return orientation;
    }

    /**
     * Get gravity
     * @return the gravity measured by the phone
     */
    public float[] getGravity() {
        return mGravity;
    }

    /**
     * Get magnetic field
     * @return the coord of magnetic field measured by the phone
     */
    public float[] getGeomagnetic() {
        return mGeomagnetic;
    }

    /**
     * Get linear acceleration
     * @return the acceleration measured by the phone
     */
    public double getAcceleration() {
        return deltaLinAcc;
    }

    /**
     * Get the phone movement
     * @return true if the smartphone is lay down, false if it is moving
     */
    public boolean isSmartphoneFrozen() {
        return smartphoneIsFrozen;
    }

    /**
     * Get the phone position in the pocket
     * @return true if it is in the pocket, false otherwise
     */
    public boolean isSmartphoneInPocket() {
        return smartphoneIsInPocket;
    }
}
