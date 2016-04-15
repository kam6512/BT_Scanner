package com.rainbow.kam.bt_scanner.ui.acativity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow.kam.ble_gatt_manager.BluetoothHelper;
import com.rainbow.kam.ble_gatt_manager.GattCustomCallbacks;
import com.rainbow.kam.ble_gatt_manager.GattManager;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.ui.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.data.dao.NursingDao;
import com.rainbow.kam.bt_scanner.data.item.UserMovementItem;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;
import com.rainbow.kam.bt_scanner.ui.fragment.device.DeviceListFragment;
import com.rainbow.kam.bt_scanner.ui.fragment.nursing.menu.GoalDialogFragment;
import com.rainbow.kam.bt_scanner.ui.fragment.nursing.menu.UserDataDialogFragment;
import com.rainbow.kam.bt_scanner.ui.fragment.nursing.user.NursingFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-04-15.
 */

@EActivity(R.layout.a_nursing)
public class NursingActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        UserDataDialogFragment.OnSaveUserDataListener,
        GoalDialogFragment.OnSaveGoalListener,
        NursingFragment.OnRefreshListener,
        GattCustomCallbacks {

    private final static String TAG = NursingActivity.class.getSimpleName();

    private final static String RSSI_LABEL = " dBm";
    private final static String RSSI_NONE = " wait....";

    @ViewById(R.id.nursing_toolbar) Toolbar toolbar;
    @ViewById(R.id.nursing_toolbar_rssi) TextView toolbarRssi;
    @ViewById(R.id.nursing_toolbar_bluetooth_flag)
    ImageView toolbarBluetoothFlag;
    @ViewById(R.id.nursing_coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @ViewById(R.id.nursing_drawer_layout) DrawerLayout drawerLayout;

    @ViewById(R.id.nursing_nav_view) NavigationView navigationView;
    @ViewById(R.id.nursing_battery) ImageView navBattery;
    @ViewById(R.id.nursing_device_name) TextView navDeviceName;
    @ViewById(R.id.nursing_device_address) TextView navDeviceAddress;
    @ViewById(R.id.nursing_update) TextView navUpdate;

    Snackbar deviceSettingSnackBar;

    FragmentManager fragmentManager;

    private final static int FRAGMENT_FRAME = R.id.nursing_fragment_frame;

    NursingFragment nursingFragment;
    UserDataDialogFragment userDataDialogFragment;
    DeviceListFragment deviceListFragment;
    GoalDialogFragment goalDialogFragment;


    private enum GATT_STATE {
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

    private enum GATT_ACTION {
        READ_TIME,
        WRITE_TIME,
        READ_EXERCISE_DATA,
        READ_BATTERY,
        END,
        VIDONN_HISTORY_BLOCK,
        VIDONN_HISTORY_DETAIL,
        VIDONN_PERSONAL_READ,
        VIDONN_PERSONAL_WRITE

    }

    private GATT_STATE state;
    private GATT_ACTION action;


    private String userAge, userHeight, deviceName, deviceAddress;

    //    private UserMovementItem currentRealmUserActivityItem;
    private NursingDao nursingDao;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        state = GATT_STATE.NONE;

        nursingDao = NursingDao.getInstance(this);

        fragmentManager = getSupportFragmentManager();

        deviceListFragment = new DeviceListFragment();
        userDataDialogFragment = new UserDataDialogFragment();
        goalDialogFragment = new GoalDialogFragment();

        nursingFragment = new NursingFragment();
        fragmentManager.beginTransaction().replace(FRAGMENT_FRAME, nursingFragment).commit();
    }


    @Override protected void onResume() {
        super.onResume();
        connectDevice();
    }


    @Override protected void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @Override public void onRefresh() {
        state = GATT_STATE.REFRESH;
        if (gattManager != null && gattManager.isConnected()) {
            disconnectDevice();
        } else {
            connectDevice();
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (state == GATT_STATE.NEED_DEVICE_NOT_CONNECT || (state == GATT_STATE.DISCONNECT_QUEUE || !gattManager.isConnected())) {
                state = GATT_STATE.DISCONNECTED;
                finish();
            } else {
                state = GATT_STATE.DISCONNECT_QUEUE;
                disconnectDevice();
                runOnUiThread(this::showDisconnectDeviceSnackBar);
            }
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
            case R.id.menu_nursing_setting_device:

                disconnectDevice();

                dismissSwipeRefresh();

                deviceListFragment.show(fragmentManager, getString(R.string.nursing_setting_device_tag));

                if (deviceSettingSnackBar != null) {
                    deviceSettingSnackBar.dismiss();
                }

                return true;

            case R.id.menu_nursing_setting_user:

                if (state == GATT_STATE.NEED_DEVICE_NOT_CONNECT) {
                    showDeviceSettingSnackBar();
                } else {
                    userDataDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_user_tag));
                }

                return true;

            case R.id.menu_nursing_setting_goal:

                if (state == GATT_STATE.NEED_DEVICE_NOT_CONNECT) {
                    showDeviceSettingSnackBar();
                } else {
                    goalDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_goal_tag));
                }

                return true;

            case R.id.menu_nursing_about_dev:


                return true;

            case R.id.menu_nursing_about_setting:

                disconnectDevice();

                dismissSwipeRefresh();

                return true;

            case R.id.menu_nursing_about_about:

                Snackbar.make(coordinatorLayout, "준비중입니다..", Snackbar.LENGTH_SHORT).show();

                return true;
            default:
                return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BluetoothHelper.onRequestEnableResult(requestCode, resultCode, this);
        BluetoothHelper.requestBluetoothPermission(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @AfterViews void setToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbarRssi.setText(RSSI_NONE);
    }


    @AfterViews void setNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);
        navDeviceName.setText(RSSI_NONE);
        navDeviceAddress.setText(RSSI_NONE);
        navUpdate.setText(RSSI_NONE);
    }


    @Override public void onDeviceSelect(DeviceVo deviceVo) {
        nursingDao.saveDeviceData(deviceVo);

        deviceListFragment.dismiss();
        connectDevice();
    }


    @Override public void onDeviceUnSelected() {
        connectDevice();
    }


    @Override public void onSaveGoal(GoalVo goalVo) {
        goalDialogFragment.dismiss();
        nursingFragment.setCircleCounterGoalRange(goalVo);
    }


    @Override public void onSaveUserData() {
        userDataDialogFragment.dismiss();
    }


    private void connectDevice() {

        if (nursingDao.isAllDataEmpty()) {
            state = GATT_STATE.NEED_DEVICE_NOT_CONNECT;
            runOnUiThread(() -> {
                showDeviceSettingSnackBar();
                dismissSwipeRefresh();
                setEmptyValue();
            });

            return;
        } else if (nursingDao.isUserDataAvailable()) {
            state = GATT_STATE.NEED_USER_CONNECT;
            showUserSettingSnackBar();
        }

        gattManager = new GattManager(getApplication(), this);
        if (gattManager.isBluetoothAvailable()) {
            loadUserDeviceData();
            gattManager.connect(deviceAddress);
            nursingFragment.setRefreshing(true);
        } else {
            BluetoothHelper.requestBluetoothEnable(this);
        }
    }


    public void disconnectDevice() {
        if ((state == GATT_STATE.NEED_DEVICE_NOT_CONNECT)) {
            return;
        }

        if (gattManager != null) {
            gattManager.disconnect();
        }
    }


    private void loadUserDeviceData() {
        UserVo userVo = nursingDao.loadUserData();
        final DeviceVo deviceVo = nursingDao.loadDeviceData();
        userAge = userVo.age;
        userHeight = userVo.height;
        deviceName = deviceVo.name;
        deviceAddress = deviceVo.address;

        runOnUiThread(() -> setDeviceValue(deviceVo));
    }


    public void showDeviceSettingSnackBar() {
        deviceSettingSnackBar = Snackbar.make(coordinatorLayout, R.string.nursing_setting_device, Snackbar.LENGTH_INDEFINITE).setAction(R.string.nursing_setting_device_action, v -> {
            deviceListFragment.show(fragmentManager, getString(R.string.nursing_setting_device_tag));
        });
        deviceSettingSnackBar.show();
    }


    public void showUserSettingSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.nursing_setting_user, Snackbar.LENGTH_LONG).setAction(R.string.nursing_setting_user_action, v -> {
            userDataDialogFragment.show(fragmentManager, getString(R.string.nursing_setting_user_tag));
        }).show();
    }


    public void showDisconnectDeviceSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.nursing_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.nursing_ignore, v -> {
            state = GATT_STATE.DISCONNECTED;
            finish();
        }).show();
    }


    public void showSwipeRefresh() {
        nursingFragment.setRefreshing(true);
    }


    public void dismissSwipeRefresh() {
        if (nursingFragment.isRefreshing()) {
            nursingFragment.setRefreshing(false);
        }
    }


    private void onReadTime(final BluetoothGattCharacteristic ch) {
        runOnUiThread(() -> setUpdateTimeValue(readUpdateTimeValue(ch)));

        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readCurrentValue());
    }


    private String readUpdateTimeValue(final BluetoothGattCharacteristic characteristic) {
        final byte[] characteristicValue = characteristic.getValue();
        final String format = getString(R.string.nursing_update_format);
        final SimpleDateFormat update = new SimpleDateFormat(format, Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, (characteristicValue[3] + characteristicValue[4]));
        calendar.set(Calendar.MONTH, characteristicValue[5] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, characteristicValue[6]);
        calendar.set(Calendar.HOUR_OF_DAY, characteristicValue[7]);
        calendar.set(Calendar.MINUTE, characteristicValue[8]);
        calendar.set(Calendar.SECOND, characteristicValue[9]);


        return update.format(calendar.getTime());
    }


    private void onReadExerciseData(final BluetoothGattCharacteristic characteristic) {

        runOnUiThread(() -> {
//            setValue(makePrimeItem(characteristic));
            nursingFragment.setCircleCounterValue(makePrimeItem(characteristic));
            setGoalRange(nursingDao.loadGoalData());
        });

        gattManager.readValue(bluetoothGattCharacteristicForBattery);
    }


    private UserMovementItem makePrimeItem(final BluetoothGattCharacteristic characteristic)
            throws ArrayIndexOutOfBoundsException {

        final byte[] characteristicValue = characteristic.getValue();

        StringBuilder hexStep = new StringBuilder();
        StringBuilder hexCalorie = new StringBuilder();

        hexStep.append(formatHexData(characteristicValue, 7))
                .append(formatHexData(characteristicValue, 6))
                .append(formatHexData(characteristicValue, 5))
                .append(formatHexData(characteristicValue, 4));

        hexCalorie
                .append(formatHexData(characteristicValue, 11))
                .append(formatHexData(characteristicValue, 10))
                .append(formatHexData(characteristicValue, 9))
                .append(formatHexData(characteristicValue, 8));


        final int step, kcal, distance;

        final int radix = 16;
        step = Integer.parseInt(hexStep.toString(), radix);
        kcal = Integer.parseInt(hexCalorie.toString(), radix);
        distance = calculateDistance(step);

        UserMovementItem item = new UserMovementItem();
        item.setStep(step);
        item.setCalorie(kcal);
        item.setDistance(distance);

        return item;
    }


    private String formatHexData(byte[] characteristicValue, int index) {
        return String.format("%02x", characteristicValue[index] & 0xff);
    }


    private int calculateDistance(int step) {
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
        return (int) ((height * convertValue) * step) / 100;
    }


    public void setDeviceValue(DeviceVo deviceValue) {
        navDeviceName.setText(deviceValue.name);
        navDeviceAddress.setText(deviceValue.address);
    }


    public void setBatteryValue(int batteryValue) {
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


    public void setUpdateTimeValue(String updateValue) {
        navUpdate.setText(updateValue);
    }


    public void updateRssiValue(int rssiValue) {
        toolbarRssi.setText(rssiValue + RSSI_LABEL);
    }


    public void setValue(List<UserMovementItem> nursingValue) {
        nursingFragment.setValue(nursingValue);
    }


    public void setGoalRange(GoalVo goalVo) {
        nursingFragment.setCircleCounterGoalRange(goalVo);
    }


    public void setEmptyValue() {
        nursingFragment.setValueEmpty();
    }


    @UiThread public void fail() {
        nursingFragment.setTextFail();
    }


    @UiThread @Override public void onDeviceConnected() {
        state = GATT_STATE.CONNECTED;
        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);
        dismissSwipeRefresh();
    }


    @Override public void onDeviceConnectFail(Exception e) {
        fail();
    }


    @Override public void onDeviceDisconnected() {
        if (state == GATT_STATE.DISCONNECT_QUEUE) {
            state = GATT_STATE.DISCONNECTED;
            finish();
        } else {
            navDeviceName.setText(RSSI_NONE);
            navDeviceAddress.setText(RSSI_NONE);
            navUpdate.setText(RSSI_NONE);
            setBatteryValue(-1);

            toolbarRssi.setText(RSSI_NONE);
            toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);

            if (state == GATT_STATE.REFRESH) {
                connectDevice();
            }
        }
    }


    @Override public void onDeviceDisconnectFail(Exception e) {
        fail();
    }


    @Override public void onServicesFound(BluetoothGatt bluetoothGatt) {
        state = GATT_STATE.GATT_RUNNING;

        gattManager.setNotification(bluetoothGatt.getServices().get(5).getCharacteristics().get(1), true);
        bluetoothGattCharacteristicForWrite = bluetoothGatt.getServices().get(5).getCharacteristics().get(0);
        bluetoothGattCharacteristicForBattery = bluetoothGatt.getServices().get(4).getCharacteristics().get(0);

    }


    @Override public void onServicesNotFound(Exception e) {
        fail();
    }


    @Override public void onDeviceReady() {
        action = GATT_ACTION.READ_TIME;
        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readDateTime());
    }


    @Override
    public void onDeviceNotify(BluetoothGattCharacteristic characteristic) {

//            try {
        byte[] val = characteristic.getValue();
        switch (action) {
            case READ_TIME:
                onReadTime(characteristic);
                action = GATT_ACTION.READ_EXERCISE_DATA;

                break;

            case READ_EXERCISE_DATA:
                onReadExerciseData(characteristic);
                action = GATT_ACTION.READ_BATTERY;

                break;

            case VIDONN_HISTORY_BLOCK:
                historyX6.readDateBlock(characteristic);

                break;

            case VIDONN_HISTORY_DETAIL:
                historyX6.readHourBlock(characteristic);

                break;

            case VIDONN_PERSONAL_WRITE:
                action = GATT_ACTION.VIDONN_PERSONAL_READ;
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readPersonalInfo());


            case VIDONN_PERSONAL_READ:
                for (int i = 0; i < val.length; i++) {
                    int data = val[i];
                    if (data < 0) {
                        data = data + 256;
                    }
                    Log.e(TAG, "VIDONN_PERSONAL_READ [" + i + "] = " + data);
//                        Log.e(TAG, "VIDONN_PERSONAL_READ HEX [" + i + "] = " + Integer.toHexString(data));
                }
                for (int i = 0; i < val.length; i++) {
                    int data = val[i];
                    if (data < 0) {
                        data = data + 256;
                    }
                    Log.e(TAG, "VIDONN_PERSONAL_READ HEX [" + i + "] = " + Integer.toHexString(data));
                }
                break;
            case END:

                for (int i = 0; i < val.length; i++) {
                    int data = val[i];
                    if (data < 0) {
                        data = data + 256;
                    }
                    Log.e(TAG, "END [" + i + "] = " + data);
//                        Log.e(TAG, "VIDONN_PERSONAL_READ HEX [" + i + "] = " + Integer.toHexString(data));
                }
                break;

            default:
                break;
        }
