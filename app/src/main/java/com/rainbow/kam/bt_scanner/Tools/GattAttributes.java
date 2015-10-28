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

package com.rainbow.kam.bt_scanner.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GattAttributes { //샘플을 참고함

    //열 속성 해쉬맵
    private static HashMap<String, String> attributes = new HashMap();

    //UUID
    public static String GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";


    public static String DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String PPCP = "00002a04-0000-1000-8000-00805f9b34fb";

    public static String Manufacturer = "00002a29-0000-1000-8000-00805f9b34fb";
    public static String Model = "00002a24-0000-1000-8000-00805f9b34fb";
    public static String Serial = "00002a25-0000-1000-8000-00805f9b34fb";
    public static String Hardware = "00002a27-0000-1000-8000-00805f9b34fb";
    public static String Firmware = "00002a26-0000-1000-8000-00805f9b34fb";
    public static String Software = "00002a28-0000-1000-8000-00805f9b34fb";

    public static String Battery = "00002a19-0000-1000-8000-00805f9b34fb";


        public static String UUID = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    //속성 값 GET
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }


    public static String getService(String postKey) {
        HashMap<String, String> serviceList = new HashMap<String, String>();
        serviceList.put("00001800", "Generic Access");
        serviceList.put("00001800", "Generic Access");
        serviceList.put("0000180a", "Device Information");
        serviceList.put("0000180f", "Battery Service");
        serviceList.put("0000feba", "Unknown Service");
        serviceList.put("0000fff0", "Unknown Service");

        Iterator<String> stringIterator = serviceList.keySet().iterator();
        while (stringIterator.hasNext()) {
            String getKey = stringIterator.next();
            if (getKey.equals(postKey)) {
                String value = serviceList.get(getKey);
                return value;
            }
        }
        return "N/A";
    }

    public static String getCharacteristic(String postKey) {
        HashMap<String, String> characteristicList = new HashMap<String, String>();
//        characteristicList.put("00002a00", "Device Name");
//        characteristicList.put("00002a01", "Appearance");
//        characteristicList.put("00002a04", "Peripheral Preferred Connection Parameters");
//
//        characteristicList.put("00002a29", "Manufacturer Name String");
//        characteristicList.put("00002a24", "Model Number String");
//        characteristicList.put("00002a25", "Serial Number String");
//        characteristicList.put("00002a27", "Hardware Revision String");
//        characteristicList.put("00002a26", "Firmware Revision String");
//        characteristicList.put("00002a28", "Software Revision String");
//
//        characteristicList.put("00002a19", "Battery Level");
//
//        characteristicList.put("0000fa10", "Unknown Characteristic");
//        characteristicList.put("0000fa11", "Unknown Characteristic");
//
//        characteristicList.put("0000fff2", "Unknown Characteristic");
//        characteristicList.put("0000fff1", "Unknown Characteristic");
//        characteristicList.put("0000fff4", "Unknown Characteristic");
//        characteristicList.put("0000fff3", "Unknown Characteristic");
//        characteristicList.put("0000fff5", "Unknown Characteristic");

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


        Iterator<String> stringIterator = characteristicList.keySet().iterator();
        while (stringIterator.hasNext()) {
            String getKey = stringIterator.next();
            if (getKey.equals(postKey)) {
                String value = characteristicList.get(getKey);
                return value;
            }
        }
        return "N/A";
    }
}
