package com.rainbow.kam.bt_scanner.tools.vidonn_sample;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.sz.vidonn2.activity.main.MyAplication;
import com.sz.vidonn2.activity.main.function.MainFunction;
import com.sz.vidonn2.data.UserInfoData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService_Vidonn2
        extends Service {
    public static final String ACTION_DATA_AVAILABLE = "com.vidonn2.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CONNECTED = "com.vidonn2.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.vidonn2.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_ERROR = "com.vidonn2.bluetooth.le.ACTION_GATT_ERROR";
    public static final String ACTION_GATT_ERROR_PUBLIC = "com.vidonn2.bluetooth.le.ACTION_GATT_ERROR_PUBLIC";
    public static final String ACTION_GATT_GO_SETTING_BLUTOOTH = "com.vidonn2.bluetooth.le.ACTION_GATT_GO_SETTING_BLUTOOTH";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.vidonn2.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_X6_ISOK = "com.vidonn2.bluetooth.le.ACTION_GATT_X6_ISOK";
    public static final String ACTION_READ_ALARM_CLOCK = "com.vidonn2.bluetooth.le.ACTION_READ_ALARM_CLOCK";
    public static final String ACTION_READ_BATTERY_LEVEL = "com.vidonn2.bluetooth.le.ACTION_READ_BATTERY_LEVEL";
    public static final String ACTION_READ_BATTERY_LEVEL_Auto = "com.vidonn2.bluetooth.le.ACTION_READ_BATTERY_LEVEL_Auto";
    public static final String ACTION_READ_CURRENT_DATE = "com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_DATE";
    public static final String ACTION_READ_CURRENT_SPORT = "com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_SPORT";
    public static final String ACTION_READ_DEV_HARDVISION = "com.vidonn2.bluetooth.le.ACTION_READ_DEV_HARDVISION";
    public static final String ACTION_READ_DEV_VISION = "com.vidonn2.bluetooth.le.ACTION_READ_DEV_VISION";
    public static final String ACTION_READ_HEART_RATE = "com.vidonn2.bluetooth.le.ACTION_READ_HEART_RATE";
    public static final String ACTION_READ_HISTORY_SPORT = "com.vidonn2.bluetooth.le.ACTION_READ_HISTORY_SPORT";
    public static final String ACTION_READ_MAC_SERIAL = "com.vidonn2.bluetooth.le.ACTION_READ_MAC_SERIAL";
    public static final String ACTION_READ_PERSONAL_INFO = "com.vidonn2.bluetooth.le.ACTION_DATA_PERSONAL_INFO";
    public static final String ACTION_READ_RSSI = "com.vidonn2.bluetooth.le.ACTION_READ_RSSI";
    public static final String ACTION_START_READ_HISTORY_SPORT = "com.vidonn2.bluetooth.le.ACTION_START_READ_HISTORY_SPORT";
    public static final String ACTION_START_READ_HISTORY_SPORT_Immediately = "com.vidonn2.bluetooth.le.ACTION_START_READ_HISTORY_SPORT_Immediately";
    public static final String ACTION_WRITE_NOTIFY_TITLE = "com.vidonn2.bluetooth.le.ACTION_WRITE_NOTIFY_TITLE";
    public static final String ACTION_WRITE_PERSIONALDATA_OTHER = "com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER";
    public static final String ACTION_WRITE_STATUS = "com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS";
    public static final String EXTRA_DATA = "com.vidonn2.bluetooth.le.EXTRA_DATA";
    private static final int OP_X6_R_AlarmClock = 105;
    private static final int OP_X6_R_CurrentValue = 3;
    private static final int OP_X6_R_Date_Time = 107;
    private static final int OP_X6_R_HistoryDetail = 102;
    private static final int OP_X6_R_HistoryMap = 101;
    private static final int OP_X6_R_MAC_SN = 1;
    private static final int OP_X6_R_PersonalInfo_Auto = 103;
    private static final int OP_X6_W_AlarmClock = 106;
    private static final int OP_X6_W_Alert = 4;
    private static final int OP_X6_W_Date_Time = 52;
    private static final int OP_X6_W_Notification_Data = 501;
    private static final int OP_X6_W_Notification_Title = 500;
    private static final int OP_X6_W_PersonalInfo_Auto = 104;
    private static final int OP_X6_W_PersonalInfo_Goal = 15;
    private static final int OP_X6_W_PersonalInfo_HandUpLight = 19;
    private static final int OP_X6_W_PersonalInfo_Language = 17;
    private static final int OP_X6_W_PersonalInfo_Other = 16;
    private static final int OP_X6_W_PersonalInfo_Personal = 11;
    private static final int OP_X6_W_PersonalInfo_ScreenTurnOver = 18;
    private static final int OP_X6_W_PersonalInfo_Sedentary = 12;
    private static final int OP_X6_W_PersonalInfo_Silent = 14;
    private static final int OP_X6_W_PersonalInfo_Sleep = 13;
    private static final int OP_X6_W_PersonalInfo_TempMode = 20;
    private static int OpCode_Current = 0;
    private static int OpCode_Switch = 0;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    private static final String TAG = BluetoothLeService_Vidonn2.class.getSimpleName();
    public static final UUID UUID_HEART_RATE_MEASUREMENT;
    public static Integer dfu_AirUpgradeCount;
    public static int dfu_ConnectTimes;
    public static int dfu_FastCount;
    public static boolean dfu_IsFastUpdate;
    public static boolean dfu_IsFirstDiscovery = false;
    public static boolean dfu_IsReSend;
    public static int dfu_IsReSendCount;
    public static boolean dfu_IsStartUpgrade;
    public static boolean dfu_IsSteadyUpgrade = false;
    public static int dfu_LastPackageLength;
    public static int dfu_PackageCount;
    public static int dfu_ReceiveCount;
    public static int dfu_historyDataCount;
    public static byte[][] dfu_xval;
    public static byte[] dfu_xval_Last;
    public static int errorTimes;
    public static boolean flag_IsMessageContext = false;
    public static int historyDataCount;
    public static boolean historyDataReadMode_Normal;
    public static int[] historyData_Immediately;
    public static int[][] historyDate_Map;
    public static int[][][][] historyDetail_Steps_All;
    public static boolean isAble_historyDetail_Steps_All;
    public static boolean isBluetoothRepairOK;
    private static BluetoothGatt mBluetoothGatt;
    public static int mConnectionState;
    public static int readPersonalInfo_Type;
    public Handler activityHandler;
    private BluetoothGattCharacteristic cha_Heart_Rate;
    private BluetoothGattCharacteristic cha_Info_Battery;
    private BluetoothGattCharacteristic cha_Info_Fireware;
    private BluetoothGattCharacteristic cha_Info_Hardware;
    private BluetoothGattCharacteristic cha_Info_Manufacturer;
    private BluetoothGattCharacteristic cha_Info_ModelType;
    private BluetoothGattCharacteristic cha_Info_Software;
    private BluetoothGattCharacteristic cha_Operation_AirUpgrade;
    private BluetoothGattCharacteristic cha_Operiation_NotificationData;
    private BluetoothGattCharacteristic cha_Operiation_Read;
    private BluetoothGattCharacteristic cha_Operiation_Read_Current;
    private BluetoothGattCharacteristic cha_Operiation_Write;
    private BluetoothGattCharacteristic cha_Write_Image_AirUpgrade;
    Thread checkWritefirmwareImgThread = new Thread() {
        public void run() {
            BluetoothLeService_Vidonn2.this.dfu_Flag_Send_id = BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue();
            for (; ; ) {
                if (!BluetoothLeService_Vidonn2.dfu_IsStartUpgrade) {
                    return;
                }
                try {
                    Thread.sleep(600L);
                } catch (InterruptedException localInterruptedException) {
                    synchronized (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount) {
                        for (; ; ) {
                            if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() != BluetoothLeService_Vidonn2.this.dfu_Flag_Send_id) {
                                break label80;
                            }
                            System.out.println("--数据包重写");
                            BluetoothLeService_Vidonn2.this.sendFirmwareImg();
                            if (MyAplication.isInFirmwareUpdate) {
                                break;
                            }
                            return;
                            localInterruptedException = localInterruptedException;
                            localInterruptedException.printStackTrace();
                        }
                        label80:
                        BluetoothLeService_Vidonn2.this.dfu_Flag_Send_id = BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue();
                    }
                }
            }
        }
    };
    public DevDecode devDecode;
    public DevDecode_X6 devDecode_X6;
    public DevOperation devOperation;
    public DevOperation_X6 devOperation_X6;
    public String[] dev_Data_AWeekData_date;
    public int[][] dev_Data_AWeek_Distance;
    public int[][] dev_Data_AWeek_Steps;
    byte[] dfu_Crc = new byte[4];
    byte[] dfu_Crc_version = new byte[8];
    byte[] dfu_FileCreateTime = new byte[8];
    byte[] dfu_FileNameDescription = new byte[32];
    public byte[] dfu_File_Data;
    public int dfu_Flag_Send_id = 0;
    byte[] dfu_ImgageSize = new byte[4];
    private boolean dfu_SendResetCode = false;
    byte[] dfu_Version = new byte[4];
    private int duf_ProcessTemp = 0;
    public boolean flag_IsMessageComing = false;
    public boolean flag_IsMessageTitle = false;
    private boolean flag_IsNeedCloseGatt = false;
    private boolean flag_mScanning = false;
    private byte[] historyDate_Data = new byte[40];
    private int historyDate_Data_ID = 0;
    private byte[] historyDetail_Data = new byte[67];
    private int historyDetail_Data_Block_Arrys_ID = 0;
    private int historyDetail_Data_Block_Arrys_ID_Fast = 0;
    private int historyDetail_Data_Block_Hour_ID = 0;
    private int historyDetail_Data_Block_Hour_ID_Fast = 0;
    private int historyDetail_Data_Block_ID = 1;
    private int historyDetail_Data_Block_ID_Fast = 1;
    private int historyDetail_Data_ID = 0;
    private int[][] historyNoData = (int[][]) Array.newInstance(Integer.TYPE, new int[]{30, 2});
    private boolean isFirstUpgradeConnect = true;
    public boolean isReadOK = false;
    public boolean isSetNotification_OK = false;
    public boolean isWriteOK = false;
    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    public String mBluetoothDeviceAddress;
    private BluetoothManager mBluetoothManager;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        private void writeOpcodeCheck02() {
            BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                public void run() {
                    if (BluetoothLeService_Vidonn2.this.writeStatus_op2 != 2) {
                        System.out.println("定时检查-写入控制命令02-initialize DFU Parameters");
                        BluetoothLeService_Vidonn2.this.writeStatus_op2 = 1;
                        BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{2});
                        BluetoothLeService_Vidonn2 .1. this.writeOpcodeCheck02();
                    }
                }
            }, 2000L);
        }


        private void writeOpcodeCheck03() {
            BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                public void run() {
                    if (BluetoothLeService_Vidonn2.this.writeStatus_op3 != 2) {
                        System.out.println("定时检查-写入控制命令03-receive firmware image");
                        BluetoothLeService_Vidonn2.this.writeStatus_op3 = 1;
                        BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{3});
                        BluetoothLeService_Vidonn2 .1. this.writeOpcodeCheck03();
                    }
                }
            }, 2000L);
        }


        private void writeOpcodeCheckCRC() {
            BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                public void run() {
                    if (BluetoothLeService_Vidonn2.this.writeStatus_opCRC != 2) {
                        BluetoothLeService_Vidonn2 .1. this.writeOpcodeCheckCRC();
                    }
                }
            }, 2000L);
        }


        public void checkErrorTimes() {
            if (BluetoothLeService_Vidonn2.errorTimes == 5) {
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_ERROR_PUBLIC");
            }
        }


        public void onCharacteristicChanged(BluetoothGatt arg1, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic) {
            for (; ; ) {
                Object localObject;
                int i1;
                try {
                    localObject = paramAnonymousBluetoothGattCharacteristic.getUuid().toString();
                    ???=paramAnonymousBluetoothGattCharacteristic.getValue();
                    System.out.println("Notify-" + BluetoothLeService_Vidonn2.byteToString( ? ??));
                    if (!MyAplication.isInFirmwareUpdate) {
                        if (BluetoothLeService_Vidonn2.OpCode_Current < 100) {
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                        }
                        if (((String) localObject).equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Operiation_Read_Current.toString())) {
                            i1 = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_CurrentValue_Auto( ???)
                            ;
                            ???=new Intent("com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_SPORT");
                            ???.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", i1);
                            BluetoothLeService_Vidonn2.this.sendBroadcast( ???);
                            return;
                        }
                        if (((String) localObject).equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Dev_Info_Battery.toString())) {
                            i1 =???[0];
                            ???=
                            new Intent("com.vidonn2.bluetooth.le.ACTION_READ_BATTERY_LEVEL_Auto");
                            ???.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", i1);
                            BluetoothLeService_Vidonn2.this.sendBroadcast( ???);
                            return;
                        }
                        if (((String) localObject).equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Heart_Rate.toString())) {
                            i1 =???[1];
                            ???=new Intent("com.vidonn2.bluetooth.le.ACTION_READ_HEART_RATE");
                            ???.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", i1 & 0xFF);
                            BluetoothLeService_Vidonn2.this.sendBroadcast( ???);
                            return;
                        }
                        if (!((String) localObject).equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Operiation_Read.toString())) {
                            continue;
                        }
                        System.out.println("OpCode_Current=" + BluetoothLeService_Vidonn2.OpCode_Current);
                        if (MyAplication.isAppStart) {
                            BluetoothLeService_Vidonn2.OpCode_Switch =???[2]&0xFF;
                        }
                    }
                    switch (BluetoothLeService_Vidonn2.OpCode_Current) {
                        case 101:
                            switch (BluetoothLeService_Vidonn2.OpCode_Switch) {
                                case 2:
                                    if (BluetoothLeService_Vidonn2.OpCode_Current < 500) {
                                        BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                    }
                                    if (BluetoothLeService_Vidonn2.this.flag_IsMessageComing) {
                                        if (BluetoothLeService_Vidonn2.OpCode_Current == 0) {
                                            BluetoothLeService_Vidonn2.this.devOperation_X6.writerNotification(BluetoothLeService_Vidonn2.this.message_EventFlag, BluetoothLeService_Vidonn2.this.message_CategoryID);
                                            BluetoothLeService_Vidonn2.OpCode_Current = 500;
                                            return;
                                            BluetoothLeService_Vidonn2.OpCode_Switch = 101;
                                        }
                                    }
                                    break;
                            }
                            break;
                        case 102:
                            BluetoothLeService_Vidonn2.OpCode_Switch = 102;
                            continue;
                            paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_MAC_SN( ???);
                            if (paramAnonymousBluetoothGattCharacteristic != null) {
                                i1 = paramAnonymousBluetoothGattCharacteristic.length;
                            }
                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                            continue;
                            paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_CurrentValue( ???);
                            if ((paramAnonymousBluetoothGattCharacteristic != null) && (paramAnonymousBluetoothGattCharacteristic.length == 3)) {
                                localObject = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_SPORT");
                                ((Intent) localObject).putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramAnonymousBluetoothGattCharacteristic[0]);
                                BluetoothLeService_Vidonn2.this.sendBroadcast((Intent) localObject);
                            }
                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                            continue;
                            if (( ???[0]&0xFF)==165)
                        {
                            paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_PersonalInfo( ???,???[
                            3]);
                            if (paramAnonymousBluetoothGattCharacteristic != null) {
                                break label4516;
                            }
                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                            continue;
                            System.out.println("解析个人信息  身高：" + paramAnonymousBluetoothGattCharacteristic[0] + " 体重:" + paramAnonymousBluetoothGattCharacteristic[1] + " 性别：" + paramAnonymousBluetoothGattCharacteristic[2] + " 年龄:" + paramAnonymousBluetoothGattCharacteristic[3]);
                            BluetoothLeService_Vidonn2.readPersonalInfo_Type = 2;
                            BluetoothLeService_Vidonn2.this.devOperation_X6.readPersonalInfo((byte) 2);
                            continue;
                            System.out.println("解析久坐提醒:" + paramAnonymousBluetoothGattCharacteristic[0] + "秒");
                            BluetoothLeService_Vidonn2.readPersonalInfo_Type = 3;
                            MyAplication.userInfo.setSedentary(paramAnonymousBluetoothGattCharacteristic[0] / 60);
                            BluetoothLeService_Vidonn2.this.devOperation_X6.readPersonalInfo((byte) 3);
                            continue;
                            System.out.println("解析目标步数:" + paramAnonymousBluetoothGattCharacteristic[0]);
                            BluetoothLeService_Vidonn2.readPersonalInfo_Type = 4;
                            MyAplication.userInfo.setScoreTaget(paramAnonymousBluetoothGattCharacteristic[0]);
                            BluetoothLeService_Vidonn2.this.devOperation_X6.readPersonalInfo((byte) 4);
                            continue;
                            System.out.println("解析睡眠时间=" + paramAnonymousBluetoothGattCharacteristic[0] + ":" + paramAnonymousBluetoothGattCharacteristic[1] + "~" + paramAnonymousBluetoothGattCharacteristic[2] + ":" + paramAnonymousBluetoothGattCharacteristic[3]);
                            BluetoothLeService_Vidonn2.readPersonalInfo_Type = 6;
                            MyAplication.sleepStartH = paramAnonymousBluetoothGattCharacteristic[0];
                            MyAplication.sleepStartM = paramAnonymousBluetoothGattCharacteristic[1];
                            MyAplication.sleepStopH = paramAnonymousBluetoothGattCharacteristic[2];
                            MyAplication.sleepStopM = paramAnonymousBluetoothGattCharacteristic[3];
                            continue;
                            System.out.println("Disconnect reminder=" + paramAnonymousBluetoothGattCharacteristic[0] + "  Time format=" + paramAnonymousBluetoothGattCharacteristic[1] + " ＵＩ Type＝" + paramAnonymousBluetoothGattCharacteristic[2]);
                            continue;
                            System.out.println("解析勿扰时间  开启=" + paramAnonymousBluetoothGattCharacteristic[0] + "  " + paramAnonymousBluetoothGattCharacteristic[1] + ":" + paramAnonymousBluetoothGattCharacteristic[2] + "~" + paramAnonymousBluetoothGattCharacteristic[3] + ":" + paramAnonymousBluetoothGattCharacteristic[4]);
                            BluetoothLeService_Vidonn2.readPersonalInfo_Type = 0;
                            MyAplication.silentEnable = paramAnonymousBluetoothGattCharacteristic[0];
                            MyAplication.silentStartH = paramAnonymousBluetoothGattCharacteristic[1];
                            MyAplication.silentStartM = paramAnonymousBluetoothGattCharacteristic[2];
                            MyAplication.silentStopH = paramAnonymousBluetoothGattCharacteristic[3];
                            MyAplication.silentStopM = paramAnonymousBluetoothGattCharacteristic[4];
                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_DATA_PERSONAL_INFO");
                            continue;
                            System.out.println("Language Code:" + paramAnonymousBluetoothGattCharacteristic[0] + "\n 0 EN/CN/JP,1 Ko,2 Many languages,3 ISO8859");
                            continue;
                            System.out.println("Screen flip:" + paramAnonymousBluetoothGattCharacteristic[0]);
                            continue;
                            System.out.println("Auto bright screen:" + paramAnonymousBluetoothGattCharacteristic[0] + "\n0 Closed,2 Auto(Portrait),3 Auto(Horizontal)");
                            continue;
                            System.out.println("Temp Mode:" + paramAnonymousBluetoothGattCharacteristic[0] + "\n0 Opened,1 Closed");
                            continue;
                        }
                        if (!BluetoothLeService_Vidonn2.byteToString( ???).replace(" ", "").contains("250220")){
                            continue;
                        }
                        switch (???[3]&0xFF)
                        {
                            case 1:
                                if (BluetoothLeService_Vidonn2.OpCode_Current == 104) {
                                    BluetoothLeService_Vidonn2.this.writePersonalInfoType = 2;
                                    paramAnonymousBluetoothGattCharacteristic = DevDecode_X6.switchBytes(DevOperation_X6.int2Bytes_2Bytes(MyAplication.userInfo.getSedentary() * 60));
                                    BluetoothLeService_Vidonn2.this.devOperation_X6.writePersonalInfo((byte) 2, paramAnonymousBluetoothGattCharacteristic);
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                break;
                            case 2:
                                BluetoothLeService_Vidonn2.this.writePersonalInfoType = 3;
                                BluetoothLeService_Vidonn2.OpCode_Current = 104;
                                paramAnonymousBluetoothGattCharacteristic = DevDecode_X6.switchBytes(DevOperation_X6.int2Bytes_4Bytes(BluetoothLeService_Vidonn2.this.personalInfo_Goal));
                                BluetoothLeService_Vidonn2.this.devOperation_X6.writePersonalInfo((byte) 3, paramAnonymousBluetoothGattCharacteristic);
                                break;
                            case 3:
                                BluetoothLeService_Vidonn2.this.writePersonalInfoType = 4;
                                BluetoothLeService_Vidonn2.OpCode_Current = 104;
                                BluetoothLeService_Vidonn2.this.devOperation_X6.writePersonalInfo((byte) 4, new byte[]{(byte) MyAplication.sleepStartH, (byte) MyAplication.sleepStartM, (byte) MyAplication.sleepStopH, (byte) MyAplication.sleepStopM});
                                break;
                            case 4:
                                if (BluetoothLeService_Vidonn2.byteToString( ???).replace(" ", "").contains("250220"))
                            {
                                BluetoothLeService_Vidonn2.OpCode_Current = 14;
                                BluetoothLeService_Vidonn2.this.writePersonalData_Silent((byte) MyAplication.silentEnable, MyAplication.silentStartH, MyAplication.silentStartM, MyAplication.silentStopH, MyAplication.silentStopM);
                                continue;
                            }
                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                            break;
                            case 5:
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                break;
                            case 6:
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                                break;
                            case 7:
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                break;
                            case 8:
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                break;
                            case 9:
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                break;
                            case 11:
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                continue;
                                if (( ???[0]&0xFF)!=165){
                                continue;
                            }
                            paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_Date_Time( ???);
                            i1 = MyAplication.currentTime[0];
                            i2 = MyAplication.currentTime[1];
                            i3 = MyAplication.currentTime[2];
                            int i4 = MyAplication.currentTime[3];
                            int i5 = MyAplication.currentTime[4];
                            if ((paramAnonymousBluetoothGattCharacteristic[0] == i1) && (paramAnonymousBluetoothGattCharacteristic[1] == i2) && (paramAnonymousBluetoothGattCharacteristic[2] == i3) && (paramAnonymousBluetoothGattCharacteristic[3] == i4) && (paramAnonymousBluetoothGattCharacteristic[4] == i5)) {
                                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_DATE", BluetoothLeService_Vidonn2.this.isWriteOK);
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                continue;
                            }
                            BluetoothLeService_Vidonn2.this.writeCurrentDate();
                            continue;
                            if (( ???[0]&0xFF)==165)
                            {
                                BluetoothLeService_Vidonn2.this.readAlarmClockData[BluetoothLeService_Vidonn2.this.readAlarmClock_ID] = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_AlarmClock( ???)
                                ;
                                if (BluetoothLeService_Vidonn2.this.readAlarmClock_ID < 7) {
                                    paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this;
                                    paramAnonymousBluetoothGattCharacteristic.readAlarmClock_ID += 1;
                                    BluetoothLeService_Vidonn2.OpCode_Current = 105;
                                    BluetoothLeService_Vidonn2.this.devOperation_X6.readAlarmClock((byte) BluetoothLeService_Vidonn2.this.readAlarmClock_ID);
                                    continue;
                                }
                                MyAplication.dev_Data_AlarmClock = BluetoothLeService_Vidonn2.this.switchAlarmClockData_X6(BluetoothLeService_Vidonn2.this.readAlarmClockData);
                                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_ALARM_CLOCK", BluetoothLeService_Vidonn2.this.isWriteOK);
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                continue;
                            }
                            int i;
                            int j;
                            int k;
                            int m;
                            if (BluetoothLeService_Vidonn2.byteToString( ???).replace(" ", "").contains("250222"))
                            {
                                System.out.println("写入闹钟成功");
                                if (BluetoothLeService_Vidonn2.this.writeAlarmClock_ID < 7) {
                                    paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this;
                                    paramAnonymousBluetoothGattCharacteristic.writeAlarmClock_ID += 1;
                                    i3 = MyAplication.dev_Data_AlarmClock[BluetoothLeService_Vidonn2.this.writeAlarmClock_ID][3];
                                    i4 = MyAplication.dev_Data_AlarmClock[BluetoothLeService_Vidonn2.this.writeAlarmClock_ID][4];
                                    i2 = MyAplication.dev_Data_AlarmClock[BluetoothLeService_Vidonn2.this.writeAlarmClock_ID][0] - 1;
                                    i1 = i2;
                                    if (i2 != 0) {
                                        i1 = i2;
                                        if (i2 != 1) {
                                            i1 = 0;
                                        }
                                    }
                                    i = (byte) BluetoothLeService_Vidonn2.this.writeAlarmClock_ID;
                                    j = (byte) i1;
                                    paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.weekTransBackTo(MyAplication.dev_Data_AlarmClock[BluetoothLeService_Vidonn2.this.writeAlarmClock_ID][2]);
                                    k = (byte) DevOperation_X6.weekTransform(MyAplication.dev_Data_AlarmClock[BluetoothLeService_Vidonn2.this.writeAlarmClock_ID][1], paramAnonymousBluetoothGattCharacteristic[0], paramAnonymousBluetoothGattCharacteristic[1], paramAnonymousBluetoothGattCharacteristic[2], paramAnonymousBluetoothGattCharacteristic[3], paramAnonymousBluetoothGattCharacteristic[4], paramAnonymousBluetoothGattCharacteristic[5], paramAnonymousBluetoothGattCharacteristic[6]);
                                    m = (byte) i3;
                                    int n = (byte) i4;
                                    BluetoothLeService_Vidonn2.OpCode_Current = 106;
                                    BluetoothLeService_Vidonn2.this.devOperation_X6.writeAlarmClock(new byte[]{i, j, k, m, n, 0});
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.sendBroadcast(new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_PERSIONALDATA_OTHER"));
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                continue;
                            }
                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                            continue;
                            System.out.println("OP_X6_R_HistoryMap=");
                            if (BluetoothLeService_Vidonn2.this.historyDate_Data_ID == 0) {
                                if (???[1]<17)
                                {
                                    BluetoothLeService_Vidonn2.this.historyDate_Data_ID = 0;
                                    BluetoothLeService_Vidonn2.historyDate_Map = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_HistoryRecodeDate( ???,???.
                                    length);
                                    BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                    BluetoothLeService_Vidonn2.historyDate_Map = BluetoothLeService_Vidonn2.this.rankHistoryDateMap(BluetoothLeService_Vidonn2.historyDate_Map);
                                    BluetoothLeService_Vidonn2.this.readHistoryData_Start_X6();
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDate_Data_ID = 1;
                                i1 = 0;
                                if (i1 >= ???.length){
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDate_Data[i1] =???[i1];
                                i1 += 1;
                                continue;
                            }
                            if (BluetoothLeService_Vidonn2.this.historyDate_Data_ID != 1) {
                                continue;
                            }
                            BluetoothLeService_Vidonn2.this.historyDate_Data_ID = 0;
                            i2 =???.length + 20;
                            i1 = 20;
                            if (i1 >= i2) {
                                BluetoothLeService_Vidonn2.historyDate_Map = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_HistoryRecodeDate(BluetoothLeService_Vidonn2.this.historyDate_Data, i2);
                                BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                BluetoothLeService_Vidonn2.historyDate_Map = BluetoothLeService_Vidonn2.this.rankHistoryDateMap(BluetoothLeService_Vidonn2.historyDate_Map);
                                BluetoothLeService_Vidonn2.this.readHistoryData_Start_X6();
                                continue;
                            }
                            BluetoothLeService_Vidonn2.this.historyDate_Data[i1] =???[(i1 - 20)];
                            i1 += 1;
                            continue;
                            System.out.println("历史数据=" + BluetoothLeService_Vidonn2.this.historyDetail_Data_ID);
                            if (BluetoothLeService_Vidonn2.this.historyDetail_Data_ID == 0) {
                                System.out.println(BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_ID + "区块--------------" + BluetoothLeService_Vidonn2.historyDate_Map[BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID][3] + "日 小时=" + BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID);
                                if (???.length< 15)
                                {
                                    System.out.println(BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID + "小时——无数据");
                                    paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this;
                                    paramAnonymousBluetoothGattCharacteristic.historyDetail_Data_Block_Hour_ID += 1;
                                    if ((BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID == 6) && (MyAplication.currentTime[3] < BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID)) {
                                        BluetoothLeService_Vidonn2.historyDataCount = 0;
                                        System.out.println("大于当前时间-----------结束");
                                        i1 = BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID;
                                        if (i1 >= 24) {
                                            BluetoothLeService_Vidonn2.this.switchHistoryDataToX5_X6();
                                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                            continue;
                                        }
                                        BluetoothLeService_Vidonn2.historyDetail_Steps_All[BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID][i1] = BluetoothLeService_Vidonn2.this.historyNoData;
                                        i1 += 1;
                                        continue;
                                    }
                                    if (BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID == 24) {
                                        BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID = 0;
                                        paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this;
                                        paramAnonymousBluetoothGattCharacteristic.historyDetail_Data_Block_Arrys_ID += 1;
                                        if (BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID > 6) {
                                            BluetoothLeService_Vidonn2.historyDataCount = 0;
                                            System.out.println("-----------结束");
                                            BluetoothLeService_Vidonn2.this.switchHistoryDataToX5_X6();
                                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                            continue;
                                        }
                                        BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_ID = BluetoothLeService_Vidonn2.historyDate_Map[BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID][0];
                                    }
                                    if (!BluetoothLeService_Vidonn2.historyDataReadMode_Normal) {
                                        continue;
                                    }
                                    BluetoothLeService_Vidonn2.this.devOperation_X6.readHistoryRecodeDatail((byte) BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_ID, (byte) BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID);
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDetail_Data_ID = 1;
                                i1 = 0;
                                if (i1 >= ???.length){
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDetail_Data[i1] =???[i1];
                                i1 += 1;
                                continue;
                            }
                            if (BluetoothLeService_Vidonn2.this.historyDetail_Data_ID == 1) {
                                BluetoothLeService_Vidonn2.this.historyDetail_Data_ID = 2;
                                i1 = 20;
                                if (i1 >= ???.length + 20){
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDetail_Data[i1] =???[
                                (i1 - 20)];
                                i1 += 1;
                                continue;
                            }
                            if (BluetoothLeService_Vidonn2.this.historyDetail_Data_ID == 2) {
                                BluetoothLeService_Vidonn2.this.historyDetail_Data_ID = 3;
                                i1 = 40;
                                if (i1 >= ???.length + 40){
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDetail_Data[i1] =???[
                                (i1 - 40)];
                                i1 += 1;
                                continue;
                            }
                            if (BluetoothLeService_Vidonn2.this.historyDetail_Data_ID != 3) {
                                continue;
                            }
                            BluetoothLeService_Vidonn2.this.historyDetail_Data_ID = 0;
                            i1 = 60;
                            if (i1 >= ???.length + 60)
                            {
                                paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.devDecode_X6.decode_HistoryRecodeDatail(BluetoothLeService_Vidonn2.this.historyDetail_Data);
                                BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID = paramAnonymousBluetoothGattCharacteristic[0][0];
                                i1 = paramAnonymousBluetoothGattCharacteristic.length;
                                localObject = (int[][]) Array.newInstance(Integer.TYPE, new int[]{i1 - 1, 2});
                                i2 = 0;
                                i1 = 1;
                                if (i1 < paramAnonymousBluetoothGattCharacteristic.length) {
                                    break label4586;
                                }
                                if (i2 > 0) {
                                    BluetoothLeService_Vidonn2.historyData_Immediately[0] = BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID;
                                    BluetoothLeService_Vidonn2.historyData_Immediately[1] = BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID;
                                    BluetoothLeService_Vidonn2.historyData_Immediately[2] = i2;
                                    BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_START_READ_HISTORY_SPORT_Immediately");
                                }
                                BluetoothLeService_Vidonn2.historyDetail_Steps_All[BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID][BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID] = localObject;
                                paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this;
                                paramAnonymousBluetoothGattCharacteristic.historyDetail_Data_Block_Hour_ID += 1;
                                if ((BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID != 6) || (MyAplication.currentTime[3] >= BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID)) {
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.historyDataCount = 0;
                                System.out.println("大于当前时间-----------结束");
                                i1 = BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID;
                                if (i1 >= 24) {
                                    BluetoothLeService_Vidonn2.this.switchHistoryDataToX5_X6();
                                    BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                }
                            }
                            else
                            {
                                BluetoothLeService_Vidonn2.this.historyDetail_Data[i1] =???[
                                (i1 - 60)];
                                i1 += 1;
                                continue;
                            }
                            BluetoothLeService_Vidonn2.historyDetail_Steps_All[BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID][i1] = BluetoothLeService_Vidonn2.this.historyNoData;
                            i1 += 1;
                            continue;
                            if (BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID == 24) {
                                BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID = 0;
                                paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this;
                                paramAnonymousBluetoothGattCharacteristic.historyDetail_Data_Block_Arrys_ID += 1;
                                if (BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID > 6) {
                                    BluetoothLeService_Vidonn2.historyDataCount = 0;
                                    System.out.println("-----------结束");
                                    BluetoothLeService_Vidonn2.this.switchHistoryDataToX5_X6();
                                    BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                    continue;
                                }
                                BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_ID = BluetoothLeService_Vidonn2.historyDate_Map[BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Arrys_ID][0];
                            }
                            if (!BluetoothLeService_Vidonn2.historyDataReadMode_Normal) {
                                continue;
                            }
                            BluetoothLeService_Vidonn2.this.devOperation_X6.readHistoryRecodeDatail((byte) BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_ID, (byte) BluetoothLeService_Vidonn2.this.historyDetail_Data_Block_Hour_ID);
                            continue;
                            if (BluetoothLeService_Vidonn2.OpCode_Current == 500) {
                                if (MyAplication.isRecodeNotificationLog) {
                                    MainFunction.writeLogtoFile(0, "Notification:" + BluetoothLeService_Vidonn2.byteToString( ? ??))
                                    ;
                                }
                                paramAnonymousBluetoothGattCharacteristic = new Intent("com.vidonn2.bluetooth.le.ACTION_WRITE_NOTIFY_TITLE");
                                paramAnonymousBluetoothGattCharacteristic.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", ???)
                                ;
                                BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGattCharacteristic);
                                if (MyAplication.isAppStart) {
                                    BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            BluetoothLeService_Vidonn2.this.flag_IsMessageComing = false;
                                            BluetoothLeService_Vidonn2.OpCode_Current = 0;
                                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                                        }
                                    }, 2000L);
                                    return;
                                    paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.byteToString( ???)
                                    ;
                                    if (((String) localObject).equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Operation_AirUpgrade.toString())) {
                                        if (???[0]==17)
                                        {
                                            if (BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade) {
                                                if (BluetoothLeService_Vidonn2.dfu_IsStartUpgrade) {
                                                    if (???.length > 4)
                                                    {
                                                        i1 = DevDecode_X6.bytesToInt2_2Bytes(new byte[]{ ? ??[
                                                        2],???[1]})/20;
                                                        System.out.println("收数据包 count=" + i1);
                                                    }
                                                    synchronized (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount) {
                                                        BluetoothLeService_Vidonn2.dfu_AirUpgradeCount = Integer.valueOf(i1);
                                                        i1 = BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() * 100 / BluetoothLeService_Vidonn2.dfu_PackageCount;
                                                        if (i1 - BluetoothLeService_Vidonn2.this.duf_ProcessTemp > 0) {
                                                            BluetoothLeService_Vidonn2.this.duf_ProcessTemp = i1;
                                                            BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2001, BluetoothLeService_Vidonn2.dfu_AirUpgradeCount).sendToTarget();
                                                        }
                                                        if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() < BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                                            System.out.println("write image count=" + BluetoothLeService_Vidonn2.dfu_AirUpgradeCount);
                                                            BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval[BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue()]);
                                                            return;
                                                        }
                                                    }
                                                    if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() == BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                                        System.out.println("write image count=" + BluetoothLeService_Vidonn2.dfu_AirUpgradeCount);
                                                        BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval_Last);
                                                        return;
                                                    }
                                                    System.out.println("--write image completed");
                                                    BluetoothLeService_Vidonn2.dfu_IsStartUpgrade = false;
                                                }
                                            } else if (???.length > 4){
                                            if (DevDecode_X6.bytesToInt2_2Bytes(new byte[]{ ? ??[
                                            2],???[1]})/
                                            20 % BluetoothLeService_Vidonn2.dfu_FastCount != 0)
                                            {
                                                System.out.println("--收数据包 不正确复位重新升级");
                                                BluetoothLeService_Vidonn2.dfu_AirUpgradeCount = Integer.valueOf(0);
                                                BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade = true;
                                                BluetoothLeService_Vidonn2.dfu_IsStartUpgrade = false;
                                                BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2001, BluetoothLeService_Vidonn2.dfu_AirUpgradeCount).sendToTarget();
                                                BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{6});
                                                    }
                                                }, 2000L);
                                                BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        BluetoothLeService_Vidonn2.this.close();
                                                    }
                                                }, 5000L);
                                            }
                                        }
                                        }
                                        else
                                        {
                                            if (paramAnonymousBluetoothGattCharacteristic.contains("10 01 ")) {
                                                if (paramAnonymousBluetoothGattCharacteristic.contains("10 01 01 ")) {
                                                    System.out.println("写入控制命令02-initialize DFU Parameters");
                                                    BluetoothLeService_Vidonn2.this.writeStatus_op2 = 1;
                                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{2});
                                                    writeOpcodeCheck02();
                                                    return;
                                                }
                                                System.out.println("重写-写入控制命令02-initialize DFU Parameters");
                                                continue;
                                            }
                                            if (paramAnonymousBluetoothGattCharacteristic.contains("10 02 ")) {
                                                if (paramAnonymousBluetoothGattCharacteristic.contains("10 02 01 ")) {
                                                    System.out.println("写入控制命令03-receive firmware image");
                                                    BluetoothLeService_Vidonn2.this.writeStatus_opCRC = 2;
                                                    BluetoothLeService_Vidonn2.this.writeStatus_op3 = 1;
                                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{3});
                                                    writeOpcodeCheck03();
                                                    return;
                                                }
                                                System.out.println("错误重写-写入控制命令03-receive firmware image");
                                                continue;
                                            }
                                            if (paramAnonymousBluetoothGattCharacteristic.contains("10 03 01 ")) {
                                                System.out.println("写入控制命令04-vlidate firmware");
                                                synchronized (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount) {
                                                    BluetoothLeService_Vidonn2.this.dfu_Flag_Send_id = (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 1);
                                                    BluetoothLeService_Vidonn2.dfu_IsStartUpgrade = false;
                                                    BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                                        public void run() {
                                                            BluetoothLeService_Vidonn2.this.writeStatus_op4 = 1;
                                                            BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{4});
                                                        }
                                                    }, 1000L);
                                                    return;
                                                }
                                            }
                                            if (paramAnonymousBluetoothGattCharacteristic.contains("10 04 01 ")) {
                                                System.out.println("写入控制命令05-复位");
                                                BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{5});
                                                BluetoothLeService_Vidonn2.this.writeStatus_op1 = 0;
                                                BluetoothLeService_Vidonn2.this.writeStatus_op8 = 0;
                                                BluetoothLeService_Vidonn2.this.writeStatus_opSize = 0;
                                                BluetoothLeService_Vidonn2.this.writeStatus_op2 = 0;
                                                BluetoothLeService_Vidonn2.this.writeStatus_opCRC = 0;
                                                BluetoothLeService_Vidonn2.this.writeStatus_op3 = 0;
                                                BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        MyAplication.isInFirmwareUpdate = false;
                                                        BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2003).sendToTarget();
                                                        BluetoothLeService_Vidonn2.this.close();
                                                    }
                                                }, 2000L);
                                                return;
                                            }
                                            if ((paramAnonymousBluetoothGattCharacteristic.contains("10 07 01 ")) && ( ???.
                                            length == 7))
                                            {
                                                i =???[6];
                                                j =???[5];
                                                k =???[4];
                                                m =???[3];
                                                i1 = BluetoothLeService_Vidonn2.this.toInt(new byte[]{i, j, k, m}) / 20;
                                                System.out.println("已接受数据包=" + i1 + "    已发送数据包=" + BluetoothLeService_Vidonn2.dfu_AirUpgradeCount);
                                                if ((i1 == BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 0) || (i1 == BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 1)) {
                                                    if (i1 == BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 1) {
                                                        BluetoothLeService_Vidonn2.dfu_AirUpgradeCount = Integer.valueOf(BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 1);
                                                    }
                                                    BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                                        public void run() {
                                                            BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval[BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue()]);
                                                            BluetoothLeService_Vidonn2.dfu_IsReSend = false;
                                                            int i = BluetoothLeService_Vidonn2.dfu_IsReSendCount;
                                                            for (; ; ) {
                                                                if ((i >= BluetoothLeService_Vidonn2.dfu_FastCount) || (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() >= BluetoothLeService_Vidonn2.dfu_PackageCount)) {
                                                                    return;
                                                                }
                                                                BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2001, BluetoothLeService_Vidonn2.dfu_AirUpgradeCount).sendToTarget();
                                                                BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval[BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue()]);
                                                                BluetoothLeService_Vidonn2.dfu_AirUpgradeCount = Integer.valueOf(BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 1);
                                                                if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() == BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                                                    BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2001, BluetoothLeService_Vidonn2.dfu_AirUpgradeCount).sendToTarget();
                                                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval_Last);
                                                                }
                                                                i += 1;
                                                            }
                                                        }
                                                    }, 500L);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            return;
                        }
                    }
                } catch (Exception???)
                {
                    return;
                }
                continue;
                continue;
                label4516:
                switch (???[3]&0xFF)
                {
                }
                continue;
                continue;
                label4586:
                localObject[(i1 - 1)][0] = paramAnonymousBluetoothGattCharacteristic[i1][0];
                localObject[(i1 - 1)][1] = paramAnonymousBluetoothGattCharacteristic[i1][1];
                int i3 = i2;
                if (localObject[(i1 - 1)][0] != 0) {
                    i3 = i2 + localObject[(i1 - 1)][1];
                }
                i1 += 1;
                int i2 = i3;
            }
        }


        public void onCharacteristicRead(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic, int paramAnonymousInt) {
            paramAnonymousBluetoothGatt = paramAnonymousBluetoothGattCharacteristic.getUuid().toString();
            byte[] arrayOfByte = paramAnonymousBluetoothGattCharacteristic.getValue();
            if (MyAplication.DevType == 0) {
                if (paramAnonymousInt == 0) {
                    BluetoothLeService_Vidonn2.this.isReadOK = true;
                    BluetoothLeService_Vidonn2.isBluetoothRepairOK = true;
                    if (paramAnonymousBluetoothGatt != null) {
                    }
                }
            }
            do {
                do {
                    do {
                        return;
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.movementData_current_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.CurrentSportData_Decode(arrayOfByte);
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_SPORT");
                            return;
                        }
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.movementData_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.WeekData_Decode(arrayOfByte);
                            if (BluetoothLeService_Vidonn2.historyDataCount < 56) {
                                if (MyAplication.isMessageCome) {
                                    BluetoothLeService_Vidonn2.this.writeSpecialNotice(2);
                                    MyAplication.isMessageCome = false;
                                }
                                if (MyAplication.isTelephoneCome) {
                                    BluetoothLeService_Vidonn2.this.writeSpecialNotice(1);
                                    MyAplication.isTelephoneCome = false;
                                }
                                BluetoothLeService_Vidonn2.historyDataCount += 1;
                                BluetoothLeService_Vidonn2.this.readHistoryValue();
                                return;
                            }
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_HISTORY_SPORT");
                            return;
                        }
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.date_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.Date_Decode(arrayOfByte);
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_CURRENT_DATE");
                            return;
                        }
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.personal_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.Data_Personal_Decode(arrayOfByte);
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_DATA_PERSONAL_INFO");
                            return;
                        }
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.clock_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.Data_AlarmClock_Decode(arrayOfByte);
                            if (!BluetoothLeService_Vidonn2.this.devDecode.checkAlarmClockData()) {
                                BluetoothLeService_Vidonn2.this.readAlarmClockData();
                                return;
                            }
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_ALARM_CLOCK");
                            return;
                        }
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.macaddress_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.MAC_Serial_Decode(arrayOfByte);
                            BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_MAC_SERIAL");
                            return;
                        }
                        if (paramAnonymousBluetoothGatt.equals(DevOperation.device_versions_UUID.toString())) {
                            BluetoothLeService_Vidonn2.this.devDecode.VersionCode_Decode(arrayOfByte);
                            paramAnonymousBluetoothGatt = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_DEV_VISION");
                            paramAnonymousBluetoothGatt.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", BluetoothLeService_Vidonn2.byteToString(paramAnonymousBluetoothGattCharacteristic.getValue()));
                            BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGatt);
                            return;
                        }
                    }
                    while (!paramAnonymousBluetoothGatt.equals(DevOperation.battery_level_UUID.toString()));
                    paramAnonymousInt = arrayOfByte[0];
                    BluetoothLeService_Vidonn2.this.devDecode.Battery_Decode(arrayOfByte);
                    paramAnonymousBluetoothGatt = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_BATTERY_LEVEL");
                    paramAnonymousBluetoothGatt.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramAnonymousInt);
                    BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGatt);
                    return;
                    BluetoothLeService_Vidonn2.this.isReadOK = false;
                    BluetoothLeService_Vidonn2.errorTimes += 1;
                    return;
                    if (paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Dev_Info_Battery.toString())) {
                        paramAnonymousInt = arrayOfByte[0];
                        paramAnonymousBluetoothGatt = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_BATTERY_LEVEL");
                        paramAnonymousBluetoothGatt.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramAnonymousInt);
                        BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGatt);
                        return;
                    }
                    if (paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Dev_Info_Fireware.toString())) {
                        try {
                            new String(arrayOfByte, "UTF-8");
                            return;
                        } catch (UnsupportedEncodingException paramAnonymousBluetoothGatt) {
                            paramAnonymousBluetoothGatt.printStackTrace();
                            return;
                        }
                    }
                    if (paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Dev_Info_Software.toString())) {
                        try {
                            BluetoothLeService_Vidonn2.this.readDevHardwareInfo();
                            paramAnonymousBluetoothGatt = new String(arrayOfByte, "UTF-8");
                            paramAnonymousBluetoothGattCharacteristic = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_DEV_VISION");
                            paramAnonymousBluetoothGattCharacteristic.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramAnonymousBluetoothGatt);
                            BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGattCharacteristic);
                            return;
                        } catch (UnsupportedEncodingException paramAnonymousBluetoothGatt) {
                            paramAnonymousBluetoothGatt.printStackTrace();
                            return;
                        }
                    }
                    if (paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Dev_Info_Hardware.toString())) {
                        try {
                            paramAnonymousBluetoothGatt = new String(arrayOfByte, "UTF-8");
                            paramAnonymousBluetoothGattCharacteristic = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_DEV_HARDVISION");
                            paramAnonymousBluetoothGattCharacteristic.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramAnonymousBluetoothGatt);
                            BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGattCharacteristic);
                            return;
                        } catch (UnsupportedEncodingException paramAnonymousBluetoothGatt) {
                            paramAnonymousBluetoothGatt.printStackTrace();
                            return;
                        }
                    }
                }
                while (!paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Dev_Info_ModelType.toString()));
                try {
                    paramAnonymousBluetoothGatt = new String(arrayOfByte, "UTF-8");
                    System.out.println("ModelType=" + paramAnonymousBluetoothGatt);
                    if (paramAnonymousBluetoothGatt.toLowerCase().contains("x6c")) {
                        MyAplication.DevType = 2;
                        MyAplication.UserCurrentSelcetDevName = "x6c-";
                        return;
                    }
                } catch (UnsupportedEncodingException paramAnonymousBluetoothGatt) {
                    paramAnonymousBluetoothGatt.printStackTrace();
                    return;
                }
            } while (!paramAnonymousBluetoothGatt.toLowerCase().contains("x6s"));
            MyAplication.DevType = 3;
            MyAplication.UserCurrentSelcetDevName = "x6s-";
        }


        public void onCharacteristicWrite(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic, int paramAnonymousInt) {
            super.onCharacteristicWrite(paramAnonymousBluetoothGatt, paramAnonymousBluetoothGattCharacteristic, paramAnonymousInt);
            if (paramAnonymousInt == 0) {
                BluetoothLeService_Vidonn2.this.isWriteOK = true;
                BluetoothLeService_Vidonn2.isBluetoothRepairOK = true;
                if (BluetoothLeService_Vidonn2.this.dfu_SendResetCode) {
                    BluetoothLeService_Vidonn2.this.dfu_SendResetCode = false;
                    BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                        public void run() {
                            BluetoothLeService_Vidonn2.this.disconnect(false);
                            BluetoothLeService_Vidonn2.this.close();
                        }
                    }, 2000L);
                }
                if (MyAplication.DevType != 0) {
                    break label153;
                }
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                label85:
                paramAnonymousBluetoothGatt = paramAnonymousBluetoothGattCharacteristic.getUuid().toString();
                if (!paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_AirUpgrade_Img.toString())) {
                    break label624;
                }
                if (paramAnonymousInt != 0) {
                    break label486;
                }
                if (BluetoothLeService_Vidonn2.this.writeStatus_opSize != 1) {
                    break label297;
                }
                BluetoothLeService_Vidonn2.this.writeStatus_opSize = 2;
            }
            label153:
            label297:
            label486:
            label624:
            label955:
            label975:
            do {
                byte[] arrayOfByte;
                do {
                    do {
                        do {
                            do {
                                do {
                                    return;
                                    BluetoothLeService_Vidonn2.this.isWriteOK = false;
                                    BluetoothLeService_Vidonn2.errorTimes += 1;
                                    break;
                                    if (BluetoothLeService_Vidonn2.this.flag_IsMessageTitle) {
                                        BluetoothLeService_Vidonn2.this.flag_IsMessageTitle = false;
                                        BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_WRITE_STATUS", BluetoothLeService_Vidonn2.this.isWriteOK);
                                        if (!MyAplication.isRecodeNotificationLog) {
                                            break label85;
                                        }
                                        MainFunction.writeLogtoFile(0, "3-写入特性状态:" + BluetoothLeService_Vidonn2.this.isWriteOK + " " + BluetoothLeService_Vidonn2.byteToString(paramAnonymousBluetoothGattCharacteristic.getValue()));
                                        break label85;
                                    }
                                    if ((!BluetoothLeService_Vidonn2.flag_IsMessageContext) || (!MyAplication.isRecodeNotificationLog)) {
                                        break label85;
                                    }
                                    MainFunction.writeLogtoFile(0, "5-写入特性状态:" + BluetoothLeService_Vidonn2.this.isWriteOK + " " + BluetoothLeService_Vidonn2.byteToString(paramAnonymousBluetoothGattCharacteristic.getValue()));
                                    break label85;
                                }
                                while ((BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade) || (!BluetoothLeService_Vidonn2.dfu_IsStartUpgrade));
                                BluetoothLeService_Vidonn2.dfu_AirUpgradeCount = Integer.valueOf(BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() + 1);
                                paramAnonymousInt = BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() * 100 / BluetoothLeService_Vidonn2.dfu_PackageCount;
                                if (paramAnonymousInt - BluetoothLeService_Vidonn2.this.duf_ProcessTemp > 0) {
                                    BluetoothLeService_Vidonn2.this.duf_ProcessTemp = paramAnonymousInt;
                                    BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2001, BluetoothLeService_Vidonn2.dfu_AirUpgradeCount).sendToTarget();
                                }
                                System.out.println("write image count=" + BluetoothLeService_Vidonn2.dfu_AirUpgradeCount);
                                if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() < BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval[BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue()]);
                                    return;
                                }
                                if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() == BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval_Last);
                                    return;
                                }
                                System.out.println("write image completed");
                                BluetoothLeService_Vidonn2.dfu_IsStartUpgrade = false;
                                return;
                                if (BluetoothLeService_Vidonn2.this.writeStatus_opSize == 1) {
                                    System.out.println("重写-写入第一个头文件-镜像大小");
                                    BluetoothLeService_Vidonn2.this.writeStatus_opSize = 1;
                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.this.dfu_ImgageSize);
                                    return;
                                }
                            }
                            while ((BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade) || (!BluetoothLeService_Vidonn2.dfu_IsStartUpgrade));
                            if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() < BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval[BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue()]);
                                return;
                            }
                            if (BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue() == BluetoothLeService_Vidonn2.dfu_PackageCount - 1) {
                                BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval_Last);
                                return;
                            }
                            BluetoothLeService_Vidonn2.dfu_IsStartUpgrade = false;
                            return;
                        }
                        while (!paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Operation_AirUpgrade.toString()));
                        if (paramAnonymousInt != 0) {
                            break label975;
                        }
                        if (BluetoothLeService_Vidonn2.this.writeStatus_op1 == 1) {
                            BluetoothLeService_Vidonn2.this.writeStatus_op1 = 2;
                            BluetoothLeService_Vidonn2.this.writeStatus_op8 = 1;
                            if (BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade) {
                                paramAnonymousBluetoothGatt = BluetoothLeService_Vidonn2.this;
                                paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade;
                                arrayOfByte = new byte[3];
                                arrayOfByte[0] = 8;
                                arrayOfByte[1] = 1;
                                paramAnonymousBluetoothGatt.writeCharacteristic(paramAnonymousBluetoothGattCharacteristic, arrayOfByte);
                                return;
                            }
                            paramAnonymousBluetoothGatt = BluetoothLeService_Vidonn2.this;
                            paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade;
                            arrayOfByte = new byte[3];
                            arrayOfByte[0] = 8;
                            arrayOfByte[1] = ((byte) BluetoothLeService_Vidonn2.dfu_FastCount);
                            paramAnonymousBluetoothGatt.writeCharacteristic(paramAnonymousBluetoothGattCharacteristic, arrayOfByte);
                            return;
                        }
                        if (BluetoothLeService_Vidonn2.this.writeStatus_op8 == 1) {
                            BluetoothLeService_Vidonn2.this.writeStatus_op8 = 2;
                            BluetoothLeService_Vidonn2.this.writeStatus_opSize = 1;
                            BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.this.dfu_ImgageSize);
                            return;
                        }
                        if (BluetoothLeService_Vidonn2.this.writeStatus_op2 == 1) {
                            BluetoothLeService_Vidonn2.this.writeStatus_op2 = 2;
                            BluetoothLeService_Vidonn2.this.writeStatus_opCRC = 1;
                            BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.this.dfu_Crc_version);
                            writeOpcodeCheckCRC();
                            return;
                        }
                        if (BluetoothLeService_Vidonn2.this.writeStatus_opCRC == 1) {
                            BluetoothLeService_Vidonn2.this.writeStatus_opCRC = 2;
                            return;
                        }
                        if (BluetoothLeService_Vidonn2.this.writeStatus_op3 != 1) {
                            break label955;
                        }
                        BluetoothLeService_Vidonn2.dfu_IsStartUpgrade = true;
                        BluetoothLeService_Vidonn2.dfu_AirUpgradeCount = Integer.valueOf(0);
                        BluetoothLeService_Vidonn2.this.writeStatus_op3 = 2;
                        BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.dfu_xval[BluetoothLeService_Vidonn2.dfu_AirUpgradeCount.intValue()]);
                    } while (!BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade);
                    new Thread(BluetoothLeService_Vidonn2.this.checkWritefirmwareImgThread).start();
                    return;
                } while (BluetoothLeService_Vidonn2.this.writeStatus_op4 != 1);
                BluetoothLeService_Vidonn2.this.writeStatus_op4 = 2;
                return;
                if (BluetoothLeService_Vidonn2.this.writeStatus_op1 == 1) {
                    System.out.println("重写 --写入控制命令01");
                    BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(0, "重写 --写入控制命令01").sendToTarget();
                    BluetoothLeService_Vidonn2.this.writeStatus_op1 = 1;
                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{1});
                    return;
                }
                if (BluetoothLeService_Vidonn2.this.writeStatus_op8 == 1) {
                    System.out.println("重写 --写入控制命令08-查询数据包个数");
                    BluetoothLeService_Vidonn2.this.writeStatus_op8 = 1;
                    if (BluetoothLeService_Vidonn2.dfu_IsSteadyUpgrade) {
                        paramAnonymousBluetoothGatt = BluetoothLeService_Vidonn2.this;
                        paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade;
                        arrayOfByte = new byte[3];
                        arrayOfByte[0] = 8;
                        arrayOfByte[1] = 1;
                        paramAnonymousBluetoothGatt.writeCharacteristic(paramAnonymousBluetoothGattCharacteristic, arrayOfByte);
                        return;
                    }
                    paramAnonymousBluetoothGatt = BluetoothLeService_Vidonn2.this;
                    paramAnonymousBluetoothGattCharacteristic = BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade;
                    arrayOfByte = new byte[3];
                    arrayOfByte[0] = 8;
                    arrayOfByte[1] = ((byte) BluetoothLeService_Vidonn2.dfu_FastCount);
                    paramAnonymousBluetoothGatt.writeCharacteristic(paramAnonymousBluetoothGattCharacteristic, arrayOfByte);
                    return;
                }
                if (BluetoothLeService_Vidonn2.this.writeStatus_op2 == 1) {
                    BluetoothLeService_Vidonn2.this.writeStatus_op2 = 1;
                    System.out.println("重写 --写入控制命令02-initialize DFU Parameters");
                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{2});
                    return;
                }
                if (BluetoothLeService_Vidonn2.this.writeStatus_opCRC == 1) {
                    System.out.println("重写 --写入第2个头文件-crc_版本");
                    BluetoothLeService_Vidonn2.this.writeStatus_opCRC = 1;
                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Write_Image_AirUpgrade, BluetoothLeService_Vidonn2.this.dfu_Crc_version);
                    return;
                }
                if (BluetoothLeService_Vidonn2.this.writeStatus_op3 == 1) {
                    System.out.println("重写 --写入控制命令03-receive firmware image");
                    BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(0, "写入控制命令03").sendToTarget();
                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{3});
                    BluetoothLeService_Vidonn2.this.writeStatus_op3 = 1;
                    return;
                }
            } while (BluetoothLeService_Vidonn2.this.writeStatus_op4 != 1);
            BluetoothLeService_Vidonn2.this.writeStatus_op4 = 1;
            BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{4});
        }


        public void onConnectionStateChange(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt1, int paramAnonymousInt2) {
            System.out.println("Bluetooth回调 status=" + paramAnonymousInt1 + "   newState=" + paramAnonymousInt2);
            BluetoothLeService_Vidonn2.this.flag_IsMessageComing = false;
            if ((paramAnonymousInt1 == 133) && (paramAnonymousInt2 == 2)) {
                BluetoothLeService_Vidonn2.isBluetoothRepairOK = false;
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_ERROR");
                if (paramAnonymousInt2 != 2) {
                    break label160;
                }
                BluetoothLeService_Vidonn2.errorTimes = 0;
                BluetoothLeService_Vidonn2.dfu_ConnectTimes = 0;
                BluetoothLeService_Vidonn2.this.readBatteryValueTimes = 0;
                BluetoothLeService_Vidonn2.mConnectionState = 2;
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_CONNECTED");
                BluetoothLeService_Vidonn2.mBluetoothGatt.discoverServices();
            }
            label160:
            do {
                do {
                    return;
                    if ((paramAnonymousInt1 == 0) && (paramAnonymousInt2 == 2)) {
                        System.out.println("蓝牙系统正常");
                        BluetoothLeService_Vidonn2.isBluetoothRepairOK = true;
                        break;
                    }
                    if (((paramAnonymousInt1 == 0) && (paramAnonymousInt2 == 0)) || (paramAnonymousInt1 == 0) || (paramAnonymousInt2 != 2)) {
                        break;
                    }
                    boolean bool = MyAplication.isAppStart;
                    break;
                } while (paramAnonymousInt2 != 0);
                BluetoothLeService_Vidonn2.mConnectionState = 0;
                Log.i(BluetoothLeService_Vidonn2.TAG, "Disconnected from GATT server.");
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_DISCONNECTED");
                if (BluetoothLeService_Vidonn2.dfu_IsStartUpgrade) {
                    BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(2002, BluetoothLeService_Vidonn2.this.getResources().getString(2131296705)).sendToTarget();
                }
            } while (!BluetoothLeService_Vidonn2.this.flag_IsNeedCloseGatt);
            BluetoothLeService_Vidonn2.this.close();
        }


        public void onDescriptorWrite(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattDescriptor paramAnonymousBluetoothGattDescriptor, int paramAnonymousInt) {
            super.onDescriptorWrite(paramAnonymousBluetoothGatt, paramAnonymousBluetoothGattDescriptor, paramAnonymousInt);
            paramAnonymousBluetoothGatt = paramAnonymousBluetoothGattDescriptor.getCharacteristic().getUuid().toString();
            paramAnonymousBluetoothGattDescriptor.getValue();
            if ((paramAnonymousInt == 0) && (paramAnonymousBluetoothGatt.equals(BluetoothLeService_Vidonn2.this.uUID_Cha_Operation_AirUpgrade.toString()))) {
                System.out.println("写入控制命令01");
                BluetoothLeService_Vidonn2.this.activityHandler.obtainMessage(0, "写入控制命令01").sendToTarget();
                BluetoothLeService_Vidonn2.this.writeStatus_op1 = 1;
                BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{1});
            }
            if ((MyAplication.DevType == 0) || (MyAplication.isInFirmwareUpdate)) {
            }
            do {
                return;
                switch (BluetoothLeService_Vidonn2.this.setNotification_ID) {
                    default:
                        return;
                }
            } while (BluetoothLeService_Vidonn2.this.cha_Operiation_Read == null);
            BluetoothLeService_Vidonn2.this.setNotification_ID = 2;
            BluetoothLeService_Vidonn2.this.setCharacteristicNotification(BluetoothLeService_Vidonn2.this.cha_Operiation_Read, true);
            return;
            try {
                if (BluetoothLeService_Vidonn2.this.cha_Info_Fireware != null) {
                    BluetoothLeService_Vidonn2.this.setCharacteristicNotification(BluetoothLeService_Vidonn2.this.cha_Info_Fireware, true);
                    BluetoothLeService_Vidonn2.this.setNotification_ID = 0;
                    BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_X6_ISOK");
                    return;
                }
            } catch (Exception paramAnonymousBluetoothGatt) {
                System.out.println("setCharacteristicNotification异常=" + paramAnonymousBluetoothGatt.toString());
                return;
            }
            BluetoothLeService_Vidonn2.this.setCharacteristicNotification(BluetoothLeService_Vidonn2.this.cha_Info_Battery, true);
            BluetoothLeService_Vidonn2.this.setNotification_ID = 3;
            return;
            try {
                if (BluetoothLeService_Vidonn2.this.cha_Heart_Rate != null) {
                    BluetoothLeService_Vidonn2.this.setCharacteristicNotification(BluetoothLeService_Vidonn2.this.cha_Heart_Rate, true);
                    BluetoothLeService_Vidonn2.this.setNotification_ID = 0;
                    BluetoothLeService_Vidonn2.this.isSetNotification_OK = true;
                }
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_X6_ISOK");
                BluetoothLeService_Vidonn2.this.setNotification_ID = 0;
                BluetoothLeService_Vidonn2.this.isSetNotification_OK = true;
                return;
            } catch (Exception paramAnonymousBluetoothGatt) {
                for (; ; ) {
                    System.out.println("cha_Heart_Rate异常=" + paramAnonymousBluetoothGatt.toString());
                }
            }
        }


        public void onReadRemoteRssi(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt1, int paramAnonymousInt2) {
            super.onReadRemoteRssi(paramAnonymousBluetoothGatt, paramAnonymousInt1, paramAnonymousInt2);
            if (paramAnonymousInt2 == 0) {
                paramAnonymousBluetoothGatt = new Intent("com.vidonn2.bluetooth.le.ACTION_READ_RSSI");
                paramAnonymousBluetoothGatt.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramAnonymousInt1);
                BluetoothLeService_Vidonn2.this.sendBroadcast(paramAnonymousBluetoothGatt);
            }
        }


        public void onReliableWriteCompleted(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt) {
            super.onReliableWriteCompleted(paramAnonymousBluetoothGatt, paramAnonymousInt);
        }


        public void onServicesDiscovered(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt) {
            if (paramAnonymousInt == 0) {
                BluetoothLeService_Vidonn2.this.broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
                if (BluetoothLeService_Vidonn2.dfu_IsStartUpgrade) {
                    BluetoothLeService_Vidonn2.dfu_IsReSend = true;
                    BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                        public void run() {
                            BluetoothLeService_Vidonn2.this.activityHandler.postDelayed(new Runnable() {
                                public void run() {
                                    BluetoothLeService_Vidonn2.this.writeCharacteristic(BluetoothLeService_Vidonn2.this.cha_Operation_AirUpgrade, new byte[]{7});
                                }
                            }, 1000L);
                        }
                    }, 500L);
                }
                return;
            }
            Log.w(BluetoothLeService_Vidonn2.TAG, "onServicesDiscovered received: " + paramAnonymousInt);
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(BluetoothDevice paramAnonymousBluetoothDevice, int paramAnonymousInt, byte[] paramAnonymousArrayOfByte) {
        }
    };
    private byte message_CategoryID;
    private byte message_EventFlag;
    public int personalInfo_Goal = 10000;
    public int[][] readAlarmClockData = (int[][]) Array.newInstance(Integer.TYPE, new int[]{8, 6});
    public int readAlarmClock_ID = 0;
    private int readBatteryValueTimes = 0;
    private BluetoothGattService service_Dev_Info;
    private BluetoothGattService service_Dev_Info_Battery;
    private BluetoothGattService service_Dev_Operiation;
    private BluetoothGattService service_Dev_Operiation_Current;
    private BluetoothGattService service_Heart_Rate;
    private BluetoothGattService service_Main_AirUpgrade;
    public int setNotification_ID = 0;
    private UUID uUID_Cha_AirUpgrade_Img = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Dev_Info_Battery = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Dev_Info_Fireware = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Dev_Info_Hardware = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Dev_Info_Manufacturer = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Dev_Info_ModelType = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Dev_Info_Software = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Heart_Rate = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Operation_AirUpgrade = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Operiation_NotificationData = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Operiation_Read = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Operiation_Read_Current = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Cha_Operiation_Write = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Notify = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Service_Dev_Info = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Service_Dev_Info_Battery = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Service_Dev_Operiation = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Service_Dev_Operiation_Current = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Service_Heart_Rate = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private UUID uUID_Service_Main_AirUpgrade = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public int writeAlarmClock_ID = 0;
    public int writePersonalInfoType = 1;
    private int writeStatus_op1 = 0;
    private int writeStatus_op2 = 0;
    private int writeStatus_op3 = 0;
    private int writeStatus_op4 = 0;
    private int writeStatus_op8 = 0;
    private int writeStatus_opCRC = 0;
    private int writeStatus_opSize = 0;

    static {
        dfu_IsFastUpdate = false;
        dfu_IsStartUpgrade = false;
        dfu_IsReSend = false;
        dfu_ConnectTimes = 0;
        dfu_IsReSendCount = 0;
        dfu_FastCount = 200;
        dfu_AirUpgradeCount = Integer.valueOf(0);
        dfu_ReceiveCount = 0;
        dfu_PackageCount = -1;
        dfu_LastPackageLength = -1;
        dfu_historyDataCount = 0;
        OpCode_Current = 0;
        OpCode_Switch = 0;
        readPersonalInfo_Type = 0;
        historyDataReadMode_Normal = true;
        isAble_historyDetail_Steps_All = false;
        historyDetail_Steps_All = (int[][][][]) Array.newInstance(Integer.TYPE, new int[]{7, 24, 30, 2});
        historyData_Immediately = new int[3];
        isBluetoothRepairOK = false;
        errorTimes = 0;
        mConnectionState = 0;
        UUID_HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
        historyDataCount = 0;
    }

    public static byte[] HexString2Bytes(String paramString) {
        int j = paramString.length() / 2;
        byte[] arrayOfByte = new byte[j];
        paramString = paramString.getBytes();
        int i = 0;
        for (; ; ) {
            if (i >= j) {
                return arrayOfByte;
            }
            arrayOfByte[i] = uniteBytes(paramString[(i * 2)], paramString[(i * 2 + 1)]);
            i += 1;
        }
    }


    private void broadcastUpdate(String paramString) {
        sendBroadcast(new Intent(paramString));
    }


    private void broadcastUpdate(String paramString, BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        paramString = new Intent(paramString);
        paramBluetoothGattCharacteristic = paramBluetoothGattCharacteristic.getValue();
        if ((paramBluetoothGattCharacteristic != null) && (paramBluetoothGattCharacteristic.length > 0)) {
            paramString.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramBluetoothGattCharacteristic);
            sendBroadcast(paramString);
            return;
        }
        Log.i(TAG, "要广播的数据为空，或者长度为0");
    }


    private void broadcastUpdate(String paramString, boolean paramBoolean) {
        paramString = new Intent(paramString);
        paramString.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramBoolean);
        sendBroadcast(paramString);
    }


    private void broadcastUpdate_AlarmClock(String paramString, int[][] paramArrayOfInt) {
        paramString = new Intent(paramString);
        if ((paramArrayOfInt != null) && (paramArrayOfInt.length > 0)) {
            int[] arrayOfInt = new int[40];
            int j = 0;
            int i = 0;
            if (i >= paramArrayOfInt.length) {
                paramString.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", arrayOfInt);
                sendBroadcast(paramString);
                return;
            }
            int k = 0;
            for (; ; ) {
                if (k >= paramArrayOfInt[i].length) {
                    i += 1;
                    break;
                }
                arrayOfInt[j] = paramArrayOfInt[i][k];
                j += 1;
                k += 1;
            }
        }
        Log.i(TAG, "要广播的数据为空，或者长度为0");
    }


    private void broadcastUpdate_CurrentValue(String paramString, int[] paramArrayOfInt) {
        paramString = new Intent(paramString);
        if ((paramArrayOfInt != null) && (paramArrayOfInt.length > 0)) {
            paramString.putExtra("com.vidonn2.bluetooth.le.EXTRA_DATA", paramArrayOfInt);
            sendBroadcast(paramString);
            return;
        }
        Log.i(TAG, "要广播的数据为空，或者长度为0");
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


    private byte[] switchAddr(String paramString, boolean paramBoolean) {
        Object localObject = new byte[6];
        paramString = paramString.replaceAll(" ", "").replaceAll(":", "");
        localObject = new String[6];
        localObject[0] = paramString.substring(0, 2);
        localObject[1] = paramString.substring(2, 4);
        localObject[2] = paramString.substring(4, 6);
        localObject[3] = paramString.substring(6, 8);
        localObject[4] = paramString.substring(8, 10);
        localObject[5] = paramString.substring(10, 12);
        if (paramBoolean) {
        }
        for (paramString = localObject[5] + localObject[4] + localObject[3] + localObject[2] + localObject[1] + localObject[0]; ; paramString = localObject[0] + localObject[1] + localObject[2] + localObject[3] + localObject[4] + localObject[5]) {
            return HexString2Bytes(paramString);
        }
    }


    public static byte uniteBytes(byte paramByte1, byte paramByte2) {
        return (byte) ((byte) (Byte.decode("0x" + new String(new byte[]{paramByte1})).byteValue() << 4) ^ Byte.decode("0x" + new String(new byte[]{paramByte2})).byteValue());
    }


    private void writeCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, byte[] paramArrayOfByte) {
        if ((this.mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.e(TAG, "BluetoothAdapter not initialized");
        }
        while (mConnectionState == 0) {
            return;
        }
        try {
            paramBluetoothGattCharacteristic.setValue(paramArrayOfByte);
            paramBluetoothGattCharacteristic.setWriteType(2);
            mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
            disconnect(false);
        }
    }


    public void ShowPairCode() {
        if (MyAplication.DevType == 0) {
            this.devOperation.ShowPairCode();
        }
    }


    public void close() {
        mConnectionState = 0;
        Log.e(TAG, "Service close");
        if (mBluetoothGatt == null) {
            return;
        }
        try {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            return;
        } catch (Exception localException) {
        }
    }


    public boolean connect(String paramString) {
        for (; ; ) {
            Object localObject;
            try {
                Log.e(TAG, "Service connect");
                if ((this.mBluetoothAdapter == null) || (paramString == null)) {
                    Log.w(TAG, "BluetoothAdapter为初始化或者地址为空");
                    return false;
                }
                if (!MyAplication.isInFirmwareUpdate) {
                    break label459;
                }
                localObject = switchAddr(paramString, false);
                if (localObject[(localObject.length - 1)] == 255) {
                    localObject[(localObject.length - 1)] = 0;
                    localObject = byteToString((byte[]) localObject);
                    String str = ((String) localObject).substring(0, ((String) localObject).length() - 1).replace(" ", ":");
                    localObject = str;
                    if (this.isFirstUpgradeConnect) {
                        localObject = str;
                        if (mConnectionState != 0) {
                            this.isFirstUpgradeConnect = false;
                            disconnect(false);
                            close();
                            localObject = str;
                        }
                    }
                    if ((this.mBluetoothDeviceAddress == null) || (!paramString.equals(this.mBluetoothDeviceAddress)) || (mBluetoothGatt == null) || (!mBluetoothGatt.connect())) {
                        break label251;
                    }
                    mConnectionState = 1;
                    return true;
                }
            } catch (Exception paramString) {
                System.out.println("--------------------发起连接异常:" + paramString.toString());
                disconnect(false);
                close();
                return false;
            }
            if ((localObject[(localObject.length - 1)] & 0xF) == 0) {
                localObject[(localObject.length - 1)] = ((byte) (localObject[(localObject.length - 1)] & 0xF0));
            } else {
                localObject[(localObject.length - 1)] = ((byte) (localObject[(localObject.length - 1)] + 1));
                continue;
                label251:
                if ((MyAplication.sdkVersion > 19) && (MyAplication.flag_FirstConnectScan)) {
                    scanDev();
                    return false;
                }
                System.out.println("--------------------发起连接:" + (String) localObject);
                paramString = this.mBluetoothAdapter.getRemoteDevice((String) localObject);
                if (paramString == null) {
                    System.out.println("Device not found.  Unable to connect.");
                    return false;
                }
                if (MyAplication.isInFirmwareUpdate) {
                    dfu_ConnectTimes += 1;
                    if (dfu_ConnectTimes > 4) {
                        dfu_ConnectTimes = 0;
                        this.mBluetoothAdapter.disable();
                        broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_GATT_GO_SETTING_BLUTOOTH");
                    }
                }
                this.mBluetoothDeviceAddress = ((String) localObject);
                mConnectionState = 1;
                mBluetoothGatt = paramString.connectGatt(this, false, this.mGattCallback);
                if (MyAplication.DevType == 0) {
                    this.devOperation_X6 = null;
                    this.devDecode_X6 = null;
                    this.devOperation = new DevOperation(mBluetoothGatt);
                    this.devDecode = new DevDecode();
                    return true;
                }
                this.devOperation = null;
                this.devDecode = null;
                this.devOperation_X6 = new DevOperation_X6(mBluetoothGatt);
                this.devDecode_X6 = new DevDecode_X6();
                return true;
                label459:
                localObject = paramString;
            }
        }
    }


    public void dfu_Update(byte[] paramArrayOfByte) {
        this.dfu_File_Data = paramArrayOfByte;
        System.out.println("开始升级=------------");
        int i = 0;
        int j;
        if (i >= 16) {
            j = 0 + 16;
            i = j;
            label29:
            if (i < 24) {
                break label387;
            }
            j += 8;
            i = j;
            label42:
            if (i < 28) {
                break label409;
            }
            j += 4;
            i = j;
            label54:
            if (i < 32) {
                break label431;
            }
            i = j + 4;
            label64:
            if (i < 36) {
                break label453;
            }
            i = 0;
            label72:
            if (i < 4) {
                break label475;
            }
            i = 4;
            label79:
            if (i < 8) {
                break label494;
            }
            dfu_PackageCount = (this.dfu_File_Data.length - 256) / 20;
            dfu_LastPackageLength = (this.dfu_File_Data.length - 256) % 20;
            if (dfu_LastPackageLength != 0) {
                dfu_PackageCount += 1;
            }
            this.activityHandler.obtainMessage(2000, Integer.valueOf(dfu_PackageCount)).sendToTarget();
            System.out.println("升级包总数=" + dfu_PackageCount + "   余数=" + dfu_LastPackageLength);
            System.out.println("fileNameDescription:\n" + byteToString(this.dfu_FileNameDescription));
            System.out.println("fileCreateTime:\n" + byteToString(this.dfu_FileCreateTime));
            System.out.println("version:\n" + byteToString(this.dfu_Version));
            System.out.println("imgageSize:\n" + byteToString(this.dfu_ImgageSize));
            System.out.println("crc:\n" + byteToString(this.dfu_Crc));
            System.out.println("crc_version:\n" + byteToString(this.dfu_Crc_version));
        }
        for (; ; ) {
            try {
                paramArrayOfByte = mBluetoothGatt;
                if (paramArrayOfByte == null) {
                    return;
                    this.dfu_FileNameDescription[i] = this.dfu_File_Data[i];
                    i += 1;
                    break;
                    label387:
                    this.dfu_FileCreateTime[(i - 16)] = this.dfu_File_Data[i];
                    i += 1;
                    break label29;
                    label409:
                    this.dfu_Version[(i - 24)] = this.dfu_File_Data[i];
                    i += 1;
                    break label42;
                    label431:
                    this.dfu_ImgageSize[(i - 28)] = this.dfu_File_Data[i];
                    i += 1;
                    break label54;
                    label453:
                    this.dfu_Crc[(i - 32)] = this.dfu_File_Data[i];
                    i += 1;
                    break label64;
                    label475:
                    this.dfu_Crc_version[i] = this.dfu_Crc[i];
                    i += 1;
                    break label72;
                    label494:
                    this.dfu_Crc_version[i] = this.dfu_Version[(i - 4)];
                    i += 1;
                    break label79;
                }
                if (mConnectionState == 0) {
                    continue;
                }
                i = dfu_PackageCount;
                dfu_xval = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{i, 20});
                i = 0;
                if (i >= dfu_xval.length - 1) {
                    if (dfu_LastPackageLength == 0) {
                        dfu_LastPackageLength = 20;
                    }
                    dfu_xval_Last = new byte[dfu_LastPackageLength];
                    i = 0;
                    if (i < dfu_LastPackageLength) {
                        break label701;
                    }
                    this.writeStatus_op1 = 0;
                    this.writeStatus_op8 = 0;
                    this.writeStatus_opSize = 0;
                    this.writeStatus_op2 = 0;
                    this.writeStatus_opCRC = 0;
                    this.writeStatus_op3 = 0;
                    setCharacteristicNotification(this.cha_Operation_AirUpgrade, true);
                    return;
                }
            } catch (Exception paramArrayOfByte) {
                System.out.println("Bluetooth-写入通知异常------------" + paramArrayOfByte.toString());
                return;
            }
            j = 0;
            for (; ; ) {
                if (j >= 20) {
                    i += 1;
                    break;
                }
                dfu_xval[i][j] = this.dfu_File_Data[(i * 20 + 256 + j)];
                j += 1;
            }
            label701:
            j = dfu_xval.length;
            dfu_xval_Last[i] = this.dfu_File_Data[((j - 1) * 20 + 256 + i)];
            i += 1;
        }
    }


    public void disconnect(boolean paramBoolean) {
        this.flag_IsNeedCloseGatt = paramBoolean;
        Log.e(TAG, "Service disconnect");
        if ((this.mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
            mBluetoothGatt.disconnect();
            return;
        } catch (Exception localException) {
        }
    }


    public void fastReadHistoryData() {
        OpCode_Current = 102;
        this.devOperation_X6.readHistoryRecodeDatail((byte) this.historyDetail_Data_Block_ID_Fast, (byte) this.historyDetail_Data_Block_Hour_ID_Fast);
        this.historyDetail_Data_Block_Hour_ID_Fast += 1;
        if (this.historyDetail_Data_Block_Hour_ID_Fast > 23) {
            this.historyDetail_Data_Block_Hour_ID_Fast = 0;
            this.historyDetail_Data_Block_Arrys_ID_Fast += 1;
        }
        if (this.historyDetail_Data_Block_Arrys_ID_Fast < 7) {
            this.historyDetail_Data_Block_ID_Fast = historyDate_Map[this.historyDetail_Data_Block_Arrys_ID_Fast][0];
            if ((this.historyDetail_Data_Block_Arrys_ID_Fast != 6) || (MyAplication.currentTime[3] >= this.historyDetail_Data_Block_Hour_ID_Fast)) {
            }
        } else {
            return;
        }
        this.activityHandler.postDelayed(new Runnable() {
            public void run() {
                BluetoothLeService_Vidonn2.this.fastReadHistoryData();
            }
        }, 200L);
    }


    public BluetoothAdapter getBluetoothAdapter() {
        return this.mBluetoothAdapter;
    }


    public byte[] getDfuFile() {
        if (MyAplication.APPVersionCode == 0) {
            localObject1 = new File(Environment.getExternalStorageDirectory() + MyAplication.filePath_firmware);
        }
        for (Object localObject1 = localObject1 + "/firmware.bin"; ; localObject1 = localObject1 + "/firmware.bin") {
            localObject3 = new File((String) localObject1);
            if (((File) localObject3).exists()) {
                break;
            }
            return null;
            localObject1 = new File(Environment.getExternalStorageDirectory() + MyAplication.filePath_firmware1);
        }
        long l = ((File) localObject3).length();
        Object localObject3 = new byte[(int) l];
        System.out.println("文件长度=" + l);
        try {
            localObject1 = new BufferedInputStream(new FileInputStream((String) localObject1));
            for (; ; ) {
                try {
                    int i = ((BufferedInputStream) localObject1).read((byte[]) localObject3);
                    if (i != -1) {
                        continue;
                    }
                } catch (IOException localIOException2) {
                    Object localObject2;
                    localIOException2.printStackTrace();
                    continue;
                }
                try {
                    ((BufferedInputStream) localObject1).close();
                    return (byte[]) localObject3;
                } catch (IOException localIOException1) {
                    localIOException1.printStackTrace();
                    return (byte[]) localObject3;
                }
            }
        } catch (FileNotFoundException localFileNotFoundException) {
            for (; ; ) {
                localObject2 = null;
                localFileNotFoundException.printStackTrace();
                continue;
                System.out.println("读取中=" + l);
            }
        }
    }


    public Handler getHandler() {
        return this.activityHandler;
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }


    /* Error */
    public void initUUID() {
        // Byte code:
        //   0: getstatic 665	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:mBluetoothGatt	Landroid/bluetooth/BluetoothGatt;
        //   3: ifnull +741 -> 744
        //   6: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   9: ldc_w 1094
        //   12: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   15: getstatic 873	com/sz/vidonn2/activity/main/MyAplication:isInFirmwareUpdate	Z
        //   18: istore_2
        //   19: iload_2
        //   20: ifne +713 -> 733
        //   23: aload_0
        //   24: getstatic 665	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:mBluetoothGatt	Landroid/bluetooth/BluetoothGatt;
        //   27: aload_0
        //   28: getfield 497	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Service_Dev_Operiation	Ljava/util/UUID;
        //   31: invokevirtual 1098	android/bluetooth/BluetoothGatt:getService	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattService;
        //   34: putfield 1100	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Operiation	Landroid/bluetooth/BluetoothGattService;
        //   37: aload_0
        //   38: getstatic 665	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:mBluetoothGatt	Landroid/bluetooth/BluetoothGatt;
        //   41: aload_0
        //   42: getfield 513	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Service_Dev_Operiation_Current	Ljava/util/UUID;
        //   45: invokevirtual 1098	android/bluetooth/BluetoothGatt:getService	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattService;
        //   48: putfield 1102	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Operiation_Current	Landroid/bluetooth/BluetoothGattService;
        //   51: aload_0
        //   52: aload_0
        //   53: getfield 1100	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Operiation	Landroid/bluetooth/BluetoothGattService;
        //   56: aload_0
        //   57: getfield 501	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Operiation_Read	Ljava/util/UUID;
        //   60: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   63: putfield 650	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Operiation_Read	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   66: aload_0
        //   67: aload_0
        //   68: getfield 1100	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Operiation	Landroid/bluetooth/BluetoothGattService;
        //   71: aload_0
        //   72: getfield 505	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Operiation_Write	Ljava/util/UUID;
        //   75: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   78: putfield 1110	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Operiation_Write	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   81: aload_0
        //   82: aload_0
        //   83: getfield 1100	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Operiation	Landroid/bluetooth/BluetoothGattService;
        //   86: aload_0
        //   87: getfield 509	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Operiation_NotificationData	Ljava/util/UUID;
        //   90: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   93: putfield 1112	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Operiation_NotificationData	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   96: aload_0
        //   97: getfield 935	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:devOperation_X6	Lcom/sz/vidonn2/bluetooth/service/DevOperation_X6;
        //   100: aload_0
        //   101: getfield 1110	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Operiation_Write	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   104: invokevirtual 1116	com/sz/vidonn2/bluetooth/service/DevOperation_X6:setWriteCharacteristic	(Landroid/bluetooth/BluetoothGattCharacteristic;)V
        //   107: aload_0
        //   108: getfield 935	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:devOperation_X6	Lcom/sz/vidonn2/bluetooth/service/DevOperation_X6;
        //   111: aload_0
        //   112: getfield 1112	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Operiation_NotificationData	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   115: invokevirtual 1119	com/sz/vidonn2/bluetooth/service/DevOperation_X6:setWriteCharacteristic_NotificationData	(Landroid/bluetooth/BluetoothGattCharacteristic;)V
        //   118: aload_0
        //   119: aload_0
        //   120: getfield 1102	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Operiation_Current	Landroid/bluetooth/BluetoothGattService;
        //   123: aload_0
        //   124: getfield 517	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Operiation_Read_Current	Ljava/util/UUID;
        //   127: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   130: putfield 1121	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Operiation_Read_Current	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   133: aload_0
        //   134: getstatic 665	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:mBluetoothGatt	Landroid/bluetooth/BluetoothGatt;
        //   137: aload_0
        //   138: getfield 459	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Service_Dev_Info	Ljava/util/UUID;
        //   141: invokevirtual 1098	android/bluetooth/BluetoothGatt:getService	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattService;
        //   144: putfield 1123	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info	Landroid/bluetooth/BluetoothGattService;
        //   147: aload_0
        //   148: aload_0
        //   149: getfield 1123	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info	Landroid/bluetooth/BluetoothGattService;
        //   152: aload_0
        //   153: getfield 475	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Dev_Info_Fireware	Ljava/util/UUID;
        //   156: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   159: putfield 653	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Info_Fireware	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   162: aload_0
        //   163: aload_0
        //   164: getfield 1123	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info	Landroid/bluetooth/BluetoothGattService;
        //   167: aload_0
        //   168: getfield 479	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Dev_Info_Hardware	Ljava/util/UUID;
        //   171: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   174: putfield 1125	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Info_Hardware	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   177: aload_0
        //   178: aload_0
        //   179: getfield 1123	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info	Landroid/bluetooth/BluetoothGattService;
        //   182: aload_0
        //   183: getfield 483	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Dev_Info_Software	Ljava/util/UUID;
        //   186: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   189: putfield 1127	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Info_Software	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   192: aload_0
        //   193: aload_0
        //   194: getfield 1123	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info	Landroid/bluetooth/BluetoothGattService;
        //   197: aload_0
        //   198: getfield 487	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Dev_Info_Manufacturer	Ljava/util/UUID;
        //   201: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   204: putfield 1129	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Info_Manufacturer	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   207: aload_0
        //   208: getstatic 665	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:mBluetoothGatt	Landroid/bluetooth/BluetoothGatt;
        //   211: aload_0
        //   212: getfield 463	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Service_Dev_Info_Battery	Ljava/util/UUID;
        //   215: invokevirtual 1098	android/bluetooth/BluetoothGatt:getService	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattService;
        //   218: putfield 1131	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info_Battery	Landroid/bluetooth/BluetoothGattService;
        //   221: aload_0
        //   222: aload_0
        //   223: getfield 1131	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Dev_Info_Battery	Landroid/bluetooth/BluetoothGattService;
        //   226: aload_0
        //   227: getfield 467	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Dev_Info_Battery	Ljava/util/UUID;
        //   230: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   233: putfield 656	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Info_Battery	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   236: aload_0
        //   237: getstatic 665	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:mBluetoothGatt	Landroid/bluetooth/BluetoothGatt;
        //   240: aload_0
        //   241: getfield 491	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Service_Heart_Rate	Ljava/util/UUID;
        //   244: invokevirtual 1098	android/bluetooth/BluetoothGatt:getService	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattService;
        //   247: putfield 1133	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Heart_Rate	Landroid/bluetooth/BluetoothGattService;
        //   250: aload_0
        //   251: aload_0
        //   252: getfield 1133	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:service_Heart_Rate	Landroid/bluetooth/BluetoothGattService;
        //   255: aload_0
        //   256: getfield 493	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:uUID_Cha_Heart_Rate	Ljava/util/UUID;
        //   259: invokevirtual 1108	android/bluetooth/BluetoothGattService:getCharacteristic	(Ljava/util/UUID;)Landroid/bluetooth/BluetoothGattCharacteristic;
        //   262: putfield 659	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:cha_Heart_Rate	Landroid/bluetooth/BluetoothGattCharacteristic;
        //   265: aload_0
        //   266: invokevirtual 1136	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:setNotification	()V
        //   269: return
        //   270: astore_1
        //   271: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   274: new 763	java/lang/StringBuilder
        //   277: dup
        //   278: ldc_w 1138
        //   281: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   284: aload_1
        //   285: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   288: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   291: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   294: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   297: goto -260 -> 37
        //   300: astore_1
        //   301: aload_0
        //   302: iconst_0
        //   303: invokevirtual 844	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:disconnect	(Z)V
        //   306: aload_0
        //   307: invokevirtual 882	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:close	()V
        //   310: aload_0
        //   311: ldc 53
        //   313: invokespecial 647	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:broadcastUpdate	(Ljava/lang/String;)V
        //   316: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   319: new 763	java/lang/StringBuilder
        //   322: dup
        //   323: ldc_w 1140
        //   326: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   329: aload_1
        //   330: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   333: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   336: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   339: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   342: return
        //   343: astore_1
        //   344: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   347: new 763	java/lang/StringBuilder
        //   350: dup
        //   351: ldc_w 1142
        //   354: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   357: aload_1
        //   358: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   361: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   364: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   367: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   370: goto -319 -> 51
        //   373: astore_1
        //   374: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   377: new 763	java/lang/StringBuilder
        //   380: dup
        //   381: ldc_w 1144
        //   384: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   387: aload_1
        //   388: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   391: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   394: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   397: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   400: goto -334 -> 66
        //   403: astore_1
        //   404: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   407: new 763	java/lang/StringBuilder
        //   410: dup
        //   411: ldc_w 1146
        //   414: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   417: aload_1
        //   418: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   421: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   424: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   427: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   430: goto -349 -> 81
        //   433: astore_1
        //   434: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   437: new 763	java/lang/StringBuilder
        //   440: dup
        //   441: ldc_w 1148
        //   444: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   447: aload_1
        //   448: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   451: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   454: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   457: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   460: goto -364 -> 96
        //   463: astore_1
        //   464: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   467: new 763	java/lang/StringBuilder
        //   470: dup
        //   471: ldc_w 1150
        //   474: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   477: aload_1
        //   478: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   481: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   484: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   487: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   490: goto -383 -> 107
        //   493: astore_1
        //   494: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   497: new 763	java/lang/StringBuilder
        //   500: dup
        //   501: ldc_w 1150
        //   504: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   507: aload_1
        //   508: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   511: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   514: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   517: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   520: goto -402 -> 118
        //   523: astore_1
        //   524: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   527: new 763	java/lang/StringBuilder
        //   530: dup
        //   531: ldc_w 1152
        //   534: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   537: aload_1
        //   538: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   541: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   544: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   547: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   550: goto -417 -> 133
        //   553: astore_1
        //   554: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   557: new 763	java/lang/StringBuilder
        //   560: dup
        //   561: ldc_w 1154
        //   564: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   567: aload_1
        //   568: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   571: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   574: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   577: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   580: goto -433 -> 147
        //   583: astore_1
        //   584: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   587: new 763	java/lang/StringBuilder
        //   590: dup
        //   591: ldc_w 1156
        //   594: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   597: aload_1
        //   598: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   601: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   604: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   607: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   610: goto -448 -> 162
        //   613: astore_1
        //   614: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   617: new 763	java/lang/StringBuilder
        //   620: dup
        //   621: ldc_w 1158
        //   624: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   627: aload_1
        //   628: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   631: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   634: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   637: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   640: goto -463 -> 177
        //   643: astore_1
        //   644: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   647: new 763	java/lang/StringBuilder
        //   650: dup
        //   651: ldc_w 1160
        //   654: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   657: aload_1
        //   658: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   661: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   664: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   667: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   670: goto -478 -> 192
        //   673: astore_1
        //   674: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   677: new 763	java/lang/StringBuilder
        //   680: dup
        //   681: ldc_w 1162
        //   684: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   687: aload_1
        //   688: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   691: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   694: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   697: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   700: goto -493 -> 207
        //   703: astore_1
        //   704: getstatic 897	java/lang/System:out	Ljava/io/PrintStream;
        //   707: new 763	java/lang/StringBuilder
        //   710: dup
        //   711: ldc_w 1164
        //   714: invokespecial 806	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
        //   717: aload_1
        //   718: invokevirtual 900	java/lang/Exception:toString	()Ljava/lang/String;
        //   721: invokevirtual 786	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   724: invokevirtual 768	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   727: invokevirtual 905	java/io/PrintStream:println	(Ljava/lang/String;)V
        //   730: goto -494 -> 236
        //   733: aload_0
        //   734: invokevirtual 1167	com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2:initUpdateUUID	()V
        //   737: goto -472 -> 265
        //   740: astore_1
        //   741: goto -476 -> 265
        //   744: return
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	745	0	this	BluetoothLeService_Vidonn2
        //   270	15	1	localException1	Exception
        //   300	30	1	localException2	Exception
        //   343	15	1	localException3	Exception
        //   373	15	1	localException4	Exception
        //   403	15	1	localException5	Exception
        //   433	15	1	localException6	Exception
        //   463	15	1	localException7	Exception
        //   493	15	1	localException8	Exception
        //   523	15	1	localException9	Exception
        //   553	15	1	localException10	Exception
        //   583	15	1	localException11	Exception
        //   613	15	1	localException12	Exception
        //   643	15	1	localException13	Exception
        //   673	15	1	localException14	Exception
        //   703	15	1	localException15	Exception
        //   740	1	1	localException16	Exception
        //   18	2	2	bool	boolean
        // Exception table:
        //   from	to	target	type
        //   23	37	270	java/lang/Exception
        //   0	19	300	java/lang/Exception
        //   265	269	300	java/lang/Exception
        //   271	297	300	java/lang/Exception
        //   344	370	300	java/lang/Exception
        //   374	400	300	java/lang/Exception
        //   404	430	300	java/lang/Exception
        //   434	460	300	java/lang/Exception
        //   464	490	300	java/lang/Exception
        //   494	520	300	java/lang/Exception
        //   524	550	300	java/lang/Exception
        //   554	580	300	java/lang/Exception
        //   584	610	300	java/lang/Exception
        //   614	640	300	java/lang/Exception
        //   644	670	300	java/lang/Exception
        //   674	700	300	java/lang/Exception
        //   704	730	300	java/lang/Exception
        //   733	737	300	java/lang/Exception
        //   37	51	343	java/lang/Exception
        //   51	66	373	java/lang/Exception
        //   66	81	403	java/lang/Exception
        //   81	96	433	java/lang/Exception
        //   96	107	463	java/lang/Exception
        //   107	118	493	java/lang/Exception
        //   118	133	523	java/lang/Exception
        //   133	147	553	java/lang/Exception
        //   147	162	583	java/lang/Exception
        //   162	177	613	java/lang/Exception
        //   177	192	643	java/lang/Exception
        //   192	207	673	java/lang/Exception
        //   207	236	703	java/lang/Exception
        //   236	265	740	java/lang/Exception
    }


    public void initUpdateUUID() {
        if (mBluetoothGatt != null) {
            try {
                this.service_Main_AirUpgrade = mBluetoothGatt.getService(this.uUID_Service_Main_AirUpgrade);
                if (this.service_Main_AirUpgrade != null) {
                    this.cha_Write_Image_AirUpgrade = this.service_Main_AirUpgrade.getCharacteristic(this.uUID_Cha_AirUpgrade_Img);
                    this.cha_Operation_AirUpgrade = this.service_Main_AirUpgrade.getCharacteristic(this.uUID_Cha_Operation_AirUpgrade);
                    return;
                }
                disconnect(false);
                close();
                return;
            } catch (Exception localException) {
                System.out.println("--------------------初始化UUID异常" + localException.toString());
            }
        }
    }


    public boolean initialize() {
        int i = 0;
        for (; ; ) {
            if (i >= this.historyNoData.length) {
                if (this.mBluetoothManager != null) {
                    break;
                }
                this.mBluetoothManager = ((BluetoothManager) getSystemService("bluetooth"));
                if (this.mBluetoothManager != null) {
                    break;
                }
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
            this.historyNoData[i][0] = -1;
            this.historyNoData[i][1] = 0;
            i += 1;
        }
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }


    public IBinder onBind(Intent paramIntent) {
        Log.e(TAG, "Service onBind ");
        return this.mBinder;
    }


    public void onCreate() {
        Log.e(TAG, "Service onCreate");
        super.onCreate();
    }


    public void onDestroy() {
        close();
        Log.e(TAG, "Service onDestroy");
        super.onDestroy();
    }


    public void onRebind(Intent paramIntent) {
        Log.e(TAG, "Service onRebind");
        super.onRebind(paramIntent);
    }


    @Deprecated
    public void onStart(Intent paramIntent, int paramInt) {
        Log.e(TAG, "Service onStart");
        super.onStart(paramIntent, paramInt);
    }


    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        Log.e(TAG, "Service onStartCommand");
        return super.onStartCommand(paramIntent, paramInt1, paramInt2);
    }


    public boolean onUnbind(Intent paramIntent) {
        Log.e(TAG, "Service onUnbind");
        close();
        return super.onUnbind(paramIntent);
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


    public int[][] rankHistoryDateMap(int[][] paramArrayOfInt) {
        int i = 0;
        if (i >= paramArrayOfInt.length - 1) {
            i = 0;
        }
        for (; ; ) {
            if (i >= paramArrayOfInt.length) {
                return paramArrayOfInt;
                j = i + 1;
                for (; ; ) {
                    if (j >= paramArrayOfInt.length) {
                        i += 1;
                        break;
                    }
                    if (paramArrayOfInt[i][1] * 10000 + paramArrayOfInt[i][2] * 100 + paramArrayOfInt[i][3] > paramArrayOfInt[j][1] * 10000 + paramArrayOfInt[j][2] * 100 + paramArrayOfInt[j][3]) {
                        int[] arrayOfInt = paramArrayOfInt[i];
                        paramArrayOfInt[i] = paramArrayOfInt[j];
                        paramArrayOfInt[j] = arrayOfInt;
                    }
                    j += 1;
                }
            }
            int j = paramArrayOfInt[i][1];
            int k = paramArrayOfInt[i][2];
            int m = paramArrayOfInt[i][3];
            System.out.println("序号:" + i + "  " + paramArrayOfInt[i][0] + "区块  时间=" + (j * 10000 + k * 100 + m));
            i += 1;
        }
    }


    public void readAlarmClockData() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readAlarmClockData();
            return;
        }
        this.readAlarmClock_ID = 6;
        this.readAlarmClock_ID = 0;
        OpCode_Current = 105;
        this.devOperation_X6.readAlarmClock((byte) this.readAlarmClock_ID);
    }


    public void readAllHistoryValue() {
        if ((MyAplication.DevType == 0) && (historyDataCount == 0)) {
            broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_START_READ_HISTORY_SPORT");
            readHistoryValue();
        }
    }


    public void readBatteryValue() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readBatteryValue();
        }
        while (this.readBatteryValueTimes >= 2) {
            return;
        }
        this.readBatteryValueTimes += 1;
        readCharacteristic(this.cha_Info_Battery);
    }


    public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic) {
        if ((this.mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
        while (mConnectionState == 0) {
            return;
        }
        try {
            mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
            disconnect(false);
        }
    }


    public void readCurrentDate() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readCurrentDate();
            return;
        }
        OpCode_Current = 107;
        this.devOperation_X6.readDate_Time();
    }


    public void readCurrentValue() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readCurrentValue();
            return;
        }
        OpCode_Current = 3;
        this.devOperation_X6.readCurrentValue();
    }


    public void readDevHardwareInfo() {
        try {
            readCharacteristic(this.cha_Info_Hardware);
            return;
        } catch (Exception localException) {
        }
    }


    public void readDevVision() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readDevVision();
            return;
        }
        readCharacteristic(this.cha_Info_Software);
    }


    public void readHistoryData_Start_X6() {
        broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_START_READ_HISTORY_SPORT");
        this.historyDetail_Data_ID = 0;
        this.historyDetail_Data_Block_ID = 1;
        this.historyDetail_Data_Block_Arrys_ID = 0;
        this.historyDetail_Data_Block_Hour_ID = 0;
        int j = MyAplication.X6_dev_Data_LastSync_Date;
        byte b = (byte) MyAplication.X6_dev_Data_LastSync_Hour;
        this.historyDetail_Data_Block_Hour_ID = b;
        int i = 0;
        for (; ; ) {
            if (i >= 7) {
            }
            for (; ; ) {
                System.out.println("历史数据开始时间区块:" + this.historyDetail_Data_Block_ID + " 时间=" + historyDate_Map[this.historyDetail_Data_Block_Arrys_ID][2] + "-" + historyDate_Map[this.historyDetail_Data_Block_Arrys_ID][3]);
                if (!historyDataReadMode_Normal) {
                    break label248;
                }
                OpCode_Current = 102;
                this.devOperation_X6.readHistoryRecodeDatail((byte) this.historyDetail_Data_Block_ID, b);
                return;
                int k = historyDate_Map[i][1] * 10000 + historyDate_Map[i][2] * 100 + historyDate_Map[i][3];
                if (k < j) {
                    break;
                }
                this.historyDetail_Data_Block_Arrys_ID = i;
                this.historyDetail_Data_Block_ID = historyDate_Map[i][0];
                System.out.println("历史数据开始时间编号:" + i + " " + k + "  " + this.historyDetail_Data_Block_ID);
            }
            i += 1;
        }
        label248:
        this.historyDetail_Data_Block_Hour_ID_Fast = this.historyDetail_Data_Block_Hour_ID;
        this.historyDetail_Data_Block_Arrys_ID_Fast = this.historyDetail_Data_Block_Arrys_ID;
        this.historyDetail_Data_Block_ID_Fast = this.historyDetail_Data_Block_ID;
        fastReadHistoryData();
    }


    public void readHistoryDateMap_X6() {
        historyDataCount = 20;
        OpCode_Current = 101;
        this.devOperation_X6.readHistoryRecodeDate();
    }


    public void readHistoryValue() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readHistoryValue();
        }
    }


    public void readMAC_SN() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readMAC_SN();
            return;
        }
        this.devOperation_X6.readMAC_SN();
    }


    public void readPersonalData() {
        if (MyAplication.DevType == 0) {
            this.devOperation.readPersonalData();
            return;
        }
        OpCode_Current = 103;
        readPersonalInfo_Type = 1;
        this.devOperation_X6.readPersonalInfo((byte) 1);
    }


    public void readRssi() {
        try {
            if (mBluetoothGatt == null) {
                return;
            }
            if (mConnectionState != 0) {
                mBluetoothGatt.readRemoteRssi();
                return;
            }
        } catch (Exception localException) {
            System.out.println("Bluetooth-读取日期据异常------------");
        }
    }


    public void resetDev() {
        try {
            if (mBluetoothGatt == null) {
                return;
            }
            if (mConnectionState == 0) {
                return;
            }
            MyAplication.isInFirmwareUpdate = true;
            this.dfu_SendResetCode = true;
            this.flag_IsNeedCloseGatt = true;
            dfu_IsFirstDiscovery = false;
            dfu_IsStartUpgrade = false;
            dfu_IsReSend = false;
            dfu_IsReSendCount = 0;
            dfu_ConnectTimes = 0;
            dfu_AirUpgradeCount = Integer.valueOf(0);
            dfu_ReceiveCount = 0;
            this.duf_ProcessTemp = 0;
            dfu_PackageCount = -1;
            dfu_LastPackageLength = -1;
            historyDataCount = 0;
            if (MyAplication.DevType == 1) {
                byte[] arrayOfByte1 = switchAddr(this.mBluetoothDeviceAddress, true);
                int i = arrayOfByte1[0];
                int j = arrayOfByte1[1];
                int k = arrayOfByte1[2];
                int m = arrayOfByte1[3];
                int n = arrayOfByte1[4];
                int i1 = arrayOfByte1[5];
                this.devOperation_X6.writeCode(new byte[]{64, 1, i, j, k, m, n, i1}, false);
                return;
            }
        } catch (Exception localException) {
            MyAplication.isInFirmwareUpdate = false;
            System.out.println("设备复位异常------------");
            return;
        }
        if (MyAplication.DevType == 2) {
            byte[] arrayOfByte2 = new byte[3];
            arrayOfByte2[0] = 64;
            arrayOfByte2[1] = 4;
            this.devOperation_X6.writeCode(arrayOfByte2, false);
        }
    }


    public void resetDev_Normal() {
        try {
            if (mBluetoothGatt == null) {
                return;
            }
            if (mConnectionState != 0) {
                this.devOperation_X6.writeCode(new byte[]{64, 1}, false);
                return;
            }
        } catch (Exception localException) {
            MyAplication.isInFirmwareUpdate = false;
            System.out.println("设备复位异常------------");
        }
    }


    public void scanDev() {
        if (!this.flag_mScanning) {
        }
        try {
            System.out.println("-----service搜索设备");
            this.activityHandler.postDelayed(new Runnable() {
                public void run() {
                    MyAplication.flag_FirstConnectScan = false;
                    if (BluetoothLeService_Vidonn2.this.flag_mScanning) {
                        System.out.println("-----service搜索结束");
                        BluetoothLeService_Vidonn2.this.flag_mScanning = false;
                        BluetoothLeService_Vidonn2.this.mBluetoothAdapter.stopLeScan(BluetoothLeService_Vidonn2.this.mLeScanCallback);
                        BluetoothLeService_Vidonn2.this.connect(BluetoothLeService_Vidonn2.this.mBluetoothDeviceAddress);
                    }
                }
            }, 3000L);
            this.flag_mScanning = true;
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            return;
        } catch (Exception localException) {
        }
    }


    public void scanDevForFirmwareUpdate() {
        if (!this.flag_mScanning) {
        }
        try {
            System.out.println("-----service搜索设备");
            this.activityHandler.postDelayed(new Runnable() {
                public void run() {
                    MyAplication.flag_FirstConnectScan = false;
                    if (BluetoothLeService_Vidonn2.this.flag_mScanning) {
                        System.out.println("-----service搜索结束");
                        BluetoothLeService_Vidonn2.this.flag_mScanning = false;
                        BluetoothLeService_Vidonn2.this.mBluetoothAdapter.stopLeScan(BluetoothLeService_Vidonn2.this.mLeScanCallback);
                    }
                }
            }, 2500L);
            this.flag_mScanning = true;
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            return;
        } catch (Exception localException) {
        }
    }


    public void sendFirmwareImg() {
        if (dfu_AirUpgradeCount.intValue() < dfu_PackageCount - 1) {
            System.out.println("write image count=" + dfu_AirUpgradeCount);
            writeCharacteristic(this.cha_Write_Image_AirUpgrade, dfu_xval[dfu_AirUpgradeCount.intValue()]);
        }
        while (dfu_AirUpgradeCount.intValue() != dfu_PackageCount - 1) {
            return;
        }
        System.out.println("write image count=" + dfu_AirUpgradeCount);
        writeCharacteristic(this.cha_Write_Image_AirUpgrade, dfu_xval_Last);
    }


    public void setBluetooth(boolean paramBoolean) {
        if (paramBoolean) {
            this.mBluetoothAdapter.enable();
            return;
        }
        dfu_ConnectTimes = 0;
        this.mBluetoothAdapter.disable();
    }


    public void setCharacteristicNotification(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, boolean paramBoolean) {
        if ((this.mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.w(TAG, "BluetoothAdapter not initialized");
        }
        while ((mConnectionState == 0) || (paramBluetoothGattCharacteristic == null)) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(paramBluetoothGattCharacteristic, paramBoolean);
        try {
            paramBluetoothGattCharacteristic = paramBluetoothGattCharacteristic.getDescriptor(this.uUID_Notify);
            paramBluetoothGattCharacteristic.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(paramBluetoothGattCharacteristic);
            return;
        } catch (Exception paramBluetoothGattCharacteristic) {
            System.out.println("setCharacteristicNotification异常=" + paramBluetoothGattCharacteristic.toString());
        }
    }


    public void setHandler(Handler paramHandler) {
        this.activityHandler = paramHandler;
    }


    public void setNotification() {
        System.out.println("--------------------setNotification");
        if (MyAplication.isInFirmwareUpdate) {
            if (!dfu_IsStartUpgrade) {
                dfu_Update(getDfuFile());
            }
        }
        do {
            return;
            this.setNotification_ID = 1;
        } while (this.cha_Operiation_Read_Current == null);
        setCharacteristicNotification(this.cha_Operiation_Read_Current, true);
    }


    public int[][] switchAlarmClockData_X6(int[][] paramArrayOfInt) {
        int[][] arrayOfInt = (int[][]) Array.newInstance(Integer.TYPE, new int[]{8, 5});
        int i = 0;
        for (; ; ) {
            if (i >= arrayOfInt.length) {
                return arrayOfInt;
            }
            arrayOfInt[i][0] = paramArrayOfInt[i][1];
            arrayOfInt[i][3] = paramArrayOfInt[i][3];
            arrayOfInt[i][4] = paramArrayOfInt[i][4];
            int j = paramArrayOfInt[i][2];
            int[] arrayOfInt1 = weekTransBackTo(paramArrayOfInt[i][2] & 0x7F);
            int k = DevOperation_X6.weekTransform(0, arrayOfInt1[0], arrayOfInt1[1], arrayOfInt1[2], arrayOfInt1[3], arrayOfInt1[4], arrayOfInt1[5], arrayOfInt1[6]);
            arrayOfInt[i][1] = ((j & 0x80) >>> 7);
            arrayOfInt[i][2] = k;
            i += 1;
        }
    }


    public void switchHistoryDataToX5_X6() {
        for (; ; ) {
            int i2;
            try {
                this.dev_Data_AWeekData_date = new String[7];
                this.dev_Data_AWeek_Steps = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 24}));
                this.dev_Data_AWeek_Distance = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{7, 24}));
                int i = 0;
                if (i >= 7) {
                    broadcastUpdate("com.vidonn2.bluetooth.le.ACTION_READ_HISTORY_SPORT");
                    return;
                }
                this.dev_Data_AWeekData_date[i] = (historyDate_Map[i][1] + "-" + historyDate_Map[i][2] + "-" + historyDate_Map[i][3]);
                int m = 0;
                int j = 0;
                int k = 0;
                if (k >= 24) {
                    k = MyAplication.userWeight * j / 965;
                    System.out.println("历史数据转换calories:" + (0 + k) + "  distance=" + j + " 原始步数=" + m);
                    i += 1;
                    continue;
                    if (n >= historyDetail_Steps_All[i][k].length) {
                        m += i1;
                        if (i1 > 0) {
                            this.dev_Data_AWeek_Steps[i][k] = i1;
                            this.dev_Data_AWeek_Distance[i][k] = (this.dev_Data_AWeek_Steps[i][k] * MyAplication.userHeight / 241);
                            j += this.dev_Data_AWeek_Distance[i][k];
                            k += 1;
                        }
                    } else {
                        i2 = i1;
                        if (historyDetail_Steps_All[i][k][n][0] != 1) {
                            break label397;
                        }
                        i2 = i1 + historyDetail_Steps_All[i][k][n][1];
                        break label397;
                    }
                    this.dev_Data_AWeek_Steps[i][k] = MyAplication.dev_Data_AWeek_Steps[i][k];
                    continue;
                }
                i1 = 0;
            } catch (Exception localException) {
                System.out.println("数据转换异常=" + localException.toString());
                return;
            }
            int n = 0;
            continue;
            label397:
            n += 1;
            int i1 = i2;
        }
    }


    public int toInt(byte[] paramArrayOfByte) {
        int j = 0;
        int i = 0;
        for (; ; ) {
            if (i >= 4) {
                return j;
            }
            j = j << 8 | paramArrayOfByte[i] & 0xFF;
            i += 1;
        }
    }


    public int[] weekTransBackTo(int paramInt) {
        return new int[]{paramInt >> 6 & 0x1, paramInt >> 5 & 0x1, paramInt >> 4 & 0x1, paramInt >> 3 & 0x1, paramInt >> 2 & 0x1, paramInt >> 1 & 0x1, paramInt & 0x1};
    }


    public int weekTransform(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7) {
        return 0x0 | paramInt1 << 6 | paramInt2 << 5 | paramInt3 << 4 | paramInt4 << 3 | paramInt5 << 2 | paramInt6 << 1 | paramInt7;
    }


    public void writeAlarmClockData(int paramInt, int[][] paramArrayOfInt) {
        if (MyAplication.DevType == 0) {
            this.devOperation.writeAlarmClockData(paramInt, paramArrayOfInt);
            return;
        }
        System.out.println("执行任务   写入闹钟");
        this.writeAlarmClock_ID = 0;
        int i2 = paramArrayOfInt[this.writeAlarmClock_ID][3];
        int i3 = paramArrayOfInt[this.writeAlarmClock_ID][4];
        int i1 = paramArrayOfInt[this.writeAlarmClock_ID][0] - 1;
        paramInt = i1;
        if (i1 != 0) {
            paramInt = i1;
            if (i1 != 1) {
                paramInt = 0;
            }
        }
        int i = (byte) this.writeAlarmClock_ID;
        int j = (byte) paramInt;
        int[] arrayOfInt = weekTransBackTo(paramArrayOfInt[this.writeAlarmClock_ID][2]);
        int k = (byte) DevOperation_X6.weekTransform(paramArrayOfInt[this.writeAlarmClock_ID][1], arrayOfInt[0], arrayOfInt[1], arrayOfInt[2], arrayOfInt[3], arrayOfInt[4], arrayOfInt[5], arrayOfInt[6]);
        int m = (byte) i2;
        int n = (byte) i3;
        OpCode_Current = 106;
        this.devOperation_X6.writeAlarmClock(new byte[]{i, j, k, m, n, 0});
    }


    public void writeCurrentDate() {
        try {
            if (MyAplication.DevType == 0) {
                this.devOperation.writeCurrentDate();
                return;
            }
            OpCode_Current = 52;
            this.devOperation_X6.writeDate_Time();
            return;
        } catch (Exception localException) {
            System.out.println("Bluetooth-写入当前时间异常------------");
        }
    }


    public void writeNotificationTitle(byte paramByte1, byte paramByte2) {
        this.flag_IsMessageComing = true;
        this.flag_IsMessageTitle = true;
        if (MyAplication.isAppStart) {
            if (OpCode_Current == 0) {
                this.devOperation_X6.writerNotification(paramByte1, paramByte2);
                OpCode_Current = 500;
                return;
            }
            this.message_EventFlag = paramByte1;
            this.message_CategoryID = paramByte2;
            return;
        }
        this.devOperation_X6.writerNotification(paramByte1, paramByte2);
        OpCode_Current = 500;
    }


    public void writePersonalData_Auto(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5) {
        if (MyAplication.DevType == 0) {
            this.devOperation.writePersonalData(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
            return;
        }
        this.personalInfo_Goal = paramInt5;
        this.writePersonalInfoType = 1;
        OpCode_Current = 104;
        this.devOperation_X6.writePersonalInfo((byte) 1, new byte[]{(byte) paramInt1, (byte) paramInt2, (byte) paramInt3, (byte) paramInt4});
    }


    public void writePersonalData_HandUpLight(int paramInt) {
        OpCode_Current = 19;
        this.devOperation_X6.writePersonalInfo((byte) 9, new byte[]{(byte) paramInt});
    }


    public void writePersonalData_Language(int paramInt) {
        OpCode_Current = 17;
        this.devOperation_X6.writePersonalInfo((byte) 7, new byte[]{(byte) paramInt});
    }


    public void writePersonalData_Other(int paramInt1, int paramInt2, int paramInt3) {
        OpCode_Current = 16;
        this.devOperation_X6.writePersonalInfo((byte) 5, new byte[]{(byte) paramInt1, (byte) paramInt2, (byte) paramInt3});
    }


    public void writePersonalData_Personal(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5) {
        OpCode_Current = 11;
        this.devOperation_X6.writePersonalInfo((byte) 1, new byte[]{(byte) paramInt1, (byte) paramInt2, (byte) paramInt3, (byte) paramInt4});
    }


    public void writePersonalData_ScreenTurnOver(int paramInt) {
        OpCode_Current = 18;
        this.devOperation_X6.writePersonalInfo((byte) 8, new byte[]{(byte) paramInt});
    }


    public void writePersonalData_Sedentary() {
        try {
            OpCode_Current = 12;
            byte[] arrayOfByte = DevDecode_X6.switchBytes(DevOperation_X6.int2Bytes_2Bytes(MyAplication.userInfo.getSedentary() * 60));
            this.devOperation_X6.writePersonalInfo((byte) 2, arrayOfByte);
            return;
        } catch (Exception localException) {
        }
    }


    public void writePersonalData_Silent(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5) {
        OpCode_Current = 14;
        this.devOperation_X6.writePersonalInfo((byte) 6, new byte[]{(byte) paramInt1, (byte) paramInt2, (byte) paramInt3, (byte) paramInt4, (byte) paramInt5});
    }


    public void writePersonalData_Sleep(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        OpCode_Current = 13;
        this.devOperation_X6.writePersonalInfo((byte) 4, new byte[]{(byte) paramInt1, (byte) paramInt2, (byte) paramInt3, (byte) paramInt4});
    }


    public void writePersonalData_TempMode(int paramInt) {
        OpCode_Current = 20;
        this.devOperation_X6.writePersonalInfo((byte) 11, new byte[]{(byte) paramInt});
    }


    public void writeSpecialNotice(int paramInt) {
        System.out.println("writeSpecialNotice：" + paramInt);
        if (MyAplication.DevType == 0) {
            this.devOperation.writeSpecialNotice(paramInt);
            return;
        }
        this.devOperation_X6.writerAlert((byte) paramInt);
    }


    public class LocalBinder
            extends Binder {
        public LocalBinder() {
        }


        public BluetoothLeService_Vidonn2 getService() {
            return BluetoothLeService_Vidonn2.this;
        }
    }
}


/* Location:              /Users/wooks/Downloads/dex2jar-2.0/vidonn-dex2jar.jar!/com/sz/vidonn2/bluetooth/service/BluetoothLeService_Vidonn2.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */