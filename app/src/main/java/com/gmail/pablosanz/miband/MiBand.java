package com.gmail.pablosanz.miband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.util.Log;

import com.gmail.pablosanz.miband.listeners.HeartRateNotifyListener;
import com.gmail.pablosanz.miband.listeners.NotifyListener;
import com.gmail.pablosanz.miband.listeners.RealtimeStepsNotifyListener;
import com.gmail.pablosanz.miband.model.BatteryInfo;
import com.gmail.pablosanz.miband.model.LedColor;
import com.gmail.pablosanz.miband.model.Profile;
import com.gmail.pablosanz.miband.model.Protocol;
import com.gmail.pablosanz.miband.model.UserInfo;
import com.gmail.pablosanz.miband.model.VibrationMode;

import java.util.Arrays;
import java.util.UUID;

public class MiBand {

    private static final String TAG = "miband-android";

    private Context context;
    private BluetoothIO io;

    public MiBand(Context context) {
        this.context = context;
        this.io = new BluetoothIO();

    }

    public static void startScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.startScan(callback);
    }

    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.stopScan(callback);
    }

    /**
     * 连接指定的手环
     *
     * @param callback
     */
    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    /**
     * 和手环配对, 实际用途未知, 不配对也可以做其他的操作
     *
     * @return data = null
     */
    public void pair(final ActionCallback callback) {
        /*ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "pair result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 1 && characteristic.getValue()[0] == 2) {
                    callback.onSuccess(null);
                } else {
                    callback.onFail(-1, "respone values no succ!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.writeAndRead(Profile.UUID_CHAR_PAIR, Protocol.PAIR, ioCallback);*/
    }

    public BluetoothDevice getDevice() {
        return this.io.getDevice();
    }

    /**
     * 读取和连接设备的信号强度RSSI值
     *
     * @param callback
     * @return data : int, rssi值
     */
    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

    /**
     * 读取手环电池信息
     *
     * @return {@link BatteryInfo}
     */
    public void getBatteryInfo(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 10) {
                    BatteryInfo info = BatteryInfo.fromByteData(characteristic.getValue());
                    callback.onSuccess(info);
                } else {
                    callback.onFail(-1, "result format wrong!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHAR_BATTERY, ioCallback);
    }

    /**
     * 让手环震动
     */
    public void startVibration(VibrationMode mode) {
        byte[] protocal;
        switch (mode) {
            case VIBRATION_WITH_LED:
                protocal = Protocol.VIBRATION_WITH_LED;
                break;
            case VIBRATION_10_TIMES_WITH_LED:
                protocal = Protocol.VIBRATION_10_TIMES_WITH_LED;
                break;
            case VIBRATION_WITHOUT_LED:
                protocal = Protocol.VIBRATION_WITHOUT_LED;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, protocal, null);
    }

    /**
     * 停止以模式Protocol.VIBRATION_10_TIMES_WITH_LED 开始的震动
     */
    public void stopVibration() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, Protocol.STOP_VIBRATION, null);
    }

    public void setNormalNotifyListener(NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_NOTIFICATION, listener);
    }

    /**
     * 重力感应器数据通知监听, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     * {@link MiBand##disableRealtimeStepsNotify} 关闭通知
     *
     * @param listener
     */
    public void setSensorDataNotifyListener(final NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });
    }

    /**
     * 开启重力感应器数据通知
     */
    public void enableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    /**
     * 关闭重力感应器数据通知
     */
    public void disableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
    }

    /**
     * 实时步数通知监听器, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     * {@link MiBand##disableRealtimeStepsNotify} 关闭通知
     *
     * @param listener
     */
    public void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_REALTIME_STEPS, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 4) {
                    int steps = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
                    listener.onNotify(steps);
                }
            }
        });
    }

    /**
     * 开启实时步数通知
     */
    public void enableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, null);
    }

    /**
     * 关闭实时步数通知
     */
    public void disableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null);
    }

    /**
     * 设置led灯颜色
     */
    public void setLedColor(LedColor color) {
        byte[] protocal;
        switch (color) {
            case RED:
                protocal = Protocol.SET_COLOR_RED;
                break;
            case BLUE:
                protocal = Protocol.SET_COLOR_BLUE;
                break;
            case GREEN:
                protocal = Protocol.SET_COLOR_GREEN;
                break;
            case ORANGE:
                protocal = Protocol.SET_COLOR_ORANGE;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, protocal, null);
    }

    /**
     * 设置用户信息
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        BluetoothDevice device = this.io.getDevice();
        byte[] data = userInfo.getBytes(device.getAddress());
        this.io.writeCharacteristic(Profile.UUID_CHAR_USER_INFO, data, null);
    }

    public void showServicesAndCharacteristics() {
        for (BluetoothGattService service : this.io.gatt.getServices()) {
            Log.d(TAG, "onServicesDiscovered:" + service.getUuid());

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(TAG, "  char:" + characteristic.getUuid());


                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(TAG, "    descriptor:" + descriptor.getUuid());
                }
            }
        }
    }

    public void setHeartRateScanListener(final HeartRateNotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_NOTIFICATION_HEARTRATE, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 2 && data[0] == 6) {
                    int heartRate = data[1] & 0xFF;
                    listener.onNotify(heartRate);
                }
            }
        });
    }

    public void startHeartRateScan() {

        MiBand.this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEARTRATE, Protocol.START_HEART_RATE_SCAN, null);
    }


    public void authenticate() {
        try {
            final BluetoothGattService serv1 = this.io.gatt.getService(UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb"));
            final BluetoothGattCharacteristic notificationChar = serv1.getCharacteristic(UUID.fromString("0000fec1-0000-3512-2118-0009af100700"));
            /*io.gatt.setCharacteristicNotification(notificationChar,true);
            io.setNotifyListener(serv1.getUuid(), notificationChar.getUuid(), new NotifyListener() {
                @Override
                public void onNotify(byte[] data) {
                    Log.d(TAG,"Notificado");
                }
            });*/
            /*BluetoothGattDescriptor notificationDescriptor = notificationChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            notificationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            io.gatt.writeDescriptor(notificationDescriptor);*/
            final BluetoothGattCharacteristic authChar=serv1.getCharacteristic(UUID.fromString("00000009-0000-3512-2118-0009af100700"));
            io.setCharacteristicNotification(authChar, true, new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG,"llegamos hasta acá");
                    io.writeCharacteristicWithNoResponse(serv1.getUuid() , authChar.getUuid() ,
                            new byte[]{0x01, 0x00,
                                    0x01, 0x02, 0x03, 0x04,
                                    0x05, 0x06, 0x07, 0x08,
                                    0x09, 0x10, 0x11, 0x12,
                                    0x13, 0x14, 0x15, 0x16
                            },
                            new ActionCallback() {
                                @Override
                                public void onSuccess(Object data) {
                                    io.writeCharacteristicWithNoResponse(UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb") ,
                                            UUID.fromString("00000009-0000-3512-2118-0009af100700"), new byte[]{0x02, 0x00}, new ActionCallback() {
                                                @Override
                                                public void onSuccess(Object data) {
                                                    Log.d(TAG, "a ver " + data);
                                                }
                                                @Override
                                                public void onFail(int errorCode, String msg) {
                                                    Log.d(TAG, "algo fallo");
                                                }
                                            });
                                }
                                @Override
                                public void onFail(int errorCode, String msg) {
                                    Log.d(TAG, "error");
                                }
                            });
                }

                @Override
                public void onFail(int errorCode, String msg) {
                    Log.d(TAG,"NO llegamos hasta acá");
                }
            });
            io.setNotifyListener(serv1.getUuid(), authChar.getUuid(), new NotifyListener() {
                @Override
                public void onNotify(byte[] data) {
                    Log.d(TAG,"Notificado");

                }
            });
            //io.gatt.setCharacteristicNotification(authChar,true);

        }
        catch(Exception e) {
            Log.d(TAG,e.getMessage());

        }
    }

}
