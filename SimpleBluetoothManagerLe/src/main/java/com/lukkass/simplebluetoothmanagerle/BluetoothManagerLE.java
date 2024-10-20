package com.lukkass.simplebluetoothmanagerle;


import static com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback.OnCharacteristicChangeListener;
import static com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback.OnCharacteristicReadListener;
import static com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback.OnCharacteristicWriteListener;
import static com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback.OnConnectionStateChangeListener;
import static com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback.OnScanDiscoveredListener;
import static com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback.OnScanStateChangeListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.lukkass.simplebluetoothmanagerle.interfaces.BleCallback;
import com.lukkass.simplebluetoothmanagerle.interfaces.BluetoothGattCallbackLE;
import com.lukkass.simplebluetoothmanagerle.models.BluetoothLE;
import com.lukkass.simplebluetoothmanagerle.utils.Functions;
import com.lukkass.simplebluetoothmanagerle.utils.Permissions;

import java.util.ArrayList;
import java.util.UUID;

public class BluetoothManagerLE  {

    public static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    public static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    private static final ArrayList<BluetoothLE> aDevices = new ArrayList<>();
    private static BluetoothGatt mBluetoothGatt;
    private static final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Deprecated
    private volatile static int mConnectionState = STATE_DISCONNECTED;
    private static int SCAN_PERIOD = 10000;
    private volatile static boolean mScanning = false;
    private static final ArrayList<String> FILTER_SERVICE = new ArrayList<>();

