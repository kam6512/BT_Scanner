package com.rainbow.kam.bt_scanner.tools.helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Calendar;

/**
 * Created by kam6512 on 2016-02-22.
 */
public class VidonnHelper {

    private static BluetoothGattCharacteristic characteristic_Write;
    private static BluetoothGatt mBluetoothGatt;


    //    private static short CRC_16(byte[] paramArrayOfByte) {
//
//        int[] table = {
//                0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
//                0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
//                0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
//                0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
//                0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
//                0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
//                0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
//                0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
//                0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
//                0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
//                0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
//                0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
//                0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
//                0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
//                0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
//                0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
//                0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
//                0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
//                0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
//                0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
//                0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
//                0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
//                0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
//                0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
//                0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
//                0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
//                0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
//                0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
//                0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
//                0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
//                0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
//                0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
//        };
//
//
//        int crc = 0x0000;
//        for (byte b : paramArrayOfByte) {
//            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
//        }
//
//        System.out.println("CRC16 = " + Integer.toHexString(crc));
//
//    }
    private static short CRC_16(byte[] paramArrayOfByte) {
        short i = 0;
//        short k = 0;
//        while (true) {
//            short m = 128;
//            try {
//                if (k >= paramArrayOfByte.length) {
//                    short n = paramArrayOfByte[k];
//                    short j = i;
//                    if ((n & m) == 0)
//                        continue;
//                    j = (short) (i ^ 0x1021);
//                    m >>= 1;
//                    i = j;
//                    break label64;
//                    i = (short) (i << 1);
//                    continue;
//                }
//            } catch (Exception e) {
//                return -1;
//            }
//
//            label64:
//            if (m == 0) {
//                k += 1;
//                continue;
//            }
//            if ((0x8000 & i) == 0)
//                continue;
//            i = (short) ((short) (i << 1) ^ 0x1021);
//        }
        return i;
    }


    //    private String byteToString(byte[] paramArrayOfByte) {
//        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length);
//        int j = paramArrayOfByte.length;
//        int i = 0;
//        for (; ; ) {
//            if (i >= j) {
//                return localStringBuilder.toString();
//            }
//            localStringBuilder.append(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[i])}).toString());
//            i += 1;
//        }
//    }
    private static byte[] short2bytes(int param) {
        byte[] arrayOfByte = new byte[2];
        short s = 1;
        int i = param;
        for (; ; ) {
            if (s < 0) {
                return arrayOfByte;
            }
            arrayOfByte[s] = ((byte) (i % 256));
            i = (short) (i >> 8);
            s -= 1;
        }
    }


    //
//
    private static void wirteCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, byte[] paramArrayOfByte) {
        paramBluetoothGattCharacteristic.setValue(paramArrayOfByte);
        paramBluetoothGattCharacteristic.setWriteType(1);
        mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
        return;
    }


    public static void setBluetoothGatt(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        mBluetoothGatt = bluetoothGatt;
        characteristic_Write = bluetoothGattCharacteristic;
    }

    //    public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
//        this.mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
//    }


//    public static void readCurrentValue() {
//        try {
//
//            writeCode(new byte[]{3, 1}, true);
//            return;
//        } catch (Exception localException) {
//            System.out.println("读取当前运动数据异常" + localException.toString());
//
//        }
//    }


    //    public void readHistoryRecodeDatail(byte paramByte1, byte paramByte2) {
//        writeCode(new byte[]{5, paramByte1, paramByte2}, true);
//    }
//
//
//    public void readHistoryRecodeDate() {
//        try {
//            writeCode(new byte[]{4}, true);
//            return;
//        } catch (Exception localException) {
//            System.out.println("读取历史映射表异常" + localException.toString());
//        }
//    }
//
//
//    public void readHistoryRecodeStatistics() {
//        writeCode(new byte[]{6}, true);
//    }

//    public void readDate_Time()
//    {
//        try
//        {
//            writeCode(new byte[] { 33 }, true);
//            return;
//        }
//        catch (Exception localException)
//        {
//            System.out.println(" 读取时间异常" + localException.toString());
//        }
//    }


    public static void writeCode(byte[] paramArrayOfByte, boolean paramBoolean) {
        byte[] arrayOfByte1 = short2bytes(CRC_16(paramArrayOfByte));
        byte[] arrayOfByte2 = new byte[paramArrayOfByte.length + 4];
        int i = 0;
        if (paramBoolean) {
            arrayOfByte2[0] = -91;
            arrayOfByte2[1] = (byte) paramArrayOfByte.length;
            arrayOfByte2[(arrayOfByte2.length - 2)] = arrayOfByte1[1];
            arrayOfByte2[(arrayOfByte2.length - 1)] = arrayOfByte1[0];
            i = 2;
            Log.e("writeCode", "arrayOfByte1 : " + arrayOfByte1[0] + arrayOfByte1[1]);
            Log.e("writeCode", "arrayOfByte2.length : " + arrayOfByte2.length);
            Log.e("writeCode", "arrayOfByte2[0] : " + arrayOfByte2[0] +
                    "\narrayOfByte2[1] : " + arrayOfByte2[1]
                    + "\narrayOfByte2[2] : " + arrayOfByte2[2]
                    + "\narrayOfByte2[3] : " + arrayOfByte2[3]);
        }
        while (true) {
            if (i >= paramArrayOfByte.length + 2) {
                wirteCharacteristic(characteristic_Write, arrayOfByte2);
                return;
//                arrayOfByte2[0] = 37;
            }
            arrayOfByte2[i] = paramArrayOfByte[(i - 2)];
            i += 1;
            Log.e("writeCode", "arrayOfByte2.length : " + arrayOfByte2.length);
            Log.e("writeCode", "arrayOfByte2[0] : " + arrayOfByte2[0] +
                    "\narrayOfByte2[1] : " + arrayOfByte2[1]
                    + "\narrayOfByte2[2] : " + arrayOfByte2[2]
                    + "\narrayOfByte2[3] : " + arrayOfByte2[3]);
        }
    }


    public static void writeDate_Time() {
        for (; ; ) {
            Calendar calendar = Calendar.getInstance();
//            calendar.get(Calendar.HOUR_OF_DAY);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            int apm = calendar.get(Calendar.AM_PM);

            int j = hour;
            if (apm == 1) {
                j = hour;
                if (hour < 12) {
                    j = hour + 12;
                }
            }

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1) {
                dayOfWeek = 7;

            } else {
                dayOfWeek -= 1;
            }
            byte[] int2Bytes_2Bytes = int2Bytes_2Bytes(year);
            writeCode(new byte[]{33, int2Bytes_2Bytes[1], int2Bytes_2Bytes[0], (byte) (month + 1), (byte) day, (byte) j, (byte) minute, (byte) second, (byte) dayOfWeek}, false);
            break;
        }
    }


    public static byte[] int2Bytes_2Bytes(int paramInt) {
        return new byte[]{(byte) ((0xFF00 & paramInt) >> 8), (byte) (paramInt & 0xFF)};
    }
}