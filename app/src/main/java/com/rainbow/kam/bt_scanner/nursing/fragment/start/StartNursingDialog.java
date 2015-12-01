package com.rainbow.kam.bt_scanner.nursing.fragment.start;

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
import android.support.annotation.Nullable;
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

import com.rainbow.kam.bt_scanner.adapter.main.MainDeviceAdapter;
import com.rainbow.kam.bt_scanner.adapter.main.MainDeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.nursing.adapter.SelectedDeviceAdapter;
import com.rainbow.kam.bt_scanner.nursing.adapter.SelectedDeviceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class StartNursingDialog extends DialogFragment {

    private final String TAG = "SelectDialog"; //로그용 태그
    private static final int REQUEST_ENABLE_BT = 1;
    private Activity activity;
    private Handler handler;
    private View view;

    private boolean isBuildVersionLM = false;
    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView selectDeviceRecyclerView;
    private RecyclerView.Adapter adapter = null;
    private ArrayList<SelectedDeviceItem> selectedDeviceItems = new ArrayList<>();

    ProgressBar searchingProgressBar;
    TextView noDeviceTextView;

    public StartNursingDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getDialog().getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nursing_start_add_device, container, false);
        activity = getActivity();

        handler = new Handler();

        searchingProgressBar = (ProgressBar) view.findViewById(R.id.nursing_device_progress);
        searchingProgressBar.setVisibility(View.INVISIBLE);

        noDeviceTextView = (TextView) view.findViewById(R.id.nursing_no_device_textview);
        noDeviceTextView.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.nursing_device_swipeRefreshLayout);

        selectDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.nursing_device_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        selectDeviceRecyclerView.setLayoutManager(layoutManager);
        selectDeviceRecyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isBuildVersionLM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

        try {
            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                throw new Exception();
            } else {

                if (isBuildVersionLM) {
                    bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                    if (bleScanner == null) {
                        throw new Exception();
                    } else {
                        setScannerL();
                    }
                } else {
                    setScanner();
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
        scanLeDevice(false);
    }

    public void scanLeDevice(final boolean enable) {//저전력 스캔
        if (enable) {   //시작중이면
            long SCAN_PERIOD = 5000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isBuildVersionLM) {
                        bleScanner.stopScan(scanCallback);
                    } else {
                        bluetoothAdapter.stopLeScan(leScanCallback);
                    }

                    isScanning = false;
                    searchingProgressBar.setVisibility(View.INVISIBLE);

                    if (selectedDeviceItems.size() < 1) {
                        noDeviceTextView.setVisibility(View.VISIBLE);
                    }
                    adapter = new SelectedDeviceAdapter(selectedDeviceItems, activity);
                    selectDeviceRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, SCAN_PERIOD); //10초 뒤에 OFF

            //시작
            if (isBuildVersionLM) {
                bleScanner.startScan(scanCallback);
            } else {
                bluetoothAdapter.startLeScan(leScanCallback);
            }

            isScanning = true;
            searchingProgressBar.setVisibility(View.VISIBLE);
            noDeviceTextView.setVisibility(View.INVISIBLE);


        } else {
            //중지
            if (isBuildVersionLM) {
                bleScanner.stopScan(scanCallback);
            } else {
                bluetoothAdapter.startLeScan(leScanCallback);
            }

            isScanning = false;
            searchingProgressBar.setVisibility(View.INVISIBLE);
            noDeviceTextView.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void startScan() {
        if (enableBluetooth()) {
            if (getScanning()) {  //스캔 시작
                scanLeDevice(false);
            } else { //재 스캔시(10초이내)
                selectedDeviceItems.clear();

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                scanLeDevice(true);
            }
        } else {
            Snackbar.make(view, "You must initialize Bluetooth", Snackbar.LENGTH_SHORT).show();
        }

    }

    public boolean getScanning() {//스캔중
        return isScanning;
    }

    public boolean enableBluetooth() {//블루투스 가동여부
        Log.d(TAG, "enableBluetooth");

        if (bluetoothAdapter.isEnabled()) { //블루투스 이미 켜짐
            Log.d(TAG, "Bluetooth isEnabled");

            return true;
        } else {    //블루투스 구동
            Log.d(TAG, "Bluetooth start");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
            return false;
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
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String deviceName = result.getDevice().getName();

                            if (deviceName == null) {
                                deviceName = "N/A";
                            }


                            if (deviceName.equals("Prime")) {
                                selectedDeviceItems.add(new SelectedDeviceItem(deviceName, result.getDevice().getAddress(), result.getDevice().getType(), result.getDevice().getBondState(), result.getRssi()));
                            }

                            for (int i = 0; i < selectedDeviceItems.size(); i++) {
                                for (int j = 1; j < selectedDeviceItems.size(); j++) {
                                    if (selectedDeviceItems.get(i).getExtraextraAddress().trim().equals(selectedDeviceItems.get(j).getExtraextraAddress().trim())) {
                                        if (i != j) {
                                            selectedDeviceItems.remove(j);
                                        }
                                    }

                                }

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "leScanCallback is Exception" + e.getMessage());
                        }
                    }
                });
            }
        };
    }

    private void setScanner() {
        Log.e(TAG, "setScanner");
        leScanCallback = new BluetoothAdapter.LeScanCallback()

        {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String deviceName = device.getName();

                            if (deviceName == null) {
                                deviceName = "N/A";

                            }

                            if (deviceName.equals("Prime")) {
                                selectedDeviceItems.add(new SelectedDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                            }

                            for (int i = 0; i < selectedDeviceItems.size(); i++) {
                                for (int j = 1; j < selectedDeviceItems.size(); j++) {
                                    if (selectedDeviceItems.get(i).getExtraextraAddress().trim().equals(selectedDeviceItems.get(j).getExtraextraAddress().trim())) {
                                        if (i != j) {
                                            selectedDeviceItems.remove(j);
                                        }
                                    }

                                }

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "leScanCallback is Exception" + e.getMessage());
                        }
                    }
                });
            }
        };
    }
}
