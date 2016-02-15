package com.rainbow.kam.bt_scanner.activity.prime;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.GoalDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.SelectDeviceDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.PrimeFragment;
import com.rainbow.kam.bt_scanner.tools.data.dao.PrimeDao;
import com.rainbow.kam.bt_scanner.tools.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.tools.data.vo.UserVo;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class PrimeActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        GoalDialogFragment.OnSaveGoalListener {

    private final String TAG = getClass().getSimpleName();

    private enum GattReadType {
        READ_TIME, READ_STEP_DATA
    }

    private GattReadType gattReadType;

    private FragmentManager fragmentManager;

    private UserDataDialogFragment userDataDialogFragment;
    private SelectDeviceDialogFragment selectDeviceDialogFragment;
    private GoalDialogFragment goalDialogFragment;

    private PrimeFragment primeFragment;

    private TextView navUpdate, navBattery;
    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavigationView navigationView;

    private Snackbar disconnectDeviceSnackBar;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;

    private String userAge, userHeight, deviceAddress;
    private String rssiUnit;

    private PrimeDao primeDao;


    private final Handler handler = new Handler();
    private final Runnable postSwipeRefresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_prime);
        rssiUnit = getString(R.string.rssi_unit);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.checkPermissions(this);
        }

        initDB();
        setFragments();
        setToolbar();
        setMaterialView();
        setNavInfoView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @Override
    public void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @Override
    public void onRefresh() {
        if (gattManager != null && gattManager.isConnected()) {
            disconnectDevice();
        } else {
            registerBluetooth();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_prime_setting_device:
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                disconnectDevice();
                selectDeviceDialogFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
                return true;
            case R.id.menu_prime_setting_user:
                userDataDialogFragment.show(fragmentManager, getString(R.string.prime_setting_user_tag));
                return true;
            case R.id.menu_prime_setting_goal:
                goalDialogFragment.show(fragmentManager, getString(R.string.prime_setting_goal_tag));
                return true;
            case R.id.menu_prime_about_dev:
                startActivity(new Intent(PrimeActivity.this, MainActivity.class));
                return true;
            case R.id.menu_prime_about_setting:
                removeAllData();
                registerBluetooth();
                return true;
            case R.id.menu_prime_about_about:
                return true;
            default:
                return true;
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (disconnectDeviceSnackBar.isShown() || !gattManager.isConnected()) {
                super.onBackPressed();
            } else {
                showDisconnectDeviceSnackBar();
                disconnectDevice();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onActivityResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    private void initDB() {
        primeDao = PrimeDao.getInstance(this);
    }


    private void setFragments() {
        fragmentManager = getSupportFragmentManager();

        primeFragment = new PrimeFragment();
        fragmentManager.beginTransaction().replace(R.id.prime_fragment_frame, primeFragment).commit();

        userDataDialogFragment = new UserDataDialogFragment();
        selectDeviceDialogFragment = new SelectDeviceDialogFragment();
        goalDialogFragment = new GoalDialogFragment();
    }


    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.prime_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbarRssi = (TextView) findViewById(R.id.prime_toolbar_rssi);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.prime_toolbar_bluetoothFlag);
    }


    private void setMaterialView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.prime_drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.prime_coordinatorLayout);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.prime_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        disconnectDeviceSnackBar = Snackbar.make(coordinatorLayout, R.string.prime_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_ignore, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setNavInfoView() {
        View nav = View.inflate(this, R.layout.v_nav_header_prime, navigationView);
        navUpdate = (TextView) nav.findViewById(R.id.prime_update);
        navBattery = (TextView) nav.findViewById(R.id.prime_battery);
    }


    private void registerBluetooth() {
        if (checkDataAvailable()) {
            gattManager = new GattManager(this, gattCallbacks);
            if (gattManager.isBluetoothAvailable()) {
                loadUserDeviceData();
                connectDevice();
            } else {
                BluetoothHelper.bluetoothRequest(this);
            }
        }
    }


    private boolean checkDataAvailable() {
        if (primeDao.isAllDataEmpty()) {
            swipeRefreshLayout.setRefreshing(false);
            showDeviceSettingSnackBar();
            return false;
        } else if (primeDao.isUserDataEmpty()) {
            showUserSettingSnackBar();
        }
        return true;
    }


    private void showDeviceSettingSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.prime_setting_device, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_setting_device_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDeviceDialogFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
            }
        }).show();
    }


    private void showUserSettingSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.prime_setting_user, Snackbar.LENGTH_LONG).setAction(R.string.prime_setting_user_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDataDialogFragment.show(fragmentManager, getString(R.string.prime_setting_user_tag));
            }
        }).show();
    }


    private void showDisconnectDeviceSnackBar() {
        disconnectDeviceSnackBar.show();
    }


    @DebugLog
    private void connectDevice() {
        try {
            gattManager.connect(deviceAddress);
            swipeRefreshLayout.post(postSwipeRefresh);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, getString(R.string.bt_fail), Toast.LENGTH_SHORT).show();
        }
    }


    @DebugLog
    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    private void loadUserDeviceData() {
        DeviceVo deviceVo = primeDao.loadDeviceData();
        UserVo userVo = primeDao.loadUserData();
        userAge = userVo.age;
        userHeight = userVo.height;
        deviceAddress = deviceVo.address;
    }


    @DebugLog
    private void saveDeviceData(String name, String address) {
        DeviceVo deviceVo = new DeviceVo();
        deviceVo.name = name;
        deviceVo.address = address;
        primeDao.saveDeviceData(deviceVo);
    }


    @DebugLog
    private void savePrimeData(RealmPrimeItem realmPrimeItem) {
        primeDao.savePrimeData(realmPrimeItem);
    }


    @DebugLog
    private void removeAllData() {
        primeDao.removeUserDeviceData();
    }


    private void setUpdateValue(Calendar calendar) {
        String format = getString(R.string.prime_update_format);
        final SimpleDateFormat update = new SimpleDateFormat(format, Locale.getDefault());
        navUpdate.setText(update.format(calendar.getTime()));
    }


    private void setBatteryValue(String batteryValue) {
        String value = batteryValue + getString(R.string.prime_battery_unit);
        navBattery.setText(value);
    }


    private void setFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeFragment.setTextFail();
            }
        });
    }


    @DebugLog
    @Override
    public void onDeviceSelect(final String name, final String address) {
        selectDeviceDialogFragment.dismiss();
        saveDeviceData(name, address);
        registerBluetooth();
    }


    @DebugLog
    @Override
    public void onDeviceUnSelected() {
        registerBluetooth();
    }


    @Override
    public void onSaveGoal() {
        goalDialogFragment.dismiss();
        primeFragment.setCircleCounterGoalRange();
    }


    private final GattCustomCallbacks.GattCallbacks gattCallbacks = new GattCustomCallbacks.GattCallbacks() {

        public void onDeviceConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }


        public void onDeviceDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (disconnectDeviceSnackBar.isShownOrQueued()) {
                        finish();
                    } else {
                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);
                        toolbarRssi.setText("--");

                        if (swipeRefreshLayout.isRefreshing()) {
                            registerBluetooth();
                        }
                    }
                }
            });
        }


        public void onServicesFound(final List<BluetoothGattService> services) {
            List<BluetoothGattCharacteristic> characteristicList = services.get(4).getCharacteristics();
            gattManager.setNotification(characteristicList.get(1), true);
            bluetoothGattCharacteristicForWrite = characteristicList.get(0);
            bluetoothGattCharacteristicForBattery = services.get(2).getCharacteristics().get(0);
            gattReadType = GattReadType.READ_TIME;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadTime);
                }
            }, 100); // Notify 콜백 메서드가 없으므로 강제로 기다린다
        }


        public void onServicesNotFound() {
            setFail();
        }


        public void onReadSuccess(BluetoothGattCharacteristic ch) {

            final String batteryValue = readBatteryValue(ch);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setBatteryValue(batteryValue);
                }
            });
        }


        public void onReadFail() {
            setFail();
        }


        public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (gattReadType) {
                            case READ_TIME:
                                setUpdateValue(makeCurrentCalendar(ch.getValue()));

                                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForReadExerciseData);
                                gattReadType = GattReadType.READ_STEP_DATA;

                                break;

                            case READ_STEP_DATA:

                                savePrimeData(makePrimeItem(ch.getValue()));
                                primeFragment.setPrimeValue(primeDao.loadPrimeListData());

                                gattManager.readValue(bluetoothGattCharacteristicForBattery);
                                gattReadType = null;

                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        setFail();
                    }
                }
            });
        }


        public void onWriteFail() {
            setFail();
        }


        public void onRSSIUpdate(final int rssi) {
            final String rssiValue = rssi + rssiUnit;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarRssi.setText(rssiValue);
                }
            });
        }
    };


    private String readBatteryValue(BluetoothGattCharacteristic ch) {
        final byte[] batteryByteValue = ch.getValue();
        final StringBuilder batteryValue = new StringBuilder(batteryByteValue.length);
        for (byte byteChar : batteryByteValue) {
            batteryValue.append(String.format("%02d", byteChar));
        }
        return batteryValue.toString();
    }


    private Calendar makeCurrentCalendar(byte[] characteristicValue) {

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, characteristicValue[2]);
        calendar.set(Calendar.MONTH, characteristicValue[3] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, characteristicValue[4]);
        calendar.set(Calendar.HOUR_OF_DAY, characteristicValue[5]);
        calendar.set(Calendar.MINUTE, characteristicValue[6]);
        calendar.set(Calendar.SECOND, characteristicValue[7]);

        return calendar;
    }


    private RealmPrimeItem makePrimeItem(byte[] characteristicValue)
            throws ArrayIndexOutOfBoundsException {

        final int age = Integer.parseInt(userAge);
        final double height = Integer.parseInt(userHeight);

        final String format = "%02x";
        final int bitFormat = 0xff;

        StringBuilder hexStep = new StringBuilder();
        hexStep.append(String.format(format, characteristicValue[2] & bitFormat));
        hexStep.append(String.format(format, characteristicValue[3] & bitFormat));
        hexStep.append(String.format(format, characteristicValue[4] & bitFormat));

        StringBuilder hexCal = new StringBuilder();
        hexCal.append(String.format(format, characteristicValue[5] & bitFormat));
        hexCal.append(String.format(format, characteristicValue[6] & bitFormat));
        hexCal.append(String.format(format, characteristicValue[7] & bitFormat));

        final int step, kcal, distance;

        final int radix = 16;
        step = Integer.parseInt(hexStep.toString(), radix);
        kcal = Integer.parseInt(hexCal.toString(), radix);

        final double convertValue;

        if (15 < age || age < 45) {
            convertValue = 0.45;
        } else if (45 <= age || age < 65) {
            convertValue = 0.40;
        } else {
            convertValue = 0.35;
        }
        distance = (int) ((height * convertValue) * step) / 100;

        RealmPrimeItem realmPrimeItem = new RealmPrimeItem();
        realmPrimeItem.setStep(step);
        realmPrimeItem.setCalorie(kcal);
        realmPrimeItem.setDistance(distance);

        return realmPrimeItem;
    }


    private void setDeviceDateTime() {
        gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForDateTime());
    }
}