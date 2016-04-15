package com.rainbow.kam.bt_scanner.tools.helper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.rainbow.kam.bt_scanner.data.item.DateHistoryBlockItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by kam6512 on 2016-02-22.
 */
public class NursingGattHelper {
    private final String TAG = NursingGattHelper.class.getSimpleName();

    private OperationX6 operationX6;
    private HistoryX6 historyX6;
    private OperationPrime operationPrime;

    private OnHistoryListener onHistoryListener;

    private int historyDetail_Data_Block_Week_ID = 1;// 1~7
    private int historyDetail_Data_Block_Hour_ID = 0;// 0~23

    private List<Integer> readBlockIndex = new ArrayList<>();


    public NursingGattHelper() {
        operationPrime = new OperationPrime();
    }


    public NursingGattHelper(NursingPresenter nursingPresenter) {
        operationX6 = new OperationX6();
        historyX6 = new HistoryX6();
        operationPrime = new OperationPrime();
        onHistoryListener = nursingPresenter;
    }


    public OperationX6 getOperationX6() {
        return operationX6;
    }


    public HistoryX6 getHistoryX6() {
        return historyX6;
    }


    public OperationPrime getPrimeHelper() {
        if (operationPrime!=null){
            return new OperationPrime();
        }
        return operationPrime;
    }


    public class OperationPrime {

        public final byte[] readTime = getBytes("8900");
        public final byte[] reset = getBytes("8700");
        public final byte[] clear = getBytes("8800");
        public final byte[] readCurrentValue = getBytes("C60108");
        public final byte[] alertDevice = getBytes("F30101");


        public byte[] getBytesForDateTime() {
            Calendar cal = new GregorianCalendar();

            StringBuilder time = new StringBuilder();
            time.append("C207");
            time.append(String.format("%02x", cal.get(Calendar.YEAR) - 2000));
            time.append(String.format("%02x", cal.get(Calendar.MONTH) + 1));
            time.append(String.format("%02x", cal.get(Calendar.DATE)));
            time.append(String.format("%02x", cal.get(Calendar.HOUR_OF_DAY)));
            time.append(String.format("%02x", cal.get(Calendar.MINUTE)));
            time.append(String.format("%02x", cal.get(Calendar.SECOND)));

            int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (week == 0) {
                week = 7;
            }
            time.append(String.format("%02x", week));

            return getBytes(time.toString());
        }


        public byte[] getBytes(String hex) {
            hex = makeHexClean(hex);
            return parseHexStringToBytes(hex);
        }


        private String makeHexClean(String hex) {
            return hex.toLowerCase(Locale.getDefault()).replaceAll("[^[0-9][a-f]]", "");
        }


        private byte[] parseHexStringToBytes(String hex) {
            byte[] bytes = new byte[(hex.length() / 2) + 1];

            int length = bytes.length;
            int checksum = 0;

            for (int i = 0; i < length - 1; ++i) {
                bytes[i] = decodeValue(hex.substring(i * 2, i * 2 + 2));

                if (i > 1 && i <= length - 2) {
                    if (bytes[i] < 0x00) {
                        checksum ^= bytes[i] + 256;
                    } else {
                        checksum ^= bytes[i];
                    }
                }
            }
            bytes[length - 1] = decodeValue(String.format("%02x", checksum));

            return bytes;
        }


        private byte decodeValue(String value) {
            return Long.decode("0x" + value).byteValue();
        }
    }

    public class OperationX6 {

        public byte[] readDateTime() {
            Log.e(TAG, "READ_DATE_TIME");
            return writeCode(new byte[]{(byte) 33}, true);
        }


        public byte[] readCurrentValue() {
            Log.e(TAG, "READ_CURRENT_VALUE");
            return writeCode(new byte[]{3, 1}, true);
        }


        public byte[] readHistoryRecodeDate() {
            Log.e(TAG, "READ_HISTORY_RECODE_DATE");
            return writeCode(new byte[]{4}, true);
        }


        public byte[] readHistoryRecodeDetail(List<Integer> readBlockIndex) {
            Log.e(TAG, "READ_HISTORY_RECODE_DETAIL");
            NursingGattHelper.this.readBlockIndex = readBlockIndex;
            historyDetail_Data_Block_Week_ID = readBlockIndex.get(0);
            return writeCode(new byte[]{5, (byte) historyDetail_Data_Block_Week_ID, 0}, true);
        }


        public byte[] readHistoryRecodeDetail(int blockID, int hour) {
            Log.e(TAG, "READ_HISTORY_RECODE_DETAIL");
            historyDetail_Data_Block_Week_ID = blockID;
            historyDetail_Data_Block_Hour_ID = hour;
            return writeCode(new byte[]{5, (byte) historyDetail_Data_Block_Week_ID, (byte) historyDetail_Data_Block_Hour_ID}, true);
        }


        public byte[] readPersonalInfo() {
            Log.e(TAG, "READ_PERSONAL_INFO");
            return writeCode(new byte[]{32, 1}, true);
        }


