package com.rainbow.kam.bt_scanner.tools.vidonn_sample;

import java.io.PrintStream;
import java.lang.reflect.Array;

public class DevDecode_X6 {
    private byte[] CRC_16(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
        int i = 0;
        int n = paramInt1;
        if (n >= paramInt1 + paramInt2) {
        }
        for (; ; ) {
            try {
                return short2bytes(i);
            } catch (Exception paramArrayOfByte) {
                int i2;
                int k;
                int m;
                return short2bytes((short) -1);
            }
            i2 = paramArrayOfByte[n];
            k = i;
            if ((i2 & i1) != 0) {
                m = (short) (i ^ 0x1021);
            }
            i1 >>= 1;
            i = m;
            break label83;
            i = (short) (i << 1);
            continue;
            int i1 = 128;
            label83:
            if (i1 == 0) {
                n += 1;
                break;
            }
            if ((0x8000 & i) != 0) {
                int j = (short) ((short) (i << 1) ^ 0x1021);
            }
        }
    }


    public static int byteToInt2_4Bytes(byte[] paramArrayOfByte) {
        int j = 0;
        int i = 0;
        for (; ; ) {
            if (i >= 4) {
                return j & 0xFFFFFFFF;
            }
            j = j << 8 | paramArrayOfByte[i] & 0xFF;
            i += 1;
        }
    }


    public static String byteToString(byte[] paramArrayOfByte) {
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


    public static int bytesToInt2_2Bytes(byte[] paramArrayOfByte) {
        return (paramArrayOfByte[0] & 0xFF) << 8 | paramArrayOfByte[1] & 0xFF;
    }


    public static byte[] cutBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
        byte[] arrayOfByte = new byte[paramInt2];
        int i = 0;
        for (; ; ) {
            if (i >= paramInt2) {
                return arrayOfByte;
            }
            arrayOfByte[i] = paramArrayOfByte[(paramInt1 + i)];
            i += 1;
        }
    }


    public static int[] separateData(byte[] paramArrayOfByte) {
        int j = paramArrayOfByte[1];
        int i = paramArrayOfByte[0];
        paramArrayOfByte[1] = ((byte) (paramArrayOfByte[1] & 0xF));
        return new int[]{j >>> 4 & 0xF, bytesToInt2_2Bytes(new byte[]{paramArrayOfByte[1], i})};
    }


    private byte[] short2bytes(short paramShort) {
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


    public static byte[] switchBytes(byte[] paramArrayOfByte) {
        int j;
        byte[] arrayOfByte;
        int i;
        do {
            try {
                j = paramArrayOfByte.length;
                arrayOfByte = new byte[j];
                i = 0;
            } catch (Exception paramArrayOfByte) {
                return null;
            }
            arrayOfByte[i] = paramArrayOfByte[(j - i - 1)];
            i += 1;
        } while (i < j);
        return arrayOfByte;
    }


    public static int[] weekTransTo(int paramInt) {
        do {
            try {
                arrayOfInt = new int[8];
                i = (byte) paramInt;
                paramInt = 0;
            } catch (Exception localException) {
                int[] arrayOfInt;
                int i;
                return null;
            }
            arrayOfInt[(7 - paramInt)] = (((int) Math.pow(2.0D, 7 - paramInt) & i) >>> 7 - paramInt);
            paramInt += 1;
        } while (paramInt < 8);
        return localException;
    }


    public int[] decode_AlarmClock(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length < 5) {
            return null;
        }
        if (paramArrayOfByte[0] != -91) {
            return null;
        }
        int i = paramArrayOfByte.length - 4;
        if (paramArrayOfByte[1] != i) {
            return null;
        }
        byte[] arrayOfByte = CRC_16(paramArrayOfByte, 2, i);
        if (arrayOfByte.length != 2) {
            return null;
        }
        if ((arrayOfByte[0] == paramArrayOfByte[(paramArrayOfByte.length - 1)]) && (arrayOfByte[1] == paramArrayOfByte[(paramArrayOfByte.length - 2)])) {
            i = cutBytes(paramArrayOfByte, 3, 1)[0];
            int j = cutBytes(paramArrayOfByte, 4, 1)[0];
            int k = cutBytes(paramArrayOfByte, 5, 1)[0];
            int m = cutBytes(paramArrayOfByte, 6, 1)[0];
            int n = cutBytes(paramArrayOfByte, 7, 1)[0];
            int i1 = cutBytes(paramArrayOfByte, 8, 1)[0];
            weekTransTo(k);
            return new int[]{i, j, k, m, n, i1};
        }
        return null;
    }


