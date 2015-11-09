package com.rainbow.kam.bt_scanner.Nursing.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Nursing.Activity.StartNursingActivity;
import com.rainbow.kam.bt_scanner.Nursing.Patient.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLE;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleUiCallbacks;
import com.rainbow.kam.bt_scanner.Tools.BLE.WrapperBLE;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by sion on 2015-11-04.
 */
public class DashboardFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity = getActivity();
    public static Handler handler;

    private View view;
    private TextView time, data;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;


    public static DashboardFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        DashboardFragment fragment = new DashboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//                Log.e(TAG, "OBJ = " + (String) msg.obj);
                switch (msg.what) {
                    case 1:
                        time.setText((String) msg.obj);
                        break;
                    case 2:
                        Bundle bundle = msg.getData();
                        String userData = "걸음 수 : "+ bundle.getString("STEP")+ "칼로리 소비 : "+ bundle.getString("CALO")+ "거리 : "+ bundle.getString("DIST");
                        data.setText(userData);
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragmnet_nursing_dashboard, container, false);
        time = (TextView) view.findViewById(R.id.deviceTime);
        data = (TextView) view.findViewById(R.id.deviceData);
//        textView.setText("Fragment #" + mPage);


        return view;
    }
}
