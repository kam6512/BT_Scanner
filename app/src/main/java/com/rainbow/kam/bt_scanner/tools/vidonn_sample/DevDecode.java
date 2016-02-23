package com.rainbow.kam.bt_scanner.tools.vidonn_sample;

import java.io.PrintStream;
import java.lang.reflect.Array;

public class DevDecode {
    private int[][] AlarmClock = (int[][]) Array.newInstance(Integer.TYPE, new int[]{8, 5});
    private final int AlarmClock_Counts = 8;
    private final int AlarmClock_ItemCounts = 5;
    private int[] CurrentSportData;
    private final int CurrentSportDataCounts = 9;
    private String[] Mac_Serial;
    private final int Mac_SerialCounts = 2;
    private boolean clockData1 = false;
    private boolean clockData2 = false;
    private int[] currentDate;
    private final int dateCounts = 6;
    private String[] historyDate = new String[7];
    private int[][] historyDistan = (int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 24});
    private int[][] historySteps = (int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 24});
    private final int history_Counts = 7;
    private final int history_ItemCounts = 24;
    private final int personalCounts = 5;
    private int[] personalInfo;


    private String byteToString(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length);
        int j = paramArrayOfByte.length;
        int i = 0;
        for (; ; ) {
            if (i >= j) {
                return localStringBuilder.toString();
            }
            localStringBuilder.append(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[i])}).trim());
            i += 1;
        }
    }


    public String Battery_Decode(byte[] paramArrayOfByte) {
        return String.valueOf(paramArrayOfByte[0]);
    }


    public boolean CurrentSportData_Decode(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length == 19) {
            try {
                String str1 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[1])}).trim();
                int i = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[2])}).trim() + str1, 16);
                int j = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[3])}).trim(), 16);
                int k = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[4])}).trim(), 16);
                int m = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[5])}).trim(), 16);
                str1 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[6])}).trim();
                String str2 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[7])}).trim();
                String str3 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[8])}).trim();
                int n = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[9])}).trim() + str3 + str2 + str1, 16);
                str1 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[10])}).trim();
                str2 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[11])}).trim();
                str3 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[12])}).trim();
                int i1 = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[13])}).trim() + str3 + str2 + str1, 16) / 10;
                str1 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[14])}).trim();
                str2 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[15])}).trim();
                str3 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[16])}).trim();
                this.CurrentSportData = new int[]{((i & 0x7E00) >> 9) + 2000, ((i & 0x1E0) >> 5) + 1, (i & 0x1F) + 1, j, k, m, n, i1, Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[17])}).trim() + str3 + str2 + str1, 16) / 100};
                return true;
            } catch (Exception paramArrayOfByte) {
                this.CurrentSportData = null;
                return false;
            }
        }
        this.CurrentSportData = null;
        return false;
    }


    public boolean Data_AlarmClock_Decode(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length == 19) {
        }
        int i;
        label373:
        do {
            try {
                k = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[1])}).trim(), 16);
                if (k == 0) {
                    this.clockData1 = true;
                }
                if (k != 1) {
                    break label373;
                }
                this.clockData2 = true;
            } catch (Exception paramArrayOfByte) {
                int k;
                int[] arrayOfInt;
                int m;
                int n;
                int i1;
                int i2;
                int i3;
                int j;
                this.AlarmClock = null;
                return false;
            }
            arrayOfInt = new int[7];
            m = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 4 + 2)])}).trim(), 16);
            n = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 4 + 3)])}).trim(), 16);
            i1 = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 4 + 3)])}).trim(), 16) & 0x7F;
            i2 = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 4 + 4)])}).trim(), 16);
            i3 = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 4 + 5)])}).trim(), 16);
            j = 0;
            for (; ; ) {
                if (j >= 7) {
                    this.AlarmClock[(k * 4 + i)][0] = m;
                    this.AlarmClock[(k * 4 + i)][1] = (n >> 7);
                    this.AlarmClock[(k * 4 + i)][2] = i1;
                    this.AlarmClock[(k * 4 + i)][3] = (i2 & 0x1F);
                    this.AlarmClock[(k * 4 + i)][4] = (i3 & 0x3F);
                    i += 1;
                    break;
                }
                arrayOfInt[j] = (((int) Math.pow(2.0D, 6 - j) & i1) >> 6 - j);
                j += 1;
            }
            return false;
            i = 0;
        } while (i < 4);
        return true;
    }


    public boolean Data_Personal_Decode(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length == 8) {
            try {
                this.personalInfo = new int[]{Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[1])}).trim(), 16), Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[2])}).trim(), 16), Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[3])}).trim(), 16), Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[4])}).trim(), 16), Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[5])}).trim() + String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[6])}).trim(), 16)};
                return true;
            } catch (Exception paramArrayOfByte) {
                this.personalInfo = null;
                return false;
            }
        }
        this.personalInfo = null;
        return false;
    }


    public boolean Date_Decode(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length == 8) {
            try {
                this.currentDate = new int[]{Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[1])}).trim(), 16) + 2000, Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[2])}).trim(), 16) + 1, Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[3])}).trim(), 16) + 1, Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[4])}).trim(), 16), Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[5])}).trim(), 16), Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[6])}).trim(), 16)};
                return true;
            } catch (Exception paramArrayOfByte) {
                this.currentDate = null;
                return false;
            }
        }
        this.currentDate = null;
        return false;
    }


    public boolean MAC_Serial_Decode(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length == 12) {
            String str1 = "";
            String str2 = "";
            int i = 1;
            if (i >= 7) {
                i = 7;
            }
            for (; ; ) {
                if (i >= 11) {
                }
                try {
                    this.Mac_Serial = new String[]{str1, str2};
                    return true;
                } catch (Exception paramArrayOfByte) {
                    this.Mac_Serial = null;
                    return false;
                }
                str1 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[i])}).trim() + str1;
                i += 1;
                break;
                str2 = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[i])}).trim() + str2;
                i += 1;
            }
        }
        this.Mac_Serial = null;
        return false;
    }


    public String[] VersionCode_Decode(byte[] paramArrayOfByte) {
        paramArrayOfByte = byteToString(paramArrayOfByte).trim();
        if (paramArrayOfByte.length() > 18) {
            return new String[]{paramArrayOfByte.substring(2, 10), paramArrayOfByte.substring(10, 18)};
        }
        return null;
    }


    public boolean WeekData_Decode(byte[] paramArrayOfByte) {
        int i;
        int k;
        if (paramArrayOfByte.length == 17) {
            try {
                str = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[2])}).trim();
                i = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[3])}).trim() + str, 16);
                System.out.println();
                j = paramArrayOfByte[1] >> 4 & 0xF;
                k = paramArrayOfByte[1] & 0xF;
                this.historyDate[j] = (((i & 0x7E00) >> 9) + 2000 + "-" + (((i & 0x1E0) >> 5) + 1) + "-" + ((i & 0x1F) + 1));
                if ((k == 0) || (k == 1) || (k == 2)) {
                    break label412;
                }
                if (k != 3) {
                    break label422;
                }
            } catch (Exception paramArrayOfByte) {
                String str;
                int j;
                int m;
                this.historyDate = null;
                this.historySteps = null;
                this.historyDistan = null;
                return false;
            }
            str = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 2 + 4)])}).trim();
            m = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 2 + 5)])}).trim() + str, 16);
            this.historySteps[j][(k * 6 + i)] = m;
            i += 1;
            break label414;
        }
        for (; ; ) {
            if (i < 6) {
                str = String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 2 + 4)])}).trim();
                m = Integer.parseInt(String.format("%02X ", new Object[]{Byte.valueOf(paramArrayOfByte[(i * 2 + 5)])}).trim() + str, 16);
                this.historyDistan[j][((k - 4) * 6 + i)] = m;
                i += 1;
                continue;
                return false;
                label412:
                i = 0;
                label414:
                if (i < 6) {
                    break;
                }
            }
            return true;
            label422:
            if ((k != 4) && (k != 5) && (k != 6) && (k != 7)) {
                return false;
            }
            i = 0;
        }
    }


    public boolean checkAlarmClockData() {
        return (this.clockData1) && (this.clockData2);
    }


    public int[][] getAlarmClock() {
        Object localObject2 = null;
        Object localObject1 = localObject2;
        int i;
        if (this.AlarmClock.length == 8) {
            i = 0;
        }
        for (; ; ) {
            if (i >= 8) {
                localObject1 = this.AlarmClock;
            }
            do {
                return (int[][]) localObject1;
                localObject1 = localObject2;
            } while (this.AlarmClock[i].length != 5);
            i += 1;
        }
    }


    public int[] getCurrentDate() {
        return this.currentDate;
    }


    public int[] getCurrentSportData() {
        return this.CurrentSportData;
    }


    public String[] getHistoryDate() {
        return this.historyDate;
    }


    public int[][] getHistoryDistan() {
        return this.historyDistan;
    }


    public int[][] getHistorySteps() {
        return this.historySteps;
    }


    public String[] getMac_Serial() {
        return this.Mac_Serial;
    }


    public int[] getPersonalInfo() {
        return this.personalInfo;
    }


    public void initData_AlarmClock() {
        this.clockData1 = false;
        this.clockData2 = false;
        this.AlarmClock = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{8, 5}));
    }


    public void initWeekData() {
        this.historyDate = new String[7];
        this.historySteps = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 24}));
        this.historyDistan = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 24}));
    }
}


/* Location:              /Users/wooks/Downloads/dex2jar-2.0/vidonn-dex2jar.jar!/com/sz/vidonn2/bluetooth/service/DevDecode.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */