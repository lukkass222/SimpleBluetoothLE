package com.lukkass.simplebluetoothmanagerle.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.text.TextUtils;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class Functions {

    public static boolean isBleSupported(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean getStatusGps(Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String describeProperties(BluetoothGattCharacteristic characteristic) {
        List<String> properties = new ArrayList<>();
        if (isCharacteristicReadable(characteristic)) properties.add("Read");
        if (isCharacteristicWriteable(characteristic)) properties.add("Write");
        if (isCharacteristicNotifiable(characteristic)) properties.add("Notify");
        return TextUtils.join(" ", properties);
    }

    public static void ShowStatusConnection(View view, String macAddress, int newState) {

        switch (newState) {
            case BluetoothGatt.STATE_CONNECTED:
                Snackbar.make(view, "CONNECTED in " + macAddress, Snackbar.LENGTH_LONG).setAction("Action", null).setAnchorView(view).show();
                break;
            case BluetoothGatt.STATE_CONNECTING:
                Snackbar.make(view, "CONNECTING in " + macAddress, Snackbar.LENGTH_LONG).setAction("Action", null).setAnchorView(view).show();
                break;
            case BluetoothGatt.STATE_DISCONNECTED:
                Snackbar.make(view, "DISCONNECTED in " + macAddress, Snackbar.LENGTH_LONG).setAction("Action", null).setAnchorView(view).show();
                break;
            case BluetoothGatt.STATE_DISCONNECTING:
                Snackbar.make(view, "DISCONNECTING in " + macAddress, Snackbar.LENGTH_LONG).setAction("Action", null).setAnchorView(view).show();
                break;

            default:
                Snackbar.make(view, "STATE is " + newState + " " + macAddress, Snackbar.LENGTH_LONG).setAction("Action", null).setAnchorView(view).show();
        }
    }

    public static boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        // return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
        return hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY);
    }

    public static boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        // return ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
        return hasProperty(characteristic, BluetoothGattCharacteristic.PROPERTY_READ);
    }

    public static boolean isCharacteristicWriteable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    public static boolean hasProperty(BluetoothGattCharacteristic characteristic, int property) {
        return characteristic != null && (characteristic.getProperties() & property) > 0;
    }

    public static String getServiceType(BluetoothGattService service) {
        return service.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY ? "Primary" : "Secondary";
    }

}
