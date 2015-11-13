package com.rainbow.kam.bt_scanner.Nursing.Activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Activity.MainActivity;
import com.rainbow.kam.bt_scanner.BuildConfig;
import com.rainbow.kam.bt_scanner.Nursing.Fragment.DashboardFragment;
import com.rainbow.kam.bt_scanner.Nursing.Fragment.SampleFragment;
import com.rainbow.kam.bt_scanner.Nursing.Patient.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLE;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleUiCallbacks;
import com.rainbow.kam.bt_scanner.Tools.BLE.Device.WrapperBleByPrime;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class MainNursingActivity extends AppCompatActivity implements BleUiCallbacks {

    public static final String TAG = MainNursingActivity.class.getSimpleName();

    private Handler handler;

    private DashboardFragment dashboardFragment;
    private SampleFragment sampleFragment;

    private BLE ble;
    private List<BluetoothGattService> serviceList;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DashBoardAdapter dashBoardAdapter;

    private RealmQuery<Patient> patientRealmQuery;
    private Realm realm;

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


    private Snackbar snackbar;


    @Override
    protected void onStart() {
        super.onStart();
        try {

            realm = Realm.getInstance(this);
            RealmResults<Patient> results = realm.where(Patient.class).findAll();
//            Log.e("Dashboard", results.size() + " / " + results.get(0).getName() + " / " + results.get(0).getAge() + " / " + results.get(0).getHeight() + " / " + results.get(0).getWeight() + " / " + results.get(0).getStep() + " / " + results.get(0).getDeviceName() + " / " + results.get(0).getDeviceAddress());

            patientName = results.get(0).getName();
            patientAge = results.get(0).getAge();
            patientHeight = results.get(0).getHeight();
            patientWeight = results.get(0).getWeight();
            patientStep = results.get(0).getStep();
            if (results.get(0).getGender().equals("남성")) {
                patientGender = "1";
            } else {
                patientGender = "0";
            }
            deviceName = results.get(0).getDeviceName();
            deviceAddress = results.get(0).getDeviceAddress();
            if (patientName == null || patientAge == null || patientHeight == null || patientWeight == null || patientStep == null || deviceName == null || deviceAddress == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            isNewUser = true;
            startActivity(new Intent(MainNursingActivity.this, StartNursingActivity.class));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
            }
        } else {
            Toast.makeText(getApplicationContext(), "권한의 남용을 주의 하십시오", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    finish();
                    Log.d(TAG, "Permission always deny");
                    Toast.makeText(getApplicationContext(), "권한의 획득을 거부하여 앱을 종료합니다", Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);

        toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        toolbarRssi = (TextView) findViewById(R.id.nursing_toolbar_rssi);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.nursing_toolbar_bluetoothFlag);
        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_white_24dp);

        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
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
                            realm.beginTransaction();
                            realm.clear(Patient.class);
                            realm.commitTransaction();
                            if (!isCharcteristicRunning) {
                                byte[] dataToWrite;
                                dataToWrite = WrapperBleByPrime.CLEAR_DATA();
                                ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                            }
//                            System.exit(0);
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

        tabLayout = (TabLayout) findViewById(R.id.nursing_tabs);
        dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager(), this);
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
        sampleFragment = new SampleFragment();
        tabLayout.setupWithViewPager(viewPager);

        snackbar = Snackbar.make(coordinatorLayout, "기기와의 연결이 비활성화 되었습니다", Snackbar.LENGTH_INDEFINITE).setAction("연결시도", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice();
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                isCharcteristicRunning = true;
                Message message = handler.obtainMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
                message.setData(msg.getData());
                byte[] dataToWrite;
                Log.e("what", msg.what + " is what");
                switch (message.what) {
                    case 0:  //Check the time
//                        dataToWrite = WrapperBleByPrime.READ_DEVICE_TIME();
                        dataToWrite = WrapperBleByPrime.READ_DEVICE_TIME();
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                        break;

                    case 1: //Check the time result And READ_STEP_DATA
                        dashboardFragment.handler.sendMessage(message);
                        dataToWrite = WrapperBleByPrime.READ_STEP_DATA(8);
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                        break;

                    case 2:
                        dashboardFragment.handler.sendMessage(message);
                        dataToWrite = WrapperBleByPrime.CALL_DEVICE();
//                        dataToWrite = WrapperBleByPrime.READ_SPORTS_CURVE_DATA();
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                        break;
                    case 3:
                        dashboardFragment.handler.sendMessage(message);
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
                    default:
//                        bleProcess = 0;
                        break;
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        connectDevice();
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnectDevice();
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

    private void connectDevice() {
        if (ble == null) {
            ble = new BLE(this, this);
        }
        if (ble.initialize() == false) {
            finish();
        }
        ble.connect(deviceAddress);
    }

    private void disconnectDevice() {
        ble.stopMonitoringRssiValue();
        ble.disconnect();
        ble.close();
        ble = null;
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
                snackbar.dismiss();
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
                        snackbar.show();

                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                        toolbarRssi.setText("No Signal");
                        Log.e(TAG, "Disconnected");
                    }
                }, 300);
            }
        });
    }

    @Override
    public void
    uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serviceList = services;
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
                            handler.sendEmptyMessage(bleProcess);
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
                                        case 2:
                                            result += "년 ";
                                            break;
                                        case 3:
                                            result += "월 ";
                                            break;
                                        case 4:
                                            result += "일 ";
                                            break;
                                        case 5:
                                            result += "시 ";
                                            break;
                                        case 6:
                                            result += "분 ";
                                            break;
                                        case 7:
                                            result += "초 ";
                                            break;
                                        case 8:
                                            result = result.substring(0, result.length() - 1);
                                            switch (Integer.valueOf(WrapperBleByPrime.setWidth(Integer.toHexString(res[i])), 16)) {
                                                case 1:
                                                    result += "월";
                                                    break;
                                                case 2:
                                                    result += "화";
                                                    break;
                                                case 3:
                                                    result += "수";
                                                    break;
                                                case 4:
                                                    result += "목";
                                                    break;
                                                case 5:
                                                    result += "금";
                                                    break;
                                                case 6:
                                                    result += "토";
                                                    break;
                                                case 7:
                                                    result += "일";
                                                    break;
                                            }
                                            break;
                                    }
                                }
                                Log.e("noty", "res = " + Integer.toHexString(res[i]) + " / lsb = " + Integer.toHexString(lsb) + " / process " + bleProcess + " / result " + result);
                            }
                            handleMessage.what = ++bleProcess;
                            handleMessage.obj = result;
                            break;
                        case 1:
                            for (int i = 0; i < res.length; i++) {
                                int lsb = characteristic.getValue()[i] & 0xff;
                                Log.e("noty", "res = " + Integer.toHexString(res[i]) + " / lsb = " + Integer.toHexString(lsb) + " / process " + bleProcess + " / result " + result);

                                if (i > 1 && i != res.length - 1) {
                                    result += Integer.valueOf(WrapperBleByPrime.setWidth(Integer.toHexString(lsb)), 16) + " / ";
                                    switch (i) {
                                        case 2:
                                        case 3:
                                        case 4:
                                            step += Integer.toHexString(lsb);
                                            Log.e(TAG, step);
                                            break;
                                        case 5:
                                        case 6:
                                        case 7:
                                            calo += Integer.toHexString(lsb);
                                            Log.e(TAG, calo);
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
                                            dist = String.valueOf(distance / 100) + "m";
                                            break;
                                    }

                                }
                            }
                            handleMessage.what = ++bleProcess;
                            bundle.putString("STEP", step);
                            bundle.putString("CALO", calo);
                            bundle.putString("DIST", dist);
                            handleMessage.setData(bundle);
                            break;
                        case 2:
                            for (int i = 0; i < res.length; i++) {
                                int lsb = characteristic.getValue()[i] & 0xff;
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
                    Toast.makeText(getApplicationContext(), "uiGotNotification " + res[0], Toast.LENGTH_LONG).show();
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
                    handler.sendEmptyMessage(bleProcess);
                } else {
                    Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();
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
                    handler.sendEmptyMessage(bleProcess);
                } else {
                    Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();
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
        private String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "SLEEP"};
        private Context context;

        public DashBoardAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return dashboardFragment.newInstance(position + 1);

                case 1:
                    return sampleFragment.newInstance(position + 1);

                case 2:
                    return sampleFragment.newInstance(position + 1);

                case 3:
                    return sampleFragment.newInstance(position + 1);

                case 4:
                    return sampleFragment.newInstance(position + 1);

                default:
                    return sampleFragment.newInstance(position + 1);
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
