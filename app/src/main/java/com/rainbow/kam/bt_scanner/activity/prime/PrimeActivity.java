package com.rainbow.kam.bt_scanner.activity.prime;

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
import android.support.annotation.Nullable;
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
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.fragment.prime.main.CalorieFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.main.DashboardFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.main.DistanceFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.main.SampleFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.main.StepFragment;
import com.rainbow.kam.bt_scanner.RealmItem.RealmBandItem;
import com.rainbow.kam.bt_scanner.RealmItem.RealmPatientItem;
import com.rainbow.kam.bt_scanner.tools.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.gatt.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class PrimeActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, GattCustomCallbacks {

    private static final String TAG = PrimeActivity.class.getSimpleName();

    private Realm realm;


    private String patientAge;
    private String patientHeight;
    private String deviceAddress;

    private final String[] weekSet = {"월", "화", "수", "목", "금", "토", "일",};
    private final String[] timeSet = {"년", "월", "일", "시", "분", "초"};

    private enum ListType {
        INIT, READ_TIME, READ_STEP_DATA, ETC, FINISH
    }

    private ListType listType = ListType.INIT;

    private DashboardFragment dashboardFragment;
    private StepFragment stepFragment;
    private CalorieFragment calorieFragment;
    private DistanceFragment distanceFragment;
    private SampleFragment sampleFragment;

    private GattManager gattManager;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPager;


    @DebugLog
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

        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.dashboard_device_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void setViewPager() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.nursing_tabs);
        DashBoardAdapter dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.nursing_viewpager);
        viewPager.setAdapter(dashBoardAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setupWithViewPager(viewPager);
    }


    @DebugLog
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
                BluetoothHelper.check(this);
            }
        }
    }


    @DebugLog
    @Override
    public void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @DebugLog
    @Override
    public void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @DebugLog
    @Override
    public void onRefresh() {
        if (gattManager.isConnected()) {
            disconnectDevice();
        } else {
            registerBluetooth();
        }
    }


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
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_nursing_dashboard:
                viewPager.setCurrentItem(0, true);
                return true;
            case R.id.menu_nursing_dashboard_step:
                viewPager.setCurrentItem(1, true);
                return true;
            case R.id.menu_nursing_dashboard_calorie:
                viewPager.setCurrentItem(2, true);
                return true;
            case R.id.menu_nursing_dashboard_distance:
                viewPager.setCurrentItem(3, true);
                return true;
            case R.id.menu_nursing_dashboard_sleep:
                viewPager.setCurrentItem(4, true);
                return true;
            case R.id.menu_nursing_info_user:
                return true;
            case R.id.menu_nursing_info_prime:
                return true;
            case R.id.menu_nursing_info_goal:
                return true;
            case R.id.menu_nursing_about_dev:
                finish();
                startActivity(new Intent(PrimeActivity.this, MainActivity.class));
                return true;
            case R.id.menu_nursing_about_setting:
                realm.beginTransaction();
                realm.clear(RealmPatientItem.class);
                realm.clear(RealmBandItem.class);
                realm.commitTransaction();
                finish();
                return true;
            case R.id.menu_nursing_about_about:
                return true;
            default:
                return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //블루투스 켜짐
                Snackbar.make(getWindow().getDecorView(), R.string.bt_on, Snackbar.LENGTH_SHORT).show();
            } else {
                //블루투스 에러
                Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
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


    @DebugLog
    private void registerBluetooth() {
        gattManager = new GattManager(this, this);
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            BluetoothHelper.initBluetoothOn(this);
        }
    }




    @DebugLog
    private void connectDevice() {
        if (!gattManager.isConnected()) {
            try {
                gattManager.connect(deviceAddress);
                listType = ListType.INIT;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                finish();
                startActivity(new Intent(PrimeActivity.this, PrimeInitialActivity.class));
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @DebugLog
    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
            listType = ListType.FINISH;
        }
    }


    @DebugLog
    private void readTime(byte[] characteristicValue) {
        StringBuilder result = new StringBuilder();
        for (int i = 2; i < characteristicValue.length - 1; i++) {  // 0 : Positive - Negative / 1 : Length / last index : checksum
            switch (i) {
                default:
                    result.append(Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    result.append(timeSet[i - 2]).append(" ");
                    break;
                case 8:
                    int j = characteristicValue[i];
                    result.append(weekSet[j - 1]);
                    break;
            }
        }
        dashboardFragment.setTime(result);
    }


    @DebugLog
    private void readStep(byte[] characteristicValue) {

        StringBuilder hexStep = new StringBuilder();
        StringBuilder hexCal = new StringBuilder();

        int step, kcal, distance, age;
        double height;

        hexStep.append(String.format("%02x", characteristicValue[2] & 0xff));
        hexStep.append(String.format("%02x", characteristicValue[3] & 0xff));
        hexStep.append(String.format("%02x", characteristicValue[4] & 0xff));
        step = Integer.parseInt(hexStep.toString(), 16);

        hexCal.append(String.format("%02x", characteristicValue[5] & 0xff));
        hexCal.append(String.format("%02x", characteristicValue[6] & 0xff));
        hexCal.append(String.format("%02x", characteristicValue[7] & 0xff));
        kcal = Integer.parseInt(hexCal.toString(), 16);

        age = Integer.parseInt(patientAge);
        height = Integer.parseInt(patientHeight);
        if (age <= 15 || age >= 65) {
            distance = (int) ((height * 0.37) * step) / 100;
        } else if (15 < age || age < 45) {
            distance = (int) ((height * 0.45) * step) / 100;
        } else if (45 <= age || age < 65) {
            distance = (int) ((height * 0.40) * step) / 100;
        } else {
            distance = (int) ((height * 0.30) * step) / 100;
        }

        saveRealmData(step, kcal, distance);

        dashboardFragment.setStepData(step, kcal, distance);
        stepFragment.setStep(step);
        calorieFragment.setCalorie(kcal);
        distanceFragment.setDist(distance);
        sampleFragment.setSample(distance);
    }


    @DebugLog
    private void saveRealmData(int step, int calorie, int distance) {

        realm.beginTransaction();
        RealmResults<RealmBandItem> results = realm.where(RealmBandItem.class).findAll();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String today = formatter.format(Calendar.getInstance().getTime());
        String lastDay;

        try {
            lastDay = results.get(results.size() - 1).getCalendar();
        } catch (Exception e) {
            lastDay = null;
        }
        if (lastDay != null && lastDay.equals(today)) {
            results.last().setCalendar(today);
            results.last().setStep(step);
            results.last().setCalorie(calorie);
            results.last().setDistance(distance);
        } else {
            RealmBandItem realmBandItem = realm.createObject(RealmBandItem.class);
            realmBandItem.setCalendar(today);
            realmBandItem.setStep(step);
            realmBandItem.setCalorie(calorie);
            realmBandItem.setDistance(distance);
        }
        Log.e("RealmBandItem", "results = " + "\n" +
                results.last().getStep() + "\n" +
                results.last().getCalorie() + "\n" +
                results.last().getDistance() + "\n" +
                results.last().getCalendar() + "\n"
        );

        realm.commitTransaction();
    }


    @Override
    public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Connected");

                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
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
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });
    }


    @DebugLog
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
                onDataNotify(null);
            }
        });
    }

    @DebugLog
    @Override
    public void onServicesNotFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onReadSuccess(BluetoothGattCharacteristic ch) {
        // Not use in this Activity
    }


    @DebugLog
    @Override
    public void onReadFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onDataNotify(@Nullable final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (listType) {
                        case INIT:
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_DEVICE_TIME());
                                }
                            }, 100); // Notify 콜백 메서드가 없으므로 강제로 기다린다.
                            listType = ListType.READ_TIME;
                            break;

                        case READ_TIME:
                            readTime(ch.getValue());

                            gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_STEP_DATA());

                            listType = ListType.READ_STEP_DATA;
                            break;

                        case READ_STEP_DATA:

                            readStep(ch.getValue());

                            listType = ListType.ETC;
                            break;

                        case ETC:
                            listType = ListType.FINISH;
                            break;
                        case FINISH:
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    onReadFail();
                }
            }
        });
    }


    @DebugLog
    @Override
    public void onWriteSuccess() {
    }


    @DebugLog
    @Override
    public void onWriteFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dashboardFragment.setFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onRSSIUpdate(final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarRssi.setText(rssi + "db");
            }
        });
    }


    @DebugLog
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

        private final String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "ETC"};
        final int PAGE_COUNT = tabTitles.length;


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
