package com.rainbow.kam.bt_scanner.activity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.rainbow.kam.bt_scanner.adapter.main.MainDeviceAdapter;
import com.rainbow.kam.bt_scanner.adapter.main.MainDeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private boolean isBuildVersionLM = false;
    private boolean isScanning;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    public static Handler handler;

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

        handler = new Handler();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isBuildVersionLM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

        try {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

                startScan();
            }

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
    protected void onDestroy() { //꺼짐
        super.onDestroy();
        scanLeDevice(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabSearch:
                if (enableBluetooth()) {

                    if (isScanning) {  //스캔 시작
                        scanLeDevice(false);
                    } else {
                        scanLeDevice(true);
                        mainDeviceItemArrayList.clear();
                        if (adapter != null) adapter.notifyDataSetChanged();
                    }
                } else {
                    Snackbar.make(coordinatorLayout, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.END);
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
    }

    private void setOtherView() {
        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(this);

        searchingProgressBar = (ProgressBar) findViewById(R.id.searching_progress_bar);
        searchingProgressBar.setVisibility(View.INVISIBLE);

        noDeviceTextView = (TextView) findViewById(R.id.no_device_textview);
        noDeviceTextView.setVisibility(View.INVISIBLE);
    }

    private boolean enableBluetooth() {//블루투스 가동여부

        if (bluetoothAdapter.isEnabled()) { //블루투스 이미 켜짐
            return true;
        } else {    //블루투스 구동
            Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BT);
            return false;
        }
    }

    public void startScan() {
        if (enableBluetooth()) {
            if (isScanning) {  //스캔 시작
                scanLeDevice(false);
            } else { //재 스캔시(10초이내)
                mainDeviceItemArrayList.clear();

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                scanLeDevice(true);
            }
        } else {
            Snackbar.make(coordinatorLayout, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();
        }
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

                    if (mainDeviceItemArrayList.size() < 1) {
                        noDeviceTextView.setVisibility(View.VISIBLE);
                    }
                    adapter = new MainDeviceAdapter(mainDeviceItemArrayList, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
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
                try {
                    String deviceName = result.getDevice().getName();

                    if (deviceName == null) {
                        deviceName = "N/A";
                    }

                    if (deviceName.equals("Prime")) {
                        mainDeviceItemArrayList.add(new MainDeviceItem(deviceName, result.getDevice().getAddress(), result.getDevice().getType(), result.getDevice().getBondState(), result.getRssi()));
                    }

                    for (int i = 0; i < mainDeviceItemArrayList.size(); i++) {
                        for (int j = 1; j < mainDeviceItemArrayList.size(); j++) {
                            if (mainDeviceItemArrayList.get(i).getExtraextraAddress().trim().equals(mainDeviceItemArrayList.get(j).getExtraextraAddress().trim())) {
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
                } catch (Exception e) {
                    Log.e(TAG, "leScanCallback is Exception" + e.getMessage());
                }
            }
        };
    }

    private void setScanner() {
        Log.e(TAG, "setScanner");
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {
                try {
                    String deviceName = device.getName();

                    if (deviceName == null) {
                        deviceName = "N/A";
                    }

                    if (deviceName.equals(getString(R.string.device_name_prime))) {
                        mainDeviceItemArrayList.add(new MainDeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                    }

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
                } catch (Exception e) {
                    Log.e(TAG, "leScanCallback is Exception" + e.getMessage());
                }
            }
        };
    }


}