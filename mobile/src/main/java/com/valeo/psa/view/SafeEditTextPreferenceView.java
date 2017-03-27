package com.valeo.psa.view;

import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

/**
 * Created by l-avaratha on 27/03/2017
 */

public class SafeEditTextPreferenceView extends EditTextPreference {

    private static final int MAC_ADDRESS_LENGTH = 17;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SafeEditTextPreferenceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SafeEditTextPreferenceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SafeEditTextPreferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeEditTextPreferenceView(Context context) {
        super(context);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (getEditText().getText().length() != MAC_ADDRESS_LENGTH) {
            positiveResult = false;
        }
        super.onDialogClosed(positiveResult);
    }
}