    public int[] decode_CurrentValue(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length < 5) {
            return null;
        }
        if (paramArrayOfByte[0] != -91) {
            return null;
        }
        int i = paramArrayOfByte.length - 4;
        if (paramArrayOfByte[1] != i) {
            return null;
        }
        byte[] arrayOfByte1 = CRC_16(paramArrayOfByte, 2, i);
        if (arrayOfByte1.length != 2) {
            return null;
        }
        if ((arrayOfByte1[0] == paramArrayOfByte[(paramArrayOfByte.length - 1)]) && (arrayOfByte1[1] == paramArrayOfByte[(paramArrayOfByte.length - 2)])) {
            byte[] arrayOfByte2 = cutBytes(paramArrayOfByte, 4, 4);
            arrayOfByte1 = cutBytes(paramArrayOfByte, 8, 4);
            paramArrayOfByte = cutBytes(paramArrayOfByte, 12, 4);
            arrayOfByte2 = switchBytes(arrayOfByte2);
            arrayOfByte1 = switchBytes(arrayOfByte1);
            paramArrayOfByte = switchBytes(paramArrayOfByte);
            i = byteToInt2_4Bytes(arrayOfByte2);
            int j = byteToInt2_4Bytes(arrayOfByte1);
            return new int[]{i, byteToInt2_4Bytes(paramArrayOfByte), j};
        }
        return null;
    }


    public int decode_CurrentValue_Auto(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) {
            return 0;
        }
        return byteToInt2_4Bytes(switchBytes(paramArrayOfByte));
    }


    public int[] decode_Date_Time(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length < 5) {
            return null;
        }
        if (paramArrayOfByte[0] != -91) {
            return null;
        }
        int i = paramArrayOfByte.length - 4;
        if (paramArrayOfByte[1] != i) {
            return null;
        }
        byte[] arrayOfByte = CRC_16(paramArrayOfByte, 2, i);
        if (arrayOfByte.length != 2) {
            return null;
        }
        if ((arrayOfByte[0] == paramArrayOfByte[(paramArrayOfByte.length - 1)]) && (arrayOfByte[1] == paramArrayOfByte[(paramArrayOfByte.length - 2)])) {
            return new int[]{bytesToInt2_2Bytes(new byte[]{cutBytes(paramArrayOfByte, 3, 1)[0], cutBytes(paramArrayOfByte, 4, 1)[0]}), cutBytes(paramArrayOfByte, 5, 1)[0], cutBytes(paramArrayOfByte, 6, 1)[0], cutBytes(paramArrayOfByte, 7, 1)[0], cutBytes(paramArrayOfByte, 8, 1)[0], cutBytes(paramArrayOfByte, 9, 1)[0], cutBytes(paramArrayOfByte, 10, 1)[0]};
        }
        return null;
    }


    public int[][] decode_HistoryRecodeDatail(byte[] paramArrayOfByte) {
        int[][] arrayOfInt2 = null;
        int[][] arrayOfInt1;
        if (paramArrayOfByte.length < 67) {
            arrayOfInt1 = arrayOfInt2;
        }
        int i;
        do {
            byte[] arrayOfByte;
            do {
                do {
                    do {
                        do {
                            do {
                                return arrayOfInt1;
                                arrayOfInt1 = arrayOfInt2;
                            } while (paramArrayOfByte[0] != -91);
                            i = paramArrayOfByte.length - 4;
                            arrayOfInt1 = arrayOfInt2;
                        } while (paramArrayOfByte[1] != i);
                        arrayOfByte = CRC_16(paramArrayOfByte, 2, i);
                        arrayOfInt1 = arrayOfInt2;
                    } while (arrayOfByte.length != 2);
                    arrayOfInt1 = arrayOfInt2;
                } while (arrayOfByte[0] != paramArrayOfByte[(paramArrayOfByte.length - 1)]);
                arrayOfInt1 = arrayOfInt2;
            } while (arrayOfByte[1] != paramArrayOfByte[(paramArrayOfByte.length - 2)]);
            arrayOfInt2 = (int[][]) Array.newInstance(Integer.TYPE, new int[]{31, 2});
            arrayOfInt2[0][0] = cutBytes(paramArrayOfByte, 4, 1)[0];
            i = 1;
            arrayOfInt1 = arrayOfInt2;
        } while (i >= arrayOfInt2.length);
        arrayOfInt2[i] = separateData(cutBytes(paramArrayOfByte, (i + 1) * 2 + 1, 2));
        if (arrayOfInt2[i][1] == 4095) {
            arrayOfInt2[i][0] = -1;
            arrayOfInt2[i][1] = 0;
        }
        for (; ; ) {
            i += 1;
            break;
            if (arrayOfInt2[i][1] == 3840) {
                arrayOfInt2[i][0] = -1;
                arrayOfInt2[i][1] = 0;
            }
        }
    }


    public int[][] decode_HistoryRecodeDate(byte[] paramArrayOfByte, int paramInt) {
        if (paramArrayOfByte.length < 5) {
            paramArrayOfByte = null;
            return paramArrayOfByte;
        }
        if (paramArrayOfByte[0] != -91) {
            return null;
        }
        int j = paramInt - 4;
        if (paramArrayOfByte[1] != j) {
            return null;
        }
        Object localObject = CRC_16(paramArrayOfByte, 2, j);
        if (localObject.length != 2) {
            return null;
        }
        if ((localObject[0] == paramArrayOfByte[(paramInt - 1)]) && (localObject[1] == paramArrayOfByte[(paramInt - 2)])) {
            localObject = (int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 4});
            byte[][] arrayOfByte = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{7, 5});
            j = 0;
            for (; ; ) {
                if (j * 5 + 8 + 1 >= paramInt) {
                    paramInt = 0;
                    for (; ; ) {
                        paramArrayOfByte = (byte[]) localObject;
                        if (paramInt >= arrayOfByte.length) {
                            break;
                        }
                        int i = arrayOfByte[paramInt][1];
                        arrayOfByte[paramInt][1] = arrayOfByte[paramInt][2];
                        arrayOfByte[paramInt][1] = i;
                        localObject[paramInt][0] = arrayOfByte[paramInt][0];
                        localObject[paramInt][1] = bytesToInt2_2Bytes(new byte[]{arrayOfByte[paramInt][2], arrayOfByte[paramInt][1]});
                        localObject[paramInt][2] = arrayOfByte[paramInt][3];
                        localObject[paramInt][3] = arrayOfByte[paramInt][4];
                        if ((localObject[paramInt][1] < 0) || (localObject[paramInt][1] > 9999)) {
                            localObject[paramInt][1] = 0;
                        }
                        if (localObject[paramInt][2] < 0) {
                            localObject[paramInt][2] = 0;
                        }
                        if (localObject[paramInt][3] < 0) {
                            localObject[paramInt][3] = 0;
                        }
                        paramInt += 1;
                    }
                }
                arrayOfByte[j] = cutBytes(paramArrayOfByte, j * 5 + 3, 5);
                j += 1;
            }
        }
        return null;
    }


    public String[] decode_MAC_SN(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length < 5) {
        }
        do {
            int i;
            do {
                do {
                    return null;
                } while (paramArrayOfByte[0] != -91);
                i = paramArrayOfByte.length - 4;
            } while (paramArrayOfByte[1] != i);
            arrayOfByte = CRC_16(paramArrayOfByte, 2, i);
        }
        while ((arrayOfByte.length != 2) || (arrayOfByte[0] != paramArrayOfByte[(paramArrayOfByte.length - 1)]) || (arrayOfByte[1] != paramArrayOfByte[(paramArrayOfByte.length - 2)]));
        byte[] arrayOfByte = switchBytes(cutBytes(paramArrayOfByte, 3, 6));
        paramArrayOfByte = switchBytes(cutBytes(paramArrayOfByte, 9, 4));
        return new String[]{byteToString(arrayOfByte), byteToString(paramArrayOfByte)};
    }


    public int[] decode_PersonalInfo(byte[] paramArrayOfByte, int paramInt) {
        if (paramArrayOfByte.length < 5) {
            return null;
        }
        if (paramArrayOfByte[0] != -91) {
            return null;
        }
        int i = paramArrayOfByte.length - 4;
        if (paramArrayOfByte[1] != i) {
            return null;
        }
        byte[] arrayOfByte = CRC_16(paramArrayOfByte, 2, i);
        if (arrayOfByte.length != 2) {
            return null;
        }
        if ((arrayOfByte[0] == paramArrayOfByte[(paramArrayOfByte.length - 1)]) && (arrayOfByte[1] == paramArrayOfByte[(paramArrayOfByte.length - 2)])) {
            if (paramInt == 1) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0] & 0xFF, cutBytes(paramArrayOfByte, 5, 1)[0] & 0xFF, cutBytes(paramArrayOfByte, 6, 1)[0] & 0xFF, cutBytes(paramArrayOfByte, 7, 1)[0] & 0xFF};
            }
            if (paramInt == 2) {
                return new int[]{bytesToInt2_2Bytes(cutBytes(switchBytes(paramArrayOfByte), 2, 2))};
            }
            if (paramInt == 3) {
                return new int[]{byteToInt2_4Bytes(cutBytes(switchBytes(paramArrayOfByte), 2, 4))};
            }
            if (paramInt == 4) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0], cutBytes(paramArrayOfByte, 5, 1)[0], cutBytes(paramArrayOfByte, 6, 1)[0], cutBytes(paramArrayOfByte, 7, 1)[0]};
            }
            if (paramInt == 6) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0], cutBytes(paramArrayOfByte, 5, 1)[0], cutBytes(paramArrayOfByte, 6, 1)[0], cutBytes(paramArrayOfByte, 7, 1)[0], cutBytes(paramArrayOfByte, 8, 1)[0]};
            }
            if (paramInt == 7) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0]};
            }
            if (paramInt == 8) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0]};
            }
            if (paramInt == 9) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0]};
            }
            if (paramInt == 11) {
                return new int[]{cutBytes(paramArrayOfByte, 4, 1)[0]};
            }
            return null;
        }
        return null;
    }


    public int[] getHistoryCalories(int[] paramArrayOfInt, int paramInt) {
        Object localObject;
        if (paramArrayOfInt == null) {
            localObject = null;
            return (int[]) localObject;
        }
        int j = paramArrayOfInt.length;
        int[] arrayOfInt = new int[j];
        int i = 0;
        for (; ; ) {
            localObject = arrayOfInt;
            if (i >= j) {
                break;
            }
            try {
                arrayOfInt[i] = (paramArrayOfInt[i] * paramInt / 965);
                i += 1;
            } catch (Exception paramArrayOfInt) {
                System.out.println("异常");
            }
        }
        return null;
    }


    public int[] getHistoryDistance(int[] paramArrayOfInt, int paramInt) {
        Object localObject;
        if (paramArrayOfInt == null) {
            localObject = null;
            return (int[]) localObject;
        }
        int j = paramArrayOfInt.length;
        int[] arrayOfInt = new int[j];
        int i = 0;
        for (; ; ) {
            localObject = arrayOfInt;
            if (i >= j) {
                break;
            }
            try {
                arrayOfInt[i] = (paramArrayOfInt[i] * paramInt / 241);
                i += 1;
            } catch (Exception paramArrayOfInt) {
                System.out.println("异常");
            }
        }
        return null;
    }
}


/* Location:              /Users/wooks/Downloads/dex2jar-2.0/vidonn-dex2jar.jar!/com/sz/vidonn2/bluetooth/service/DevDecode_X6.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */