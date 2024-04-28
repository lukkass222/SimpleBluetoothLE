package com.lukkass.example;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lukkass.simplebluetoothmanagerle.BluetoothManagerLE;
import com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback;
import com.lukkass.simplebluetoothmanagerle.models.BluetoothLE;
import com.lukkass.simplebluetoothmanagerle.utils.Functions;
import com.lukkass.simplebluetoothmanagerle.utils.Permissions;

import java.util.Objects;


public class exampleActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ArrayAdapter<String> deviceListAdapter;

    private static ArrayAdapter<String> messageListAdapter;
    private Button findButton;
    private Button readButton;
    private static final String TAG = "exampleActivity";
    private static BluetoothGattCharacteristic writerCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothManagerLE.isSupported(this);

        initializeLayout();
        configureBLE();
    }

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    private void initializeLayout() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        TextView deviceListTitleTextView = new TextView(this);
        deviceListTitleTextView.setText("Device List");
        deviceListTitleTextView.setBackgroundResource(android.R.color.holo_blue_light);
        ListView deviceListView = new ListView(this);
        LinearLayout.LayoutParams deviceListParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        deviceListParams.weight = 1;
        deviceListView.setLayoutParams(deviceListParams);

        TextView messageListTitleTextView = new TextView(this);
        messageListTitleTextView.setBackgroundResource(android.R.color.holo_blue_light);
        messageListTitleTextView.setText("Message List");
        ListView messageListView = new ListView(this);
        LinearLayout.LayoutParams messageListParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        messageListParams.weight = 1;
        messageListView.setLayoutParams(messageListParams);

        Button
                sendButton = new Button(this);
        sendButton.setText("Send");
        findButton = new Button(this);
        findButton.setText("Find");
        readButton = new Button(this);
        readButton.setText("Read");

        rootLayout.addView(deviceListTitleTextView);
        rootLayout.addView(deviceListView);
        rootLayout.addView(messageListTitleTextView);
        rootLayout.addView(messageListView);
        rootLayout.addView(findButton);
        rootLayout.addView(sendButton);
        rootLayout.addView(readButton);
        setContentView(rootLayout);

        findButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        readButton.setOnClickListener(this);

        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(this);
        messageListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        messageListView.setAdapter(messageListAdapter);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0)); // For XiaoMi users !!!
    }

    private void configureBLE() {

        BluetoothManagerLE.useCallback().setListener(new BleCallback.OnConnectionStateChangeListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i(TAG, "connection status: " + newState);

                if (status != BluetoothGatt.GATT_SUCCESS)
                    return;

                new Handler(Looper.getMainLooper()).post(() -> {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothProfile.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "CONNECTING", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            Toast.makeText(getApplicationContext(), "DISCONNECTED", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothProfile.STATE_DISCONNECTING:
                            Toast.makeText(getApplicationContext(), "DISCONNECTING", Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            break;
                    }
                });

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }
        });

        BluetoothManagerLE.useCallback().setListener(new BleCallback.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS)
                    return;

                printServices(gatt);

                for (BluetoothGattService gattService : gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        if (Functions.isCharacteristicWriteable(characteristic)) {
                            writerCharacteristic = characteristic;
                        }

                        if (Functions.isCharacteristicNotifiable(characteristic)) {
                            BluetoothManagerLE.setNotifiable(characteristic);
                        }
                    }
                }
            }
        });

        BluetoothManagerLE.useCallback().setListener( new BleCallback.OnCharacteristicChangeListener() {
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                String text = new String(characteristic.getValue());
                Log.i("BluetoothGattCallback", "Characteristic changed: "+ text);
                new Handler(Looper.getMainLooper()).post(() -> messageListAdapter.add((text)));
            }
        });

        BluetoothManagerLE.useCallback().setListener(new BleCallback.OnScanDiscoveredListener() {
            @Override
            public void onScanDiscovered(BluetoothLE bluetoothLE) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    deviceListAdapter.add(String.format("%s\n%s", bluetoothLE.getName(), bluetoothLE.getMacAddress()));
                    deviceListAdapter.notifyDataSetChanged();
                });
            }
        });

        BluetoothManagerLE.useCallback().setListener(new BleCallback.OnCharacteristicReadListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS)
                    return;
                String text = new String(characteristic.getValue());
                new Handler(Looper.getMainLooper()).post(() -> messageListAdapter.add((text)));
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == readButton) {

            if (!BluetoothManagerLE.isConnected()) {
                Toast.makeText(this, "DISCONNECTED", Toast.LENGTH_SHORT).show();
                return;
            }

            BluetoothGattCharacteristic readCharacteristic =
                    BluetoothManagerLE.getCharacteristic("00001800-0000-1000-8000-00805f9b34fb",
                            "00002a00-0000-1000-8000-00805f9b34fb");

            boolean result = BluetoothManagerLE.read(readCharacteristic);
            Log.i(TAG, "Read success? " + result);

        } else if (v == findButton) {

            if (!BluetoothManagerLE.isEnabled(this))
                return;

            BluetoothManagerLE.disconnect();
            BluetoothManagerLE.scanLeDevice(new BleCallback.OnScanStateChangeListener() {
                @Override
                public void onStateChange(boolean isScanning) {

                    Log.i(TAG, isScanning ? "scan started!" : "scan finish!");

                    if (isScanning)
                        deviceListAdapter.clear();
                }
            }, null);

        } else {

            if (writerCharacteristic == null) {
                Toast.makeText(this, "Not Ready", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = "Hello World\n";
            Log.i(TAG, "Sending message " + message);
            messageListAdapter.add(message);
            boolean writeResult = BluetoothManagerLE.write(writerCharacteristic, message.getBytes());
            Log.i(TAG, "Write result: " + writeResult);
            assert writeResult;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String address = Objects.requireNonNull(deviceListAdapter.getItem(position)).split("\n")[1];
        BluetoothManagerLE.connect(this, address);
        Log.i(TAG, "Current device: " + address);
    }

    private void printServices(BluetoothGatt gatt) {

        Log.e("STATUS Discovered", "getServices SIZE: " + gatt.getServices().size());
        String aux;
        for (BluetoothGattService gattService : gatt.getServices()) {
            aux = "SERVICE "
                    + (gattService.getUuid().toString())
                    + " "
                    + Functions.getServiceType(gattService);

            Log.e("SERVICE", aux);

            for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {

                aux = "characteristic "
                        + (characteristic.getUuid().toString())
                        + " "
                        + Functions.describeProperties(characteristic);

                Log.w("characteristic", aux);

                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d("descriptor", descriptor.getUuid().toString());
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Permissions.REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {

                Log.e("PermissionsResult", permissions[i] + " result: " + grantResults[i] + " code: " + requestCode);
            }
        }
    }

}