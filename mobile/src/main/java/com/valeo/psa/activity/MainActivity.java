package com.valeo.psa.activity;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.SpannableStringBuilder;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trncic.library.DottedProgressBar;
import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TrxUtils;
import com.valeo.psa.R;
import com.valeo.psa.model.Car;
import com.valeo.psa.model.ViewModel;
import com.valeo.psa.model.ViewModelId;
import com.valeo.psa.utils.BlurBuilder;
import com.valeo.psa.utils.PreferenceUtils;
import com.valeo.psa.view.CarListAdapter;
import com.valeo.psa.view.DividerItemDecoration;
import com.valeo.psa.view.MyRecyclerAdapter;
import com.valeo.psa.view.ReverseProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_BACK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FRONT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_INSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_NEAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_FL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_FR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_RL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_RR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_THATCHAM;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_TRUNK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_WELCOME;
import static com.valeo.bleranging.BleRangingHelper.RKE_USE_TIMEOUT;

public class MainActivity extends AppCompatActivity implements MyRecyclerAdapter.OnStartDragListener,
        MyRecyclerAdapter.OnIconLongPressedListener, View.OnTouchListener, BleRangingListener {
    private static final String TAG = MainActivity.class.getName();
    private final static int FIVE_MINUTES_IN_MILLI = 300000;
    private final static int MINUTE_IN_MILLI = 60000;
    private final static int SECOND_IN_MILLI = 1000;
    private final static float SCROLL_THRESHOLD = 10;
    private static final int RESULT_SETTINGS = 20;
    private static final int REQUEST_ENABLE_BT = 25117;
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    private static final int NOTIFICATION_ID_1 = 1;
    private static final int MAX_ROWS = 12;
    private static final int MAX_COLUMNS = 10;
    private final Handler mHandler = new Handler();
    private final Paint paintOne = new Paint();
    private final Paint paintTwo = new Paint();
    private final Paint paintCar = new Paint();
    private final Paint paintUnlock = new Paint();
    private final Paint paintLock = new Paint();
    private Toolbar toolbar;
    private FrameLayout main_frame;
    private NestedScrollView content_main;
    private CoordinatorLayout main_scroll;
    private AppBarLayout main_appbar;
    private ImageView blur_on_touch;
    private RelativeLayout content_start_car_dialog;
    private ReverseProgressBar start_car_timeout;
    private TextView car_start_countdown_min_sec;
    private TextView activity_title;
    private TextView ble_status;
    private TextView car_door_status;
    private TextView tips;
    private TextView nfc_disclaimer;
    private ImageView nfc_logo;
    private RecyclerView control_trunk_windows_lights;
    private RecyclerView car_model_recyclerView;
    private TextView selected_car_model_pinned;
    private TextView rke_loading_progress_bar;
    private ImageButton vehicle_locked;
    private ImageButton driver_s_door_unlocked;
    private ImageButton vehicle_unlocked;
    private ImageButton start_button;
    private ImageView chessboard;
    private ImageView signalReceived;
    private Animation pulseAnimation;
    private Animation pulseAnimation2;
    private ImageView start_button_first_wave;
    private ImageView start_button_second_wave;
    private LayerDrawable layerDrawable;
    private GradientDrawable welcome_area;
    private GradientDrawable start_area_fl;
    private GradientDrawable start_area_fr;
    private GradientDrawable start_area_rl;
    private GradientDrawable start_area_rr;
    private GradientDrawable trunk_area;
    private GradientDrawable lock_area;
    private GradientDrawable unlock_area_left;
    private GradientDrawable unlock_area_right;
    private GradientDrawable unlock_area_back;
    private GradientDrawable unlock_area_front;
    private GradientDrawable thatcham_area;
    private GradientDrawable remote_parking_area;
    private DottedProgressBar little_round_progressBar;
    private TextView debug_info;
    private ItemTouchHelper ith;
    private Typeface romanTypeFace;
    private Typeface lightTypeFace;
    private Typeface boldTypeFace;
    private CountDownTimer countDownTimer = null;
    private int progressMin;
    private int progressSec;
    private GestureDetectorCompat mDetector;
    private volatile View pressedView;
    private boolean isIconLongPressed = false;
    private float mDownX;
    private float mDownY;
    private CarListAdapter mCarListAdapter = null;
    private CarDoorStatus carDoorStatus;
    private BleRangingHelper mBleRangingHelper;
    private boolean showMenu = true;
    private KeyguardManager mKeyguardManager;
    private Car selectedCar = null;
    private NotificationManagerCompat notificationManager;
    private boolean predictionColor[][] = new boolean[MAX_ROWS][MAX_COLUMNS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psa_activity_main);
        setView();
        setRecyclerView();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setFonts();
        setOnClickListeners();
        main_appbar.setExpanded(false, false);
        this.mBleRangingHelper = new BleRangingHelper(this, this);
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
        pulseAnimation2 = AnimationUtils.loadAnimation(this, R.anim.pulse);
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            tips.setVisibility(View.VISIBLE);
            nfc_disclaimer.setVisibility(View.VISIBLE);
            nfc_logo.setVisibility(View.VISIBLE);
        }
        notificationManager = NotificationManagerCompat.from(MainActivity.this);
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!mKeyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a lock screen.
            Toast.makeText(this, getString(R.string.set_security_lock), Toast.LENGTH_LONG).show();
        }
        showAuthenticationScreen();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            PSALogs.d(TAG, "showAuthenticationScreen " + REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
//            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS); // TODO uncomment to activate
        }
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
            case R.id.menu_login:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, RESULT_SETTINGS);
                break;
            case R.id.menu_reconnect_ble:
                mBleRangingHelper.connect();
                break;
            case R.id.menu_reconnect_ble_pc:
                mBleRangingHelper.connectToPC();
                break;
            case R.id.menu_reconnect_ble_remote_control:
                mBleRangingHelper.connectToRemoteControl();
                break;
            case R.id.menu_relaunch_ble_scan:
                mBleRangingHelper.relaunchScan();
                break;
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
                        Snackbar.make(content_main, R.string.permision_available,
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        PSALogs.i(TAG, permissions[i] + " permission was NOT granted.");
                        Snackbar.make(content_main, R.string.permissions_not_granted,
                                Snackbar.LENGTH_SHORT).show();
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
        mDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (pressedView != null) {
                    View childView = control_trunk_windows_lights.findContainingItemView(pressedView);
                    onIconLongPressed(control_trunk_windows_lights.getChildAdapterPosition(childView));
                    isIconLongPressed = true;
                    mDownX = e.getX();
                    mDownY = e.getY();
                }
                super.onLongPress(e);
            }
        });
        vehicle_locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBleRangingHelper.isRKEButtonClickable()) {
                    rke_loading_progress_bar.setVisibility(View.VISIBLE);
                    carDoorStatus = CarDoorStatus.LOCKED;
                    vehicle_locked.setBackgroundResource(R.mipmap.slider_button);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    vehicle_unlocked.setBackgroundResource(0);
                    startButtonAnimation(false);
                    PSALogs.d("performLock", "RKE LOCK");
                    mBleRangingHelper.performRKELockAction(true); //lockVehicle
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
                if (mBleRangingHelper.isRKEButtonClickable()) {
                    rke_loading_progress_bar.setVisibility(View.VISIBLE);
                    carDoorStatus = CarDoorStatus.DRIVER_DOOR_OPEN;
                    driver_s_door_unlocked.setBackgroundResource(R.mipmap.slider_button);
                    vehicle_locked.setBackgroundResource(0);
                    vehicle_unlocked.setBackgroundResource(0);
                    start_button.setBackgroundResource(0);
                    startButtonAnimation(false);
                    PSALogs.d("performLock", "RKE UNLOCK 1");
                    mBleRangingHelper.performRKELockAction(false); //unlockVehicle
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
                if (mBleRangingHelper.isRKEButtonClickable()) {
                    rke_loading_progress_bar.setVisibility(View.VISIBLE);
                    carDoorStatus = CarDoorStatus.UNLOCKED;
                    vehicle_unlocked.setBackgroundResource(R.mipmap.slider_button);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    vehicle_locked.setBackgroundResource(0);
                    startButtonAnimation(true);
                    PSALogs.d("performLock", "RKE UNLOCK 2");
                    mBleRangingHelper.performRKELockAction(false); //unlockVehicle
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rke_loading_progress_bar.setVisibility(View.GONE);
                        }
                    }, RKE_USE_TIMEOUT);
                    createNotification(NOTIFICATION_ID_1, getString(R.string.notif_unlock_it),
                            R.mipmap.car_all_doors_button, getString(R.string.vehicle_unlocked));
                }
            }
        });
    }

    private void createNotification(int notifId, String message, int largeIcon, String secondSubText) {
        Intent actionIntent = new Intent(this, MainActivity.class);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        String carBrand = selectedCar == null ? mCarListAdapter.getCars()
                .get(0).getBrandCar() : selectedCar.getBrandCar();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(carBrand)
                        .setContentText(message);
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(largeIcon,
                        secondSubText, actionPendingIntent)
                        .build();
        notificationBuilder.extend(
                new NotificationCompat.WearableExtender()
                        .addAction(action)
                        .setBackground(BitmapFactory
                                .decodeResource(getResources(), R.mipmap.car_model_ds5)));
        notificationManager.notify(notifId, notificationBuilder.build());
    }

    /**
     * Set Fonts
     */
    private void setFonts() {
        try {
            romanTypeFace = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTStd-Ex.otf");
            lightTypeFace = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTStd-Lt.otf");
            boldTypeFace = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTStd-Bd.otf");
        } catch (Exception e) {
            PSALogs.e(TAG, "Font not loaded !");
        }
        activity_title = (TextView) toolbar.findViewById(R.id.activity_title);
        final TextView unlock_trunk_title = (TextView) toolbar.findViewById(R.id.unlock_trunk_title);
        final TextView lock_trunk_title = (TextView) toolbar.findViewById(R.id.lock_trunk_title);
        final TextView close_all_windows_title = (TextView) toolbar.findViewById(R.id.close_all_windows_title);
        activity_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        unlock_trunk_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        lock_trunk_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        close_all_windows_title.setTypeface(lightTypeFace, Typeface.NORMAL);
        activity_title.setGravity(Gravity.CENTER);
        ble_status.setTypeface(romanTypeFace, Typeface.NORMAL);
        car_door_status.setTypeface(lightTypeFace, Typeface.NORMAL);
        tips.setTypeface(boldTypeFace, Typeface.BOLD);
        nfc_disclaimer.setTypeface(lightTypeFace, Typeface.NORMAL);
    }

    private void setPaint() {
        paintOne.setColor(Color.LTGRAY);
        paintTwo.setColor(Color.BLACK);
        paintTwo.setStyle(Paint.Style.STROKE); // print border
        paintCar.setColor(Color.DKGRAY);
        paintUnlock.setColor(Color.GREEN);
        paintLock.setColor(Color.RED);
    }

    /**
     * Find all view by their id
     */
    private void setView() {
        main_frame = (FrameLayout) findViewById(R.id.main_frame);
        content_main = (NestedScrollView) findViewById(R.id.content_main);
        main_scroll = (CoordinatorLayout) findViewById(R.id.main_scroll);
        main_appbar = (AppBarLayout) findViewById(R.id.main_appbar);
        blur_on_touch = (ImageView) findViewById(R.id.blur_on_touch);
        content_start_car_dialog = (RelativeLayout) findViewById(R.id.content_start_car_dialog);
        start_car_timeout = (ReverseProgressBar) findViewById(R.id.start_car_timeout);
        car_start_countdown_min_sec = (TextView) findViewById(R.id.car_start_countdown_min_sec);
        ble_status = (TextView) findViewById(R.id.ble_status);
        car_door_status = (TextView) findViewById(R.id.car_door_status);
        tips = (TextView) findViewById(R.id.tips);
        nfc_disclaimer = (TextView) findViewById(R.id.nfc_disclaimer);
        nfc_logo = (ImageView) findViewById(R.id.nfc_logo);
        control_trunk_windows_lights = (RecyclerView) findViewById(R.id.control_trunk_windows_lights);
        car_model_recyclerView = (RecyclerView) findViewById(R.id.car_model_recyclerView);
        selected_car_model_pinned = (TextView) findViewById(R.id.selected_car_model_pinned);
        rke_loading_progress_bar = (TextView) findViewById(R.id.rke_loading_progress_bar);
        vehicle_locked = (ImageButton) findViewById(R.id.vehicle_locked);
        driver_s_door_unlocked = (ImageButton) findViewById(R.id.driver_s_door_unlocked);
        vehicle_unlocked = (ImageButton) findViewById(R.id.vehicle_unlocked);
        start_button = (ImageButton) findViewById(R.id.start_button);
        start_button_first_wave = (ImageView) findViewById(R.id.start_button_first_wave);
        start_button_second_wave = (ImageView) findViewById(R.id.start_button_second_wave);
        chessboard = (ImageView) findViewById(R.id.chessboard);
        signalReceived = (ImageView) findViewById(R.id.signalReceived);
        setPaint();
        updateCarDrawable();
        applyNewDrawable();
        little_round_progressBar = (DottedProgressBar) findViewById(R.id.little_round_progressBar);
        debug_info = (TextView) findViewById(R.id.debug_info);
    }

    /**
     * Set the MainActivity's Recycler View
     */
    private void setRecyclerView() {
        control_trunk_windows_lights.setHasFixedSize(true);
        control_trunk_windows_lights.setAdapter(new MyRecyclerAdapter(createActionControlList(),
                R.layout.psa_control_row, lightTypeFace, this, this));
        control_trunk_windows_lights.setLayoutManager(new LinearLayoutManager(this));
        control_trunk_windows_lights.setItemAnimator(new DefaultItemAnimator());
        control_trunk_windows_lights.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider_line));
        ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(((MyRecyclerAdapter) recyclerView.getAdapter()).getItems(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                // and notify the adapter that its dataset has changed
                recyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }
        };
        ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(control_trunk_windows_lights);
        //control_trunk_windows_lights.addOnItemTouchListener(new MyGestureListener());
        // make recycler view horizontal then set adapter
        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        car_model_recyclerView.setLayoutManager(llm);
        CarListAdapter.OnCarSelectionListener mCarSelectionListener = new CarListAdapter.OnCarSelectionListener() {
            @Override
            public void onCarSelection(int position) {
                selectedCar = mCarListAdapter.getCars().get(position);
                mCarListAdapter.setSelectedCarRegistrationPlate(selectedCar.getRegPlate());
                PreferenceUtils.loadSharedPreferencesFromInputStream(MainActivity.this,
                        getResources().openRawResource(
                                getResources().getIdentifier(selectedCar.getCarConfigFileName(),
                                        "raw", getPackageName())),
                        SdkPreferencesHelper.SAVED_CC_CONNECTION_OPTION);
            }
        };
        mCarListAdapter = new CarListAdapter(mCarSelectionListener);
        car_model_recyclerView.setAdapter(mCarListAdapter);
        mCarListAdapter.setCars(createCarList());
        // load car_one connection pref, when the app is launched
        PreferenceUtils.loadSharedPreferencesFromInputStream(MainActivity.this,
                getResources().openRawResource(R.raw.car_one),
                SdkPreferencesHelper.SAVED_CC_CONNECTION_OPTION);
        car_model_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastPos = -1;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = llm.findFirstCompletelyVisibleItemPosition();
                    if (position == -1) {
                        position = ((recyclerView.computeHorizontalScrollOffset() + 1) / recyclerView.computeHorizontalScrollExtent()); // +1 to avoid 0 division
                    }
                    if (position != -1 && position != lastPos) {
                        selectedCar = ((CarListAdapter) recyclerView.getAdapter()).getCars().get(position);
                        if (PreferenceUtils.loadSharedPreferencesFromInputStream(MainActivity.this,
                                getResources().openRawResource(
                                        getResources().getIdentifier(
                                                selectedCar.getCarConfigFileName(),
                                                "raw", getPackageName())),
                                SdkPreferencesHelper.SAVED_CC_CONNECTION_OPTION)) {
                            mBleRangingHelper.restartConnection(true);
                            lastPos = position;
                        } else {
                            Snackbar.make(recyclerView, "pref file not found", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    CarListAdapter.ViewHolder vh = (CarListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    if(vh != null) {
                        selected_car_model_pinned.setText(vh.getBrandCar().getText().toString());
                    }
                }
            }
        });
    }

    /**
     * Create an list of Car
     *
     * @return a list of Car
     */
    private List<Car> createCarList() {
        List<Car> resultList = new ArrayList<>(4);
        resultList.add(new Car(R.mipmap.car_model_ds5, "1", getString(R.string.ds5), getString(R.string.VIN), "car_one"));
        resultList.add(new Car(R.mipmap.car_model_ds5_2, "2", getString(R.string.ds5_2), getString(R.string.VIN2), "car_two"));
        resultList.add(new Car(R.mipmap.car_model_ds5_3, "3", getString(R.string.ds5_3), getString(R.string.VIN3), "car_three"));
        return resultList;
    }

    /**
     * Create an control action list of ViewModel
     *
     * @return a list of ViewModel
     */
    private List<ViewModel> createActionControlList() {
        List<ViewModel> resultList = new ArrayList<>(3);
        resultList.add(new ViewModel(ContextCompat.getDrawable(this, R.mipmap.open_trunk_button), getString(R.string.unlock_trunk), ViewModelId.UNLOCK_TRUNK));
        resultList.add(new ViewModel(ContextCompat.getDrawable(this, R.mipmap.close_windows_button), getString(R.string.close_all_windows), ViewModelId.CLOSE_ALL_WINDOWS));
        resultList.add(new ViewModel(ContextCompat.getDrawable(this, R.mipmap.flash_lights_button), getString(R.string.flash_lights), ViewModelId.FLASH_LIGHTS));
        return resultList;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        ith.startDrag(viewHolder);
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
            ble_status.setVisibility(View.GONE);
            activity_title.setText(R.string.cancel_start_car);
            activity_title.setGravity(Gravity.CENTER_VERTICAL);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) activity_title.getLayoutParams();
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.main_icon);
            layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            activity_title.setLayoutParams(layoutParams);
            activity_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToolbarStartCar(false);
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                }
            });
        } else {
            content_start_car_dialog.setVisibility(View.GONE);
            ble_status.setVisibility(View.VISIBLE);
            setActivityTitle();
            activity_title.setGravity(Gravity.CENTER);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) activity_title.getLayoutParams();
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            activity_title.setLayoutParams(layoutParams);
            activity_title.setOnClickListener(null);
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
    private void switchToolbars(boolean mainToNewToolBar, int resId) {
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
            blurActivity(true);
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
            blurActivity(false);
        }
    }

    private void blurActivity(boolean blurActivity) {
        if (blurActivity) {
            Bitmap blurredBitmap = BlurBuilder.blur(main_frame);
            blur_on_touch.setImageDrawable(new BitmapDrawable(getResources(), blurredBitmap));
            blur_on_touch.setVisibility(View.VISIBLE);
            main_scroll.setVisibility(View.GONE);
            ble_status.setVisibility(View.GONE);
        } else {
            blur_on_touch.setImageDrawable(null);
            blur_on_touch.setVisibility(View.GONE);
            main_scroll.setVisibility(View.VISIBLE);
            ble_status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onIconLongPressed(int position) {
        ViewModel viewModel = ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).getItems().get(position);
        if (viewModel != null) {
            switch (viewModel.getViewModelId()) {
                case UNLOCK_TRUNK:
                    PSALogs.d("Longpress", "UNLOCK_TRUNK detected ");
                    switchToolbars(true, R.id.unlock_trunk_toolbar);
                    break;
                case LOCK_TRUNK:
                    PSALogs.d("Longpress", "LOCK_TRUNK detected ");
                    switchToolbars(true, R.id.lock_trunk_toolbar);
                    break;
                case CLOSE_ALL_WINDOWS:
                    PSALogs.d("Longpress", "CLOSE_ALL_WINDOWS detected ");
                    switchToolbars(true, R.id.close_all_windows_toolbar);
                    little_round_progressBar.startProgress();
                    break;
                case FLASH_LIGHTS:
                    PSALogs.d("Longpress", "FLASH_LIGHTS detected ");
                    break;
                case UNKNOWN:
                    PSALogs.d("Longpress", "UNKNOWN detected ");
                    break;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        pressedView = v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isIconLongPressed) {
                    PSALogs.d("Longpress", "ACTION_DOWN");
                    mDetector.onTouchEvent(event);
                    pressedView = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isIconLongPressed && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                    PSALogs.d("Longpress", "ACTION_MOVE");
                    // no break to prevent moving while long pressing
                } else {
                    break;
                }
            case MotionEvent.ACTION_CANCEL:
                PSALogs.d("Longpress", "ACTION_CANCEL");
                // no break because no action up will be called
            case MotionEvent.ACTION_UP:
                if (isIconLongPressed) {
                    final View childView = control_trunk_windows_lights.findContainingItemView(pressedView);
                    final int position = control_trunk_windows_lights.getChildAdapterPosition(childView);
                    ViewModel viewModel = ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).getItems().get(position);
                    if (viewModel != null) {
                        switch (viewModel.getViewModelId()) {
                            case UNLOCK_TRUNK:
                                PSALogs.d("Longpress", "ACTION_UP UNLOCK_TRUNK");
                                switchToolbars(false, R.id.unlock_trunk_toolbar);
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).
                                                getItems().set(position,
                                                new ViewModel(ContextCompat.getDrawable(MainActivity.this, R.mipmap.close_trunk_button),
                                                        getString(R.string.lock_trunk),
                                                        ViewModelId.LOCK_TRUNK));
                                        control_trunk_windows_lights.getAdapter().notifyDataSetChanged();
                                    }
                                }, 100);
                                break;
                            case LOCK_TRUNK:
                                PSALogs.d("Longpress", "ACTION_UP LOCK_TRUNK");
                                switchToolbars(false, R.id.lock_trunk_toolbar);
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).
                                                getItems().set(position,
                                                new ViewModel(ContextCompat.getDrawable(MainActivity.this, R.mipmap.open_trunk_button),
                                                        getString(R.string.unlock_trunk),
                                                        ViewModelId.UNLOCK_TRUNK));
                                        control_trunk_windows_lights.getAdapter().notifyDataSetChanged();
                                    }
                                }, 100);
                                break;
                            case CLOSE_ALL_WINDOWS:
                                PSALogs.d("Longpress", "ACTION_UP CLOSE_ALL_WINDOWS");
                                switchToolbars(false, R.id.close_all_windows_toolbar);
                                little_round_progressBar.stopProgress();
                                break;
                            case FLASH_LIGHTS:
                                PSALogs.d("Longpress", "ACTION_UP FLASH_LIGHTS");
                                break;
                            case UNKNOWN:
                                PSALogs.d("Longpress", "ACTION_UP UNKNOWN");
                                break;
                        }
                    }
                } else {
                    PSALogs.d("Longpress", "ACTION_UP isIconLongPressed = false");
                }
                isIconLongPressed = false;
                mDownX = 0;
                mDownY = 0;
                pressedView = null;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                PSALogs.d("Longpress", "ACTION_OUTSIDE");
                break;
            case MotionEvent.ACTION_SCROLL:
                PSALogs.d("Longpress", "ACTION_SCROLL");
                break;
        }
        return true;
    }

    @Override
    public void lightUpArea(String area) {
        switch (area) {
            case PREDICTION_WELCOME:
                welcome_area.setColor(Color.WHITE);
                break;
            case PREDICTION_LOCK:
                lock_area.setColor(Color.RED);
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
                remote_parking_area.setColor(Color.RED);
                thatcham_area.setColor(Color.RED);
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
            case PREDICTION_LOCK:
                lock_area.setColor(Color.BLACK);
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
        chessboard.setBackground(drawChessBoard(1080, 1080));
    }

    @Override
    public void printDebugInfo(SpannableStringBuilder spannableStringBuilder) {
        debug_info.setText(spannableStringBuilder);
    }

    @Override
    public void updateBLEStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBleRangingHelper.isFullyConnected()) {
                    ble_status.setText(R.string.connected_over_ble);
                } else {
                    ble_status.setText(R.string.not_connected_over_ble);
                    vehicle_locked.setBackgroundResource(0);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    vehicle_unlocked.setBackgroundResource(0);
                    startButtonAnimation(false);
                }
            }
        });
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
        updateCarDrawable();
    }

    @Override
    public void updateCarDrawable() {
        layerDrawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.rssi_localization);
        Drawable carDrawable;
        switch (SdkPreferencesHelper.getInstance().getConnectedCarType()) {
            case ConnectedCarFactory.TYPE_2_A:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_2_a_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_2_a_open);
                }
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_2_B:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_2_b_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_2_b_open);
                }
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_3_A:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_3_a_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_3_a_open);
                }
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_4_A:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_4_a_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_4_a_open);
                }
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_4_B:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_4_b_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_4_b_open);
                }
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_5_A:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_5_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_5_open);
                }
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            case ConnectedCarFactory.TYPE_7_A:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_7_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_7_open);
                }
                trunk_area = null;
                break;
            case ConnectedCarFactory.TYPE_8_A:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_8_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_8_open);
                }
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
            default:
                if (carDoorStatus == CarDoorStatus.LOCKED) {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_8_close);
                } else {
                    carDrawable = ContextCompat.getDrawable(this, R.drawable.car_8_open);
                }
                trunk_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.trunk_area);
                break;
        }
        welcome_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.welcome_area);
        start_area_fl = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_fl);
        start_area_fr = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_fr);
        start_area_rl = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_rl);
        start_area_rr = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area_rr);
        lock_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.lock_area);
        unlock_area_left = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_left);
        unlock_area_right = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_right);
        unlock_area_back = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_back);
        unlock_area_front = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_front);
        thatcham_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.thatcham_area);
        remote_parking_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.remote_parking_area);
        layerDrawable.setDrawableByLayerId(R.id.car_drawable, carDrawable);
    }

    private BitmapDrawable drawChessBoard(final int measuredWidth, final int measuredHeight) {
        for (boolean[] predictTab : predictionColor) {
            Arrays.fill(predictTab, true);
        }
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int stepX = measuredWidth / MAX_ROWS;
        int stepY = measuredHeight / MAX_COLUMNS;
        PSALogs.e("chess", "stepX " + stepX + " measuredWidth " + measuredWidth);
        PSALogs.e("chess", "stepY " + stepY + " measuredHeight " + measuredHeight);
        for (int width = 0, x = 0; width < measuredWidth && x < MAX_ROWS; width += stepX, x++) {
            for (int height = 0, y = 0; height < measuredHeight && y < MAX_COLUMNS; height += stepY, y++) {
                if ((y == 4 || y == 5) && (x == 4 || x == 5 || x == 6 || x == 7)) { // paint Car tiles
                    canvas.drawRect(width, height, width + stepX, height + stepY, paintCar);
                } else if (predictionColor[x][y]) { // paint phone position tiles in unlock or lock zone
                    if (((y == 2 || y == 3 || y == 6 || y == 7) && (x == 2 || x == 3 || x == 4 || x == 5 || x == 6 || x == 7 || x == 8 || x == 9))
                            || ((y == 4 || y == 5) && (x == 2 || x == 3 || x == 8 || x == 9))) {
                        canvas.drawRect(width, height, width + stepX, height + stepY, paintUnlock);
                        canvas.drawRect(width, height, width + stepX, height + stepY, paintTwo);
                    } else {
                        canvas.drawRect(width, height, width + stepX, height + stepY, paintLock);
                        canvas.drawRect(width, height, width + stepX, height + stepY, paintTwo);
                    }
                } else { // paint unoccupied zones tiles
                    canvas.drawRect(width, height, width + stepX, height + stepY, paintOne);
                    canvas.drawRect(width, height, width + stepX, height + stepY, paintTwo);
                }
            }
        }
        return new BitmapDrawable(getResources(), bitmap);
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
        notificationManager.notify(NOTIFICATION_ID_1, notification);
    }

    @Override
    public void askBleOn() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void showSnackBar(String message) {
        Snackbar.make(main_scroll, message, Snackbar.LENGTH_LONG).show();
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
    public void onBackPressed() {
        super.onBackPressed();
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
            if (TrxUtils.createLogFile(this)) {
                TrxUtils.writeFirstColumnSettings();
                String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
                TrxUtils.appendSettingLogs(connectedCarType,
                        SdkPreferencesHelper.getInstance().getConnectedCarBase(),
                        SdkPreferencesHelper.getInstance().getTrxAddressConnectable(),
                        SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl(),
                        SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC(), SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft(),
                        SdkPreferencesHelper.getInstance().getTrxAddressFrontRight(), SdkPreferencesHelper.getInstance().getTrxAddressLeft(),
                        SdkPreferencesHelper.getInstance().getTrxAddressMiddle(), SdkPreferencesHelper.getInstance().getTrxAddressRight(),
                        SdkPreferencesHelper.getInstance().getTrxAddressTrunk(), SdkPreferencesHelper.getInstance().getTrxAddressRearLeft(),
                        SdkPreferencesHelper.getInstance().getTrxAddressBack(), SdkPreferencesHelper.getInstance().getTrxAddressRearRight(),
                        SdkPreferencesHelper.getInstance().getRssiLogNumber(), SdkPreferencesHelper.getInstance().getRollingAvElement(),
                        SdkPreferencesHelper.getInstance().getCryptoPreAuthTimeout(),
                        SdkPreferencesHelper.getInstance().getCryptoActionTimeout(),
                        SdkPreferencesHelper.getInstance().getWantedSpeed(), SdkPreferencesHelper.getInstance().getOneStepSize());
                TrxUtils.writeFirstColumnLogs();
            }
            mBleRangingHelper.initializeConnectedCar();
        }
        setActivityTitle();
    }

    private enum CarDoorStatus {
        LOCKED, DRIVER_DOOR_OPEN, UNLOCKED
    }
}
