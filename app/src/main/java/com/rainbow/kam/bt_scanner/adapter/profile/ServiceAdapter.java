package com.rainbow.kam.bt_scanner.adapter.profile;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<BluetoothGattService> serviceItemArrayList = Lists.newArrayList();

    private final OnServiceItemClickListener onServiceItemClickListener;


    public ServiceAdapter(Context context) {
        this.onServiceItemClickListener = (OnServiceItemClickListener) context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_profile_bluetooth_service, parent, false);
        return new ServiceViewHolder(view);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
        serviceViewHolder.bindViews(serviceItemArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return serviceItemArrayList.size();
    }


    @DebugLog
    public void setServiceList(List<BluetoothGattService> bluetoothGattServices) {
        if (getItemCount() == 0) {
            // 서비스는 한 기기에서 오직 1개의 리스트만 있고 변경되지 않으므로 한번 가져오고 난 뒤에는 가져올 일이없다
            serviceItemArrayList.addAll(bluetoothGattServices);
            notifyDataSetChanged();
        }
    }


    class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.profile_parent_list_item_service_name)
        TextView serviceTitle;

        @Bind(R.id.profile_parent_list_item_service_UUID)
        TextView serviceUuid;

        @Bind(R.id.profile_parent_list_item_service_type)
        TextView serviceType;

        @BindString(R.string.profile_uuid_label) String uuidLabel;


        public ServiceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }


        private void bindViews(BluetoothGattService bluetoothGattService) {
            String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = GattAttributes.resolveServiceName(uuid.substring(0, 8));
            uuid = uuidLabel + uuid.substring(4, 8);
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
