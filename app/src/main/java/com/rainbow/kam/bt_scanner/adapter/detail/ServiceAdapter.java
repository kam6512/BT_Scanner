package com.rainbow.kam.bt_scanner.adapter.detail;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.dev.DetailActivity;
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = DetailActivity.TAG + " - " + getClass().getSimpleName();

    private static final int TYPE_SERVICE = 0;

    private final ArrayList<ServiceItem> serviceItemArrayList;

    private OnServiceItemClickListener onServiceItemClickListener;


    public ServiceAdapter(ArrayList<ServiceItem> serviceItemArrayList, Activity activity) {
        this.serviceItemArrayList = serviceItemArrayList;
        try {
            onServiceItemClickListener = (OnServiceItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServiceItemClickListener");
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.detail_bluetooth_service_item, parent, false);
        return new ServiceViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServiceViewHolder serviceViewHolder = (ServiceViewHolder) holder;
        ServiceItem serviceItem = serviceItemArrayList.get(position);
        serviceViewHolder.serviceTitle.setText(serviceItem.getTitle());
        serviceViewHolder.serviceUuid.setText(serviceItem.getUuid());
        serviceViewHolder.serviceType.setText(serviceItem.getType());

        String title = GattAttributes.getService(serviceItem.getUuid().substring(0, 8));
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

        private final TextView serviceTitle;
        private final TextView serviceUuid;
        private final TextView serviceType;


        public ServiceViewHolder(View itemView) {
            super(itemView);
            serviceTitle = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_title);
            serviceUuid = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_UUID);
            serviceType = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_type);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onServiceItemClickListener.onServiceItemClick(getLayoutPosition());
                }
            });
        }
    }


    public interface OnServiceItemClickListener {
        void onServiceItemClick(int position);
    }
}
