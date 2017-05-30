package com.valeo.psa.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;
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

/**
 * Created by l-avaratha on 09/03/2017
 */
public class MainFragment extends Fragment implements MyRecyclerAdapter.OnStartDragListener,
        MyRecyclerAdapter.OnIconLongPressedListener, View.OnTouchListener {
    private final static String TAG = MainFragment.class.getName();
    private final static float SCROLL_THRESHOLD = 10;
    private MainFragmentActionListener mListener;
    private FrameLayout main_frame;
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
    private ImageView blur_on_touch;
    private RecyclerView car_model_recyclerView;
    private TextView selected_car_model_pinned;
    private CarListAdapter mCarListAdapter = null;
    private Car selectedCar = null;
    private int lastPos = -1;
    private TextView ble_status;
    private Typeface romanTypeFace;
    private Typeface lightTypeFace;
    private NotificationManagerCompat notificationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        setView(rootView);
        main_appbar.setExpanded(false, false);
        try {
            romanTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Ex.otf");
            lightTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt.otf");
        } catch (Exception e) {
            PSALogs.e(TAG, "Font not loaded !");
        }
        setOnClickListeners();
        setRecyclerView();
        ble_status.setTypeface(romanTypeFace, Typeface.NORMAL);
        setVersionNumber();
        notificationManager = NotificationManagerCompat.from(getActivity());
        return rootView;
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        main_frame = (FrameLayout) rootView.findViewById(R.id.main_frame);
        main_scroll = (CoordinatorLayout) rootView.findViewById(R.id.main_scroll);
        main_appbar = (AppBarLayout) rootView.findViewById(R.id.main_appbar);
        blur_on_touch = (ImageView) rootView.findViewById(R.id.blur_on_touch);
        ble_status = (TextView) rootView.findViewById(R.id.ble_status);
        version_number = (TextView) rootView.findViewById(R.id.version_number);
        control_trunk_windows_lights = (RecyclerView) rootView.findViewById(R.id.control_trunk_windows_lights);
        car_model_recyclerView = (RecyclerView) rootView.findViewById(R.id.car_model_recyclerView);
        selected_car_model_pinned = (TextView) rootView.findViewById(R.id.selected_car_model_pinned);
    }

    private void setVersionNumber() {
        try {
            PSALogs.d("version", getActivity().getPackageName());
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
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

    /**
     * Set OnClickListeners
     */
    private void setOnClickListeners() {
        mDetector = new GestureDetectorCompat(getActivity(), new GestureDetector.SimpleOnGestureListener() {
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


    /**
     * Set the MainActivity's Recycler View
     */
    private void setRecyclerView() {
        control_trunk_windows_lights.setHasFixedSize(true);
        control_trunk_windows_lights.setAdapter(new MyRecyclerAdapter(createActionControlList(),
                R.layout.psa_control_row, lightTypeFace, this, this));
        control_trunk_windows_lights.setLayoutManager(new LinearLayoutManager(getActivity()));
        control_trunk_windows_lights.setItemAnimator(new DefaultItemAnimator());
        control_trunk_windows_lights.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider_line));
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
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        car_model_recyclerView.setLayoutManager(llm);
        CarListAdapter.OnCarSelectionListener mCarSelectionListener = new CarListAdapter.OnCarSelectionListener() {
            @Override
            public void onCarSelection(int position) {
                selectedCar = mCarListAdapter.getCars().get(position);
                mCarListAdapter.setSelectedCarRegistrationPlate(selectedCar.getRegPlate());
                PreferenceUtils.loadSharedPreferencesFromInputStream(getActivity(),
                        getResources().openRawResource(
                                getResources().getIdentifier(selectedCar.getCarConfigFileName(),
                                        "raw", getActivity().getPackageName())),
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
                        if (PreferenceUtils.loadSharedPreferencesFromInputStream(getActivity(),
                                getResources().openRawResource(
                                        getResources().getIdentifier(
                                                selectedCar.getCarConfigFileName(),
                                                "raw", getActivity().getPackageName())),
                                SdkPreferencesHelper.SAVED_CC_CONNECTION_OPTION)) {
                            mListener.restartConnection(true);
                            lastPos = position;
                        } else {
                            Snackbar.make(recyclerView, "pref file not found", Snackbar.LENGTH_SHORT).show();
                        }
                        if (selectedCar.getBrandCar().equalsIgnoreCase(getString(R.string.ds5))) {
                            SdkPreferencesHelper.getInstance().setAreBeaconsInside(false);
                        } else if (selectedCar.getBrandCar().equalsIgnoreCase(getString(R.string.ds5_2))) {
                            SdkPreferencesHelper.getInstance().setAreBeaconsInside(true);
                        } else if (selectedCar.getBrandCar().equalsIgnoreCase(getString(R.string.ds5_3))) {
                            SdkPreferencesHelper.getInstance().setAreBeaconsInside(true);
                        }
                    }
                    CarListAdapter.ViewHolder vh = (CarListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    if (vh != null) {
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
        List<Car> resultList = new ArrayList<>(4);
        resultList.add(new Car(R.mipmap.car_model_ds5, "1", getString(R.string.ds5), getString(R.string.VIN), "car_one"));
        resultList.add(new Car(R.mipmap.car_model_ds5_2, "2", getString(R.string.ds5_2), getString(R.string.VIN2), "car_two"));
        resultList.add(new Car(R.mipmap.car_model_ds5_3, "3", getString(R.string.ds5_3), getString(R.string.VIN3), "car_yagi"));
        resultList.add(new Car(R.mipmap.car_model_ds5_2, "4", getString(R.string.ds5_4), getString(R.string.VIN4), "car_three"));
        return resultList;
    }

    /**
     * Create an control action list of ViewModel
     *
     * @return a list of ViewModel
     */
    private List<ViewModel> createActionControlList() {
        List<ViewModel> resultList = new ArrayList<>(3);
        resultList.add(new ViewModel(ContextCompat.getDrawable(getActivity(), R.mipmap.open_trunk_button), getString(R.string.unlock_trunk), ViewModelId.UNLOCK_TRUNK));
        resultList.add(new ViewModel(ContextCompat.getDrawable(getActivity(), R.mipmap.close_windows_button), getString(R.string.close_all_windows), ViewModelId.CLOSE_ALL_WINDOWS));
        resultList.add(new ViewModel(ContextCompat.getDrawable(getActivity(), R.mipmap.flash_lights_button), getString(R.string.flash_lights), ViewModelId.FLASH_LIGHTS));
        return resultList;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        ith.startDrag(viewHolder);
    }

    public void blurActivity(boolean blurActivity) {
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
                    mListener.switchToolbars(true, R.id.unlock_trunk_toolbar);
                    break;
                case LOCK_TRUNK:
                    PSALogs.d("Longpress", "LOCK_TRUNK detected ");
                    mListener.switchToolbars(true, R.id.lock_trunk_toolbar);
                    break;
                case CLOSE_ALL_WINDOWS:
                    PSALogs.d("Longpress", "CLOSE_ALL_WINDOWS detected ");
                    mListener.switchToolbars(true, R.id.close_all_windows_toolbar);
                    mListener.startProgress();
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
                                mListener.switchToolbars(false, R.id.unlock_trunk_toolbar);
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).
                                                getItems().set(position,
                                                new ViewModel(ContextCompat.getDrawable(getActivity(), R.mipmap.close_trunk_button),
                                                        getString(R.string.lock_trunk),
                                                        ViewModelId.LOCK_TRUNK));
                                        control_trunk_windows_lights.getAdapter().notifyDataSetChanged();
                                    }
                                }, 100);
                                break;
                            case LOCK_TRUNK:
                                PSALogs.d("Longpress", "ACTION_UP LOCK_TRUNK");
                                mListener.switchToolbars(false, R.id.lock_trunk_toolbar);
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyRecyclerAdapter) control_trunk_windows_lights.getAdapter()).
                                                getItems().set(position,
                                                new ViewModel(ContextCompat.getDrawable(getActivity(), R.mipmap.open_trunk_button),
                                                        getString(R.string.unlock_trunk),
                                                        ViewModelId.UNLOCK_TRUNK));
                                        control_trunk_windows_lights.getAdapter().notifyDataSetChanged();
                                    }
                                }, 100);
                                break;
                            case CLOSE_ALL_WINDOWS:
                                PSALogs.d("Longpress", "ACTION_UP CLOSE_ALL_WINDOWS");
                                mListener.switchToolbars(false, R.id.close_all_windows_toolbar);
                                mListener.stopProgress();
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

    public void setSnackBarMessage(String message) {
        Snackbar.make(main_scroll, message, Snackbar.LENGTH_LONG).show();
    }

    public void setAdapterLastPosition() {
        SdkPreferencesHelper.getInstance().setAdapterLastPosition(lastPos);
    }

    public void setBleStatus(int textId) {
        ble_status.setText(textId);
    }

    private void createNotification(int notifId, String message, int largeIcon, String secondSubText) {
        Intent actionIntent = new Intent(getActivity(), MainFragment.class);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(getActivity(), 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        String carBrand = selectedCar == null ? mCarListAdapter.getCars()
                .get(0).getBrandCar() : selectedCar.getBrandCar();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getActivity())
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

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        if (mContext instanceof Activity) {
            mListener = (MainFragmentActionListener) mContext;
        }
    }

    public interface MainFragmentActionListener {
        void switchToolbars(boolean mainToNewToolBar, int resId);

        void restartConnection(boolean createCar);

        void startProgress();

        void stopProgress();
    }
}
