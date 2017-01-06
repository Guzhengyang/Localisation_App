package com.valeo.bleranging.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
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
    private Context mContext;
    private FaceDetector detector;
    private CameraSource cameraSource;

    public FaceDetectorUtils(Context context) {
        this.mContext = context;
    }

    public void createFaceDetector() {
        detector = new FaceDetector.Builder(mContext)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(false)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(
                new LargestFaceFocusingProcessor(
                        detector,
                        new FaceTracker(mContext)));
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraSource = new CameraSource.Builder(mContext, detector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedPreviewSize(320, 240)
                    .build()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFaceDetector() {
        cameraSource.release();
        detector.release();
        detector = null;
    }

    public static class FaceTracker extends Tracker<Face> {
        private final Context mContext;
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        public FaceTracker(Context context) {
            mContext = context;
        }

        @Override
        public void onDone() {
            super.onDone();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    PSALogs.e("FACE", "GONE");
                    Toast.makeText(mContext, "FACE GONE !", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
