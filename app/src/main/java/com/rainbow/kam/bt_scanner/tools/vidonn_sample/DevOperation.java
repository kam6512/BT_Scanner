package com.rainbow.kam.bt_scanner.tools.vidonn_sample;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class DevOperation {
    public static final UUID Alarm_UUID;
    public static final UUID battery_level_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final UUID battery_service_UUID;
    public static final UUID clock_UUID;
    public static final UUID date_UUID;
    public static final UUID device_name_UUID;
    public static final UUID device_pair_new_UUID;
    public static final UUID device_pair_old_UUID;
    public static final UUID device_versions_UUID;
    public static final UUID jz_UUID;
    public static final UUID macaddress_UUID;
    public static final UUID main_service_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    public static final UUID movementData_UUID;
    public static final UUID movementData_current_UUID;
    public static final UUID personal_UUID;
    public static final UUID sleep_UUID;
    public static final UUID wr_UUID;
    private BluetoothGatt mBluetoothGatt;

    static {
        device_pair_old_UUID = UUID.fromString("0000f007-0000-1000-8000-00805f9b34fb");
        device_name_UUID = UUID.fromString("0000f010-0000-1000-8000-00805f9b34fb");
        device_pair_new_UUID = UUID.fromString("0000f011-0000-1000-8000-00805f9b34fb");
        device_versions_UUID = UUID.fromString("0000f012-0000-1000-8000-00805f9b34fb");
        personal_UUID = UUID.fromString("0000f013-0000-1000-8000-00805f9b34fb");
        movementData_UUID = UUID.fromString("0000f014-0000-1000-8000-00805f9b34fb");
        macaddress_UUID = UUID.fromString("0000f015-0000-1000-8000-00805f9b34fb");
        date_UUID = UUID.fromString("0000f016-0000-1000-8000-00805f9b34fb");
        clock_UUID = UUID.fromString("0000f017-0000-1000-8000-00805f9b34fb");
        Alarm_UUID = UUID.fromString("0000f018-0000-1000-8000-00805f9b34fb");
        movementData_current_UUID = UUID.fromString("0000f019-0000-1000-8000-00805f9b34fb");
        jz_UUID = UUID.fromString("0000f01b-0000-1000-8000-00805f9b34fb");
        sleep_UUID = UUID.fromString("0000f01c-0000-1000-8000-00805f9b34fb");
        wr_UUID = UUID.fromString("0000f01d-0000-1000-8000-00805f9b34fb");
        battery_service_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    }

    public DevOperation(BluetoothGatt paramBluetoothGatt) {
        this.mBluetoothGatt = paramBluetoothGatt;
    }


    private void PktCalcCheckSum(byte[] paramArrayOfByte, int paramInt) {
        int j = 0;
        int i = 1;
        for (; ; ) {
            if (i >= paramInt - 1) {
                paramArrayOfByte[(paramInt - 1)] = ((byte) j);
                return;
            }
            j += (paramArrayOfByte[i] ^ i);
            i += 1;
        }
    }


    private boolean VerifyData(byte[] paramArrayOfByte, int paramInt) {
        int j = 0;
        if (paramArrayOfByte[0] != -11) {
            return false;
        }
        for (int i = 1; ; i = (byte) (i + 1)) {
            if (i >= paramInt - 1) {
                if (j != paramArrayOfByte[(paramInt - 1)]) {
                    break;
                }
                return true;
            }
            j = (byte) ((paramArrayOfByte[i] & 0xFF ^ i) + j);
        }
    }


    private void wirteCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, byte[] paramArrayOfByte) {
        try {
            paramBluetoothGattCharacteristic.setValue(paramArrayOfByte);
            paramBluetoothGattCharacteristic.setWriteType(2);
            this.mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
        }
    }


    public void ShowPairCode() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(device_pair_old_UUID);
                if (localObject != null) {
                    wirteCharacteristic((BluetoothGattCharacteristic) localObject, new byte[]{1});
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-写入配对码质量异常------------");
        }
    }


    public String byteToString(byte[] paramArrayOfByte) {
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


    public List<BluetoothGattService> getSupportedGattServices() {
        if (this.mBluetoothGatt == null) {
            return null;
        }
        return this.mBluetoothGatt.getServices();
    }


    public int[][] packClock(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, int[] paramArrayOfInt4, int[] paramArrayOfInt5) {
        int[][] arrayOfInt = (int[][]) Array.newInstance(Integer.TYPE, new int[]{4, 5});
        int i = 0;
        for (; ; ) {
            if (i >= arrayOfInt.length) {
                return arrayOfInt;
            }
            arrayOfInt[i][0] = paramArrayOfInt1[i];
            arrayOfInt[i][1] = paramArrayOfInt2[i];
            arrayOfInt[i][2] = paramArrayOfInt3[i];
            arrayOfInt[i][3] = paramArrayOfInt4[i];
            arrayOfInt[i][4] = paramArrayOfInt5[i];
            i += 1;
        }
    }


    public void readAlarmClockData() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(clock_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
        }
    }


    public void readBatteryValue() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(battery_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(battery_level_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取电量数据异常------------");
        }
    }


    public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        try {
            this.mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
        }
    }


    public void readCurrentDate() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(date_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取日期据异常------------");
        }
    }


    public void readCurrentValue() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(movementData_current_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取当前运动数据异常------------");
        }
    }


    public void readDevVision() {
        try {
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject == null) {
                return;
            }
            localObject = ((BluetoothGattService) localObject).getCharacteristic(device_versions_UUID);
            if (localObject != null) {
                readCharacteristic((BluetoothGattCharacteristic) localObject);
                return;
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取 版本号异常------------");
        }
    }


    public void readHistoryValue() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(movementData_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取历史数据异常------------");
        }
    }


    public void readMAC_SN() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(macaddress_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取MAC与序列号异常------------");
        }
    }


    public void readPersonalData() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(personal_UUID);
                if (localObject != null) {
                    readCharacteristic((BluetoothGattCharacteristic) localObject);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取个人信息数据异常------------");
        }
    }


    public int weekTransform(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7) {
        return 0x0 | paramInt1 << 6 | paramInt2 << 5 | paramInt3 << 4 | paramInt4 << 3 | paramInt5 << 2 | paramInt6 << 1 | paramInt7;
    }


    public void writeAlarmClockData(int paramInt, int[][] paramArrayOfInt) {
        for (; ; ) {
            byte[] arrayOfByte;
            try {
                if (this.mBluetoothGatt == null) {
                    return;
                }
                Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
                if (localObject == null) {
                    break;
                }
                localObject = ((BluetoothGattService) localObject).getCharacteristic(clock_UUID);
                if (localObject == null) {
                    break;
                }
                arrayOfByte = new byte[19];
                arrayOfByte[0] = -11;
                arrayOfByte[1] = ((byte) paramInt);
                i = paramArrayOfInt[paramInt][0];
            } catch (Exception paramArrayOfInt) {
                try {
                    if ((paramArrayOfInt.length != 4) || (paramArrayOfInt[0].length != 5)) {
                        break;
                    }
                    paramInt = paramArrayOfInt[1].length;
                    if (paramInt != 5) {
                        break;
                    }
                    paramInt = 0;
                    if (paramInt < 4) {
                        break label112;
                    }
                    PktCalcCheckSum(arrayOfByte, 19);
                    wirteCharacteristic((BluetoothGattCharacteristic) localObject, arrayOfByte);
                    return;
                } catch (Exception paramArrayOfInt) {
                    return;
                }
                paramArrayOfInt = paramArrayOfInt;
                System.out.println("Bluetooth-写入闹钟 2异常------------");
                return;
            }
            label112:
            int i;
            int j = paramArrayOfInt[paramInt][1];
            int k = paramArrayOfInt[paramInt][2];
            int m = paramArrayOfInt[paramInt][3];
            int n = paramArrayOfInt[paramInt][4];
            arrayOfByte[(paramInt * 4 + 2)] = ((byte) i);
            arrayOfByte[(paramInt * 4 + 3)] = ((byte) (j << 7 & 0x80 | k & 0x7F));
            arrayOfByte[(paramInt * 4 + 4)] = ((byte) (m & 0x1F));
            arrayOfByte[(paramInt * 4 + 5)] = ((byte) (n & 0x3F));
            paramInt += 1;
        }
    }


    public void writeCurrentDate() {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject1 = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject1 != null) {
                localObject1 = ((BluetoothGattService) localObject1).getCharacteristic(date_UUID);
                if (localObject1 != null) {
                    Object localObject2 = Calendar.getInstance();
                    ((Calendar) localObject2).get(11);
                    int k = ((Calendar) localObject2).get(1);
                    int m = ((Calendar) localObject2).get(2);
                    int n = ((Calendar) localObject2).get(5);
                    int j = ((Calendar) localObject2).get(10);
                    int i1 = ((Calendar) localObject2).get(12);
                    int i2 = ((Calendar) localObject2).get(13);
                    int i = j;
                    if (((Calendar) localObject2).get(9) == 1) {
                        i = j;
                        if (j < 12) {
                            i = j + 12;
                        }
                    }
                    localObject2 = new byte[8];
                    localObject2[0] = -11;
                    localObject2[1] = ((byte) (k - 2000));
                    localObject2[2] = ((byte) (m + 1 - 1));
                    localObject2[3] = ((byte) (n - 1));
                    localObject2[4] = ((byte) i);
                    localObject2[5] = ((byte) i1);
                    localObject2[6] = ((byte) i2);
                    PktCalcCheckSum((byte[]) localObject2, 8);
                    wirteCharacteristic((BluetoothGattCharacteristic) localObject1, (byte[]) localObject2);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-写入数据异常------------");
        }
    }


    public void writePersonalData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5) {
        try {
            if (this.mBluetoothGatt == null) {
                return;
            }
            Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
            if (localObject != null) {
                localObject = ((BluetoothGattService) localObject).getCharacteristic(personal_UUID);
                if (localObject != null) {
                    byte[] arrayOfByte = new byte[8];
                    arrayOfByte[0] = -11;
                    arrayOfByte[1] = ((byte) paramInt1);
                    arrayOfByte[2] = ((byte) paramInt2);
                    arrayOfByte[3] = ((byte) paramInt3);
                    arrayOfByte[4] = ((byte) paramInt4);
                    arrayOfByte[5] = ((byte) (paramInt5 >> 8 & 0xFF));
                    arrayOfByte[6] = ((byte) (paramInt5 & 0xFF));
                    PktCalcCheckSum(arrayOfByte, 8);
                    wirteCharacteristic((BluetoothGattCharacteristic) localObject, arrayOfByte);
                    return;
                }
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-写入个人信息数据异常------------");
        }
    }


    public void writeSpecialNotice(int paramInt) {
        for (; ; ) {
            byte[] arrayOfByte;
            int i;
            try {
                if (this.mBluetoothGatt == null) {
                    return;
                }
                Object localObject = this.mBluetoothGatt.getService(main_service_UUID);
                if (localObject == null) {
                    break;
                }
                localObject = ((BluetoothGattService) localObject).getCharacteristic(Alarm_UUID);
                if ((localObject == null) || ((paramInt != 1) && (paramInt != 2))) {
                    break;
                }
                arrayOfByte = new byte[18];
                i = 0;
                if (i >= arrayOfByte.length) {
                    arrayOfByte[0] = -11;
                    arrayOfByte[1] = ((byte) paramInt);
                    PktCalcCheckSum(arrayOfByte, 18);
                    wirteCharacteristic((BluetoothGattCharacteristic) localObject, arrayOfByte);
                    return;
                }
            } catch (Exception localException) {
                System.out.println("Bluetooth-写入通知异常------------");
                return;
            }
            arrayOfByte[i] = 0;
            i += 1;
        }
    }
}


/* Location:              /Users/wooks/Downloads/dex2jar-2.0/vidonn-dex2jar.jar!/com/sz/vidonn2/bluetooth/service/DevOperation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */