package com.rainbow.kam.bt_scanner.adapter;

import android.bluetooth.BluetoothDevice;

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


    public int getExtraBondState() {
        return this.extraBondState;
    }


    public int getExtraType() {
        return this.extraType;
    }


    public int getExtraRssi() {
        return this.extraRssi;
    }


//    public void setExtraRssi(int rssi) {
//        this.extraRssi = rssi;
//    }

}
