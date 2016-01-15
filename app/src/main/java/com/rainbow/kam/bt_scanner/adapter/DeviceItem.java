package com.rainbow.kam.bt_scanner.adapter;

import android.bluetooth.BluetoothDevice;

import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceItem { //카드 뷰 틀
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
        switch (extraBondState) {
            case 10:
                return BluetoothHelper.BOND_NONE;

            case 11:
                return BluetoothHelper.BOND_BONDING;

            case 12:
                return BluetoothHelper.BOND_BONDED;

            default:
                return BluetoothHelper.BOND_NONE;
        }
    }


    public String getExtraType() {
        switch (extraType) {
            case 0:
                return BluetoothHelper.DEVICE_TYPE_UNKNOWN;

            case 1:
                return BluetoothHelper.DEVICE_TYPE_CLASSIC;

            case 2:
                return BluetoothHelper.DEVICE_TYPE_LE;

            case 3:
                return BluetoothHelper.DEVICE_TYPE_DUAL;

            default:
                return BluetoothHelper.DEVICE_TYPE_UNKNOWN;
        }
    }


    public int getExtraRssi() {
        return this.extraRssi;
    }
}
