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

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_DISCOVER = "com.example.devicerpc.action.DISCOVER";
    private static final String ACTION_PAIR = "com.example.devicerpc.action.PAIR";
    private static final String ACTION_UNPAIR = "com.example.devicerpc.action.UNPAIR";

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
    }


    private final BroadcastReceiver IntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_DISCOVER)) {
                Intent visibilityIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                visibilityIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
                startActivityForResult(visibilityIntent, 2);
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_PAIR)) {
                String macAddress = intent.getStringExtra("data");
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
                bluetoothDevice.createBond();
            }
        }
    };

    private final BroadcastReceiver unpairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UNPAIR)) {
                try {
                    String macAddress = intent.getStringExtra("data");
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
                    Method removeBondMethod = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                    removeBondMethod.invoke(bluetoothDevice, (Object[]) null);
                } catch (Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoverReceiver);
        unregisterReceiver(pairReceiver);
        unregisterReceiver(unpairReceiver);
    }
}