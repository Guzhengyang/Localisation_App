package com.valeo.psa.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;
import com.valeo.psa.interfaces.CalibrationDialogFragmentListener;
import com.valeo.psa.interfaces.CountOffFragmentListener;
import com.valeo.psa.interfaces.InstructionContainerFragmentListener;

/**
 * Created by l-avaratha on 05/05/2017
 */

public class CalibrationDialogFragment extends DialogFragment implements
        InstructionContainerFragmentListener, CountOffFragmentListener {
    private InstructionContainerFragment instructionContainerFragment;
    private CountOffFragment countOffFragment;
    private CalibrationDialogFragmentListener calibrationDialogFragmentListener;

    public CalibrationDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calibration_fragment, container, false);
        instructionContainerFragment = new InstructionContainerFragment();
        countOffFragment = new CountOffFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.calibration_container, instructionContainerFragment).commit();
        return rootView;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void switchToCountOff(boolean switchToCountOff) {
        PSALogs.d("frag", "switchToCountOff " + switchToCountOff);
        if (switchToCountOff) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.calibration_container, countOffFragment).commit();
        } else {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.calibration_container, instructionContainerFragment).commit();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        calibrationDialogFragmentListener = (CalibrationDialogFragmentListener) context;
    }

    @Override
    public void dismissDialog() {
        getDialog().dismiss();
    }

    @Override
    public boolean isFrozen() {
        return calibrationDialogFragmentListener.isFrozen();
    }

    @Override
    public boolean isConnected() {
        return calibrationDialogFragmentListener.isConnected();
    }

    @Override
    public void setSmartphoneOffset() {
        calibrationDialogFragmentListener.setSmartphoneOffset();
    }
}
