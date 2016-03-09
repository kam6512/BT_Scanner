package com.rainbow.kam.bt_scanner.fragment.device;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
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
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceItem;
import com.rainbow.kam.bt_scanner.activity.nursing.NursingActivity;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-02-17.
 */
public class DeviceListFragment extends DialogFragment implements SwipeRefreshLayout.OnRefreshListener, DialogInterface.OnCancelListener {
    private final String TAG = getClass().getSimpleName();

    private static final String[] AVAILABLE_DEVICE = {"X6S", "Prime"};
    private static final int SCAN_PERIOD = 5000;

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
    private final Runnable stop = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };

    private Observer<DeviceItem> deviceItemObserver = new Observer<DeviceItem>() {
        @Override
        public void onCompleted() {
        }


        @Override
        public void onError(Throwable e) {
            stopScan();
            noDeviceTextView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onNext(DeviceItem deviceItem) {
            if (context instanceof MainActivity) {
                deviceAdapter.addDevice(deviceItem);
            } else if (context instanceof NursingActivity) {
                String deviceName = deviceItem.getExtraName();
                if (deviceName != null && (deviceName.contains(AVAILABLE_DEVICE[0]) || deviceName.contains(AVAILABLE_DEVICE[1]))) {
                    deviceAdapter.addDevice(deviceItem);
                }
            }
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_device_list, container, false);
        if (BluetoothHelper.IS_BUILD_VERSION_LM) {
            if (context instanceof MainActivity) {
                BluetoothHelper.requestBluetoothPermission((MainActivity) context);
            } else if (context instanceof NursingActivity) {
                BluetoothHelper.requestBluetoothPermission((NursingActivity) context);
                setDialogSetting();
            }
        }

        setRecyclerView();
        setOtherView();

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDeviceObservable()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.computation())
                .subscribe(deviceItemObserver);
    }


    @Override
    public void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @Override
    public void onPause() { //꺼짐
        super.onPause();
        stopScan();

    }


    @Override
    public void onRefresh() {
        if (isScanning) {  //스캔 시작
            stopScan();
        } else { //재 스캔시(10초이내)
            registerBluetooth();
        }
    }


    private void setDialogSetting() {
        Dialog deviceListDialog = getDialog();
        Window window = deviceListDialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        deviceListDialog.setCanceledOnTouchOutside(false);
        deviceListDialog.setCancelable(true);
        deviceListDialog.setOnCancelListener(this);
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        deviceAdapter.cancel();
    }


    private void setRecyclerView() {
        RecyclerView selectDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.prime_device_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        selectDeviceRecyclerView.setLayoutManager(layoutManager);
        selectDeviceRecyclerView.setHasFixedSize(true);
        deviceAdapter = new DeviceAdapter(context);
        selectDeviceRecyclerView.setAdapter(deviceAdapter);
    }


    private void setOtherView() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.prime_device_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        noDeviceTextView = (TextView) view.findViewById(R.id.prime_no_device_textView);
        noDeviceTextView.setVisibility(View.INVISIBLE);
    }


    @SuppressLint("NewApi")
    private void registerBluetooth() {
        try {
            bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothAdapter.isEnabled() && bluetoothManager != null && bluetoothAdapter != null) {

                if (BluetoothHelper.IS_BUILD_VERSION_LM) {
                    bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                }

                startScan();
            } else {
                if (context instanceof MainActivity) {
                    BluetoothHelper.requestBluetoothEnable((MainActivity) context);
                } else if (context instanceof NursingActivity) {
                    BluetoothHelper.requestBluetoothEnable((NursingActivity) context);
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, R.string.bt_fail, Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void startScan() {
        handler.postDelayed(stop, SCAN_PERIOD); //5초 뒤에 OFF

        isScanning = true;
        deviceAdapter.clear();

        noDeviceTextView.setVisibility(View.INVISIBLE);

        swipeRefreshLayout.post(postSwipeRefresh);

        //시작
        if (BluetoothHelper.IS_BUILD_VERSION_LM) {
            if (bleScanner != null && bluetoothAdapter.isEnabled()) {
                bleScanner.startScan(scanCallback);
            }
        } else {
            //noinspection deprecation
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void stopScan() {
        handler.removeCallbacks(stop);

        isScanning = false;

        if (deviceAdapter.getItemCount() < 1) {
            noDeviceTextView.setVisibility(View.VISIBLE);
        } else {
            noDeviceTextView.setVisibility(View.INVISIBLE);
        }

        swipeRefreshLayout.setRefreshing(false);

        //중지
        if (BluetoothHelper.IS_BUILD_VERSION_LM) {
            if (bleScanner != null && bluetoothAdapter.isEnabled()) {
                bleScanner.stopScan(scanCallback);
            }
        } else {
            //noinspection deprecation
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Observable<DeviceItem> getDeviceObservable() {
        return Observable.create(subscriber -> {
            if (BluetoothHelper.IS_BUILD_VERSION_LM) {
                scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        if (result != null && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(new DeviceItem(result.getDevice(), result.getRssi()));
                        }
                    }


                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult result : results) {
                            if (result != null && !subscriber.isUnsubscribed()) {
                                subscriber.onNext(new DeviceItem(result.getDevice(), result.getRssi()));
                            }
                        }
                    }


                    @Override
                    public void onScanFailed(int errorCode) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        stopScan();
                    }
                };
            } else {
                if (subscriber.isUnsubscribed()) {
                    return;
                }
                leScanCallback = (device, rssi, scanRecord) -> subscriber.onNext(new DeviceItem(device, rssi));
            }
        });
    }
}
