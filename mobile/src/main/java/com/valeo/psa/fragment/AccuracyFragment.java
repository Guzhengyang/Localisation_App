package com.valeo.psa.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.SpinnerListener;
import com.valeo.psa.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;

/**
 * Created by l-avaratha on 09/03/2017
 */

public class AccuracyFragment extends Fragment implements SpinnerListener {
    private Spinner accuracy_spinner;
    private List<String> accuracy_spinner_classes;
    private ArrayAdapter<String> accuracy_spinner_adapter;
    private Button start_accuracy_measure;
    private Button stop_accuracy_measure;
    private TextView accuracy_zone_result;
    private String selectedAccuracyZone = PREDICTION_UNKNOWN;
    private AccuracyFragmentActionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.accuracy_fragment, container);
        setView(rootView);
        setOnClickListeners();
        setSpinner();
        return rootView;
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);
        if (mContext instanceof Activity) {
            mListener = (AccuracyFragmentActionListener) mContext;
        }
    }

    private void setView(View rootView) {
        accuracy_spinner = (Spinner) rootView.findViewById(R.id.accuracy_spinner);
        start_accuracy_measure = (Button) rootView.findViewById(R.id.start_accuracy_measure);
        stop_accuracy_measure = (Button) rootView.findViewById(R.id.stop_accuracy_measure);
        accuracy_zone_result = (TextView) rootView.findViewById(R.id.accuracy_zone_result);
    }

    /**
     * Set OnClickListeners
     */
    private void setOnClickListeners() {
        start_accuracy_measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PSALogs.d("accuracy", "start counting");
                if (!selectedAccuracyZone.equals(PREDICTION_UNKNOWN)) {
                    accuracy_spinner.setEnabled(false);
                    start_accuracy_measure.setEnabled(false);
                    stop_accuracy_measure.setEnabled(true);
                    mListener.calculateAccuracyFor(selectedAccuracyZone);
                    accuracy_zone_result.setText("");
                    accuracy_zone_result.setVisibility(View.INVISIBLE);
                }
            }
        });
        stop_accuracy_measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PSALogs.d("accuracy", "stop counting");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stop_accuracy_measure.setEnabled(false);
                        accuracy_spinner.setEnabled(true);
                        start_accuracy_measure.setEnabled(true);
                        accuracy_zone_result.setText(String.format(Locale.FRANCE,
                                getString(R.string.accuracy_zone_result),
                                selectedAccuracyZone,
                                mListener.getCalculatedAccuracy()));
                        accuracy_zone_result.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void setSpinner() {
        accuracy_spinner_classes = new ArrayList<>();
        accuracy_spinner_adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, accuracy_spinner_classes);
        accuracy_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accuracy_spinner.setAdapter(accuracy_spinner_adapter);
        accuracy_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                selectedAccuracyZone = (String) adapterView.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedAccuracyZone = PREDICTION_UNKNOWN;
            }
        });
    }

    @Override
    public void updateAccuracySpinner() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (accuracy_spinner != null && accuracy_spinner_classes != null
                        && accuracy_spinner_adapter != null) {
                    if (mListener.getStandardClasses() != null) {
                        accuracy_spinner_classes.clear();
                        accuracy_spinner_classes.addAll(
                                Arrays.asList(mListener.getStandardClasses()));
                        accuracy_spinner_adapter.notifyDataSetChanged();
                    } else {
                        PSALogs.e("spinner", "mBleRangingHelper.getStandardClasses is NULL");
                    }
                }
            }
        });
    }

    public interface AccuracyFragmentActionListener {
        void calculateAccuracyFor(String zone);

        int getCalculatedAccuracy();

        String[] getStandardClasses();
    }

}
