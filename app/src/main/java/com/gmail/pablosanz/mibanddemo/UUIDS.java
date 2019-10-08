package com.gmail.pablosanz.mibanddemo;

import java.util.UUID;

public class UUIDS {
    public static String BASE = "0000%s-0000-1000-8000-00805f9b34fb";
    public static UUID SERVICE_MIBAND1=UUID.fromString(String.format(BASE,"fee0"));
    public static UUID SERVICE_MIBAND2=UUID.fromString(String.format(BASE,"fee1"));
    public static UUID SERVICE_ALERT =UUID.fromString(String.format(BASE,"1802"));
    public static UUID SERVICE_ALERT_NOTIFICATION = UUID.fromString(String.format(BASE,"1811"));
    public static UUID SERVICE_HEART_RATE = UUID.fromString(String.format(BASE,"180d"));
    public static UUID SERVICE_DEVICE_INFO = UUID.fromString(String.format(BASE,"180a"));

    public static UUID CHARACTERISTIC_HZ = UUID.fromString("00000002-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_SENSOR = UUID.fromString("00000001-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_HEART_RATE_MEASURE = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_HEART_RATE_CONTROL = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_ALERT = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTIC_BATTERY = UUID.fromString("00000006-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_LE_PARAMS = UUID.fromString(String.format(BASE,"FF09"));
    public static int CHARACTERISTIC_REVISION = 0x2a28;
    public static int CHARACTERISTIC_SERIAL = 0x2a25;
    public static int CHARACTERISTIC_HRDW_REVISION = 0x2a27;
    public static UUID CHARACTERISTIC_CONFIGURATION = UUID.fromString("00000003-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_DEVICEEVENT = UUID.fromString("00000010-0000-3512-2118-0009af100700");

    public static UUID CHARACTERISTIC_CURRENT_TIME = UUID.fromString(String.format(BASE,"2A2B"));
    public static UUID CHARACTERISTIC_AGE = UUID.fromString(String.format(BASE,"2A80"));
    public static UUID CHARACTERISTIC_USER_SETTINGS = UUID.fromString("00000008-0000-3512-2118-0009af100700");

    public static UUID CHARACTERISTIC_ACTIVITY_DATA = UUID.fromString("00000005-0000-3512-2118-0009af100700");
    public static UUID CHARACTERISTIC_FETCH = UUID.fromString("00000004-0000-3512-2118-0009af100700");

    public static int NOTIFICATION_DESCRIPTOR = 0x2902;

}