//            } catch (Exception e) {
//                Log.e(TAG, "Exception " + e.getMessage());
//                control.fail();
//            }
    }


    @Override public void onSetNotificationSuccess() {

    }


    @Override public void onSetNotificationFail(Exception e) {

    }


    @Override
    public void onReadSuccess(BluetoothGattCharacteristic characteristic) {
        Observable<BluetoothGattCharacteristic> observable = Observable.just(characteristic);
        observable.onBackpressureBuffer().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
        observable.map(characteristic1 -> characteristic1.getValue()[0])
                .subscribe(
                        this::setBatteryValue,
                        throwable -> {
                        },
                        () -> {
                            action = GATT_ACTION.VIDONN_HISTORY_BLOCK;
                            gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readHistoryRecodeDate());
//                                        gattReadType = GattReadType.VIDONN_PERSONAL_READ;
//                                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readPersonalInfo());
//                                        gattReadType = GattReadType.VIDONN_PERSONAL_WRITE;
//                                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.writePersonalInfo(new byte[]{(byte) 180, (byte) 90, (byte) 0, (byte) 18}));

                        }
                );
    }


    @Override public void onReadFail(Exception e) {
        fail();
    }


    @Override public void onWriteSuccess() {

    }


    @Override public void onWriteFail(Exception e) {
        fail();
    }


    @Override public void onRSSIUpdate(int rssi) {
        updateRssiValue(rssi);
    }


    @Override public void onRSSIMiss() {

    }
}
