package com.rainbow.kam.bt_scanner.tools.helper;

import java.util.Calendar;

/**
 * Created by kam6512 on 2016-02-22.
 */
public class VidonnHelper {
    public static class DeCodeX6 {


        public static int[][] decode_HistoryRecodeDate(byte[] data, int length) {

            int dataLength = length - 4;

            byte[] crc = CRC_16(data, 2, dataLength);

            if ((crc[0] == data[(length - 1)]) && (crc[1] == data[(length - 2)])) {
                int[][] blockData = new int[7][4];

                byte[][] blockData_Byte = new byte[7][5];

                for (int i = 0; i * 5 + 8 + 1 < length; i++) {
                    int n = i * 5 + 3;
                    blockData_Byte[i] = cutBytes(data, n, 5);
                }

                for (int i = 0; i < blockData_Byte.length; i++) {
                    byte temp = blockData_Byte[i][1];
                    blockData_Byte[i][1] = blockData_Byte[i][2];
                    blockData_Byte[i][1] = temp;
                    blockData[i][0] = blockData_Byte[i][0];
                    blockData[i][1] = bytesToInt2_2Bytes(new byte[]{blockData_Byte[i][2], blockData_Byte[i][1]});
                    blockData[i][2] = blockData_Byte[i][3];
                    blockData[i][3] = blockData_Byte[i][4];
                }

                return blockData;
            }
            return null;
        }


        public static int[][] decode_HistoryRecodeDetail(byte[] data) {
            if (data.length < 67) {
                return null;
            }
            if (data[0] != -91) {
                return null;
            }
            int dataLength = data.length - 4;

            if (data[1] != dataLength) {
                return null;
            }

            byte[] crc = CRC_16(data, 2, dataLength);

            if (crc.length != 2) {
                return null;
            }

            if ((crc[0] == data[(data.length - 1)]) && (crc[1] == data[(data.length - 2)])) {
                int[][] steps = new int[31][2];

                steps[0][0] = cutBytes(data, 4, 1)[0];

                for (int i = 1; i < steps.length; i++) {
                    byte[] stepsData = cutBytes(data, (i + 1) * 2 + 1, 2);

                    steps[i] = separateData(stepsData);

                    if (steps[i][1] == 4095) {
                        steps[i][0] = -1;
                        steps[i][1] = 0;
                    } else if (steps[i][1] == 3840) {
                        steps[i][0] = -1;
                        steps[i][1] = 0;
                    }
                }

                return steps;
            }
            return null;
        }


        public static int[] separateData(byte[] res) {
            int[] targets = new int[2];

            byte temp_Type = res[1];
            byte temp_Step = res[0];

            targets[0] = (temp_Type >>> 4 & 0xF);

            res[1] = (byte) (res[1] & 0xF);

            targets[1] = bytesToInt2_2Bytes(new byte[]{res[1], temp_Step});

            return targets;
        }


        private static byte[] short2bytes(short s) {
            byte[] bytes = new byte[2];
            for (int i = 1; i >= 0; i--) {
                bytes[i] = (byte) (s % 256);
                s = (short) (s >> 8);
            }
            return bytes;
        }


        public static byte[] cutBytes(byte[] data, int start, int length) {
            byte[] data_temp = new byte[length];
            for (int i = 0; i < length; i++) {
                data_temp[i] = data[(start + i)];
            }
            return data_temp;
        }


        public static int bytesToInt2_2Bytes(byte[] src) {
            int value = (src[0] & 0xFF) << 8 | src[1] & 0xFF;
            return value;
        }


        private static byte[] CRC_16(byte[] data, int start, int length) {
            try {
                short crc_result = 0;
                int Poly = 4129;
                for (int i = start; i < start + length; i++) {
                    for (int j = 128; j != 0; j >>= 1) {
                        if ((crc_result & 0x8000) != 0) {
                            crc_result = (short) (crc_result << 1);
                            crc_result = (short) (crc_result ^ Poly);
                        } else {
                            crc_result = (short) (crc_result << 1);
                        }
                        if ((data[i] & j) != 0) {
                            crc_result = (short) (crc_result ^ Poly);
                        }
                    }
                }
                return
                        short2bytes(crc_result);
            } catch (Exception localException) {
                return short2bytes((short) -1);
            }
        }
    }

    public static class OperationX6 {


        public static byte[] readCurrentValue() {
            return writeCode(new byte[]{3, 1}, true);
        }


        public static byte[] readHistoryRecodeDate() {
            return writeCode(new byte[]{4}, true);
        }


        public static byte[] readHistoryRecodeDetail(byte blockID, byte hour) {
            return writeCode(new byte[]{5, blockID, hour}, true);
        }


        public static byte[] readHistoryRecodeStatistics() {
            return writeCode(new byte[]{6}, true);
        }


        public static byte[] readDate_Time() {
            return writeCode(new byte[]{(byte) 33}, true);
        }


        public static byte[] writeDate_Time() {
            Calendar calendar = Calendar.getInstance();
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


        public static byte[] writeCode(byte[] opCode_Data, boolean isRead) {
            byte[] code_data = opCode_Data;
            byte[] crc = short2bytes(CRC_16(code_data));

            byte[] data_Send = new byte[code_data.length + 4];

            if (isRead)
                data_Send[0] = -91;
            else {
                data_Send[0] = 37;
            }

            data_Send[1] = (byte) code_data.length;

            data_Send[(data_Send.length - 2)] = crc[1];
            data_Send[(data_Send.length - 1)] = crc[0];

            for (int i = 2; i < 2 + code_data.length; i++) {
                data_Send[i] = code_data[(i - 2)];
            }

            return data_Send;
        }


        public static byte[] short2bytes(short s) {
            byte[] bytes = new byte[2];
            for (int i = 1; i >= 0; i--) {
                bytes[i] = (byte) (s % 256);
                s = (short) (s >> 8);
            }
            return bytes;
        }


        public static byte[] int2Bytes_2Bytes(int value) {
            byte[] byte_src = new byte[2];
            byte_src[0] = (byte) ((value & 0xFF00) >> 8);
            byte_src[1] = (byte) (value & 0xFF);
            return byte_src;
        }


        private static short CRC_16(byte[] data) {
            try {
                short crc_result = 0;
                int Poly = 4129;
                for (int i = 0; i < data.length; i++) {
                    for (int j = 128; j != 0; j >>= 1) {
                        if ((crc_result & 0x8000) != 0) {
                            crc_result = (short) (crc_result << 1);
                            crc_result = (short) (crc_result ^ Poly);
                        } else {
                            crc_result = (short) (crc_result << 1);
                        }
                        if ((data[i] & j) != 0) {
                            crc_result = (short) (crc_result ^ Poly);
                        }
                    }
                }

                return crc_result;
            } catch (Exception localException) {
            }
            return -1;
        }
    }
}