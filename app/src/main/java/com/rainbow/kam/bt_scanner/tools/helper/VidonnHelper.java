package com.rainbow.kam.bt_scanner.tools.helper;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

import java.lang.reflect.Array;
import java.util.Calendar;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2016-02-22.
 */
public class VidonnHelper {


    public static byte[] readCurrentValue() {
        return writeCode(new byte[]{(byte) 3, (byte) 1}, true);
    }


    public static byte[] readHistoryRecodeDate() {
        return writeCode(new byte[]{(byte) 4}, true);
    }


    public static byte[] readHistoryRecodeDatail(byte blockID, byte hour) {
        return writeCode(new byte[]{(byte) 5, blockID, hour}, true);
    }


    public static byte[] readHistoryRecodeStatistics() {
        return writeCode(new byte[]{(byte) 6}, true);
    }


    public static byte[] readDate_Time() {
        return writeCode(new byte[]{(byte) 33}, true);
    }


    public static byte[] writeDate_Time() {
        Calendar calendar = Calendar.getInstance();
//            calendar.get(11);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int apm = calendar.get(Calendar.AM_PM);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            dayOfWeek = 7;
        } else {
            dayOfWeek--;
        }
        if (apm == 1 && hour < 12) {
            hour += 12;
        }
        byte[] year01 = int2Bytes_2Bytes(year);
        return writeCode(new byte[]{(byte) 33, year01[1], year01[0], (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second, (byte) dayOfWeek}, false);

    }


    @DebugLog
    public static int[][] decode_HistoryRecodeDatail(byte[] data) {
        int[][] iArr = null;
            byte dataLength = (byte) (data.length - 4);
            if (data[1] == dataLength) {
                byte[] crc = CRC_16(data, 2, dataLength);
                if (crc.length == 2 && crc[0] == data[data.length - 1] && crc[1] == data[data.length - 2]) {
                    iArr = (int[][]) Array.newInstance(Integer.TYPE, new int[]{31, 2});
                    iArr[0][0] = cutBytes(data, 4, 1)[0];
                    for (int i = 1; i < iArr.length; i++) {
                        iArr[i] = separateData(cutBytes(data, ((i + 1) * 2) + 1, 2));
                        if (iArr[i][1] == 4095) {
                            iArr[i][0] = -1;
                            iArr[i][1] = 0;
                        } else if (iArr[i][1] == 3840) {
                            iArr[i][0] = -1;
                            iArr[i][1] = 0;
                        }
                    }
                }

        }
        return iArr;
    }


    public static byte[] writeCode(byte[] opCode_Data, boolean isRead) {
        byte[] crc = short2bytes(OPERATION_CRC_16(opCode_Data));
        byte[] data_Send = new byte[(opCode_Data.length + 4)];
        if (isRead) {
            data_Send[0] = (byte) -91;
        } else {
            data_Send[0] = (byte) 37;
        }
        data_Send[1] = (byte) opCode_Data.length;
        data_Send[data_Send.length - 2] = crc[1];
        data_Send[data_Send.length - 1] = crc[0];
        for (int i = 2; i < opCode_Data.length + 2; i++) {
            data_Send[i] = opCode_Data[i - 2];
        }
        return data_Send;
    }


    public static byte[] cutBytes(byte[] data, int start, int length) {
        byte[] data_temp = new byte[length];
        for (int i = 0; i < length; i++) {
            data_temp[i] = data[start + i];
        }
        return data_temp;
    }


    public static int[] separateData(byte[] res) {
        int[] targets = new int[2];
        byte temp_Type = res[1];
        byte temp_Step = res[0];
        targets[0] = (temp_Type >>> 4) & 15;
        res[1] = (byte) (res[1] & 15);
        targets[1] = bytesToInt2_2Bytes(new byte[]{res[1], temp_Step});
        return targets;
    }


    public static int bytesToInt2_2Bytes(byte[] src) {
        return ((src[0] & MotionEventCompat.ACTION_MASK) << 8) | (src[1] & MotionEventCompat.ACTION_MASK);
    }


    public static byte[] short2bytes(short s) {
        byte[] bytes = new byte[2];
        for (int i = 1; i >= 0; i--) {
            bytes[i] = (byte) (s % AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY);
            s = (short) (s >> 8);
        }
        return bytes;
    }


    public static byte[] int2Bytes_2Bytes(int value) {
        return new byte[]{(byte) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & value) >> 8), (byte) (value & MotionEventCompat.ACTION_MASK)};
    }


    private static short OPERATION_CRC_16(byte[] data) {
        short crc_result = (short) 0;
        int i = 0;
        while (i < data.length) {
            try {
                for (int j = TransportMediator.FLAG_KEY_MEDIA_NEXT; j != 0; j >>= 1) {
                    if ((AccessibilityNodeInfoCompat.ACTION_PASTE & crc_result) != 0) {
                        crc_result = (short) (((short) (crc_result << 1)) ^ 4129);
                    } else {
                        crc_result = (short) (crc_result << 1);
                    }
                    if ((data[i] & j) != 0) {
                        crc_result = (short) (crc_result ^ 4129);
                    }
                }
                i++;
            } catch (Exception e) {
                return (short) -1;
            }
        }
        return crc_result;
    }


    private static byte[] CRC_16(byte[] data, int start, int length) {
        short crc_result = (short) 0;
        for (int i = start; i < start + length; i++) {
            for (int j = TransportMediator.FLAG_KEY_MEDIA_NEXT; j != 0; j >>= 1) {
                if ((AccessibilityNodeInfoCompat.ACTION_PASTE & crc_result) != 0) {
                    crc_result = (short) (((short) (crc_result << 1)) ^ 4129);
                } else {
                    crc_result = (short) (crc_result << 1);
                }
                if ((data[i] & j) != 0) {
                    crc_result = (short) (crc_result ^ 4129);
                }
            }
        }
        try {
            return short2bytes(crc_result);
        } catch (Exception e) {
            return short2bytes((short) -1);
        }
    }
}