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
import android.widget.Toast;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

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
            bindPreferenceSummaryToValue(connected_car_type, ConnectedCarFactory.TYPE_4_A);
            bindPreferenceSummaryToValue(connected_car_base, ConnectedCarFactory.BASE_3);
            user_speed_enabled.setSummary(R.string.pref_user_speed_enabled_summary);
            bindPreferenceSummaryToValue(wanted_speed, String.valueOf(SdkPreferencesHelper.WANTED_SPEED));
            bindPreferenceSummaryToValue(one_step_size, String.valueOf(SdkPreferencesHelper.ONE_STEP_SIZE));
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
        private static final int PICK_IMPORT_FILE_RESULT_CODE = 92163;
        private Preference export_preferences;
        private Preference import_preferences;
        private String sharedPreferencesName;

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
            sharedPreferencesName = getArguments().getString(FILE_NAME,
                    SdkPreferencesHelper.getInstance().getConnectedCarType());
            manager.setSharedPreferencesName(sharedPreferencesName);
            sharedPreferences = manager.getSharedPreferences();
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);
            setViews();
            bindSummaries();
            setOnClickListeners();
        }

        private void setOnClickListeners() {
            export_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String filePath = "sdcard/inBlueConfig/" + sharedPreferencesName;
                    File exportedPrefs = new File(filePath);
                    if (!exportedPrefs.exists()) {
                        try {
                            if (exportedPrefs.createNewFile()) {
                                saveSharedPreferencesToFile(exportedPrefs);
                                Toast.makeText(getActivity(), "Preference exported to " + filePath, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Cannot create file.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        saveSharedPreferencesToFile(exportedPrefs);
                        Toast.makeText(getActivity(), "Preference exported to " + filePath, Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            import_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    selectFile(PICK_IMPORT_FILE_RESULT_CODE);
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

        private boolean saveSharedPreferencesToFile(File dst) {
            boolean res = false;
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                manager.setSharedPreferencesName(sharedPreferencesName);
                sharedPreferences = manager.getSharedPreferences();
                PSALogs.d("map save", manager.getSharedPreferencesName() + " =? " + sharedPreferencesName);
                for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
                    PSALogs.d("map save", entry.getKey() + " " + entry.getValue());
                }
                output.writeObject(sharedPreferences.getAll());
                res = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return res;
        }

        private boolean loadSharedPreferencesFromFile(File src) {
            boolean res = false;
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(src));
                manager.setSharedPreferencesName(sharedPreferencesName);
                sharedPreferences = manager.getSharedPreferences();
                PSALogs.d("map save", manager.getSharedPreferencesName() + " =? " + sharedPreferencesName);
                SharedPreferences.Editor prefEdit = sharedPreferences.edit();
                prefEdit.clear();
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Map.Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();
                    if (v instanceof Boolean) {
                        prefEdit.putBoolean(key, (Boolean) v);
                        PSALogs.d("map load", key + " " + v.toString() + " bool");
                    } else if (v instanceof Float) {
                        prefEdit.putFloat(key, (Float) v);
                        PSALogs.d("map load", key + " " + v.toString() + " float");
                    } else if (v instanceof Integer) {
                        prefEdit.putInt(key, (Integer) v);
                        PSALogs.d("map load", key + " " + v.toString() + " int");
                    } else if (v instanceof Long) {
                        prefEdit.putLong(key, (Long) v);
                        PSALogs.d("map load", key + " " + v.toString() + " long");
                    } else if (v instanceof String) {
                        prefEdit.putString(key, ((String) v));
                        PSALogs.d("map load", key + " " + v.toString() + " string");
                        if (findPreference(key) != null) {
                            PSALogs.d("map change", findPreference(key).getSummary() + " to " + v.toString());
                            findPreference(key).setSummary(v.toString());
                        }
                    }
                }
                prefEdit.apply();
                prefEdit.commit();
                Toast.makeText(getActivity(), "Preference imported from " + src.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                res = true;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return res;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICK_IMPORT_FILE_RESULT_CODE:
                    if (resultCode == RESULT_OK) {
                        String filePath = data.getData().getPath();
                        loadSharedPreferencesFromFile(new File(filePath));
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

            export_preferences = findPreference(getString(R.string.export_pref_name));
            import_preferences = findPreference(getString(R.string.import_pref_name));
        }

        private void setDefaultValues() {

        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        private void bindSummaries() {
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
        private static final int PICK_IMPORT_GENERAL_FILE_RESULT_CODE = 92164;
        private ListPreference connected_car_type;
        private ListPreference connected_car_base;
        private ListPreference selected_ml_model;
        private CheckBoxPreference is_indoor_enabled;
        private CheckBoxPreference connected_car_trame_enabled;
        private EditTextPreference connected_car_trame;
        private CheckBoxPreference com_simulation_enabled;
        private EditTextPreference thatcham_timeout;
        private EditTextPreference crypto_pre_auth_timeout;
        private EditTextPreference crypto_action_timeout;
        private EditTextPreference rssi_log_number;
        private EditTextPreference rolling_av_element;
        private CheckBoxPreference user_speed_enabled;
        private EditTextPreference wanted_speed;
        private EditTextPreference one_step_size;
        private EditTextPreference address_connectable;
        private EditTextPreference address_connectable_pc;
        private EditTextPreference address_connectable_remote_control;
        private EditTextPreference address_front_left;
        private EditTextPreference address_front_right;
        private EditTextPreference address_left;
        private EditTextPreference address_middle;
        private EditTextPreference address_right;
        private EditTextPreference address_trunk;
        private EditTextPreference address_rear_left;
        private EditTextPreference address_back;
        private EditTextPreference address_rear_right;
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
            setOnClickListeners();
        }

        private void setOnClickListeners() {
            export_preferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String filePath = "sdcard/inBlueConfig/" + SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION;
                    File exportedPrefs = new File(filePath);
                    if (!exportedPrefs.exists()) {
                        try {
                            if (exportedPrefs.createNewFile()) {
                                saveSharedPreferencesToFile(exportedPrefs);
                                Toast.makeText(getActivity(), "Preference exported to " + filePath, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Cannot create file.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        saveSharedPreferencesToFile(exportedPrefs);
                        Toast.makeText(getActivity(), "Preference exported to " + filePath, Toast.LENGTH_SHORT).show();
                    }
                    return false;
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

        private boolean saveSharedPreferencesToFile(File dst) {
            boolean res = false;
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                manager.setSharedPreferencesName(sharedPreferencesName);
                sharedPreferences = manager.getSharedPreferences();
                PSALogs.d("map save", manager.getSharedPreferencesName() + " =? " + sharedPreferencesName);
                for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
                    PSALogs.d("map save", entry.getKey() + " " + entry.getValue());
                }
                output.writeObject(sharedPreferences.getAll());
                res = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return res;
        }

        private boolean loadSharedPreferencesFromFile(File src) {
            boolean res = false;
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(src));
                manager.setSharedPreferencesName(sharedPreferencesName);
                sharedPreferences = manager.getSharedPreferences();
                PSALogs.d("map save", manager.getSharedPreferencesName() + " =? " + sharedPreferencesName);
                SharedPreferences.Editor prefEdit = sharedPreferences.edit();
                prefEdit.clear();
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Map.Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();
                    if (v instanceof Boolean) {
                        prefEdit.putBoolean(key, (Boolean) v);
                        PSALogs.d("map load", key + " " + v.toString() + " bool");
                    } else if (v instanceof Float) {
                        prefEdit.putFloat(key, (Float) v);
                        PSALogs.d("map load", key + " " + v.toString() + " float");
                    } else if (v instanceof Integer) {
                        prefEdit.putInt(key, (Integer) v);
                        PSALogs.d("map load", key + " " + v.toString() + " int");
                    } else if (v instanceof Long) {
                        prefEdit.putLong(key, (Long) v);
                        PSALogs.d("map load", key + " " + v.toString() + " long");
                    } else if (v instanceof String) {
                        prefEdit.putString(key, ((String) v));
                        PSALogs.d("map load", key + " " + v.toString() + " string");
                        if (findPreference(key) != null) {
                            PSALogs.d("map change", findPreference(key).getSummary() + " to " + v.toString());
                            findPreference(key).setSummary(v.toString());
                        }
                    }
                }
                prefEdit.apply();
                prefEdit.commit();
                Toast.makeText(getActivity(), "Preference imported from " + src.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                res = true;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return res;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICK_IMPORT_GENERAL_FILE_RESULT_CODE:
                    if (resultCode == RESULT_OK) {
                        String filePath = data.getData().getPath();
                        loadSharedPreferencesFromFile(new File(filePath));
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
            selected_ml_model = ((ListPreference) findPreference(getString(R.string.machine_learning_model_pref_name)));
            is_indoor_enabled = ((CheckBoxPreference) findPreference(getString(R.string.is_indoor_enabled_pref_name)));
            connected_car_trame_enabled = ((CheckBoxPreference) findPreference(getString(R.string.connected_car_trame_enabled_pref_name)));
            connected_car_trame = ((EditTextPreference) findPreference(getString(R.string.connected_car_trame_pref_name)));
            com_simulation_enabled = ((CheckBoxPreference) findPreference(getString(R.string.com_simulation_enabled_pref_name)));
            thatcham_timeout = ((EditTextPreference) findPreference(getString(R.string.thatcham_timeout_pref_name)));
            crypto_pre_auth_timeout = ((EditTextPreference) findPreference(getString(R.string.crypto_pre_auth_timeout_pref_name)));
            crypto_action_timeout = ((EditTextPreference) findPreference(getString(R.string.crypto_action_timeout_pref_name)));
            rssi_log_number = ((EditTextPreference) findPreference(getString(R.string.rssi_log_number_pref_name)));
            rolling_av_element = ((EditTextPreference) findPreference(getString(R.string.rolling_av_element_pref_name)));
            user_speed_enabled = ((CheckBoxPreference) findPreference(getString(R.string.user_speed_enabled_pref_name)));
            wanted_speed = ((EditTextPreference) findPreference(getString(R.string.wanted_speed_pref_name)));
            one_step_size = ((EditTextPreference) findPreference(getString(R.string.one_step_size_pref_name)));
            address_connectable = ((EditTextPreference) findPreference(getString(R.string.address_connectable_pref_name)));
            address_connectable_pc = ((EditTextPreference) findPreference(getString(R.string.address_connectable_pref_name)));
            address_connectable_remote_control = ((EditTextPreference) findPreference(getString(R.string.address_connectable_remote_control_pref_name)));
            address_front_left = ((EditTextPreference) findPreference(getString(R.string.address_front_left_pref_name)));
            address_front_right = ((EditTextPreference) findPreference(getString(R.string.address_front_right_pref_name)));
            address_left = ((EditTextPreference) findPreference(getString(R.string.address_left_pref_name)));
            address_middle = ((EditTextPreference) findPreference(getString(R.string.address_middle_pref_name)));
            address_right = ((EditTextPreference) findPreference(getString(R.string.address_right_pref_name)));
            address_trunk = ((EditTextPreference) findPreference(getString(R.string.address_trunk_pref_name)));
            address_rear_left = ((EditTextPreference) findPreference(getString(R.string.address_rear_left_pref_name)));
            address_back = ((EditTextPreference) findPreference(getString(R.string.address_back_pref_name)));
            address_rear_right = ((EditTextPreference) findPreference(getString(R.string.address_rear_right_pref_name)));
            export_preferences = findPreference(getString(R.string.export_pref_name));
            import_preferences = findPreference(getString(R.string.import_pref_name));
        }

        private void setDefaultValues() {
            is_indoor_enabled.setChecked(SdkPreferencesHelper.getInstance().getIsIndoor());
            connected_car_trame_enabled.setChecked(SdkPreferencesHelper.getInstance().getConnectedCarTrameEnabled());
            com_simulation_enabled.setChecked(SdkPreferencesHelper.getInstance().getComSimulationEnabled());
            user_speed_enabled.setChecked(SdkPreferencesHelper.getInstance().getUserSpeedEnabled());
            thatcham_timeout.setText(thatcham_timeout.getSummary().toString());
            crypto_pre_auth_timeout.setText(crypto_pre_auth_timeout.getSummary().toString());
            crypto_action_timeout.setText(crypto_action_timeout.getSummary().toString());
            rssi_log_number.setText(rssi_log_number.getSummary().toString());
            rolling_av_element.setText(rolling_av_element.getSummary().toString());
            wanted_speed.setText(wanted_speed.getSummary().toString());
            one_step_size.setText(one_step_size.getSummary().toString());
            address_connectable.setText(address_connectable.getSummary().toString());
            address_connectable_pc.setText(address_connectable_pc.getSummary().toString());
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
            bindPreferenceSummaryToValue(connected_car_type, ConnectedCarFactory.TYPE_4_A);
            bindPreferenceSummaryToValue(connected_car_base, ConnectedCarFactory.BASE_3);
            bindPreferenceSummaryToValue(selected_ml_model, ConnectedCarFactory.MODEL_RF);
            is_indoor_enabled.setSummary(R.string.pref_is_indoor_enabled_summary);
            connected_car_trame_enabled.setSummary(R.string.pref_car_forced_trame_enabled_summary);
            com_simulation_enabled.setSummary(R.string.pref_com_simulation_enabled);
            bindPreferenceSummaryToValue(connected_car_trame, "");
            bindPreferenceSummaryToValue(thatcham_timeout, String.valueOf(SdkPreferencesHelper.THATCHAM_TIMEOUT));
            bindPreferenceSummaryToValue(crypto_pre_auth_timeout, String.valueOf(SdkPreferencesHelper.CRYPTO_PRE_AUTH_TIMEOUT));
            bindPreferenceSummaryToValue(crypto_action_timeout, String.valueOf(SdkPreferencesHelper.CRYPTO_ACTION_TIMEOUT));
            bindPreferenceSummaryToValue(rssi_log_number, String.valueOf(SdkPreferencesHelper.RSSI_LOG_NUMBER));
            bindPreferenceSummaryToValue(rolling_av_element, String.valueOf(SdkPreferencesHelper.ROLLING_AVERAGE_ELEMENTS));
            user_speed_enabled.setSummary(R.string.pref_user_speed_enabled_summary);
            bindPreferenceSummaryToValue(wanted_speed, String.valueOf(SdkPreferencesHelper.WANTED_SPEED));
            bindPreferenceSummaryToValue(one_step_size, String.valueOf(SdkPreferencesHelper.ONE_STEP_SIZE));
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
