package com.rainbow.kam.bt_scanner.activity.prime;

import android.bluetooth.*;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.design.widget.*;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.profile.MainActivity;
import com.rainbow.kam.bt_scanner.adapter.device.DeviceAdapter;
import com.rainbow.kam.bt_scanner.data.dao.PrimeDao;
import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.data.vo.*;
import com.rainbow.kam.bt_scanner.fragment.device.DeviceListFragment;
import com.rainbow.kam.bt_scanner.fragment.prime.menu.*;
import com.rainbow.kam.bt_scanner.fragment.prime.user.PrimeFragment;
import com.rainbow.kam.bt_scanner.tools.gatt.*;
import com.rainbow.kam.bt_scanner.tools.helper.*;

import java.text.SimpleDateFormat;
import java.util.*;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class PrimeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        DeviceAdapter.OnDeviceSelectListener,
        UserDataDialogFragment.OnSaveUserDataListener,
        GoalDialogFragment.OnSaveGoalListener,
        PrimeFragment.OnRefreshListener {

    private final String TAG = getClass().getSimpleName();

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

    private enum GattReadType {
        READ_TIME,
        READ_EXERCISE_DATA,
        READ_BATTERY,
        END,
        VIDONN_HISTORY_BLOCK,
        VIDONN_HISTORY_DETAIL;
    }

    private enum DeviceType {
        DEVICE_PRIME,
        DEVICE_VIDONN;
    }

    private connectionStateType state;
    private GattReadType gattReadType;
    private DeviceType deviceType;

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;

    private String userAge, userHeight, deviceName, deviceAddress;
    private String rssiUnit;
    private final String none = "--";

    private PrimeDao primeDao;

    private RealmPrimeItem currentRealmPrimeItem;

    private FragmentManager fragmentManager;

    private PrimeFragment primeFragment;
    private UserDataDialogFragment userDataDialogFragment;
    private DeviceListFragment deviceListFragment;
    private GoalDialogFragment goalDialogFragment;

    private TextView toolbarRssi;
    private ImageView toolbarBluetoothFlag;
    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;


    private ImageView navBattery;
    private TextView navDeviceName, navDeviceAddress, navUpdate;

    private MaterialDialog removeDialog, reconnectDialog;

    private Snackbar deviceSettingSnackBar;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;


    private int historyDetail_Data_Block_Week_ID = 1;// 1~7
    private int historyDetail_Data_Block_Hour_ID = 0;// 0~23

    private int dateBlockIndex = 0;
    private int innerHourBlockIndex = 0;

    private byte[] historyDate_Data = new byte[40];
    private byte[] historyDetail_Data = new byte[67];

    private int dayStepTotal = 0;

    private List<Integer> stepList = new ArrayList<>();


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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menu_prime_setting_device:

                if (deviceSettingSnackBar != null) {
                    deviceSettingSnackBar.dismiss();
                }

                dismissSwipeRefresh();

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

                if (!(state == connectionStateType.NEED_DEVICE_NOT_CONNECT)) {
                    disconnectDevice();
                }

                dismissSwipeRefresh();

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
                state = connectionStateType.DISCONNECT_QUEUE;
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
        toolbarRssi.setText(none);
        rssiUnit = getString(R.string.bt_rssi_unit);
        toolbarBluetoothFlag = (ImageView) findViewById(R.id.prime_toolbar_bluetoothFlag);
    }


    private void setMaterialView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.prime_drawer_layout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.prime_coordinatorLayout);
    }


    private void setNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.prime_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);

        navDeviceName = (TextView) navHeader.findViewById(R.id.prime_device_name);
        navDeviceName.setText(none);

        navDeviceAddress = (TextView) navHeader.findViewById(R.id.prime_device_address);
        navDeviceAddress.setText(none);

        navUpdate = (TextView) navHeader.findViewById(R.id.prime_update);
        navUpdate.setText(none);

        navBattery = (ImageView) navHeader.findViewById(R.id.prime_battery);
    }


    private void setMaterialDialog() {

        final int REMOVE_USER = 0, REMOVE_HISTORY = 1, REMOVE_ALL = 2;
        final List<String> removeChoiceList = new ArrayList<>();
        removeChoiceList.add(getString(R.string.prime_remove_item_user_device));
        removeChoiceList.add(getString(R.string.prime_remove_item_exercise));
        removeChoiceList.add(getString(R.string.prime_remove_item_all));
        removeDialog = new MaterialDialog.Builder(this)
                .title(R.string.prime_remove_title)
                .items(removeChoiceList)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        return false;
                    }
                })
                .positiveText(R.string.prime_remove_accept).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        switch (dialog.getSelectedIndex()) {
                            case REMOVE_USER:
                                primeDao.clearSharedPreferenceData();
                                break;
                            case REMOVE_HISTORY:
                                primeDao.removePrimeData();
                                break;
                            case REMOVE_ALL:
                                primeDao.clearSharedPreferenceData();
                                primeDao.removePrimeData();
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.prime_remove_denied).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        registerBluetooth();
                    }
                })
                .canceledOnTouchOutside(false).build();


        reconnectDialog = new MaterialDialog.Builder(this)
                .title(R.string.prime_reconnect_title)
                .content(R.string.prime_reconnect_content)
                .positiveText(R.string.prime_reconnect_accept)
                .negativeText(R.string.prime_reconnect_denied)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        primeDao.removePrimeData();
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
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
                checkDeviceType();
                setNavHeaderDeviceValue();

                connectDevice();
            } else {
                BluetoothHelper.requestBluetoothEnable(this);
            }
        }
    }


    private boolean isDeviceDataAvailable() {
        if (primeDao.isAllDataEmpty()) {
            state = connectionStateType.NEED_DEVICE_NOT_CONNECT;
            showDeviceSettingSnackBar();
            dismissSwipeRefresh();
            primeFragment.setValueEmpty();
            return false;
        } else if (primeDao.isUserDataAvailable()) {
            state = connectionStateType.NEED_USER_CONNECT;
            showUserSettingSnackBar();
        }
        return true;
    }


    private void showDeviceSettingSnackBar() {

        deviceSettingSnackBar = Snackbar.make(coordinatorLayout, R.string.prime_setting_device, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_setting_device_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceListFragment.show(fragmentManager, getString(R.string.prime_setting_device_tag));
            }
        });
        deviceSettingSnackBar.show();
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

        Snackbar.make(coordinatorLayout, R.string.prime_disconnect_snack, Snackbar.LENGTH_INDEFINITE).setAction(R.string.prime_ignore, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = connectionStateType.DISCONNECTED;
                finish();
            }
        }).show();
    }


    private void dismissSwipeRefresh() {
        if (primeFragment.isRefreshing()) {
            primeFragment.setRefreshing(false);
        }
    }


    private void connectDevice() {
        try {
            gattManager.connect(deviceAddress);
            primeFragment.setRefreshing(true);
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
        deviceName = deviceVo.name;
        deviceAddress = deviceVo.address;
    }


    private void checkDeviceType() {
        if (deviceName.contains(getString(R.string.device_name_prime))) {
            deviceType = DeviceType.DEVICE_PRIME;
        } else if (deviceName.contains(getString(R.string.device_name_Vidonn))) {
            deviceType = DeviceType.DEVICE_VIDONN;
        }
    }


    private void setNavHeaderDeviceValue() {
        navDeviceName.setText(deviceName);
        navDeviceAddress.setText(deviceAddress);
    }


    private void setNavHeaderBatteryValue(int batteryValue) {
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


    private void setNavHeaderUpdateValue(String updateValue) {
        navUpdate.setText(updateValue);
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
    public void onDeviceSelect(DeviceVo deviceVo) {

        primeDao.saveDeviceData(deviceVo);

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
        overWriteHistory(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeFragment.setPrimeValue(primeDao.loadPrimeListData());
            }
        });
    }


    @Override
    public void onSaveGoal(GoalVo goalVo) {
        goalDialogFragment.dismiss();
        primeFragment.setCircleCounterGoalRange(goalVo);
    }


    private void overWriteHistory(boolean isOverWriteAllData) {
        loadUserDeviceData();

        int distance = calculateDistance(currentRealmPrimeItem.getStep());
        currentRealmPrimeItem.setDistance(distance);
        primeDao.overWritePrimeData(currentRealmPrimeItem, isOverWriteAllData);
    }


    private void setAvailableGatt(List<BluetoothGattService> services) {
        switch (deviceType) {
            case DEVICE_PRIME:
                gattManager.setNotification(services.get(4).getCharacteristics().get(1), true);
                bluetoothGattCharacteristicForWrite = services.get(4).getCharacteristics().get(0);
                bluetoothGattCharacteristicForBattery = services.get(2).getCharacteristics().get(0);
                break;
            case DEVICE_VIDONN:
                gattManager.setNotification(services.get(5).getCharacteristics().get(1), true);
                bluetoothGattCharacteristicForWrite = services.get(5).getCharacteristics().get(0);
                bluetoothGattCharacteristicForBattery = services.get(4).getCharacteristics().get(0);
                break;
        }
    }


    private void onReadTime(final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setNavHeaderUpdateValue(readUpdateTimeValue(ch));
            }
        });
        switch (deviceType) {
            case DEVICE_PRIME:
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_PRIME);
                break;
            case DEVICE_VIDONN:
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readCurrentValue());
                break;
        }
    }


    private String readUpdateTimeValue(final BluetoothGattCharacteristic characteristic) {
        final byte[] characteristicValue = characteristic.getValue();
        final String format = getString(R.string.prime_update_format);
        final SimpleDateFormat update = new SimpleDateFormat(format, Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        if (deviceType == DeviceType.DEVICE_PRIME) {
            calendar.set(Calendar.YEAR, characteristicValue[2]);
            calendar.set(Calendar.MONTH, characteristicValue[3] - 1);
            calendar.set(Calendar.DAY_OF_MONTH, characteristicValue[4]);
            calendar.set(Calendar.HOUR_OF_DAY, characteristicValue[5]);
            calendar.set(Calendar.MINUTE, characteristicValue[6]);
            calendar.set(Calendar.SECOND, characteristicValue[7]);
        } else {
            calendar.set(Calendar.YEAR, (characteristicValue[3] + characteristicValue[4]));
            calendar.set(Calendar.MONTH, characteristicValue[5] - 1);
            calendar.set(Calendar.DAY_OF_MONTH, characteristicValue[6]);
            calendar.set(Calendar.HOUR_OF_DAY, characteristicValue[7]);
            calendar.set(Calendar.MINUTE, characteristicValue[8]);
            calendar.set(Calendar.SECOND, characteristicValue[9]);
        }


        return update.format(calendar.getTime());
    }


    private void onReadExerciseData(final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeDao.savePrimeData(makePrimeItem(characteristic));
                primeFragment.setPrimeValue(primeDao.loadPrimeListData());
                primeFragment.setCircleCounterGoalRange(primeDao.loadGoalData());
            }
        });

        gattManager.readValue(bluetoothGattCharacteristicForBattery);
    }


    private RealmPrimeItem makePrimeItem(final BluetoothGattCharacteristic characteristic)
            throws ArrayIndexOutOfBoundsException {

        final byte[] characteristicValue = characteristic.getValue();

        StringBuilder hexStep = new StringBuilder();
        StringBuilder hexCalorie = new StringBuilder();
        switch (deviceType) {
            case DEVICE_PRIME:
                hexStep.append(formatHexData(characteristicValue, 2))
                        .append(formatHexData(characteristicValue, 3))
                        .append(formatHexData(characteristicValue, 4));

                hexCalorie
                        .append(formatHexData(characteristicValue, 5))
                        .append(formatHexData(characteristicValue, 6))
                        .append(formatHexData(characteristicValue, 7));
                break;
            case DEVICE_VIDONN:
                hexStep.append(formatHexData(characteristicValue, 7))
                        .append(formatHexData(characteristicValue, 6))
                        .append(formatHexData(characteristicValue, 5))
                        .append(formatHexData(characteristicValue, 4));

                hexCalorie
                        .append(formatHexData(characteristicValue, 11))
                        .append(formatHexData(characteristicValue, 10))
                        .append(formatHexData(characteristicValue, 9))
                        .append(formatHexData(characteristicValue, 8));
                break;
        }


        final int step, kcal, distance;

        final int radix = 16;
        step = Integer.parseInt(hexStep.toString(), radix);
        kcal = Integer.parseInt(hexCalorie.toString(), radix);
        distance = calculateDistance(step);


        currentRealmPrimeItem = new RealmPrimeItem();
        currentRealmPrimeItem.setStep(step);
        currentRealmPrimeItem.setCalorie(kcal);
        currentRealmPrimeItem.setDistance(distance);

        return currentRealmPrimeItem;
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


    private void onReadBattery(final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int batteryValue = characteristic.getValue()[0];
                setNavHeaderBatteryValue(batteryValue);
            }
        });
        switch (deviceType) {
            case DEVICE_PRIME:
                gattReadType = GattReadType.END;
                state = connectionStateType.GATT_END;
                break;
            case DEVICE_VIDONN:
                gattReadType = GattReadType.END;
                state = connectionStateType.GATT_END;
//                gattReadType = GattReadType.VIDONN_HISTORY_BLOCK;
//                gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readHistoryRecodeDate());
                break;
        }
    }


    private void onReadHistoryBlock(final BluetoothGattCharacteristic characteristic) {
        byte[] blockData = characteristic.getValue();

        int[][] historyDate_Map;
        if (dateBlockIndex == 0) {
            if (blockData.length < 20) {
                dateBlockIndex = 0;

                historyDate_Map = VidonnHelper.DeCodeX6.decode_HistoryRecodeDate(blockData, blockData.length);
                for (int[] aHistoryDate_Map : historyDate_Map) {
                    Log.e(TAG, "dateBlockIndex : " + dateBlockIndex + " / " +
                            aHistoryDate_Map[0] + "Block  Date=" + aHistoryDate_Map[1] + "/"
                            + aHistoryDate_Map[2] + "/" + aHistoryDate_Map[3]);
                }
            } else {
                dateBlockIndex = 1;
                System.arraycopy(blockData, 0, historyDate_Data, 0, blockData.length);
            }
        } else if (dateBlockIndex == 1) {
            dateBlockIndex = 0;
            int dataLength = 20 + blockData.length;

            System.arraycopy(blockData, 0, historyDate_Data, 20, dataLength - 20);

            historyDate_Map = VidonnHelper.DeCodeX6.decode_HistoryRecodeDate(historyDate_Data, dataLength);
            for (int[] aHistoryDate_Map : historyDate_Map) {
                Log.e(TAG, "dateBlockIndex : " + dateBlockIndex + " / " +
                        aHistoryDate_Map[0] + "Block  Date=" + aHistoryDate_Map[1] + "/"
                        + aHistoryDate_Map[2] + "/" + aHistoryDate_Map[3]);
            }

            gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readHistoryRecodeDetail((byte) historyDetail_Data_Block_Week_ID, (byte) historyDetail_Data_Block_Hour_ID));
            gattReadType = GattReadType.VIDONN_HISTORY_DETAIL;
        }
    }


    private boolean updateBlockIndex() {
        historyDetail_Data_Block_Hour_ID++;
        if (historyDetail_Data_Block_Hour_ID == 24) {
            historyDetail_Data_Block_Hour_ID = 0;

            stepList.add(dayStepTotal);
            dayStepTotal = 0;

            historyDetail_Data_Block_Week_ID++;
        }
        if ((historyDetail_Data_Block_Week_ID > 7)) {
            Log.e(TAG, "Over");

            for (int step : stepList) {
                Log.e(TAG, "Step : " + step);
            }

            gattReadType = GattReadType.END;
            historyDetail_Data_Block_Week_ID = 1;
            historyDetail_Data_Block_Hour_ID = 0;
            return false;
        }
        return true;
    }


    private void addHistoryDetail(byte[] detailData, int dataLength) {
        int innerHourBlockCount = 20;
        int indexStart = innerHourBlockIndex * innerHourBlockCount;
        for (int i = indexStart; i < indexStart + dataLength; i++) {
            historyDetail_Data[i] = detailData[i - indexStart];
        }
        if (innerHourBlockIndex != 3) {
            innerHourBlockIndex++;
        } else {
            innerHourBlockIndex = 0;
        }
    }


    private void onReadDetailBlock(final BluetoothGattCharacteristic characteristic) {
        byte[] detailData = characteristic.getValue();
        int dataLength = detailData.length;

        switch (innerHourBlockIndex) {
            case 0:
                Log.e(TAG, historyDetail_Data_Block_Week_ID + "Block  "
                        + historyDetail_Data_Block_Hour_ID + " Hour / " + detailData.length + " data.length");

                if (dataLength < 15) {
                    if (updateBlockIndex()) {

                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readHistoryRecodeDetail((byte) historyDetail_Data_Block_Week_ID,
                                (byte) historyDetail_Data_Block_Hour_ID));
                    }
                } else {
                    addHistoryDetail(detailData, dataLength);
                }
                break;
            case 1:
            case 2:
                addHistoryDetail(detailData, dataLength);
                break;
            case 3:
                addHistoryDetail(detailData, dataLength);


                int[][] steps = VidonnHelper.DeCodeX6.decode_HistoryRecodeDetail(historyDetail_Data);

                for (int i = 1; i < steps.length; i++) {
                    dayStepTotal += steps[i][1];
                }

                if (updateBlockIndex()) {
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readHistoryRecodeDetail((byte) historyDetail_Data_Block_Week_ID,
                            (byte) historyDetail_Data_Block_Hour_ID));
                }
                break;
        }
    }


    private final GattCustomCallbacks.GattCallbacks gattCallbacks = new GattCustomCallbacks.GattCallbacks() {

        public void onDeviceConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    state = connectionStateType.CONNECTED;
                    toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_connected_white_24dp);

                    dismissSwipeRefresh();

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

                        navDeviceName.setText(none);
                        navDeviceAddress.setText(none);
                        navUpdate.setText(none);
                        setNavHeaderBatteryValue(-1);
                        toolbarRssi.setText(none);
                        toolbarBluetoothFlag.setImageResource(R.drawable.ic_bluetooth_disabled_white_24dp);


                        if (primeFragment.isRefreshing() || state == connectionStateType.REFRESH) {
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


        public void onReadSuccess(final BluetoothGattCharacteristic characteristic) {
            onReadBattery(characteristic);
        }


        public void onReadFail() {
            fail();
        }


        @DebugLog
        public void onDeviceReady() {
            switch (deviceType) {
                case DEVICE_PRIME:
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_TIME);
                    break;
                case DEVICE_VIDONN:
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readDate_Time());
                    break;
            }
            gattReadType = GattReadType.READ_TIME;
        }


        @DebugLog
        public synchronized void onDeviceNotify(final BluetoothGattCharacteristic characteristic) {
            try {
                switch (gattReadType) {
                    case READ_TIME:
                        onReadTime(characteristic);
                        gattReadType = GattReadType.READ_EXERCISE_DATA;

                        break;

                    case READ_EXERCISE_DATA:
                        onReadExerciseData(characteristic);
                        gattReadType = GattReadType.READ_BATTERY;

                        break;

                    case VIDONN_HISTORY_BLOCK:
                        onReadHistoryBlock(characteristic);

                        break;

                    case VIDONN_HISTORY_DETAIL:
                        onReadDetailBlock(characteristic);

                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception " + e.getMessage());

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