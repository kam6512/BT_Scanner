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

import java.util.HashMap;

public class GattAttributes {

    public static class Descriptor {
        public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805F9B34FB";
    }

    private final static HashMap<String, String> services = new HashMap<>();
    private final static HashMap<String, String> characteristics = new HashMap<>();
    private final static SparseArray<String> valueFormats = new SparseArray<>();
    private final static SparseArray<String> appearance = new SparseArray<>();

    private final static String unknown = "Unknown Service";

    static public String resolveServiceName(final String uuid) {
        String res = services.get(uuid);
        if (res == null) {
            return unknown;
        }
        return res;
    }

    static public String resolveCharacteristicName(final String uuid) {
        String result = characteristics.get(uuid);
        if (result == null) result = "Unknown Characteristic";
        return result;
    }

    static public String resolveValueTypeDescription(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Integer tmp = getValueFormat(bluetoothGattCharacteristic);
        return valueFormats.get(tmp, "Unknown Format");
    }

    static public String resolveAppearance(int key) {
        Integer tmp = key;
        return appearance.get(tmp, "Unknown Appearance");
    }

    private static int getValueFormat(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        int properties = bluetoothGattCharacteristic.getProperties();
        if ((BluetoothGattCharacteristic.FORMAT_FLOAT & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_FLOAT;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SFLOAT & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SFLOAT;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT16 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT16;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT32 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT32;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT8 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT8;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT16 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT16;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT32 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT32;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT8 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT8;
        }
        return 0;
    }

    static {
        services.put("00001811", "Alert Notification Service");
        services.put("0000180f", "Battery Service");
        services.put("00001810", "Blood Pressure");
        services.put("00001805", "Current Time Service");
        services.put("00001818", "Cycling Power");
        services.put("00001816", "Cycling Speed and Cadence");
        services.put("0000180a", "Device Information");
        services.put("00001800", "Generic Access");
        services.put("00001801", "Generic Attribute");
        services.put("00001808", "Glucose");
        services.put("00001809", "Health Thermometer");
        services.put("0000180d", "Heart Rate");
        services.put("00001812", "Human Interface Device");
        services.put("00001802", "Immediate Alert");
        services.put("00001803", "Link Loss");
        services.put("00001819", "Location and Navigation");
        services.put("00001807", "Next DST Change Service");
        services.put("0000180e", "Phone Alert Status Service");
        services.put("00001806", "Reference Time Update Service");
        services.put("00001814", "Running Speed and Cadence");
        services.put("00001813", "Scan Parameters");
        services.put("00001804", "Tx Power");

        characteristics.put("00002a43", "Alert Category ID");
        characteristics.put("00002a42", "Alert Category ID Bit Mask");
        characteristics.put("00002a06", "Alert Level");
        characteristics.put("00002a44", "Alert Notification Control Point");
        characteristics.put("00002a3f", "Alert Status");
        characteristics.put("00002a01", "Appearance");
        characteristics.put("00002a19", "Battery Level");
        characteristics.put("00002a49", "Blood Pressure Feature");
        characteristics.put("00002a35", "Blood Pressure Measurement");
        characteristics.put("00002a38", "Body Sensor Location");
        characteristics.put("00002a22", "Boot Keyboard Input Report");
        characteristics.put("00002a32", "Boot Keyboard Output Report");
        characteristics.put("00002a33", "Boot Mouse Input Report");
        characteristics.put("00002a5c", "CSC Feature");
        characteristics.put("00002a5b", "CSC Measurement");
        characteristics.put("00002a2b", "Current Time");
        characteristics.put("00002a66", "Cycling Power Control Point");
        characteristics.put("00002a65", "Cycling Power Feature");
        characteristics.put("00002a63", "Cycling Power Measurement");
        characteristics.put("00002a64", "Cycling Power Vector");
        characteristics.put("00002a08", "Date Time");
        characteristics.put("00002a0a", "Day Date Time");
        characteristics.put("00002a09", "Day of Week");
        characteristics.put("00002a00", "Device Name");
        characteristics.put("00002a0d", "DST Offset");
        characteristics.put("00002a0c", "Exact Time 256");
        characteristics.put("00002a26", "Firmware Revision String");
        characteristics.put("00002a51", "Glucose Feature");
        characteristics.put("00002a18", "Glucose Measurement");
        characteristics.put("00002a34", "Glucose Measurement Context");
        characteristics.put("00002a27", "Hardware Revision String");
        characteristics.put("00002a39", "Heart Rate Control Point");
        characteristics.put("00002a37", "Heart Rate Measurement");
        characteristics.put("00002a4c", "HID Control Point");
        characteristics.put("00002a4a", "HID Information");
        characteristics.put("00002a2a", "IEEE 11073-20601 Regulatory Certification Data List");
        characteristics.put("00002a36", "Intermediate Cuff Pressure");
        characteristics.put("00002a1e", "Intermediate Temperature");
        characteristics.put("00002a6b", "LN Control Point");
        characteristics.put("00002a6a", "LN Feature");
        characteristics.put("00002a0f", "Local Time Information");
        characteristics.put("00002a67", "Location and Speed");
        characteristics.put("00002a29", "Manufacturer Name String");
        characteristics.put("00002a21", "Measurement Interval");
        characteristics.put("00002a24", "Model Number String");
        characteristics.put("00002a68", "Navigation");
        characteristics.put("00002a46", "New Alert");
        characteristics.put("00002a04", "Peripheral Preferred Connection Parameters");
        characteristics.put("00002a02", "Peripheral Privacy Flag");
        characteristics.put("00002a50", "PnP ID");
        characteristics.put("00002a69", "Position Quality");
        characteristics.put("00002a4e", "Protocol Mode");
        characteristics.put("00002a03", "Reconnection Address");
        characteristics.put("00002a52", "Record Access Control Point");
        characteristics.put("00002a14", "Reference Time Information");
        characteristics.put("00002a4d", "Report");
        characteristics.put("00002a4b", "Report Map");
        characteristics.put("00002a40", "Ringer Control Point");
        characteristics.put("00002a41", "Ringer Setting");
        characteristics.put("00002a54", "RSC Feature");
        characteristics.put("00002a53", "RSC Measurement");
        characteristics.put("00002a55", "SC Control Point");
        characteristics.put("00002a4f", "Scan Interval Window");
        characteristics.put("00002a31", "Scan Refresh");
        characteristics.put("00002a5d", "Sensor Location");
        characteristics.put("00002a25", "Serial Number String");
        characteristics.put("00002a05", "Service Changed");
        characteristics.put("00002a28", "Software Revision String");
        characteristics.put("00002a47", "Supported New Alert Category");
        characteristics.put("00002a48", "Supported Unread Alert Category");
        characteristics.put("00002a23", "System ID");
        characteristics.put("00002a1c", "Temperature Measurement");
        characteristics.put("00002a1d", "Temperature Type");
        characteristics.put("00002a12", "Time Accuracy");
        characteristics.put("00002a13", "Time Source");
        characteristics.put("00002a16", "Time Update Control Point");
        characteristics.put("00002a17", "Time Update State");
        characteristics.put("00002a11", "Time with DST");
        characteristics.put("00002a0e", "Time Zone");
        characteristics.put("00002a07", "Tx Power Level");
        characteristics.put("00002a45", "Unread Alert Status");

        valueFormats.put(52, "32bit float");
        valueFormats.put(50, "16bit float");
        valueFormats.put(34, "16bit signed int");
        valueFormats.put(36, "32bit signed int");
        valueFormats.put(33, "8bit signed int");
        valueFormats.put(18, "16bit unsigned int");
        valueFormats.put(20, "32bit unsigned int");
        valueFormats.put(17, "8bit unsigned int");

        appearance.put(833, "Heart Rate Sensor: Belt");
        appearance.put(832, "Generic Heart Rate Sensor");
        appearance.put(0, "Unknown");
        appearance.put(64, "Generic Phone");
        appearance.put(1157, "Cycling: Speed and Cadence Sensor");
        appearance.put(1152, "General Cycling");
        appearance.put(1153, "Cycling Computer");
        appearance.put(1154, "Cycling: Speed Sensor");
        appearance.put(1155, "Cycling: Cadence Sensor");
        appearance.put(1156, "Cycling: Speed and Cadence Sensor");
        appearance.put(1157, "Cycling: Power Sensor");
    }
}
