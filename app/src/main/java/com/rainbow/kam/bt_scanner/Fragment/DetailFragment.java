package com.rainbow.kam.bt_scanner.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLE;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLEGattAttributes;

import java.util.Locale;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class DetailFragment extends Fragment {

    private class FieldReference {
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
    }

    private Activity activity;
    private View view;

    private BluetoothGattCharacteristic bluetoothGattCharacteristic = null;
    private BLE ble;
    private byte[] rawValue = null;
    private int intValue = 0;
    private String asciiValue = "";
    private String strValue = "";
    private String lastUpdateTime = "";
    private boolean notificationEnabled = false;

    FieldReference fieldReference;

    public DetailFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public DetailFragment(BLE ble) {
        this.ble = ble;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.bluetoothGattCharacteristic = characteristic;
        rawValue = null;
        intValue = 0;
        asciiValue = "";
        strValue = "";
        lastUpdateTime = "-";
        notificationEnabled = false;
        onStartDetail();
    }

    public BluetoothGattCharacteristic getCharacteristic(int index) {
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
        this.rawValue = rawValue;
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
        if ((!bluetoothGattCharacteristic.equals(this.bluetoothGattCharacteristic)) || (notificationEnabled == true)) {
            return;
        }

        notificationEnabled = true;
        onStartDetail();
//        notifyDataSetChanged();
    }

    public byte[] parseHexStringToBytes(final String hex) {
        try {
            String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
            byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally

            String part = "";
            int checksum = 0;
            for (int i = 0; i < bytes.length; ++i) {
                part = "0x" + tmp.substring(i * 2, i * 2 + 2);
                bytes[i] = Long.decode(part).byteValue();
                checksum = (checksum ^ bytes[i]);
                Log.e("part", "part : " + part);
            }
            Log.e("cS","checkSum : " + checksum);
            return bytes;
        } catch (Exception e) {
            return null;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_detail, container, false);
        fieldReference = new FieldReference();

        fieldReference.deviceName = (TextView) view.findViewById(R.id.characteristic_device_name);
        fieldReference.deviceAddress = (TextView) view.findViewById(R.id.characteristic_device_address);
        fieldReference.serviceName = (TextView) view.findViewById(R.id.characteristic_service_name);
        fieldReference.serviceUuid = (TextView) view.findViewById(R.id.characteristic_service_uuid);
        fieldReference.charName = (TextView) view.findViewById(R.id.characteristic_detail_name);
        fieldReference.charUuid = (TextView) view.findViewById(R.id.characteristic_detail_uuid);

        fieldReference.charDataType = (TextView) view.findViewById(R.id.characteristic_detail_type);
        fieldReference.charProperties = (TextView) view.findViewById(R.id.characteristic_detail_properties);

        fieldReference.charStrValue = (TextView) view.findViewById(R.id.characteristic_detail_ascii_value);
        fieldReference.charDecValue = (TextView) view.findViewById(R.id.characteristic_detail_decimal_value);
        fieldReference.charHexValue = (EditText) view.findViewById(R.id.characteristic_detail_hex_value);
        fieldReference.charDateValue = (TextView) view.findViewById(R.id.characteristic_detail_timestamp);

        fieldReference.notificationBtn = (ToggleButton) view.findViewById(R.id.characteristic_detail_notification_switcher);
        fieldReference.readBtn = (Button) view.findViewById(R.id.characteristic_detail_read_btn);
        fieldReference.writeBtn = (Button) view.findViewById(R.id.characteristic_detail_write_btn);
        fieldReference.writeBtn.setTag(fieldReference.charHexValue);

        fieldReference.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ble.requestCharacteristicValue(bluetoothGattCharacteristic);
            }
        });

        fieldReference.writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText hex = (EditText) v.getTag();
                String newValue = hex.getText().toString().toLowerCase(Locale.getDefault());
                Log.e("writeBtn", newValue);

                byte[] dataToWrite = parseHexStringToBytes(newValue);

                ble.writeDataToCharacteristic(bluetoothGattCharacteristic, dataToWrite);
            }
        });

        fieldReference.notificationBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == notificationEnabled) {
                    return;
                }
                ble.setNotificationForCharacteristic(bluetoothGattCharacteristic, isChecked);
                notificationEnabled = isChecked;
            }
        });

        view.setTag(fieldReference);
        return view;
    }

    public void onStartDetail() {
        Log.e("DetailFragment", "onStartDetail");
        fieldReference.deviceName.setText(ble.getBluetoothDevice().getName());
        fieldReference.deviceAddress.setText(ble.getBluetoothDevice().getAddress());

        String tmp = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
        fieldReference.serviceUuid.setText(tmp);
        fieldReference.serviceName.setText(BLEGattAttributes.resolveServiceName(tmp));

        String uuid = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveCharacteristicName(uuid);

        fieldReference.charName.setText(name);
        fieldReference.charUuid.setText(uuid);

        int format = ble.getValueFormat(bluetoothGattCharacteristic);
        fieldReference.charDataType.setText(BLEGattAttributes.resolveValueTypeDescription(format));
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
        fieldReference.charProperties.setText(propertiesString + "]");

        fieldReference.notificationBtn.setEnabled((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0);
        fieldReference.notificationBtn.setChecked(notificationEnabled);
        fieldReference.readBtn.setEnabled((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
        fieldReference.writeBtn.setEnabled((props & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0);
        fieldReference.charHexValue.setEnabled(fieldReference.writeBtn.isEnabled());

        fieldReference.charHexValue.setText(asciiValue);
        fieldReference.charStrValue.setText(strValue);
        fieldReference.charDecValue.setText(String.format("%d", intValue));
        fieldReference.charDateValue.setText(lastUpdateTime);

    }


}
