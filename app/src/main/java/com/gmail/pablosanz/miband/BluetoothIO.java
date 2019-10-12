package com.gmail.pablosanz.miband;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.drm.DrmStore;
import android.util.Log;

import com.gmail.pablosanz.miband.listeners.NotifyListener;
import com.gmail.pablosanz.miband.model.Profile;

import java.util.HashMap;
import java.util.UUID;

class BluetoothIO extends BluetoothGattCallback {
    private static final String TAG = "BluetoothIO";
    BluetoothGatt gatt;
    ActionCallback currentCallback;

    HashMap<UUID, NotifyListener> notifyListeners = new HashMap<UUID, NotifyListener>();
    NotifyListener disconnectedListener = null;

    public void connect(final Context context, BluetoothDevice device, final ActionCallback callback) {
        BluetoothIO.this.currentCallback = callback;
        device.connectGatt(context, false, BluetoothIO.this);

    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.disconnectedListener = disconnectedListener;
    }

    public BluetoothDevice getDevice() {
        if (null == gatt) {
            Log.e(TAG, "connect to miband first");
            return null;
        }
        return gatt.getDevice();
    }

    public void cmdWriteAndRead(final UUID service,final UUID uuid, byte[] valueToWrite, final ActionCallback callback) {
        ActionCallback readCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object characteristic) {
                BluetoothIO.this.readCharacteristic(service, uuid, callback);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.cmdWriteCharacteristic(service, uuid, valueToWrite, readCallback);
    }

    public void writeCharacteristic(UUID characteristicUUID, byte[] value, ActionCallback callback) {
        writeCharacteristic(Profile.UUID_SERVICE_MILI, characteristicUUID, value, callback);
    }

    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " not exist");
                return;
            }
            chara.setValue(value);

            if (false == this.gatt.writeCharacteristic(chara)) {
                this.onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void writeCharacteristicWithNoResponse(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " not exist");
                return;
            }
            chara.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            chara.setValue(value);

            if (false == this.gatt.writeCharacteristic(chara)) {
                this.onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void cmdWriteCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " not exist");
                return;
            }
            chara.setValue(value);
            this.gatt.writeCharacteristic(chara);
            this.onSuccess(null);
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }


    public void readCharacteristic(UUID serviceUUID, UUID uuid, ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(uuid);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
                return;
            }
            if (false == this.gatt.readCharacteristic(chara)) {
                this.onFail(-1, "gatt.readCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "readCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID uuid, ActionCallback callback) {
        this.readCharacteristic(Profile.UUID_SERVICE_MILI, uuid, callback);
    }

    public void readRssi(ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            this.gatt.readRemoteRssi();
        } catch (Throwable tr) {
            Log.e(TAG, "readRssi", tr);
            this.onFail(-1, tr.getMessage());
        }

    }

    /*public void setNotifyListener(UUID serviceUUID, UUID characteristicId, NotifyListener listener) {
        if (null == gatt) {
            Log.e(TAG, "connect to miband first");
            return;
        }

        BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicId);
        if (chara == null) {
            Log.e(TAG, "characteristicId " + characteristicId.toString() + " not found in service " + serviceUUID.toString());
            return;
        }


        this.gatt.setCharacteristicNotification(chara, true);
        BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.gatt.writeDescriptor(descriptor);
        this.notifyListeners.put(characteristicId, listener);
    }*/
    public void setNotifyListener(UUID serviceUUID, UUID charaUUID, NotifyListener listener) {
        //enable chara notofication
        if (this.gatt == null ) {
            Log.d(TAG, "Device is not connected or services are not discovered");
            return;
        }

        BluetoothGattCharacteristic chara = this.gatt.getService(serviceUUID).getCharacteristic(charaUUID);

        Log.d(TAG, "setCharacteristicNotification: " + this.gatt.setCharacteristicNotification(chara, true));
        BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        Log.d(TAG, "setValue: " + descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
        Log.d(TAG, "writeDescriptor: " + this.gatt.writeDescriptor(descriptor));
        this.notifyListeners.put(charaUUID, listener);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            gatt.close();
            if (this.disconnectedListener != null)
                this.disconnectedListener.onNotify(null);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicWrite fail");
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(rssi);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.gatt = gatt;
            this.onSuccess(null);
        } else {
            this.onFail(status, "onServicesDiscovered fail");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if(characteristic!=null) {
            if (this.notifyListeners.containsKey(characteristic.getUuid())) {
                this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
            }
        }
    }

    private void onSuccess(Object data) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onSuccess(data);
        }
    }

    private void onFail(int errorCode, String msg) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onFail(errorCode, msg);
        }
    }

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                 boolean enable,ActionCallback callback ) {
        this.currentCallback=callback;
        return gatt.setCharacteristicNotification(characteristic,enable);
    }

        @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if(currentCallback!=null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                currentCallback.onSuccess(descriptor);
            } else {
                currentCallback.onFail(-1, "error writing descriptor");
            }
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }

    public void writeDescriptor(BluetoothGattDescriptor descriptor,ActionCallback callback) {
        currentCallback=callback;
        gatt.writeDescriptor(descriptor);
    }
}
