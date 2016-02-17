package com.rainbow.kam.bt_scanner.activity.prime;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.rainbow.kam.bt_scanner.data.dao.PrimeDao;
import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;
import com.rainbow.kam.bt_scanner.fragment.device.DeviceListFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.GoalDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.user.PrimeFragment;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
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

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;

    private enum GattReadType {
        READ_TIME, READ_STEP_DATA
    }

    private GattReadType gattReadType;

    private FragmentManager fragmentManager;

    private UserDataDialogFragment userDataDialogFragment;
    private DeviceListFragment deviceListFragment;
    private GoalDialogFragment goalDialogFragment;

    private PrimeFragment primeFragment;

    private TextView navUpdate, navBattery;
    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Snackbar disconnectDeviceSnackBar;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;

    private String userAge, userHeight, deviceAddress;
    private String rssiUnit;

    private PrimeDao primeDao;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.requestBluetoothPermission(this);
        }

        initDB();
        setFragments();
        setToolbar();
        setMaterialView();
        setNavigationView();
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
                deviceListFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
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
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
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
        deviceListFragment = new DeviceListFragment();
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
        rssiUnit = getString(R.string.rssi_unit);
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

        disconnectDeviceSnackBar = Snackbar.make(coordinatorLayout, R.string.prime_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_ignore, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav = navigationView.getHeaderView(0);
        navUpdate = (TextView) nav.findViewById(R.id.prime_update);
        navBattery = (TextView) nav.findViewById(R.id.prime_battery);
    }


    private void registerBluetooth() {
        if (isDeviceDataAvailable()) {
            gattManager = new GattManager(this, gattCallbacks);
            if (gattManager.isBluetoothAvailable()) {
                loadUserDeviceData();
                connectDevice();
            } else {
                BluetoothHelper.requestBluetoothEnable(this);
            }
        }
    }


    private boolean isDeviceDataAvailable() {
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
                deviceListFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
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


    private void connectDevice() {
        try {
            gattManager.connect(deviceAddress);
            swipeRefreshLayout.post(postSwipeRefresh);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, getString(R.string.bt_fail), Toast.LENGTH_SHORT).show();
        }
    }


    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    private void loadUserDeviceData() {
        UserVo userVo = primeDao.loadUserData();
        DeviceVo deviceVo = primeDao.loadDeviceData();
        userAge = userVo.age;
        userHeight = userVo.height;
        deviceAddress = deviceVo.address;
    }


    private void saveDeviceData(String name, String address) {
        DeviceVo deviceVo = new DeviceVo();
        deviceVo.name = name;
        deviceVo.address = address;
        primeDao.saveDeviceData(deviceVo);
    }


    private void savePrimeData(RealmPrimeItem realmPrimeItem) {
        primeDao.savePrimeData(realmPrimeItem);
    }


    @DebugLog
    private void removeAllData() {
        primeDao.clearSharedPreferenceData();
    }


    private void setBatteryValue(String batteryValue) {
        navBattery.setText(batteryValue);
    }


    private void setUpdateValue(String updateValue) {
        navUpdate.setText(updateValue);
    }


    private void setAvailableGatt(List<BluetoothGattService> services) {
        List<BluetoothGattCharacteristic> characteristicList = services.get(4).getCharacteristics();
        gattManager.setNotification(characteristicList.get(1), true);
        bluetoothGattCharacteristicForWrite = characteristicList.get(0);
        bluetoothGattCharacteristicForBattery = services.get(2).getCharacteristics().get(0);
    }


    private String readBatteryValue(BluetoothGattCharacteristic ch) {
        final byte[] batteryByteValue = ch.getValue();
        final StringBuilder batteryValue = new StringBuilder(batteryByteValue.length);
        for (byte byteChar : batteryByteValue) {
            batteryValue.append(String.format("%02d", byteChar));
        }
        batteryValue.append(getString(R.string.prime_battery_unit));
        return batteryValue.toString();
    }


    private String readUpdateTimeValue(BluetoothGattCharacteristic ch) {
        final byte[] characteristicValue = ch.getValue();
        final String format = getString(R.string.prime_update_format);
        final SimpleDateFormat update = new SimpleDateFormat(format, Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, characteristicValue[2]);
        calendar.set(Calendar.MONTH, characteristicValue[3] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, characteristicValue[4]);
        calendar.set(Calendar.HOUR_OF_DAY, characteristicValue[5]);
        calendar.set(Calendar.MINUTE, characteristicValue[6]);
        calendar.set(Calendar.SECOND, characteristicValue[7]);

        return update.format(calendar.getTime());
    }


    private RealmPrimeItem makePrimeItem(BluetoothGattCharacteristic ch)
            throws ArrayIndexOutOfBoundsException {

        final byte[] characteristicValue = ch.getValue();

        StringBuilder hexStep = new StringBuilder()
                .append(formatHexData(characteristicValue, 2))
                .append(formatHexData(characteristicValue, 3))
                .append(formatHexData(characteristicValue, 4));

        StringBuilder hexCal = new StringBuilder()
                .append(formatHexData(characteristicValue, 5))
                .append(formatHexData(characteristicValue, 6))
                .append(formatHexData(characteristicValue, 7));

        final int step, kcal, distance;

        final int radix = 16;
        step = Integer.parseInt(hexStep.toString(), radix);
        kcal = Integer.parseInt(hexCal.toString(), radix);

        final int age = Integer.parseInt(userAge);
        final double height = Integer.parseInt(userHeight);

        final double convertValue;

        if (15 < age && age < 45) {
            convertValue = 0.45;
        } else if (45 <= age && age < 65) {
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


    private String formatHexData(byte[] characteristicValue, int index) {
        return String.format("%02x", characteristicValue[index] & 0xff);
    }


    private void fail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeFragment.setTextFail();
            }
        });
    }


    @Override
    public void onDeviceSelect(final String name, final String address) {
        goalDialogFragment.dismiss();
        saveDeviceData(name, address);
        registerBluetooth();
    }


    @Override
    public void onDeviceUnSelected() {
        registerBluetooth();
    }


    @Override
    public void onSaveGoal() {
        goalDialogFragment.dismiss();
        primeFragment.setCircleCounterGoalRange(primeDao.loadGoalData());
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
            gattReadType = GattReadType.READ_TIME;
            setAvailableGatt(services);
        }


        public void onServicesNotFound() {
            fail();
        }


        public void onReadSuccess(final BluetoothGattCharacteristic ch) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setBatteryValue(readBatteryValue(ch));
                }
            });
        }


        public void onReadFail() {
            fail();
        }


        @DebugLog
        public void onDeviceReady() {
            gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_TIME);
        }


        public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (gattReadType) {
                            case READ_TIME:
                                setUpdateValue(readUpdateTimeValue(ch));

                                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_PRIME);
                                gattReadType = GattReadType.READ_STEP_DATA;

                                break;

                            case READ_STEP_DATA:

                                savePrimeData(makePrimeItem(ch));
                                primeFragment.setPrimeValue(primeDao.loadPrimeListData());
                                primeFragment.setCircleCounterGoalRange(primeDao.loadGoalData());

                                gattManager.readValue(bluetoothGattCharacteristicForBattery);
                                gattReadType = null;

                                break;
                            default:
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                fail();
            }

        }


        public void onWriteFail() {
            fail();
        }


        public void onRSSIUpdate(final int rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarRssi.setText(rssi + rssiUnit);
                }
            });
        }
    };


    private void setDeviceDateTime() {
        gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.getBytesForDateTime());
    }
}