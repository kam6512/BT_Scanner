package com.rainbow.kam.bt_scanner.tools.helper;

import com.rainbow.kam.bt_scanner.tools.data.item.RealmPrimeItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeHelper {

    public static final int INDEX_STEP = 0;
    public static final int INDEX_CALORIE = 1;
    public static final int INDEX_DISTANCE = 2;

    public static final byte[] getBytesForReadTime = getBytes("8900");
    public static byte[] getBytesForReset = getBytes("8700");
    public static byte[] getBytesForClear = getBytes("8800");
    public static final byte[] getBytesForReadExerciseData = getBytes("C60108");
    public static byte[] getBytesForCall = getBytes("f30101");


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

        return getBytes(time.toString());
    }


    public static byte[] getBytes(String hex) {
        hex = hex.toLowerCase(Locale.getDefault()).replaceAll("[^[0-9][a-f]]", "");
        return parseHexStringToBytes(hex);
    }


    private static byte[] parseHexStringToBytes(String hex) {
        byte[] bytes = new byte[(hex.length() / 2) + 1];

        int checksum = 0;

        for (int i = 0; i < bytes.length - 1; ++i) {
            bytes[i] = decodeValue(hex.substring(i * 2, i * 2 + 2));

            if (i > 1 && i <= bytes.length - 2) {
                if (bytes[i] < 0x00) {
                    checksum ^= bytes[i] + 256;
                } else {
                    checksum ^= bytes[i];
                }
            }
        }
        bytes[bytes.length - 1] = decodeValue(String.format("%02x", checksum));

        return bytes;
    }


    private static byte decodeValue(String value) {
        return Long.decode("0x" + value).byteValue();
    }
}
