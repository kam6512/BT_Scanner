package com.rainbow.kam.bt_scanner.fragment.nurs.splash;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.nurs.selected.SelectDeviceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class SplashNursingDialog extends DialogFragment {

    private final String TAG = getClass().getSimpleName(); //로그용 태그
    private static final int REQUEST_ENABLE_BT = 1;
    private static final boolean isBuildVersionLM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    private static long SCAN_PERIOD = 5000;

    private Activity activity;
    private View view;
    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView selectDeviceRecyclerView;
    private RecyclerView.Adapter adapter = null;
    private ArrayList<SelectDeviceItem> selectDeviceItems = new ArrayList<>();

    private ProgressBar searchingProgressBar;
    private TextView noDeviceTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_nursing_splash_add_device, container, false);
        activity = getActivity();

        setWindowSetting();
        setRecyclerView();
        setOtherView();

        setScannerCallback();

        return view;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();

        try {

            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();

            selectDeviceItems.clear();
            adapter.notifyDataSetChanged();
            if (bluetoothAdapter == null) {
                throw new Exception();
            }

            if (isBuildVersionLM) {
                bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (bleScanner == null) {
                    throw new Exception();
                }

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            startScan();
                        }
                    });
                }
                startScan();
            }

        } catch (Exception e) {
            Toast.makeText(activity, R.string.bt_fail, Toast.LENGTH_LONG).show();

            if (bluetoothAdapter == null) {
                Log.e(TAG, "BA null");
            }
            if (bluetoothManager == null) {
                Log.e(TAG, "BM null");
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopScan();
    }

    private void setWindowSetting() {
        Window window = getDialog().getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void setRecyclerView() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.nursing_device_swipeRefreshLayout);

        selectDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.nursing_device_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        selectDeviceRecyclerView.setLayoutManager(layoutManager);
        selectDeviceRecyclerView.setHasFixedSize(true);
    }

    private void setOtherView() {
        searchingProgressBar = (ProgressBar) view.findViewById(R.id.nursing_searching_progress_bar);
        searchingProgressBar.setVisibility(View.INVISIBLE);

        noDeviceTextView = (TextView) view.findViewById(R.id.nursing_no_device_textview);
        noDeviceTextView.setVisibility(View.INVISIBLE);
    }

    private void setScannerCallback() {
        if (isBuildVersionLM) {
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
                    processResult(result);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    if (result != null) {
                        processResult(result);
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
            }

            private void processResult(final ScanResult result) {

                String deviceName = result.getDevice().getName();

                if (deviceName == null) {
                    deviceName = "N/A";
                }

                selectDeviceItems.add(new SelectDeviceItem(deviceName, result.getDevice().getAddress(), result.getDevice().getType(), result.getDevice().getBondState(), result.getRssi()));
                deviceListFilter();
            }
        };
    }

    private void setScanner() {
        Log.e(TAG, "setScanner");
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {
                String deviceName = device.getName();

                if (deviceName == null) {
                    deviceName = "N/A";
                }
                selectDeviceItems.add(new SelectDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                deviceListFilter();
            }
        };
    }

    private void deviceListFilter() {

////                    if (deviceName.equals(getString(R.string.device_name_prime))) {
////                        selectDeviceItems.add(new MainDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
////                    }

        for (int i = 0; i < selectDeviceItems.size(); i++) {
            for (int j = 1; j < selectDeviceItems.size(); j++) {
                String tempStrI = selectDeviceItems.get(i).getExtraextraAddress().trim();
                String tempStrJ = selectDeviceItems.get(j).getExtraextraAddress().trim();
                if (tempStrI.equals(tempStrJ)) {
                    if (i != j) {
                        selectDeviceItems.remove(j);
                    }
                }
            }
        }
    }

    private void initBluetoothOn() {//블루투스 가동여부
        Toast.makeText(activity, R.string.bt_must_start, Toast.LENGTH_SHORT).show();
        Snackbar.make(view, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void startScan() {
        if (bluetoothAdapter.isEnabled()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();

                    if (bluetoothAdapter.isEnabled()) {
                        if (selectDeviceItems.size() < 1) {
                            noDeviceTextView.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        initBluetoothOn();
                    }
                }
            }, SCAN_PERIOD); //5초 뒤에 OFF

            //시작
            selectDeviceItems.clear();
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            isScanning = true;
            searchingProgressBar.setVisibility(View.VISIBLE);
            noDeviceTextView.setVisibility(View.INVISIBLE);

            if (isBuildVersionLM) {
                bleScanner.startScan(scanCallback);
            } else {
                bluetoothAdapter.startLeScan(leScanCallback);
            }
        } else {
            if (isScanning) {  //스캔 시작
                stopScan();
            }
            initBluetoothOn();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void stopScan() {

        //중지
        if (isBuildVersionLM) {
            bleScanner.stopScan(scanCallback);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
        swipeRefreshLayout.setRefreshing(false);
        isScanning = false;
        searchingProgressBar.setVisibility(View.INVISIBLE);
        noDeviceTextView.setVisibility(View.INVISIBLE);

    }

}
