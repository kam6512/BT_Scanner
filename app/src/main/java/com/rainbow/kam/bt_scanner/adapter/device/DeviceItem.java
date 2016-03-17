package com.rainbow.kam.bt_scanner.adapter.device;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceItem{ //카드 뷰 틀
    private final String extraName;
    private final String extraAddress;
    private final int extraBondState;
    private final int extraType;
    private final int extraRssi;

    private SparseArray<String> bondList = GattAttributes.BOND_LIST;
    private SparseArray<String> typeList = GattAttributes.TYPE_LIST;

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
        return bondList.get(extraBondState,  bondList.get(bondList.keyAt(0)));
    }


    public String getExtraType() {
        return typeList.get(extraType,  typeList.get(typeList.keyAt(0)));
    }


    public int getExtraRssi() {
        return this.extraRssi;
    }
}
