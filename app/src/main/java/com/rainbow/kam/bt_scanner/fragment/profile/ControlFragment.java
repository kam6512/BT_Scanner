package com.rainbow.kam.bt_scanner.fragment.profile;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;
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
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class ControlFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Context context;
    private View view;

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
    private String name;
    private String address;
    private String uuid;

    private String hexValue;
    private String strValue;
    private String lastUpdateTime;
    private boolean notificationEnabled;

    private OnControlListener onControlListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                this.context = context;
                onControlListener = (OnControlListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnControlListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_profile_control, container, false);
        setControlInfoView();
        setValueView();
        setBtn();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        onControlListener.onControlReady();
    }


    @Override
    public void onPause() {
        super.onPause();
        notificationEnabled = false;
        onControlListener.setNotification(notificationEnabled);
    }


    private void setControlInfoView() {
        deviceName = (TextView) view.findViewById(R.id.characteristic_device_name);
        deviceAddress = (TextView) view.findViewById(R.id.characteristic_device_address);

        serviceName = (TextView) view.findViewById(R.id.characteristic_service_name);
        serviceUuid = (TextView) view.findViewById(R.id.characteristic_service_uuid);

        charName = (TextView) view.findViewById(R.id.control_name);
        charUuid = (TextView) view.findViewById(R.id.control_uuid);

        charDataType = (TextView) view.findViewById(R.id.control_type);
        charProperties = (TextView) view.findViewById(R.id.control_properties);
    }


    private void setValueView() {
        charHexValue = (EditText) view.findViewById(R.id.control_hex_value);
        charStrValue = (TextView) view.findViewById(R.id.control_ascii_value);
        charDateValue = (TextView) view.findViewById(R.id.control_timestamp);
    }


    private void setBtn() {
        readBtn = (Button) view.findViewById(R.id.control_read_btn);
        writeBtn = (Button) view.findViewById(R.id.control_write_btn);
        notificationBtn = (ToggleButton) view.findViewById(R.id.control_notification_switcher);

        readBtn.setOnClickListener(this);
        writeBtn.setOnClickListener(this);
        notificationBtn.setOnCheckedChangeListener(this);
    }


    public void init(String name, String address, BluetoothGattCharacteristic characteristic) {
        this.name = name;
        this.address = address;
        this.bluetoothGattCharacteristic = characteristic;
        if (!Objects.equals(uuid, bluetoothGattCharacteristic.getUuid().toString())) {
            hexValue = "";
            strValue = "";
            lastUpdateTime = "";
            notificationEnabled = false;
            uuid = bluetoothGattCharacteristic.getUuid().toString();
        }
        initViewValue();
        bindView();
    }


    private void initViewValue() {
        if (isVisible()) {
            deviceName.setText(name);
            deviceAddress.setText(address);

            String service = bluetoothGattCharacteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault());
            serviceUuid.setText(service);
            serviceName.setText(GattAttributes.resolveServiceName(service.substring(0, 8)));

            String characteristic = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
            charUuid.setText(characteristic);
            charName.setText(GattAttributes.resolveCharacteristicName(characteristic.substring(0, 8)));
            charDataType.setText(GattAttributes.resolveValueTypeDescription(bluetoothGattCharacteristic));

            int props = bluetoothGattCharacteristic.getProperties();
            StringBuilder propertiesString = new StringBuilder();
            propertiesString.append(String.format("0x%04X [ ", props));
            if ((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                propertiesString.append("read ");
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                propertiesString.append("write ");
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                propertiesString.append("notify ");
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                propertiesString.append("indicate ");
            }
            if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                propertiesString.append("write_no_response ");
            }
            charProperties.setText(propertiesString + "]");

            notificationBtn.setEnabled((props & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0);
            notificationBtn.setChecked(notificationEnabled);

            readBtn.setEnabled((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
            writeBtn.setEnabled((props & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0);
            charHexValue.setEnabled(writeBtn.isEnabled());
        }
    }


    public void newValueForCharacteristic(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        byte[] rawValue = bluetoothGattCharacteristic.getValue();

        setStrValue(rawValue);
        setHexValue(rawValue);
        setTimeStamp();

        bindView();
    }


    private void setHexValue(byte[] rawValue) {
        if (rawValue != null && rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
            for (byte byteChar : rawValue) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            hexValue = "0x" + stringBuilder.toString();
        } else {
            hexValue = "";
        }
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


    private void setTimeStamp() {
        lastUpdateTime = new SimpleDateFormat(context.getString(R.string.profile_timestamp)).format(new Date());
    }


    private void bindView() {
        if (isVisible()) {
            charHexValue.setText(hexValue);
            charStrValue.setText(strValue);
            charDateValue.setText(lastUpdateTime);
        }
    }


    public void setFail() {
        hexValue = context.getString(R.string.profile_fail);
        strValue = context.getString(R.string.profile_fail);
        lastUpdateTime = context.getString(R.string.profile_fail);
        bindView();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_read_btn:
                onControlListener.setReadValue();
                break;
            case R.id.control_write_btn:
                String newValue = charHexValue.getText().toString().toLowerCase(Locale.getDefault());
                if (!TextUtils.isEmpty(newValue) || newValue.length() > 1) {
                    try {
                        byte[] dataToWrite = PrimeHelper.getBytes(newValue);
                        onControlListener.setWriteValue(dataToWrite);
                    } catch (StringIndexOutOfBoundsException e) {
                        onControlListener.setWriteValue(null);
                    }

                } else {
                    Snackbar.make(v, "dataToWrite value is empty!", Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.control_notification_switcher:
                if (isChecked == notificationEnabled) {
                    return;
                }
                onControlListener.setNotification(isChecked);
                notificationEnabled = isChecked;
                break;
        }
    }


    public interface OnControlListener {
        void onControlReady();

        void setNotification(boolean isNotificationEnable);

        void setReadValue();

        void setWriteValue(byte[] data);
    }
}