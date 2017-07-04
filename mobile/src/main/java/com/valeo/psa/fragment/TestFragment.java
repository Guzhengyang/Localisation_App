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

import com.valeo.bleranging.listeners.MeasureListener;
import com.valeo.psa.R;


/**
 * Created by l-avaratha on 09/03/2017
 */

public class TestFragment extends Fragment implements MeasureListener {
    private EditText test_index;
    private TestFragmentActionListener mListener;
    private Button start_test;
    private ImageView result_test;
    private Handler mHandler;
    private boolean result = false;

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
        test_index.setText("0");
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
                            changeColor(result);
                            result = !result;
                        }
                    }
                });
            }
        });
    }

    private void changeColor(boolean result) {
        if (result) {
            result_test.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        } else {
            result_test.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    public interface TestFragmentActionListener {
    }
}
