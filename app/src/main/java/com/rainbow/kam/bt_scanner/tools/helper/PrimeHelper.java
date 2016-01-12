package com.rainbow.kam.bt_scanner.tools.helper;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeHelper {

    public static String KEY = "USER";
    public static String KEY_NAME = "USER_NAME";
    public static String KEY_AGE = "USER_AGE";
    public static String KEY_HEIGHT = "USER_HEIGHT";
    public static String KEY_WEIGHT = "USER_WEIGHT";
    public static String KEY_STEP = "USER_STEP";
    public static String KEY_GENDER = "USER_GENDER";
    public static String KEY_DEVICE_NAME = "DEVICE_NAME";
    public static String KEY_DEVICE_ADDRESS = "DEVICE_ADDRESS";


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

}
