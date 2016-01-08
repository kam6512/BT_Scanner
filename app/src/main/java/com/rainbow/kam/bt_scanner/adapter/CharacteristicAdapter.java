package com.rainbow.kam.bt_scanner.adapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private final String TAG = getClass().getSimpleName();

    private final ArrayList<BluetoothGattCharacteristic> characteristicArrayList = new ArrayList<>();

    private final OnCharacteristicItemClickListener onCharacteristicItemClickListener;


    public CharacteristicAdapter(OnCharacteristicItemClickListener onCharacteristicItemClickListener) {
        this.onCharacteristicItemClickListener = onCharacteristicItemClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_profile_bluetooth_characteristics, parent, false);
        return new CharacteristicViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
        characteristicViewHolder.bindViews(characteristicArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return characteristicArrayList.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @DebugLog
    public void add(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        if (characteristicArrayList.equals(bluetoothGattCharacteristics)) {
            Log.e("setCharacteristic", "maintain");
        } else {
            characteristicArrayList.clear();
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
                characteristicArrayList.add(bluetoothGattCharacteristic);
            }
            notifyDataSetChanged();
            Log.e("setCharacteristic", "re-add");
        }
    }

    private class CharacteristicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView characteristicTitle;
        private final TextView characteristicUuid;


        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            characteristicTitle = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_name);
            characteristicUuid = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_UUID);
            itemView.setOnClickListener(this);
        }


        @DebugLog
        private void bindViews(BluetoothGattCharacteristic characteristicItem) {

            String uuid = characteristicItem.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = GattAttributes.resolveCharacteristicName(uuid.substring(0, 8));
            uuid = "UUID : 0x" + uuid.substring(4, 8);

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
