package com.valeo.psa.activity;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private final static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (stringValue.isEmpty()) {
                return false;
            }
            preference.setSummary(stringValue);
            return true;
        }
    };
    private static SharedPreferences sharedPreferences;
    private static PreferenceManager manager;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference, String defaultValue) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                sharedPreferences.getString(preference.getKey(), defaultValue));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (PSALogs.DEBUG) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || SpecificPreferenceViewPagerFragment.class.getName().equals(fragmentName)
                || PSASettingsFragment.class.getName().equals(fragmentName);
    }

    public static class PSASettingsFragment extends PreferenceFragment {
        private ListPreference connected_car_type;
        private ListPreference connected_car_base;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            manager = getPreferenceManager();
            manager.setSharedPreferencesName(SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION);
            sharedPreferences = manager.getSharedPreferences();
            addPreferencesFromResource(R.xml.pref_psa);
            setViews();
            bindSummaries();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDefaultValues();
        }

        private void setViews() {
            connected_car_type = ((ListPreference) findPreference(SdkPreferencesHelper.CONNECTED_CAR_TYPE_PREFERENCES_NAME));
            connected_car_base = ((ListPreference) findPreference(SdkPreferencesHelper.CONNECTED_CAR_BASE_PREFERENCES_NAME));
        }

        private void setDefaultValues() {

        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        private void bindSummaries() {
            bindPreferenceSummaryToValue(connected_car_type, ConnectedCarFactory.TYPE_4_A);
            bindPreferenceSummaryToValue(connected_car_base, ConnectedCarFactory.BASE_3);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SpecificPreferenceViewPagerFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View result = inflater.inflate(R.layout.pager, container, false);
            ViewPager pager = (ViewPager) result.findViewById(R.id.pager);
            pager.setAdapter(buildAdapter());
            switch (SdkPreferencesHelper.getInstance().getConnectedCarType()) {
                case ConnectedCarFactory.TYPE_4_A:
                    pager.setCurrentItem(0);
                    break;
                case ConnectedCarFactory.TYPE_5_A:
                    pager.setCurrentItem(1);
                    break;
                case ConnectedCarFactory.TYPE_7_A:
                    pager.setCurrentItem(2);
                    break;
                case ConnectedCarFactory.TYPE_8_A:
                    pager.setCurrentItem(3);
                    break;
                default:
                    pager.setCurrentItem(0);
                    break;
            }
            return (result);
        }

        private PagerAdapter buildAdapter() {
            return (new SampleAdapter(getActivity(), getChildFragmentManager()));
        }
    }

    public static class SampleAdapter extends FragmentPagerAdapter {
        Context ctxt = null;

        public SampleAdapter(Context ctxt, FragmentManager mgr) {
            super(mgr);
            this.ctxt = ctxt;
        }

        @Override
        public int getCount() {
            return (4);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SpecificPreferenceFragment.newInstance(ConnectedCarFactory.TYPE_4_A);
                case 1:
                    return SpecificPreferenceFragment.newInstance(ConnectedCarFactory.TYPE_5_A);
                case 2:
                    return SpecificPreferenceFragment.newInstance(ConnectedCarFactory.TYPE_7_A);
                case 3:
                    return SpecificPreferenceFragment.newInstance(ConnectedCarFactory.TYPE_8_A);
                default:
                    return SpecificPreferenceFragment.newInstance(SdkPreferencesHelper.getInstance().getConnectedCarType());
            }
        }

        @Override
        public String getPageTitle(int position) {
            String currentConfigType = "Current Config: ";
            switch (position) {
                case 0:
                    if (SdkPreferencesHelper.getInstance().getConnectedCarType().equals(ConnectedCarFactory.TYPE_4_A)) {
                        return SpecificPreferenceFragment.getTitle(currentConfigType + "4 beacons");
                    } else {
                        return SpecificPreferenceFragment.getTitle("4 beacons");
                    }
                case 1:
                    if (SdkPreferencesHelper.getInstance().getConnectedCarType().equals(ConnectedCarFactory.TYPE_5_A)) {
                        return SpecificPreferenceFragment.getTitle(currentConfigType + "5 beacons");
                    } else {
                        return SpecificPreferenceFragment.getTitle("5 beacons");
                    }
                case 2:
                    if (SdkPreferencesHelper.getInstance().getConnectedCarType().equals(ConnectedCarFactory.TYPE_7_A)) {
                        return SpecificPreferenceFragment.getTitle(currentConfigType + "7 beacons");
                    } else {
                        return SpecificPreferenceFragment.getTitle("7 beacons");
                    }
                case 3:
                    if (SdkPreferencesHelper.getInstance().getConnectedCarType().equals(ConnectedCarFactory.TYPE_8_A)) {
                        return SpecificPreferenceFragment.getTitle(currentConfigType + "8 beacons");
                    } else {
                        return SpecificPreferenceFragment.getTitle("8 beacons");
                    }
                default:
                    return SpecificPreferenceFragment.getTitle(SdkPreferencesHelper.getInstance().getConnectedCarType());
            }
        }
    }

    /**
     * This fragment shows specific preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SpecificPreferenceFragment extends PreferenceFragment {
        private static final String FILE_NAME = "filename";

        private EditTextPreference ratio_max_min_thr;
        private EditTextPreference ratio_close_to_car_thr;
        //        private EditTextPreference near_door_thr;
//        private EditTextPreference equally_near_door_thr;
//        private EditTextPreference nearer_door_thr;
        private EditTextPreference offset_ear_start;
        private EditTextPreference offset_ear_lock;
        private EditTextPreference offset_ear_unlock;
        private EditTextPreference offset_pocket_start;
        private EditTextPreference offset_pocket_lock;
        private EditTextPreference offset_pocket_unlock;
        private EditTextPreference start_thr;
        private EditTextPreference unlock_thr;
        private EditTextPreference lock_thr;
        private EditTextPreference welcome_thr;
        private EditTextPreference near_door_ratio;
        private EditTextPreference near_back_door_thr_min;
        private EditTextPreference near_back_door_thr_max;
        private EditTextPreference near_door_thr_mb;
        private EditTextPreference near_door_thr_ml_mr_max;
        private EditTextPreference near_door_thr_ml_mr_min;
        private EditTextPreference near_door_thr_tl_tr_max;
        private EditTextPreference near_door_thr_tl_tr_min;
        private EditTextPreference near_door_thr_mrl_mrr;
        private EditTextPreference near_door_thr_trl_trr;
        private EditTextPreference average_delta_unlock_thr;
        private EditTextPreference average_delta_lock_thr;
        private EditTextPreference unlock_valid_nb;
        private EditTextPreference unlock_mode;
        private EditTextPreference lock_mode;
        private EditTextPreference start_mode;
        private EditTextPreference ecretage_100;
        private EditTextPreference ecretage_70;
        private EditTextPreference ecretage_50;
        private EditTextPreference ecretage_30;
        private EditTextPreference equalizer_left;
        private EditTextPreference equalizer_middle;
        private EditTextPreference equalizer_right;
        private EditTextPreference equalizer_trunk;
        private EditTextPreference equalizer_back;
        private EditTextPreference equalizer_front_left;
        private EditTextPreference equalizer_rear_left;
        private EditTextPreference equalizer_front_right;
        private EditTextPreference equalizer_rear_right;

        static SpecificPreferenceFragment newInstance(String preferenceFilename) {
            SpecificPreferenceFragment frag = new SpecificPreferenceFragment();
            Bundle args = new Bundle();
            args.putString(FILE_NAME, preferenceFilename);
            frag.setArguments(args);
            return (frag);
        }

        static String getTitle(String title) {
            return title;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            manager = getPreferenceManager();
            manager.setSharedPreferencesName(getArguments().getString(FILE_NAME, SdkPreferencesHelper.getInstance().getConnectedCarType()));
            sharedPreferences = manager.getSharedPreferences();
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);
            setViews();
            bindSummaries();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDefaultValues();
        }

        private void setViews() {
            ratio_max_min_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.RATIO_MAX_MIN_THR_PREFERENCES_NAME));
            ratio_close_to_car_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.RATIO_CLOSE_TO_CAR_THR_PREFERENCES_NAME));
//            near_door_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_THR_PREFERENCES_NAME));
//            equally_near_door_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALLY_NEAR_DOOR_THR_PREFERENCES_NAME));
//            nearer_door_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEARER_DOOR_THR_PREFERENCES_NAME));
            offset_ear_start = ((EditTextPreference) findPreference(SdkPreferencesHelper.OFFSET_EAR_FOR_START_PREFERENCES_NAME));
            offset_ear_lock = ((EditTextPreference) findPreference(SdkPreferencesHelper.OFFSET_EAR_FOR_LOCK_PREFERENCES_NAME));
            offset_ear_unlock = ((EditTextPreference) findPreference(SdkPreferencesHelper.OFFSET_EAR_FOR_UNLOCK_PREFERENCES_NAME));
            offset_pocket_start = ((EditTextPreference) findPreference(SdkPreferencesHelper.OFFSET_POCKET_FOR_START_PREFERENCES_NAME));
            offset_pocket_lock = ((EditTextPreference) findPreference(SdkPreferencesHelper.OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME));
            offset_pocket_unlock = ((EditTextPreference) findPreference(SdkPreferencesHelper.OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME));
            start_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.START_THR_PREFERENCES_NAME));
            unlock_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.UNLOCK_THR_PREFERENCES_NAME));
            lock_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.LOCK_THR_PREFERENCES_NAME));
            welcome_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.WELCOME_THR_PREFERENCES_NAME));
            near_door_ratio = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_PREFERENCES_NAME));
            near_back_door_thr_min = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME));
            near_back_door_thr_max = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME));
            near_door_thr_mb = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_MB_PREFERENCES_NAME));
            near_door_thr_ml_mr_max = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME));
            near_door_thr_ml_mr_min = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME));
            near_door_thr_tl_tr_max = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_TL_TR_MAX_PREFERENCES_NAME));
            near_door_thr_tl_tr_min = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_TL_TR_MIN_PREFERENCES_NAME));
            near_door_thr_mrl_mrr = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_MRL_MRR_PREFERENCES_NAME));
            near_door_thr_trl_trr = ((EditTextPreference) findPreference(SdkPreferencesHelper.NEAR_DOOR_RATIO_THR_TRL_TRR_PREFERENCES_NAME));
            average_delta_unlock_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME));
            average_delta_lock_thr = ((EditTextPreference) findPreference(SdkPreferencesHelper.AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME));
            unlock_valid_nb = ((EditTextPreference) findPreference(SdkPreferencesHelper.UNLOCK_VALID_NB_PREFERENCES_NAME));
            unlock_mode = ((EditTextPreference) findPreference(SdkPreferencesHelper.UNLOCK_MODE_PREFERENCES_NAME));
            lock_mode = ((EditTextPreference) findPreference(SdkPreferencesHelper.LOCK_MODE_PREFERENCES_NAME));
            start_mode = ((EditTextPreference) findPreference(SdkPreferencesHelper.START_MODE_PREFERENCES_NAME));
            ecretage_100 = ((EditTextPreference) findPreference(SdkPreferencesHelper.ECRETAGE_70_100_PREFERENCES_NAME));
            ecretage_70 = ((EditTextPreference) findPreference(SdkPreferencesHelper.ECRETAGE_50_70_PREFERENCES_NAME));
            ecretage_50 = ((EditTextPreference) findPreference(SdkPreferencesHelper.ECRETAGE_30_50_PREFERENCES_NAME));
            ecretage_30 = ((EditTextPreference) findPreference(SdkPreferencesHelper.ECRETAGE_30_30_PREFERENCES_NAME));
            equalizer_left = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_LEFT_PREFERENCES_NAME));
            equalizer_middle = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_MIDDLE_PREFERENCES_NAME));
            equalizer_right = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_RIGHT_PREFERENCES_NAME));
            equalizer_trunk = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_TRUNK_PREFERENCES_NAME));
            equalizer_back = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_BACK_PREFERENCES_NAME));
            equalizer_front_left = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_FRONT_LEFT_PREFERENCES_NAME));
            equalizer_rear_left = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_REAR_LEFT_PREFERENCES_NAME));
            equalizer_front_right = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME));
            equalizer_rear_right = ((EditTextPreference) findPreference(SdkPreferencesHelper.EQUALIZER_REAR_RIGHT_PREFERENCES_NAME));
        }

        private void setDefaultValues() {
            ratio_max_min_thr.setText(ratio_max_min_thr.getSummary().toString());
            ratio_close_to_car_thr.setText(ratio_close_to_car_thr.getSummary().toString());
//            near_door_thr.setText(near_door_thr.getSummary().toString());
//            equally_near_door_thr.setText(equally_near_door_thr.getSummary().toString());
//            nearer_door_thr.setText(nearer_door_thr.getSummary().toString());
            offset_ear_start.setText(offset_ear_start.getSummary().toString());
            offset_ear_lock.setText(offset_ear_lock.getSummary().toString());
            offset_ear_unlock.setText(offset_ear_unlock.getSummary().toString());
            offset_pocket_start.setText(offset_pocket_start.getSummary().toString());
            offset_pocket_lock.setText(offset_pocket_lock.getSummary().toString());
            offset_pocket_unlock.setText(offset_pocket_unlock.getSummary().toString());
            start_thr.setText(start_thr.getSummary().toString());
            unlock_thr.setText(unlock_thr.getSummary().toString());
            lock_thr.setText(lock_thr.getSummary().toString());
            welcome_thr.setText(welcome_thr.getSummary().toString());
            near_door_ratio.setText(near_door_ratio.getSummary().toString());
            near_back_door_thr_min.setText(near_back_door_thr_min.getSummary().toString());
            near_back_door_thr_max.setText(near_back_door_thr_max.getSummary().toString());
            near_door_thr_mb.setText(near_door_thr_mb.getSummary().toString());
            near_door_thr_ml_mr_max.setText(near_door_thr_ml_mr_max.getSummary().toString());
            near_door_thr_ml_mr_min.setText(near_door_thr_ml_mr_min.getSummary().toString());
            near_door_thr_tl_tr_max.setText(near_door_thr_tl_tr_max.getSummary().toString());
            near_door_thr_tl_tr_min.setText(near_door_thr_tl_tr_min.getSummary().toString());
            near_door_thr_mrl_mrr.setText(near_door_thr_mrl_mrr.getSummary().toString());
            near_door_thr_trl_trr.setText(near_door_thr_trl_trr.getSummary().toString());
            average_delta_unlock_thr.setText(average_delta_unlock_thr.getSummary().toString());
            average_delta_lock_thr.setText(average_delta_lock_thr.getSummary().toString());
            unlock_valid_nb.setText(unlock_valid_nb.getSummary().toString());
            unlock_mode.setText(unlock_mode.getSummary().toString());
            lock_mode.setText(lock_mode.getSummary().toString());
            start_mode.setText(start_mode.getSummary().toString());
            ecretage_100.setText(ecretage_100.getSummary().toString());
            ecretage_70.setText(ecretage_70.getSummary().toString());
            ecretage_50.setText(ecretage_50.getSummary().toString());
            ecretage_30.setText(ecretage_30.getSummary().toString());
            equalizer_left.setText(equalizer_left.getSummary().toString());
            equalizer_middle.setText(equalizer_middle.getSummary().toString());
            equalizer_right.setText(equalizer_right.getSummary().toString());
            equalizer_trunk.setText(equalizer_trunk.getSummary().toString());
            equalizer_back.setText(equalizer_back.getSummary().toString());
            equalizer_front_left.setText(equalizer_front_left.getSummary().toString());
            equalizer_rear_left.setText(equalizer_rear_left.getSummary().toString());
            equalizer_front_right.setText(equalizer_front_right.getSummary().toString());
            equalizer_rear_right.setText(equalizer_rear_right.getSummary().toString());
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        private void bindSummaries() {
            bindPreferenceSummaryToValue(ratio_max_min_thr, String.valueOf(SdkPreferencesHelper.RATIO_MAX_MIN_THR));
            bindPreferenceSummaryToValue(ratio_close_to_car_thr, String.valueOf(SdkPreferencesHelper.RATIO_CLOSE_TO_CAR_THR));
//            bindPreferenceSummaryToValue(near_door_thr, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_THR));
//            bindPreferenceSummaryToValue(equally_near_door_thr, String.valueOf(SdkPreferencesHelper.EQUALLY_NEAR_DOOR_THR));
//            bindPreferenceSummaryToValue(nearer_door_thr, String.valueOf(SdkPreferencesHelper.NEARER_DOOR_THR));
            bindPreferenceSummaryToValue(offset_ear_start, String.valueOf(SdkPreferencesHelper.OFFSET_EAR_FOR_START));
            bindPreferenceSummaryToValue(offset_ear_lock, String.valueOf(SdkPreferencesHelper.OFFSET_EAR_FOR_LOCK));
            bindPreferenceSummaryToValue(offset_ear_unlock, String.valueOf(SdkPreferencesHelper.OFFSET_EAR_FOR_UNLOCK));
            bindPreferenceSummaryToValue(offset_pocket_start, String.valueOf(SdkPreferencesHelper.OFFSET_POCKET_FOR_START));
            bindPreferenceSummaryToValue(offset_pocket_lock, String.valueOf(SdkPreferencesHelper.OFFSET_POCKET_FOR_LOCK));
            bindPreferenceSummaryToValue(offset_pocket_unlock, String.valueOf(SdkPreferencesHelper.OFFSET_POCKET_FOR_UNLOCK));
            bindPreferenceSummaryToValue(start_thr, String.valueOf(SdkPreferencesHelper.START_THRESHOLD));
            bindPreferenceSummaryToValue(unlock_thr, String.valueOf(SdkPreferencesHelper.UNLOCK_IN_THE_RUN_THRESHOLD));
            bindPreferenceSummaryToValue(lock_thr, String.valueOf(SdkPreferencesHelper.WALK_AWAY_LOCKING_THRESHOLD));
            bindPreferenceSummaryToValue(welcome_thr, String.valueOf(SdkPreferencesHelper.WELCOME_THRESHOLD));
            bindPreferenceSummaryToValue(near_door_ratio, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD));
            bindPreferenceSummaryToValue(near_back_door_thr_min, String.valueOf(SdkPreferencesHelper.NEAR_BACKDOOR_RATIO_THRESHOLD_MIN));
            bindPreferenceSummaryToValue(near_back_door_thr_max, String.valueOf(SdkPreferencesHelper.NEAR_BACKDOOR_RATIO_THRESHOLD_MAX));
            bindPreferenceSummaryToValue(near_door_thr_mb, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_MB));
            bindPreferenceSummaryToValue(near_door_thr_ml_mr_max, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX));
            bindPreferenceSummaryToValue(near_door_thr_ml_mr_min, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN));
            bindPreferenceSummaryToValue(near_door_thr_tl_tr_max, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX));
            bindPreferenceSummaryToValue(near_door_thr_tl_tr_min, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN));
            bindPreferenceSummaryToValue(near_door_thr_mrl_mrr, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR));
            bindPreferenceSummaryToValue(near_door_thr_trl_trr, String.valueOf(SdkPreferencesHelper.NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR));
            bindPreferenceSummaryToValue(average_delta_unlock_thr, String.valueOf(SdkPreferencesHelper.AVERAGE_DELTA_UNLOCK_THRESHOLD));
            bindPreferenceSummaryToValue(average_delta_lock_thr, String.valueOf(SdkPreferencesHelper.AVERAGE_DELTA_LOCK_THRESHOLD));
            bindPreferenceSummaryToValue(unlock_valid_nb, String.valueOf(SdkPreferencesHelper.UNLOCK_VALID_NB));
            bindPreferenceSummaryToValue(unlock_mode, String.valueOf(SdkPreferencesHelper.UNLOCK_MODE));
            bindPreferenceSummaryToValue(lock_mode, String.valueOf(SdkPreferencesHelper.LOCK_MODE));
            bindPreferenceSummaryToValue(start_mode, String.valueOf(SdkPreferencesHelper.START_MODE));
            bindPreferenceSummaryToValue(ecretage_100, String.valueOf(SdkPreferencesHelper.ECRETAGE_70_100));
            bindPreferenceSummaryToValue(ecretage_70, String.valueOf(SdkPreferencesHelper.ECRETAGE_50_70));
            bindPreferenceSummaryToValue(ecretage_50, String.valueOf(SdkPreferencesHelper.ECRETAGE_30_50));
            bindPreferenceSummaryToValue(ecretage_30, String.valueOf(SdkPreferencesHelper.ECRETAGE_30_30));
            bindPreferenceSummaryToValue(equalizer_left, String.valueOf(SdkPreferencesHelper.EQUALIZER_LEFT));
            bindPreferenceSummaryToValue(equalizer_middle, String.valueOf(SdkPreferencesHelper.EQUALIZER_MIDDLE));
            bindPreferenceSummaryToValue(equalizer_right, String.valueOf(SdkPreferencesHelper.EQUALIZER_RIGHT));
            bindPreferenceSummaryToValue(equalizer_trunk, String.valueOf(SdkPreferencesHelper.EQUALIZER_TRUNK));
            bindPreferenceSummaryToValue(equalizer_back, String.valueOf(SdkPreferencesHelper.EQUALIZER_BACK));
            bindPreferenceSummaryToValue(equalizer_front_left, String.valueOf(SdkPreferencesHelper.EQUALIZER_FRONT_LEFT));
            bindPreferenceSummaryToValue(equalizer_rear_left, String.valueOf(SdkPreferencesHelper.EQUALIZER_REAR_LEFT));
            bindPreferenceSummaryToValue(equalizer_front_right, String.valueOf(SdkPreferencesHelper.EQUALIZER_FRONT_RIGHT));
            bindPreferenceSummaryToValue(equalizer_rear_right, String.valueOf(SdkPreferencesHelper.EQUALIZER_REAR_RIGHT));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private ListPreference connected_car_type;
        private ListPreference connected_car_base;
        private CheckBoxPreference connected_car_trame_enabled;
        private EditTextPreference connected_car_trame;
        private EditTextPreference thatcham_timeout;
        private EditTextPreference crypto_pre_auth_timeout;
        private EditTextPreference crypto_action_timeout;
        private EditTextPreference rssi_log_number;
        private EditTextPreference rolling_av_element;
        private EditTextPreference start_nb_element;
        private EditTextPreference lock_nb_element;
        private EditTextPreference unlock_nb_element;
        private EditTextPreference welcome_nb_element;
        private EditTextPreference long_nb_element;
        private EditTextPreference short_nb_element;
        private EditTextPreference lin_acc_size;
        private EditTextPreference correction_lin_acc;
        private EditTextPreference frozen_threshold;
        private EditTextPreference address_connectable;
        private EditTextPreference address_connectable_2;
        private EditTextPreference address_front_left;
        private EditTextPreference address_front_right;
        private EditTextPreference address_left;
        private EditTextPreference address_middle;
        private EditTextPreference address_right;
        private EditTextPreference address_trunk;
        private EditTextPreference address_rear_left;
        private EditTextPreference address_back;
        private EditTextPreference address_rear_right;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            manager = getPreferenceManager();
            manager.setSharedPreferencesName(SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION);
            sharedPreferences = manager.getSharedPreferences();
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            setViews();
            bindSummaries();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDefaultValues();
        }

        private void setViews() {
            connected_car_type = ((ListPreference) findPreference(SdkPreferencesHelper.CONNECTED_CAR_TYPE_PREFERENCES_NAME));
            connected_car_base = ((ListPreference) findPreference(SdkPreferencesHelper.CONNECTED_CAR_BASE_PREFERENCES_NAME));
            connected_car_trame_enabled = ((CheckBoxPreference) findPreference(SdkPreferencesHelper.CONNECTED_CAR_TRAME_ENABLED_PREFERENCES_NAME));
            connected_car_trame = ((EditTextPreference) findPreference(SdkPreferencesHelper.CONNECTED_CAR_TRAME_PREFERENCES_NAME));
            thatcham_timeout = ((EditTextPreference) findPreference(SdkPreferencesHelper.THATCHAM_TIMEOUT_PREFERENCES_NAME));
            crypto_pre_auth_timeout = ((EditTextPreference) findPreference(SdkPreferencesHelper.CRYPTO_PRE_AUTH_TIMEOUT_PREFERENCES_NAME));
            crypto_action_timeout = ((EditTextPreference) findPreference(SdkPreferencesHelper.CRYPTO_ACTION_TIMEOUT_PREFERENCES_NAME));
            rssi_log_number = ((EditTextPreference) findPreference(SdkPreferencesHelper.RSSI_LOG_NUMBER_PREFERENCES_NAME));
            rolling_av_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.ROLLING_AV_ELEMENT_PREFERENCES_NAME));
            start_nb_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.START_NB_ELEMENT_PREFERENCES_NAME));
            lock_nb_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.LOCK_NB_ELEMENT_PREFERENCES_NAME));
            unlock_nb_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.UNLOCK_NB_ELEMENT_PREFERENCES_NAME));
            welcome_nb_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.WELCOME_NB_ELEMENT_PREFERENCES_NAME));
            long_nb_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.LONG_NB_ELEMENT_PREFERENCES_NAME));
            short_nb_element = ((EditTextPreference) findPreference(SdkPreferencesHelper.SHORT_NB_ELEMENT_PREFERENCES_NAME));
            lin_acc_size = ((EditTextPreference) findPreference(SdkPreferencesHelper.LIN_ACC_SIZE_PREFERENCES_NAME));
            correction_lin_acc = ((EditTextPreference) findPreference(SdkPreferencesHelper.CORRECTION_LIN_ACC_PREFERENCES_NAME));
            frozen_threshold = ((EditTextPreference) findPreference(SdkPreferencesHelper.FROZEN_THRESHOLD_PREFERENCES_NAME));
            address_connectable = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_CONNECTABLE_PREFERENCE_NAME));
            address_connectable_2 = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_CONNECTABLE_2_PREFERENCE_NAME));
            address_front_left = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_FRONT_LEFT_PREFERENCE_NAME));
            address_front_right = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_FRONT_RIGHT_PREFERENCE_NAME));
            address_left = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_LEFT_PREFERENCE_NAME));
            address_middle = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_MIDDLE_PREFERENCE_NAME));
            address_right = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_RIGHT_PREFERENCE_NAME));
            address_trunk = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_TRUNK_PREFERENCE_NAME));
            address_rear_left = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_REAR_LEFT_PREFERENCE_NAME));
            address_back = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_BACK_PREFERENCE_NAME));
            address_rear_right = ((EditTextPreference) findPreference(SdkPreferencesHelper.ADDRESS_REAR_RIGHT_PREFERENCE_NAME));
        }

        private void setDefaultValues() {
            thatcham_timeout.setText(thatcham_timeout.getSummary().toString());
            crypto_pre_auth_timeout.setText(crypto_pre_auth_timeout.getSummary().toString());
            crypto_action_timeout.setText(crypto_action_timeout.getSummary().toString());
            rssi_log_number.setText(rssi_log_number.getSummary().toString());
            rolling_av_element.setText(rolling_av_element.getSummary().toString());
            start_nb_element.setText(start_nb_element.getSummary().toString());
            lock_nb_element.setText(lock_nb_element.getSummary().toString());
            unlock_nb_element.setText(unlock_nb_element.getSummary().toString());
            welcome_nb_element.setText(welcome_nb_element.getSummary().toString());
            long_nb_element.setText(long_nb_element.getSummary().toString());
            short_nb_element.setText(short_nb_element.getSummary().toString());
            lin_acc_size.setText(lin_acc_size.getSummary().toString());
            correction_lin_acc.setText(correction_lin_acc.getSummary().toString());
            frozen_threshold.setText(frozen_threshold.getSummary().toString());
            address_connectable.setText(address_connectable.getSummary().toString());
            address_connectable_2.setText(address_connectable_2.getSummary().toString());
            address_front_left.setText(address_front_left.getSummary().toString());
            address_front_right.setText(address_front_right.getSummary().toString());
            address_left.setText(address_left.getSummary().toString());
            address_middle.setText(address_middle.getSummary().toString());
            address_right.setText(address_right.getSummary().toString());
            address_trunk.setText(address_trunk.getSummary().toString());
            address_rear_left.setText(address_rear_left.getSummary().toString());
            address_back.setText(address_back.getSummary().toString());
            address_rear_right.setText(address_rear_right.getSummary().toString());
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        private void bindSummaries() {
            bindPreferenceSummaryToValue(connected_car_type, ConnectedCarFactory.TYPE_4_A);
            bindPreferenceSummaryToValue(connected_car_base, ConnectedCarFactory.BASE_3);
            connected_car_trame_enabled.setSummary(R.string.pref_car_forced_trame_enabled_summary);
            bindPreferenceSummaryToValue(connected_car_trame, "");
            bindPreferenceSummaryToValue(thatcham_timeout, String.valueOf(SdkPreferencesHelper.THATCHAM_TIMEOUT));
            bindPreferenceSummaryToValue(crypto_pre_auth_timeout, String.valueOf(SdkPreferencesHelper.CRYPTO_PRE_AUTH_TIMEOUT));
            bindPreferenceSummaryToValue(crypto_action_timeout, String.valueOf(SdkPreferencesHelper.CRYPTO_ACTION_TIMEOUT));
            bindPreferenceSummaryToValue(rssi_log_number, String.valueOf(SdkPreferencesHelper.RSSI_LOG_NUMBER));
            bindPreferenceSummaryToValue(rolling_av_element, String.valueOf(SdkPreferencesHelper.ROLLING_AVERAGE_ELEMENTS));
            bindPreferenceSummaryToValue(start_nb_element, String.valueOf(SdkPreferencesHelper.START_NB_ELEMENT));
            bindPreferenceSummaryToValue(lock_nb_element, String.valueOf(SdkPreferencesHelper.LOCK_NB_ELEMENT));
            bindPreferenceSummaryToValue(unlock_nb_element, String.valueOf(SdkPreferencesHelper.UNLOCK_NB_ELEMENT));
            bindPreferenceSummaryToValue(welcome_nb_element, String.valueOf(SdkPreferencesHelper.WELCOME_NB_ELEMENT));
            bindPreferenceSummaryToValue(long_nb_element, String.valueOf(SdkPreferencesHelper.LONG_NB_ELEMENT));
            bindPreferenceSummaryToValue(short_nb_element, String.valueOf(SdkPreferencesHelper.SHORT_NB_ELEMENT));
            bindPreferenceSummaryToValue(lin_acc_size, String.valueOf(SdkPreferencesHelper.LIN_ACC_SIZE));
            bindPreferenceSummaryToValue(correction_lin_acc, String.valueOf(SdkPreferencesHelper.CORRECTION_LIN_ACC));
            bindPreferenceSummaryToValue(frozen_threshold, String.valueOf(SdkPreferencesHelper.FROZEN_THRESHOLD));
            bindPreferenceSummaryToValue(address_connectable, SdkPreferencesHelper.BLE_ADDRESS_CONNECTABLE);
            bindPreferenceSummaryToValue(address_connectable_2, SdkPreferencesHelper.BLE_ADDRESS_CONNECTABLE_2);
            bindPreferenceSummaryToValue(address_front_left, SdkPreferencesHelper.BLE_ADDRESS_FRONT_LEFT);
            bindPreferenceSummaryToValue(address_front_right, SdkPreferencesHelper.BLE_ADDRESS_FRONT_RIGHT);
            bindPreferenceSummaryToValue(address_left, SdkPreferencesHelper.BLE_ADDRESS_LEFT);
            bindPreferenceSummaryToValue(address_middle, SdkPreferencesHelper.BLE_ADDRESS_MIDDLE);
            bindPreferenceSummaryToValue(address_right, SdkPreferencesHelper.BLE_ADDRESS_RIGHT);
            bindPreferenceSummaryToValue(address_trunk, SdkPreferencesHelper.BLE_ADDRESS_TRUNK);
            bindPreferenceSummaryToValue(address_rear_left, SdkPreferencesHelper.BLE_ADDRESS_REAR_LEFT);
            bindPreferenceSummaryToValue(address_back, SdkPreferencesHelper.BLE_ADDRESS_BACK);
            bindPreferenceSummaryToValue(address_rear_right, SdkPreferencesHelper.BLE_ADDRESS_REAR_RIGHT);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
