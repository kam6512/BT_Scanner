package com.rainbow.kam.bt_scanner.activity.nurs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.dev.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.nurs.dashboard_NotInUse.DashboardItem;
import com.rainbow.kam.bt_scanner.fragment.nurs.main.CalorieFragment;
import com.rainbow.kam.bt_scanner.fragment.nurs.main.DashboardFragment;
import com.rainbow.kam.bt_scanner.fragment.nurs.main.DistanceFragment;
import com.rainbow.kam.bt_scanner.fragment.nurs.main.SampleFragment;
import com.rainbow.kam.bt_scanner.fragment.nurs.main.StepFragment;
import com.rainbow.kam.bt_scanner.patient.Band;
import com.rainbow.kam.bt_scanner.patient.Patient;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;
import com.rainbow.kam.bt_scanner.tools.ble.BLE;
import com.rainbow.kam.bt_scanner.tools.ble.BleHelper;
import com.rainbow.kam.bt_scanner.tools.ble.BleUiCallbacks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class MainNursingActivity extends AppCompatActivity implements BleUiCallbacks {

    public static final String TAG = MainNursingActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private Realm realm;
    private Activity activity;

    private Handler handler;
    private Runnable runnable;

    private String patientName = null;
    private String patientAge = null;
    private String patientHeight = null;
    private String patientWeight = null;
    private String patientStep = null;
    private String patientGender = null;

    private String deviceName = null;
    private String deviceAddress = null;

    private String[] weekSet = {"월", "화", "수", "목", "금", "토", "일",};
    private String[] timeSet = {"년", "월", "일", "시", "분", "초"};

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

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private BLE ble;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private MaterialDialog materialDialog;
    private TextView materialContent;
    private int count = 0;

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
            realm = Realm.getInstance(activity);

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
            PermissionV21.check(this);
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
        initDialog();

    }

    @Override
    public void onResume() {
        super.onResume();
        registerBluetooth();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ble != null) {
            disconnectDevice();
        }
    }

    @Override
    public void onBackPressed() {
        if (ble != null) {
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
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
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

    public void registerBluetooth() {

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Toast.makeText(this, R.string.bt_fail, Toast.LENGTH_LONG).show();
            finish();
        } else {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter.isEnabled()) {
                connectDevice();
            } else {
                initBluetoothOn();
            }
        }
    }

    private void initBluetoothOn() {//블루투스 가동여부
        Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();
        Snackbar.make(coordinatorLayout, R.string.bt_must_start, Snackbar.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
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

    private void setMaterialNavigationView() {

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);

        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.dashboard_device_swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    connectDevice();
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
//                                dataToWrite = BleHelper.CLEAR_DATA();
//                                ble.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
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

    private void initDialog() {

        materialDialog = new MaterialDialog.Builder(this).title(R.string.device_disconnected).content(R.string.reconnect).positiveText(R.string.reconnect_accept).negativeText(R.string.exit)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        registerBluetooth();
                        materialContent.setText(R.string.connecting);

                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        dialog.dismiss();
                        finish();

                    }
                }).showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        materialContent.setText(R.string.reconnect);
                    }
                }).autoDismiss(false).cancelable(false).build();
