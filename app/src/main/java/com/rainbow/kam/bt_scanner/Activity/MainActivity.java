package com.rainbow.kam.bt_scanner.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
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
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Adapter.MainAdapter.DeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleActivityManager;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity"; //로그용 태그
    private static final int REQUEST_ENABLE_BT = 1; //result 플래그

    private BluetoothManager bluetoothManager;  //블루투스 매니저
    private BluetoothAdapter bluetoothAdapter;  //블루투스 어댑터

    public static Handler handler;  //핸들러 - Find 메세지 핸들링

    private DrawerLayout drawerLayout;

    private CoordinatorLayout coordinatorLayout;

    private RecyclerView.Adapter adapter = null; //리사이클러 어댑터
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<>(); //어댑터 데이터 클래스(틀)


    private BleActivityManager bleActivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //툴바 적용
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        initBluetooth();

        //핸들러 초기화
        handler = new Handler();

        FloatingActionButton fabSync = (FloatingActionButton) findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(this);


        ProgressBar progressBar = (ProgressBar) findViewById(R.id.main_progress);
        progressBar.setVisibility(View.INVISIBLE);

        TextView hasCard = (TextView) findViewById(R.id.hasCard);
        hasCard.setVisibility(View.INVISIBLE);

        //리사이클러 그룹 초기화
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinatorLayout);

        bleActivityManager = new BleActivityManager(TAG, this, handler, bluetoothAdapter, bluetoothManager, recyclerView, adapter, deviceItemArrayList, coordinatorLayout, progressBar, hasCard, false);

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
                        Log.d(TAG, "Bluetooth is not enabled");
                        //블루투스 에러
                        break;
                }
                break;
        }
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.nav_scan:
                                Snackbar.make(getWindow().getDecorView(), "Scanner Layout", Snackbar.LENGTH_LONG).show();
                                return true;
                            case R.id.nav_nursing:
//                                Intent startNursing = new Intent(MainActivity.this, MainNursingActivity.class);
//                                startActivity(startNursing);
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
                });
    }

    private void initBluetooth() {
        try {
            //블루투스 매니저/어댑터 초기화
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                throw new Exception();

            }
        } catch (Exception e) {
            Toast.makeText(this, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    public boolean enableBluetooth() {//블루투스 가동여부
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
    public void onClick(View v) { //버튼 클릭 리스너
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
