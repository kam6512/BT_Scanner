package com.rainbow.kam.bt_scanner.mvp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.dao.PrimeDao;
import com.rainbow.kam.bt_scanner.data.item.ActivityHistoryItem;
import com.rainbow.kam.bt_scanner.data.item.RealmUserActivityItem;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;
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

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kam6512 on 2016-03-03.
 */
public class NursingPresenter implements BaseNursingPresenter {

    final private Context context;
    final private AppCompatActivity activity;

    public enum connectionStateType {
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

    public enum GattReadType {
        READ_TIME,
        READ_EXERCISE_DATA,
        READ_BATTERY,
        END,
        VIDONN_HISTORY_BLOCK,
        VIDONN_HISTORY_DETAIL
    }

    public enum DeviceType {
        DEVICE_PRIME,
        DEVICE_VIDONN
    }

    private connectionStateType state;
    private GattReadType gattReadType;
    private DeviceType deviceType;

    private int historyDetail_Data_Block_Week_ID = 1;// 1~7
    private int historyDetail_Data_Block_Hour_ID = 0;// 0~23

    private int dateBlockIndex = 0;
    private int innerHourBlockIndex = 0;

    private byte[] historyDate_Data = new byte[40];
    private byte[] historyDetail_Data = new byte[67];

    List<ActivityHistoryItem> historyItemList = new ArrayList<>(7);

    public static final String TAG = NursingPresenter.class.getSimpleName();

    private String userAge, userHeight, deviceName, deviceAddress;

    private NursingViewControl control;

    private RealmUserActivityItem currentRealmUserActivityItem;

    private PrimeDao primeDao;

    private GattManager gattManager;

    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForBattery;


    public NursingPresenter(final Context context) {
        this.context = context;
        this.activity = (AppCompatActivity) context;
        this.control = (NursingViewControl) context;
        state = NursingPresenter.connectionStateType.NONE;
    }


    @Override
    public void initDB() {
        primeDao = PrimeDao.getInstance(context);
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
        if (primeDao.isAllDataEmpty()) {
            state = connectionStateType.NEED_DEVICE_NOT_CONNECT;
            activity.runOnUiThread(() -> {
                control.showDeviceSettingSnackBar();
                control.dismissSwipeRefresh();
                control.setPrimeEmptyValue();
            });

            return false;
        } else if (primeDao.isUserDataAvailable()) {
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

            activity.runOnUiThread(control::showDisconnectDeviceSnackBar);


            disconnectDevice();
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


    private void loadUserDeviceData() {
        UserVo userVo = primeDao.loadUserData();
        final DeviceVo deviceVo = primeDao.loadDeviceData();
        userAge = userVo.age;
        userHeight = userVo.height;
        deviceName = deviceVo.name;
        deviceAddress = deviceVo.address;

        checkDeviceType();
        activity.runOnUiThread(() -> control.setDeviceValue(deviceVo));
    }


    private void checkDeviceType() {
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
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_PRIME);
                break;
            case DEVICE_VIDONN:
                gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readCurrentValue());
                break;
        }
    }


