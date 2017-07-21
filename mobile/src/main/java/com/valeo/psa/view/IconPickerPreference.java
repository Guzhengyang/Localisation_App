package com.valeo.psa.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.valeo.bleranging.persistence.Constants.TYPE_8_A;
import static com.valeo.psa.R.array.entry_values_list_preference;
import static java.util.Arrays.asList;

/**
 * Created by l-avaratha on 19/10/2016
 */

public class IconPickerPreference extends ListPreference {

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final Resources resources;
    private CharSequence[] iconFile;
    private CharSequence[] iconName;
    private List<IconItem> icons;
    private String selectedIconFile, defaultIconFile;
    private ImageView icon;
    private TextView summary;

    public IconPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        resources = context.getResources();
        PreferenceManager manager = getPreferenceManager();
        if (manager != null) {
            PSALogs.d("icon", "manager != null");
            manager.setSharedPreferencesName(SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION);
            sharedPreferences = manager.getSharedPreferences();
        } else {
            PSALogs.d("icon", "manager IS null");
            sharedPreferences = context.getSharedPreferences(
                    SdkPreferencesHelper.SAVED_CC_GENERIC_OPTION, Context.MODE_PRIVATE);
        }
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.attrs_icon, 0, 0);
        try {
            defaultIconFile = a.getString(R.styleable.attrs_icon_iconFile);
        } finally {
            a.recycle();
        }
    }

    private String getEntry(String value) {
        String[] entries = resources.getStringArray(R.array.iconName);
        String[] values = resources.getStringArray(R.array.iconFile);
        int index = asList(values).indexOf(value);
        return entries[index];
    }

    private String getIconFileFromId(String selectedIconId, String defaultIconFile) {
        String[] ids = resources.getStringArray(entry_values_list_preference);
        int index = Arrays.asList(ids).indexOf(selectedIconId);
        String[] values = resources.getStringArray(R.array.iconFile);
        return values[index];
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        String selectedIconId = sharedPreferences.getString(context.
                getString(R.string.connected_car_type_pref_name), TYPE_8_A);
        selectedIconFile = getIconFileFromId(selectedIconId, defaultIconFile);
        icon = (ImageView) view.findViewById(R.id.ic_selected);
        updateIcon();
        summary = (TextView) view.findViewById(R.id.ic_summary);
        summary.setText(getEntry(selectedIconFile));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (icons != null) {
            for (int i = 0; i < iconName.length; i++) {
                IconItem item = icons.get(i);
                if (item.isChecked) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(
                            context.getString(R.string.connected_car_type_pref_name),
                            item.id);
                    editor.apply();
                    editor.commit();
                    selectedIconFile = item.file;
                    updateIcon();
                    summary.setText(item.name);
                    break;
                }
            }
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton(null, null);
        String[] iconId = resources.getStringArray(entry_values_list_preference);
        iconName = getEntries();
        iconFile = getEntryValues();
        if (iconName == null || iconFile == null
                || iconName.length != iconFile.length || iconId.length != iconName.length) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array "
                            + "and an entryValues array which are both the same length");
        }
        String selectedIcon = sharedPreferences.getString(
                context.getString(R.string.connected_car_type_pref_name),
                resources.getString(R.string.icon_default));
        icons = new ArrayList<>();
        for (int i = 0; i < iconName.length; i++) {
            boolean isSelected = selectedIcon.equals(iconFile[i]);
            IconItem item = new IconItem(iconId[i], iconName[i], iconFile[i], isSelected);
            icons.add(item);
        }
        CustomListPreferenceAdapter customListPreferenceAdapter = new CustomListPreferenceAdapter(
                context, R.layout.item_picker, icons);
        builder.setAdapter(customListPreferenceAdapter, null);
    }

    private void updateIcon() {
        int identifier = resources.getIdentifier(selectedIconFile, "drawable",
                context.getPackageName());
        icon.setImageResource(identifier);
        icon.setTag(selectedIconFile);
    }

    private static class IconItem {
        private final String id;
        private final String file;
        private final String name;
        private boolean isChecked;

        public IconItem(CharSequence id, CharSequence name, CharSequence file, boolean isChecked) {
            this(id.toString(), name.toString(), file.toString(), isChecked);
        }

        public IconItem(String id, String name, String file, boolean isChecked) {
            this.id = id;
            this.name = name;
            this.file = file;
            this.isChecked = isChecked;
        }
    }

    private static class ViewHolder {
        ImageView iconImage;
        TextView iconName;
        RadioButton radioButton;
    }

    private class CustomListPreferenceAdapter extends ArrayAdapter<IconItem> {

        private final Context context;
        private final List<IconItem> icons;
        private final int resource;

        public CustomListPreferenceAdapter(Context context, int resource, List<IconItem> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            this.icons = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, parent, false);
                holder = new ViewHolder();
                holder.iconName = (TextView) convertView.findViewById(R.id.iconName);
                holder.iconImage = (ImageView) convertView.findViewById(R.id.iconImage);
                holder.radioButton = (RadioButton) convertView.findViewById(R.id.iconRadio);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iconName.setText(icons.get(position).name);
            int identifier = context.getResources().getIdentifier(icons.get(position).file,
                    "drawable", context.getPackageName());
            holder.iconImage.setImageResource(identifier);
            holder.radioButton.setChecked(icons.get(position).isChecked);
            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ViewHolder holder = (ViewHolder) v.getTag();
                    for (int i = 0; i < icons.size(); i++) {
                        icons.get(i).isChecked = i == position;
                    }
                    getDialog().dismiss();
                }
            });
            return convertView;
        }
    }

}
