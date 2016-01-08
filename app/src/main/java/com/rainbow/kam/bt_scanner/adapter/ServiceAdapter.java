package com.rainbow.kam.bt_scanner.adapter;

import android.bluetooth.BluetoothGattService;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = getClass().getSimpleName();


    private final ArrayList<BluetoothGattService> serviceItemArrayList = new ArrayList<>();

    private final OnServiceItemClickListener onServiceItemClickListener;


    public ServiceAdapter(OnServiceItemClickListener onServiceItemClickListener) {
        this.onServiceItemClickListener = onServiceItemClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_profile_bluetooth_service, parent, false);
        return new ServiceViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
        serviceViewHolder.bindViews(serviceItemArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return serviceItemArrayList.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    public void add(BluetoothGattService bluetoothGattService) {
        serviceItemArrayList.add(bluetoothGattService);
    }


    public class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView serviceTitle;
        private final TextView serviceUuid;
        private final TextView serviceType;


        public ServiceViewHolder(View itemView) {
            super(itemView);
            serviceTitle = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_name);
            serviceUuid = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_UUID);
            serviceType = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_type);
            itemView.setOnClickListener(this);
        }


        private void bindViews(BluetoothGattService bluetoothGattService) {
            String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = GattAttributes.resolveServiceName(uuid.substring(0, 8));
            uuid = "UUID : 0x" + uuid.substring(4, 8);
            String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? "primary" : "secondary";

            serviceTitle.setText(name);
            serviceUuid.setText(uuid);
            serviceType.setText(type);
        }


        @Override
        public void onClick(View v) {
            onServiceItemClickListener.onServiceItemClick(getLayoutPosition());
        }
    }


    public interface OnServiceItemClickListener {
        void onServiceItemClick(int position);
    }
}
