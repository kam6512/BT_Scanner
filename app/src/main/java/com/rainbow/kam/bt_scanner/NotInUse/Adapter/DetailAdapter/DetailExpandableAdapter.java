package com.rainbow.kam.bt_scanner.NotInUse.Adapter.DetailAdapter;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.NotInUse.BluetoothPackage.BluetoothService;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kam6512 on 2015-10-26.
 */
public class DetailExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<BluetoothService> bluetoothServices;
    private ArrayList<HashMap<String, String>> serviceData;
    private int collapsedGroupLayout;
    private String[] groupFrom;
    private int[] groupTo;

    private ArrayList<ArrayList<HashMap<String, String>>> characteristicsData;
    private int childLayout;
    private String[] childFrom;
    private int[] childTo;

    private String[][] gattData;

    private ExpandableListView expandableListView;

    private LayoutInflater layoutInflater;


    public DetailExpandableAdapter(Context context, ArrayList<HashMap<String, String>> serviceData, int collapsedGroupLayout, String[] groupFrom, int[] groupTo, ArrayList<ArrayList<HashMap<String, String>>> characteristicsData, int collapsedChildLayout, String[] childFrom, int[] childTo, String[][] data, ExpandableListView expandableListView) {
        this.context = context;

        this.serviceData = serviceData;
        this.collapsedGroupLayout = collapsedGroupLayout;
        this.groupFrom = groupFrom;
        this.groupTo = groupTo;

        this.characteristicsData = characteristicsData;
        this.childLayout = collapsedChildLayout;
        this.childFrom = childFrom;
        this.childTo = childTo;

        this.gattData = data;

        this.expandableListView = expandableListView;

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setServices(ArrayList<BluetoothService> bluetoothServices) {
        this.bluetoothServices = bluetoothServices;
    }

    @Override
    public int getGroupCount() {
        return serviceData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return characteristicsData.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return serviceData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return characteristicsData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void bindValue(int group, int child, String value) {

    }

    private void bindView(View view, Map<String, ?> data, String[] from, int[] to) {
        bindView(view, data, from, to, 10000, 10000);
    }

    private void bindView(View view, Map<String, ?> data, String[] from, int[] to, int group, int child) {
        int len = to.length;
        for (int i = 0; i < len; i++) {
            TextView textView = (TextView) view.findViewById(to[i]);
            if (textView != null) {
                textView.setText((String) data.get(from[i]));
            }
            if (to[i] == R.id.detail_parent_list_item_service_UUID) {
                String value = BLEGattAttributes.getService(textView.getText().toString().substring(0, 8));
                if (value != "N/A") {
                    TextView title = (TextView) view.findViewById(to[i - 1]);
                    title.setText(value);
                }
                textView.setText("UUID : " + "0x" + textView.getText().toString().substring(4, 8).toUpperCase());
            } else if (to[i] == R.id.detail_child_list_item_characteristics_UUID) {
                String value = BLEGattAttributes.getCharacteristic(textView.getText().toString().substring(0, 8));
                if (value != "N/A") {
                    TextView title = (TextView) view.findViewById(to[i - 1]);
                    title.setText(value);
                }
                textView.setText("UUID : " + "0x" + textView.getText().toString().substring(4, 8).toUpperCase());
            }
            if (group != 10000 || child != 10000) {
                TextView valueText = (TextView) view.findViewById(R.id.detail_child_list_item_characteristics_value);
                try {
                    valueText.setText(gattData[group][child]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e("valueText", "error : " + group + " / " + child);
                    valueText.setText("값을 가져올 수가 없습니다.");
                }

            }
        }

    }

    public void updateValue(int i, int j, String data) {
        View view = layoutInflater.inflate(R.layout.detail_bluetooth_characteristics_item, null);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        Log.e("Group", "getGroup");
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.detail_bluetooth_service_item, null);
        } else {
            view = convertView;
        }

        bindView(view, serviceData.get(groupPosition), groupFrom, groupTo);
//        ServiceHolder serviceHolder = new ServiceHolder(view);
//        serviceHolder.detail_parent_list_item_service_title.setText(serviceData.get(groupPosition).get("NAME"));
//        serviceHolder.detail_parent_list_item_service_UUID.setText(serviceData.get(groupPosition).get("UUID"));
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        Log.e("Child", "getChild");
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.detail_bluetooth_characteristics_item, null);
        } else {
            view = convertView;
        }

        bindView(view, characteristicsData.get(groupPosition).get(childPosition), childFrom, childTo, groupPosition, childPosition

        );

//        CharacteristicsHolder characteristicsHolder = new CharacteristicsHolder(view);
//        characteristicsHolder.detail_child_list_item_characteristics_title.setText(characteristicsData.get(groupPosition).get(childPosition).get("NAME"));
//        characteristicsHolder.detail_child_list_item_characteristics_UUID.setText(characteristicsData.get(groupPosition).get(childPosition).get("UUID"));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public class ServiceHolder {
        public TextView detail_parent_list_item_service_title;
        public TextView detail_parent_list_item_service_UUID;
        public FloatingActionButton detail_parent_list_item_expand_arrow;


        public ServiceHolder(View itemView) {

            detail_parent_list_item_service_title = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_title);
            detail_parent_list_item_service_UUID = (TextView) itemView.findViewById(R.id.detail_parent_list_item_service_UUID);
//            detail_parent_list_item_expand_arrow = (FloatingActionButton) itemView.findViewById(R.id.detail_parent_list_item_expand_arrow);
        }
    }

    public class CharacteristicsHolder {
        public TextView detail_child_list_item_characteristics_title;
        public TextView detail_child_list_item_characteristics_UUID;
        public TextView detail_child_list_item_characteristics_value;


        public CharacteristicsHolder(View itemView) {

            detail_child_list_item_characteristics_title = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_title);
            detail_child_list_item_characteristics_UUID = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_UUID);
            detail_child_list_item_characteristics_value = (TextView) itemView.findViewById(R.id.detail_child_list_item_characteristics_value);
        }
    }

}
