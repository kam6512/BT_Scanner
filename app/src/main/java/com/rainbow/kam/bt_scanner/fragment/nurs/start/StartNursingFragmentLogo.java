package com.rainbow.kam.bt_scanner.fragment.nurs.start;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class StartNursingFragmentLogo extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.fragment_nursing_start_splash, container, false);

        return view;
    }
}
