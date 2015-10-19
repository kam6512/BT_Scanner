package com.rainbow.kam.bt_scanner.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.Activity.DetailGatt;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.ServiceCheck;

import java.util.ArrayList;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //태그
    private static final String TAG = "DetailGatt";

    //고정 네임
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private ArrayList<DeviceItem> deviceItemArrayList; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private Activity activity; //액티비티
    private View view; //SnackBar대비 뷰
    int listLength;
    Device[] devices;

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Activity activity, Context context, View view, int listLength) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.listLength = listLength;
        devices = new Device[this.listLength];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder");
        return new Device(LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false));

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //뷰홀더 적용
        devices[position] = (Device) holder;

        //각각의 뷰 속성 적용
        devices[position].extraName.setText(deviceItemArrayList.get(position).getExtraName());
        devices[position].extraAddress.setText(deviceItemArrayList.get(position).getExtraextraAddress());
        devices[position].extraBondState.setText(String.valueOf(deviceItemArrayList.get(position).getExtraBondState()));
        devices[position].extraType.setText(String.valueOf(deviceItemArrayList.get(position).getExtraType()));
        devices[position].extraRssi.setText(String.valueOf(deviceItemArrayList.get(position).getExtraRssi()));
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        devices[holder.getLayoutPosition()].stopConnect();
    }

    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    public class Device extends RecyclerView.ViewHolder { //뷰 초기화

        private DetailGatt detailGatt;

        private CardView cardView;

        private TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        public TextView state;
        public TextView address;
        public TextView dataField;

//        private Handler handler;
//        private Runnable runnable;


        public Device(View itemView) {
            super(itemView);


            Log.e(TAG, "leScanCallback  " + "start item");
            cardView = (CardView) itemView.findViewById(R.id.card);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            state = (TextView) itemView.findViewById(R.id.detail_state);
            address = (TextView) itemView.findViewById(R.id.detail_address);
            dataField = (TextView) itemView.findViewById(R.id.detail_datafield);

//            handler = new Handler();
//            runnable = new Runnable() {
//                @Override
//                public void run() {
//                    if (new ServiceCheck().BtnSVC_Run(context, "com.rainbow.kam.BluetoothService")) {
//                        handler.postDelayed(runnable,2000+(getLayoutPosition()*100));
//                    } else {
//                        startConnect();
//                    }
//                }
//            };
//
//            handler.postDelayed(runnable, 2000);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startConnect();
                }
            });



        }

        private void startConnect() {
            if (detailGatt != null) {
                detailGatt.destroy();
                dataField.setText("No Data");
                state.setText("disconnected");
            }



            detailGatt = new DetailGatt(activity, context, devices[getLayoutPosition()], extraName.getText().toString(), extraAddress.getText().toString());
            detailGatt.init();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!detailGatt.connected) {
                        stopConnect();
                        dataField.setText("No Data");
                        state.setText("disconnected");
                        detailGatt.destroy();
                        startConnect();
                    }

                }
            }, 5000);
        }

        private void stopConnect() {
            if (detailGatt != null) {
                detailGatt.destroy();
            }
        }


    }
}