    private String readUpdateTimeValue(final BluetoothGattCharacteristic characteristic) {
        final byte[] characteristicValue = characteristic.getValue();
        final String format = context.getString(R.string.prime_update_format);
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
            primeDao.savePrimeData(makePrimeItem(characteristic));

        });


        activity.runOnUiThread(() -> {
            control.setPrimeValue(primeDao.loadPrimeListData());
            control.setPrimeGoalRange(primeDao.loadGoalData());

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


    @Override
    public void overWriteHistory(boolean isOverWriteAllData) {
        loadUserDeviceData();
        activity.runOnUiThread(() -> {
            int distance = calculateDistance(currentRealmUserActivityItem.getStep());
            currentRealmUserActivityItem.setDistance(distance);
            primeDao.overWritePrimeData(currentRealmUserActivityItem, isOverWriteAllData);


            control.setPrimeValue(primeDao.loadPrimeListData());
        });

    }


    private void onReadHistoryBlock(final BluetoothGattCharacteristic characteristic) {

        byte[] blockData = characteristic.getValue();
        int[][] historyDate_Map;
        if (dateBlockIndex == 0) {
            historyItemList.clear();
            if (blockData.length < 20) {
                dateBlockIndex = 0;
                historyDate_Map = VidonnHelper.DeCodeX6.decode_HistoryRecodeDate(blockData, blockData.length);
                for (int i = 0; i < historyDate_Map.length; i++) {
                    int[] historyBlock = historyDate_Map[i];
//                    Log.e(TAG, "dateBlockIndex : " + dateBlockIndex + " / " +
//                            aHistoryDate_Map[0] + "Block  Date=" + aHistoryDate_Map[1] + "/"
//                            + aHistoryDate_Map[2] + "/" + aHistoryDate_Map[3]);

                    historyItemList.add(new ActivityHistoryItem());
                    historyItemList.get(i).historyBlockNumber = historyBlock[0];
                    historyItemList.get(i).historyBlockCalendar = historyBlock[1] + "/"
                            + historyBlock[2] + "/" + historyBlock[3];
                }
            } else {
                dateBlockIndex = 1;
                System.arraycopy(blockData, 0, historyDate_Data, 0, blockData.length);
            }
        } else if (dateBlockIndex == 1) {
            dateBlockIndex = 0;

            historyItemList.clear();

            int dataLength = 20 + blockData.length;

            System.arraycopy(blockData, 0, historyDate_Data, 20, dataLength - 20);

            historyDate_Map = VidonnHelper.DeCodeX6.decode_HistoryRecodeDate(historyDate_Data, dataLength);

            for (int i = 0; i < historyDate_Map.length; i++) {
                int[] historyBlock = historyDate_Map[i];
//                    Log.e(TAG, "dateBlockIndex : " + dateBlockIndex + " / " +
//                            aHistoryDate_Map[0] + "Block  Date=" + aHistoryDate_Map[1] + "/"
//                            + aHistoryDate_Map[2] + "/" + aHistoryDate_Map[3]);
                historyItemList.add(new ActivityHistoryItem());
                historyItemList.get(i).historyBlockNumber = historyBlock[0];
                historyItemList.get(i).historyBlockCalendar = historyBlock[1] + "/"
                        + historyBlock[2] + "/" + historyBlock[3];
            }

            gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readHistoryRecodeDetail((byte) historyDetail_Data_Block_Week_ID, (byte) historyDetail_Data_Block_Hour_ID));
            gattReadType = GattReadType.VIDONN_HISTORY_DETAIL;
        }
    }


    private boolean updateBlockIndex() {
        historyDetail_Data_Block_Hour_ID++;
        if (historyDetail_Data_Block_Hour_ID == 24) {
            historyDetail_Data_Block_Hour_ID = 0;


            historyDetail_Data_Block_Week_ID++;
        }
        if ((historyDetail_Data_Block_Week_ID > 3)) {
            Log.e(TAG, "Over");

//            for (int step : stepList) {
//                Log.e(TAG, "Step : " + step);
//            }
            for (ActivityHistoryItem activityHistoryItem : historyItemList) {
                Log.e(TAG,
                        "activityHistoryItem.historyBlockNumber : " + activityHistoryItem.historyBlockNumber +
                                "\nactivityHistoryItem.historyBlockCalendar : " + activityHistoryItem.historyBlockCalendar +
                                "\nactivityHistoryItem.totalStep : " + activityHistoryItem.totalStep
                );
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
        System.arraycopy(detailData, 0, historyDetail_Data, indexStart, dataLength);
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
                    Log.e(TAG, (i * 2 - 1) + "~" + (i * 2) + "min data=" + steps[i][1] + "  type=" + steps[i][0]);
                    Log.e(TAG, "=======================================================");
                    Log.e(TAG, "before totalStep : " + historyItemList.get(historyDetail_Data_Block_Week_ID - 1).totalStep);
                    historyItemList.get(historyDetail_Data_Block_Week_ID - 1).totalStep += steps[i][1];
                    Log.e(TAG, "add totalStep : " + steps[i][1]);
                    Log.e(TAG, "after totalStep : " + historyItemList.get(historyDetail_Data_Block_Week_ID - 1).totalStep);

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
                                        gattReadType = GattReadType.VIDONN_HISTORY_BLOCK;
                                        gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readHistoryRecodeDate());
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
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, PrimeHelper.READ_TIME);
                    break;
                case DEVICE_VIDONN:
                    gattManager.writeValue(bluetoothGattCharacteristicForWrite, VidonnHelper.OperationX6.readDate_Time());
                    break;
            }
            gattReadType = GattReadType.READ_TIME;
        }


        @DebugLog
        public void onDeviceNotify(final BluetoothGattCharacteristic characteristic) {
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

                control.fail();


            }
        }


        public void onWriteFail() {

            control.fail();


        }


        public void onRSSIUpdate(final int rssiValue) {
            activity.runOnUiThread(() -> control.updateRssiValue(rssiValue));

        }
    };


    public void removeDeviceUserData() {
        primeDao.clearSharedPreferenceData();
    }


    public void removeHistoryData() {
        primeDao.removePrimeData();
    }


    public void saveUserData(DeviceVo deviceVo) {
        primeDao.saveDeviceData(deviceVo);
    }


    public boolean isHistoryDataAvailable() {
        return primeDao.isPrimeDataAvailable();
    }


    public void applicationExit() {
        state = NursingPresenter.connectionStateType.DISCONNECTED;
        activity.finish();
    }
}

interface BaseNursingPresenter {
    void initDB();

    void initializeViews();

    void overWriteHistory(boolean isOverWriteAllData);

    void registerBluetooth();

    void disconnectDevice();

    void backPressed();

    void userSettingPressed();

    void goalSettingPressed();

    void applicationExit();
}