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

    private int historyDetail_Data_Block_Week_ID = 1;// 1~7
    private int historyDetail_Data_Block_Hour_ID = 0;// 0~23

    private List<Integer> readBlockIndex = new ArrayList<>();

    private OnHistoryListener onHistoryListener;


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
            Log.e(TAG, "onReadHourBlockEnd");
//            onHistoryListener.onReadHourBlockEnd(operationX6.readHistoryRecodeDetail(historyDetail_Data_Block_Week_ID,
//                    historyDetail_Data_Block_Hour_ID));

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

        void onReadHourBlockEnd();

        void onReadAllBlockEnd(List<DateHistoryBlockItem> historyItemList);
    }
}