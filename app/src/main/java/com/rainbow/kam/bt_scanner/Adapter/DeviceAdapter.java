package com.rainbow.kam.bt_scanner.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.Activity.DetailActivity;
import com.rainbow.kam.bt_scanner.R;

import java.util.ArrayList;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DeviceItem> deviceItemArrayList; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private View view; //SnackBar대비 뷰

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Context context, View view) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.context = context;
        this.view = view;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Device(LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //뷰홀더 적용
        Device device = (Device) holder;

        //각각의 뷰 속성 적용
        device.extraName.setText(deviceItemArrayList.get(position).getExtraName());
        device.extraAddress.setText(deviceItemArrayList.get(position).getExtraextraAddress());
        device.extraBondState.setText(String.valueOf(deviceItemArrayList.get(position).getExtraBondState()));
        device.extraType.setText(String.valueOf(deviceItemArrayList.get(position).getExtraType()));
        device.extraRssi.setText(String.valueOf(deviceItemArrayList.get(position).getExtraRssi()));
    }

    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    private class Device extends RecyclerView.ViewHolder { //뷰 초기화

        private CardView cardView;

        private TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        public Device(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //카드 뷰 클릭시 세부 액티비티로
                    final Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(DetailActivity.DEVICE_NAME, extraName.getText().toString()); //이름
                    intent.putExtra(DetailActivity.DEVICE_ADDRESS, extraAddress.getText().toString()); //주소
                    context.startActivity(intent);
                }
            });

        }
    }
}
