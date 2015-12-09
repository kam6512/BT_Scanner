package com.rainbow.kam.bt_scanner.adapter.dev.detail;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
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
public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = DetailActivity.TAG + " - " + getClass().getSimpleName();

    private static final int TYPE_SERVICE = 0;

    private ArrayList<ServiceItem> serviceItemArrayList;


    private View view;
    private Handler handler = DetailActivity.handler;

    public ServiceAdapter(ArrayList<ServiceItem> serviceItemArrayList) {
        this.serviceItemArrayList = serviceItemArrayList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        view = layoutInflater.inflate(R.layout.detail_bluetooth_service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
        ServiceItem serviceItem = serviceItemArrayList.get(position);
        serviceViewHolder.serviceTitle.setText(serviceItem.getTitle());
        serviceViewHolder.serviceUuid.setText(serviceItem.getUuid());
        serviceViewHolder.serviceType.setText(serviceItem.getType());

        String title = BLEGattAttributes.getService(serviceItem.getUuid().substring(0, 8));
        serviceViewHolder.serviceTitle.setText(title);
        serviceViewHolder.serviceUuid.setText("UUID : " + "0x" + serviceItem.getUuid().substring(4, 8).toUpperCase());
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_SERVICE;
    }

    @Override
    public int getItemCount() {
        return serviceItemArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearList() {
        serviceItemArrayList.clear();
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
                    Message message = Message.obtain(handler, 1, getLayoutPosition(), 0);
                    handler.sendMessage(message);
                }
            });
            serviceTitle = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_title);
            serviceUuid = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_UUID);
            serviceType = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_type);
        }

        public View getView() {
            return view;
        }
    }
}
