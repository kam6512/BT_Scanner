package com.rainbow.kam.bt_scanner.operation;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by kam6512 on 2016-03-24.
 */
public class Prime extends Operator {
    private static final String hexLabel = "0x";
    private static final String hexFormat = "%02x";
    private static final String replaceReg = "[^[0-9][a-f]]";

    private static final String OPCODE_READ_TIME = "8900";
    private static final String OPCODE_WRITE_TIME = "C207";
    private static final String OPCODE_READ_CURRENT_VALUE = "C60108";


    @Override public byte[] readTime() {
        return getBytes(OPCODE_READ_TIME);
    }


    @Override public byte[] writeTime() {
        Calendar cal = new GregorianCalendar();

        StringBuilder time = new StringBuilder();
        time.append(OPCODE_WRITE_TIME);
        time.append(String.format(hexFormat, cal.get(Calendar.YEAR) - 2000));
        time.append(String.format(hexFormat, cal.get(Calendar.MONTH) + 1));
        time.append(String.format(hexFormat, cal.get(Calendar.DATE)));
        time.append(String.format(hexFormat, cal.get(Calendar.HOUR_OF_DAY)));
        time.append(String.format(hexFormat, cal.get(Calendar.MINUTE)));
        time.append(String.format(hexFormat, cal.get(Calendar.SECOND)));

        int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week == 0) {
            week = 7;
        }
        time.append(String.format(hexFormat, week));

        return getBytes(time.toString());
    }


    @Override public byte[] readCurrentValue() {
        return getBytes(OPCODE_READ_CURRENT_VALUE);
    }


    private byte[] getBytes(String hex) {
        hex = hex.toLowerCase(Locale.getDefault()).replaceAll(replaceReg, "");
        return parseHexStringToBytes(hex);
    }


    private byte[] parseHexStringToBytes(String hex) {
        byte[] bytes = new byte[(hex.length() / 2) + 1];

        int length = bytes.length;
        int checksum = 0;

        for (int i = 0; i < length - 1; ++i) {
            bytes[i] = Long.decode(hexLabel + hex.substring(i * 2, i * 2 + 2)).byteValue();

            if (i > 1 && i <= length - 2) {
                if (bytes[i] < 0x00) {
                    checksum ^= bytes[i] + 256;
                } else {
                    checksum ^= bytes[i];
                }
            }
        }
        bytes[length - 1] = Long.decode(hexLabel + String.format(hexFormat, checksum)).byteValue();

        return bytes;
    }


    private byte decodeValue(String value) {
        return Long.decode(hexLabel + value).byteValue();
    }
}
