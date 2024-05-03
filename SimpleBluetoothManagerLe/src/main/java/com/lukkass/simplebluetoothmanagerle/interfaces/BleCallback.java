package com.lukkass.simplebluetoothmanagerle.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import com.lukkass.simplebluetoothmanagerle.BluetoothManagerLE;
import com.lukkass.simplebluetoothmanagerle.models.BluetoothLE;


public class BleCallback {
    
    
    
    /**
     * Callback interface used to indicating when {@link BluetoothManagerLE#scanLeDevice} state
     */
    public interface OnScanStateChangeListener {
        /**
         * @param isScanning is true when {@link BluetoothManagerLE#scanLeDevice} starts,
         *                  false when {@link BluetoothManagerLE#scanLeDevice} stops.
         */
        default void onStateChange(final boolean isScanning){}

        default void onScanningElapsedTime(final float elapsedTime) {}
    }

    /**
     * Callback interface used to deliver scan results.
     */
    public interface OnScanDiscoveredListener {
        /**
         * Callback reporting an LE device found during a device scan initiated
         * by the {@link BluetoothManagerLE#scanLeDevice} function.
         *
         */
        void onScanDiscovered(final BluetoothLE bluetoothLE);
    }

    /**
     * Callback interface used to indicating when GATT client has connected/disconnected to/from a remote
     * GATT server.
     */
    public interface OnConnectionStateChangeListener {
        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote
         * GATT server.
         *
         * @param gatt GATT client
         * @param status Status of the connect or disconnect operation. {@link
         * BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         * @param newState Returns the new connection state. Can be one of {@link
         * BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}
         */
        void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState);
    }

    /**
     * Callback interface used to deliver services results.
     */
    public interface OnServicesDiscoveredListener {
        /**
         * Callback invoked when the list of remote services, characteristics and descriptors
         * for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device has been explored
         * successfully.
         */
        void onServicesDiscovered(BluetoothGatt gatt, final int status);
    }

    /**
     * Callback interface used to receive data from the device.
     */
    public interface OnCharacteristicChangeListener {
        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt           GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result of a remote
         *                       notification event.
         */
        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    }


    public interface OnCharacteristicReadListener {

        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    }

    public interface OnCharacteristicWriteListener {

        void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    }
    
    //-------------------------------------mudar para um arraylist no futuro--------------------------------------------------------

    protected OnScanStateChangeListener onScanStateChangeListener ;
    
    public void setListener(OnScanStateChangeListener _onScanStateChangeListener) {
        onScanStateChangeListener= _onScanStateChangeListener;
    }
    
    protected OnScanDiscoveredListener onScanDiscoveredListener;

    public void setListener(OnScanDiscoveredListener _onScanDiscoveredListener) {
        onScanDiscoveredListener = _onScanDiscoveredListener;
    }
    
    public OnConnectionStateChangeListener onConnectionStateChangeListener;
    public void  setListener(OnConnectionStateChangeListener _onConnectionStateChangeListener){
        onConnectionStateChangeListener = _onConnectionStateChangeListener;
    }

    protected OnServicesDiscoveredListener onServicesDiscoveredListener;
    public void  setListener(OnServicesDiscoveredListener _onServicesDiscoveredListener){
        onServicesDiscoveredListener = _onServicesDiscoveredListener;
    }

    protected OnCharacteristicChangeListener onCharacteristicChangeListener;
    public void  setListener(OnCharacteristicChangeListener _onCharacteristicChangeListener){
        onCharacteristicChangeListener = _onCharacteristicChangeListener;
    }

    protected OnCharacteristicReadListener onCharacteristicReadListener;
    public void  setListener(OnCharacteristicReadListener _onCharacteristicReadListener){
        onCharacteristicReadListener = _onCharacteristicReadListener;
    }

    protected OnCharacteristicWriteListener onCharacteristicWriteListener;
    public void  setListener(OnCharacteristicWriteListener _onCharacteristicWriteListener){
        onCharacteristicWriteListener = _onCharacteristicWriteListener;
    }
}
