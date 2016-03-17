package com.rainbow.kam.bt_scanner.adapter.device;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.vo.DeviceVo;

import java.util.Objects;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Animation.AnimationListener {


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

    private final Animation expandAnimation, collapseAnimation;

    private DeviceViewHolder animateDeviceViewHolder;


    public DeviceAdapter(Context context) {
        this.context = context;
        this.onDeviceSelectListener = (OnDeviceSelectListener) context;

        expandAnimation = AnimationUtils.loadAnimation(context, R.anim.expand_device_item);
        expandAnimation.setAnimationListener(this);
        expandAnimation.setInterpolator(context, android.R.anim.anticipate_overshoot_interpolator);
        collapseAnimation = AnimationUtils.loadAnimation(context, R.anim.collapse_device_item);
        collapseAnimation.setAnimationListener(this);
        collapseAnimation.setInterpolator(context, android.R.anim.anticipate_overshoot_interpolator);

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.i_bluetooth_device, parent, false);
        return new DeviceViewHolder(root);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(sortedList.get(position));
    }

    @Override
    public int getItemCount() {
        return sortedList.size();
    }


    public void addDevice(final DeviceItem deviceItem) {
        sortedList.add(deviceItem);
    }


    public void clear() {
        sortedList.clear();
    }


    public void cancel() {
        onDeviceSelectListener.onDeviceUnSelected();
    }


    @Override
    public void onAnimationStart(Animation animation) {
        animateDeviceViewHolder.itemView.requestLayout();
        if (animation == expandAnimation) {
            animateDeviceViewHolder.expandGroup.setVisibility(View.VISIBLE);

            animateDeviceViewHolder.expendImageView.setImageResource(R.drawable.ic_expand_less_white_36dp);
        }
    }


    @Override
    public void onAnimationEnd(Animation animation) {
        animateDeviceViewHolder.itemView.requestLayout();
        if (animation == collapseAnimation) {
            animateDeviceViewHolder.expandGroup.setVisibility(View.GONE);
            animateDeviceViewHolder.expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);
        }
    }


    @Override
    public void onAnimationRepeat(Animation animation) {
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder { //뷰 초기화

        private DeviceItem deviceItem;
        @Bind(R.id.item_name) TextView extraName;
        @Bind(R.id.item_address) TextView extraAddress;
        @Bind(R.id.item_bond) TextView extraBondState;
        @Bind(R.id.item_type) TextView extraType;
        @Bind(R.id.item_rssi) TextView extraRssi;

        @Bind(R.id.row_expand) LinearLayout expandGroup;


        @Bind(R.id.button_expand) ImageView expendImageView;

        @BindString(R.string.device_name_def) String defaultDeviceName;


        public DeviceViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            expendImageView.setColorFilter(ContextCompat.getColor(context, android.R.color.black));
        }


        private void bindViews(DeviceItem deviceItem) {
            this.deviceItem = deviceItem;
            String deviceName = this.deviceItem.getExtraName();
            if (Strings.isNullOrEmpty(deviceName)) {
                deviceName = defaultDeviceName;
            }
            extraName.setText(deviceName);
            extraAddress.setText(this.deviceItem.getExtraAddress());
            extraBondState.setText(String.valueOf(this.deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(this.deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(this.deviceItem.getExtraRssi()));

            expandGroup.setVisibility(View.GONE);

            expendImageView.setImageResource(R.drawable.ic_expand_more_white_36dp);


        }


        @OnClick(R.id.device_item_card)
        public void clickDeviceItem() {
            DeviceVo deviceVo = new DeviceVo();
            deviceVo.name = deviceItem.getExtraName();
            deviceVo.address = deviceItem.getExtraAddress();
            onDeviceSelectListener.onDeviceSelect(deviceVo);
        }


        @OnClick(R.id.button_expand)
        public void clickExpandIcon() {
            animateDeviceViewHolder = this;
            if (expandGroup.isShown()) {
                collapsedView();
            } else {
                expandView();
            }
        }


        void expandView() {
            expandAnimation.reset();
            expandGroup.clearAnimation();
            expandGroup.startAnimation(expandAnimation);
        }


        void collapsedView() {
            collapseAnimation.reset();
            expandGroup.clearAnimation();
            expandGroup.startAnimation(collapseAnimation);
        }
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(DeviceVo deviceVo);

        void onDeviceUnSelected();
    }
}