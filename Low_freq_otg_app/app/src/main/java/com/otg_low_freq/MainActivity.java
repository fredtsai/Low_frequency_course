package com.otg_low_freq;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import static java.lang.Thread.sleep;

public class MainActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    private int start_flag=0;

    private EditText power_text, mode_text, state_text;
    private boolean isOpen;
    private Handler handler;
    private MainActivity activity;

    private Button AddPowerBtn, MinusPowerBtn, ConnectBtn, TurnOnBtn, TurnOffBtn, StartBtn;

    public byte[] writeBuffer;
    public byte[] readBuffer;

    public int baudRate;
    public byte stopBit;
    public byte dataBit;
    public byte parity;
    public byte flowControl;

    private IntentFilter usbDeviceStateFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MyApp.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);
        initUI();

        final Intent intent = getIntent();

        if (!MyApp.driver.UsbFeatureSupported())
        {
            Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("��ʾ")
                    .setMessage("USB HOST")
                    .setPositiveButton("ȷ��",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    System.exit(0);
                                }
                            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ���ֳ�������Ļ��״̬
        writeBuffer = new byte[512];
        readBuffer = new byte[512];
        isOpen = false;
        activity = this;

        usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    ConnectBtn.setText("連線至低週波裝置");
                    ConnectBtn.setEnabled(true);
                    StartBtn.setEnabled(false);
                    AddPowerBtn.setEnabled(false);
                    MinusPowerBtn.setEnabled(false);
                    TurnOnBtn.setEnabled(false);
                    TurnOffBtn.setEnabled(false);
                    mode_text.setText("");
                    state_text.setText("");
                    power_text.setText("");
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                }
            }
        };

        registerReceiver(mUsbReceiver, usbDeviceStateFilter);

        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start_flag ==0){
                    byte[] to_sned = toByteArray("70");
                    MyApp.driver.WriteData(to_sned, to_sned.length);
                    start_flag=1;
                    StartBtn.setText("結束");
                }
                else{
                    byte[] to_sned = toByteArray("71");
                    MyApp.driver.WriteData(to_sned, to_sned.length);
                    start_flag=0;
                    StartBtn.setText("開始");
                }
            }
        });

        AddPowerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                byte[] to_send = toByteArray("62");
                MyApp.driver.WriteData(to_send, to_send.length);
            }
        });

        MinusPowerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                byte[] to_send = toByteArray("63");
                MyApp.driver.WriteData(to_send, to_send.length);
            }
        });

        TurnOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                byte[] to_send = toByteArray("80");
                MyApp.driver.WriteData(to_send, to_send.length);
            }
        });

        TurnOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                byte[] to_send = toByteArray("81");
                MyApp.driver.WriteData(to_send, to_send.length);
            }
        });

        ConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                OpenUART();
            }
        });


        handler = new Handler() {
            public void handleMessage(Message msg) {
                String get_data = (String) msg.obj;
                Log.d("FREDTEST ", get_data);

                String type_data = get_data.substring(0,5);
                if(type_data.trim().equals("aa 11")) {
                    String stop_data = get_data.substring(15,17);;
                    if(stop_data.trim().equals("ff")) {
                        String low_freq_data_0 = get_data.substring(6, 8);
                        String low_freq_data_1 = get_data.substring(9, 11);
                        String low_freq_data_2 = get_data.substring(12, 14);

                        int value0 = Integer.decode("0x" + low_freq_data_0);
                        int value1 = Integer.decode("0x" + low_freq_data_1);
                        int value2 = Integer.decode("0x" + low_freq_data_2);

                        power_text.setText(String.valueOf(value0));


                        if(value1 == 1){
                            mode_text.setText("拍打");
                            start_flag=1;
                            StartBtn.setText("結束");
                        }
                        else if(value1 == 2){
                            mode_text.setText("按壓");
                            start_flag=1;
                            StartBtn.setText("結束");
                        }
                        else if(value1 == 3){
                            mode_text.setText("揉捏");
                            start_flag=1;
                            StartBtn.setText("結束");
                        }
                        else if(value1 == 4){
                            mode_text.setText("停止");
                            start_flag=0;
                            StartBtn.setText("開始");
                        }

                        if(value2 ==0) {
                            state_text.setText("脫落");
                            start_flag=0;
                            StartBtn.setText("結束");
                        }
                        else {
                            state_text.setText("正常");
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        isOpen = false;
        MyApp.driver.CloseDevice();
        super.onDestroy();
    }

    private void initUI() {
        power_text = (EditText) findViewById(R.id.power_text);
        mode_text = (EditText) findViewById(R.id.mode_text);
        state_text = (EditText) findViewById(R.id.state_text);

        StartBtn = (Button) findViewById(R.id.start_btn);
        AddPowerBtn = (Button) findViewById(R.id.add_power_btn);
        MinusPowerBtn = (Button) findViewById(R.id.minus_power_btn);
        ConnectBtn = (Button) findViewById(R.id.connect_btn);
        TurnOnBtn = (Button) findViewById(R.id.trunon_btn);
        TurnOffBtn = (Button) findViewById(R.id.trunoff_btn);

        ConnectBtn.setText("裝置連線");
        ConnectBtn.setEnabled(true);

        StartBtn.setEnabled(false);
        AddPowerBtn.setEnabled(false);
        MinusPowerBtn.setEnabled(false);
        TurnOnBtn.setEnabled(false);
        TurnOffBtn.setEnabled(false);

        baudRate = 115200;
        stopBit = 1;
        dataBit = 8;
        parity = 0;
        flowControl = 0;

        return;
    }

    private void OpenUART() {
        if (!isOpen) {
            int retval = MyApp.driver.ResumeUsbPermission();
            if (retval == 0) {
                retval = MyApp.driver.ResumeUsbList();
                if (retval == -1)
                {
                    Toast.makeText(MainActivity.this, "Open failed!",
                            Toast.LENGTH_SHORT).show();
                    MyApp.driver.CloseDevice();
                } else if (retval == 0){
                    if (MyApp.driver.mDeviceConnection != null) {
                        if (!MyApp.driver.UartInit()) {
                            Toast.makeText(MainActivity.this, "Initialization failed!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this, "Device opened",
                                Toast.LENGTH_SHORT).show();
                        isOpen = true;
                        if (MyApp.driver.SetConfig(baudRate, dataBit, stopBit, parity,
                                flowControl)) {
                            Toast.makeText(MainActivity.this, "Config successfully",
                                    Toast.LENGTH_SHORT).show();
                            ConnectBtn.setText("連線成功");
                            ConnectBtn.setEnabled(false);
                            StartBtn.setEnabled(true);
                            AddPowerBtn.setEnabled(true);
                            MinusPowerBtn.setEnabled(true);
                            TurnOnBtn.setEnabled(true);
                            TurnOffBtn.setEnabled(true);
                        } else {
                            Toast.makeText(MainActivity.this, "Config failed!",
                                    Toast.LENGTH_SHORT).show();
                            ConnectBtn.setText("裝置連線");
                            ConnectBtn.setEnabled(true);
                        }
                        new readThread().start();
                    } else {
                        Toast.makeText(MainActivity.this, "Open failed!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setIcon(R.drawable.icon);
                    builder.setTitle("δ��Ȩ��");
                    builder.setMessage("ȷ���˳���");
                    builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    builder.setNegativeButton("����", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    });
                    builder.show();

                }
            }
        } else {
            isOpen = false;
            try {
                sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            MyApp.driver.CloseDevice();
        }
    }


    private class readThread extends Thread {

        public void run() {

            byte[] buffer = new byte[4096];

            while (true) {

                Message msg = Message.obtain();
                if (!isOpen) {
                    break;
                }
                int length = MyApp.driver.ReadData(buffer, 4096);
                if (length > 0) {
                    String recv = toHexString(buffer, length);
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    private byte[] toByteArray(String arg) {
        if (arg != null) {
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }
}
