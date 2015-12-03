package com.rainbow.kam.bt_scanner.adapter.nurs.selected;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class SelecteDeviceItem { //카드 뷰 틀
    private String extraName;
    private String extraAddress;
    private int extraBondState;
    private int extraType;
    private int extraRssi;

    public SelecteDeviceItem(String extraName,
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
