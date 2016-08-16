package com.valeo.psa.activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
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
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
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

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), defaultValue));
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
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.OFFSET_POCKET_FOR_START_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.OFFSET_POCKET_FOR_START));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.OFFSET_POCKET_FOR_LOCK));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.OFFSET_POCKET_FOR_UNLOCK));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.START_THR_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.START_THRESHOLD));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.UNLOCK_THR_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.UNLOCK_IN_THE_RUN_THRESHOLD));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.LOCK_THR_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.WALK_AWAY_LOCKING_THRESHOLD));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.WELCOME_THR_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.WELCOME_THRESHOLD));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.NEXT_TO_DOOR_RATIO_THR_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.NEXT_TO_DOOR_RATIO_THRESHOLD));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.NEXT_TO_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.NEXT_TO_BACKDOOR_RATIO_THRESHOLD_MIN));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.NEXT_TO_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.NEXT_TO_BACKDOOR_RATIO_THRESHOLD_MAX));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.NEXT_TO_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.NEXT_TO_DOOR_RATIO_THRESHOLD_ML_MR_MAX));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.NEXT_TO_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.NEXT_TO_DOOR_RATIO_THRESHOLD_ML_MR_MIN));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.AVERAGE_DELTA_UNLOCK_THRESHOLD));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.AVERAGE_DELTA_LOCK_THRESHOLD));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.RSSI_LOG_NUMBER_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.RSSI_LOG_NUMBER));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ROLLING_AV_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.ROLLING_AVERAGE_ELEMENTS));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.START_NB_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.START_NB_ELEMENT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.LOCK_NB_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.LOCK_NB_ELEMENT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.UNLOCK_NB_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.UNLOCK_NB_ELEMENT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.WELCOME_NB_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.WELCOME_NB_ELEMENT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.LONG_NB_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.LONG_NB_ELEMENT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.SHORT_NB_ELEMENT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.SHORT_NB_ELEMENT));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.UNLOCK_MODE_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.UNLOCK_MODE));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.LOCK_MODE_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.LOCK_MODE));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.START_MODE_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.START_MODE));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ECRETAGE_70_100_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.ECRETAGE_70_100));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ECRETAGE_50_70_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.ECRETAGE_50_70));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ECRETAGE_30_50_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.ECRETAGE_30_50));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ECRETAGE_30_30_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.ECRETAGE_30_30));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.LIN_ACC_SIZE_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.LIN_ACC_SIZE));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.CORRECTION_LIN_ACC_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.CORRECTION_LIN_ACC));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.EQUALIZER_LEFT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.EQUALIZER_LEFT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.EQUALIZER_MIDDLE_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.EQUALIZER_MIDDLE));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.EQUALIZER_RIGHT_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.EQUALIZER_RIGHT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.EQUALIZER_BACK_PREFERENCES_NAME), String.valueOf(SdkPreferencesHelper.EQUALIZER_BACK));

            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ADDRESS_CONNECTABLE_PREFERENCE_NAME), String.valueOf(SdkPreferencesHelper.BLE_ADDRESS_CONNECTABLE));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ADDRESS_LEFT_PREFERENCE_NAME), String.valueOf(SdkPreferencesHelper.BLE_ADDRESS_LEFT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ADDRESS_MIDDLE_PREFERENCE_NAME), String.valueOf(SdkPreferencesHelper.BLE_ADDRESS_MIDDLE));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ADDRESS_RIGHT_PREFERENCE_NAME), String.valueOf(SdkPreferencesHelper.BLE_ADDRESS_RIGHT));
            bindPreferenceSummaryToValue(findPreference(SdkPreferencesHelper.ADDRESS_BACK_PREFERENCE_NAME), String.valueOf(SdkPreferencesHelper.BLE_ADDRESS_BACK));
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
