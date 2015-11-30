package com.rainbow.kam.bt_scanner.adapter.detail;

import android.bluetooth.BluetoothGattService;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceItem {
    private String title;
    private String uuid;
    private String type;
    private BluetoothGattService bluetoothGattService;

    public ServiceItem(String title, String uuid, String type, BluetoothGattService bluetoothGattService) {
        this.title = title;
        this.uuid = uuid;
        this.type = type;
        this.bluetoothGattService = bluetoothGattService;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getType() {
        return this.type;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }
}
