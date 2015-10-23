package com.rainbow.kam.bt_scanner.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Activity.DetailActivity;
import com.rainbow.kam.bt_scanner.BluetoothPackage.BluetoothService;
import com.rainbow.kam.bt_scanner.BluetoothPackage.DetailGatt;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.ServiceCheck;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //태그
    private static final String TAG = "DeviceAdapter";

    //고정 네임
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private ArrayList<DeviceItem> deviceItemArrayList; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private Activity activity; //액티비티
    private View view; //SnackBar대비 뷰
    private int listLength;
    private Device[] devices;

    private boolean isServiceHandling = false;

    public static SimpleExpandableListAdapter[] simpleExpandableListAdapter = null;
    public static ExpandableListView.OnChildClickListener[] onChildClickListener = null;

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Activity activity, Context context, View view, int listLength) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.listLength = listLength;
        simpleExpandableListAdapter = new SimpleExpandableListAdapter[this.listLength];
        onChildClickListener = new ExpandableListView.OnChildClickListener[this.listLength];
        devices = new Device[this.listLength];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new Device(LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false));

        Device device;
        View root;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        root = layoutInflater.inflate(R.layout.bluetooth_device_item, parent, false);
        device = new Device(root);
        return device;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        //뷰홀더 적용
        devices[position] = (Device) holder;

//        //각각의 뷰 속성 적용
//        devices[position].extraName.setText(deviceItemArrayList.get(position).getExtraName());
//        devices[position].extraAddress.setText(deviceItemArrayList.get(position).getExtraextraAddress());
//        devices[position].extraBondState.setText(String.valueOf(deviceItemArrayList.get(position).getExtraBondState()));
//        devices[position].extraType.setText(String.valueOf(deviceItemArrayList.get(position).getExtraType()));
//        devices[position].extraRssi.setText(String.valueOf(deviceItemArrayList.get(position).getExtraRssi()));

        devices[position].bindViews(deviceItemArrayList.get(position));

    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
//        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        //디바이스의 커넥션 OFF
        devices[holder.getLayoutPosition()].stopConnect();
    }

    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class Device extends RecyclerView.ViewHolder { //뷰 초기화

        private DetailGatt detailGatt;

//        public ExpandableListView expandableListView;

        private CardView cardView;

        public TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        public TextView state;
        public TextView address;
        public TextView dataField;

        private Handler handler;
        private Runnable runnable;


        public Device(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card);

//            expandableListView = (ExpandableListView) itemView.findViewById(R.id.gatt_services_list);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            state = (TextView) itemView.findViewById(R.id.detail_state);
            address = (TextView) itemView.findViewById(R.id.detail_address);
            dataField = (TextView) itemView.findViewById(R.id.detail_datafield);


            //핸들러
            handler = new Handler();

            //자동시작
            runnable = new Runnable() {
                @Override
                public void run() {
                    boolean isServiceRun = new ServiceCheck().BtnSVC_Run(context, "com.rainbow.kam.bt_scanner.BluetoothPackage.BluetoothService");
                    if (isServiceRun || isServiceHandling) {
                        Log.e(TAG, "is Service Ruuning " + getLayoutPosition());
                        if (extraName.getText().toString() == "") {
                            extraName.setText("Gatt 서비스 리소스를 기다리는 중...");
                        }
                        if (!(getLayoutPosition() < 0)) {
                            handler.postDelayed(runnable, 2000);
                        }


                    } else {
                        isServiceHandling = true;
                        Log.e(TAG, "is Service Start " + getLayoutPosition());

                        if (extraName.getText().toString() == "") {
                            extraName.setText("BLE 네임정보를 가져오는 중....");
                        }

                        startConnect();
                    }
                }
            };
            handler.postDelayed(runnable, 2000);


            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((detailGatt != null) && detailGatt.isDataFind) {
                        if (simpleExpandableListAdapter == null || onChildClickListener == null) {
                            Log.e(TAG, "hasNull");
                        } else {
//                            DetailActivity detailActivity = new DetailActivity(simpleExpandableListAdapter, onChildClickListener);
////                            activity.startActivity(new Intent(activity, detailActivity.getClass()));
//                             context.startActivity(new Intent(context,detailActivity.getClass()));

                            Intent i = new Intent(context, DetailActivity.class);
                            i.putExtra("position", getLayoutPosition());
                            context.startActivity(i);
                        }


                    }

                }
            });
        }

        public void bindViews(DeviceItem deviceItem) {
            //각각의 뷰 속성 적용
            extraName.setText(deviceItem.getExtraName());
            extraAddress.setText(deviceItem.getExtraextraAddress());
            extraBondState.setText(String.valueOf(deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(deviceItem.getExtraRssi()));

        }


        private void startConnect() {
            if (extraAddress.getText() != null) {


                //일단 null이 아니면 커넥션을 끊고
                if (detailGatt != null) {
                    detailGatt.destroy();
                    dataField.setText("No Data");
                    state.setText("disconnected");
                }

                // 커넥션
                try {
                    detailGatt = new DetailGatt(activity, context, devices[getLayoutPosition()], extraName.getText().toString(), extraAddress.getText().toString());
                    detailGatt.init();
                } catch (Exception e) {

                }

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (detailGatt != null && detailGatt.isBluetoothServiceClosed == true) {
                            if (!detailGatt.isDataFind) {

                                startConnect();
                            }
                        } else {
                            startConnect();

                        }
                    }
                };
                if (!detailGatt.isDataFind) {
                    //정보를 받아 오지 못하면 핸들러로 5초마다 재 커넥션
                    new Handler().postDelayed(runnable, 5000);
                } else {


                    isServiceHandling = false;
                }
            }
        }

        //커넥션 OFF
        private void stopConnect() {
            if (detailGatt != null) {
                Log.e(TAG, " stopConnect ");
                detailGatt.destroy();
                handler.removeCallbacks(runnable);

            }
        }

        public void stopHandling() {
            Log.e(TAG, " stopHandling ");
            if (simpleExpandableListAdapter == null) {
                Log.e(TAG, " simpleExpandableListAdapter is null ");
            } else if (onChildClickListener == null) {
                Log.e(TAG, " onChildClickListener is null ");
            } else if (detailGatt.getGattServiceAdapter() == null) {
                Log.e(TAG, " onChildClickListener is null ");
            } else if (detailGatt.getOnChildClickListener() == null) {
                Log.e(TAG, " onChildClickListener is null ");
            } else {
                simpleExpandableListAdapter[getLayoutPosition()] = detailGatt.getGattServiceAdapter();
                onChildClickListener[getLayoutPosition()] = detailGatt.getOnChildClickListener();
            }


            isServiceHandling = false;
            stopConnect();
        }


    }
}
