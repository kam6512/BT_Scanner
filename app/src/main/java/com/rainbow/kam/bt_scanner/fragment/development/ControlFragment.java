package com.rainbow.kam.bt_scanner.fragment.development;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.gatt.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class ControlFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Activity activity;

    private TextView deviceName;
    private TextView deviceAddress;
    private TextView serviceName;
    private TextView serviceUuid;
    private TextView charUuid;
    private TextView charName;
    private TextView charDataType;
    private TextView charStrValue;
    private EditText charHexValue;
    private TextView charDateValue;
    private TextView charProperties;

    private ToggleButton notificationBtn;
    private Button readBtn;
    private Button writeBtn;

    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothDevice bluetoothDevice;
    private GattManager gattManager;

    private String asciiValue = "";
    private String strValue = "";
    private String lastUpdateTime = "";
    private boolean notificationEnabled = false;

    private OnControlReadyListener onControlReadyListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                activity = (Activity) context;
                onControlReadyListener = (OnControlReadyListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnControlReadyListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_profile_control, container, false);

        deviceName = (TextView) view.findViewById(R.id.characteristic_device_name);
        deviceAddress = (TextView) view.findViewById(R.id.characteristic_device_address);

        serviceName = (TextView) view.findViewById(R.id.characteristic_service_name);
        serviceUuid = (TextView) view.findViewById(R.id.characteristic_service_uuid);

        charName = (TextView) view.findViewById(R.id.characteristic_detail_name);
        charUuid = (TextView) view.findViewById(R.id.characteristic_detail_uuid);

        charDataType = (TextView) view.findViewById(R.id.characteristic_detail_type);
        charProperties = (TextView) view.findViewById(R.id.characteristic_detail_properties);

        charHexValue = (EditText) view.findViewById(R.id.characteristic_detail_hex_value);
        charStrValue = (TextView) view.findViewById(R.id.characteristic_detail_ascii_value);
        charDateValue = (TextView) view.findViewById(R.id.characteristic_detail_timestamp);

        readBtn = (Button) view.findViewById(R.id.characteristic_detail_read_btn);
        writeBtn = (Button) view.findViewById(R.id.characteristic_detail_write_btn);
        notificationBtn = (ToggleButton) view.findViewById(R.id.characteristic_detail_notification_switcher);

        readBtn.setOnClickListener(this);
        writeBtn.setOnClickListener(this);
        notificationBtn.setOnCheckedChangeListener(this);


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onControlReadyListener.onControlReady();
    }


    public void init(GattManager gattManager, BluetoothGattCharacteristic characteristic) {
        this.gattManager = gattManager;
        bluetoothDevice = gattManager.getBluetoothDevice();
        this.bluetoothGattCharacteristic = characteristic;
        asciiValue = "";
        strValue = "";
        lastUpdateTime = "-";
        notificationEnabled = false;
        bindView();
    }


    public void newValueForCharacteristic(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        byte[] rawValue = bluetoothGattCharacteristic.getValue();

        setStrValue(rawValue);
        setAsciiValue(rawValue);
        setTimeStamp();

        bindView();
    }


    private void setStrValue(byte[] rawValue) {
        if (rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
            for (byte byteChar : rawValue) {
                try {
                    stringBuilder.append(String.format("%c", byteChar));
                } catch (IllegalFormatCodePointException e) {
                    stringBuilder.append((char) byteChar);
                }
            }
            this.strValue = stringBuilder.toString();
        }
    }


    private void setAsciiValue(byte[] rawValue) {
        if (rawValue != null && rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
            for (byte byteChar : rawValue) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            asciiValue = "0x" + stringBuilder.toString();
        } else {
            asciiValue = "";
        }
    }


    private void setTimeStamp() {
        lastUpdateTime = new SimpleDateFormat(activity.getString(R.string.timestamp)).format(new Date());
        notificationEnabled = true;
    }


    public void setFail() {
        strValue = activity.getString(R.string.fail_characteristic);
        asciiValue = activity.getString(R.string.fail_characteristic);
        lastUpdateTime = activity.getString(R.string.fail_characteristic);

        bindView();
    }


    private void bindView() {

        if (bluetoothDevice != null) {
            deviceName.setText(bluetoothDevice.getName());
            deviceAddress.setText(bluetoothDevice.getAddress());

            String tmp = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
            serviceUuid.setText(tmp);
            serviceName.setText(GattAttributes.resolveServiceName(tmp));

            String uuid = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
            String name = GattAttributes.resolveCharacteristicName(uuid);
            charName.setText(name);
            charUuid.setText(uuid);

            charDataType.setText(GattAttributes.resolveValueTypeDescription(bluetoothGattCharacteristic));

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
            charDateValue.setText(lastUpdateTime);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.characteristic_detail_read_btn:
                gattManager.readValue(bluetoothGattCharacteristic);
                break;
            case R.id.characteristic_detail_write_btn:
                String newValue = charHexValue.getText().toString().toLowerCase(Locale.getDefault());
                if (!TextUtils.isEmpty(newValue) || newValue.length() > 1) {
                    byte[] dataToWrite = PrimeHelper.parseHexStringToBytesDEV(newValue);
                    gattManager.writeValue(bluetoothGattCharacteristic, dataToWrite);
                } else {
                    Snackbar.make(v, "dataToWrite value is empty!", Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.characteristic_detail_notification_switcher:
                if (isChecked == notificationEnabled) {
                    return;
                }
                gattManager.setNotification(bluetoothGattCharacteristic, isChecked);
                notificationEnabled = isChecked;
                break;
        }
    }


    public interface OnControlReadyListener {
        void onControlReady();
    }


    public void removeListener() {
        onControlReadyListener = null;
    }
}
