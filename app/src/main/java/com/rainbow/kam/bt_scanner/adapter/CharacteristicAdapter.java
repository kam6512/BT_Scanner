package com.rainbow.kam.bt_scanner.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.development.DeviceProfileActivity;
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = DeviceProfileActivity.TAG + " - " + getClass().getSimpleName();

    private final Activity activity;

    private final ArrayList<BluetoothGattCharacteristic> characteristicArrayList = new ArrayList<>();

    private OnCharacteristicItemClickListener onCharacteristicItemClickListener;


    public CharacteristicAdapter(Activity activity) {
        this.activity = activity;
        try {
            onCharacteristicItemClickListener = (OnCharacteristicItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServiceItemClickListener");
        }
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


    public boolean isListEquals(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return !characteristicArrayList.isEmpty() && bluetoothGattCharacteristic.equals(characteristicArrayList.get(0));
    }


    public void add(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        characteristicArrayList.add(bluetoothGattCharacteristic);
    }


    public void clearList() {
        characteristicArrayList.clear();
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


        private void bindViews(BluetoothGattCharacteristic characteristicItem) {

            String uuid = characteristicItem.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = GattAttributes.resolveCharacteristicName(uuid.substring(0, 8));
            uuid = activity.getString(R.string.detail_label_uuid) + activity.getString(R.string.uuid_unit) + uuid.substring(4, 8);

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


    public void removeListener() {
        onCharacteristicItemClickListener = null;
    }
}
