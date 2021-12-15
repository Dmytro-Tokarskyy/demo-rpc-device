package com.example.devicerpc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_DISCOVER = "com.example.devicerpc.action.DISCOVER";
    private static final String ACTION_PAIR = "com.example.devicerpc.action.PAIR";
    private static final String ACTION_UNPAIR = "com.example.devicerpc.action.UNPAIR";
    private static final String ACTION_COMMAND = "com.example.devicerpc.action.COMMAND";

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter discoverIntent = new IntentFilter(ACTION_DISCOVER);
        registerReceiver(discoverReceiver, discoverIntent);

        IntentFilter pairIntent = new IntentFilter(ACTION_PAIR);
        registerReceiver(pairReceiver, pairIntent);

        IntentFilter unpairIntent = new IntentFilter(ACTION_UNPAIR);
        registerReceiver(unpairReceiver, unpairIntent);

        IntentFilter commandIntent = new IntentFilter(ACTION_COMMAND);
        registerReceiver(IntentReceiver, commandIntent);
    }


    private final BroadcastReceiver IntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_COMMAND)) {
                String data = intent.getStringExtra("data");
                String method = "";
                String params = "";
                try {
                    JSONObject requestJson = new JSONObject(data);
                    if (requestJson.has("method")) {
                        method = requestJson.getString("method");
                        params = requestJson.getString("params");

                        switch (method) {
                            case "discover":
                                discover();
                                break;
                            case "pair":
                                pair(params);
                                break;
                            case "unpair":
                                unpair(params);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void discover() {
        Intent visibilityIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        visibilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
        startActivityForResult(visibilityIntent, 2);
    }

    private void pair(String macAddress) {
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
        bluetoothDevice.createBond();
    }

    private void unpair(String macAddress) {
        try {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
            Method removeBondMethod = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
            removeBondMethod.invoke(bluetoothDevice, (Object[]) null);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_DISCOVER)) {
                discover();
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_PAIR)) {
                pair(intent.getStringExtra("data"));
            }
        }
    };

    private final BroadcastReceiver unpairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UNPAIR)) {
                unpair(intent.getStringExtra("data"));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoverReceiver);
        unregisterReceiver(pairReceiver);
        unregisterReceiver(unpairReceiver);
        unregisterReceiver(IntentReceiver);
    }
}