package com.rainbow.kam.bt_scanner.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.net.Uri;
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

import com.rainbow.kam.bt_scanner.adapter.main.DeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BleActivityManager;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements  View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "MainActivity"; //로그용 태그
    private static final int REQUEST_ENABLE_BT = 1; //result 플래그

    private BluetoothManager bluetoothManager;  //블루투스 매니저
    private BluetoothAdapter bluetoothAdapter;  //블루투스 어댑터

    public static Handler handler;  //핸들러 - Find 메세지 핸들링

    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinatorLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter = null; //리사이클러 어댑터
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<>(); //어댑터 데이터 클래스(틀)

    private FloatingActionButton fabSync;
    private ProgressBar progressBar;
    private TextView hasCard;

    private BleActivityManager bleActivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
        setMaterialDesignView();
        setRecyclerView();
        setOtherView();

        //핸들러 초기화
        handler = new Handler();


        bleActivityManager = new BleActivityManager(TAG, this, handler, bluetoothAdapter, bluetoothManager, null, recyclerView, adapter, deviceItemArrayList, coordinatorLayout, progressBar, hasCard, false);
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
        fabSync = (FloatingActionButton) findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.main_progress);
        progressBar.setVisibility(View.INVISIBLE);

        hasCard = (TextView) findViewById(R.id.hasCard);
        hasCard.setVisibility(View.INVISIBLE);
    }

    private boolean enableBluetooth() {//블루투스 가동여부
        Log.d(TAG, "enableBluetooth");

        if (bluetoothAdapter.isEnabled()) { //블루투스 이미 켜짐
            Log.d(TAG, "Bluetooth isEnabled");

            return true;
        } else {    //블루투스 구동
            Log.d(TAG, "Bluetooth start");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BT);
            return false;
        }
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
                        Log.d(TAG, "Bluetooth is not enabled");
                        finish();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleActivityManager.onResume();
    }

    @Override
    protected void onDestroy() { //꺼짐
        super.onDestroy();
        bleActivityManager.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_sync: //블루투스 기기 찾기(4.0)
                if (enableBluetooth()) {
                    if (bleActivityManager.getScanning()) {  //스캔 시작
                        bleActivityManager.scanLeDevice(false);
                    } else { //재 스캔시(10초이내)
                        deviceItemArrayList.clear();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        bleActivityManager.scanLeDevice(true);
                    }
                } else {
                    Snackbar.make(coordinatorLayout, "You musst initalize Bluetooth", Snackbar.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            case R.id.nav_scan:
                Snackbar.make(getWindow().getDecorView(), "Scanner Layout", Snackbar.LENGTH_LONG).show();
                return true;
            case R.id.nav_nursing:
                finish();
                return true;
            case R.id.nav_link1:
                Intent browser1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.android.com/"));
                startActivity(browser1);
                return true;
            case R.id.nav_link2:
                Intent browser2 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://developer.android.com/"));
                startActivity(browser2);
                return true;
            default:
                return true;
        }
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
}