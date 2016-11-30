package com.valeo.bleranging.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;

/**
 * Created by l-avaratha on 25/11/2016
 */

public class SoundUtils {

    /**
     * Make a sound noise from volume at 80%, the sound button level let us decide the remaining 20%
     *
     * @param noiseSelected the noise tonalite
     * @param duration      the length of the noise
     */
    public static void makeNoise(Context mContext, Handler mMainHandler, int noiseSelected, int duration) {
        float streamVolumeOnFifteen = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_SYSTEM);
        float maxVolumeOnFifteen = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        float currentVolumeOnHundred = streamVolumeOnFifteen / maxVolumeOnFifteen;
        currentVolumeOnHundred *= 20;
        currentVolumeOnHundred = currentVolumeOnHundred + 80;
        try {
            final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, (int) currentVolumeOnHundred);
            toneG.startTone(noiseSelected, duration);
            if (mMainHandler != null) {
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toneG.release();
                    }
                }, duration);
            }
        } catch (RuntimeException e) {
            // do nothing
        }
    }
}
