/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rainbow.kam.bt_scanner.tools.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GattAttributes {

    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805F9B34FB";

    private final static Map<String, String> SERVICES = new HashMap<>();
    private final static Map<String, String> CHARACTERISTICS = new HashMap<>();
    private final static SparseArray<String> VALUE_FORMATS = new SparseArray<>();

    private final static String UNKNOWN = "Unknown";

    private final static List<Integer> FORMAT_LIST;


    public static String resolveServiceName(final String uuid) {
        String res = SERVICES.get(uuid);
        if (res != null) {
            return res;
        }
        return UNKNOWN;
    }


    public static String resolveCharacteristicName(final String uuid) {
        String res = CHARACTERISTICS.get(uuid);
        if (res != null) {
            return res;
        }
        return UNKNOWN;
    }


    public static String resolveValueTypeDescription(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Integer format = getValueFormat(bluetoothGattCharacteristic);
        return VALUE_FORMATS.get(format, UNKNOWN);
    }


    private static int getValueFormat(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        int properties = bluetoothGattCharacteristic.getProperties();
        int format;
        int formatListLength = FORMAT_LIST.size();
        for (int i = 0; i < formatListLength; i++) {
            format = FORMAT_LIST.get(i);
            if ((format & properties) != 0) return format;
        }
        return 0;
    }


    static {
        SERVICES.put("00001811", "Alert Notification Service");
        SERVICES.put("0000180f", "Battery Service");
        SERVICES.put("00001810", "Blood Pressure");
        SERVICES.put("00001805", "Current Time Service");
        SERVICES.put("00001818", "Cycling Power");
        SERVICES.put("00001816", "Cycling Speed and Cadence");
        SERVICES.put("0000180a", "Device Information");
        SERVICES.put("00001800", "Generic Access");
        SERVICES.put("00001801", "Generic Attribute");
        SERVICES.put("00001808", "Glucose");
        SERVICES.put("00001809", "Health Thermometer");
        SERVICES.put("0000180d", "Heart Rate");
        SERVICES.put("00001812", "Human Interface Device");
        SERVICES.put("00001802", "Immediate Alert");
        SERVICES.put("00001803", "Link Loss");
        SERVICES.put("00001819", "Location and Navigation");
        SERVICES.put("00001807", "Next DST Change Service");
        SERVICES.put("0000180e", "Phone Alert Status Service");
        SERVICES.put("00001806", "Reference Time Update Service");
        SERVICES.put("00001814", "Running Speed and Cadence");
        SERVICES.put("00001813", "Scan Parameters");
        SERVICES.put("00001804", "Tx Power");

        CHARACTERISTICS.put("00002a43", "Alert Category ID");
        CHARACTERISTICS.put("00002a42", "Alert Category ID Bit Mask");
        CHARACTERISTICS.put("00002a06", "Alert Level");
        CHARACTERISTICS.put("00002a44", "Alert Notification Control Point");
        CHARACTERISTICS.put("00002a3f", "Alert Status");
        CHARACTERISTICS.put("00002a01", "Appearance");
        CHARACTERISTICS.put("00002a19", "Battery Level");
        CHARACTERISTICS.put("00002a49", "Blood Pressure Feature");
        CHARACTERISTICS.put("00002a35", "Blood Pressure Measurement");
        CHARACTERISTICS.put("00002a38", "Body Sensor Location");
        CHARACTERISTICS.put("00002a22", "Boot Keyboard Input Report");
        CHARACTERISTICS.put("00002a32", "Boot Keyboard Output Report");
        CHARACTERISTICS.put("00002a33", "Boot Mouse Input Report");
        CHARACTERISTICS.put("00002a5c", "CSC Feature");
        CHARACTERISTICS.put("00002a5b", "CSC Measurement");
        CHARACTERISTICS.put("00002a2b", "Current Time");
        CHARACTERISTICS.put("00002a66", "Cycling Power Control Point");
        CHARACTERISTICS.put("00002a65", "Cycling Power Feature");
        CHARACTERISTICS.put("00002a63", "Cycling Power Measurement");
        CHARACTERISTICS.put("00002a64", "Cycling Power Vector");
        CHARACTERISTICS.put("00002a08", "Date Time");
        CHARACTERISTICS.put("00002a0a", "Day Date Time");
        CHARACTERISTICS.put("00002a09", "Day of Week");
        CHARACTERISTICS.put("00002a00", "Device Name");
        CHARACTERISTICS.put("00002a0d", "DST Offset");
        CHARACTERISTICS.put("00002a0c", "Exact Time 256");
        CHARACTERISTICS.put("00002a26", "Firmware Revision String");
        CHARACTERISTICS.put("00002a51", "Glucose Feature");
        CHARACTERISTICS.put("00002a18", "Glucose Measurement");
        CHARACTERISTICS.put("00002a34", "Glucose Measurement Context");
        CHARACTERISTICS.put("00002a27", "Hardware Revision String");
        CHARACTERISTICS.put("00002a39", "Heart Rate Control Point");
        CHARACTERISTICS.put("00002a37", "Heart Rate Measurement");
        CHARACTERISTICS.put("00002a4c", "HID Control Point");
        CHARACTERISTICS.put("00002a4a", "HID Information");
        CHARACTERISTICS.put("00002a2a", "IEEE 11073-20601 Regulatory Certification Data List");
        CHARACTERISTICS.put("00002a36", "Intermediate Cuff Pressure");
        CHARACTERISTICS.put("00002a1e", "Intermediate Temperature");
        CHARACTERISTICS.put("00002a6b", "LN Control Point");
        CHARACTERISTICS.put("00002a6a", "LN Feature");
        CHARACTERISTICS.put("00002a0f", "Local Time Information");
        CHARACTERISTICS.put("00002a67", "Location and Speed");
        CHARACTERISTICS.put("00002a29", "Manufacturer Name String");
        CHARACTERISTICS.put("00002a21", "Measurement Interval");
        CHARACTERISTICS.put("00002a24", "Model Number String");
        CHARACTERISTICS.put("00002a68", "Navigation");
        CHARACTERISTICS.put("00002a46", "New Alert");
        CHARACTERISTICS.put("00002a04", "Peripheral Preferred Connection Parameters");
        CHARACTERISTICS.put("00002a02", "Peripheral Privacy Flag");
        CHARACTERISTICS.put("00002a50", "PnP ID");
        CHARACTERISTICS.put("00002a69", "Position Quality");
        CHARACTERISTICS.put("00002a4e", "Protocol Mode");
        CHARACTERISTICS.put("00002a03", "Reconnection Address");
        CHARACTERISTICS.put("00002a52", "Record Access Control Point");
        CHARACTERISTICS.put("00002a14", "Reference Time Information");
        CHARACTERISTICS.put("00002a4d", "Report");
        CHARACTERISTICS.put("00002a4b", "Report Map");
        CHARACTERISTICS.put("00002a40", "Ringer Control Point");
        CHARACTERISTICS.put("00002a41", "Ringer Setting");
        CHARACTERISTICS.put("00002a54", "RSC Feature");
        CHARACTERISTICS.put("00002a53", "RSC Measurement");
        CHARACTERISTICS.put("00002a55", "SC Control Point");
        CHARACTERISTICS.put("00002a4f", "Scan Interval Window");
        CHARACTERISTICS.put("00002a31", "Scan Refresh");
        CHARACTERISTICS.put("00002a5d", "Sensor Location");
        CHARACTERISTICS.put("00002a25", "Serial Number String");
        CHARACTERISTICS.put("00002a05", "Service Changed");
        CHARACTERISTICS.put("00002a28", "Software Revision String");
        CHARACTERISTICS.put("00002a47", "Supported New Alert Category");
        CHARACTERISTICS.put("00002a48", "Supported Unread Alert Category");
        CHARACTERISTICS.put("00002a23", "System ID");
        CHARACTERISTICS.put("00002a1c", "Temperature Measurement");
        CHARACTERISTICS.put("00002a1d", "Temperature Type");
        CHARACTERISTICS.put("00002a12", "Time Accuracy");
        CHARACTERISTICS.put("00002a13", "Time Source");
        CHARACTERISTICS.put("00002a16", "Time Update Control Point");
        CHARACTERISTICS.put("00002a17", "Time Update State");
        CHARACTERISTICS.put("00002a11", "Time with DST");
        CHARACTERISTICS.put("00002a0e", "Time Zone");
        CHARACTERISTICS.put("00002a07", "Tx Power Level");
        CHARACTERISTICS.put("00002a45", "Unread Alert Status");

        VALUE_FORMATS.put(52, "32bit float");
        VALUE_FORMATS.put(50, "16bit float");
        VALUE_FORMATS.put(34, "16bit signed int");
        VALUE_FORMATS.put(36, "32bit signed int");
        VALUE_FORMATS.put(33, "8bit signed int");
        VALUE_FORMATS.put(18, "16bit unsigned int");
        VALUE_FORMATS.put(20, "32bit unsigned int");
        VALUE_FORMATS.put(17, "8bit unsigned int");

        FORMAT_LIST = Arrays.asList(
                BluetoothGattCharacteristic.FORMAT_FLOAT,
                BluetoothGattCharacteristic.FORMAT_SFLOAT,
                BluetoothGattCharacteristic.FORMAT_SINT16,
                BluetoothGattCharacteristic.FORMAT_SINT32,
                BluetoothGattCharacteristic.FORMAT_SINT8,
                BluetoothGattCharacteristic.FORMAT_UINT16,
                BluetoothGattCharacteristic.FORMAT_UINT32,
                BluetoothGattCharacteristic.FORMAT_UINT8
        );
    }
}
