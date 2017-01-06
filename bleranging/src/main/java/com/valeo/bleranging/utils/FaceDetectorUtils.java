package com.valeo.bleranging.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

/**
 * Created by l-avaratha on 06/01/2017
 */

public class FaceDetectorUtils {
    private static FaceDetector detector;

    public static void createFaceDetector(Context context) {
        detector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(false)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(
                new LargestFaceFocusingProcessor(
                        detector,
                        new FaceTracker(context)));
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            new CameraSource.Builder(context, detector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedPreviewSize(320, 240)
                    .build()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFaceDetector() {
        detector.release();
        detector = null;
    }

    public static class FaceTracker extends Tracker<Face> {
        private final Context mContext;

        public FaceTracker(Context context) {
            mContext = context;
        }

        @Override
        public void onDone() {
            super.onDone();
            PSALogs.e("FACE", "GONE");
            Toast.makeText(mContext, "FACE GONE !", Toast.LENGTH_SHORT).show();
        }
    }
}
