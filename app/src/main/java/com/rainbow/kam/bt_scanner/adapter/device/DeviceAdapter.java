package com.rainbow.kam.bt_scanner.adapter.device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;

import java.util.Objects;

import hugo.weaving.DebugLog;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    private final OnDeviceSelectListener onDeviceSelectListener;

    private final SortedListAdapterCallback<DeviceItem> sortedListAdapterCallback = new SortedListAdapterCallback<DeviceItem>(this) {
        @Override
        public int compare(DeviceItem deviceItem1, DeviceItem deviceItem2) {
            return deviceItem1.getExtraAddress().compareTo(deviceItem2.getExtraAddress());
        }


        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }


        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }


        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }


        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }


        @Override
        public boolean areContentsTheSame(DeviceItem oldDeviceItem, DeviceItem newDeviceItem) {
            return oldDeviceItem.getExtraAddress().equals(newDeviceItem.getExtraAddress());
        }


        @Override
        public boolean areItemsTheSame(DeviceItem DeviceItem1, DeviceItem DeviceItem2) {
            return Objects.equals(DeviceItem1.getExtraAddress(), DeviceItem2.getExtraAddress());
        }
    };

    private final SortedList<DeviceItem> sortedList = new SortedList<>(DeviceItem.class, sortedListAdapterCallback);


    public DeviceAdapter(Context context) {
        this.context = context;
        this.onDeviceSelectListener = (OnDeviceSelectListener) context;
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
        deviceViewHolder.bindViews(sortedList.get(position));
    }


    @Override
    public int getItemCount() {
        return sortedList.size();
    }


    @DebugLog
    public void addDevice(final BluetoothDevice bluetoothDevice, final int rssi) {
        sortedList.add(new DeviceItem(bluetoothDevice, rssi));
    }


    @DebugLog
    public void clear() {
        sortedList.clear();
    }


    public void cancel() {
        onDeviceSelectListener.onDeviceUnSelected();
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Animation.AnimationListener { //뷰 초기화

        private DeviceItem deviceItem;

        private final TextView extraName, extraAddress, extraBondState, extraType, extraRssi;

        private final CardView deviceItemCardView;


        private final TableRow[] expendRowGroup = new TableRow[3];
        private final Animation expandAnimation, collapseAnimation;

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

            expendRowGroup[0] = (TableRow) itemView.findViewById(R.id.row_type);
            expendRowGroup[1] = (TableRow) itemView.findViewById(R.id.row_bond);
            expendRowGroup[2] = (TableRow) itemView.findViewById(R.id.row_rssi);

            expandAnimation = AnimationUtils.loadAnimation(context, R.anim.expand_device_item);
            expandAnimation.setAnimationListener(this);
            collapseAnimation = AnimationUtils.loadAnimation(context, R.anim.collapse_device_item);
            collapseAnimation.setAnimationListener(this);

            expendImageView = (ImageView) itemView.findViewById(R.id.item_expend);
            expendImageView.setColorFilter(ContextCompat.getColor(context, android.R.color.black));
            expendImageView.setOnClickListener(this);
        }


        private void bindViews(DeviceItem deviceItem) {
            this.deviceItem = deviceItem;
            String deviceName = this.deviceItem.getExtraName();
            if (deviceName == null) {
                deviceName = context.getString(R.string.device_name_def);
            }
            extraName.setText(deviceName);
            extraAddress.setText(this.deviceItem.getExtraAddress());
            extraBondState.setText(String.valueOf(this.deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(this.deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(this.deviceItem.getExtraRssi()));

            for (TableRow tableRow : expendRowGroup) {
                tableRow.setVisibility(View.GONE);
            }
            expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);
        }


        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.item_expend) {
                if (expendRowGroup[0].isShown()) {
                    collapsedView();
                } else {
                    expandView();
                }
            } else {
                onDeviceSelectListener.onDeviceSelect(deviceItem.getExtraName(), deviceItem.getExtraAddress());
            }
        }


        void expandView() {
            expandAnimation.reset();
            for (TableRow tableRow : expendRowGroup) {
                tableRow.clearAnimation();
                tableRow.startAnimation(expandAnimation);
            }
        }


        void collapsedView() {
            collapseAnimation.reset();
            for (TableRow tableRow : expendRowGroup) {
                tableRow.clearAnimation();
                tableRow.startAnimation(collapseAnimation);
            }
        }


        @Override
        public void onAnimationStart(Animation animation) {
            itemView.requestLayout();
            if (animation == expandAnimation) {
                for (TableRow tableRow : expendRowGroup) {
                    tableRow.setVisibility(View.VISIBLE);
                }
                expendImageView.setImageResource(R.drawable.ic_expand_less_white_36dp);
            }
        }


        @Override
        public void onAnimationEnd(Animation animation) {
            itemView.requestLayout();
            if (animation == collapseAnimation) {
                for (TableRow tableRow : expendRowGroup) {
                    tableRow.setVisibility(View.GONE);
                }
                expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);
            }
        }


        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(String name, String address);

        void onDeviceUnSelected();
    }
}