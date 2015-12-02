package com.rainbow.kam.bt_scanner.adapter.dev.detail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.dev.DetailActivity;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = DetailActivity.TAG + " - " + getClass().getSimpleName();

    private static final int TYPE_SERVICE = 0;
    private static final int TYPE_CHARACTERISTIC = 1;

    private ArrayList<ServiceItem> serviceItemArrayList;
    private ArrayList<CharacteristicItem> characteristicItemArrayList;
    private boolean isRoot = true;
    private View view;

    private ServiceViewHolder selectServiceViewHolder = null;
    private Handler handler;

    public DetailAdapter(ArrayList<CharacteristicItem> characteristicItemArrayList) {
        this.characteristicItemArrayList = characteristicItemArrayList;
        this.isRoot = false;

    }

    public DetailAdapter(ArrayList<ServiceItem> serviceItemArrayList, boolean isRoot) {
        this.serviceItemArrayList = serviceItemArrayList;
        this.isRoot = isRoot;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        handler = DetailActivity.handler;
        switch (viewType) {
            case TYPE_SERVICE:
                view = layoutInflater.inflate(R.layout.detail_bluetooth_service_item, parent, false);
                return new ServiceViewHolder(view);

            case TYPE_CHARACTERISTIC:
                view = layoutInflater.inflate(R.layout.detail_bluetooth_characteristics_item, parent, false);
                return new CharacteristicViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_SERVICE:
                ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
                serviceViewHolder.serviceTitle.setText(serviceItemArrayList.get(position).getTitle());
                serviceViewHolder.serviceUuid.setText(serviceItemArrayList.get(position).getUuid());
                serviceViewHolder.serviceType.setText(serviceItemArrayList.get(position).getType());


                String title = BLEGattAttributes.getService(serviceViewHolder.serviceUuid.getText().toString().substring(0, 8));
                serviceViewHolder.serviceTitle.setText(title);
                serviceViewHolder.serviceUuid.setText("UUID : " + "0x" + serviceViewHolder.serviceUuid.getText().toString().substring(4, 8).toUpperCase());


                break;
            case TYPE_CHARACTERISTIC:
                CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
                characteristicViewHolder.characteristicTitle.setText(characteristicItemArrayList.get(position).getTitle());
                characteristicViewHolder.characteristicUuid.setText(characteristicItemArrayList.get(position).getUuid());
                characteristicViewHolder.characteristicValue.setText(characteristicItemArrayList.get(position).getValue());
                break;
        }
    }

    public ServiceViewHolder getServiceViewHolder() {
        if (selectServiceViewHolder != null) {
            return selectServiceViewHolder;
        } else {
            Log.e(TAG, "selecteServiceViewHolder is null");
            return null;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (isRoot) {
            return TYPE_SERVICE;
        } else {
            return TYPE_CHARACTERISTIC;
        }
    }

    @Override
    public int getItemCount() {
        if (getItemViewType(0) == 0) {
            return serviceItemArrayList.size();
        } else {
            return characteristicItemArrayList.size();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearList(int position) {
        switch (getItemViewType(position)) {
            case TYPE_SERVICE:
                serviceItemArrayList.clear();
                break;
            case TYPE_CHARACTERISTIC:
                characteristicItemArrayList.clear();
                break;
        }
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        private TextView serviceTitle;
        private TextView serviceUuid;
        private TextView serviceType;
        private View view;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectServiceViewHolder = ServiceViewHolder.this;
                    Message message = Message.obtain(handler, 1, getLayoutPosition(), 0);
                    handler.sendMessage(message);
                }
            });
            serviceTitle = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_title);
            serviceUuid = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_UUID);
            serviceType = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_type);
        }

        /*
                public Bundle getParams() {
                    Bundle bundle = new Bundle();
                    bundle.putFloat("X", view.getX());
                    bundle.putFloat("Y", view.getY());
                    return bundle;
                }
        */
        public View getView() {
            return view;
        }
    }

    private class CharacteristicViewHolder extends RecyclerView.ViewHolder {

        private TextView characteristicTitle;
        private TextView characteristicUuid;
        private TextView characteristicValue;

        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, getLayoutPosition() + "");
                    Message message = Message.obtain(DetailActivity.handler, 2, getLayoutPosition(), 0);
                    handler.sendMessage(message);
                }
            });
            characteristicTitle = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_title);
            characteristicUuid = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_UUID);
            characteristicValue = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_value);
        }
    }
}
