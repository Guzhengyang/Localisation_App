package com.valeo.psa.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.valeo.psa.R;
import com.valeo.psa.adapter.CalibrationAdapter;
import com.valeo.psa.interfaces.InstructionContainerFragmentListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l-avaratha on 09/05/2017
 */

public class InstructionContainerFragment extends Fragment implements InstructionContainerFragmentListener {
    public InstructionContainerFragmentListener instructionContainerFragmentListener = null;

    public InstructionContainerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.instruction_container_fragment, container, false);
        final ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        final List<Fragment> fragments = getFragments();
        final CalibrationAdapter calibrationAdapter = new CalibrationAdapter(getChildFragmentManager(), fragments);
        mViewPager.setAdapter(calibrationAdapter);
        final TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager, true);
        return rootView;
    }

    private List<Fragment> getFragments() {
        final List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(FragmentOneInfo.newInstance("Info", 1));
        fragmentList.add(FragmentTwoInstruction.newInstance("Instruction", 2));
        fragmentList.add(FragmentThreeLaunchCalibration.newInstance("Launcher", 3));
        return fragmentList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            instructionContainerFragmentListener = (InstructionContainerFragmentListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement InstructionContainerFragmentListener");
        }
    }

    @Override
    public void switchToCountOff(boolean switchToCountOff) {
        if (instructionContainerFragmentListener != null) {
            instructionContainerFragmentListener.switchToCountOff(switchToCountOff);
        }
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

    public static class FragmentTwoInstruction extends Fragment {
        public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
        private int item;

        public static FragmentTwoInstruction newInstance(String message, int item) {
            FragmentTwoInstruction f = new FragmentTwoInstruction();
            Bundle bdl = new Bundle(item);
            f.item = item;
            bdl.putString(EXTRA_MESSAGE, message);
            f.setArguments(bdl);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.instruction_fragment, container, false);
        }
    }

    public static class FragmentThreeLaunchCalibration extends Fragment {
        public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
        public InstructionContainerFragmentListener instructionContainerFragmentListener2 = null;
        private int item;
        private Button start_calibration_button;

        public static FragmentThreeLaunchCalibration newInstance(String message, int item) {
            FragmentThreeLaunchCalibration f = new FragmentThreeLaunchCalibration();
            Bundle bdl = new Bundle(item);
            f.item = item;
            bdl.putString(EXTRA_MESSAGE, message);
            f.setArguments(bdl);
            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            onAttachToParentFragment(getParentFragment());
        }

        public void onAttachToParentFragment(Fragment fragment) {
            try {
                instructionContainerFragmentListener2 = (InstructionContainerFragmentListener) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString()
                        + " must implement InstructionContainerFragmentListener 2");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.launch_calibration_fragment, container, false);
            start_calibration_button = (Button) rootView.findViewById(R.id.start_calibration_button);
            start_calibration_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instructionContainerFragmentListener2.switchToCountOff(true);
                }
            });
            return rootView;
        }
    }
}
