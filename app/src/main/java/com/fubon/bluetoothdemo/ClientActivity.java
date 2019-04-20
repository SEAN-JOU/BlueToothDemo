package com.fubon.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.UUID;


public class ClientActivity extends AppCompatActivity {


    private static final int CONN_SUCCESS = 0x1;
    private static final int CONN_FAIL = 0x2;
    private static final int RECEIVER_INFO = 0x3;
    private static final int SET_EDITTEXT_NULL = 0x4;
    private static final String BLUETOOTH_UUID = "00001101-0000-1000-ffff-00805F9B34FB";

    Button send;
    TextView chatview;
    EditText editText;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket bluetoothSocket = null;

    PrintStream out;
    BufferedReader in;
    private boolean isReceiver = true;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECEIVER_INFO:
                    setInfo(msg.obj.toString() + "\n");
                    break;
                case SET_EDITTEXT_NULL:
                    editText.setText("");
                    break;
                case CONN_SUCCESS:
                    setInfo("連線成功\n");
                    send.setEnabled(true);
                    System.out.println("Name"+bluetoothDevice.getName());
                    System.out.println("Uuids"+bluetoothDevice.getUuids());
                    System.out.println("Address"+bluetoothDevice.getAddress());
                    new Thread(new ReceiverInfoThread()).start();
                    break;
                case CONN_FAIL:
                    setInfo("連線失敗\n");
                    setInfo(msg.obj.toString()+"\n");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        setTitle("client");
        send = findViewById(R.id.send1);
        chatview = findViewById(R.id.chatview1);
        editText= findViewById(R.id.editText1);


        String mac=getIntent().getExtras().get("MAC").toString();

        if(mac != null) {
            init(mac);
        }


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = editText.getText().toString();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(ClientActivity.this,"不能發送空值",Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        out.println(content);
                        out.flush();
                        handler.sendEmptyMessage(SET_EDITTEXT_NULL);

                    }
                }).start();

            }
        });
    }


    private void init(final String mac) {
        chatview.setText("正在與服務器連接.....");

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

                   bluetoothDevice= bluetoothAdapter.getRemoteDevice(mac);

                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_UUID));

                    if (bluetoothSocket != null) {

                        bluetoothSocket.connect();
                        out = new PrintStream(bluetoothSocket.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
                    }

                    handler.sendEmptyMessage(CONN_SUCCESS);

                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage(CONN_FAIL, e.getLocalizedMessage());
                    handler.sendMessage(message);
                }
            }
        }).start();
    }


    private void setInfo(String info) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(chatview.getText());
        stringBuilder.append(info);
        chatview.setText(stringBuilder);
    }

    private class ReceiverInfoThread implements Runnable {
        @Override
        public void run() {
            String info = null;
            while (isReceiver) {
                try {
                    info = in.readLine();
                    Message message = handler.obtainMessage(RECEIVER_INFO, info);
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
