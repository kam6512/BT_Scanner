package com.rainbow.kam.bt_scanner.fragment.profile;

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
import com.rainbow.kam.bt_scanner.tools.helper.NursingGattHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Locale;
import java.util.Objects;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class ControlFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private NursingGattHelper.OperationPrime operationPrime;

    private Context context;
    private View view;

    @Bind(R.id.characteristic_device_name)
    TextView deviceName;
    @Bind(R.id.characteristic_device_address)
    TextView deviceAddress;

    @Bind(R.id.characteristic_service_name)
    TextView serviceName;
    @Bind(R.id.characteristic_service_uuid)
    TextView serviceUuid;

    @Bind(R.id.control_name)
    TextView charName;
    @Bind(R.id.control_uuid)
    TextView charUuid;

    @Bind(R.id.control_type)
    TextView charDataType;
    @Bind(R.id.control_properties)
    TextView charProperties;

    @Bind(R.id.control_hex_value)
    EditText charHexValue;
    @Bind(R.id.control_ascii_value)
    TextView charStrValue;
    @Bind(R.id.control_timestamp)
    TextView charDateValue;


    @Bind(R.id.control_notification_switcher)
    ToggleButton notificationBtn;
    @Bind(R.id.control_read_btn)
    Button readBtn;
    @Bind(R.id.control_write_btn)
    Button writeBtn;

    @BindString(R.string.profile_fail)
    String profileFail;

    @BindString(R.string.profile_timestamp)
    String profileTimeStamp;

    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private String name;
    private String address;
    private String uuid;

    private String hexValue;
    private String strValue;
    private String lastUpdateTime;
    private boolean notificationEnabled;

    private OnControlListener onControlListener;


    public ControlFragment() {
        operationPrime = new NursingGattHelper().getPrimeHelper();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onControlListener = (OnControlListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_profile_control, container, false);
        ButterKnife.bind(this, view);
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


    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void setBtn() {

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
        lastUpdateTime = new SimpleDateFormat(profileTimeStamp, Locale.getDefault()).format(new Date().getTime());
    }


    private void bindView() {
        if (isVisible()) {
            charHexValue.setText(hexValue);
            charStrValue.setText(strValue);
            charDateValue.setText(lastUpdateTime);
        }
    }


    public void setFail() {
        hexValue = profileFail;
        strValue = profileFail;
        lastUpdateTime = profileFail;
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
                        byte[] dataToWrite = operationPrime.getBytes(newValue);
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