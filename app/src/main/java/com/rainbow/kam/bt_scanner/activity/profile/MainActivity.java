package com.rainbow.kam.bt_scanner.activity.profile;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeInitialActivity;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.tools.BluetoothHelper;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, DeviceAdapter.OnDeviceSelectListener {
    private final String TAG = getClass().getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private DrawerLayout drawerLayout;
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


    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        if (BluetoothHelper.isBuildVersionLM) {
            BluetoothHelper.check(this);
        }

        setToolbar();
        setMaterialDesignView();
        setRecyclerView();
        setOtherView();

        setScannerCallback();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    }


    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @DebugLog
    @Override
    protected void onPause() { //꺼짐
        super.onPause();
        stopScan();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabSearch:
                toggleScan();
                break;
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(new Intent(this, PrimeInitialActivity.class));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        if (isScanning) {  //스캔 시작
            stopScan();
        } else { //재 스캔시(10초이내)
            registerBluetooth();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onActivityResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    private void setMaterialDesignView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }


    private void setRecyclerView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        deviceAdapter = new DeviceAdapter(this);
        recyclerView.setAdapter(deviceAdapter);
    }


    private void setOtherView() {
        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(this);

        noDeviceTextView = (TextView) findViewById(R.id.no_device_textView);
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


    @SuppressLint("NewApi")
    private void registerBluetooth() {
        try {
            bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothManager != null && bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {

                if (BluetoothHelper.isBuildVersionLM) {
                    bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                }

                startScan();

            } else {
                initBluetoothOn();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.bt_fail, Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
        }
    }


    @DebugLog
    private void initBluetoothOn() {//블루투스 가동여부

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    @DebugLog
    private void toggleScan() {
        if (isScanning) {
            stopScan();
        } else { //재 스캔시(10초이내)
            registerBluetooth();
        }
    }


    @DebugLog
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void startScan() {
        long SCAN_PERIOD = 5000;
        handler.postDelayed(runnable, SCAN_PERIOD); //5초 뒤에 OFF

        deviceAdapter.clear();
        isScanning = true;
        noDeviceTextView.setVisibility(View.INVISIBLE);

        swipeRefreshLayout.post(postSwipeRefresh);

        //시작
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


    @Override
    public void onDeviceSelect(String name, String address) {
        Intent intent = new Intent(this, DeviceProfileActivity.class);
        intent.putExtra(DeviceProfileActivity.EXTRAS_DEVICE_NAME, name);
        intent.putExtra(DeviceProfileActivity.EXTRAS_DEVICE_ADDRESS, address);
        startActivity(intent);
    }


}

