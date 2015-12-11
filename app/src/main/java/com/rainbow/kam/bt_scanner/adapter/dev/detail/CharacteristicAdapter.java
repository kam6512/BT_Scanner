package com.rainbow.kam.bt_scanner.adapter.dev.detail;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.dev.DetailActivity;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = DetailActivity.TAG + " - " + getClass().getSimpleName();

    private static final int TYPE_CHARACTERISTIC = 1;

    private ArrayList<CharacteristicItem> characteristicItemArrayList;


    private OnCharacteristicItemClickListener onCharacteristicItemClickListener;

    public CharacteristicAdapter(ArrayList<CharacteristicItem> characteristicItemArrayList, Activity activity) {
        this.characteristicItemArrayList = characteristicItemArrayList;
        try {
            onCharacteristicItemClickListener = (OnCharacteristicItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServiceItemClickListener");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.detail_bluetooth_characteristics_item, parent, false);
        return new CharacteristicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        CharacteristicViewHolder characteristicViewHolder = (CharacteristicViewHolder) holder;
        CharacteristicItem characteristicItem = characteristicItemArrayList.get(position);
        characteristicViewHolder.characteristicTitle.setText(characteristicItem.getTitle());
        characteristicViewHolder.characteristicUuid.setText(characteristicItem.getUuid());
        characteristicViewHolder.characteristicValue.setText(characteristicItem.getValue());
    }

    @Override
    public int getItemViewType(int position) {

        return TYPE_CHARACTERISTIC;

    }

    @Override
    public int getItemCount() {
        return characteristicItemArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearList() {
        characteristicItemArrayList.clear();
    }

    private class CharacteristicViewHolder extends RecyclerView.ViewHolder {

        private TextView characteristicTitle;
        private TextView characteristicUuid;
        private TextView characteristicValue;

        public CharacteristicViewHolder(View itemView) {
            super(itemView);
            characteristicTitle = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_title);
            characteristicUuid = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_UUID);
            characteristicValue = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_value);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCharacteristicItemClickListener.onCharacteristicItemClick(getLayoutPosition());
                }
            });
        }
    }

    public interface OnCharacteristicItemClickListener {
        void onCharacteristicItemClick(int position);
    }
}
