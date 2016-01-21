package com.rainbow.kam.bt_scanner.adapter.device;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.helper.DeviceAdapterHelper;

import java.util.LinkedHashMap;

import hugo.weaving.DebugLog;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DeviceAdapter";

    private final LinkedHashMap<String, DeviceItem> deviceLinkedHashMap = new LinkedHashMap<>();

    private final OnDeviceSelectListener onDeviceSelectListener;

    private final Activity activity;


    public DeviceAdapter(Activity activity) {
        this.activity = activity;
        this.onDeviceSelectListener = (OnDeviceSelectListener) activity;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.i_bluetooth_device, parent, false);
        return new DeviceViewHolder(root);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(DeviceAdapterHelper.getValue(deviceLinkedHashMap, position));
    }


    @Override
    public int getItemCount() {
        return deviceLinkedHashMap.size();
    }


    @DebugLog
    public void addDevice(final BluetoothDevice bluetoothDevice, final int rssi) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!deviceLinkedHashMap.containsKey(bluetoothDevice.getAddress())) {
                    deviceLinkedHashMap.put(bluetoothDevice.getAddress(), new DeviceItem(bluetoothDevice, rssi));
                    notifyDataSetChanged();
                }
            }
        });
    }


    @DebugLog
    public void clear() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceLinkedHashMap.clear();
                notifyDataSetChanged();
            }
        });
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Animation.AnimationListener { //뷰 초기화

        private final TextView extraName;
        private final TextView extraAddress;
        private final TextView extraBondState;
        private final TextView extraType;
        private final TextView extraRssi;

        private final CardView deviceItemCardView;

        private final TableRow type;
        private final TableRow bond;
        private final TableRow rssi;
        private final Animation expandAnimation;
        private final Animation collapseAnimation;

        private final ImageView expendImageView;


        public DeviceViewHolder(final View itemView) {
            super(itemView);
            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            deviceItemCardView = (CardView) itemView.findViewById(R.id.device_item_card);
            deviceItemCardView.setOnClickListener(this);

            type = (TableRow) itemView.findViewById(R.id.row_type);
            bond = (TableRow) itemView.findViewById(R.id.row_bond);
            rssi = (TableRow) itemView.findViewById(R.id.row_rssi);

            expandAnimation = AnimationUtils.loadAnimation(activity, R.anim.expand_device_item);
            expandAnimation.setAnimationListener(this);
            collapseAnimation = AnimationUtils.loadAnimation(activity, R.anim.collapse_device_item);
            collapseAnimation.setAnimationListener(this);

            expendImageView = (ImageView) itemView.findViewById(R.id.item_expend);
            expendImageView.setOnClickListener(this);
            expendImageView.setColorFilter(Color.parseColor("#000000"));
        }


        private void bindViews(DeviceItem deviceItem) {
            String deviceName = deviceItem.getExtraName();
            if (deviceName == null) {
                deviceName = "N/A";
            }
            extraName.setText(deviceName);
            extraAddress.setText(deviceItem.getExtraAddress());
            extraBondState.setText(String.valueOf(deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(deviceItem.getExtraRssi()));


            expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);

            type.setVisibility(View.GONE);
            bond.setVisibility(View.GONE);
            rssi.setVisibility(View.GONE);
        }


        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.item_expend) {
                if (type.isShown() && bond.isShown() && rssi.isShown()) {
                    collapsedView();
                } else {
                    expandView();
                }
            } else {
                final DeviceItem deviceItem = deviceLinkedHashMap.get(extraAddress.getText().toString());
                onDeviceSelectListener.onDeviceSelect(deviceItem.getExtraName(), deviceItem.getExtraAddress());
            }
        }


        void expandView() {
            expandAnimation.reset();
            type.clearAnimation();
            type.startAnimation(expandAnimation);
            bond.clearAnimation();
            bond.startAnimation(expandAnimation);
            rssi.clearAnimation();
            rssi.startAnimation(expandAnimation);
        }


        void collapsedView() {
            collapseAnimation.reset();
            type.clearAnimation();
            type.startAnimation(collapseAnimation);
            bond.clearAnimation();
            bond.startAnimation(collapseAnimation);
            rssi.clearAnimation();
            rssi.startAnimation(collapseAnimation);
        }


        @Override
        public void onAnimationStart(Animation animation) {
            itemView.requestLayout();
            if (animation == expandAnimation) {
                type.setVisibility(View.VISIBLE);
                bond.setVisibility(View.VISIBLE);
                rssi.setVisibility(View.VISIBLE);
                expendImageView.setImageResource(R.drawable.ic_expand_less_white_36dp);
            }
        }


        @Override
        public void onAnimationEnd(Animation animation) {
            itemView.requestLayout();
            if (animation == collapseAnimation) {
                type.setVisibility(View.GONE);
                bond.setVisibility(View.GONE);
                rssi.setVisibility(View.GONE);
                expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);
            }
        }


        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }


    public interface OnDeviceSelectListener {
        void onDeviceSelect(String name, String address);
    }
}