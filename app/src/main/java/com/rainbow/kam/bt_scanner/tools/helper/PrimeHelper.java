package com.rainbow.kam.bt_scanner.tools.helper;

import android.os.Bundle;

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

    public static final String KEY_INDEX = "INDEX";

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;


    public static final byte[] getBytesForReadTime = parseHexStringToBytes("0x8900");


    public static byte[] getBytesForReset = parseHexStringToBytes("0x8700");


    public static byte[] getBytesForClear = parseHexStringToBytes("0x8800");


    public static final byte[] getBytesForReadExerciseData = parseHexStringToBytes("0xC60108");


    public static byte[] getBytesForCall = parseHexStringToBytes("0xf30101");


    public static byte[] getBytesForDateTime() {

        Calendar cal = new GregorianCalendar();

        StringBuilder time = new StringBuilder();
        time.append("0xC207");
        time.append(String.format("%02x", cal.get(Calendar.YEAR) - 2000));
        time.append(String.format("%02x", cal.get(Calendar.MONTH) + 1));
        time.append(String.format("%02x", cal.get(Calendar.DATE)));
        time.append(String.format("%02x", cal.get(Calendar.HOUR_OF_DAY)));
        time.append(String.format("%02x", cal.get(Calendar.MINUTE)));
        time.append(String.format("%02x", cal.get(Calendar.SECOND)));

        int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week == 0) {
            week = 7;
        }
        time.append("0").append(String.format("%2s", week));

        return parseHexStringToBytes(time.toString());
    }


    public static byte[] getBytesForUserData(int gender, int height, int weight, int stride, int runningStride) {
        String userData = "0x832f000000000000000000000000000000000000000000000000000000000000000000" +
                String.format("%02d", gender) +
                String.format("%02d", height) +
                String.format("%02d", weight) +
                String.format("%02d", stride) +
                String.format("%02d", runningStride) +
                "0b1e071e0200000000";

        return parseHexStringToBytes(userData);
    }


    public static byte[] getBytes(String hex) {
        return parseHexStringToBytes(hex);
    }


    @DebugLog
    private static byte[] parseHexStringToBytes(String hex) {

        hex = hex.toLowerCase(Locale.getDefault()).substring(2).replaceAll("[^[0-9][a-f]]", "");

        byte[] tempBytes = new byte[(hex.length() / 2) + 1];

        int checksum = 0;

        for (int i = 0; i < tempBytes.length - 1; ++i) {
            tempBytes[i] = Long.decode("0x" + hex.substring(i * 2, i * 2 + 2)).byteValue();

            if (i > 1 && i <= tempBytes.length - 2) {
                if (tempBytes[i] < 0x00) {
                    checksum = checksum ^ tempBytes[i] + 256;
                } else {
                    checksum ^= tempBytes[i];
                }
            }
        }

        tempBytes[tempBytes.length - 1] = Long.decode("0x" + String.format("%02x", checksum)).byteValue();
        return tempBytes;
    }


    @DebugLog
    public static Calendar readTime(byte[] characteristicValue) {

        Calendar calendar = new GregorianCalendar();

        for (int i = 2; i < characteristicValue.length - 1; i++) {  // 0 : Positive - Negative / 1 : Length / last index : checksum
            switch (i) {
                case 2:
                    calendar.set(Calendar.YEAR, Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    break;
                case 3:
                    calendar.set(Calendar.MONTH, Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    break;
                case 4:
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    break;
                case 5:
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    break;
                case 6:
                    calendar.set(Calendar.MINUTE, Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    break;
                case 7:
                    calendar.set(Calendar.SECOND, Integer.valueOf(Integer.toHexString(characteristicValue[i]), 16));
                    break;
            }
        }
        return calendar;
    }


    @DebugLog
    public static Bundle readValue(byte[] characteristicValue, String userAge, String userHeight) {
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
