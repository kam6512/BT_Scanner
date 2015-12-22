package com.rainbow.kam.bt_scanner.adapter.dev;

/**
 * Created by kam6512 on 2015-10-14.
 */
public class SelectDeviceItem { //카드 뷰 틀
    private final String extraName;
    private final String extraAddress;
    private final int extraBondState;
    private final int extraType;
    private final int extraRssi;

    public SelectDeviceItem(String extraName,
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

}
