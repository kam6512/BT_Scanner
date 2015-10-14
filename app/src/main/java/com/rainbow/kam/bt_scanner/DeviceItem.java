package com.rainbow.kam.bt_scanner;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceItem {
    private String extraName;
    private String extraAddress;
    private int extraBondState;
    private int extraType;
    private int extraRssi;

    public DeviceItem(String extraName,
                      String extraAddress,
                      int extraBondState,
                      int extraType,
                      int extraRssi) {
        this.extraName = extraName;
        this.extraAddress = extraAddress;
        this.extraBondState = extraBondState;
        this.extraType = extraType;
        this.extraRssi = extraRssi;
    }

    public String getExtraName() {
        return this.extraName;
    }

    public String getExtraextraAddress() {
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

}
