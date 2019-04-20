package com.fubon.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
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
import java.net.Socket;
import java.util.UUID;

public class ServerActivity extends AppCompatActivity {


    private static final int CONN_SUCCESS = 0x1;
    private static final int CONN_FAIL = 0x2;
    private static final int RECEIVER_INFO = 0x3;
    private static final int SET_EDITTEXT_NULL = 0x4;
    private static final String BLUETOOTH_UUID = "00001101-0000-1000-ffff-00805F9B34FB";

    Button send;
    TextView chatview;
    EditText editText;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothServerSocket bluetoothServerSocket = null;
    BluetoothSocket bluetoothSocket = null;
    PrintStream out;//打印流
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
        setContentView(R.layout.activity_server);

        setTitle("server");
        send = findViewById(R.id.send);
        chatview = findViewById(R.id.chatview);
        editText= findViewById(R.id.editText);
        init();


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = editText.getText().toString();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(ServerActivity.this,"不能發送空值",Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                            out.println(content);//打印
                            out.flush();//沖洗掉
                            handler.sendEmptyMessage(SET_EDITTEXT_NULL);

                    }
                }).start();

            }
        });

    }

    private void init() {
        chatview.setText("服務器已啟動.....");

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    bluetoothAdapter =BluetoothAdapter.getDefaultAdapter();// 检查设备是否支持蓝牙

                    bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("test", UUID.fromString(BLUETOOTH_UUID));//監聽RCF是否有符合的UUID

                    bluetoothSocket = bluetoothServerSocket.accept();//直到接收到了客户端的请求


                    if (bluetoothSocket != null) {
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

    private void setInfo(String info) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(chatview.getText());
        stringBuilder.append(info);
        chatview.setText(stringBuilder);
    }
}
