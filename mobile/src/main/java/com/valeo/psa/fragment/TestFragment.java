package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.valeo.bleranging.listeners.TestListener;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import static com.valeo.bleranging.persistence.Constants.PREDICTION_ACCESS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LOCK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;

/**
 * Obsolete fragment launching then displaying the results of a test.
 * It might have been replaced by automatic connexion and the chessboards displays.
 * Since it's actually hidden by default, it's most likely useless now.
 */
public class TestFragment extends Fragment implements TestListener {
    private EditText test_index;
    private TestFragmentActionListener mListener;
    private Button start_test;
    private ImageView result_test;
    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.test_fragment, container, false);
        setView(rootView);
        setOnClickListeners();
        return rootView;
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        if (mContext instanceof Activity) {
            mListener = (TestFragmentActionListener) mContext;
        }
    }

    private void setView(View rootView) {
        test_index = (EditText) rootView.findViewById(R.id.test_index);
        test_index.setText("0.5");
        start_test = (Button) rootView.findViewById(R.id.start_test);
        result_test = (ImageView) rootView.findViewById(R.id.result_test);
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Set OnClickListeners
     */
    private void setOnClickListeners() {
        start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null && isAdded()) {
                            mListener.setNewThreshold(Double.valueOf(test_index.getText().toString()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void changeColor(String result) {
        if(isAdded()) {
            switch (result) {
                case PREDICTION_UNKNOWN:
                    PSALogs.d("debug", "black");
                    result_test.setBackgroundColor(getResources().getColor(android.R.color.black));
                    break;
                case PREDICTION_LOCK:
                    PSALogs.d("debug", "red");
                    result_test.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case PREDICTION_ACCESS:
                    PSALogs.d("debug", "green");
                    result_test.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    break;
            }
        }
    }

    public interface TestFragmentActionListener {
        void setNewThreshold(double value);
    }
}
