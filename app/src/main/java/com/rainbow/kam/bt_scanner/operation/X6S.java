package com.rainbow.kam.bt_scanner.operation;

import java.util.Calendar;

/**
 * Created by kam6512 on 2016-03-24.
 */
public class X6S extends Operator {

    private static final byte[] OPCODE_READ_TIME = {33};
    private static final byte OPCODE_WRITE_TIME = 33;
    private static final byte[] OPCODE_READ_CURRENT_VALUE = {3, 1};
    private static final byte[] OPCODE_READ_USER = {32, 1};
    private static final byte[] OPCODE_WRITE_USER = {32, 1};
    private static final byte[] OPCODE_READ_BLOCK_DATE = {4};
    private static final byte OPCODE_READ_HISTORY = 5;

    private static final byte READ = -91;
    private static final byte WRITE = 37;


    @Override public byte[] readTime() {
        return writeCode(OPCODE_READ_TIME, true);
    }


    @Override public byte[] writeTime() {
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
        byte[] year01 = int2Bytes_2Bytes(year);


        return writeCode(new byte[]{OPCODE_WRITE_TIME, year01[1], year01[0], (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second, (byte) dayOfWeek}, false);
    }


    @Override public byte[] readCurrentValue() {
        return writeCode(OPCODE_READ_CURRENT_VALUE, true);
    }


    @Override public byte[] readUserData() {
        return writeCode(OPCODE_READ_USER, true);
    }


    @Override public byte[] writeUserData(int height, int weight, int gender, int age) {
        byte[] physicalInfo = new byte[]{(byte) height, (byte) weight, (byte) gender, (byte) age};
        byte[] writeData = new byte[physicalInfo.length + 2];
        System.arraycopy(OPCODE_WRITE_USER, 0, writeData, 0, OPCODE_WRITE_USER.length);
        System.arraycopy(physicalInfo, 0, writeData, 2, physicalInfo.length);
        return writeCode(writeData, false);
    }


    @Override public byte[] readDateBlockX6S() {
        return writeCode(OPCODE_READ_BLOCK_DATE, true);
    }


    @Override public byte[] readHistoryX6S(int blockID, int hour) {
        return writeCode(new byte[]{OPCODE_READ_HISTORY, (byte) blockID, (byte) hour}, true);
    }


    @Override public byte[] resetX6S(int flag) {
        return writeCode(new byte[]{(byte) 0x40, (byte) flag}, false);
    }


    private byte[] writeCode(byte[] opCode_Data, boolean isRead) {
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


    private byte[] int2Bytes_2Bytes(int value) {
        byte[] byte_src = new byte[2];
        byte_src[0] = (byte) ((value & 0xFF00) >> 8);
        byte_src[1] = (byte) (value & 0xFF);
        return byte_src;
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
