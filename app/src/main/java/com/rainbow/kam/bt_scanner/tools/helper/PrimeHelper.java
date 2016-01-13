package com.rainbow.kam.bt_scanner.tools.helper;

import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeHelper {

    public static final String KEY = "USER";
    public static final String KEY_NAME = "USER_NAME";
    public static final String KEY_AGE = "USER_AGE";
    public static final String KEY_HEIGHT = "USER_HEIGHT";
    public static final String KEY_WEIGHT = "USER_WEIGHT";
    public static final String KEY_STEP_STRIDE = "KEY_STEP_STRIDE";
    public static final String KEY_GENDER = "USER_GENDER";
    public static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static final String KEY_STEP = "STEP";
    public static final String KEY_KCAL = "KCAL";
    public static final String KEY_DISTANCE = "DISTANCE";

    private static final String[] weekSet = {"월", "화", "수", "목", "금", "토", "일",};
    private static final String[] timeSet = {"년", "월", "일", "시", "분", "초"};


    public static byte[] READ_DEVICE_TIME() {
        return parseHexStringToBytes("0x890000", false);
    }


    public static byte[] READ_DEVICE_BATTERY() {

        return parseHexStringToBytes("0xC60108", true);
    }


    public static byte[] RESTORE_FACTORY_SETTING() {
        return parseHexStringToBytes("0x870000", false);
    }


    public static byte[] CLEAR_DATA() {
        return parseHexStringToBytes("0x880000", false);
    }


    public static byte[] READ_STEP_DATA() {
        return parseHexStringToBytes("0xC6010808", false);
    }


    public static byte[] CALL_DEVICE() {
        return parseHexStringToBytes("0xf30101", true);
    }


    public static byte[] SET_DEVICE_TIME_NOW() {
        String setDate = "0xC207";
        Calendar cal = new GregorianCalendar();

        String year = String.format("%02x", cal.get(Calendar.YEAR) - 2000);
        String month = String.format("%02x", cal.get(Calendar.MONTH) + 1);
        String date = String.format("%02x", cal.get(Calendar.DATE));
        String hour = String.format("%02x", cal.get(Calendar.HOUR_OF_DAY));
        String min = String.format("%02x", cal.get(Calendar.MINUTE));
        String sec = String.format("%02x", cal.get(Calendar.SECOND));
        int weekTemp = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekTemp == 0) {
            weekTemp = 7;
        }
        String week = "0" + String.format("%2s", weekTemp);


        setDate += year + month + date + hour + min + sec + week;
        Log.e("TIME", year + " / " + month + " / " + date + " / " + hour + " / " + min + " / " + sec + " / " + week);
        return parseHexStringToBytes(setDate, true);
    }


    public static byte[] READ_SPORTS_CURVE_DATA() {
        return parseHexStringToBytes("0xC403031503", true);
    }


    public static byte[] OFF_ANTI_LOST() {
        return parseHexStringToBytes("0x01a102a2", true);
    }


    public static byte[] SET_USER_DATA(int gender, int height, int weight, int stride, int runningStride) {

        String info = "0x832f000000000000000000000000000000000000000000000000000000000000000000";
        info += String.format("%02d", gender);
        info += String.format("%02d", height);
        info += String.format("%02d", weight);
        info += String.format("%02d", stride);
        info += String.format("%02d", runningStride);
        info += "0b1e071e0200000000";

        return parseHexStringToBytes(info, true);
    }


    public static byte[] WRITE_FROM_CONTROL(String hex) {
        return parseHexStringToBytes(hex, false);
    }


    private static byte[] parseHexStringToBytes(String hex, boolean isCheckSumEmpty) {
        String res;
        if (isCheckSumEmpty) {
            String cs = String.format("%02x", getCheckSum(hex));
            res = (hex + cs).toLowerCase(Locale.getDefault()).substring(2).replaceAll("[^[0-9][a-f]]", "");
        } else {
            res = hex.toLowerCase(Locale.getDefault()).substring(2).replaceAll("[^[0-9][a-f]]", "");
        }


        byte[] resBytes = new byte[res.length() / 2];
        String resPart;

        for (int i = 0; i < resBytes.length; ++i) {
            resPart = "0x" + res.substring(i * 2, i * 2 + 2);
            resBytes[i] = Long.decode(resPart).byteValue();
        }

        return resBytes;
    }


    private static int getCheckSum(String hex) {
        hex = hex.toLowerCase(Locale.getDefault()).substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] hexBytes = new byte[hex.length() / 2];

        int checksum = 0;

        for (int i = 0; i < hexBytes.length; ++i) {
            hexBytes[i] = Integer.decode("0x" + hex.substring(i * 2, i * 2 + 2)).byteValue();
            if (i > 1 && i <= hexBytes.length - 1) {
                if (hexBytes[i] < 0x00) {
                    checksum = checksum ^ hexBytes[i] + 256;
                } else {
                    checksum ^= hexBytes[i];
                }
            }
        }
        Log.e("getCheckSum", checksum + " / ");
        return checksum;
    }


    public static StringBuilder readTime(byte[] characteristicValue) {
        StringBuilder result = new StringBuilder();
        for (int i = 2; i < characteristicValue.length - 1; i++) {  // 0 : Positive - Negative / 1 : Length / last index : checksum
            switch (i) {
                default:
                    result.append(Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    result.append(timeSet[i - 2]).append(" ");
                    break;
                case 8:
                    int j = characteristicValue[i];
                    result.append(weekSet[j - 1]);
                    break;
            }
        }
        return result;
    }


    @DebugLog
    public static Bundle readStep(byte[] characteristicValue, String userAge, String userHeight) {
        StringBuilder hexStep = new StringBuilder();
        StringBuilder hexCal = new StringBuilder();

        int step, kcal, distance, age;
        double height;

        hexStep.append(String.format("%02x", characteristicValue[2] & 0xff));
        hexStep.append(String.format("%02x", characteristicValue[3] & 0xff));
        hexStep.append(String.format("%02x", characteristicValue[4] & 0xff));
        step = Integer.parseInt(hexStep.toString(), 16);

        hexCal.append(String.format("%02x", characteristicValue[5] & 0xff));
        hexCal.append(String.format("%02x", characteristicValue[6] & 0xff));
        hexCal.append(String.format("%02x", characteristicValue[7] & 0xff));
        kcal = Integer.parseInt(hexCal.toString(), 16);

        age = Integer.parseInt(userAge);
        height = Integer.parseInt(userHeight);
        if (age <= 15 || age >= 65) {
            distance = (int) ((height * 0.37) * step) / 100;
        } else if (15 < age || age < 45) {
            distance = (int) ((height * 0.45) * step) / 100;
        } else if (45 <= age || age < 65) {
            distance = (int) ((height * 0.40) * step) / 100;
        } else {
            distance = (int) ((height * 0.30) * step) / 100;
        }


        Bundle bundle = new Bundle();
        bundle.putInt(KEY_STEP, step);
        bundle.putInt(KEY_KCAL, kcal);
        bundle.putInt(KEY_DISTANCE, distance);
        return bundle;


    }
}
