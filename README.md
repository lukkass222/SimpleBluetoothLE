# SimpleBluetoothLE
 This project is a simple interface to facilitate the use of the native Android API, Bluetooth le (BLE), which allows basic operations with BLE.

 ## Introduction

This project is a simple interface to facilitate the use of the native API of Android, Bluetooth le (BLE), I hope that this library saves you programming time :)

Add the following in your app's build.gradle file:

```
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
![Warning](img/warning.png) **Min SDK version is 18** ![Warning](img/warning.png)
```
    defaultConfig {
        minSdkVersion 18
        ...
    }

    dependencies {
        implementation "com.github.lukkass222:SimpleBluetoothLE:0.9.0"
    }
```
## Usage

* **Required**

1) checking support

```
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothManagerLE.isSupported(this);
    }
```

2) configure the callbacks

//examples:
```

        BluetoothManagerLE.useCallback().setListener(new BleCallback.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i(TAG, "connection status: " + newState);
                if (status != BluetoothGatt.GATT_SUCCESS)
                    return;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }
        });

         BluetoothManagerLE.useCallback().setListener(new BleCallback.OnScanDiscoveredListener() {
            @Override
            public void onScanDiscovered(BluetoothLE bluetoothLE) {
                //code
            }
        });

```

3) scan devices in the area.
```
            if (!BluetoothManagerLE.isEnabled(this))
                return;

            BluetoothManagerLE.disconnect();
            BluetoothManagerLE.scanLeDevice(new BleCallback.OnScanStateChangeListener() {
                @Override
                public void onStateChange(boolean isScanning) {
                    Log.i(TAG, isScanning ? "scan started!" : "scan finish!");
                }
            }, null);
```

4) check the list of found devices
```
    BluetoothManagerLE.getListDevices();
```

5) Connect to a found device
```
    BluetoothManagerLE.connect(this, address);
```
6) get a characteristic
```
BluetoothGattCharacteristic readCharacteristic = BluetoothManagerLE.getCharacteristic("00001800-0000-1000-8000-00805f9b34fb", "00002a00-0000-1000-8000-00805f9b34fb");

```

7) releasing the callbacks

```
@Override
public void onDestroy() {
    super.onDestroy();

    BluetoothManagerLE.useCallback().setListener((BleCallback.OnConnectionStateChangeListener) null);
    BluetoothManagerLE.useCallback().setListener((BleCallback.OnScanDiscoveredListener) null);
    BluetoothManagerLE.useCallback().setListener((BleCallback.OnScanStateChangeListener) null);

    if (BluetoothManagerLE.isScanning())
        BluetoothManagerLE.stopScanDevice();
    }
```

* **Write in Ble devices**

1) send data to the device

```
BluetoothManagerLE.write(writerCharacteristic, message.getBytes());
```

* **Read in Ble devices**

1) read the Device Characteristic

```
           BluetoothManagerLE.read(readCharacteristic, new BleCallback.OnCharacteristicReadListener() {
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status != BluetoothGatt.GATT_SUCCESS)
                        return;
                    String text = new String(characteristic.getValue());
                   
                }
            });
```
* **Receive from Ble device**

1) receive data from device

```
if (Functions.isCharacteristicNotifiable(characteristic)) {
    BluetoothManagerLE.setNotifiable(characteristic, new BleCallback.OnCharacteristicChangeListener() {
       @Override
       public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
           String text = new String(characteristic.getValue());
           Log.i("BluetoothGattCallback", "Characteristic changed: "+ text);
       }
    });
}
```


Usage examples are located in:
- [`/Example`](example/src/main/java/com/lukkass/example/exampleActivity.java)

## License

This code is open-sourced software licensed under the [MIT license.](https://opensource.org/licenses/MIT)
