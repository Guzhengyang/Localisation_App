package com.valeo.psa.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

/**
 * Created by l-avaratha on 09/03/2017
 */
public class NfcFragment extends Fragment {
    private TextView tips;
    private TextView nfc_disclaimer;
    private ImageView nfc_logo;
    private Typeface lightTypeFace;
    private Typeface boldTypeFace;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.nfc_fragment, container, false);
        setView(rootView);
        return rootView;
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        tips = (TextView) rootView.findViewById(R.id.tips);
        nfc_disclaimer = (TextView) rootView.findViewById(R.id.nfc_disclaimer);
        nfc_logo = (ImageView) rootView.findViewById(R.id.nfc_logo);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NfcManager manager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            tips.setVisibility(View.VISIBLE);
            nfc_disclaimer.setVisibility(View.VISIBLE);
            nfc_logo.setVisibility(View.VISIBLE);
            try {
                lightTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Lt.otf");
                boldTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeueLTStd-Bd.otf");
            } catch (Exception e) {
                PSALogs.e("NfcFragment", "Font not loaded !");
            }
            tips.setTypeface(boldTypeFace, Typeface.BOLD);
            nfc_disclaimer.setTypeface(lightTypeFace, Typeface.NORMAL);
        } else {
            tips.setVisibility(View.GONE);
            nfc_disclaimer.setVisibility(View.GONE);
            nfc_logo.setVisibility(View.GONE);
        }
    }
}
