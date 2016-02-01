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
    public static final String KEY_GENDER = "USER_GENDER";
    public static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static final String KEY_STEP = "STEP";
    public static final String KEY_CALORIE = "CALORIE";
    public static final String KEY_DISTANCE = "DISTANCE";

    public static final String KEY_GOAL_STEP = "STEP_GOAL";
    public static final String KEY_GOAL_CALORIE = "CALORIE_GOAL";
    public static final String KEY_GOAL_DISTANCE = "DISTANCE_GOAL";

    public static final String KEY_INDEX = "INDEX";

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;


    public static final byte[] getBytesForReadTime = parseHexStringToBytes("8900");


    public static byte[] getBytesForReset = parseHexStringToBytes("8700");


    public static byte[] getBytesForClear = parseHexStringToBytes("8800");


    public static final byte[] getBytesForReadExerciseData = parseHexStringToBytes("C60108");


    public static byte[] getBytesForCall = parseHexStringToBytes("f30101");


    public static byte[] getBytesForDateTime() {
        Calendar cal = new GregorianCalendar();

        StringBuilder time = new StringBuilder();
        time.append("C207");
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
        time.append(String.format("%2d", week));

        return parseHexStringToBytes(time.toString());
    }


    //Test
    public static byte[] getBytesForUserData(int gender, int height, int weight, int stride, int runningStride) {
        String userData = "832f000000000000000000000000000000000000000000000000000000000000000000" +
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

        hex = hex.toLowerCase(Locale.getDefault()).replaceAll("[^[0-9][a-f]]", "");

        byte[] bytes = new byte[(hex.length() / 2) + 1];

        int checksum = 0;

        for (int i = 0; i < bytes.length - 1; ++i) {
            bytes[i] = Long.decode("0x" + hex.substring(i * 2, i * 2 + 2)).byteValue();

            if (i > 1 && i <= bytes.length - 2) {
                if (bytes[i] < 0x00) {
                    checksum = checksum ^ bytes[i] + 256;
                } else {
                    checksum ^= bytes[i];
                }
            }
        }
        bytes[bytes.length - 1] = Long.decode("0x" + String.format("%02x", checksum)).byteValue();

        return bytes;
    }


    @DebugLog
    public static Calendar readTime(byte[] characteristicValue) {

        Calendar calendar = new GregorianCalendar();

        for (int i = 2; i < characteristicValue.length - 1; i++) {  // 0 : Positive - Negative / 1 : Length / last index : checksum
            switch (i) {
                case 2:
                    calendar.set(Calendar.YEAR, characteristicValue[i]);
                    break;
                case 3:
                    calendar.set(Calendar.MONTH, characteristicValue[i] - 1);
                    break;
                case 4:
                    calendar.set(Calendar.DAY_OF_MONTH, characteristicValue[i]);
                    break;
                case 5:
                    calendar.set(Calendar.HOUR_OF_DAY, characteristicValue[i]);
                    break;
                case 6:
                    calendar.set(Calendar.MINUTE, characteristicValue[i]);
                    break;
                case 7:
                    calendar.set(Calendar.SECOND, characteristicValue[i]);
                    break;
            }
        }
        return calendar;
    }


    @DebugLog
    public static Bundle readValue(byte[] characteristicValue, String userAge, String userHeight) throws ArrayIndexOutOfBoundsException {
        String hexStep = String.format("%02x", characteristicValue[2] & 0xff) +
                String.format("%02x", characteristicValue[3] & 0xff) +
                String.format("%02x", characteristicValue[4] & 0xff);
        String hexCal = String.format("%02x", characteristicValue[5] & 0xff) +
                String.format("%02x", characteristicValue[6] & 0xff) +
                String.format("%02x", characteristicValue[7] & 0xff);

        int step, kcal, distance, age;
        double height;

        step = Integer.parseInt(hexStep, 16);
        kcal = Integer.parseInt(hexCal, 16);

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
        bundle.putInt(KEY_CALORIE, kcal);
        bundle.putInt(KEY_DISTANCE, distance);
        return bundle;
    }
}
