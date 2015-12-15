package com.rainbow.kam.bt_scanner.adapter.dev.main;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class MainDeviceItem { //카드 뷰 틀
    private final String deviceName;
    private final String deviceAddress;
    private final int deviceBondState;
    private final int deviceType;
    private final int deviceRssi;

    public MainDeviceItem(String deviceName,
                          String deviceAddress,
                          int deviceBondState,
                          int deviceType,
                          int deviceRssi) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.deviceBondState = deviceBondState;
        this.deviceType = deviceType;
        this.deviceRssi = deviceRssi;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getDeviceAddress() {
        return this.deviceAddress;
    }

    public int getDeviceBondState() {
        return this.deviceBondState;
    }

    public int getDeviceType() {
        return this.deviceType;
    }

    public int getDeviceRssi() {
        return this.deviceRssi;
    }

}