    private static final BluetoothGattCallbackLE mGattCallback = new BluetoothGattCallbackLE() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mConnectionState = newState;
        }
    };
    
    public static boolean isSupported(Activity activity){
        
        if (Functions.isBleSupported(activity)) {
            //    BluetoothManager bluetoothManager = (BluetoothManager) act.getSystemService(Context.BLUETOOTH_SERVICE);
            //      mBluetoothAdapter = bluetoothManager.getAdapter();
            Permissions.checkPermisionStatus(activity);
        } else {
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage("error BLUETOOTH LOW ENERGY not found")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            
            return false;
        }
        return true;
    }
    
    /**
     * Starts a scan for Bluetooth LE devices, looking for devices that
     * advertise given services.<p>
     *
     *  automatically calls {@link #stopScanDevice()} later using {@link BluetoothManagerLE#getScanPeriod()}
     *
     * <p>Devices which advertise all specified services are reported using the
     * {@link OnScanDiscoveredListener#onScanDiscovered} callback.
     *
     * @param isScanningCallback callback used to monitor the {@link #isScanning()} status
     * @param DiscoveryCallback the callback LE scan results are delivered
     * @return true, if the scan was started successfully
     * @see #useFilterService()
     */
    public static boolean scanLeDevice(final OnScanStateChangeListener isScanningCallback, final OnScanDiscoveredListener DiscoveryCallback) {

        if (mScanning) {
            stopScanDeviceN();
            return false; //scan ja em execução
        }

        if (isScanningCallback != null) useCallback().setListener(isScanningCallback);

        mScanning = startScanDevice(DiscoveryCallback);
        if (mScanning) {

            if (true) {

                //https://stackoverflow.com/questions/61023968/what-do-i-use-now-that-handler-is-deprecated
                // Create a background thread that has a Looper
                //    HandlerThread handlerThread = new HandlerThread("HandlerThread");
                //  handlerThread.start();

                //      ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

                final long startTime = System.currentTimeMillis();

                new Thread(() -> {
                    // Execute a task in the background thread.
                    // backgroundExecutor.execute(() -> {

                    do {

                        long elapsedTime = (System.currentTimeMillis() - startTime);
                        if (elapsedTime >= SCAN_PERIOD) {

                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (mScanning)
                                    stopScanDevice();
                            });

                            elapsedTime = SCAN_PERIOD;
                        }

                        if (mGattCallback.getOnScanStateChangeListener() != null)
                            mGattCallback.getOnScanStateChangeListener().onScanningElapsedTime(elapsedTime);

                        //scannableTimeout
                        long remainingTime = SCAN_PERIOD - elapsedTime;

                    } while (mScanning);

                    //desligar o executor após o uso.
                    // backgroundExecutor.shutdown();
                    // handlerThread.quitSafely(); // or handlerThread.quitSafely();
                },"scanLeDevice").start();

            } else {

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (mScanning)
                        stopScanDevice();
                }, SCAN_PERIOD);
            }
        }

        return isScanning();
    }
    
    /**
     * Starts a scan for Bluetooth LE devices, looking for devices that
     * advertise given services.
     *
     * <p>Devices which advertise all specified services are reported using the
     * {@link OnScanDiscoveredListener#onScanDiscovered} callback.
     * @see BluetoothManagerLE#useCallback()
     *
     * @return true, if the scan was started successfully
     * @see #useFilterService()
     */
    public static boolean startScanDevice() {
        return startScanDevice(null);
    }

    /**
     * Starts a scan for Bluetooth LE devices, looking for devices that
     * advertise given services.
     *
     * <p>Devices which advertise all specified services are reported using the
     * {@link OnScanDiscoveredListener#onScanDiscovered} callback.
     *
     * @param DiscoveryCallback the callback LE scan results are delivered
     * @return true, if the scan was started successfully
     *
     * @see #useFilterService()
     */
    @SuppressLint("MissingPermission")
    public static boolean startScanDevice(final OnScanDiscoveredListener DiscoveryCallback) {

        aDevices.clear();

        if (DiscoveryCallback != null)
            mGattCallback.getCallback().setListener(DiscoveryCallback);

        if (!FILTER_SERVICE.isEmpty()) {
            UUID[] filter = new UUID[FILTER_SERVICE.size()];

            for (int i = 0; i < filter.length; i++) {
                filter[i] = UUID.fromString(FILTER_SERVICE.get(i));
            }

            mScanning=  mBluetoothAdapter.startLeScan(filter, mLeScanCallback);
        } else {
            mScanning=  mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        if (!mScanning) stopScanDeviceN();

        if ( mGattCallback.getOnScanStateChangeListener() != null)
            mGattCallback.getOnScanStateChangeListener().onStateChange(mScanning);

        return mScanning;
    }

    /**
     * stops device discovery.<p>
     * Use {@link #startScanDevice()} to start device discovery
     * @see #scanLeDevice(OnScanStateChangeListener, OnScanDiscoveredListener)
     */
    @SuppressLint("MissingPermission")
    public static void stopScanDevice() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        
        if ( mGattCallback.getOnScanStateChangeListener() != null) {
            mGattCallback.getOnScanStateChangeListener().onStateChange(mScanning);
        }
    }
    
    private static void stopScanDeviceN() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }
    
    @SuppressLint("MissingPermission")
    private static final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            boolean isNewItem = true;

            for (int i = 0; i < aDevices.size(); i++) {
                if (aDevices.get(i).getMacAddress().equals(device.getAddress())) {
                    isNewItem = false;
                }
            }

            if (isNewItem) {
                BluetoothLE bluetoothLE = new BluetoothLE(rssi, device);
                aDevices.add(bluetoothLE);

                if (mGattCallback.getOnScanDiscoveredListener() != null)
                    mGattCallback.getOnScanDiscoveredListener().onScanDiscovered(bluetoothLE);
            }
        }
    };

    /**
     * connect at a {@link BluetoothDevice} object for the given Bluetooth hardware address.
     * <p>Valid Bluetooth hardware addresses must be upper case, in big endian byte order, and in a
     * format such as "00:11:22:33:AA:BB". The helper {@link BluetoothAdapter#checkBluetoothAddress} is
     * available to validate a Bluetooth address.
     * <p>A {@link BluetoothDevice} will always be returned for a valid
     * hardware address, even if this adapter has never seen that device.
     *
     * @param address valid Bluetooth MAC address
     * @return The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct
     * GATT client operations.
     * @throws IllegalArgumentException if address is invalid
     *
     */
    @SuppressLint("MissingPermission")
    public static BluetoothGatt connect(@NonNull final Context _context, @NonNull final String address) {
        return connect(_context, address, null);
    }

    /**
     * connect at a {@link BluetoothDevice} object for the given Bluetooth hardware address.
     * <p>Valid Bluetooth hardware addresses must be upper case, in big endian byte order, and in a
     * format such as "00:11:22:33:AA:BB". The helper {@link BluetoothAdapter#checkBluetoothAddress} is
     * available to validate a Bluetooth address.
     * <p>A {@link BluetoothDevice} will always be returned for a valid
     * hardware address, even if this adapter has never seen that device.
     *
     * @param address valid Bluetooth MAC address
     * @param callback GATT callback handler that will receive asynchronous callbacks          
     * @return The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct
     * GATT client operations.
     * @throws IllegalArgumentException if address is invalid
     *
     */
    @SuppressLint("MissingPermission")
    public static BluetoothGatt connect(@NonNull final Context _context, @NonNull final String address,
                                        final OnConnectionStateChangeListener callback) {

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        return connect(_context, device, callback);
    }
    
    /**
     * Connect to GATT Server hosted by this device. Caller acts as GATT client.
     * The callback is used to deliver results to Caller, such as connection status as well
     * as any further GATT client operations.
     * The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct
     * GATT client operations.
     *<p>
     * use the {@link OnConnectionStateChangeListener }
     * interface to monitor the connection status in {@link BluetoothManagerLE#useCallback()}
     * @param device device you want to connect.
     * @return The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct
     *      * GATT client operations.
     */
    public static BluetoothGatt connect(final Context _context, final BluetoothDevice device) {
        
        return connect(_context, device, null);
    }

    /**
     * Connect to GATT Server hosted by this device. Caller acts as GATT client.
     * The callback is used to deliver results to Caller, such as connection status as well
     * as any further GATT client operations.
     * The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct
     * GATT client operations.
     *<p>
     * @param callback GATT callback handler that will receive asynchronous callbacks
     * @param device device you want to connect.
     * @return The method returns a BluetoothGatt instance. You can use BluetoothGatt to conduct 
     * GATT client operations.
     */
    @SuppressLint("MissingPermission")
    public static BluetoothGatt connect(final Context _context, final BluetoothDevice device, final OnConnectionStateChangeListener callback) {

        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        
        if (callback != null)
            mGattCallback.getCallback().setListener(callback);

        mBluetoothGatt = device.connectGatt(_context, false, mGattCallback);
        
        if ( mGattCallback.getCallback().onConnectionStateChangeListener != null)
            mGattCallback.getCallback().onConnectionStateChangeListener
                    .onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_CONNECTING);

        return mBluetoothGatt;
    }
    
    @SuppressLint("MissingPermission")
    public static void disconnect() {
        if (mBluetoothGatt != null) {
            if ( mGattCallback.getCallback().onConnectionStateChangeListener != null)
                mGattCallback.getCallback().onConnectionStateChangeListener
                        .onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_DISCONNECTING);
            mBluetoothGatt.disconnect();
           // mBluetoothGatt.close();
        }
        
        mConnectionState = STATE_DISCONNECTED;
       // mBluetoothGatt = null;
    }

    /**
     * verifica se tem as permições do bluetooth e do gps é depois solicita a ligação dos recursos,
     * casos estejam desligados.
     * o gps só é usado até o sdk 30
     * @return retorna true se o gps e o bluetooth estiverem ligados
     */
    public static boolean isEnabled(Context _context) {

        boolean ble_per;
        boolean gps_per = true;

        if (android.os.Build.VERSION.SDK_INT > 30) {
            ble_per = (Permissions.checkSelfPermission(_context, Manifest.permission.BLUETOOTH_SCAN));
        }
        else {

            gps_per = (Permissions.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION));

            if (gps_per) {
                if (!Functions.getStatusGps(_context)) {
                    gps_per = false;
                    new Handler().postDelayed(() -> _context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)), 3000);
                }
            }

            ble_per = (Permissions.checkSelfPermission(_context, Manifest.permission.BLUETOOTH));
        }

        if (ble_per) {
            if (!mBluetoothAdapter.isEnabled()) {
                ble_per = false;
                _context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }
        return (gps_per && ble_per);
    }
    
    public static boolean write(BluetoothGattCharacteristic characteristic, byte[] aBytes) {
        return write(characteristic, aBytes, null);
    }

    @SuppressLint("MissingPermission")
    public static boolean write(BluetoothGattCharacteristic characteristic, byte[] aBytes,
                                OnCharacteristicWriteListener callback) {
        if (callback != null) useCallback().setListener(callback);
        characteristic.setValue(aBytes);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }
    
    public static boolean read(BluetoothGattCharacteristic mBluetoothGattCharacteristic) {
        return read(mBluetoothGattCharacteristic, null);
    }

    @SuppressLint("MissingPermission")
    public static boolean read(BluetoothGattCharacteristic characteristic, OnCharacteristicReadListener callback) {
        if (callback != null) useCallback().setListener(callback);
        return mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    public static void setNotifiable(BluetoothGattService service){
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            setNotifiable(characteristic, null);
        }
    }
    
    public static void setNotifiable(BluetoothGattService service, OnCharacteristicChangeListener onCharacteristicChangeListener) {
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            setNotifiable(characteristic, onCharacteristicChangeListener);
        }
    }
    
    public static boolean setNotifiable(BluetoothGattCharacteristic characteristic) {
        return setNotifiable(characteristic, null);
    }
    
    @SuppressLint("MissingPermission")
    public static boolean setNotifiable(BluetoothGattCharacteristic characteristic, OnCharacteristicChangeListener onCharacteristicChangeListener) {
        if (onCharacteristicChangeListener != null)
            useCallback().setListener(onCharacteristicChangeListener);
        
        boolean STATUS = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        boolean ENABLE_NOTIFICATION_VALUE = false;
        boolean writeDescriptor = false;
        //       Log.i("setNotifications", "Notifications STATUS " + STATUS);
        //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

            //   BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
            ENABLE_NOTIFICATION_VALUE = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //        Log.i("NOTIFICATION", ("ENABLE_NOTIFICATION_VALUE: "+ ENABLE_NOTIFICATION_VALUE));
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            //   characteristic.addDescriptor(descriptor);
            //   characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            writeDescriptor = mBluetoothGatt.writeDescriptor(descriptor);
         //   Log.i("writeDescriptor", "writeDescriptor STATUS " + writeDescriptor);
        }
        return STATUS && ENABLE_NOTIFICATION_VALUE && writeDescriptor;
    }

    /**
     * Returns a characteristic with a given UUID out of the list of
     * characteristics offered by this service.
     *
     * <p>This is a convenience function to allow access to a given characteristic
     * without enumerating over the list returned by {@link BluetoothGattService#getCharacteristics}
     * manually.
     *
     * <p>If a remote service offers multiple characteristics with the same
     * UUID, the first instance of a characteristic with the given UUID
     * is returned.
     *
     * @return GATT characteristic object or null if no characteristic with the given UUID was
     * found.
     */
    public static BluetoothGattCharacteristic getCharacteristic(String service, String characteristic) {
        BluetoothGattService service1 = mBluetoothGatt.getService(UUID.fromString(service));

        if (service1 != null)
            return service1.getCharacteristic(UUID.fromString(characteristic));

        return null;
    }
    
    @Deprecated
    public static int ConnectionState(){
        return mConnectionState;
    }
    @Deprecated
    public static boolean isConnected() {
        return ConnectionState() == STATE_CONNECTED;
    }

    public static boolean isScanning() {
        return mScanning;
    }

    /**
     * usado em {@link #scanLeDevice(OnScanStateChangeListener, OnScanDiscoveredListener)}
     * @param scanPeriod
     * 
     * 
     */
    public static void setScanPeriod(int scanPeriod) {
        SCAN_PERIOD = scanPeriod;
    }

    public static int getScanPeriod() {
        return SCAN_PERIOD;
    }
/**
 *  Array of services to look for
 */
    public static ArrayList<String> useFilterService() {
     //   FILTER_SERVICE = filterService;
        return FILTER_SERVICE;
    }

    public static BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }
    
    public static ArrayList<BluetoothLE> getListDevices() {
        return aDevices;
    }
    
    public static BluetoothAdapter getAdapter() {
        return mBluetoothAdapter;
    }
    public static BleCallback useCallback()
    {
       return mGattCallback.getCallback();
    }

}