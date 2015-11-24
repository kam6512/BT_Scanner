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

package com.rainbow.kam.bt_scanner.Tools.BLE;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BLEGattAttributes {
    //열 속성 해쉬맵
    private static HashMap<String, String> attributes = new HashMap();

    public static class Services {
        public static String GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
        public static String DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
        public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
        public static String HEART_RATE_MEASUREMENT_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    }

    public static class Characteristic {
        public static String DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
        public static String APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
        public static String PPCP = "00002a04-0000-1000-8000-00805f9b34fb";

        public static String MANUFACTURER = "00002a29-0000-1000-8000-00805f9b34fb";
        public static String MODEL = "00002a24-0000-1000-8000-00805f9b34fb";
        public static String SERIAL = "00002a25-0000-1000-8000-00805f9b34fb";
        public static String HARDWARE = "00002a27-0000-1000-8000-00805f9b34fb";
        public static String FIRMWARE = "00002a26-0000-1000-8000-00805f9b34fb";
        public static String SOFTWARE = "00002a28-0000-1000-8000-00805f9b34fb";


        public static String BATTRY = "00002a19-0000-1000-8000-00805f9b34fb";

        public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
        final static public String BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";
    }

    public static class Descriptor {
        public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    }

    //속성 값 GET
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }


    public static String getService(String postKey) {
        HashMap<String, String> serviceList = new HashMap<>();
        serviceList.put("00001800", "Generic Access");
        serviceList.put("00001800", "Generic Access");
        serviceList.put("0000180a", "Device Information");
        serviceList.put("0000180f", "Battery Service");
        serviceList.put("0000feba", "Unknown Service");
        serviceList.put("0000fff0", "Unknown Service");

        for (String getKey : serviceList.keySet()) {
            if (getKey.equals(postKey)) {
                return serviceList.get(getKey);
            }
        }
        return "N/A";
    }

    public static String getCharacteristic(String postKey) {
        HashMap<String, String> characteristicList = new HashMap<>();

        characteristicList.put("00002a00", "기기 이름");
        characteristicList.put("00002a01", "외부 카테고리");
        characteristicList.put("00002a04", "권장하는 기기 연결 수");

        characteristicList.put("00002a29", "제조사");
        characteristicList.put("00002a24", "모델 넘버");
        characteristicList.put("00002a25", "시리얼 넘버");
        characteristicList.put("00002a27", "하드웨어 업데이트 넘버");
        characteristicList.put("00002a26", "펌웨어 업데이트 넘버");
        characteristicList.put("00002a28", "소프트웨어 업데이트 넘버");

        characteristicList.put("00002a19", "배터리 잔량");

        characteristicList.put("0000fa10", "Unknown Characteristic");
        characteristicList.put("0000fa11", "Unknown Characteristic");

        characteristicList.put("0000fff2", "Unknown Characteristic");
        characteristicList.put("0000fff1", "Unknown Characteristic");
        characteristicList.put("0000fff4", "Unknown Characteristic");
        characteristicList.put("0000fff3", "Unknown Characteristic");
        characteristicList.put("0000fff5", "Unknown Characteristic");


        for (String getKey : characteristicList.keySet()) {
            if (getKey.equals(postKey)) {
                return characteristicList.get(getKey);
            }
        }
        return "N/A";
    }

    /**
     * =============================================================================================================
     **/

    private static HashMap<String, String> services = new HashMap<>();
    private static HashMap<String, String> characteristics = new HashMap<>();
    private static SparseArray<String> valueFormats = new SparseArray<>();
    private static SparseArray<String> appearance = new SparseArray<>();

    public static String unknown = "Unknown Service";


    static public String resolveServiceName(final String uuid) {
        String res = services.get(uuid);
        if (res == null) {
            return unknown;
        }
        return res;
    }

    static public String resolveValueTypeDescription(final int format) {
        Integer tmp = format;
        return valueFormats.get(tmp, "Unknown Format");
    }

    static public String resolveCharacteristicName(final String uuid) {
        String result = characteristics.get(uuid);
        if (result == null) result = "Unknown Characteristic";
        return result;
    }

    static public String resolveUuid(final String uuid) {
        String result = services.get(uuid);
        if (result != null) return "Service: " + result;

        result = characteristics.get(uuid);
        if (result != null) return "Characteristic: " + result;

        result = "Unknown UUID";
        return result;
    }

    static public String resolveAppearance(int key) {
        Integer tmp = key;
        return appearance.get(tmp, "Unknown Appearance");
    }

    static public boolean isService(final String uuid) {
        return services.containsKey(uuid);
    }

    static public boolean isCharacteristic(final String uuid) {
        return characteristics.containsKey(uuid);
    }

    static {
        services.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
        services.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        services.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
        services.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
        services.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
        services.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
        services.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        services.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        services.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        services.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
        services.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
        services.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate");
        services.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        services.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
        services.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
        services.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");
        services.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
        services.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
        services.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
        services.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
        services.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
        services.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");

        characteristics.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        characteristics.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        characteristics.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        characteristics.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        characteristics.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        characteristics.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        characteristics.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        characteristics.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        characteristics.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        characteristics.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        characteristics.put("00002a22-0000-1000-8000-00805f9b34fb", "Boot Keyboard Input Report");
        characteristics.put("00002a32-0000-1000-8000-00805f9b34fb", "Boot Keyboard Output Report");
        characteristics.put("00002a33-0000-1000-8000-00805f9b34fb", "Boot Mouse Input Report");
        characteristics.put("00002a5c-0000-1000-8000-00805f9b34fb", "CSC Feature");
        characteristics.put("00002a5b-0000-1000-8000-00805f9b34fb", "CSC Measurement");
        characteristics.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        characteristics.put("00002a66-0000-1000-8000-00805f9b34fb", "Cycling Power Control Point");
        characteristics.put("00002a65-0000-1000-8000-00805f9b34fb", "Cycling Power Feature");
        characteristics.put("00002a63-0000-1000-8000-00805f9b34fb", "Cycling Power Measurement");
        characteristics.put("00002a64-0000-1000-8000-00805f9b34fb", "Cycling Power Vector");
        characteristics.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        characteristics.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        characteristics.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        characteristics.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        characteristics.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        characteristics.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        characteristics.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        characteristics.put("00002a51-0000-1000-8000-00805f9b34fb", "Glucose Feature");
        characteristics.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        characteristics.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        characteristics.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        characteristics.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        characteristics.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        characteristics.put("00002a4c-0000-1000-8000-00805f9b34fb", "HID Control Point");
        characteristics.put("00002a4a-0000-1000-8000-00805f9b34fb", "HID Information");
        characteristics.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
        characteristics.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        characteristics.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        characteristics.put("00002a6b-0000-1000-8000-00805f9b34fb", "LN Control Point");
        characteristics.put("00002a6a-0000-1000-8000-00805f9b34fb", "LN Feature");
        characteristics.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        characteristics.put("00002a67-0000-1000-8000-00805f9b34fb", "Location and Speed");
        characteristics.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        characteristics.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        characteristics.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        characteristics.put("00002a68-0000-1000-8000-00805f9b34fb", "Navigation");
        characteristics.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        characteristics.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
        characteristics.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        characteristics.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
        characteristics.put("00002a69-0000-1000-8000-00805f9b34fb", "Position Quality");
        characteristics.put("00002a4e-0000-1000-8000-00805f9b34fb", "Protocol Mode");
        characteristics.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        characteristics.put("00002a52-0000-1000-8000-00805f9b34fb", "Record Access Control Point");
        characteristics.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        characteristics.put("00002a4d-0000-1000-8000-00805f9b34fb", "Report");
        characteristics.put("00002a4b-0000-1000-8000-00805f9b34fb", "Report Map");
        characteristics.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        characteristics.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        characteristics.put("00002a54-0000-1000-8000-00805f9b34fb", "RSC Feature");
        characteristics.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        characteristics.put("00002a55-0000-1000-8000-00805f9b34fb", "SC Control Point");
        characteristics.put("00002a4f-0000-1000-8000-00805f9b34fb", "Scan Interval Window");
        characteristics.put("00002a31-0000-1000-8000-00805f9b34fb", "Scan Refresh");
        characteristics.put("00002a5d-0000-1000-8000-00805f9b34fb", "Sensor Location");
        characteristics.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        characteristics.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        characteristics.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        characteristics.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        characteristics.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        characteristics.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        characteristics.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        characteristics.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        characteristics.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        characteristics.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        characteristics.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        characteristics.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        characteristics.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        characteristics.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        characteristics.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        characteristics.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");

        valueFormats.put(52, "32bit float");
        valueFormats.put(50, "16bit float");
        valueFormats.put(34, "16bit signed int");
        valueFormats.put(36, "32bit signed int");
        valueFormats.put(33, "8bit signed int");
        valueFormats.put(18, "16bit unsigned int");
        valueFormats.put(20, "32bit unsigned int");
        valueFormats.put(17, "8bit unsigned int");

        // lets add also couple appearance string description
        // https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.gap.appearance.xml
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