//        });
        materialContent = materialDialog.getContentView();
    }

    private void connectDevice() {
        if (ble == null) {
            ble = new BLE(this, this);
        }
        if (!ble.initialize()) {
            finish();
        }
        ble.connect(deviceAddress);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (ble.isConnected() || isFinishing()) {
                    count = 0;

                } else {
                    count++;
                    materialContent.setText("연결 실패, 다시 연결 중... " + count + "회 재시도");

                    if (ble == null) {
                        ble = new BLE(MainNursingActivity.this, MainNursingActivity.this);
                    }
                    if (!ble.initialize()) {
                        finish();
                    }
                    ble.connect(deviceAddress);
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void disconnectDevice() {
        if (ble != null) {
            ble.stopMonitoringRssiValue();
            ble.disconnect();
            ble.close();
        }
    }

    private void loadNotifyData(BluetoothGattCharacteristic characteristic) {
        byte[] characteristicValue = characteristic.getValue();
        byte[] dataToWrite;
        switch (listType) {
            case READ_TIME:

                readTime(characteristicValue);

                listType = ListType.READ_STEP_DATA;
                dataToWrite = BleHelper.READ_STEP_DATA(8);
                ble.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
                isGattProcessRunning = true;
                break;

            case READ_STEP_DATA:

                readStep(characteristicValue);

                listType = ListType.ETC;
                dataToWrite = BleHelper.CALL_DEVICE();
                ble.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
                isGattProcessRunning = true;

                break;

            case ETC:
                for (int i = 0; i < characteristicValue.length; i++) {
                    int lsb = characteristic.getValue()[i] & 0xff;
                    Log.e("noty", "characteristicValue = " + Integer.toHexString(characteristicValue[i]) + " / lsb = " + Integer.toHexString(lsb));
                }
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
            if (i > 1 && i != characteristicValue.length - 1) {
                result += Integer.valueOf(BleHelper.setWidth(Integer.toHexString(characteristicValue[i])), 16);
                switch (i) {
                    default:
                        result += timeSet[i - 2] + " ";

                        break;
                    case 8:
                        result = result.substring(0, result.length() - 1);

                        int j = Integer.valueOf(BleHelper.setWidth(Integer.toHexString(characteristicValue[i])), 16);
                        result += weekSet[j - 1];
                        break;
                }
            }
        }
        dashboardFragment.setTime(result);

    }

    private void readStep(byte[] characteristicValue) {
        String result = "";

        String hexStep = "";
        String hexCalo = "";
        String hexDist = "";

        int distance;
        int step = 0;
        int age;

        for (int i = 0; i < characteristicValue.length; i++) {

            int lsb = characteristicValue[i] & 0xff;

            if (i > 1 && i != characteristicValue.length - 1) {

                result += Integer.valueOf(BleHelper.setWidth(Integer.toHexString(lsb)), 16) + " / ";

                switch (i) {
                    case 2:
                    case 3:
                    case 4:
                        hexStep += BleHelper.setWidth(Integer.toHexString(lsb));
                        break;

                    case 5:
                    case 6:
                    case 7:
                        hexCalo += BleHelper.setWidth(Integer.toHexString(lsb));
                        break;
                    case 10:
                        step = Integer.valueOf(hexStep, 16);
                        age = Integer.parseInt(patientAge);
                        double height = Integer.parseInt(patientHeight);
                        if (age <= 15 || age >= 65) {
                            distance = (int) ((height * 0.37) * step);
                        } else if (15 < age || age < 45) {
                            distance = (int) ((height * 0.45) * step);
                        } else if (45 <= age || age < 65) {
                            distance = (int) ((height * 0.40) * step);
                        } else {
                            distance = (int) ((height * 0.30) * step);
                        }
                        Log.e(TAG, "dist : " + hexDist + " distance : " + distance + " age : " + age + " steps : " + step + " height  : " + height);

                        hexDist = String.valueOf(distance / 100);  //CM -> M
                        break;
                }
                Log.e("noty", "characteristicValue = " + Integer.toHexString(characteristicValue[i]) + " / lsb = " + Integer.toHexString(lsb) + " / result " + result);
            }
        }

        stepFragment.setStep(step);
        calorieFragment.setCalorie(Integer.valueOf(hexCalo));
        distanceFragment.setDist(Integer.valueOf(hexDist));
        sampleFragment.setSample(Integer.valueOf(hexDist));

        Bundle bundle = new Bundle();
        bundle.putString("STEP", hexStep);
        bundle.putString("CALO", hexCalo);
        bundle.putString("DIST", hexDist);
        dashboardFragment.setStepData(bundle);

        addDataToRealmDB(step, Integer.valueOf(hexCalo), Integer.valueOf(hexDist));
    }

    private void addDataToRealmDB(int step, int calo, int dist) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String today = formatter.format(calendar.getTime());

        realm = Realm.getInstance(this);
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
                band.setCalorie(calo);
                band.setDistance(dist);
            }
        } else {
            Band band = realm.createObject(Band.class);
            band.setCalendar(today);
            band.setStep(step);
            band.setCalorie(calo);
            band.setDistance(dist);
        }
        realm.commitTransaction();

        results = realm.where(Band.class).findAll();

        ArrayList<DashboardItem> arrayList = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Log.e(TAG, "band [" + i + "] : " + results.size() + " step : " + results.get(i).getStep() + " calo : " + results.get(i).getCalorie() + " dist : " + results.get(i).getDistance() + " calendat : " + results.get(i).getCalendar());
            arrayList.add(new DashboardItem(results.get(i).getStep(), results.get(i).getCalorie(), results.get(i).getDistance(), results.get(i).getCalendar()));
        }
    }

//    @Override
//    public void onDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
//
//    }

    @Override
    public void onDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Connected");

                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                if (materialDialog.isShowing() && !isDestroyed()) {
                    materialDialog.dismiss();
                }
                swipeRefreshLayout.setRefreshing(false);
                System.gc();
            }
        });
    }

    @Override
    public void onDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Disconnected");

                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                        toolbarRssi.setText("No Signal");

                        if (!materialDialog.isShowing()) {
                            if (!isFinishing() || !isDestroyed()) {
                                materialDialog.show();
                            }
                        } else {
                            materialDialog.getBuilder().content("실패").build();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 100);
            }
        });
    }

    @Override
    public void
    onServicesFound(BluetoothGatt gatt, BluetoothDevice device, final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothGattService bluetoothGattService = services.get(4); // 0xFFF0
                ble.getCharacteristics(bluetoothGattService);
            }
        });
    }

    @Override
    public void onCharacteristicFound(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                characteristicList = chars;
                bluetoothGattCharacteristicForWrite = characteristicList.get(0); //0xFFF2
                bluetoothGattCharacteristicForNotify = characteristicList.get(1); //0xFFF1
//                uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristicForNotify);

            }
        });
    }

//    @Override
//    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                ble.setNotification(bluetoothGattCharacteristicForNotify, true);
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Check the time
//                        listType = ListType.READ_TIME;
//                        byte[] dataToWrite = BleHelper.READ_DEVICE_TIME();
//                        MainNursingActivity.this.ble.writeValue(bluetoothGattCharacteristicForWrite, dataToWrite);
//
//                    }
//                }, 1000);
//            }
//        });
//    }

    @Override
    public void onNewDataFound(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {

    }


    @Override
    public void onDataNotify(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadNotifyData(characteristic);
            }
        });
    }

    @Override
    public void onWriteSuccess(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("onWriteSuccess", description);
                isGattProcessRunning = false;
            }
        });
    }

    @Override
    public void onWriteFail(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("onWriteFail", description);
                isGattProcessRunning = false;
            }
        });
    }

    @Override
    public void onRssiUpdate(BluetoothGatt gatt, BluetoothDevice device, final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarRssi.setText(rssi + "db");
            }
        });
    }


    private class DashBoardAdapter extends FragmentStatePagerAdapter {

        final int PAGE_COUNT = 5;
        private String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "ETC"};

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
