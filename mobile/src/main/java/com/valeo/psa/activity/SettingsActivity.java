package com.valeo.psa.activity;


import android.annotation.TargetApi;
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
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;
import com.valeo.psa.utils.PreferenceUtils;
import com.valeo.psa.view.SafeEditTextPreferenceView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.valeo.bleranging.model.connectedcar.ConnectedCar.THATCHAM_ORIENTED;
import static com.valeo.bleranging.utils.LogFileUtils.CONFIG_DIR;

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
                || ConnectionPreferenceFragment.class.getName().equals(fragmentName)
                || PSASettingsFragment.class.getName().equals(fragmentName);
    }

    public static class PSASettingsFragment extends PreferenceFragment {
        private ListPreference connected_car_type;
        private ListPreference connected_car_base;
        private CheckBoxPreference user_speed_enabled;
        private EditTextPreference wanted_speed;
        private EditTextPreference one_step_size;

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
            connected_car_type = ((ListPreference) findPreference(getString(R.string.connected_car_type_pref_name)));
            connected_car_base = ((ListPreference) findPreference(getString(R.string.connected_car_base_pref_name)));
            user_speed_enabled = ((CheckBoxPreference) findPreference(getString(R.string.user_speed_enabled_pref_name)));
            wanted_speed = ((EditTextPreference) findPreference(getString(R.string.wanted_speed_pref_name)));
            one_step_size = ((EditTextPreference) findPreference(getString(R.string.one_step_size_pref_name)));
        }

        private void setDefaultValues() {
            wanted_speed.setText(wanted_speed.getSummary().toString());
            one_step_size.setText(one_step_size.getSummary().toString());
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        private void bindSummaries() {
            bindPreferenceSummaryToValue(connected_car_type, ConnectedCarFactory.TYPE_4_B);
            bindPreferenceSummaryToValue(connected_car_base, ConnectedCarFactory.BASE_3);
            user_speed_enabled.setSummary(R.string.pref_user_speed_enabled_summary);
            bindPreferenceSummaryToValue(wanted_speed, String.valueOf(SdkPreferencesHelper.WANTED_SPEED));
            bindPreferenceSummaryToValue(one_step_size, String.valueOf(SdkPreferencesHelper.ONE_STEP_SIZE));
        }
    }

    /**
     * This fragment shows connection preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ConnectionPreferenceFragment extends PreferenceFragment {
        private static final int PICK_IMPORT_GENERAL_FILE_RESULT_CODE = 92141;
        private final TextWatcher textWatcher = new TextWatcher() {
            private boolean allowSemiColonAdd = false;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start,
                                      int before, int count) {
                allowSemiColonAdd = count == 1;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 17) {
                    editable.delete(17, editable.length());
                    return;
                }
                if (!allowSemiColonAdd) { // if user deletes element, delete semicolon automatically
                    for (int i = 1; i < 6; i++) {
                        if (editable.length() == (3 * i) - 1) {
                            editable.delete(editable.length() - 1, editable.length());
                            return;
                        }
                    }
                } else {
                    if (editable.length() < 15) {
                        for (int i = 1; i < 6; i++) {
                            if (editable.length() == (3 * i) - 1) {
                                editable.append(":");
                                return;
                            }
                        }
                    }
                }
            }
        };
        private SafeEditTextPreferenceView address_connectable;
        private SafeEditTextPreferenceView address_connectable_pc;
        private SafeEditTextPreferenceView address_connectable_remote_control;
        private SafeEditTextPreferenceView address_front_left;
        private SafeEditTextPreferenceView address_front_right;
        private SafeEditTextPreferenceView address_left;
        private SafeEditTextPreferenceView address_middle;
        private SafeEditTextPreferenceView address_right;
        private SafeEditTextPreferenceView address_trunk;
        private SafeEditTextPreferenceView address_rear_left;
        private SafeEditTextPreferenceView address_back;
        private SafeEditTextPreferenceView address_rear_right;
        private Preference export_preferences;
        private Preference import_preferences;
        private String sharedPreferencesName;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            manager = getPreferenceManager();
            sharedPreferencesName = SdkPreferencesHelper.SAVED_CC_CONNECTION_OPTION;
            manager.setSharedPreferencesName(sharedPreferencesName);
            sharedPreferences = manager.getSharedPreferences();
            addPreferencesFromResource(R.xml.pref_connection);
            setHasOptionsMenu(true);
            setViews();
            bindSummaries();
            addTextWatchers();
            setOnClickListeners(getActivity());
        }

        private void setOnClickListeners(final Context mContext) {
            export_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String filePath = CONFIG_DIR + SdkPreferencesHelper.SAVED_CC_CONNECTION_OPTION;
                    File exportedPrefs = new File(mContext.getExternalCacheDir(), filePath);
                    if (!exportedPrefs.exists()) {
                        try {
                            if (!exportedPrefs.createNewFile()) {
                                Toast.makeText(getActivity(), "Cannot create file.", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                    Toast.makeText(getActivity(), "Preference exported to " + filePath, Toast.LENGTH_SHORT).show();
                    return PreferenceUtils.saveSharedPreferencesToFile(getActivity(), exportedPrefs, sharedPreferencesName);
                }
            });

            import_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    selectFile(PICK_IMPORT_GENERAL_FILE_RESULT_CODE);
                    return false;
                }
            });
        }

        private void selectFile(int code) {
            String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer.equalsIgnoreCase("SAMSUNG")) {
                Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                intent.putExtra("CONTENT_TYPE", "*/*");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, code);
            } else {
                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("file/*");
                startActivityForResult(intent, code);
            }
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICK_IMPORT_GENERAL_FILE_RESULT_CODE:
                    if (resultCode == RESULT_OK) {
                        String filePath = data.getData().getPath();
                        File file = new File(filePath);
                        try {
                            PreferenceUtils.loadSharedPreferencesFromInputStream(getActivity(),
                                    new FileInputStream(file), sharedPreferencesName);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getActivity(), "Preference imported from " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDefaultValues();
        }

        private void setViews() {
            address_connectable = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_connectable_pref_name)));
            address_connectable_pc = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_connectable_pc_pref_name)));
            address_connectable_remote_control = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_connectable_remote_control_pref_name)));
            address_front_left = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_front_left_pref_name)));
            address_front_right = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_front_right_pref_name)));
            address_left = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_left_pref_name)));
            address_middle = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_middle_pref_name)));
            address_right = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_right_pref_name)));
            address_trunk = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_trunk_pref_name)));
            address_rear_left = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_rear_left_pref_name)));
            address_back = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_back_pref_name)));
            address_rear_right = ((SafeEditTextPreferenceView) findPreference(getString(R.string.address_rear_right_pref_name)));
            export_preferences = findPreference(getString(R.string.export_pref_name));
            import_preferences = findPreference(getString(R.string.import_pref_name));
        }

        private void setDefaultValues() {
            address_connectable.setText(address_connectable.getSummary().toString());
            if (address_connectable_pc != null && address_connectable_pc.getSummary() != null) {
                address_connectable_pc.setText(address_connectable_pc.getSummary().toString());
            }
            address_connectable_remote_control.setText(address_connectable_remote_control.getSummary().toString());
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
            bindPreferenceSummaryToValue(address_connectable, SdkPreferencesHelper.BLE_ADDRESS_CONNECTABLE);
            bindPreferenceSummaryToValue(address_connectable_pc, SdkPreferencesHelper.BLE_ADDRESS_CONNECTABLE_PC);
            bindPreferenceSummaryToValue(address_connectable_remote_control, SdkPreferencesHelper.BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL);
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

        private void addTextWatchers() {
            address_connectable.getEditText().addTextChangedListener(textWatcher);
            address_connectable_pc.getEditText().addTextChangedListener(textWatcher);
            address_connectable_remote_control.getEditText().addTextChangedListener(textWatcher);
            address_front_left.getEditText().addTextChangedListener(textWatcher);
            address_front_right.getEditText().addTextChangedListener(textWatcher);
            address_left.getEditText().addTextChangedListener(textWatcher);
            address_middle.getEditText().addTextChangedListener(textWatcher);
            address_right.getEditText().addTextChangedListener(textWatcher);
            address_trunk.getEditText().addTextChangedListener(textWatcher);
            address_rear_left.getEditText().addTextChangedListener(textWatcher);
            address_back.getEditText().addTextChangedListener(textWatcher);
            address_rear_right.getEditText().addTextChangedListener(textWatcher);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private static final int PICK_IMPORT_GENERAL_FILE_RESULT_CODE = 92164;
        private ListPreference connected_car_type;
        private ListPreference connected_car_base;
        private ListPreference opening_orientation_type;
        private CheckBoxPreference com_simulation_enabled;
        private CheckBoxPreference is_channel_limited;
        private CheckBoxPreference is_calibrated;
        private CheckBoxPreference security_wal_enabled;
        private CheckBoxPreference are_beacons_inside;
        private CheckBoxPreference print_inside_enabled;
        private CheckBoxPreference print_rooftop_enabled;
        private CheckBoxPreference is_mini_prediction_used;
        private EditTextPreference offset_smartphone;
        private EditTextPreference offset_hysteresis_lock;
        private EditTextPreference offset_hysteresis_unlock;
        private EditTextPreference threshold_prob_standard;
        private EditTextPreference threshold_dist_away_standard;
        private EditTextPreference threshold_lock;
        private EditTextPreference thatcham_timeout;
        private EditTextPreference unlock_timeout;
        private EditTextPreference crypto_pre_auth_timeout;
        private EditTextPreference crypto_action_timeout;
        private EditTextPreference rssi_log_number;
        private EditTextPreference lin_acc_size;
        private EditTextPreference frozen_threshold;
        private CheckBoxPreference user_speed_enabled;
        private EditTextPreference wanted_speed;
        private EditTextPreference one_step_size;
        private EditTextPreference flash_frequency;
        private EditTextPreference measurement_interval;
        private EditTextPreference active_scanning_period;
        private EditTextPreference inactive_scanning_period;
        private Preference export_preferences;
        private Preference import_preferences;
        private String sharedPreferencesName;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            manager = getPreferenceManager();
            sharedPreferencesName = SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION;
            manager.setSharedPreferencesName(sharedPreferencesName);
            sharedPreferences = manager.getSharedPreferences();
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            setViews();
            bindSummaries();
            setOnClickListeners(getActivity());
        }

        private void setOnClickListeners(final Context mContext) {
            export_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String filePath = CONFIG_DIR + SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION;
                    File exportedPrefs = new File(mContext.getExternalCacheDir(), filePath);
                    if (!exportedPrefs.exists()) {
                        try {
                            if (!exportedPrefs.createNewFile()) {
                                Toast.makeText(getActivity(), "Cannot create file.", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                    Toast.makeText(getActivity(), "Preference exported to " + filePath, Toast.LENGTH_SHORT).show();
                    return PreferenceUtils.saveSharedPreferencesToFile(getActivity(), exportedPrefs, sharedPreferencesName);
                }
            });

            import_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    selectFile(PICK_IMPORT_GENERAL_FILE_RESULT_CODE);
                    return false;
                }
            });
        }

        private void selectFile(int code) {
            String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer.equalsIgnoreCase("SAMSUNG")) {
                Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                intent.putExtra("CONTENT_TYPE", "*/*");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, code);
            } else {
                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("file/*");
                startActivityForResult(intent, code);
            }
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICK_IMPORT_GENERAL_FILE_RESULT_CODE:
                    if (resultCode == RESULT_OK) {
                        String filePath = data.getData().getPath();
                        File file = new File(filePath);
                        try {
                            PreferenceUtils.loadSharedPreferencesFromInputStream(getActivity(),
                                    new FileInputStream(file), sharedPreferencesName);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getActivity(), "Preference imported from " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDefaultValues();
        }

        private void setViews() {
            connected_car_type = ((ListPreference) findPreference(getString(R.string.connected_car_type_pref_name)));
            connected_car_base = ((ListPreference) findPreference(getString(R.string.connected_car_base_pref_name)));
            opening_orientation_type = ((ListPreference) findPreference(getString(R.string.opening_orientation_type_pref_name)));
            com_simulation_enabled = ((CheckBoxPreference) findPreference(getString(R.string.com_simulation_enabled_pref_name)));
            is_channel_limited = ((CheckBoxPreference) findPreference(getString(R.string.is_channel_limited_pref_name)));
            is_calibrated = ((CheckBoxPreference) findPreference(getString(R.string.is_calibrated_pref_name)));
            security_wal_enabled = ((CheckBoxPreference) findPreference(getString(R.string.security_wal_enabled_pref_name)));
            are_beacons_inside = ((CheckBoxPreference) findPreference(getString(R.string.are_beacons_inside_pref_name)));
            offset_smartphone = ((EditTextPreference) findPreference(getString(R.string.offset_smartphone_pref_name)));
            threshold_lock = ((EditTextPreference) findPreference(getString(R.string.threshold_lock_pref_name)));
            offset_hysteresis_lock = ((EditTextPreference) findPreference(getString(R.string.offset_hysteresis_lock_pref_name)));
            offset_hysteresis_unlock = ((EditTextPreference) findPreference(getString(R.string.offset_hysteresis_unlock_pref_name)));
            threshold_prob_standard = ((EditTextPreference) findPreference(getString(R.string.threshold_prob_standard_pref_name)));
            threshold_dist_away_standard = ((EditTextPreference) findPreference(getString(R.string.threshold_dist_away_standard_pref_name)));
//            back_timeout = ((EditTextPreference) findPreference(getString(R.string.back_timeout_pref_name)));
            thatcham_timeout = ((EditTextPreference) findPreference(getString(R.string.thatcham_timeout_pref_name)));
            unlock_timeout = ((EditTextPreference) findPreference(getString(R.string.unlock_timeout_pref_name)));
            crypto_pre_auth_timeout = ((EditTextPreference) findPreference(getString(R.string.crypto_pre_auth_timeout_pref_name)));
            crypto_action_timeout = ((EditTextPreference) findPreference(getString(R.string.crypto_action_timeout_pref_name)));
            rssi_log_number = ((EditTextPreference) findPreference(getString(R.string.rssi_log_number_pref_name)));
            print_inside_enabled = ((CheckBoxPreference) findPreference(getString(R.string.print_inside_enabled_pref_name)));
            print_rooftop_enabled = ((CheckBoxPreference) findPreference(getString(R.string.print_rooftop_enabled_pref_name)));
            is_mini_prediction_used = ((CheckBoxPreference) findPreference(getString(R.string.is_mini_prediction_used_pref_name)));
            lin_acc_size = ((EditTextPreference) findPreference(getString(R.string.lin_acc_size_pref_name)));
            frozen_threshold = ((EditTextPreference) findPreference(getString(R.string.frozen_threshold_pref_name)));
            user_speed_enabled = ((CheckBoxPreference) findPreference(getString(R.string.user_speed_enabled_pref_name)));
            wanted_speed = ((EditTextPreference) findPreference(getString(R.string.wanted_speed_pref_name)));
            one_step_size = ((EditTextPreference) findPreference(getString(R.string.one_step_size_pref_name)));
            flash_frequency = ((EditTextPreference) findPreference(getString(R.string.flash_frequency_pref_name)));
            measurement_interval = ((EditTextPreference) findPreference(getString(R.string.measurement_interval_pref_name)));
            active_scanning_period = ((EditTextPreference) findPreference(getString(R.string.active_scanning_period_pref_name)));
            inactive_scanning_period = ((EditTextPreference) findPreference(getString(R.string.inactive_scanning_period_pref_name)));
            export_preferences = findPreference(getString(R.string.export_pref_name));
            import_preferences = findPreference(getString(R.string.import_pref_name));
        }

        private void setDefaultValues() {
            com_simulation_enabled.setChecked(SdkPreferencesHelper.getInstance().getComSimulationEnabled());
            is_channel_limited.setChecked(SdkPreferencesHelper.getInstance().isChannelLimited());
            is_calibrated.setChecked(SdkPreferencesHelper.getInstance().isCalibrated());
            security_wal_enabled.setChecked(SdkPreferencesHelper.getInstance().getSecurityWALEnabled());
            are_beacons_inside.setChecked(SdkPreferencesHelper.getInstance().getAreBeaconsInside());
            print_inside_enabled.setChecked(SdkPreferencesHelper.getInstance().isPrintInsideEnabled());
            print_rooftop_enabled.setChecked(SdkPreferencesHelper.getInstance().isPrintRooftopEnabled());
            is_mini_prediction_used.setChecked(SdkPreferencesHelper.getInstance().isMiniPredictionUsed());
            user_speed_enabled.setChecked(SdkPreferencesHelper.getInstance().getUserSpeedEnabled());
            offset_smartphone.setText(offset_smartphone.getSummary().toString());
            threshold_lock.setText(threshold_lock.getSummary().toString());
            offset_hysteresis_lock.setText(offset_hysteresis_lock.getSummary().toString());
            offset_hysteresis_unlock.setText(offset_hysteresis_unlock.getSummary().toString());
            threshold_prob_standard.setText(threshold_prob_standard.getSummary().toString());
            threshold_dist_away_standard.setText(threshold_dist_away_standard.getSummary().toString());
//            back_timeout.setText(back_timeout.getSummary().toString());
            thatcham_timeout.setText(thatcham_timeout.getSummary().toString());
            unlock_timeout.setText(unlock_timeout.getSummary().toString());
            crypto_pre_auth_timeout.setText(crypto_pre_auth_timeout.getSummary().toString());
            crypto_action_timeout.setText(crypto_action_timeout.getSummary().toString());
            rssi_log_number.setText(rssi_log_number.getSummary().toString());
            lin_acc_size.setText(lin_acc_size.getSummary().toString());
            frozen_threshold.setText(frozen_threshold.getSummary().toString());
            wanted_speed.setText(wanted_speed.getSummary().toString());
            one_step_size.setText(one_step_size.getSummary().toString());
            flash_frequency.setText(flash_frequency.getSummary().toString());
            measurement_interval.setText(measurement_interval.getSummary().toString());
            active_scanning_period.setText(active_scanning_period.getSummary().toString());
            inactive_scanning_period.setText(inactive_scanning_period.getSummary().toString());
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        private void bindSummaries() {
            bindPreferenceSummaryToValue(connected_car_type, ConnectedCarFactory.TYPE_8_A);
            bindPreferenceSummaryToValue(connected_car_base, ConnectedCarFactory.BASE_3);
            bindPreferenceSummaryToValue(opening_orientation_type, THATCHAM_ORIENTED);
            com_simulation_enabled.setSummary(R.string.pref_com_simulation_enabled_summary);
            is_channel_limited.setSummary(R.string.pref_is_channel_limited_summary);
            is_calibrated.setSummary(R.string.pref_is_calibrated_summary);
            security_wal_enabled.setSummary(R.string.pref_security_wal_enabled_summary);
            are_beacons_inside.setSummary(R.string.pref_are_beacons_inside_summary);
            bindPreferenceSummaryToValue(offset_smartphone, String.valueOf(SdkPreferencesHelper.OFFSET_SMARTPHONE));
            bindPreferenceSummaryToValue(threshold_lock, String.valueOf(SdkPreferencesHelper.THRESHOLD_LOCK));
            bindPreferenceSummaryToValue(offset_hysteresis_lock, String.valueOf(SdkPreferencesHelper.OFFSET_HYSTERESIS_LOCK));
            bindPreferenceSummaryToValue(offset_hysteresis_unlock, String.valueOf(SdkPreferencesHelper.OFFSET_HYSTERESIS_UNLOCK));
            bindPreferenceSummaryToValue(threshold_prob_standard, String.valueOf(SdkPreferencesHelper.THRESHOLD_PROB_STANDARD));
            bindPreferenceSummaryToValue(threshold_dist_away_standard, String.valueOf(SdkPreferencesHelper.THRESHOLD_DIST_AWAY_STANDARD));
//            bindPreferenceSummaryToValue(back_timeout, String.valueOf(SdkPreferencesHelper.BACK_TIMEOUT));
            bindPreferenceSummaryToValue(thatcham_timeout, String.valueOf(SdkPreferencesHelper.THATCHAM_TIMEOUT));
            bindPreferenceSummaryToValue(unlock_timeout, String.valueOf(SdkPreferencesHelper.UNLOCK_TIMEOUT));
            bindPreferenceSummaryToValue(crypto_pre_auth_timeout, String.valueOf(SdkPreferencesHelper.CRYPTO_PRE_AUTH_TIMEOUT));
            bindPreferenceSummaryToValue(crypto_action_timeout, String.valueOf(SdkPreferencesHelper.CRYPTO_ACTION_TIMEOUT));
            bindPreferenceSummaryToValue(rssi_log_number, String.valueOf(SdkPreferencesHelper.RSSI_LOG_NUMBER));
            print_inside_enabled.setSummary(R.string.pref_print_inside_enabled_summary);
            print_rooftop_enabled.setSummary(R.string.pref_print_rooftop_enabled_summary);
            is_mini_prediction_used.setSummary(R.string.pref_is_mini_prediction_used_summary);
            bindPreferenceSummaryToValue(lin_acc_size, String.valueOf(SdkPreferencesHelper.LIN_ACC_SIZE));
            bindPreferenceSummaryToValue(frozen_threshold, String.valueOf(SdkPreferencesHelper.FROZEN_THRESHOLD));
            user_speed_enabled.setSummary(R.string.pref_user_speed_enabled_summary);
            bindPreferenceSummaryToValue(wanted_speed, String.valueOf(SdkPreferencesHelper.WANTED_SPEED));
            bindPreferenceSummaryToValue(one_step_size, String.valueOf(SdkPreferencesHelper.ONE_STEP_SIZE));
            bindPreferenceSummaryToValue(flash_frequency, String.valueOf(SdkPreferencesHelper.DEFAULT_FLASH_FREQUENCY));
            bindPreferenceSummaryToValue(measurement_interval, String.valueOf(SdkPreferencesHelper.DEFAULT_MEASUREMENT_INTERVAL));
            bindPreferenceSummaryToValue(active_scanning_period, String.valueOf(SdkPreferencesHelper.DEFAULT_ACTIVE_SCAN_PERIOD));
            bindPreferenceSummaryToValue(inactive_scanning_period, String.valueOf(SdkPreferencesHelper.DEFAULT_INACTIVE_SCAN_PERIOD));
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
