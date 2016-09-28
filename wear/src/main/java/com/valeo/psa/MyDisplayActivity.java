package com.valeo.psa;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;

public class MyDisplayActivity extends Activity {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_display);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        String title = "";
        String message = "";
        CardFragment cardFragment = CardFragment.create(title, message,
                R.drawable.peugeot_notif_logo);
        fragmentTransaction.add(R.id.frame_layout, cardFragment);
        fragmentTransaction.commit();
    }
}
