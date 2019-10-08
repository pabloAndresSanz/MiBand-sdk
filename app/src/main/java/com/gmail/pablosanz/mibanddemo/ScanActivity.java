package com.gmail.pablosanz.mibanddemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.pablosanz.miband.MiBand;
import com.zhaoxiaodan.mibanddemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScanActivity extends Activity {
    private static final String TAG = "==[mibandtest]==";
    private MiBand miband;


    HashMap<String, BluetoothDevice> devices = new HashMap<String, BluetoothDevice>();

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!wantedPermissions.isEmpty())
            ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[wantedPermissions.size()]), 0);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==0) {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        miband = new MiBand(this);

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.item, new ArrayList<String>());

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                Log.d(TAG,
                        "Device: name:" + device.getName() + ",uuid:"
                                + device.getUuids() + ",add:"
                                + device.getAddress() + ",type:"
                                + device.getType() + ",bondState:"
                                + device.getBondState() + ",rssi:" + result.getRssi());

                String item = device.getName() + "|" + device.getAddress();
                if (device.getName()!=null) {
                    if (!devices.containsKey(item)) {
                        devices.put(item, device);
                        adapter.add(item);
                    }
                }

            }
        };


        ((Button) findViewById(R.id.starScanButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Starting scan...");
                MiBand.startScan(scanCallback);
            }
        });

        ((Button) findViewById(R.id.stopScanButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stoping scan...");
                MiBand.stopScan(scanCallback);
            }
        });


        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                if (devices.containsKey(item)) {

                    Log.d(TAG, "停止扫描...");
                    MiBand.stopScan(scanCallback);

                    BluetoothDevice device = devices.get(item);
                    Intent intent = new Intent();
                    intent.putExtra("device", device);
                    intent.setClass(ScanActivity.this, MainActivity.class);
                    ScanActivity.this.startActivity(intent);
                    ScanActivity.this.finish();
                }
            }
        });

    }
}
