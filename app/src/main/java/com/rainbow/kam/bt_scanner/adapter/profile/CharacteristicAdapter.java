package com.rainbow.kam.bt_scanner.adapter.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    private final ArrayList<BluetoothGattCharacteristic> characteristicArrayList = new ArrayList<>();

    private final OnCharacteristicItemClickListener onCharacteristicItemClickListener;


    public CharacteristicAdapter(Context context) {
        this.context = context;
        this.onCharacteristicItemClickListener = (OnCharacteristicItemClickListener) context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_profile_bluetooth_characteristics, parent, false);
        return new CharacteristicViewHolder(view);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
        characteristicViewHolder.bindViews(characteristicArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return characteristicArrayList.size();
    }


    @DebugLog
    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        if (!characteristicArrayList.equals(bluetoothGattCharacteristics)) {
            characteristicArrayList.clear();
            characteristicArrayList.addAll(bluetoothGattCharacteristics);
            notifyDataSetChanged();
        }
    }


    private class CharacteristicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView characteristicTitle;
        private final TextView characteristicUuid;


        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            characteristicTitle = (TextView) itemView.findViewById(R.id.profile_child_list_item_characteristics_name);
            characteristicUuid = (TextView) itemView.findViewById(R.id.profile_child_list_item_characteristics_UUID);
            itemView.setOnClickListener(this);
        }


        private void bindViews(BluetoothGattCharacteristic characteristicItem) {

            String uuid = characteristicItem.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = GattAttributes.resolveCharacteristicName(uuid.substring(0, 8));
            uuid = context.getString(R.string.profile_uuid_label) + uuid.substring(4, 8);

            characteristicTitle.setText(name);
            characteristicUuid.setText(uuid);
        }


        @Override
        public void onClick(View v) {
            onCharacteristicItemClickListener.onCharacteristicItemClick(getLayoutPosition());
        }
    }


    public interface OnCharacteristicItemClickListener {
        void onCharacteristicItemClick(int position);
    }
}
