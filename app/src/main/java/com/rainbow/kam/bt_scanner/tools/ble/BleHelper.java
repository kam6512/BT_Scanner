package com.rainbow.kam.bt_scanner.tools.ble;

import android.util.Log;

import com.rainbow.kam.bt_scanner.NotInUse.Tools.BandDeviceList;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by sion on 2015-11-04.
 */
public class BleHelper {
    public static byte[] READ_DEVICE_TIME() {
        return parseHexStringToBytes("0x8900");
    }

    public static byte[] READ_DEVICE_BATTERY() {

        return parseHexStringToBytes("0xC60108");
    }

    public static byte[] RESTORE_FACTORY_SETTING() {
        return parseHexStringToBytes("0x8700");
    }

    public static byte[] CLEAR_DATA() {
        return parseHexStringToBytes("0x8800");
    }


    public static byte[] READ_STEP_DATA(int week) {
        return parseHexStringToBytes("0xC601" + String.format("%02d", week));
    }

    public static byte[] CALL_DEVICE() {
        return parseHexStringToBytes("0xf30101");
    }

    public static byte[] SET_DEVICE_TIME_NOW() {
        String setDate = "0xC207";
        Calendar cal = new GregorianCalendar();
        String year = setWidth(String.format("%2s", Integer.toHexString(cal.get(Calendar.YEAR) - 2000)));
        String month = setWidth(String.format("%2s", Integer.toHexString(cal.get(Calendar.MONTH) + 1)));
        String date = setWidth(String.format("%2s", Integer.toHexString(cal.get(Calendar.DATE))));
        String hour = setWidth(String.format("%2s", Integer.toHexString(cal.get(Calendar.HOUR_OF_DAY))));
        String min = setWidth(String.format("%2s", Integer.toHexString(cal.get(Calendar.MINUTE))));
        String sec = setWidth(String.format("%2s", Integer.toHexString(cal.get(Calendar.SECOND))));
        int weekTemp = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekTemp == 0) {
            weekTemp = 7;
        }
        String week = setWidth(String.format("%2s", weekTemp));
        setDate += year + month + date + hour + min + sec + week;
        Log.e("TIME", year + " / " + month + " / " + date + " / " + hour + " / " + min + " / " + sec + " / " + week);
        return parseHexStringToBytes(setDate);
    }

    public static byte[] READ_SPORTS_CURVE_DATA() {
        return parseHexStringToBytes("0xC403031503");
//        return parseHexStringToBytes("0xC7020108");
    }

    public static byte[] OFF_ANTI_LOST() {
        return parseHexStringToBytes("0x01a102a2");
    }

    public static byte[] SET_USER_DATA(int gender, int height, int weight, int stride, int runningStride) {

        String ex = "0x832f00000000000000000000000000000000000000000000000000000000000000000001b46446640b1e071e0200000000";

        String info = "0x832f000000000000000000000000000000000000000000000000000000000000000000";
        info += setWidth(String.valueOf(gender));
        info += setWidth(Integer.toHexString(height));
        info += setWidth(Integer.toHexString(weight));
        info += setWidth(Integer.toHexString(stride));
        info += setWidth(Integer.toHexString(runningStride));
        info += "0b1e071e0200000000";

        Log.e("USERDATA", ex + "\n" + info);
        return parseHexStringToBytes(info);
    }


    public static String setWidth(String format) {
        if (format.trim().length() == 2) {
            return format;
        } else {
            return "0" + format.trim();
        }
    }


    public static byte[] parseHexStringToBytes(String hex) {
        hex = hex.toLowerCase(Locale.getDefault());
        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] bytes = new byte[tmp.length() / 2];
        String part = "";
        int checksum = 0;

        for (int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
            bytes[i] = Integer.decode(part).byteValue();
            if (i > 1) {
                if (bytes[i] < 0x00) {

                    checksum = checksum ^ bytes[i] + 256;

                } else {
                    checksum ^= bytes[i];

                }
            } else if (i == bytes.length - 1) {

            }
        }

        String cs = setWidth(Integer.toHexString(checksum));
        Log.e("CheckSum", cs);
        String res = (hex + cs).substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] resBytes = new byte[res.length() / 2];
        String resPart = "";

        for (int i = 0; i < resBytes.length; ++i) {
            resPart = "0x" + res.substring(i * 2, i * 2 + 2);
            resBytes[i] = Long.decode(resPart).byteValue();
        }

        return resBytes;
    }
}
