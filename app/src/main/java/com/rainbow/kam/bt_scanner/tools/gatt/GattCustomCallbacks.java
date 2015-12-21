package com.rainbow.kam.bt_scanner.tools.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by kam6512 on 2015-10-29.
 */
public interface GattCustomCallbacks {

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onServicesFound(final List<BluetoothGattService> services);

    void onServicesNotFound();

    void onReadSuccess(
            final BluetoothGattCharacteristic ch);

    void onReadFail();


    void onDataNotify(
            final BluetoothGattCharacteristic ch);

    void onWriteSuccess();

    void onWriteFail();

    void onRSSIUpdate(final int rssi);
    void onRSSIMiss();

/*
/* define Null Adapter class for that interface * /
class Null implements GattCustomCallbacks {
@Override
public void onDeviceConnected() {
}

@Override
public void onDeviceDisconnected() {
}

@Override
public void onServicesFound(List<BluetoothGattService> services) {
}

@Override
public void onServicesNotFound() {

}

@Override
public void onReadSuccess(BluetoothGattCharacteristic ch) {
}

@Override
public void onReadFail() {
}

@Override
public void onDataNotify(BluetoothGattCharacteristic ch) {
}

@Override
public void onWriteSuccess() {
}

@Override
public void onWriteFail() {
}

@Override
public void onRSSIUpdate(
int rssi) {
}

@Override
public void onRSSIMiss() {

}
}
*/
}
