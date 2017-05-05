package com.valeo.psa.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.psa.R;
import com.valeo.psa.adapter.CalibrationAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l-avaratha on 05/05/2017
 */

public class CalibrationDialogFragment extends DialogFragment {
    public CalibrationDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calibration_fragment, container);
        final ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        List<Fragment> fragments = getFragments();
        CalibrationAdapter calibrationAdapter = new CalibrationAdapter(getChildFragmentManager(), fragments);
        mViewPager.setAdapter(calibrationAdapter);
        final TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager, true);
        return rootView;
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(FragmentOneInfo.newInstance("Info", 1));
        fragmentList.add(FragmentTwoConsigne.newInstance("Consigne", 2));
        return fragmentList;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    public static class FragmentOneInfo extends Fragment {
        public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
        private int item;

        public static FragmentOneInfo newInstance(String message, int item) {
            FragmentOneInfo f = new FragmentOneInfo();
            Bundle bdl = new Bundle(item);
            f.item = item;
            bdl.putString(EXTRA_MESSAGE, message);
            f.setArguments(bdl);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.info_fragment, container, false);
        }
    }

    public static class FragmentTwoConsigne extends Fragment {
        public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
        private int item;
        private Button start_calibration_button;

        public static FragmentTwoConsigne newInstance(String message, int item) {
            FragmentTwoConsigne f = new FragmentTwoConsigne();
            Bundle bdl = new Bundle(item);
            f.item = item;
            bdl.putString(EXTRA_MESSAGE, message);
            f.setArguments(bdl);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.consigne_fragment, container, false);
            start_calibration_button = (Button) rootView.findViewById(R.id.start_calibration_button);
            start_calibration_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(rootView, R.string.calibration_done, Snackbar.LENGTH_LONG).show();
                    SdkPreferencesHelper.getInstance().setIsCalibrated(true);
                }
            });
            return rootView;
        }
    }
}
