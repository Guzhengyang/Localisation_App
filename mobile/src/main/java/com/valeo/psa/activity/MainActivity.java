package com.valeo.psa.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.valeo.psa.fragment.ChessBoardFragment;
import com.valeo.psa.fragment.DebugFragment;
import com.valeo.psa.fragment.NfcFragment;
import com.valeo.psa.fragment.RkeFragment;
import com.valeo.psa.fragment.StartFragment;
import com.valeo.psa.model.Car;
import com.valeo.psa.model.ViewModel;
import com.valeo.psa.model.ViewModelId;
import com.valeo.psa.utils.BlurBuilder;
import com.valeo.psa.utils.PreferenceUtils;
import com.valeo.psa.view.CarListAdapter;
import com.valeo.psa.view.DividerItemDecoration;
import com.valeo.psa.view.MyRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MyRecyclerAdapter.OnStartDragListener,
        MyRecyclerAdapter.OnIconLongPressedListener, View.OnTouchListener, BleRangingListener,
        RkeFragment.RkeFragmentActionListener, AccuracyFragment.AccuracyFragmentActionListener,
        StartFragment.StartFragmentActionListener {
    private static final String TAG = MainActivity.class.getName();
    private final static float SCROLL_THRESHOLD = 10;
    private static final int RESULT_SETTINGS = 20;
    private static final int REQUEST_ENABLE_BT = 25117;
    //    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    private static final int NOTIFICATION_ID_1 = 1;
    private Toolbar toolbar;
    private FrameLayout main_frame;
    private RkeFragment rkeFragment;
    private ChessBoardFragment chessboardFragment;
    private DebugFragment debugFragment;
    private AccuracyFragment accuracyFragment;
    private NfcFragment nfcFragment;
    private StartFragment startFragment;
    private NestedScrollView content_main;
    private CoordinatorLayout main_scroll;
    private AppBarLayout main_appbar;
    private TextView version_number;
    private RecyclerView control_trunk_windows_lights;
    private GestureDetectorCompat mDetector;
    private volatile View pressedView;
    private boolean isIconLongPressed = false;
    private float mDownX;
    private float mDownY;
    private ItemTouchHelper ith;
    private DottedProgressBar little_round_progressBar;
    private ImageView blur_on_touch;
    private RecyclerView car_model_recyclerView;
    private TextView selected_car_model_pinned;
    private CarListAdapter mCarListAdapter = null;
    private Car selectedCar = null;
    private int lastPos = -1;
    private TextView activity_title;
    private TextView ble_status;
    private Typeface romanTypeFace;
    private Typeface lightTypeFace;
    private BleRangingHelper mBleRangingHelper;
    private boolean showMenu = true;
    //    private KeyguardManager mKeyguardManager;
    private NotificationManagerCompat notificationManager;

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
    private RelativeLayout main_rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psa_activity_main);
        setView();
        setFragments();
        setRecyclerView();
//        setSpinner();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setFonts();
        setOnClickListeners();
        main_appbar.setExpanded(false, false);
        this.mBleRangingHelper = new BleRangingHelper(this, chessboardFragment, debugFragment,
                rkeFragment, accuracyFragment);
        final Bundle bundleArgs = new Bundle();
        bundleArgs.putBoolean("lockStatus", mBleRangingHelper.getLockStatus());
        debugFragment.setArguments(bundleArgs);
        setVersionNumber();
        notificationManager = NotificationManagerCompat.from(MainActivity.this);
//        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        if (!mKeyguardManager.isKeyguardSecure()) {
//            // Show a message that the user hasn't set up a lock screen.
//            Toast.makeText(this, getString(R.string.set_security_lock), Toast.LENGTH_LONG).show();
//        }
//        showAuthenticationScreen();
    }

    private void setFragments() {
        rkeFragment = new RkeFragment();
        chessboardFragment = new ChessBoardFragment();
        debugFragment = new DebugFragment();
        accuracyFragment = new AccuracyFragment();
        nfcFragment = new NfcFragment();
        startFragment = new StartFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.door_status_switcher, rkeFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.chessboard_rl, chessboardFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.debug_rl, debugFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.accuracy_rl, accuracyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.nfc_rl, nfcFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.start_rl, startFragment).commit();
        showHideFragment(startFragment); // hide startFragment
    }

    private void setVersionNumber() {
        try {
            PSALogs.d("version", getPackageName());
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = "Unknown";
            if (packageInfo != null) {
                versionName = String.format(getString(R.string.selected_version_number),
                        packageInfo.versionName);
            }
            version_number.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
            romanTypeFace = Typeface.createFromAsset(getAssets(), "HelveticaNeueLTStd-Ex.otf");
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
        ble_status.setTypeface(romanTypeFace, Typeface.NORMAL);
    }

    /**
     * Find all view by their id
     */
    private void setView() {
        main_rl = (RelativeLayout) findViewById(R.id.main_rl);
        main_frame = (FrameLayout) findViewById(R.id.main_frame);
        content_main = (NestedScrollView) findViewById(R.id.content_main);
        main_scroll = (CoordinatorLayout) findViewById(R.id.main_scroll);
        main_appbar = (AppBarLayout) findViewById(R.id.main_appbar);
        blur_on_touch = (ImageView) findViewById(R.id.blur_on_touch);
        ble_status = (TextView) findViewById(R.id.ble_status);
        version_number = (TextView) findViewById(R.id.version_number);
        control_trunk_windows_lights = (RecyclerView) findViewById(R.id.control_trunk_windows_lights);
        car_model_recyclerView = (RecyclerView) findViewById(R.id.car_model_recyclerView);
        selected_car_model_pinned = (TextView) findViewById(R.id.selected_car_model_pinned);
        little_round_progressBar = (DottedProgressBar) findViewById(R.id.little_round_progressBar);
    }

    public void showHideFragment(final Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        if (fragment.isHidden()) {
            ft.show(fragment);
            PSALogs.d("hidden", "Show");
        } else {
            ft.hide(fragment);
            PSALogs.d("Shown", "Hide");
        }
        ft.commit();
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
        // load "last adapter_position car" connection pref, when the app is launched
        lastPos = SdkPreferencesHelper.getInstance().getAdapterLastPosition();
        car_model_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        if (selectedCar.getBrandCar().equalsIgnoreCase(getString(R.string.ds5))) {
                            SdkPreferencesHelper.getInstance().setAreBeaconsInside(false);
                        } else if (selectedCar.getBrandCar().equalsIgnoreCase(getString(R.string.ds5_2))) {
                            SdkPreferencesHelper.getInstance().setAreBeaconsInside(true);
                        }
                    }
                    CarListAdapter.ViewHolder vh = (CarListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    if(vh != null) {
                        selected_car_model_pinned.setText(vh.getBrandCar().getText().toString());
                    }
                }
            }
        });
        if (lastPos != -1) {
            car_model_recyclerView.scrollToPosition(lastPos);
            Car tempCar = mCarListAdapter.getCars().get(lastPos);
            if (tempCar != null) {
                selected_car_model_pinned.setText(tempCar.getBrandCar());
            }
        }
    }

    /**
     * Create an list of Car
     *
     * @return a list of Car
     */
    private List<Car> createCarList() {
        List<Car> resultList = new ArrayList<>(2);
        resultList.add(new Car(R.mipmap.car_model_ds5, "1", getString(R.string.ds5), getString(R.string.VIN), "car_one"));
//        resultList.add(new Car(R.mipmap.car_model_ds5_2, "2", getString(R.string.ds5_2), getString(R.string.VIN2), "car_two"));
        resultList.add(new Car(R.mipmap.car_model_ds5_3, "3", getString(R.string.ds5_3), getString(R.string.VIN3), "car_yagi"));
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
    public void updateBLEStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBleRangingHelper.isFullyConnected()) {
                    ble_status.setText(R.string.connected_over_ble);
                } else {
                    ble_status.setText(R.string.not_connected_over_ble);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SdkPreferencesHelper.getInstance().setAdapterLastPosition(lastPos);
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
                LogFileUtils.createBufferedWriter();
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
            main_rl.setVisibility(View.GONE);
            showHideFragment(startFragment);
            mBleRangingHelper.setIsStartRequested(true);
            startFragment.startButtonActions(mBleRangingHelper);
        }
    }

    @Override
    public void startButtonActionsFinished() {
        if (startFragment != null) {
            main_rl.setVisibility(View.VISIBLE);
            mBleRangingHelper.setIsStartRequested(false);
            showHideFragment(startFragment);
        }
    }
}
