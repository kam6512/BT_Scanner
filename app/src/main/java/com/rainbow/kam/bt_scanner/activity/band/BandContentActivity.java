package com.rainbow.kam.bt_scanner.activity.band;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.development.MainActivity;
import com.rainbow.kam.bt_scanner.fragment.band.content.CalorieFragment;
import com.rainbow.kam.bt_scanner.fragment.band.content.DashboardFragment;
import com.rainbow.kam.bt_scanner.fragment.band.content.DistanceFragment;
import com.rainbow.kam.bt_scanner.fragment.band.content.SampleFragment;
import com.rainbow.kam.bt_scanner.fragment.band.content.StepFragment;
import com.rainbow.kam.bt_scanner.RealmItem.RealmBandItem;
import com.rainbow.kam.bt_scanner.RealmItem.RealmPatientItem;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.gatt.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class BandContentActivity extends AppCompatActivity implements GattCustomCallbacks {

    private static final String TAG = BandContentActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private Realm realm;


    private String patientAge;
    private String patientHeight;
    private String deviceAddress;

    private final String[] weekSet = {"월", "화", "수", "목", "금", "토", "일",};
    private final String[] timeSet = {"년", "월", "일", "시", "분", "초"};

    private enum ListType {
        READ_TIME, READ_STEP_DATA, ETC
    }

    private ListType listType = ListType.READ_TIME;

    private DashboardFragment dashboardFragment;
    private StepFragment stepFragment;
    private CalorieFragment calorieFragment;
    private DistanceFragment distanceFragment;
    private SampleFragment sampleFragment;

    private GattManager gattManager;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private CoordinatorLayout coordinatorLayout;
    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_band_main);

        setFragments();
        setToolbar();
        setMaterialNavigationView();
        setViewPager();

        queryUserInfo();
    }


    private void setFragments() {
        dashboardFragment = new DashboardFragment();
        stepFragment = new StepFragment();
        calorieFragment = new CalorieFragment();
        distanceFragment = new DistanceFragment();
        sampleFragment = new SampleFragment();
    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbarRssi = (TextView) findViewById(R.id.nursing_toolbar_rssi);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.nursing_toolbar_bluetoothFlag);
        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_white_24dp);

    }


    private void setMaterialNavigationView() {

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.dashboard_device_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (gattManager.isConnected()) {
                    disconnectDevice();
                } else {
                    connectDevice();
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (menuItem.getItemId()) {
                    case R.id.menu_nursing_dashboard:
                        viewPager.setCurrentItem(0, true);
                        Snackbar.make(coordinatorLayout, "nursing_dashboard", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_dashboard_step:
                        viewPager.setCurrentItem(1, true);
                        Snackbar.make(coordinatorLayout, "nursing_dashboard_step", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_dashboard_calorie:
                        viewPager.setCurrentItem(2, true);
                        Snackbar.make(coordinatorLayout, "nursing_dashboard_calorie", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_dashboard_distance:
                        viewPager.setCurrentItem(3, true);
                        Snackbar.make(coordinatorLayout, "nursing_dashboard_distance", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_dashboard_sleep:
                        viewPager.setCurrentItem(4, true);
                        Snackbar.make(coordinatorLayout, "nursing_dashboard_sleep", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_info_user:
                        Snackbar.make(coordinatorLayout, "nursing_info_user", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_info_prime:
                        Snackbar.make(coordinatorLayout, "nursing_info_prime", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_info_goal:
                        Snackbar.make(coordinatorLayout, "nursing_info_goal", Snackbar.LENGTH_LONG).show();
                        return true;
                    case R.id.menu_nursing_about_dev:
                        Snackbar.make(coordinatorLayout, "nursing_about_dev", Snackbar.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(BandContentActivity.this, MainActivity.class));
                        return true;
                    case R.id.menu_nursing_about_setting:
                        Snackbar.make(coordinatorLayout, "nursing_about_setting", Snackbar.LENGTH_LONG).show();

                        realm.beginTransaction();
                        realm.clear(RealmPatientItem.class);
                        realm.clear(RealmBandItem.class);
                        realm.commitTransaction();

                        Toast.makeText(BandContentActivity.this, "앱을 재시작합니다", Toast.LENGTH_LONG).show();
                        finish();
                        return true;
                    case R.id.menu_nursing_about_about:
                        Snackbar.make(coordinatorLayout, "nursing_about_about", Snackbar.LENGTH_LONG).show();
                        return true;

                    default:
                        return true;
                }
            }
        });
    }


    private void setViewPager() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.nursing_tabs);
        DashBoardAdapter dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.nursing_viewpager);
        viewPager.setAdapter(dashBoardAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }


            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }


    private void queryUserInfo() {
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());

            RealmResults<RealmPatientItem> results = realm.where(RealmPatientItem.class).findAll();

            RealmPatientItem realmPatientItem = results.get(0);
            patientAge = realmPatientItem.getAge();
            patientHeight = realmPatientItem.getHeight();
            deviceAddress = realmPatientItem.getDeviceAddress();

            Log.e("RealmPatientItem", "results = " + "\n" +
                    results.get(0).getName() + "\n" +
                    results.get(0).getAge() + "\n" +
                    results.get(0).getHeight() + "\n" +
                    results.get(0).getWeight() + "\n" +
                    results.get(0).getStep() + "\n" +
                    results.get(0).getGender() + "\n" +
                    results.get(0).getDeviceName() + "\n" +
                    results.get(0).getDeviceAddress()
            );

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PermissionV21.check(this);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (gattManager != null) {
            disconnectDevice();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                    Snackbar.make(getWindow().getDecorView(), R.string.bt_on, Snackbar.LENGTH_SHORT).show();
                } else {
                    //블루투스 에러
                    Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
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


    private void registerBluetooth() {
        if (gattManager == null) {
            gattManager = new GattManager(this, this);
        }
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            initBluetoothOn();
        }
    }


    private void initBluetoothOn() {//블루투스 가동여부

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    private void connectDevice() {
        if (!gattManager.isConnected()) {
            try {
                gattManager.connect(deviceAddress);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                finish();
                startActivity(new Intent(BandContentActivity.this, BandInitialActivity.class));
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    private void startDeviceWrite() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                listType = ListType.READ_TIME;
                byte[] dataToWrite = PrimeHelper.READ_DEVICE_TIME();
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
            }
        }, 1000); // Notify 콜백 메서드가 없으므로 강제로 기다린다.
    }


    private void loadNotifyData(BluetoothGattCharacteristic characteristic) {
        byte[] characteristicValue = characteristic.getValue();

        switch (listType) {
            case READ_TIME:
                readTime(characteristicValue);

                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_STEP_DATA());

                listType = ListType.READ_STEP_DATA;
                break;

            case READ_STEP_DATA:

                readStep(characteristicValue);

                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.SET_DEVICE_TIME_NOW());

                listType = ListType.ETC;
                break;

            case ETC:
                break;

            default:
                break;
        }
    }


    private void readTime(byte[] characteristicValue) {
        String result = "";
        for (int i = 2; i < characteristicValue.length - 1; i++) {  // 0 : Positive - Negative / 1 : Length / last index : checksum
            result += Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16);
            switch (i) {
                default:
                    result += timeSet[i - 2] + " ";
                    break;
                case 8:
                    result = result.substring(0, result.length() - 1);
                    int j = Integer.valueOf(String.format("%02x", characteristicValue[i]));
                    result += weekSet[j - 1];
                    break;
            }
        }
        dashboardFragment.setTime(result);
    }


    private void readStep(byte[] characteristicValue) {

        String hexStep = "";
        String hexCal = "";

        int step;
        int kcal;
        int distance;
        int age;

        for (int i = 0; i < characteristicValue.length; i++) {
            int lsb = characteristicValue[i] & 0xff;
            switch (i) {
                case 2:
                case 3:
                case 4:
                    hexStep += String.format("%02x", lsb);
                    break;
                case 5:
                case 6:
                case 7:
                    hexCal += String.format("%02x", lsb);
                    break;
            }
        }

        age = Integer.parseInt(patientAge);
        step = Integer.valueOf(hexStep, 16);
        kcal = Integer.valueOf(hexCal, 16);

        double height = Integer.parseInt(patientHeight);
        if (age <= 15 || age >= 65) distance = (int) ((height * 0.37) * step);
        else if (15 < age || age < 45) distance = (int) ((height * 0.45) * step);
        else if (45 <= age || age < 65) distance = (int) ((height * 0.40) * step);
        else distance = (int) ((height * 0.30) * step);

        distance = distance / 100;  //CM -> M

        addDataToRealmDB(step, kcal, distance);

        dashboardFragment.setStepData(step, kcal, distance);

        stepFragment.setStep(step);
        calorieFragment.setCalorie(kcal);
        distanceFragment.setDist(distance);
        sampleFragment.setSample(distance);
    }


    private void addDataToRealmDB(int step, int calorie, int distance) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String today = formatter.format(calendar.getTime());

        realm.beginTransaction();

        RealmResults<RealmBandItem> results = realm.where(RealmBandItem.class).findAll();
        if (results.size() != 0) {
            if (results.get(results.size() - 1).getCalendar() != null) {
                if (results.get(results.size() - 1).getCalendar().equals(today)) {
                    results.removeLast();
                }
                RealmBandItem realmBandItem = realm.createObject(RealmBandItem.class);
                realmBandItem.setCalendar(today);
                realmBandItem.setStep(step);
                realmBandItem.setCalorie(calorie);
                realmBandItem.setDistance(distance);
            }
        } else {
            RealmBandItem realmBandItem = realm.createObject(RealmBandItem.class);
            realmBandItem.setCalendar(today);
            realmBandItem.setStep(step);
            realmBandItem.setCalorie(calorie);
            realmBandItem.setDistance(distance);
        }
        realm.commitTransaction();
    }


    @SuppressWarnings("unused")
    //Realm 테스트 용 메서드
    private void readRealm() {
        RealmResults<RealmBandItem> results = realm.where(RealmBandItem.class).findAll();

        for (int i = 0; i < results.size(); i++) {
            Log.e(TAG, "band [" + i + "] : " + results.size() +
                    " step : " + results.get(i).getStep() +
                    " calorie : " + results.get(i).getCalorie() +
                    " distance : " + results.get(i).getDistance() +
                    " calendar : " + results.get(i).getCalendar());
        }
    }


    @Override
    public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Connected");

                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Disconnected");

                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                toolbarRssi.setText("--");
                if (swipeRefreshLayout.isRefreshing()) {
                    connectDevice();
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
    }


    @Override
    public void onServicesFound(final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothGattService bluetoothGattService = services.get(4); // 0xFFF0
                characteristicList = bluetoothGattService.getCharacteristics();
                bluetoothGattCharacteristicForWrite = characteristicList.get(0); // 0xFFF2
                bluetoothGattCharacteristicForNotify = characteristicList.get(1); // 0xFFF1

                gattManager.setNotification(bluetoothGattCharacteristicForNotify, true);
                startDeviceWrite();
            }
        });
    }


    @Override
    public void onServicesNotFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @Override
    public void onReadSuccess(BluetoothGattCharacteristic ch) {
        // Not use in this Activity
    }


    @Override
    public void onReadFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @Override
    public void onDataNotify(final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                try {
                loadNotifyData(ch);
//                } catch (Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    onReadFail();
//                }
            }
        });
    }


    @Override
    public void onWriteSuccess() {
    }


    @Override
    public void onWriteFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @Override
    public void onRSSIUpdate(final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarRssi.setText(rssi + "db");
            }
        });
    }


    @Override
    public void onRSSIMiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarRssi.setText("--");
            }
        });
    }


    private class DashBoardAdapter extends FragmentStatePagerAdapter {

        final int PAGE_COUNT = 5;
        private final String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "ETC"};


        public DashBoardAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return dashboardFragment;
                case 1:
                    return stepFragment;
                case 2:
                    return calorieFragment;
                case 3:
                    return distanceFragment;
                default:
                    return sampleFragment;
            }
        }


        @Override
        public int getCount() {
            return PAGE_COUNT;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
