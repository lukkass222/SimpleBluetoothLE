package com.lukkass.simplebluetoothmanagerle.interfaces;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;


public class BluetoothGattCallbackLE extends BluetoothGattCallback {

    protected final BleCallback callback = new BleCallback();

    public BleCallback.OnScanStateChangeListener getOnScanStateChangeListener(){
        return callback.onScanStateChangeListener;
    }

    public BleCallback.OnScanDiscoveredListener getOnScanDiscoveredListener(){
        return callback.onScanDiscoveredListener;
    }
    
    public BleCallback getCallback() {
        return callback;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (callback.onConnectionStateChangeListener != null)
            callback.onConnectionStateChangeListener.onConnectionStateChange(gatt, status, newState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (callback.onServicesDiscoveredListener != null)
            callback.onServicesDiscoveredListener.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (callback.onCharacteristicWriteListener != null)
            callback.onCharacteristicWriteListener.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (callback.onCharacteristicReadListener != null)
            callback.onCharacteristicReadListener.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (callback.onCharacteristicChangeListener != null)
            callback.onCharacteristicChangeListener.onCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
        super.onCharacteristicChanged(gatt, characteristic, value);
        if (callback.onCharacteristicChangeListener != null)
            callback.onCharacteristicChangeListener.onCharacteristicChanged(gatt, characteristic);
    }

}








   
   

