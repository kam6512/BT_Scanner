package com.rainbow.kam.bt_scanner.activity.nurse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.rainbow.kam.bt_scanner.activity.dev.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.nurse.dashboard_NotInUse.DashboardItem;
import com.rainbow.kam.bt_scanner.fragment.nurse.main.CalorieFragment;
import com.rainbow.kam.bt_scanner.fragment.nurse.main.DashboardFragment;
import com.rainbow.kam.bt_scanner.fragment.nurse.main.DistanceFragment;
import com.rainbow.kam.bt_scanner.fragment.nurse.main.SampleFragment;
import com.rainbow.kam.bt_scanner.fragment.nurse.main.StepFragment;
import com.rainbow.kam.bt_scanner.patient.Band;
import com.rainbow.kam.bt_scanner.patient.Patient;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.gatt.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class MainNursingActivity extends AppCompatActivity implements GattCustomCallbacks {

    private static final String TAG = MainNursingActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private Realm realm;
    private Activity activity;

    private Handler handler;

    private String patientName = null;
    private String patientAge = null;
    private String patientHeight = null;
    private String patientWeight = null;
    private String patientStep = null;
    private String patientGender = null;

    private String deviceName = null;
    private String deviceAddress = null;

    private final String[] weekSet = {"월", "화", "수", "목", "금", "토", "일",};
    private final String[] timeSet = {"년", "월", "일", "시", "분", "초"};

    private boolean isGattProcessRunning = false;

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
    private Toolbar toolbar;
    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPager;
    private DashBoardAdapter dashBoardAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        activity = this;
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(activity).build());

            RealmResults<Patient> results = realm.where(Patient.class).findAll();

            Patient patient = results.get(0);
            patientName = patient.getName();
            patientAge = patient.getAge();
            patientHeight = patient.getHeight();
            patientWeight = patient.getWeight();
            patientStep = patient.getStep();
            if (patient.getGender().equals("남성")) {
                patientGender = "1";
            } else {
                patientGender = "0";
            }
            deviceName = patient.getDeviceName();
            deviceAddress = patient.getDeviceAddress();

            if (patientName == null || patientAge == null || patientHeight == null || patientWeight == null || patientStep == null || deviceName == null || deviceAddress == null) {
                throw new Exception("Check out the Realm");
            }


        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                PermissionV21.check(this);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_main);

        setFragments();
        setToolbar();
        setMaterialNavigationView();
        setViewPager();
    }

    private void setFragments() {
        dashboardFragment = new DashboardFragment();
        stepFragment = new StepFragment();
        calorieFragment = new CalorieFragment();
        distanceFragment = new DistanceFragment();
        sampleFragment = new SampleFragment();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
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

    @Override
    public void onResume() {
        super.onResume();
        registerBluetooth();
        Log.e(TAG, "onResume");
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
                switch (resultCode) {
                    case RESULT_OK:
                        connectDevice();
                        break;
                    default:
                        Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
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
        Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();
        Snackbar.make(coordinatorLayout, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    private void setMaterialNavigationView() {

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);

        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.dashboard_device_swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!gattManager.isConnected()) {
                        connectDevice();
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                }
            });
        }
        navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
        if (navigationView != null) {
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
                            startActivity(new Intent(MainNursingActivity.this, MainActivity.class));
                            return true;
                        case R.id.menu_nursing_about_setting:
                            Snackbar.make(coordinatorLayout, "nursing_about_setting", Snackbar.LENGTH_LONG).show();

                            realm = Realm.getInstance(activity);
                            realm.beginTransaction();
                            realm.clear(Patient.class);
                            realm.clear(Band.class);
                            realm.commitTransaction();
//                            Realm.deleteRealm(new RealmConfiguration.Builder(activity).build());
//                            if (!isGattProcessRunning) {
//                                byte[] dataToWrite;
//                                dataToWrite = PrimeHelper.CLEAR_DATA();
//                                gattManager.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
//                            }

                            Toast.makeText(MainNursingActivity.this, "앱을 재시작합니다", Toast.LENGTH_LONG).show();
                            System.exit(0);
                            return true;
                        case R.id.menu_nursing_about_about:
                            Snackbar.make(coordinatorLayout, "nursing_about_about", Snackbar.LENGTH_LONG).show();
                            realm.beginTransaction();
                            realm.clear(Band.class);
                            realm.commitTransaction();
                            return true;

                        default:
                            return true;
                    }
                }
            });
        }
    }


    private void setViewPager() {
        tabLayout = (TabLayout) findViewById(R.id.nursing_tabs);
        dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager());
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


    private void connectDevice() {

        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (!gattManager.isConnected() || isFinishing()) {
                    if (gattManager.isBluetoothAvailable()) {
                        try {
                            gattManager.connect(deviceAddress);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        handler.postDelayed(this, 3000);
                    } else {
                        initBluetoothOn();
                    }
                    Log.e(TAG, "runnable");
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void disconnectDevice() {
        if (gattManager != null) {
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
        byte[] dataToWrite;
        switch (listType) {
            case READ_TIME:

                readTime(characteristicValue);

                dataToWrite = PrimeHelper.READ_STEP_DATA();
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);

                isGattProcessRunning = true;
                listType = ListType.READ_STEP_DATA;
                break;

            case READ_STEP_DATA:

                readStep(characteristicValue);

                dataToWrite = PrimeHelper.SET_DEVICE_TIME_NOW();
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);

                isGattProcessRunning = true;
                listType = ListType.ETC;
                break;

            case ETC:
                for (int i = 0; i < characteristicValue.length; i++) {
                    int lsb = characteristic.getValue()[i] & 0xff;
                    Log.e("ETC", "characteristicValue = " + Integer.toHexString(characteristicValue[i]) + " / lsb = " + Integer.toHexString(lsb));
                }
//                dataToWrite = PrimeHelper.CALL_DEVICE();
//                gattManager.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
                isGattProcessRunning = false;
                break;

            default:
                isGattProcessRunning = false;
                break;
        }
    }


    private void readTime(byte[] characteristicValue) {
        String result = "";
        for (int i = 0; i < characteristicValue.length; i++) {
            if (i > 1 && i != characteristicValue.length - 1) { // 0 : Positive - Negative / 1 : Length / last index : checksum
                result += Integer.valueOf(PrimeHelper.setWidth(Integer.toHexString(characteristicValue[i])), 16);
                switch (i) {
                    default:
                        result += timeSet[i - 2] + " ";
                        break;
                    case 8:
                        result = result.substring(0, result.length() - 1);
                        int j = Integer.valueOf(PrimeHelper.setWidth(Integer.toHexString(characteristicValue[i])), 16);
                        result += weekSet[j - 1];
                        break;
                }
            }
        }
        dashboardFragment.setTime(result);

    }


    private void readStep(byte[] characteristicValue) {

        String hexStep = "";
        String hexCal = "";
        String hexDist = "";

        int distance;
        int step = 0;
        int age;

        for (int i = 2; i < characteristicValue.length; i++) {
            int lsb = characteristicValue[i] & 0xff;
            switch (i) {
                case 2:
                case 3:
                case 4:
                    hexStep += PrimeHelper.setWidth(Integer.toHexString(lsb));
                    break;
                case 5:
                case 6:
                case 7:
                    hexCal += PrimeHelper.setWidth(Integer.toHexString(lsb));
                    break;
                case 10:
                    step = Integer.valueOf(hexStep, 16);
                    age = Integer.parseInt(patientAge);
                    double height = Integer.parseInt(patientHeight);
                    if (age <= 15 || age >= 65) distance = (int) ((height * 0.37) * step);
                    else if (15 < age || age < 45) distance = (int) ((height * 0.45) * step);
                    else if (45 <= age || age < 65) distance = (int) ((height * 0.40) * step);
                    else distance = (int) ((height * 0.30) * step);

                    hexDist = String.valueOf(distance / 100);  //CM -> M
                    break;
            }
        }

        addDataToRealmDB(step, Integer.valueOf(hexCal), Integer.valueOf(hexDist));

        Bundle bundle = new Bundle();
        bundle.putString("STEP", hexStep);
        bundle.putString("CALORIE", hexCal);
        bundle.putString("DISTANCE", hexDist);
        dashboardFragment.setStepData(bundle);

        stepFragment.setStep(step);
        calorieFragment.setCalorie(Integer.valueOf(hexCal));
        distanceFragment.setDist(Integer.valueOf(hexDist));
        sampleFragment.setSample(Integer.valueOf(hexDist));
    }


    private void addDataToRealmDB(int step, int calorie, int distance) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String today = formatter.format(calendar.getTime());

//        realm = Realm.getInstance(new RealmConfiguration.Builder(activity).build());
        realm.beginTransaction();

        RealmResults<Band> results = realm.where(Band.class).findAll();
        if (results.size() != 0) {
            if (results.get(results.size() - 1).getCalendar() != null) {
                if (results.get(results.size() - 1).getCalendar().equals(today)) {
                    Log.e(TAG, "calendar : " + results.get(results.size() - 1).getCalendar() + " // " + today);
                    results.removeLast();
                }
                Band band = realm.createObject(Band.class);
                band.setCalendar(today);
                band.setStep(step);
                band.setCalorie(calorie);
                band.setDistance(distance);
            }
        } else {
            Band band = realm.createObject(Band.class);
            band.setCalendar(today);
            band.setStep(step);
            band.setCalorie(calorie);
            band.setDistance(distance);
        }
        realm.commitTransaction();

        results = realm.where(Band.class).findAll();

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ArrayList<DashboardItem> arrayList = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Log.e(TAG, "band [" + i + "] : " + results.size() + " step : " + results.get(i).getStep() + " calorie : " + results.get(i).getCalorie() + " distance : " + results.get(i).getDistance() + " calendar : " + results.get(i).getCalendar());
            arrayList.add(new DashboardItem(results.get(i).getStep(), results.get(i).getCalorie(), results.get(i).getDistance(), results.get(i).getCalendar()));
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Disconnected");

                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                        toolbarRssi.setText("No Signal");

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 100);
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

    }

    @Override
    public void onReadSuccess(BluetoothGattCharacteristic ch) {

    }

    @Override
    public void onReadFail() {

    }

    @Override
    public void onDataNotify(final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadNotifyData(ch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onWriteSuccess() {
        isGattProcessRunning = false;

    }

    @Override
    public void onWriteFail() {
        isGattProcessRunning = false;
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
                    if (!dashboardFragment.isAdded()) {
                        return dashboardFragment;
                    }
                case 1:
                    if (!stepFragment.isAdded()) {
                        return stepFragment;
                    }
                case 2:
                    if (!calorieFragment.isAdded()) {
                        return calorieFragment;
                    }
                case 3:
                    if (!distanceFragment.isAdded()) {
                        return distanceFragment;
                    }
                default:
                    if (!sampleFragment.isAdded()) {
                        return sampleFragment;
                    }
            }
            return sampleFragment;
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
