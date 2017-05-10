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
import com.valeo.psa.interfaces.ConsigneContainerFragmentListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l-avaratha on 09/05/2017
 */

public class ConsigneContainerFragment extends Fragment implements ConsigneContainerFragmentListener {
    public ConsigneContainerFragmentListener consigneContainerFragmentListener = null;

    public ConsigneContainerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.consigne_container_fragment, container, false);
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
        fragmentList.add(FragmentTwoConsigne.newInstance("Consigne", 2));
        return fragmentList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            consigneContainerFragmentListener = (ConsigneContainerFragmentListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement CalibrationDialogFragmentListener");
        }
    }

    @Override
    public void switchToCountOff(boolean switchToCountOff) {
        if (consigneContainerFragmentListener != null) {
            consigneContainerFragmentListener.switchToCountOff(switchToCountOff);
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

    public static class FragmentTwoConsigne extends Fragment {
        public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
        public ConsigneContainerFragmentListener consigneContainerFragmentListener2 = null;
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
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            onAttachToParentFragment(getParentFragment());
        }

        public void onAttachToParentFragment(Fragment fragment) {
            try {
                consigneContainerFragmentListener2 = (ConsigneContainerFragmentListener) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString()
                        + " must implement CalibrationDialogFragmentListener 2");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.consigne_fragment, container, false);
            start_calibration_button = (Button) rootView.findViewById(R.id.start_calibration_button);
            start_calibration_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    consigneContainerFragmentListener2.switchToCountOff(true);
                }
            });
            return rootView;
        }
    }
}
