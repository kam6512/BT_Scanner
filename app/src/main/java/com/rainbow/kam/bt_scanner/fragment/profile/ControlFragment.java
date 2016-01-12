package com.rainbow.kam.bt_scanner.fragment.profile;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class ControlFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Context context;

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

    private String hexValue;
    private String strValue;
    private String lastUpdateTime;
    private boolean notificationEnabled;

    private OnControlListener onControlListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Control", "onCreate");
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("Control", "onViewCreated");
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("Control", "onViewStateRestored");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("Control", "onStart");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("Control", "onPause");
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d("Control", "onStop");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Control", "onDestroyView");

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Control", "onDestroy");
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("Control", "onDetach");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("Control", "onAttach");
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
        final View view = inflater.inflate(R.layout.f_profile_control, container, false);
        Log.d("Control", "onCreateView");
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
    public void onResume() {
        Log.d("Control", "onResume");
        super.onResume();
        onControlListener.onControlReady();
    }


    public void init(String name, String address, BluetoothGattCharacteristic characteristic) {
        this.name = name;
        this.address = address;
        this.bluetoothGattCharacteristic = characteristic;
        hexValue = "";
        strValue = "";
        lastUpdateTime = "";
        notificationEnabled = false;
        initView();
        bindView();
    }


    private void initView() {
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
        notificationEnabled = true;
    }


    public void setFail() {
        hexValue = context.getString(R.string.profile_fail);
        strValue = context.getString(R.string.profile_fail);
        lastUpdateTime = context.getString(R.string.profile_fail);

        bindView();
    }


    private void bindView() {
        if (isVisible()) {
            charHexValue.setText(hexValue);
            charStrValue.setText(strValue);
            charDateValue.setText(lastUpdateTime);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.characteristic_detail_read_btn:
                onControlListener.setReadValue();
                break;
            case R.id.characteristic_detail_write_btn:
                String newValue = charHexValue.getText().toString().toLowerCase(Locale.getDefault());
                if (!TextUtils.isEmpty(newValue) || newValue.length() > 1) {
                    byte[] dataToWrite = PrimeHelper.WRITE_FROM_CONTROL(newValue);
                    onControlListener.setWriteValue(dataToWrite);
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