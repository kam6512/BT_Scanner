package com.rainbow.kam.bt_scanner.fragment.prime.setting;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeSettingActivity;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.tools.BluetoothHelper;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class SelectDeviceDialogFragment extends DialogFragment implements SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = getClass().getSimpleName();
    private Context context;
    private View view;
    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private TextView noDeviceTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DeviceAdapter deviceAdapter;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stopScan();

            if (deviceAdapter.getItemCount() < 1) {
                noDeviceTextView.setVisibility(View.VISIBLE);
            }
            deviceAdapter.notifyDataSetChanged();
        }
    };

    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @DebugLog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_band_init_add_device, container, false);

        setWindowSetting();
        setRecyclerView();
        setOtherView();

        setScannerCallback();

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        return view;
    }


    @DebugLog
    @Override
    public void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @DebugLog
    @Override
    public void onPause() { //꺼짐
        super.onPause();
        stopScan();
    }


    private void setWindowSetting() {
        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }


    private void setRecyclerView() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.nursing_device_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        RecyclerView selectDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.nursing_device_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        selectDeviceRecyclerView.setLayoutManager(layoutManager);
        selectDeviceRecyclerView.setHasFixedSize(true);
        deviceAdapter = new DeviceAdapter((PrimeSettingActivity) context);
        selectDeviceRecyclerView.setAdapter(deviceAdapter);
    }


    private void setOtherView() {

        noDeviceTextView = (TextView) view.findViewById(R.id.nursing_no_device_textView);
        noDeviceTextView.setVisibility(View.INVISIBLE);
    }


    private void setScannerCallback() {
        if (BluetoothHelper.isBuildVersionLM) {
            setScannerL();
        } else {
            setScanner();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setScannerL() {
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (result != null) {
                    deviceAdapter.addDevice(result.getDevice(), result.getRssi());
                }
            }


            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    if (result != null) {
                        deviceAdapter.addDevice(result.getDevice(), result.getRssi());
                    }
                }
            }


            @Override
            public void onScanFailed(int errorCode) {
                stopScan();
            }
        };
    }


    private void setScanner() {
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {
                deviceAdapter.addDevice(device, rssi);
            }
        };
    }


    @Override
    public void onRefresh() {
        if (isScanning) {  //스캔 시작
            stopScan();
        } else { //재 스캔시(10초이내)
            registerBluetooth();
        }
    }


    @DebugLog
    @SuppressLint("NewApi")
    private void registerBluetooth() {
        try {
            bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothAdapter.isEnabled() && bluetoothManager != null && bluetoothAdapter != null) {

                deviceAdapter.clear();

                if (BluetoothHelper.isBuildVersionLM) {
                    bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                }

                startScan();
            } else {
               BluetoothHelper.initBluetoothOn(context);
            }
        } catch (Exception e) {
            Toast.makeText(context, R.string.bt_fail, Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
        }
    }



    @DebugLog
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void startScan() {
        long SCAN_PERIOD = 5000;
        handler.postDelayed(runnable, SCAN_PERIOD); //5초 뒤에 OFF

        //시작
        deviceAdapter.clear();
        isScanning = true;
        noDeviceTextView.setVisibility(View.INVISIBLE);

        swipeRefreshLayout.post(postSwipeRefresh);

        if (BluetoothHelper.isBuildVersionLM) {
            if (bleScanner != null) {
                bleScanner.startScan(scanCallback);
            }
        } else {
            //noinspection deprecation
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }


    @DebugLog
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void stopScan() {
        handler.removeCallbacks(runnable);

        isScanning = false;
        noDeviceTextView.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);

        //중지
        if (BluetoothHelper.isBuildVersionLM) {
            if (bleScanner != null && bluetoothAdapter.isEnabled()) {
                bleScanner.stopScan(scanCallback);
            }
        } else {
            //noinspection deprecation
            bluetoothAdapter.stopLeScan(leScanCallback);
        }


    }


}
