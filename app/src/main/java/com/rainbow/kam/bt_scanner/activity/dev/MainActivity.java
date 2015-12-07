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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.nurs.SplashNursingActivity;
import com.rainbow.kam.bt_scanner.adapter.dev.main.MainDeviceAdapter;
import com.rainbow.kam.bt_scanner.adapter.dev.main.MainDeviceItem;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final boolean isBuildVersionLM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    private static long SCAN_PERIOD = 5000;

    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinatorLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter = null;
    private ArrayList<MainDeviceItem> mainDeviceItemArrayList = new ArrayList<>();

    private FloatingActionButton fabSearch;
    private ProgressBar searchingProgressBar;
    private TextView noDeviceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionV21.check(this);

        setToolbar();
        setMaterialDesignView();
        setRecyclerView();
        setOtherView();

        setScannerCallback();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();

        try {
            // onCreate 에서 세팅시 pause/resume 사이에 bluetooth 를 꺼버리면 .....
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();

            mainDeviceItemArrayList.clear();
            adapter.notifyDataSetChanged();
            if (bluetoothAdapter == null) {
                throw new Exception();
            }

            if (isBuildVersionLM) {
                bleScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (bleScanner == null) {
                    throw new Exception();
                }
            }

            startScan();

        } catch (Exception e) {
            Toast.makeText(this, R.string.bt_fail, Toast.LENGTH_LONG).show();

            if (bluetoothAdapter == null) {
                Log.e(TAG, "BA null");
            }
            if (bluetoothManager == null) {
                Log.e(TAG, "BM null");
            }
        }
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
        startActivity(new Intent(this, SplashNursingActivity.class));
        drawerLayout.closeDrawer(GravityCompat.START);
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
                switch (resultCode) {
                    case RESULT_OK:
                        //블루투스 켜짐
                        break;
                    default:
                        //블루투스 에러
                        Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
                startScan();
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_request, Toast.LENGTH_SHORT).show();

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(myAppSettings, 0);

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
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new MainDeviceAdapter(mainDeviceItemArrayList, MainActivity.this);
        recyclerView.setAdapter(adapter);
    }

    private void setOtherView() {
        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(this);

        searchingProgressBar = (ProgressBar) findViewById(R.id.searching_progress_bar);
        searchingProgressBar.setVisibility(View.INVISIBLE);

        noDeviceTextView = (TextView) findViewById(R.id.no_device_textview);
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

                mainDeviceItemArrayList.add(new MainDeviceItem(deviceName, result.getDevice().getAddress(), result.getDevice().getType(), result.getDevice().getBondState(), result.getRssi()));
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
                mainDeviceItemArrayList.add(new MainDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                deviceListFilter();
            }
        };
    }

    private void deviceListFilter() {

////                    if (deviceName.equals(getString(R.string.device_name_prime))) {
////                        mainDeviceItemArrayList.add(new MainDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
////                    }

        for (int i = 0; i < mainDeviceItemArrayList.size(); i++) {
            for (int j = 1; j < mainDeviceItemArrayList.size(); j++) {
                String tempStrI = mainDeviceItemArrayList.get(i).getExtraextraAddress().trim();
                String tempStrJ = mainDeviceItemArrayList.get(j).getExtraextraAddress().trim();
                if (tempStrI.equals(tempStrJ)) {
                    if (i != j) {
                        mainDeviceItemArrayList.remove(j);
                    }
                }
            }
        }
    }

    private void initBluetoothOn() {//블루투스 가동여부
        Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();
        Snackbar.make(coordinatorLayout, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BT);
    }

    private void toggleScan() {
        if (bluetoothAdapter.isEnabled()) {

            if (isScanning) {  //스캔 시작
                stopScan();
            } else { //재 스캔시(10초이내)
                if (adapter != null) {
                    mainDeviceItemArrayList.clear();
                    adapter.notifyDataSetChanged();
                }
                startScan();
            }
        } else {
            if (isScanning) {  //스캔 시작
                stopScan();
            }
            initBluetoothOn();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void startScan() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();

                if (bluetoothAdapter.isEnabled()) {
                    if (mainDeviceItemArrayList.size() < 1) {
                        noDeviceTextView.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    initBluetoothOn();
                }
            }
        }, SCAN_PERIOD); //5초 뒤에 OFF

        //시작
        mainDeviceItemArrayList.clear();
        adapter.notifyDataSetChanged();
        isScanning = true;
        searchingProgressBar.setVisibility(View.VISIBLE);
        noDeviceTextView.setVisibility(View.INVISIBLE);

        if (isBuildVersionLM) {
            bleScanner.startScan(scanCallback);
        } else {
            bluetoothAdapter.startLeScan(leScanCallback);
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

        isScanning = false;
        searchingProgressBar.setVisibility(View.INVISIBLE);
        noDeviceTextView.setVisibility(View.INVISIBLE);

    }
}