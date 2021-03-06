package com.rainbow.kam.bt_scanner.activity.nursing;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.dao.NursingDao;
import com.rainbow.kam.bt_scanner.data.item.DateHistoryBlockItem;
import com.rainbow.kam.bt_scanner.data.item.RealmUserActivityItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.helper.NursingGattHelper;
import com.rainbow.kam.bt_scanner.tools.helper.NursingGattHelper.OnHistoryListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-03-03.
 */
public class NursingPresenter implements BaseNursingPresenter, OnHistoryListener {

    final private Context context;
    final private AppCompatActivity activity;

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
        VIDONN_HISTORY_DETAIL,
        VIDONN_PERSONAL_READ,
        VIDONN_PERSONAL_WRITE

    }

    private enum DeviceType {
        DEVICE_PRIME,
        DEVICE_VIDONN
    }

    private connectionStateType state;
    private GattReadType gattReadType;
    private DeviceType deviceType;


    private static final String TAG = NursingPresenter.class.getSimpleName();

    private String userAge, userHeight, deviceName, deviceAddress;

    private NursingViewControl control;

    private RealmUserActivityItem currentRealmUserActivityItem;

    private NursingDao nursingDao;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;

    private NursingGattHelper.OperationX6 operationX6;
    private NursingGattHelper.HistoryX6 historyX6;
    private NursingGattHelper.OperationPrime operationPrime;


    public NursingPresenter(final Context context) {
        this.context = context;
        this.activity = (AppCompatActivity) context;
        this.control = (NursingViewControl) context;

        NursingGattHelper nursingGattHelper = new NursingGattHelper(this);
        operationX6 = nursingGattHelper.getOperationX6();
        historyX6 = nursingGattHelper.getHistoryX6();
        operationPrime = nursingGattHelper.getPrimeHelper();

        state = NursingPresenter.connectionStateType.NONE;
    }


    @Override
    public void initDB() {
        nursingDao = NursingDao.getInstance(context);
    }


    @Override
    public void initializeViews() {
        activity.runOnUiThread(() -> {
            control.setFragments();
            control.setToolbar();
            control.setMaterialView();
            control.setNavigationView();
            control.setMaterialDialog();
        });
    }


    public void onRefresh() {
        state = NursingPresenter.connectionStateType.REFRESH;
        if (gattManager != null && gattManager.isConnected()) {
            disconnectDevice();
        } else {
            registerBluetooth();
        }
    }


    @Override
    public void registerBluetooth() {
        if (isDeviceDataAvailable()) {
            if (gattManager != null) {
                gattManager = null;
            }
            gattManager = new GattManager(context, gattCallbacks);
            if (gattManager.isBluetoothAvailable()) {
                loadUserDeviceData();
                connectDevice();
            } else {
                BluetoothHelper.requestBluetoothEnable(activity);
            }
        }
    }


    private boolean isDeviceDataAvailable() {
        if (nursingDao.isAllDataEmpty()) {
            state = connectionStateType.NEED_DEVICE_NOT_CONNECT;
            activity.runOnUiThread(() -> {
                control.showDeviceSettingSnackBar();
                control.dismissSwipeRefresh();
                control.setEmptyValue();
            });

            return false;
        } else if (nursingDao.isUserDataAvailable()) {
            state = connectionStateType.NEED_USER_CONNECT;
//            control.showUserSettingSnackBar();
        }
        return true;
    }


    @Override
    public void backPressed() {
        if (state == NursingPresenter.connectionStateType.NEED_DEVICE_NOT_CONNECT
                || (state == NursingPresenter.connectionStateType.DISCONNECT_QUEUE
                || !gattManager.isConnected())) {
            applicationExit();
        } else {
            state = NursingPresenter.connectionStateType.DISCONNECT_QUEUE;
            disconnectDevice();
            activity.runOnUiThread(control::showDisconnectDeviceSnackBar);
        }
    }


    @Override
    public void userSettingPressed() {
        if (state == NursingPresenter.connectionStateType.NEED_DEVICE_NOT_CONNECT) {
            control.showDeviceSettingSnackBar();
        } else {
            control.showUserSettingFragment();
        }
    }


    @Override
    public void goalSettingPressed() {
        activity.runOnUiThread(() -> {
            if (state == NursingPresenter.connectionStateType.NEED_DEVICE_NOT_CONNECT) {
                control.showDeviceSettingSnackBar();
            } else {
                control.showGoalSettingFragment();
            }
        });
    }


    private void connectDevice() {
        try {
            gattManager.connect(deviceAddress);
            activity.runOnUiThread(control::showSwipeRefresh);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, context.getString(R.string.bt_fail), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void disconnectDevice() {
        if ((state == NursingPresenter.connectionStateType.NEED_DEVICE_NOT_CONNECT)) {
            return;
        }

        if (gattManager != null) {
            gattManager.disconnect();
        }
    }


    @Override
    public void overWriteTodayHistory() {
        loadUserDeviceData();
        activity.runOnUiThread(() -> {
            int distance = calculateDistance(currentRealmUserActivityItem.getStep());
            currentRealmUserActivityItem.setDistance(distance);
            nursingDao.overWritePrimeData(currentRealmUserActivityItem);

            control.setValue(nursingDao.loadPrimeListData());
        });
    }


    @Override
    public void onReadDayBlockEnd(List<DateHistoryBlockItem> historyItemList) {

        Collections.sort(historyItemList);

        activity.runOnUiThread(() -> {
            int matchingIndex = nursingDao.matchingRealmItem(historyItemList);

            List<Integer> readBlockIndex = new ArrayList<>();

            if (matchingIndex == -1) {
                for (DateHistoryBlockItem dateHistoryBlockItem : historyItemList) {
                    readBlockIndex.add(dateHistoryBlockItem.historyBlockNumber);
                }
            } else {
                for (int i = matchingIndex; i < historyItemList.size(); i++) {
                    readBlockIndex.add(historyItemList.get(i).historyBlockNumber);
                }
            }
            gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readHistoryRecodeDetail(readBlockIndex));
            gattReadType = GattReadType.VIDONN_HISTORY_DETAIL;
        });
    }


    @Override
    public void onReadHourBlockEnd(byte[] bytes) {
        gattManager.writeValue(bluetoothGattCharacteristicForWrite, bytes);
    }


    @Override
    public void onReadAllBlockEnd(List<DateHistoryBlockItem> historyItemList) {
        for (DateHistoryBlockItem dateHistoryBlockItem : historyItemList) {
            Log.e(TAG,
                    "dateHistoryBlockItem.historyBlockNumber : " + dateHistoryBlockItem.historyBlockNumber +
                            "\ndateHistoryBlockItem.historyBlockCalendar : " + dateHistoryBlockItem.historyBlockCalendar +
                            "\ndateHistoryBlockItem.totalStep : " + dateHistoryBlockItem.totalStep
            );
        }
        gattReadType = GattReadType.END;
    }


    private void loadUserDeviceData() {
        UserVo userVo = nursingDao.loadUserData();
        final DeviceVo deviceVo = nursingDao.loadDeviceData();
        userAge = userVo.age;
        userHeight = userVo.height;
        deviceName = deviceVo.name;
        deviceAddress = deviceVo.address;

        activity.runOnUiThread(() -> control.setDeviceValue(deviceVo));

        if (deviceName.contains(context.getString(R.string.device_name_prime))) {
            deviceType = DeviceType.DEVICE_PRIME;
        } else if (deviceName.contains(context.getString(R.string.device_name_Vidonn))) {
            deviceType = DeviceType.DEVICE_VIDONN;
        }

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
        activity.runOnUiThread(() -> control.setUpdateTimeValue(readUpdateTimeValue(ch)));
        switch (deviceType) {
            case DEVICE_PRIME:
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationPrime.readCurrentValue);
                break;
            case DEVICE_VIDONN:
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readCurrentValue());
                break;
        }
    }


    private String readUpdateTimeValue(final BluetoothGattCharacteristic characteristic) {
        final byte[] characteristicValue = characteristic.getValue();
        final String format = context.getString(R.string.nursing_update_format);
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

        activity.runOnUiThread(() -> {
//            nursingDao.savePrimeData(makePrimeItem(characteristic));

            control.setValue(nursingDao.loadPrimeListData());
            control.setGoalRange(nursingDao.loadGoalData());
        });

        gattManager.readValue(bluetoothGattCharacteristicForBattery);
    }


    private RealmUserActivityItem makePrimeItem(final BluetoothGattCharacteristic characteristic)
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

        currentRealmUserActivityItem = new RealmUserActivityItem();
        currentRealmUserActivityItem.setStep(step);
        currentRealmUserActivityItem.setCalorie(kcal);
        currentRealmUserActivityItem.setDistance(distance);

        return currentRealmUserActivityItem;
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


    public void saveUserData(DeviceVo deviceVo) {
        nursingDao.saveDeviceData(deviceVo);
    }


    public void removeDeviceUserData() {
        nursingDao.clearSharedPreferenceData();
    }


    public void removeHistoryData() {
        nursingDao.removePrimeData();
    }


    public boolean isHistoryDataAvailable() {
        return nursingDao.isPrimeDataAvailable();
    }


    public void applicationExit() {
        state = NursingPresenter.connectionStateType.DISCONNECTED;
        activity.finish();
    }


    private void history() {

        RealmResults<RealmUserActivityItem> historyItems = nursingDao.loadPrimeResultData();
        Calendar calendar = Calendar.getInstance();
//        calendar
//        CahistoryItems.last().getCalendar();
    }


    private final GattCustomCallbacks.GattCallbacks gattCallbacks = new GattCustomCallbacks.GattCallbacks() {

        public void onDeviceConnected() {
            state = connectionStateType.CONNECTED;
            activity.runOnUiThread(control::onDeviceConnected);
        }


        public void onDeviceDisconnected() {
            if (state == NursingPresenter.connectionStateType.DISCONNECT_QUEUE) {
                applicationExit();
            } else {
                activity.runOnUiThread(control::onDeviceDisconnected);

                if (state == NursingPresenter.connectionStateType.REFRESH) {
                    registerBluetooth();
                }
            }
        }


        public void onServicesFound(final List<BluetoothGattService> services) {
            state = connectionStateType.GATT_RUNNING;
            setAvailableGatt(services);
        }


        public void onServicesNotFound() {
            control.fail();
        }


        public void onReadSuccess(final BluetoothGattCharacteristic characteristic) {
            Observable<BluetoothGattCharacteristic> observable = Observable.just(characteristic);
            observable.onBackpressureBuffer().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
            observable.map(characteristic1 -> characteristic1.getValue()[0])
                    .subscribe(
                            control::setBatteryValue,
                            throwable -> {
                            },
                            () -> {
                                switch (deviceType) {
                                    case DEVICE_PRIME:
                                        gattReadType = GattReadType.END;
                                        state = connectionStateType.GATT_END;
                                        break;
                                    case DEVICE_VIDONN:

//                                        gattReadType = GattReadType.VIDONN_HISTORY_BLOCK;
//                                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readHistoryRecodeDate());
//                                        gattReadType = GattReadType.VIDONN_PERSONAL_READ;
//                                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readPersonalInfo());
                                        gattReadType = GattReadType.VIDONN_PERSONAL_WRITE;
                                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.writePersonalInfo(new byte[]{(byte) 180, (byte) 90, (byte) 0, (byte) 18}));
                                        break;
                                }
                            }
                    );
        }


        public void onReadFail() {
            control.fail();
        }


        @DebugLog
        public void onDeviceReady() {
            switch (deviceType) {
                case DEVICE_PRIME:
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationPrime.readTime);
                    break;
                case DEVICE_VIDONN:
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, operationX6.readDateTime());
                    break;
            }
            gattReadType = GattReadType.READ_TIME;
        }


        @DebugLog
        public void onDeviceNotify(final BluetoothGattCharacteristic characteristic) {
//            try {
            byte[] val = characteristic.getValue();
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
                    historyX6.readDateBlock(characteristic);

                    break;

                case VIDONN_HISTORY_DETAIL:
                    historyX6.readHourBlock(characteristic);

                    break;

                case VIDONN_PERSONAL_WRITE:
                    gattReadType = GattReadType.VIDONN_PERSONAL_READ;
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


        public void onWriteFail() {
            control.fail();
        }


        public void onRSSIUpdate(final int rssiValue) {
            activity.runOnUiThread(() -> control.updateRssiValue(rssiValue));
        }
    };
}

interface BaseNursingPresenter {
    void initDB();

    void initializeViews();

    void overWriteTodayHistory();

    void registerBluetooth();

    void disconnectDevice();

    void backPressed();

    void userSettingPressed();

    void goalSettingPressed();

    void applicationExit();
}