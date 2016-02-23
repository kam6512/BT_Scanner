package com.rainbow.kam.bt_scanner.tools.vidonn_sample;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Calendar;

public class DevOperation_X6 {
    private BluetoothGattCharacteristic cha_Operiation_NotificationData;
    private BluetoothGattCharacteristic characteristic_Write;
    private BluetoothGatt mBluetoothGatt;


    public DevOperation_X6(BluetoothGatt paramBluetoothGatt) {
        this.mBluetoothGatt = paramBluetoothGatt;
    }


    private short CRC_16(byte[] paramArrayOfByte) {
        int i = 0;
        int n = 0;
        for (; ; ) {
            try {
                if (n < paramArrayOfByte.length) {
                    break label59;
                }
                return i;
            } catch (Exception paramArrayOfByte) {
                int i2;
                int k;
                int m;
                return -1;
            }
            i2 = paramArrayOfByte[n];
            k = i;
            if ((i2 & i1) != 0) {
                m = (short) (i ^ 0x1021);
            }
            i1 >>= 1;
            i = m;
            break label64;
            i = (short) (i << 1);
            continue;
            label59:
            int i1 = 128;
            label64:
            if (i1 == 0) {
                n += 1;
            } else if ((0x8000 & i) != 0) {
                int j = (short) ((short) (i << 1) ^ 0x1021);
            }
        }
    }


