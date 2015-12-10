package com.rainbow.kam.bt_scanner.fragment.dev;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BLE;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;
import com.rainbow.kam.bt_scanner.tools.ble.BleHelper;

import java.util.Locale;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class DetailFragment extends Fragment {

    private TextView deviceName;
    private TextView deviceAddress;
    private TextView serviceName;
    private TextView serviceUuid;
    private TextView charUuid;
    private TextView charName;
    private TextView charDataType;
    private TextView charStrValue;
    private EditText charHexValue;
    private TextView charDecValue;
    private TextView charDateValue;
    private TextView charProperties;

    private ToggleButton notificationBtn;
    private Button readBtn;
    private Button writeBtn;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic = null;
    private BLE ble;
    private int intValue = 0;
    private String asciiValue = "";
    private String strValue = "";
    private String lastUpdateTime = "";
    private boolean notificationEnabled = false;

    private OnDetailViewCreatedListener onDetailViewCreatedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onDetailViewCreatedListener = (OnDetailViewCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDetailViewCreatedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        deviceName = (TextView) view.findViewById(R.id.characteristic_device_name);
        deviceAddress = (TextView) view.findViewById(R.id.characteristic_device_address);
        serviceName = (TextView) view.findViewById(R.id.characteristic_service_name);
        serviceUuid = (TextView) view.findViewById(R.id.characteristic_service_uuid);
        charName = (TextView) view.findViewById(R.id.characteristic_detail_name);
        charUuid = (TextView) view.findViewById(R.id.characteristic_detail_uuid);

        charDataType = (TextView) view.findViewById(R.id.characteristic_detail_type);
        charProperties = (TextView) view.findViewById(R.id.characteristic_detail_properties);

        charStrValue = (TextView) view.findViewById(R.id.characteristic_detail_ascii_value);
        charDecValue = (TextView) view.findViewById(R.id.characteristic_detail_decimal_value);
        charHexValue = (EditText) view.findViewById(R.id.characteristic_detail_hex_value);
        charDateValue = (TextView) view.findViewById(R.id.characteristic_detail_timestamp);

        notificationBtn = (ToggleButton) view.findViewById(R.id.characteristic_detail_notification_switcher);
        readBtn = (Button) view.findViewById(R.id.characteristic_detail_read_btn);
        writeBtn = (Button) view.findViewById(R.id.characteristic_detail_write_btn);

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ble.requestCharacteristicValue(bluetoothGattCharacteristic);
            }
        });

        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText hex = charHexValue;
                String newValue = hex.getText().toString().toLowerCase(Locale.getDefault());
                if (!TextUtils.isEmpty(newValue) || newValue.length() > 1) {
                    byte[] dataToWrite = BleHelper.parseHexStringToBytesDEV(newValue);
                    ble.writeDataToCharacteristic(bluetoothGattCharacteristic, dataToWrite);
                }
            }
        });

        notificationBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == notificationEnabled) {
                    return;
                }
                ble.setNotificationForCharacteristic(bluetoothGattCharacteristic, isChecked);
                notificationEnabled = isChecked;
            }
        });

        onDetailViewCreatedListener.onDetailViewCreated();

        return view;
    }

    public void setBle(BLE ble) {
        this.ble = ble;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.bluetoothGattCharacteristic = characteristic;
        intValue = 0;
        asciiValue = "";
        strValue = "";
        lastUpdateTime = "-";
        notificationEnabled = false;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return bluetoothGattCharacteristic;
    }

    public void clearCharacteristic() {
        bluetoothGattCharacteristic = null;
    }

    public void newValueForCharacterictic(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final String strValue, final int intValue, final byte[] rawValue, final String timeStamp) {
        if (!bluetoothGattCharacteristic.equals(this.bluetoothGattCharacteristic)) {
            return;
        }

        this.intValue = intValue;
        this.strValue = strValue;
        if (rawValue != null && rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
            for (byte byteChar : rawValue) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            asciiValue = "0x" + stringBuilder.toString();
        } else {
            asciiValue = "";
        }

        lastUpdateTime = timeStamp;
        if (lastUpdateTime == null) {
            lastUpdateTime = "";
        }
    }

    public void setNotificationEnabledForService(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if ((!bluetoothGattCharacteristic.equals(this.bluetoothGattCharacteristic)) || (notificationEnabled)) {
            return;
        }
        notificationEnabled = true;
        bindView();
    }


    public void bindView() {

        if (ble != null) {
            deviceName.setText(ble.getBluetoothDevice().getName());
            deviceAddress.setText(ble.getBluetoothDevice().getAddress());

            String tmp = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
            serviceUuid.setText(tmp);
            serviceName.setText(BLEGattAttributes.resolveServiceName(tmp));

            String uuid = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = BLEGattAttributes.resolveCharacteristicName(uuid);
            charName.setText(name);
            charUuid.setText(uuid);

            int format = ble.getValueFormat(bluetoothGattCharacteristic);
            charDataType.setText(BLEGattAttributes.resolveValueTypeDescription(format));

            int props = bluetoothGattCharacteristic.getProperties();
            String propertiesString = String.format("0x%04X [", props);
            if ((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                propertiesString += "read ";
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                propertiesString += "write ";
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                propertiesString += "notify ";
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                propertiesString += "indicate ";
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                propertiesString += "write_no_response ";
            }
            charProperties.setText(propertiesString + "]");

            notificationBtn.setEnabled((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0);
            notificationBtn.setChecked(notificationEnabled);
            readBtn.setEnabled((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
            writeBtn.setEnabled((props & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0);
            charHexValue.setEnabled(writeBtn.isEnabled());

            charHexValue.setText(asciiValue);
            charStrValue.setText(strValue);
            charDecValue.setText(String.format("%d", intValue));
            charDateValue.setText(lastUpdateTime);
        }
    }

    public interface OnDetailViewCreatedListener {
        public void onDetailViewCreated();
    }
}
