package com.rainbow.kam.bt_scanner.activity.dev;

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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.nurse.SplashNursingActivity;
import com.rainbow.kam.bt_scanner.adapter.SelectDeviceAdapter;
import com.rainbow.kam.bt_scanner.adapter.SelectDeviceItem;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SelectDeviceAdapter.OnDeviceSelectListener {

    private final String TAG = getClass().getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;


    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar searchingProgressBar;
    private TextView noDeviceTextView;
    private RecyclerView.Adapter adapter = null;
    private final LinkedHashMap<String, SelectDeviceItem> selectDeviceItemLinkedHashMap = new LinkedHashMap<>();


    @Override
    protected void onStart() {
        super.onStart();
        if (PermissionV21.isBuildVersionLM) {
            PermissionV21.check(this);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
        setMaterialDesignView();
        setRecyclerView();
        setOtherView();

        setScannerCallback();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();
    }


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
        startActivity(new Intent(this, SplashNursingActivity.class));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                    Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
                } else {
                    //블루투스 에러
                    Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
            } else {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(myAppSettings, 0);

                Toast.makeText(getApplicationContext(), R.string.permission_request, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }

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
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinatorLayout);
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new SelectDeviceAdapter(selectDeviceItemLinkedHashMap, MainActivity.this);
        recyclerView.setAdapter(adapter);
    }


    private void setOtherView() {
        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(this);

        searchingProgressBar = (ProgressBar) findViewById(R.id.searching_progress_bar);
        searchingProgressBar.setVisibility(View.INVISIBLE);

        noDeviceTextView = (TextView) findViewById(R.id.no_device_textView);
        noDeviceTextView.setVisibility(View.INVISIBLE);
    }


    private void setScannerCallback() {
        if (PermissionV21.isBuildVersionLM) {
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
                BluetoothDevice bluetoothDevice = result.getDevice();

                String deviceName = bluetoothDevice.getName();
                if (deviceName == null) {
                    deviceName = "N/A";
                }

                if (!selectDeviceItemLinkedHashMap.containsKey(bluetoothDevice.getAddress())) {
                    selectDeviceItemLinkedHashMap.put(bluetoothDevice.getAddress(), new SelectDeviceItem(deviceName, bluetoothDevice.getAddress(), bluetoothDevice.getType(), bluetoothDevice.getBondState(), result.getRssi()));
                }
            }
        };
    }


    private void setScanner() {
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {
                String deviceName = device.getName();
                if (deviceName == null) {
                    deviceName = "N/A";
                }
                if (!selectDeviceItemLinkedHashMap.containsKey(device.getAddress())) {
                    selectDeviceItemLinkedHashMap.put(device.getAddress(), new SelectDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                }
            }
        };
    }


    @SuppressLint("NewApi")
    private void registerBluetooth() {
        try {
            bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothManager != null && bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {

                selectDeviceItemLinkedHashMap.clear();
                adapter.notifyDataSetChanged();

                if (PermissionV21.isBuildVersionLM) {
                    bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                }

                startScan();

            } else {
                initBluetoothOn();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.bt_fail, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void initBluetoothOn() {//블루투스 가동여부
        Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();
        Snackbar.make(coordinatorLayout, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BT);
    }


    private void toggleScan() {
        if (isScanning) {  //스캔 시작
            stopScan();
        } else { //재 스캔시(10초이내)
            registerBluetooth();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void startScan() {
        long SCAN_PERIOD = 5000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();

                if (selectDeviceItemLinkedHashMap.size() < 1) {
                    noDeviceTextView.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();

            }
        }, SCAN_PERIOD); //5초 뒤에 OFF

        //시작
        selectDeviceItemLinkedHashMap.clear();
        adapter.notifyDataSetChanged();
        isScanning = true;
        searchingProgressBar.setVisibility(View.VISIBLE);
        noDeviceTextView.setVisibility(View.INVISIBLE);

        if (PermissionV21.isBuildVersionLM) {
            if (bleScanner != null) {
                bleScanner.startScan(scanCallback);
            }
        } else {
            //noinspection deprecation
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void stopScan() {
        //중지
        if (PermissionV21.isBuildVersionLM) {
            if (bleScanner != null && bluetoothAdapter.isEnabled()) {
                bleScanner.stopScan(scanCallback);
            }

        } else {
            //noinspection deprecation
            bluetoothAdapter.stopLeScan(leScanCallback);
        }

        isScanning = false;

        searchingProgressBar.setVisibility(View.INVISIBLE);
        noDeviceTextView.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onDeviceSelect(String name, String address) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRAS_DEVICE_NAME, name);
        intent.putExtra(DetailActivity.EXTRAS_DEVICE_ADDRESS, address);
        startActivity(intent);
    }
}

