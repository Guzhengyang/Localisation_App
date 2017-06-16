package com.valeo.psa.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.trncic.library.DottedProgressBar;
import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.LogFileUtils;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;
import com.valeo.psa.fragment.AccuracyFragment;
import com.valeo.psa.fragment.CalibrationDialogFragment;
import com.valeo.psa.fragment.ChessBoardFragment;
import com.valeo.psa.fragment.DebugFragment;
import com.valeo.psa.fragment.MainFragment;
import com.valeo.psa.fragment.MeasureFragment;
import com.valeo.psa.fragment.NfcFragment;
import com.valeo.psa.fragment.RkeFragment;
import com.valeo.psa.fragment.StartFragment;
import com.valeo.psa.interfaces.CalibrationDialogFragmentListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BleRangingListener,
        RkeFragment.RkeFragmentActionListener, AccuracyFragment.AccuracyFragmentActionListener,
        StartFragment.StartFragmentActionListener, MainFragment.MainFragmentActionListener,
        CalibrationDialogFragmentListener, MeasureFragment.MeasureFragmentActionListener {
    private static final String TAG = MainActivity.class.getName();
    private static final int RESULT_SETTINGS = 20;
    private static final int REQUEST_ENABLE_BT = 25117;
    //    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    private static final int NOTIFICATION_ID_1 = 1;
    private Toolbar toolbar;
    private DottedProgressBar little_round_progressBar;
    private MainFragment mainFragment;
    private RkeFragment rkeFragment;
    private ChessBoardFragment chessboardFragment;
    private DebugFragment debugFragment;
    private MeasureFragment measureFragment;
    private AccuracyFragment accuracyFragment;
    private StartFragment startFragment;
    private TextView activity_title;
    private Typeface lightTypeFace;
    private BleRangingHelper mBleRangingHelper;
    private boolean showMenu = true;
    //    private KeyguardManager mKeyguardManager;

    //    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void showAuthenticationScreen() {
//        // Create the Confirm Credentials screen. You can customize the title and description. Or
//        // we will provide a generic one for you if you leave it null
//        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
//        if (intent != null) {
//            PSALogs.d(TAG, "showAuthenticationScreen " + REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
////            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS); // TODO uncomment to activate
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psa_activity_main);
        setFragments();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        little_round_progressBar = (DottedProgressBar) findViewById(R.id.little_round_progressBar);
        setSupportActionBar(toolbar);
        setFonts();
        this.mBleRangingHelper = new BleRangingHelper(this, chessboardFragment, debugFragment,
                measureFragment, rkeFragment, accuracyFragment);
        final Bundle bundleArgs = new Bundle();
        bundleArgs.putBoolean("lockStatus", mBleRangingHelper.getLockStatus());
        debugFragment.setArguments(bundleArgs);
//        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        if (!mKeyguardManager.isKeyguardSecure()) {
//            // Show a message that the user hasn't set up a lock screen.
//            Toast.makeText(this, getString(R.string.set_security_lock), Toast.LENGTH_LONG).show();
//        }
//        showAuthenticationScreen();
        if (!SdkPreferencesHelper.getInstance().isCalibrated()) {
            new CalibrationDialogFragment().show(getSupportFragmentManager(), getString(R.string.calibration));
        }
    }

    private void setFragments() {
        mainFragment = new MainFragment();
        rkeFragment = new RkeFragment();
        chessboardFragment = new ChessBoardFragment();
        debugFragment = new DebugFragment();
        measureFragment = new MeasureFragment();
        accuracyFragment = new AccuracyFragment();
        final NfcFragment nfcFragment = new NfcFragment();
        startFragment = new StartFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_frame, mainFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.door_status_switcher, rkeFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.chessboard_rl, chessboardFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.debug_rl, debugFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.measure_rl, measureFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.accuracy_rl, accuracyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.nfc_rl, nfcFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.start_rl, startFragment).commit();
        showHideFragment(startFragment); // hide startFragment
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                if (!PSALogs.DEBUG) {
                    settingIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.PSASettingsFragment.class.getName());
                    settingIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                }
                startActivityForResult(settingIntent, RESULT_SETTINGS);
                break;
