package com.rainbow.kam.bt_scanner.ui.adapter.device;
import android.bluetooth.BluetoothDevice;

import com.rainbow.kam.ble_gatt_manager.GattAttributes;
/**
 * Created by kam6512 on 2015-10-14.
 */
public class DeviceItem{ //카드 뷰 틀
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
        return GattAttributes.getBond(extraBondState);
    }


    public String getExtraType() {
        return GattAttributes.getType(extraType);
    }


    public int getExtraRssi() {
        return this.extraRssi;
    }
}
