package com.fubon.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button bt1, bt2, cbt, scan,server,client;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        bt1 = findViewById(R.id.bt1);

        bt2 = findViewById(R.id.bt2);

        cbt = findViewById(R.id.cbt);

        scan = findViewById(R.id.scan);

        server=findViewById(R.id.server);

        client=findViewById(R.id.client);

        editText=findViewById(R.id.editText);

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 5000);
                startActivity(intent);
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.enable();
                Toast.makeText(MainActivity.this, "藍芽已開啟", Toast.LENGTH_SHORT).show();
            }
        });
        cbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.disable();
                Toast.makeText(MainActivity.this, "藍芽已關閉", Toast.LENGTH_SHORT).show();
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.startDiscovery();//掃描藍芽設備


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(receiver, filter);
                }
                else
                {
                    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                    for(BluetoothDevice device:devices)
                    {
                        System.out.println(device.getName()+"  "+device.getAddress());
                    }
                }
            }
        });

        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ServerActivity.class));
            }
        });

        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editText.getText().toString().isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                    intent.putExtra("MAC", editText.getText().toString().trim());
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this,"請填入MAC",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                System.out.println(deviceName+ " " + deviceHardwareAddress);
            }
        }
    };
}
