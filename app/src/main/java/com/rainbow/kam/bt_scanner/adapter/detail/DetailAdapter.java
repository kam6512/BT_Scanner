package com.rainbow.kam.bt_scanner.adapter.detail;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.DetailActivity;
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

    private ServiceViewHolder selecteServiceViewHolder = null;

    public DetailAdapter(ArrayList<ServiceItem> serviceItemArrayList, boolean isRoot) {
        this.serviceItemArrayList = serviceItemArrayList;
        this.isRoot = isRoot;
    }

    public DetailAdapter(ArrayList<CharacteristicItem> characteristicItemArrayList) {
        this.characteristicItemArrayList = characteristicItemArrayList;
        this.isRoot = false;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
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
                serviceViewHolder.service_title.setText(serviceItemArrayList.get(position).getTitle());
                serviceViewHolder.service_uuid.setText(serviceItemArrayList.get(position).getUuid());
                serviceViewHolder.service_type.setText(serviceItemArrayList.get(position).getType());


                String title = BLEGattAttributes.getService(serviceViewHolder.service_uuid.getText().toString().substring(0, 8));
                serviceViewHolder.service_title.setText(title);
                serviceViewHolder.service_uuid.setText("UUID : " + "0x" + serviceViewHolder.service_uuid.getText().toString().substring(4, 8).toUpperCase());


                break;
            case TYPE_CHARACTERISTIC:
                CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
                characteristicViewHolder.characteristic_title.setText(characteristicItemArrayList.get(position).getTitle());
                characteristicViewHolder.characteristic_uuid.setText(characteristicItemArrayList.get(position).getUuid());
                characteristicViewHolder.characteristic_value.setText(characteristicItemArrayList.get(position).getValue());
                break;
        }
    }

    public ServiceViewHolder getServiceViewHolder() {
        if (selecteServiceViewHolder != null) {
            return selecteServiceViewHolder;
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

        private TextView service_title;
        private TextView service_uuid;
        private TextView service_type;
        private View view;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selecteServiceViewHolder = ServiceViewHolder.this;
                    Message message = Message.obtain(DetailActivity.handler, 1, getLayoutPosition(),0);
                    DetailActivity.handler.sendMessage(message);
                }
            });
            service_title = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_title);
            service_uuid = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_UUID);
            service_type = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_type);
        }

        public Bundle getParams() {
            Bundle bundle = new Bundle();
            bundle.putFloat("X", view.getX());
            bundle.putFloat("Y", view.getY());
            return bundle;
        }

        public View getView() {
            return view;
        }
    }

    private class CharacteristicViewHolder extends RecyclerView.ViewHolder {

        private TextView characteristic_title;
        private TextView characteristic_uuid;
        private TextView characteristic_value;

        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, getLayoutPosition()+"");
                    Message message = Message.obtain(DetailActivity.handler, 2, getLayoutPosition(),0);
                    DetailActivity.handler.sendMessage(message);
                }
            });
            characteristic_title = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_title);
            characteristic_uuid = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_UUID);
            characteristic_value = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_value);
        }
    }
}
