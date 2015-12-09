package com.rainbow.kam.bt_scanner.adapter.dev.main;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class MainDeviceItem { //카드 뷰 틀
    private String deviceName;
    private String deviceAddress;
    private int deviceBondState;
    private int deviceType;
    private int deviceRssi;

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