//            case R.id.menu_login:
//                Intent loginIntent = new Intent(this, LoginActivity.class);
//                startActivityForResult(loginIntent, RESULT_SETTINGS);
//                break;
//            case R.id.menu_security_questions:
//                Intent securityQuestionsIntent = new Intent(this, SecurityQuestionsActivity.class);
//                startActivityForResult(securityQuestionsIntent, RESULT_SETTINGS);
//                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_settings).setVisible(showMenu);
        return true;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == BleRangingHelper.REQUEST_PERMISSION_ALL) {
            // Received permission result for permission.
            PSALogs.i(TAG, "Received response for permission request.");
            if (permissions.length > 0 && grantResults.length > 0) {
                for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
                    // Check if the permission has been granted
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // permission has been granted
                        PSALogs.i(TAG, permissions[i] + " permission has now been granted.");
                        showSnackBar(getString(R.string.permision_available));
                    } else {
                        PSALogs.i(TAG, permissions[i] + " permission was NOT granted.");
                        showSnackBar(getString(R.string.permissions_not_granted));
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    mBleRangingHelper.toggleBluetooth(true);
                    mBleRangingHelper.relaunchScan();
                } else {
                    Toast.makeText(this, "This app won't work without bluetooth.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Set Fonts
     */
    private void setFonts() {
        try {
            lightTypeFace = Typeface.createFromAsset(getAssets(), "HelveticaNeueLTStd-Lt.otf");
        } catch (Exception e) {
            PSALogs.e(TAG, "Font not loaded !");
        }
        activity_title = (TextView) toolbar.findViewById(R.id.activity_title);
        activity_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        activity_title.setGravity(Gravity.CENTER);
        final TextView unlock_trunk_title = (TextView) toolbar.findViewById(R.id.unlock_trunk_title);
        final TextView lock_trunk_title = (TextView) toolbar.findViewById(R.id.lock_trunk_title);
        final TextView close_all_windows_title = (TextView) toolbar.findViewById(R.id.close_all_windows_title);
        unlock_trunk_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        lock_trunk_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        close_all_windows_title.setTypeface(lightTypeFace, Typeface.NORMAL);
    }

    public void showHideFragment(final Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            if (fragment.isHidden()) {
                ft.show(fragment);
            } else {
                ft.hide(fragment);
            }
            ft.commit();
        }
    }

    /**
     * Get the status bar height
     *
     * @param res the app resources
     * @return the height of the status bar
     */
    private int statusBarHeight(Resources res) {
        return (int) (R.dimen.status_bar_height * res.getDisplayMetrics().density);
    }

    /**
     * Switch between toolbar's (normal and new toolbar mode)
     *
     * @param mainToNewToolBar boolean to determine which toolbar to inflate
     * @param resId            the resource id of the new toolbar to show or hide
     */
    public void switchToolbars(boolean mainToNewToolBar, int resId) {
        if (mainToNewToolBar) {
            showMenu = false;
            invalidateOptionsMenu();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = layoutParams.height + statusBarHeight(getResources());
            toolbar.setLayoutParams(layoutParams);
            toolbar.findViewById(resId).setVisibility(View.VISIBLE);
            toolbar.findViewById(R.id.main_toolbar).setVisibility(View.GONE);
            mainFragment.blurActivity(true);
        } else {
            showMenu = true;
            invalidateOptionsMenu();
            toolbar.findViewById(resId).setVisibility(View.GONE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = layoutParams.height - statusBarHeight(getResources());
            toolbar.setLayoutParams(layoutParams);
            toolbar.findViewById(R.id.main_toolbar).setVisibility(View.VISIBLE);
            mainFragment.blurActivity(false);
        }
    }

    private void setActivityTitle() {
        switch (SdkPreferencesHelper.getInstance().getConnectedCarBase()) {
            case ConnectedCarFactory.BASE_1:
                activity_title.setText(String.format(Locale.FRANCE, getString(R.string.title_activity_main), getString(R.string.psu), getString(R.string.psu)));
                break;
            case ConnectedCarFactory.BASE_2:
                activity_title.setText(String.format(Locale.FRANCE, getString(R.string.title_activity_main), getString(R.string.wal), getString(R.string.psu)));
                break;
            case ConnectedCarFactory.BASE_3:
                activity_title.setText(String.format(Locale.FRANCE, getString(R.string.title_activity_main), getString(R.string.wal), getString(R.string.uir)));
                break;
            case ConnectedCarFactory.BASE_4:
                activity_title.setText(String.format(Locale.FRANCE, getString(R.string.title_activity_main), getString(R.string.psu), getString(R.string.uir)));
                break;
        }
    }

    @Override
    public void restartConnection(boolean createCar) {
        mBleRangingHelper.restartConnection(createCar);
    }

    @Override
    public void startProgress() {
        little_round_progressBar.startProgress();
    }

    @Override
    public void stopProgress() {
        little_round_progressBar.stopProgress();
    }

    @Override
    public void updateBLEStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBleRangingHelper.isFullyConnected()) {
                    mainFragment.setBleStatus(R.string.connected_over_ble);
                } else {
                    mainFragment.setBleStatus(R.string.not_connected_over_ble);
                    if (rkeFragment != null) {
                        rkeFragment.resetDisplayAfterDisconnection();
                    }
                }
            }
        });
    }

    @Override
    public void doWelcome() {
        Intent actionIntent = new Intent(this, MainActivity.class);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(this, 0, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new android.support.v7.app.NotificationCompat.Builder(this)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(android.support.v7.app.NotificationCompat.VISIBILITY_PRIVATE)
                .setSmallIcon(R.mipmap.peugeot_notif_logo)
                .setContentTitle(getString(R.string.welcome_notif_title))
                .setContentText(getString(R.string.welcome_notif_message))
                .setContentIntent(actionPendingIntent)
                .build();
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_1, notification);
    }

    @Override
    public void askBleOn() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void showSnackBar(String message) {
        mainFragment.setSnackBarMessage(message);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mainFragment.setAdapterLastPosition();
        mBleRangingHelper.closeApp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBleRangingHelper != null && !mBleRangingHelper.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBleRangingHelper != null && !mBleRangingHelper.isCloseAppCalled()) {
            if (LogFileUtils.createLogFile(this)) {
                LogFileUtils.writeFirstColumnSettings();
                String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
                LogFileUtils.appendSettingLogs(connectedCarType,
                        SdkPreferencesHelper.getInstance().getConnectedCarBase(),
                        SdkPreferencesHelper.getInstance().getTrxAddressConnectable(),
                        SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl(),
                        SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC(), SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft(),
                        SdkPreferencesHelper.getInstance().getTrxAddressFrontRight(), SdkPreferencesHelper.getInstance().getTrxAddressLeft(),
                        SdkPreferencesHelper.getInstance().getTrxAddressMiddle(), SdkPreferencesHelper.getInstance().getTrxAddressRight(),
                        SdkPreferencesHelper.getInstance().getTrxAddressTrunk(), SdkPreferencesHelper.getInstance().getTrxAddressRearLeft(),
                        SdkPreferencesHelper.getInstance().getTrxAddressBack(), SdkPreferencesHelper.getInstance().getTrxAddressRearRight(),
                        SdkPreferencesHelper.getInstance().getRssiLogNumber(),
                        SdkPreferencesHelper.getInstance().getCryptoPreAuthTimeout(),
                        SdkPreferencesHelper.getInstance().getCryptoActionTimeout(),
                        SdkPreferencesHelper.getInstance().getWantedSpeed(), SdkPreferencesHelper.getInstance().getOneStepSize());
                LogFileUtils.writeFirstColumnLogs();
            }
            mBleRangingHelper.initializeConnectedCar();
        }
        if (chessboardFragment != null) {
            if (SdkPreferencesHelper.getInstance().getConnectedCarType()
                    .equalsIgnoreCase(ConnectedCarFactory.TYPE_8_A)) {
                if (chessboardFragment.isHidden()) {
                    showHideFragment(chessboardFragment); // show chessboardFragment
                }
            } else if (!chessboardFragment.isHidden()) {
                showHideFragment(chessboardFragment); // hide chessboardFragment
            }
        }
        setActivityTitle();
    }

    @Override
    public boolean isRKEButtonClickable() {
        return mBleRangingHelper.isRKEButtonClickable();
    }

    @Override
    public void performRKELockAction(boolean b) {
        mBleRangingHelper.performRKELockAction(b);
    }

    @Override
    public void updateCarDrawable() {
        if (debugFragment != null) {
            debugFragment.updateCarDrawable(mBleRangingHelper.getLockStatus());
        }
    }

    @Override
    public void calculateAccuracy() {
        mBleRangingHelper.calculateAccuracy();
    }

    @Override
    public int getCalculatedAccuracy(String zone) {
        return mBleRangingHelper.getCalculatedAccuracy(zone);
    }

    @Override
    public String[] getStandardClasses() {
        return mBleRangingHelper.getStandardClasses();
    }

    @Override
    public void startButtonActions() {
        if (startFragment != null) {
            toolbar.setVisibility(View.GONE);
            showHideFragment(mainFragment);
            showHideFragment(startFragment);
            mBleRangingHelper.setIsStartRequested(true);
            startFragment.startButtonActions(mBleRangingHelper);
        }
    }

    @Override
    public void startButtonActionsFinished() {
        if (startFragment != null) {
            toolbar.setVisibility(View.VISIBLE);
            mBleRangingHelper.setIsStartRequested(false);
            showHideFragment(startFragment);
            showHideFragment(mainFragment);
        }
    }

    @Override
    public boolean isFrozen() {
        return mBleRangingHelper.isSmartphoneFrozen();
    }

    @Override
    public boolean isConnected() {
        return mBleRangingHelper.isFullyConnected();
    }

    @Override
    public void setSmartphoneOffset() {
        mBleRangingHelper.setSmartphoneOffset();
    }

    @Override
    public void incrementCounter(String counterValue) {
        mBleRangingHelper.incrementCounter(counterValue);
    }

    @Override
    public void cancelCounter() {
        mBleRangingHelper.cancelCounter();
    }

    @Override
    public String printCounter() {
        return mBleRangingHelper.getCounter();
    }
}
