package com.rainbow.kam.bt_scanner.nursing.activity;

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
import android.os.Message;
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
import com.rainbow.kam.bt_scanner.activity.MainActivity;
import com.rainbow.kam.bt_scanner.nursing.adapter.DashboardItem;
import com.rainbow.kam.bt_scanner.nursing.fragment.main.CalorieFragment;
import com.rainbow.kam.bt_scanner.nursing.fragment.main.DashboardFragment;
import com.rainbow.kam.bt_scanner.nursing.fragment.main.DistanceFragment;
import com.rainbow.kam.bt_scanner.nursing.fragment.main.SampleFragment;
import com.rainbow.kam.bt_scanner.nursing.fragment.main.StepFragment;
import com.rainbow.kam.bt_scanner.nursing.patient.Band;
import com.rainbow.kam.bt_scanner.nursing.patient.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BLE;
import com.rainbow.kam.bt_scanner.tools.ble.BleUiCallbacks;
import com.rainbow.kam.bt_scanner.tools.ble.device.WrapperBleByPrime;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;

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

    private Realm realm;
    private Activity activity;

    private String patientName = null;
    public static String patientAge = null;
    public static String patientHeight = null;
    public static String patientWeight = null;
    public static String patientStep = null;
    private String patientGender = null;
    private String deviceName = null;
    private String deviceAddress = null;

    private int bleProcess = 0;
    private boolean isNewUser = false;
    public static boolean isCharcteristicRunning = false;

    private String[] weekSet = {"월", "화", "수", "목", "금", "토", "일",};
    private String[] timeSet = {"년", "월", "일", "시", "분", "초"};

    private int READ_TIME = 0;
    private int READ_DATA = 1;
    private int ETC = 2;

    public static Handler handler;
    private Runnable runnable;

    private DashboardFragment dashboardFragment;
    private StepFragment stepFragment;
    private CalorieFragment calorieFragment;
    private DistanceFragment distanceFragment;
    private SampleFragment sampleFragment;

    private BLE ble;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    //    private Snackbar snackbar;
    private MaterialDialog materialDialog;

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
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
                throw new Exception();
            }

            PermissionV21.check(this);

        } catch (Exception e) {

            Realm.removeDefaultConfiguration();

//            realm = Realm.getInstance(this);
//            realm.beginTransaction();
//
//            isNewUser = true;
//            realm.clear(Band.class);
            startActivity(new Intent(MainNursingActivity.this, StartNursingActivity.class));

//            realm.commitTransaction();


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        Toast.makeText(getApplicationContext(), "권한 획득 필요.", Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(getWindow().getDecorView(), "권한 획득, 감사합니다.", Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "권한 획득, 감사합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                    Toast.makeText(getApplicationContext(), "[권한] 탭 -> [위치] 권한을 허용해주십시오", Toast.LENGTH_SHORT).show();
//                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 0);
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(myAppSettings, 0);

                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);

        setToolbar();
        setNavigationView();
        setViewPager();
        setSnackBar();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (ble == null) {
                    return;
                }
                isCharcteristicRunning = true;
                Message message = handler.obtainMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
                message.setData(msg.getData());
                byte[] dataToWrite;
                Log.e("what", msg.what + " is what");
                switch (message.what) {
                    case 0:  //Check the time
                        bleProcess = READ_TIME;
                        dataToWrite = WrapperBleByPrime.READ_DEVICE_TIME();
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                        break;

                    case 1: //Check the time result And READ_STEP_DATA

                        DashboardFragment.handler.sendMessage(message);

                        bleProcess = READ_DATA;
                        dataToWrite = WrapperBleByPrime.READ_STEP_DATA(8);
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                        break;

                    case 2:

                        DashboardFragment.handler.sendMessage(message);
                        StepFragment.handler.sendMessage(Message.obtain(message));
                        CalorieFragment.handler.sendMessage(Message.obtain(message));
                        DistanceFragment.handler.sendMessage(Message.obtain(message));
                        SampleFragment.handler.sendMessage(Message.obtain(message));

                        addBandData(Message.obtain(message));

                        bleProcess = ETC;
//                        dataToWrite = WrapperBleByPrime.();
//                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                        break;

                    case 3:
                        DashboardFragment.handler.sendMessage(message);

                        Log.e(TAG, "READ_SPORTS_CURVE_DATA's result = " + msg.obj);
                        break;

                    case -1: //new User

                        dataToWrite = WrapperBleByPrime.SET_USER_DATA(Integer.valueOf(patientGender), Integer.valueOf(patientHeight), Integer.valueOf(patientWeight), Integer.valueOf(patientStep), Integer.valueOf(patientStep) + 30);
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);

                        break;

                    case -2://re-connect
                        disconnectDevice();
                        connectDevice();
                        break;
                    case -10000:
                        finish();
                        break;
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        enableBluetooth();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        if (resultCode == 1) {
            connectDevice();
        } else {
            Toast.makeText(this, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();
            finish();
        }

    }


    public boolean enableBluetooth() {//블루투스 가동여부
        Log.d(TAG, "enableBluetooth");
        BluetoothManager bluetoothManager;
        BluetoothAdapter bluetoothAdapter;
        try {
            //블루투스 매니저/어댑터 초기화
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                throw new Exception();
            }
            if (bluetoothAdapter.isEnabled()) { //블루투스 이미 켜짐
                Log.d(TAG, "Bluetooth isEnabled");
                connectDevice();
                return true;
            } else {    //블루투스 구동
                Log.d(TAG, "Bluetooth start");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
        return false;

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

    private void setNavigationView() {
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
                            if (!isCharcteristicRunning) {
//                                byte[] dataToWrite;
//                                dataToWrite = WrapperBleByPrime.CLEAR_DATA();
//                                ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                            }

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
        dashboardFragment = new DashboardFragment();
        stepFragment = new StepFragment();
        calorieFragment = new CalorieFragment();
        distanceFragment = new DistanceFragment();
        sampleFragment = new SampleFragment();

        tabLayout.setupWithViewPager(viewPager);
    }

    private void setSnackBar() {

        materialDialog = new MaterialDialog.Builder(this).title("기기와의 연결이 비활성화 되었습니다").content("다시 연결하기").positiveText("재시도").negativeText("종료")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        enableBluetooth();
                        materialDialog.getBuilder().content("연결중").build();
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
                        finish();
                        materialDialog.dismiss();
                    }
                }).showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {

                    }
                }).autoDismiss(false).cancelable(false).build();
//        });
    }

    private void connectDevice() {
        if (ble == null) {
            ble = new BLE(this, this);
        }
        if (!ble.initialize()) {
            finish();
        }
        ble.connect(deviceAddress);
    }

    private void disconnectDevice() {
        if (ble != null) {
            ble.stopMonitoringRssiValue();
            ble.disconnect();
            ble.close();
            ble = null;
        }
    }

    private void addBandData(Message message) {

        Bundle bundle = message.getData();
        int step = Integer.valueOf(bundle.getString("STEP", "0"), 16);
        int calorie = Integer.valueOf(bundle.getString("CALO", "0"), 16);
        int distance = Integer.valueOf(bundle.getString("DIST", "0"), 16);

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

        ArrayList<DashboardItem> arrayList = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Log.e(TAG, "band [" + i + "] : " + results.size() + " step : " + results.get(i).getStep() + " calo : " + results.get(i).getCalorie() + " dist : " + results.get(i).getDistance() + " calendat : " + results.get(i).getCalendar());
            arrayList.add(new DashboardItem(results.get(i).getStep(), results.get(i).getCalorie(), results.get(i).getDistance(), results.get(i).getCalendar()));
        }


        stepFragment.setArrayList(this, arrayList);