        public byte[] writePersonalInfo(byte[] data) {
            Log.e(TAG, "WRITE_PERSONAL_INFO");
            byte[] newData = new byte[data.length + 2];
            newData[0] = 32;
            newData[1] = 1;
            System.arraycopy(data, 0, newData, 2, data.length);
            return writeCode(newData, false);
        }


        public byte[] resetSystem() {
            return writeCode(new byte[]{64, 1}, true);
        }


        public byte[] resetDefault() {
            return writeCode(new byte[]{64, 2}, true);
        }


        public byte[] resetHistory() {
            return writeCode(new byte[]{64, 3}, true);
        }


        public byte[] writeDateTime() {
            Log.e(TAG, "WRITE_DATE_TIME");
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
            for (int i = 0; i < year01.length; i++) {
                byte data = year01[i];
                Log.e(TAG, "WRITE_DATE_TIME : year01" +
                        " [" + i + "] = " + Integer.toHexString(data));
            }
            return writeCode(new byte[]{(byte) 33, year01[1], year01[0], (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second, (byte) dayOfWeek}, false);

        }


        private byte[] writeCode(byte[] opCode_Data, boolean isRead) {
            byte[] code_data = opCode_Data;
            for (int i = 0; i < code_data.length; i++) {
                byte data = code_data[i];
//                Log.e(TAG, "WRITE_CODE : OPCODE_DATA [" + i + "] = " + Integer.toHexString(data));
            }
            byte[] crc = CRC_16(code_data);

            byte[] data_Send = new byte[code_data.length + 4];

            if (isRead)
                data_Send[0] = -91;
            else {
                data_Send[0] = 37;
            }

            data_Send[1] = (byte) code_data.length;

            data_Send[(data_Send.length - 2)] = crc[1];
            data_Send[(data_Send.length - 1)] = crc[0];

            for (int i = 0; i < data_Send.length; i++) {
                byte data = data_Send[i];
//                Log.e(TAG, "data_Send [" + i + "] = " + Integer.toHexString(data));
            }

            for (int i = 2; i < 2 + code_data.length; i++) {
                data_Send[i] = code_data[(i - 2)];
            }
            for (int i = 0; i < data_Send.length; i++) {
                byte data = data_Send[i];
                Log.e(TAG, "apply code_data in data_Send [" + i + "] = " + Integer.toHexString(data));
            }
            return data_Send;
        }


        private byte[] int2Bytes_2Bytes(int value) {
            byte[] byte_src = new byte[2];
            byte_src[0] = (byte) ((value & 0xFF00) >> 8);
            byte_src[1] = (byte) (value & 0xFF);
            return byte_src;
        }


        private byte[] CRC_16(byte[] data) {
            for (int i = 0; i < data.length; i++) {
                byte temp = data[i];
//                Log.e(TAG, "CRC_16 [" + i + "] = " + temp);
            }
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

    public class HistoryX6 {

        private DeCodeX6 deCodeX6 = new DeCodeX6();

        private int dateBlockIndex = 0;
        private int innerHourBlockIndex = 0;

        private byte[] historyDate_Data = new byte[40];
        private byte[] historyDetail_Data = new byte[67];

        private List<DateHistoryBlockItem> historyItemList = new ArrayList<>(7);


        public void readDateBlock(final BluetoothGattCharacteristic characteristic) {
            byte[] blockData = characteristic.getValue();
            int[][] historyDate_Map;
            if (dateBlockIndex == 0) {
                historyItemList.clear();
                if (blockData.length < 20) {
                    dateBlockIndex = 0;

                    historyDate_Map = deCodeX6.decode_HistoryRecodeDate(blockData, blockData.length);

                    for (int[] aHistoryDate_Map : historyDate_Map) {
                        Log.e(TAG, aHistoryDate_Map[0] + "Block  Date=" + aHistoryDate_Map[1] + "/" + aHistoryDate_Map[2] + "/" + aHistoryDate_Map[3]);
                    }


                    addDayBlock(historyDate_Map);
                } else {
                    dateBlockIndex = 1;
                    System.arraycopy(blockData, 0, historyDate_Data, 0, blockData.length);
                }
            } else if (dateBlockIndex == 1) {
                dateBlockIndex = 0;

                historyItemList.clear();

                int dataLength = 20 + blockData.length;

                System.arraycopy(blockData, 0, historyDate_Data, 20, dataLength - 20);

                historyDate_Map = deCodeX6.decode_HistoryRecodeDate(historyDate_Data, dataLength);

                for (int[] aHistoryDate_Map : historyDate_Map) {
                    Log.e(TAG, aHistoryDate_Map[0] + "Block  Date=" + aHistoryDate_Map[1] + "/" + aHistoryDate_Map[2] + "/" + aHistoryDate_Map[3]);
                }

                addDayBlock(historyDate_Map);
            }
        }


        private void addDayBlock(int[][] historyDate_Map) {
            final String format = "yy년 MM월 dd일";
            final SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.KOREA);
            Calendar calendar = GregorianCalendar.getInstance();
            String today;
            for (int[] historyBlock : historyDate_Map) {
                calendar.clear();
                calendar.set(historyBlock[1], historyBlock[2] - 1, historyBlock[3]);
                today = formatter.format(calendar.getTime());

                DateHistoryBlockItem dateHistoryBlockItem = new DateHistoryBlockItem();
                dateHistoryBlockItem.historyBlockNumber = historyBlock[0];
                dateHistoryBlockItem.historyBlockCalendar = today;
                historyItemList.add(dateHistoryBlockItem);
            }

            onHistoryListener.onReadDayBlockEnd(historyItemList);
        }


        public void readHourBlock(final BluetoothGattCharacteristic characteristic) {


            byte[] detailData = characteristic.getValue();
            int dataLength = detailData.length;

            switch (innerHourBlockIndex) {
                case 0:
                    Log.e(TAG, historyDetail_Data_Block_Week_ID + "Block  "
                            + historyDetail_Data_Block_Hour_ID + " Hour / " + detailData.length + " data.length");

                    if (dataLength < 15) {
                        updateBlockIndex();
                    } else {
                        addHistoryDetail(detailData, dataLength);
                    }
                    break;
                case 1:
                case 2:
                    addHistoryDetail(detailData, dataLength);
                    break;
                case 3:
                    addHistoryDetail(detailData, dataLength);

                    int[][] steps = deCodeX6.decode_HistoryRecodeDetail(historyDetail_Data);

                    for (int i = 0; i < steps.length; i++) {
//                        Log.e(TAG, "=======================================================");
//                        Log.e(TAG, (i * 2 - 1) + "~" + (i * 2) + "min data=" + steps[i][1] + "  type=" + steps[i][0]);
                        historyItemList.get(historyDetail_Data_Block_Week_ID - 1).totalStep += steps[i][1];
//                        Log.e(TAG, historyDetail_Data_Block_Week_ID + " Block  : "
//                                + historyDetail_Data_Block_Hour_ID + " Hour = " + steps[i][1]);

                    }
                    updateBlockIndex();
                    break;
            }
        }


        private void updateBlockIndex() {
            historyDetail_Data_Block_Hour_ID++;
            if (historyDetail_Data_Block_Hour_ID == 24) {
                historyDetail_Data_Block_Hour_ID = 0;

                checkBlockIndex();

            }
            if ((historyDetail_Data_Block_Week_ID > readBlockIndex.get(readBlockIndex.size() - 1))) {
                Log.e(TAG, "Over");

                historyDetail_Data_Block_Week_ID = 1;
                historyDetail_Data_Block_Hour_ID = 0;
                onHistoryListener.onReadAllBlockEnd(historyItemList);
                return;
            }
            onHistoryListener.onReadHourBlockEnd(operationX6.readHistoryRecodeDetail(historyDetail_Data_Block_Week_ID,
                    historyDetail_Data_Block_Hour_ID));

        }


        private void checkBlockIndex() {
            int currentIndex = readBlockIndex.indexOf(historyDetail_Data_Block_Week_ID);
            currentIndex += 1;
            if (currentIndex < readBlockIndex.size()) {
                historyDetail_Data_Block_Week_ID = readBlockIndex.get(currentIndex);
            } else {
                historyDetail_Data_Block_Week_ID = 8;
            }
        }


        private void addHistoryDetail(byte[] detailData, int dataLength) {
            int innerHourBlockCount = 20;
            int indexStart = innerHourBlockIndex * innerHourBlockCount;
            System.arraycopy(detailData, 0, historyDetail_Data, indexStart, dataLength);
            if (innerHourBlockIndex != 3) {
                innerHourBlockIndex++;
            } else {
                innerHourBlockIndex = 0;
            }
        }
    }

    private class DeCodeX6 {

        public int[][] decode_HistoryRecodeDate(byte[] data, int length) {

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


        public int[][] decode_HistoryRecodeDetail(byte[] data) {
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


        private int[] separateData(byte[] res) {
            int[] targets = new int[2];

            byte temp_Type = res[1];
            byte temp_Step = res[0];

            targets[0] = (temp_Type >>> 4 & 0xF);

            res[1] = (byte) (res[1] & 0xF);

            targets[1] = bytesToInt2_2Bytes(new byte[]{res[1], temp_Step});

            return targets;
        }


        private byte[] short2bytes(short s) {
            byte[] bytes = new byte[2];
            for (int i = 1; i >= 0; i--) {
                bytes[i] = (byte) (s % 256);
                s = (short) (s >> 8);
            }
            return bytes;
        }


        private byte[] cutBytes(byte[] data, int start, int length) {
            byte[] data_temp = new byte[length];
            for (int i = 0; i < length; i++) {
                data_temp[i] = data[(start + i)];
            }
            return data_temp;
        }


        private int bytesToInt2_2Bytes(byte[] src) {
            int value = (src[0] & 0xFF) << 8 | src[1] & 0xFF;
            return value;
        }


        private byte[] CRC_16(byte[] data, int start, int length) {
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

    public interface OnHistoryListener {

        void onReadDayBlockEnd(List<DateHistoryBlockItem> historyItemList);

        void onReadHourBlockEnd(byte[] bytes);

        void onReadAllBlockEnd(List<DateHistoryBlockItem> historyItemList);
    }
}