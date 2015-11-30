package com.rainbow.kam.bt_scanner.adapter.detail;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicItem {
    private String title;
    private String uuid;
    private String value;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    public CharacteristicItem(String title, String uuid, String value, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.title = title;
        this.uuid = uuid;
        this.value = value;
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getValue() {
        return this.value;
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return this.bluetoothGattCharacteristic;
    }
}