//        calorieFragment.setArrayList(arrayList);
//        distanceFragment.setArrayList(arrayList);

    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {

    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Connected");
                if (materialDialog.isShowing()) {
                    materialDialog.dismiss();
                }
                toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                        toolbarRssi.setText("No Signal");
                        Log.e(TAG, "Disconnected");
                        if (!materialDialog.isShowing()) {
                            if (!isFinishing() || !isDestroyed()) {
                                materialDialog.show();
                            }

                        } else {
                            materialDialog.getBuilder().content("실패");
                        }
                    }
                }, 100);
            }
        });
    }

    @Override
    public void
    uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothGattService bluetoothGattService = services.get(4);
                ble.getCharacteristicsForService(bluetoothGattService);
            }
        });
    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                characteristicList = chars;
                bluetoothGattCharacteristicForWrite = characteristicList.get(0);
                bluetoothGattCharacteristicForNotify = characteristicList.get(1);
                uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristicForNotify);

            }
        });
    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ble.setNotificationForCharacteristic(bluetoothGattCharacteristicForNotify, true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isNewUser) {
                            handler.sendEmptyMessage(-1);
                        } else {
                            handler.sendEmptyMessage(READ_TIME);
                        }
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {

    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                byte[] res = characteristic.getValue();

                Bundle bundle = new Bundle();
                String step = "";
                String calo = "";
                String dist = "";

                final Message handleMessage = new Message();

                if (isNewUser) {
                    isNewUser = false;
                } else {
                    switch (bleProcess) {
                        case 0:
                            for (int i = 0; i < res.length; i++) {
                                int lsb = characteristic.getValue()[i] & 0xff;
                                if (i > 1 && i != res.length - 1) {
                                    result += Integer.valueOf(WrapperBleByPrime.setWidth(Integer.toHexString(res[i])), 16);

                                    switch (i) {
                                        default:
                                            result += timeSet[i - 2] + " ";

                                            break;
                                        case 8:
                                            result = result.substring(0, result.length() - 1);

                                            int j = Integer.valueOf(WrapperBleByPrime.setWidth(Integer.toHexString(res[i])), 16);
                                            result += weekSet[j - 1];
                                            break;
                                    }
                                }
                                Log.e("noty", "res = " + Integer.toHexString(res[i]) + " / lsb = " + Integer.toHexString(lsb) + " / process " + bleProcess + " / result " + result);
                            }

                            handleMessage.what = READ_TIME + 1;
                            handleMessage.obj = result;
                            break;

                        case 1:

                            for (int i = 0; i < res.length; i++) {

                                int lsb = characteristic.getValue()[i] & 0xff;


                                if (i > 1 && i != res.length - 1) {

                                    result += Integer.valueOf(WrapperBleByPrime.setWidth(Integer.toHexString(lsb)), 16) + " / ";

                                    switch (i) {

                                        case 2:
                                        case 3:
                                        case 4:

                                            step += WrapperBleByPrime.setWidth(Integer.toHexString(lsb));
                                            break;

                                        case 5:
                                        case 6:
                                        case 7:

                                            calo += WrapperBleByPrime.setWidth(Integer.toHexString(lsb));
                                            break;

                                        case 8:

                                            dist += Integer.toHexString(lsb);
                                            break;

                                        case 9:

                                            dist += Integer.toHexString(lsb);
                                            break;

                                        case 10:

                                            dist += Integer.toHexString(lsb);
                                            int distance;
                                            int steps = Integer.valueOf(step, 16);
                                            int age = Integer.parseInt(patientAge);
                                            double height = Integer.parseInt(patientHeight);
                                            if (age <= 15 || age >= 65) {
                                                distance = (int) ((height * 0.37) * steps);
                                            } else if (15 < age || age < 45) {
                                                distance = (int) ((height * 0.45) * steps);
                                            } else if (45 <= age || age < 65) {
                                                distance = (int) ((height * 0.40) * steps);
                                            } else {
                                                distance = (int) ((height * 0.30) * steps);
                                            }
                                            Log.e(TAG, "dist : " + dist + " distance : " + distance + " age : " + age + " steps : " + steps + " height  : " + height);

                                            dist = String.valueOf(distance / 100);  //CM -> M
                                            break;
                                    }
                                    Log.e("noty", "res = " + Integer.toHexString(res[i]) + " / lsb = " + Integer.toHexString(lsb) + " / process " + bleProcess + " / result " + result);
                                }
                            }

                            handleMessage.what = READ_DATA + 1;
                            bundle.putString("STEP", step);
                            bundle.putString("CALO", calo);
                            bundle.putString("DIST", dist);
                            handleMessage.setData(bundle);
                            break;

                        case 2:
                            for (int i = 0; i < res.length; i++) {
                                int lsb = characteristic.getValue()[i] & 0xff;
                                result += Integer.valueOf(WrapperBleByPrime.setWidth(Integer.toHexString(res[i])), 16);
                                Log.e("noty", "res = " + Integer.toHexString(res[i]) + " / lsb = " + Integer.toHexString(lsb) + " / process " + bleProcess + " / result " + result);

                            }

                            handleMessage.what = 3;
                            handleMessage.obj = result;
                            break;

                        default:
                            break;
                    }
                    handleMessage.arg1 = 0;
                    handleMessage.arg2 = 0;


                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendMessage(handleMessage);
                        }
                    }, 300);
//                    Toast.makeText(getApplicationContext(), "uiGotNotification " + res[0], Toast.LENGTH_LONG).show();
                    isCharcteristicRunning = false;

                }
            }
        });
    }

    @Override
    public void uiSuccessfulWrite(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("uiSuccessfulWrite", description);

                isCharcteristicRunning = false;

                if (isNewUser) {

                    Toast.makeText(getApplicationContext(), "유저등록성공.", Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(READ_TIME);

                } else {

//                    Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void uiFailedWrite(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("uiFailedWrite", description);

                isCharcteristicRunning = false;

                if (isNewUser) {

                    handler.sendEmptyMessage(READ_TIME);

                }
            }
        });
    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, final int rssi) {
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
                    return DashboardFragment.newInstance(position + 1);

                case 1:
                    return StepFragment.newInstance(position + 1);
                case 2:
                    return CalorieFragment.newInstance(position + 1);
                case 3:
                    return DistanceFragment.newInstance(position + 1);
                default:
                    return SampleFragment.newInstance(position + 1);
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
