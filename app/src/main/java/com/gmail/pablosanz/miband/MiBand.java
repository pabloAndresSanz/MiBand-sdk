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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

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

    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

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

    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

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

    public void stopVibration() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, Protocol.STOP_VIBRATION, null);
    }

    public void setNormalNotifyListener(NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_NOTIFICATION, listener);
    }

    public void setSensorDataNotifyListener(final NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });
    }

    public void enableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    public void disableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
    }

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

    public void enableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, null);
    }

    public void disableRealtimeStepsNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null);
    }

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

    public void setUserInfo(UserInfo userInfo, ActionCallback callback) {
        BluetoothDevice device = this.io.getDevice();
        byte[] data = userInfo.getBytes(device.getAddress());
        this.io.writeCharacteristic(UUID.fromString("00000004-0000-3512-2118-0009af100700")
                //Profile.UUID_CHAR_USER_INFO
                , data, callback);
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

    private byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
    private byte[] KEY=new byte[] {0x01, 0x02, 0x03, 0x04,
            0x05, 0x06, 0x07, 0x08,
            0x09, 0x10, 0x11, 0x12,
            0x13, 0x14, 0x15, 0x16
    };
    public void authenticate() {
        try {
            final BluetoothGattService serv1 = this.io.gatt.getService(UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb"));
            final BluetoothGattCharacteristic authChar = serv1.getCharacteristic(UUID.fromString("00000009-0000-3512-2118-0009af100700"));
            io.setNotifyListener(serv1.getUuid(), authChar.getUuid(), new NotifyListener() {
                @Override
                public void onNotify(byte[] data) {
                    if (data[0]==16 & data[1]==2 & data[2]==1) {
                        //viene la cadena a encriptar desde el data[3] en adelante 16 posiciones
                        byte[] aEncriptar=Arrays.copyOfRange(data,3,19);
                        try {
                            byte[] encriptado=encrypt(KEY,aEncriptar);
                            byte[] aEnviar=new byte[encriptado.length+2];
                            aEnviar[0]=0x03;
                            aEnviar[1]=0x00;
                            for(int i=0;i<encriptado.length;i++) {
                                aEnviar[2+i]=encriptado[i];
                            }
                            io.writeCharacteristic(serv1.getUuid(), authChar.getUuid(), aEnviar, new ActionCallback() {
                                @Override
                                public void onSuccess(Object data) {
                                    Log.d(TAG,"exito?");
                                }

                                @Override
                                public void onFail(int errorCode, String msg) {
                                    Log.d(TAG,"fallo");
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (data[0]==16 & data[1]==2 & data[2]==4) {
                        UserInfo userInfo = new UserInfo(17902323, 1, 32, 180, 90, "pablo", 0);
                        Log.d(TAG, "setUserInfo:" + userInfo.toString());
                        MiBand.this.setUserInfo(userInfo, new ActionCallback() {
                            @Override
                            public void onSuccess(Object data) {
                                MiBand.this.setHeartRateScanListener(new HeartRateNotifyListener() {
                                    @Override
                                    public void onNotify(int i) {
                                        Log.d(TAG,"leyendo pulsaciones " + String.valueOf(i));
                                    }
                                });
                                final BluetoothGattService heartServ = io.gatt.getService(Profile.UUID_SERVICE_HEARTRATE);
                                final BluetoothGattCharacteristic heartChar = heartServ.getCharacteristic(Profile.UUID_NOTIFICATION_HEARTRATE);
                                io.setCharacteristicNotification(heartChar, true, new ActionCallback() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        MiBand.this.startHeartRateScan();
                                    }
                                    @Override
                                    public void onFail(int errorCode, String msg) {
                                        Log.d(TAG,"falló al setear notificaciones de corazón");
                                    }
                                });
                            }

                            @Override
                            public void onFail(int errorCode, String msg) {
                                Log.d(TAG,"fallo ");
                            }
                        });
                    }
                    Log.d(TAG,"you are notified");
                }
            });
            io.setCharacteristicNotification(authChar, true, new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    byte[] enviar=new byte[18];
                    enviar[0]=0x01;
                    enviar[1]=0x00;
                    for (int i=0;i<16;i++) {
                        enviar[2+i]=KEY[i];
                    }
                    io.writeCharacteristic(serv1.getUuid(), authChar.getUuid(),
                            enviar, new ActionCallback() {
                                @Override
                                public void onSuccess(Object data) {
                                    io.writeCharacteristic(serv1.getUuid(), authChar.getUuid(),
                                            new byte[]{0x02, 0x00}, new ActionCallback() {
                                                @Override
                                                public void onSuccess(Object data) {
                                                    Log.d(TAG,"llegamos");
                                                }

                                                @Override
                                                public void onFail(int errorCode, String msg) {
                                                    Log.d(TAG,"fallo");
                                                }
                                            });
                                }

                                @Override
                                public void onFail(int errorCode, String msg) {
                                    Log.d(TAG,"fallo");
                                }
                            });
                }
                @Override
                public void onFail(int errorCode, String msg) {
                    Log.d(TAG,"fallo");
                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());

        }
    }

}
