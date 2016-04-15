package com.rainbow.kam.bt_scanner.data;

import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rainbow.kam.bt_scanner.operation.Operator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by kam6512 on 2016-03-23.
 */
public class Device implements Comparable<Device> {

    public static final String KEY_NAME = "DEVICE_NAME";
    public static final String KEY_ADD = "DEVICE_ADD";
    public static final String KEY_TYPE = "DEVICE_TYPE";
    public static final String KEY_MENU = "DEVICE_MENU";
    public static final String KEY_UUID = "DEVICE_UUID";

    public static final String UUID_INDEX_CONTROL_SERVICE = "CONTROL_SERVICE";
    public static final String UUID_INDEX_NOTIFY = "NOTIFY";
    public static final String UUID_INDEX_WRITE = "WRITE";

    private final String name;
    private final String address;
    private final DEVICE_TYPE type;

    private static final EnumMap<DEVICE_TYPE, ArrayList<String>> COMMAND_MENU = Maps.newEnumMap(DEVICE_TYPE.class);
    private static final EnumMap<DEVICE_TYPE, HashMap<String, UUID>> UUID_MAP = Maps.newEnumMap(DEVICE_TYPE.class);


    public Device(@NonNull final String name, @NonNull final String address) {
        this.name = name;
        this.address = address;
        this.type = getTypeByName(name);
    }


    public Device(@NonNull final String name, @NonNull final String address, @NonNull final DEVICE_TYPE type) {
        this.name = name;
        this.address = address;
        this.type = type;
    }


    static {
        COMMAND_MENU.put(DEVICE_TYPE.Prime, Lists.newArrayList(
                Operator.READ_TIME,
                Operator.WRITE_TIME,
                Operator.READ_CURRENT_VALUE,
                Operator.READ_BATTERY_VALUE
        ));
        COMMAND_MENU.put(DEVICE_TYPE.X6S, Lists.newArrayList(
                Operator.READ_TIME,
                Operator.WRITE_TIME,
                Operator.READ_CURRENT_VALUE,
                Operator.READ_BATTERY_VALUE,
                Operator.READ_USER_INFO,
                Operator.WRITE_USER_INFO,
                Operator.READ_RECODED_DATE,
                Operator.READ_HISTORY,
                Operator.RESET
        ));

        UUID_MAP.put(DEVICE_TYPE.Prime, new HashMap<String, UUID>() {{
            put(UUID_INDEX_CONTROL_SERVICE, UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB"));
            put(UUID_INDEX_NOTIFY, UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB"));
            put(UUID_INDEX_WRITE, UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB"));
        }});

        UUID_MAP.put(DEVICE_TYPE.X6S, new HashMap<String, UUID>() {{
            put(UUID_INDEX_CONTROL_SERVICE, UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB"));
            put(UUID_INDEX_NOTIFY, UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB"));
            put(UUID_INDEX_WRITE, UUID.fromString("0000FFE4-0000-1000-8000-00805F9B34FB"));
        }});
    }

    public String getName() {
        return name;
    }


    public String getAddress() {
        return address;
    }


    public DEVICE_TYPE getType() {
        return type;
    }


    private DEVICE_TYPE getTypeByName(@NonNull final String deviceName) {
        for (DEVICE_TYPE type : DEVICE_TYPE.values()) {
            if (deviceName.contains(type.name())) {
                return type;
            }
        }
        return null;
    }


    public List<DEVICE_TYPE> getTypes() {
        return Lists.newArrayList(DEVICE_TYPE.values());
    }


    public ArrayList<String> getCommand() {
        return Lists.newArrayList(COMMAND_MENU.get(type));
    }


    public HashMap<String, UUID> getUUID() {
        return new HashMap<>(UUID_MAP.get(type));
    }


    @Override public boolean equals(@NonNull final Object o) {

        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        Device device = (Device) o;
        return address.equals(device.address);
    }


    @Override public String toString() {
        return "Device{" +
                "name='" + this.name + '\'' +
                ", address='" + this.address + '\'' +
                ", type=" + this.type +
                '}';
    }


    @Override public int hashCode() {
        return this.address.hashCode();
    }


    @Override public int compareTo(@NonNull final Device anotherDevice) {
        return this.address.compareTo(anotherDevice.getAddress());
    }
}
