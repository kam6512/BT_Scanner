package com.rainbow.kam.bt_scanner.activity.prime;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import com.rainbow.kam.bt_scanner.tools.helper.VidonnHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class PrimeActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        UserDataDialogFragment.OnSaveUserDataListener,
        GoalDialogFragment.OnSaveGoalListener {

    private final String TAG = getClass().getSimpleName();


    private enum DeviceType {
        DEVICE_PRIME, DEVICE_VIDONN;
    }

    private DeviceType deviceType;

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;

    private enum GattReadType {
        READ_TIME, READ_STEP_DATA
    }

    private GattReadType gattReadType;

    private enum connectionStateType {
        NONE,
        NEED_DEVICE_NOT_CONNECT,
        NEED_USER_CONNECT,
        CONNECTED,
        DISCONNECT_QUEUE,
        DISCONNECTED,
        GATT_RUNNING,
        GATT_END,
        REFRESH
    }

    private connectionStateType state;

    private FragmentManager fragmentManager;

    private UserDataDialogFragment userDataDialogFragment;
    private DeviceListFragment deviceListFragment;
    private GoalDialogFragment goalDialogFragment;

    private PrimeFragment primeFragment;

    private ImageView navBattery;
    private TextView navDeviceName, navDeviceAddress, navUpdate;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MaterialDialog removeDialog, reconnectDialog;

    private Snackbar deviceSettingSnackBar;

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

        state = connectionStateType.NONE;

        setContentView(R.layout.a_prime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothHelper.requestBluetoothPermission(this);
        }

        initDB();
        setFragments();
        setToolbar();
        setMaterialView();
        setNavigationView();
        setMaterialDialog();
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
        state = connectionStateType.REFRESH;
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
            deviceSettingSnackBar.dismiss();
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
                if (!(state == connectionStateType.NEED_DEVICE_NOT_CONNECT)) {
                    disconnectDevice();
                }

                deviceListFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
                return true;
            case R.id.menu_prime_setting_user:
                if (state == connectionStateType.NEED_DEVICE_NOT_CONNECT) {
                    showDeviceSettingSnackBar();
                } else {
                    userDataDialogFragment.show(fragmentManager, getString(R.string.prime_setting_user_tag));
                }
                return true;
            case R.id.menu_prime_setting_goal:
                if (state == connectionStateType.NEED_DEVICE_NOT_CONNECT) {
                    showDeviceSettingSnackBar();
                } else {
                    goalDialogFragment.show(fragmentManager, getString(R.string.prime_setting_goal_tag));
                }

                return true;
            case R.id.menu_prime_about_dev:
                startActivity(new Intent(PrimeActivity.this, MainActivity.class));
                return true;
            case R.id.menu_prime_about_setting:

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (!(state == connectionStateType.NEED_DEVICE_NOT_CONNECT)) {
                    disconnectDevice();
                }
                removeDialog.show();
                return true;
            case R.id.menu_prime_about_about:
                Snackbar.make(coordinatorLayout, "준비중입니다..", Snackbar.LENGTH_SHORT).show();
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
            if (state == connectionStateType.NEED_DEVICE_NOT_CONNECT || (state == connectionStateType.DISCONNECT_QUEUE || !gattManager.isConnected())) {
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

        deviceListFragment = new DeviceListFragment();
        userDataDialogFragment = new UserDataDialogFragment();
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
        toolbarRssi.setText("--");
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
    }


    private void setNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav = navigationView.getHeaderView(0);
        navDeviceName = (TextView) nav.findViewById(R.id.prime_device_name);
        navDeviceAddress = (TextView) nav.findViewById(R.id.prime_device_address);
        navUpdate = (TextView) nav.findViewById(R.id.prime_update);
        navBattery = (ImageView) nav.findViewById(R.id.prime_battery);
    }


    private void setMaterialDialog() {

        final int REMOVE_USER = 0, REMOVE_HISTORY = 1, REMOVE_ALL = 2;
        List<String> removeChoiceList = new ArrayList<String>();
        removeChoiceList.add("유저정보(기기, 신체 정보)");
        removeChoiceList.add("운동정보(도보량, 소모열량, 활동거리 저장 DB)");
        removeChoiceList.add("전체 삭제");
        removeDialog = new MaterialDialog.Builder(this).title("어떤 데이터를 삭제하시겠습니까?").items(removeChoiceList).itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                return false;
            }
        }).positiveText("삭제").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
                switch (dialog.getSelectedIndex()) {
                    case REMOVE_USER:
                        removeUserDeviceData();
                        break;
                    case REMOVE_HISTORY:
                        removePrimeData();
                        break;
                    case REMOVE_ALL:
                        removeUserDeviceData();
                        removePrimeData();
                        break;
                }
            }
        }).negativeText("취소").onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        }).onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                registerBluetooth();
            }
        }).canceledOnTouchOutside(false).build();

        reconnectDialog = new MaterialDialog.Builder(this).title("이전 데이터와 연동하시겠습니까?").content("이전 기기에서 사용한 운동량 데이터가 남아있습니다. 이전 데이터를 삭제하거나 이어서 저장 할 수 있습니다.")
                .positiveText("이어하기").negativeText("처음부터").onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        primeDao.removePrimeData();
                    }
                }).onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deviceListFragment.dismiss();
                        registerBluetooth();
                    }
                }).build();
    }


    private void registerBluetooth() {
        if (isDeviceDataAvailable()) {
            if (gattManager != null) {
                gattManager = null;
            }
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
            primeFragment.setNoneValue();
            return false;
        } else if (primeDao.isUserDataAvailable()) {
            showUserSettingSnackBar();
        }
        return true;
    }


    private void showDeviceSettingSnackBar() {
        state = connectionStateType.NEED_DEVICE_NOT_CONNECT;
        deviceSettingSnackBar = Snackbar.make(coordinatorLayout, R.string.prime_setting_device, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_setting_device_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceListFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
//            fragmentManager.beginTransaction().add(R.id.prime_fragment_frame,deviceListFragment).commit();
            }
        });
        deviceSettingSnackBar.show();
    }


    private void showUserSettingSnackBar() {
        state = connectionStateType.NEED_USER_CONNECT;
        Snackbar.make(coordinatorLayout, R.string.prime_setting_user, Snackbar.LENGTH_LONG).setAction(R.string.prime_setting_user_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDataDialogFragment.show(fragmentManager, getString(R.string.prime_setting_user_tag));
            }
        }).show();
    }


    private void showDisconnectDeviceSnackBar() {
        state = connectionStateType.DISCONNECT_QUEUE;
        Snackbar.make(coordinatorLayout, R.string.prime_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_ignore, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = connectionStateType.DISCONNECTED;
                finish();
            }
        }).show();
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
        if (gattManager != null) {
            gattManager.disconnect();
        }
    }


    private void loadUserDeviceData() {
        UserVo userVo = primeDao.loadUserData();
        DeviceVo deviceVo = primeDao.loadDeviceData();
        userAge = userVo.age;
        userHeight = userVo.height;
        deviceAddress = deviceVo.address;

        setDeviceValue(deviceVo.name, deviceAddress);

        if (Objects.equals(deviceVo.name, "Prime")) {
            deviceType = DeviceType.DEVICE_PRIME;
        } else {
            deviceType = DeviceType.DEVICE_VIDONN;
        }
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


    private void removeUserDeviceData() {
        primeDao.clearSharedPreferenceData();
    }


    private void removePrimeData() {
        primeDao.removePrimeData();
    }


    private void setDeviceValue(String deviceName, String deviceAddress) {
        navDeviceName.setText(deviceName);
        navDeviceAddress.setText("[" + deviceAddress + "]");
    }


    private void setBatteryValue(int batteryValue) {
        Drawable drawable;
        if (0 <= batteryValue && batteryValue <= 25)
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_battery_alert_white_36dp);
        else if (25 < batteryValue && batteryValue <= 100) {
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_battery_std_white_36dp);
        } else {
            drawable = ContextCompat.getDrawable(this, R.drawable.ic_battery_unknown_white_36dp);
        }

        navBattery.setImageDrawable(drawable);
    }


    private void setUpdateValue(String updateValue) {
        navUpdate.setText(updateValue);
    }


    private void setAvailableGatt(List<BluetoothGattService> services) {
        List<BluetoothGattCharacteristic> characteristicList;
        if (deviceType == DeviceType.DEVICE_PRIME) {

            characteristicList = services.get(4).getCharacteristics();
            gattManager.setNotification(characteristicList.get(1), true);
            bluetoothGattCharacteristicForWrite = characteristicList.get(0);
            bluetoothGattCharacteristicForBattery = services.get(2).getCharacteristics().get(0);
        } else {
            gattManager.setNotification(services.get(6).getCharacteristics().get(0), true);
            bluetoothGattCharacteristicForWrite = services.get(5).getCharacteristics().get(0);
            bluetoothGattCharacteristicForBattery = services.get(4).getCharacteristics().get(0);
        }
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
        saveDeviceData(name, address);
        if (primeDao.isPrimeDataAvailable()) {
            reconnectDialog.show();
        } else {
            deviceListFragment.dismiss();
            registerBluetooth();
        }
    }


    @Override
    public void onDeviceUnSelected() {
        registerBluetooth();
    }


    @Override
    public void onSaveUserData() {
        userDataDialogFragment.dismiss();
        gattCallbacks.onDeviceReady();
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
                    state = connectionStateType.CONNECTED;
                    toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                    swipeRefreshLayout.setRefreshing(false);

                }
            });
        }


        public void onDeviceDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == connectionStateType.DISCONNECT_QUEUE) {
                        finish();
                    } else {
                        String none = "--";
                        navDeviceName.setText(none);
                        navDeviceAddress.setText(none);
                        setBatteryValue(-1);
                        toolbarRssi.setText(none);
                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);


                        if (swipeRefreshLayout.isRefreshing() || state == connectionStateType.REFRESH) {
                            registerBluetooth();
                        }
                    }
                }
            });
        }


        public void onServicesFound(final List<BluetoothGattService> services) {
            state = connectionStateType.GATT_RUNNING;
            setAvailableGatt(services);
        }


        public void onServicesNotFound() {
            fail();
        }


        public void onReadSuccess(final BluetoothGattCharacteristic ch) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int batteryValue = ch.getValue()[0];
                    setBatteryValue(batteryValue);
                    state = connectionStateType.GATT_END;
                }
            });

        }


        public void onReadFail() {
            fail();
        }


        @DebugLog
        public void onDeviceReady() {
            gattReadType = GattReadType.READ_TIME;
            if (deviceType == DeviceType.DEVICE_PRIME) {
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_TIME);
            } else {
                VidonnHelper.setBluetoothGatt(gattManager.getGatt(), bluetoothGattCharacteristicForWrite);
                VidonnHelper.writeDate_Time();
            }
        }


        public void onDeviceNotify(final BluetoothGattCharacteristic ch) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (deviceType == DeviceType.DEVICE_PRIME) {
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
                        } else {

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