package com.rainbow.kam.bt_scanner.NotInUse.Tools;

/**
 * Created by sion on 2015-11-10.
 */
public class DeviceListAttribute {
    private String deviceName;
    private boolean availableStep;
    private boolean availableCalorie;
    private boolean availableDistance;
    private boolean availablSleep;
    private boolean availablHeartRate;

    public DeviceListAttribute(final String deviceName, final boolean availableStep, final boolean availableCalorie, final boolean availableDistance, final boolean availablSleep, final boolean availablHeartRate) {
        this.deviceName = deviceName;
        this.availableStep = availableStep;
        this.availableCalorie = availableCalorie;
        this.availableDistance = availableDistance;
        this.availablSleep = availablSleep;
        this.availablHeartRate = availablHeartRate;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean getAvailableStep() {
        return availableStep;
    }
    public boolean getAvailableCalorie() {
        return availableCalorie;
    }
    public boolean getAvailableDistance() {
        return availableDistance;
    }
    public boolean getAvailableSleep() {
        return availablSleep;
    }
    public boolean getAvailableHeartRate() {
        return availablHeartRate;
    }
}