    private String byteToString(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length);
        int j = paramArrayOfByte.length;
        int i = 0;
        for (; ; ) {
            if (i >= j) {
                return localStringBuilder.toString();
            }
            localStringBuilder.append(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[i])}).toString());
            i += 1;
        }
    }


    public static byte[] int2Bytes_2Bytes(int paramInt) {
        return new byte[]{(byte) ((0xFF00 & paramInt) >> 8), (byte) (paramInt & 0xFF)};
    }


    public static byte[] int2Bytes_4Bytes(int paramInt) {
        int i = (byte) (paramInt & 0xFF);
        int j = (byte) (paramInt >> 8 & 0xFF);
        int k = (byte) (paramInt >> 16 & 0xFF);
        return new byte[]{(byte) (paramInt >>> 24), k, j, i};
    }


    public static byte[] short2bytes(short paramShort) {
        byte[] arrayOfByte = new byte[2];
        short s = 1;
        int i = paramShort;
        paramShort = s;
        for (; ; ) {
            if (paramShort < 0) {
                return arrayOfByte;
            }
            arrayOfByte[paramShort] = ((byte) (i % 256));
            i = (short) (i >> 8);
            paramShort -= 1;
        }
    }


    public static int weekTransform(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8) {
        return 0x0 | paramInt1 << 7 | paramInt2 << 6 | paramInt8 << 5 | paramInt7 << 4 | paramInt6 << 3 | paramInt5 << 2 | paramInt4 << 1 | paramInt3;
    }


    private void wirteCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, byte[] paramArrayOfByte) {
        try {
            paramBluetoothGattCharacteristic.setValue(paramArrayOfByte);
            paramBluetoothGattCharacteristic.setWriteType(1);
            this.mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
        }
    }


    public byte[] packageAttributeData(byte paramByte, byte[] paramArrayOfByte, int paramInt) {
        int m = paramArrayOfByte.length;
        int k = m;
        if (m > paramInt) {
            k = paramInt;
        }
        byte[] arrayOfByte = int2Bytes_2Bytes(k);
        int i = arrayOfByte[0];
        int j = arrayOfByte[1];
        arrayOfByte = new byte[k + 3];
        arrayOfByte[0] = paramByte;
        arrayOfByte[1] = j;
        arrayOfByte[2] = i;
        paramInt = 0;
        for (; ; ) {
            if (paramInt >= k) {
                return arrayOfByte;
            }
            arrayOfByte[(paramInt + 3)] = paramArrayOfByte[paramInt];
            paramInt += 1;
        }
    }


    public byte[] packageNotificationData(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3) {
        int j = paramArrayOfByte1.length;
        int k = paramArrayOfByte2.length;
        byte[] arrayOfByte = new byte[j + 5 + k + paramArrayOfByte3.length];
        arrayOfByte[0] = 0;
        arrayOfByte[1] = -95;
        arrayOfByte[2] = -94;
        arrayOfByte[3] = -93;
        arrayOfByte[4] = -92;
        int i = 0;
        if (i >= paramArrayOfByte1.length) {
            i = 0;
            label64:
            if (i < paramArrayOfByte2.length) {
                break label104;
            }
            i = 0;
        }
        for (; ; ) {
            if (i >= paramArrayOfByte3.length) {
                return arrayOfByte;
                arrayOfByte[(i + 5)] = paramArrayOfByte1[i];
                i += 1;
                break;
                label104:
                arrayOfByte[(i + 5 + j)] = paramArrayOfByte2[i];
                i += 1;
                break label64;
            }
            arrayOfByte[(i + 5 + j + k)] = paramArrayOfByte3[i];
            i += 1;
        }
    }


    public byte[][] reCountData(int paramInt, byte[] paramArrayOfByte) {
        int i = paramArrayOfByte.length;
        int j = i / paramInt;
        int m = i % paramInt;
        i = j;
        if (m != 0) {
            i = j + 1;
        }
        byte[][] arrayOfByte = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{i, paramInt});
        j = 0;
        if (j >= i) {
            return arrayOfByte;
        }
        if ((j == i - 1) && (m != 0)) {
            arrayOfByte1 = new byte[m];
            i = 0;
            for (; ; ) {
                if (i >= arrayOfByte1.length) {
                    arrayOfByte[j] = arrayOfByte1;
                    return arrayOfByte;
                }
                arrayOfByte1[i] = paramArrayOfByte[(j * paramInt + i)];
                i += 1;
            }
        }
        byte[] arrayOfByte1 = new byte[paramInt];
        int k = 0;
        for (; ; ) {
            if (k >= arrayOfByte1.length) {
                arrayOfByte[j] = arrayOfByte1;
                j += 1;
                break;
            }
            arrayOfByte1[k] = paramArrayOfByte[(j * paramInt + k)];
            k += 1;
        }
    }


    public void readAlarmClock(byte paramByte) {
        try {
            writeCode(new byte[]{34, paramByte}, true);
            return;
        } catch (Exception localException) {
            System.out.println("读取闹钟异常" + localException.toString());
        }
    }


    public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        try {
            this.mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
        }
    }


    public void readCurrentValue() {
        try {
            writeCode(new byte[]{3, 1}, true);
            return;
        } catch (Exception localException) {
            System.out.println("读取当前运动数据异常" + localException.toString());
        }
    }


    public void readDate_Time() {
        try {
            writeCode(new byte[]{33}, true);
            return;
        } catch (Exception localException) {
            System.out.println(" 读取时间异常" + localException.toString());
        }
    }


    public void readDevVision() {
        try {
            byte[] arrayOfByte1 = new byte[1];
            arrayOfByte1[0] = 2;
            byte[] arrayOfByte2 = short2bytes(CRC_16(arrayOfByte1));
            byte[] arrayOfByte3 = new byte[arrayOfByte1.length + 4];
            arrayOfByte3[0] = -91;
            arrayOfByte3[1] = ((byte) arrayOfByte1.length);
            arrayOfByte3[(arrayOfByte3.length - 2)] = arrayOfByte2[1];
            arrayOfByte3[(arrayOfByte3.length - 1)] = arrayOfByte2[0];
            int i = 2;
            for (; ; ) {
                if (i >= arrayOfByte1.length + 2) {
                    wirteCharacteristic(this.characteristic_Write, arrayOfByte3);
                    return;
                }
                arrayOfByte3[i] = arrayOfByte1[(i - 2)];
                i += 1;
            }
            return;
        } catch (Exception localException) {
            System.out.println("读取版本号异常" + localException.toString());
        }
    }


    public void readHistoryRecodeDatail(byte paramByte1, byte paramByte2) {
        writeCode(new byte[]{5, paramByte1, paramByte2}, true);
    }


    public void readHistoryRecodeDate() {
        try {
            writeCode(new byte[]{4}, true);
            return;
        } catch (Exception localException) {
            System.out.println("读取历史映射表异常" + localException.toString());
        }
    }


    public void readHistoryRecodeStatistics() {
        writeCode(new byte[]{6}, true);
    }


    public void readMAC_SN() {
        try {
            writeCode(new byte[]{2}, true);
            return;
        } catch (Exception localException) {
            System.out.println("读取MAC_SN号异常" + localException.toString());
        }
    }


    public void readPersonalInfo(byte paramByte) {
        try {
            writeCode(new byte[]{32, paramByte}, true);
            return;
        } catch (Exception localException) {
            System.out.println("读取个人信息异常" + localException.toString());
        }
    }


    public boolean sendNotificationData(String paramString1, String paramString2, String paramString3) {
        for (; ; ) {
            int i;
            try {
                byte[] arrayOfByte = paramString1.getBytes("UTF-8");
                paramString1 = packageAttributeData((byte) 3, paramString1, 160);
            } catch (UnsupportedEncodingException paramString1) {
                try {
                    paramString1 = paramString2.getBytes("UTF-8");
                } catch (UnsupportedEncodingException paramString1) {
                    paramString1.printStackTrace();
                    BluetoothLeService_Vidonn2.flag_IsMessageContext = false;
                    return false;
                }
                try {
                    paramString3 = paramString3.getBytes("UTF-8");
                    arrayOfByte = packageAttributeData((byte) 1, arrayOfByte, 32);
                    if (!paramString2.equals("")) {
                        break label117;
                    }
                    paramString1 = new byte[0];
                    paramString1 = reCountData(20, packageNotificationData(arrayOfByte, paramString1, packageAttributeData((byte) 0, paramString3, 32)));
                    i = 0;
                    if (i < paramString1.length) {
                        break label130;
                    }
                    BluetoothLeService_Vidonn2.flag_IsMessageContext = false;
                    return true;
                } catch (UnsupportedEncodingException paramString1) {
                    BluetoothLeService_Vidonn2.flag_IsMessageContext = false;
                    paramString1.printStackTrace();
                    return false;
                }
                paramString1 = paramString1;
                BluetoothLeService_Vidonn2.flag_IsMessageContext = false;
                paramString1.printStackTrace();
                return false;
            }
            label117:
            continue;
            try {
                label130:
                this.cha_Operiation_NotificationData.setValue(paramString1[i]);
                this.cha_Operiation_NotificationData.setWriteType(1);
                this.mBluetoothGatt.writeCharacteristic(this.cha_Operiation_NotificationData);
                Thread.sleep(100L);
                i += 1;
            } catch (Exception paramString1) {
                BluetoothLeService_Vidonn2.flag_IsMessageContext = false;
            }
        }
        return false;
    }


    public void setReset(byte paramByte) {
        writeCode(new byte[]{64, paramByte}, true);
    }


    public void setWriteCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        this.characteristic_Write = paramBluetoothGattCharacteristic;
    }


    public void setWriteCharacteristic_NotificationData(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        this.cha_Operiation_NotificationData = paramBluetoothGattCharacteristic;
    }


    public void writeAlarmClock(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) {
        }
        for (; ; ) {
            return;
            try {
                if (paramArrayOfByte.length == 6) {
                    int i = paramArrayOfByte[0];
                    int j = paramArrayOfByte[1];
                    int k = paramArrayOfByte[2];
                    int m = paramArrayOfByte[3];
                    int n = paramArrayOfByte[4];
                    int i1 = paramArrayOfByte[5];
                    System.out.println("----写入闹钟  ID:" + paramArrayOfByte[0] + " type:" + paramArrayOfByte[1] + " enable:" + paramArrayOfByte[2] + " Time:" + paramArrayOfByte[3] + ":" + paramArrayOfByte[4] + " remindTime:" + paramArrayOfByte[5]);
                    writeCode(new byte[]{34, i, j, k, m, n, i1}, false);
                    return;
                }
            } catch (Exception paramArrayOfByte) {
                System.out.println("写入闹钟异常" + paramArrayOfByte.toString());
            }
        }
    }


    public void writeCode(byte[] paramArrayOfByte, boolean paramBoolean) {
        byte[] arrayOfByte1 = short2bytes(CRC_16(paramArrayOfByte));
        byte[] arrayOfByte2 = new byte[paramArrayOfByte.length + 4];
        int i;
        if (paramBoolean) {
            arrayOfByte2[0] = -91;
            arrayOfByte2[1] = ((byte) paramArrayOfByte.length);
            arrayOfByte2[(arrayOfByte2.length - 2)] = arrayOfByte1[1];
            arrayOfByte2[(arrayOfByte2.length - 1)] = arrayOfByte1[0];
            i = 2;
        }
        for (; ; ) {
            if (i >= paramArrayOfByte.length + 2) {
                wirteCharacteristic(this.characteristic_Write, arrayOfByte2);
                return;
                arrayOfByte2[0] = 37;
                break;
            }
            arrayOfByte2[i] = paramArrayOfByte[(i - 2)];
            i += 1;
        }
    }


    public void writeDate_Time() {
        for (; ; ) {
            int k;
            int i4;
            try {
                Object localObject = Calendar.getInstance();
                ((Calendar) localObject).get(11);
                int m = ((Calendar) localObject).get(1);
                int n = ((Calendar) localObject).get(2);
                int i1 = ((Calendar) localObject).get(5);
                k = ((Calendar) localObject).get(10);
                int i2 = ((Calendar) localObject).get(12);
                int i3 = ((Calendar) localObject).get(13);
                i4 = ((Calendar) localObject).get(9);
                int i = ((Calendar) localObject).get(7);
                if (i == 1) {
                    i = 7;
                    break label188;
                    localObject = int2Bytes_2Bytes(m);
                    writeCode(new byte[]{33, localObject[1], localObject[0], (byte) (n + 1), (byte) i1, (byte) j, (byte) i2, (byte) i3, (byte) i}, false);
                } else {
                    i -= 1;
                }
            } catch (Exception localException) {
                System.out.println("写入时间异常" + localException.toString());
                return;
            }
            label188:
            int j = k;
            if (i4 == 1) {
                j = k;
                if (k < 12) {
                    j = k + 12;
                }
            }
        }
    }


    public void writePersonalInfo(byte paramByte, byte[] paramArrayOfByte) {
        try {
            byte[] arrayOfByte = new byte[paramArrayOfByte.length + 2];
            arrayOfByte[0] = 32;
            arrayOfByte[1] = paramByte;
            int i = 0;
            for (; ; ) {
                if (i >= paramArrayOfByte.length) {
                    System.out.println("写入个人信息" + byteToString(arrayOfByte));
                    writeCode(arrayOfByte, false);
                    return;
                }
                arrayOfByte[(i + 2)] = paramArrayOfByte[i];
                i += 1;
            }
            return;
        } catch (Exception paramArrayOfByte) {
            System.out.println("写入个人信息异常" + paramArrayOfByte.toString());
        }
    }


    public void writerAlert(byte paramByte) {
        writeCode(new byte[]{35, paramByte}, false);
    }


    public void writerNotification(byte paramByte1, byte paramByte2) {
        writeCode(new byte[]{97, 0, paramByte1, paramByte2, 1, -95, -94, -93, -92}, false);
    }


    public void writerNotificationCancel() {
        writeCode(new byte[]{97, 2, 1, 1, 1, -95, -94, -93, -92}, false);
    }
}


/* Location:              /Users/wooks/Downloads/dex2jar-2.0/vidonn-dex2jar.jar!/com/sz/vidonn2/bluetooth/service/DevOperation_X6.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */