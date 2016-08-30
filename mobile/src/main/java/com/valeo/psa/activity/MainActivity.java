package com.valeo.psa.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.psa.R;
import com.valeo.psa.model.Car;
import com.valeo.psa.model.ViewModel;
import com.valeo.psa.model.ViewModelId;
import com.valeo.psa.utils.BlurBuilder;
import com.valeo.psa.view.CarListAdapter;
import com.valeo.psa.view.DividerItemDecoration;
import com.valeo.psa.view.MyRecyclerAdapter;
import com.valeo.psa.view.ReverseProgressBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyRecyclerAdapter.OnStartDragListener,
        MyRecyclerAdapter.OnIconLongPressedListener, View.OnTouchListener, BleRangingListener {
    private static final String TAG = MainActivity.class.getName();
    private final static int FIVE_MINUTES_IN_MILLI = 300000;
    private final static int MINUTE_IN_MILLI = 60000;
    private final static int SECOND_IN_MILLI = 1000;
    private final static float SCROLL_THRESHOLD = 10;
    private static final int RESULT_SETTINGS = 20;
    private Toolbar toolbar;
    private NestedScrollView content_main;
    private NestedScrollView main_scroll;
    private RelativeLayout main_scroll_relativeLayout;
    private ImageView blur_on_touch;
    private RelativeLayout content_start_car_dialog;
    private ReverseProgressBar start_car_timeout;
    private TextView car_start_countdown_min_sec;
    private TextView activity_title;
    private TextView ble_status;
    private TextView car_door_status;
    private TextView tips;
    private TextView nfc_disclaimer;
    private RecyclerView control_trunk_windows_lights;
    private RecyclerView car_model_recyclerView;
    private ImageButton vehicle_locked;
    private ImageButton driver_s_door_unlocked;
    private ImageButton vehicle_unlocked;
    private ImageButton start_button;
    private ImageView signalReceived;
    private LayerDrawable layerDrawable;
    private GradientDrawable welcome_area;
    private GradientDrawable start_area;
    private GradientDrawable lock_area;
    private GradientDrawable unlock_area_left;
    private GradientDrawable unlock_area_right;
    private GradientDrawable unlock_area_back;
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
    private CarDoorStatus carDoorStatus = CarDoorStatus.LOCKED;
    private BleRangingHelper mBleRangingHelper;

    /**
     * Get the status bar height
     *
     * @param res the app resources
     * @return the height of the status bar
     */
    private static int statusBarHeight(Resources res) {
        return (int) (R.dimen.status_bar_height * res.getDisplayMetrics().density);
    }

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
        SdkPreferencesHelper.initializeInstance(this);
        this.mBleRangingHelper = new BleRangingHelper(this, this);
        //TODO setbackground over the true value of carDoorStatus
        switch (carDoorStatus) {
            case LOCKED:
                vehicle_locked.setBackgroundResource(R.mipmap.slider_button);
                break;
            case DRIVER_DOOR_OPEN:
                driver_s_door_unlocked.setBackgroundResource(R.mipmap.slider_button);
                break;
            case UNLOCKED:
                vehicle_unlocked.setBackgroundResource(R.mipmap.slider_button);
                break;
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
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SETTINGS:
                Log.d("settings", "ok");
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
        content_main.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.d("test content_main", "isNestedScrollingEnabled " + v.isNestedScrollingEnabled()
                        + " isSmoothScrollingEnabled " + v.isSmoothScrollingEnabled()
                        + " hasNestedScrollingParent " + v.hasNestedScrollingParent());
            }
        });
        main_scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.d("test main_scroll", "isNestedScrollingEnabled " + v.isNestedScrollingEnabled()
                        + " isSmoothScrollingEnabled " + v.isSmoothScrollingEnabled()
                        + " hasNestedScrollingParent " + v.hasNestedScrollingParent());
                Log.d("test content_main", "isNestedScrollingEnabled " + content_main.isNestedScrollingEnabled()
                        + " isSmoothScrollingEnabled " + content_main.isSmoothScrollingEnabled()
                        + " hasNestedScrollingParent " + content_main.hasNestedScrollingParent());
                if (scrollY < 488) {
                    //TODO annule main_scroll event and scroll only content_main
                } else {
                    Log.d("onScroll main_scroll", String.format("%1$d %2$d %3$d %4$d", scrollX, scrollY, oldScrollX, oldScrollY));
                }
            }
        });
        vehicle_locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carDoorStatus == CarDoorStatus.DRIVER_DOOR_OPEN) {
                    car_door_status.setText(getString(R.string.vehicle_locked));
                    carDoorStatus = CarDoorStatus.LOCKED;
                    vehicle_locked.setBackgroundResource(R.mipmap.slider_button);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    mBleRangingHelper.performLockVehicleRequest(true); //lockVehicle
                }
            }
        });
        driver_s_door_unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carDoorStatus == CarDoorStatus.LOCKED || carDoorStatus == CarDoorStatus.UNLOCKED) {
                    car_door_status.setText(getString(R.string.driver_s_door_unlocked));
                    carDoorStatus = CarDoorStatus.DRIVER_DOOR_OPEN;
                    driver_s_door_unlocked.setBackgroundResource(R.mipmap.slider_button);
                    vehicle_locked.setBackgroundResource(0);
                    vehicle_unlocked.setBackgroundResource(0);
                    start_button.setBackgroundResource(0);
                    mBleRangingHelper.performLockVehicleRequest(false); //unlockVehicle
                }
            }
        });
        vehicle_unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carDoorStatus == CarDoorStatus.DRIVER_DOOR_OPEN) {
                    car_door_status.setText(getString(R.string.vehicle_unlocked));
                    carDoorStatus = CarDoorStatus.UNLOCKED;
                    vehicle_unlocked.setBackgroundResource(R.mipmap.slider_button);
                    driver_s_door_unlocked.setBackgroundResource(0);
                    start_button.setBackgroundResource(R.mipmap.start_button_waves);
                    mBleRangingHelper.performLockVehicleRequest(false); //unlockVehicle
                }
            }
        });
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
            Log.e(TAG, "Font not loaded !");
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

    /**
     * Find all view by their id
     */
    private void setView() {
        content_main = (NestedScrollView) findViewById(R.id.content_main);
        main_scroll = (NestedScrollView) findViewById(R.id.main_scroll);
        main_scroll_relativeLayout = (RelativeLayout) findViewById(R.id.main_scroll_relativeLayout);
        blur_on_touch = (ImageView) findViewById(R.id.blur_on_touch);
        content_start_car_dialog = (RelativeLayout) findViewById(R.id.content_start_car_dialog);
        start_car_timeout = (ReverseProgressBar) findViewById(R.id.start_car_timeout);
        car_start_countdown_min_sec = (TextView) findViewById(R.id.car_start_countdown_min_sec);
        ble_status = (TextView) findViewById(R.id.ble_status);
        car_door_status = (TextView) findViewById(R.id.car_door_status);
        tips = (TextView) findViewById(R.id.tips);
        nfc_disclaimer = (TextView) findViewById(R.id.nfc_disclaimer);
        control_trunk_windows_lights = (RecyclerView) findViewById(R.id.control_trunk_windows_lights);
        car_model_recyclerView = (RecyclerView) findViewById(R.id.car_model_recyclerView);
        vehicle_locked = (ImageButton) findViewById(R.id.vehicle_locked);
        driver_s_door_unlocked = (ImageButton) findViewById(R.id.driver_s_door_unlocked);
        vehicle_unlocked = (ImageButton) findViewById(R.id.vehicle_unlocked);
        start_button = (ImageButton) findViewById(R.id.start_button);
        signalReceived = (ImageView) findViewById(R.id.signalReceived);
        layerDrawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.rssi_localization);
        welcome_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.welcome_area);
        start_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.start_area);
        lock_area = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.lock_area);
        unlock_area_left = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_left);
        unlock_area_right = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_right);
        unlock_area_back = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.unlock_area_back);
        debug_info = (TextView) findViewById(R.id.debug_info);
    }

    /**
     * Set the MainActivity's Recycler View
     */
    private void setRecyclerView() {
        control_trunk_windows_lights.setHasFixedSize(true);
        control_trunk_windows_lights.setAdapter(new MyRecyclerAdapter(MainActivity.this, new WeakReference<>(control_trunk_windows_lights), createActionControlList(), R.layout.psa_control_row, lightTypeFace, this, this));
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
                //TODO
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
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        car_model_recyclerView.setLayoutManager(llm);
        CarListAdapter.OnCarSelectionListener mCarSelectionListener = new CarListAdapter.OnCarSelectionListener() {
            @Override
            public void onCarSelection(View carSelected, int position) {
                Car selectedCar = mCarListAdapter.getCars().get(position);
                mCarListAdapter.setSelectedCarRegistrationPlate(selectedCar.getRegPlate());
            }
        };
        mCarListAdapter = new CarListAdapter(mCarSelectionListener);
        car_model_recyclerView.setAdapter(mCarListAdapter);
        mCarListAdapter.setCars(createCarList());
    }

    /**
     * Create an list of Car
     *
     * @return a list of Car
     */
    private List<Car> createCarList() {
        List<Car> resultList = new ArrayList<>(3);
        resultList.add(new Car(R.mipmap.car_model_208, "1", getString(R.string.peugeot_208), getString(R.string.VIN)));
        resultList.add(new Car(R.drawable.car_logo, "2", getString(R.string.peugeot_209), getString(R.string.VIN2)));
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
    public void updateStartCarTimeoutBar(int timeout) {
        start_car_timeout.setProgress(timeout);
    }

    /**
     * Set the current remaining progress time in minutes and seconds
     *
     * @param progressMin the progress remaining minutes
     * @param progressSec the progress remaining seconds
     */
    public void updateStartCarTimeout(int progressMin, int progressSec) {
        car_start_countdown_min_sec.setText(String.format(
                getString(R.string.car_start_countdown_min_sec),
                progressMin, progressSec));
    }

    /**
     * Switch between toolbar's (normal and start car mode)
     *
     * @param mainToStart boolean to determine which toolbar to inflate
     */
    public void switchToolbarStartCar(boolean mainToStart) {
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
            activity_title.setText(R.string.title_activity_main);
            activity_title.setGravity(Gravity.CENTER);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) activity_title.getLayoutParams();
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            activity_title.setLayoutParams(layoutParams);
            activity_title.setOnClickListener(null);
        }
    }

    /**
     * Switch between toolbar's (normal and new toolbar mode)
     *
     * @param mainToNewToolBar boolean to determine which toolbar to inflate
     * @param resId            the resource id of the new toolbar to show or hide
     */
    public void switchToolbars(boolean mainToNewToolBar, int resId) {
        if (mainToNewToolBar) {
            toolbar.findViewById(R.id.main_toolbar).setVisibility(View.GONE);
            ble_status.setVisibility(View.GONE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = layoutParams.height + statusBarHeight(getResources());
            toolbar.setLayoutParams(layoutParams);
            toolbar.findViewById(resId).setVisibility(View.VISIBLE);
            blurActivity(true);
        } else {
            toolbar.findViewById(resId).setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = layoutParams.height - statusBarHeight(getResources());
            toolbar.setLayoutParams(layoutParams);
            toolbar.findViewById(R.id.main_toolbar).setVisibility(View.VISIBLE);
            ble_status.setVisibility(View.VISIBLE);
            blurActivity(false);
        }
    }

    private void blurActivity(boolean blurActivity) {
        if (blurActivity) {
            Bitmap blurredBitmap = BlurBuilder.blur(main_scroll);
            blur_on_touch.setImageDrawable(new BitmapDrawable(getResources(), blurredBitmap));
            blur_on_touch.setVisibility(View.VISIBLE);
            main_scroll.setVisibility(View.GONE);
        } else {
            blur_on_touch.setImageDrawable(null);
            main_scroll.setVisibility(View.VISIBLE);
            blur_on_touch.setVisibility(View.GONE);
        }
    }

    @Override
    public void onIconLongPressed(int position) {
        ViewModel viewModel = ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).getItems().get(position);
        if (viewModel != null) {
            switch (viewModel.getViewModelId()) {
                case UNLOCK_TRUNK:
                    Log.d("Longpress", "UNLOCK_TRUNK detected ");
                    switchToolbars(true, R.id.unlock_trunk_toolbar);
                    break;
                case LOCK_TRUNK:
                    Log.d("Longpress", "LOCK_TRUNK detected ");
                    switchToolbars(true, R.id.lock_trunk_toolbar);
                    break;
                case CLOSE_ALL_WINDOWS:
                    Log.d("Longpress", "CLOSE_ALL_WINDOWS detected ");
                    switchToolbars(true, R.id.close_all_windows_toolbar);
                    break;
                case FLASH_LIGHTS:
                    Log.d("Longpress", "FLASH_LIGHTS detected ");
                    break;
                case UNKNOWN:
                    Log.d("Longpress", "UNKNOWN detected ");
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
                    Log.d("icon onTouchEvent", "ACTION_DOWN");
                    mDetector.onTouchEvent(event);
                    pressedView = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isIconLongPressed && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                    Log.d("icon onTouchEvent", "ACTION_MOVE");
                } else {
                    break;
                }
            case MotionEvent.ACTION_CANCEL:
                Log.d("icon onTouchEvent", "ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                if (isIconLongPressed) {
                    final View childView = control_trunk_windows_lights.findContainingItemView(pressedView);
                    final int position = control_trunk_windows_lights.getChildAdapterPosition(childView);
                    ViewModel viewModel = ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).getItems().get(position);
                    if (viewModel != null) {
                        switch (viewModel.getViewModelId()) {
                            case UNLOCK_TRUNK:
                                Log.d("icon onTouchEvent", "ACTION_UP UNLOCK_TRUNK");
                                switchToolbars(false, R.id.unlock_trunk_toolbar);
                                new Handler(getMainLooper()).postDelayed(new Runnable() {
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
                                Log.d("icon onTouchEvent", "ACTION_UP LOCK_TRUNK");
                                switchToolbars(false, R.id.lock_trunk_toolbar);
                                new Handler(getMainLooper()).postDelayed(new Runnable() {
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
                                Log.d("icon onTouchEvent", "ACTION_UP CLOSE_ALL_WINDOWS");
                                switchToolbars(false, R.id.close_all_windows_toolbar);
                                break;
                            case FLASH_LIGHTS:
                                Log.d("icon onTouchEvent", "ACTION_UP FLASH_LIGHTS");
                                break;
                            case UNKNOWN:
                                Log.d("icon onTouchEvent", "ACTION_UP UNKNOWN");
                                break;
                        }
                    }
                } else {
                    Log.d("icon onTouchEvent", "ACTION_UP isIconLongPressed = false");
                }
                isIconLongPressed = false;
                mDownX = 0;
                mDownY = 0;
                pressedView = null;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                Log.d("icon onTouchEvent", "ACTION_OUTSIDE");
                break;
            case MotionEvent.ACTION_SCROLL:
                Log.d("icon onTouchEvent", "ACTION_SCROLL");
                break;
        }
        return true;
    }

    @Override
    public void lightUpArea(int area) {
        switch (area) {
            case BleRangingHelper.WELCOME_AREA:
                welcome_area.setColor(Color.WHITE);
                break;
            case BleRangingHelper.LOCK_AREA:
                lock_area.setColor(Color.RED);
                break;
            case BleRangingHelper.UNLOCK_LEFT_AREA:
                unlock_area_left.setColor(Color.GREEN);
                break;
            case BleRangingHelper.UNLOCK_RIGHT_AREA:
                unlock_area_right.setColor(Color.GREEN);
                break;
            case BleRangingHelper.UNLOCK_BACK_AREA:
                unlock_area_back.setColor(Color.GREEN);
                break;
            case BleRangingHelper.START_AREA:
                start_area.setColor(Color.CYAN);
                break;
        }
        signalReceived.setImageDrawable(layerDrawable);
    }

    @Override
    public void darkenArea(int area) {
        switch (area) {
            case BleRangingHelper.WELCOME_AREA:
                welcome_area.setColor(Color.BLACK);
                break;
            case BleRangingHelper.LOCK_AREA:
                lock_area.setColor(Color.BLACK);
                break;
            case BleRangingHelper.UNLOCK_LEFT_AREA:
                unlock_area_left.setColor(Color.BLACK);
                break;
            case BleRangingHelper.UNLOCK_RIGHT_AREA:
                unlock_area_right.setColor(Color.BLACK);
                break;
            case BleRangingHelper.UNLOCK_BACK_AREA:
                unlock_area_back.setColor(Color.BLACK);
                break;
            case BleRangingHelper.START_AREA:
                start_area.setColor(Color.BLACK);
                break;
        }
        signalReceived.setImageDrawable(layerDrawable);
    }

    @Override
    public void printDebugInfo(SpannableStringBuilder spannableStringBuilder) {
        if (mBleRangingHelper.isFullyConnected()) {
            ble_status.setText(R.string.connected_over_ble);
        } else {
            ble_status.setText(R.string.not_connected_over_ble);
        }
        debug_info.setText(spannableStringBuilder);
    }

    @Override
    public void onBackPressed() {
        mBleRangingHelper.closeApp();
        super.onBackPressed();
    }

    private enum CarDoorStatus {
        LOCKED, DRIVER_DOOR_OPEN, UNLOCKED
    }
}
