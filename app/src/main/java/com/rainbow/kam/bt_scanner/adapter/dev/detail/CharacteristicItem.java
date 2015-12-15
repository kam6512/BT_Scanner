package com.rainbow.kam.bt_scanner.adapter.dev.detail;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicItem {
    private final String title;
    private final String uuid;
    private final String value;
    private final BluetoothGattCharacteristic bluetoothGattCharacteristic;

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
