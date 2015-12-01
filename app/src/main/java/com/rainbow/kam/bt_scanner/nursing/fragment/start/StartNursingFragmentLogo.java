package com.rainbow.kam.bt_scanner.nursing.fragment.start;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.nursing.activity.StartNursingActivity;
import com.rainbow.kam.bt_scanner.nursing.patient.Patient;
import com.rainbow.kam.bt_scanner.tools.design.RippleView;

import java.lang.ref.WeakReference;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;

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
