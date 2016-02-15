package com.rainbow.kam.bt_scanner.adapter.device;

import android.bluetooth.BluetoothDevice;

import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;

/**
 * Created by kam6512 on 2015-10-14.
 */
class DeviceItem { //카드 뷰 틀
    private final String extraName;
    private final String extraAddress;
    private final int extraBondState;
    private final int extraType;
    private final int extraRssi;


    public DeviceItem(BluetoothDevice bluetoothDevice, int rssi) {
        this.extraName = bluetoothDevice.getName();
        this.extraAddress = bluetoothDevice.getAddress();
        this.extraBondState = bluetoothDevice.getBondState();
        this.extraType = bluetoothDevice.getType();
        this.extraRssi = rssi;
    }


    public String getExtraName() {
        return this.extraName;
    }


    public String getExtraAddress() {
        return this.extraAddress;
    }


    public String getExtraBondState() {
        return BluetoothHelper.BOND_LIST.get(extraBondState, BluetoothHelper.BOND_NONE);
    }


    public String getExtraType() {
        return BluetoothHelper.TYPE_LIST.get(extraType, BluetoothHelper.DEVICE_TYPE_UNKNOWN);
    }


    public int getExtraRssi() {
        return this.extraRssi;
    }
}
