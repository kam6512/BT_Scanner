package com.rainbow.kam.bt_scanner.operation;

import java.util.Calendar;

/**
 * Created by kam6512 on 2016-03-24.
 */
public class X6S {

    public final static String READ_TIME = "READ TIME";
    public final static String WRITE_TIME = "WRITE TIME";
    public final static String READ_CURRENT_VALUE = "READ CURRENT VALUE";
    public final static String READ_USER_INFO = "READ USER INFO";
    public final static String WRITE_USER_INFO = "WRITE USER INFO";
    public final static String READ_RECODED_DATE = "READ RECODED DATE";
    public final static String READ_HISTORY = "READ HISTORY";
    public final static String RESET = "RESET";

    public final static String READ_BATTERY_VALUE = "READ BATTERY VALUE";


    private static final byte[] OPCODE_READ_TIME = {33};
    private static final byte OPCODE_WRITE_TIME = 33;
    private static final byte[] OPCODE_READ_CURRENT_VALUE = {3, 1};
    private static final byte[] OPCODE_READ_USER = {32, 1};
    private static final byte[] OPCODE_WRITE_USER = {32, 1};
    private static final byte[] OPCODE_READ_BLOCK_DATE = {4};
    private static final byte OPCODE_READ_HISTORY = 5;

    private static final byte READ = -91;
    private static final byte WRITE = 37;


     public byte[] readTime() {
        return getBytes(OPCODE_READ_TIME, true);
    }


     public byte[] writeTime() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
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
        byte[] splitYear = new byte[2];
        splitYear[0] = (byte) ((year & 0xFF00) >> 8);
        splitYear[1] = (byte) (year & 0xFF);

        return getBytes(new byte[]{OPCODE_WRITE_TIME, splitYear[1], splitYear[0], (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second, (byte) dayOfWeek}, false);
    }


     public byte[] readCurrentValue() {
        return getBytes(OPCODE_READ_CURRENT_VALUE, true);
    }


     public byte[] readUserData() {
        return getBytes(OPCODE_READ_USER, true);
    }


    
    public byte[] writeUserData(int height, int weight, int gender, int age) {
        byte[] physicalInfo = new byte[]{(byte) height, (byte) weight, (byte) gender, (byte) age};
        byte[] writeData = new byte[physicalInfo.length + 2];
        System.arraycopy(OPCODE_WRITE_USER, 0, writeData, 0, OPCODE_WRITE_USER.length);
        System.arraycopy(physicalInfo, 0, writeData, 2, physicalInfo.length);
        return getBytes(writeData, false);
    }


     public byte[] readDateBlockX6S() {
        return getBytes(OPCODE_READ_BLOCK_DATE, true);
    }


     public byte[] readHistoryX6S(int blockID, int hour) {
        return getBytes(new byte[]{OPCODE_READ_HISTORY, (byte) blockID, (byte) hour}, true);
    }


     public byte[] resetX6S(int flag) {
        return getBytes(new byte[]{(byte) 0x40, (byte) flag}, false);
    }


    private byte[] getBytes(byte[] opCode_Data, boolean isRead) {
        byte[] crc = CRC_16(opCode_Data);

        byte[] writeData = new byte[opCode_Data.length + 4];

        if (isRead)
            writeData[0] = READ;
        else {
            writeData[0] = WRITE;
        }

        byte length = (byte) opCode_Data.length;
        writeData[1] = length;

        writeData[(writeData.length - 2)] = crc[1];
        writeData[(writeData.length - 1)] = crc[0];

        System.arraycopy(opCode_Data, 0, writeData, 2, opCode_Data.length);

        return writeData;
    }


    private byte[] CRC_16(byte[] data) {
        short crc_result = 0;
        byte[] bytes = new byte[2];
        int Poly = 4129;
        for (byte aData : data) {
            for (int j = 128; j != 0; j >>= 1) {
                if ((crc_result & 0x8000) != 0) {
                    crc_result = (short) (crc_result << 1);
                    crc_result = (short) (crc_result ^ Poly);
                } else {
                    crc_result = (short) (crc_result << 1);
                }
                if ((aData & j) != 0) {
                    crc_result = (short) (crc_result ^ Poly);
                }
            }
        }

        for (int i = 1; i >= 0; i--) {
            bytes[i] = (byte) (crc_result % 256);
            crc_result = (short) (crc_result >> 8);
        }
        return bytes;
    }

}
