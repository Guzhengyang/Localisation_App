package com.valeo.bleranging.utils;

import android.content.Context;
import android.media.AudioManager;

import java.util.Date;

/**
 * Created by l-avaratha on 20/10/2016
 */

public class CallReceiver extends PhoneCallReceiver {
    private static boolean smartphoneComIsActivated = false;

    public CallReceiver() {
    }

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        AudioManager audM = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (!audM.isBluetoothScoOn()) { // if no bluetooth headset connected
            smartphoneComIsActivated = true;
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        smartphoneComIsActivated = false;
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        AudioManager audM = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (!audM.isBluetoothScoOn()) { // if no bluetooth headset connected
            smartphoneComIsActivated = true;
        }
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        smartphoneComIsActivated = false;
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }
}
