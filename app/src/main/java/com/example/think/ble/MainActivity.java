package com.example.think.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected = false;

    private ArrayList<String> dev ;

    private RecyclerView recyclerView;

    public static final String TAG = "Boomerr---test  " + MainActivity.class.getName();

    private Button scan;


    private Button send1;

    private Button send2;

    private Button send3;

    private Button send4;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    dev.add("连接失败");
                    bleAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    dev.add("连接成功");
                    bleAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    dev.add("未连接");
                    bleAdapter.notifyDataSetChanged();
                    break;
                case 4:
                    dev.add("发现服务");
                    bleAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private boolean mScanning;

    private BleAdapter bleAdapter;
    private Button send;

    //自定义UUID
    final UUID UUID_SERVICE = UUID.fromString("8206e6ec-183c-4f00-824a-b4910b98969c");
    //
    //  设备特征值UUID, 需固件配合同时修改
    //
    final UUID UUID_WRITE = UUID.fromString("8206e6ed-183c-4f00-824a-b4910b98969c");  // 用于发送数据到设备
    final UUID UUID_NOTIFICATION = UUID.fromString("78b48f99-f825-442f-adea-97d4425fe14a"); // 用于接收设备推送的数据

    private BluetoothDevice mDevice;


    //各种UUID
    //6f413ee1-556f-411e-b010-c10fb0736efe
    //6E400001-B5A3-F393-E0A9-E50E24DCCA9E
    //8206e6ec-183c-4f00-824a-b4910b98969c
    //78b48f99-f825-442f-adea-97d4425fe14a
    //



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        scan = (Button) findViewById(R.id.btn_scan);

        send1 = (Button) findViewById(R.id.send1);

        send2 = (Button) findViewById(R.id.send2);

        send3 = (Button) findViewById(R.id.send3);

        send4 = (Button) findViewById(R.id.send4);


        send1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        final byte[] bytes = new byte[3];
                        bytes[0] = (byte) 0x01;
                        bytes[1] = (byte) 0x01;
                        bytes[2] = (byte) '\n';
                        startSend(bytes);
                        break;
                }
                return true;
            }
        });

        send2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        final byte[] bytes = new byte[3];
                        bytes[0] = (byte) 0x01;
                        bytes[1] = (byte) 0x00;
                        bytes[2] = (byte) '\n';
                        startSend(bytes);
                        break;
                }
                return true;
            }
        });
        send3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        final byte[] bytes = new byte[3];
                        bytes[0] = (byte) 0x02;
                        bytes[1] = (byte) 0x01;
                        bytes[2] = (byte) '\n';
                        startSend(bytes);
                        break;
                }
                return true;
            }
        });
        send4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        final byte[] bytes = new byte[3];
                        bytes[0] = (byte) 0x02;
                        bytes[1] = (byte) 0x00;
                        bytes[2] = (byte) '\n';
                        startSend(bytes);
                        break;
                }
                return true;
            }
        });



        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanning = true;
                scanLeDevice(true);

            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dev = new ArrayList<>();

        bleAdapter = new BleAdapter(dev);

        recyclerView.setAdapter(bleAdapter);
    }
    //关闭BluetoothGatt
    private void closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
    }

    //初始化
    private void initialize() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"不支持低功耗蓝牙",Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        enableBle();

    }

    //打开蓝牙
    private void enableBle(){
        if(bluetoothAdapter != null && ! bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new   Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }


    //关闭蓝牙
    private void closeBle(){
        bluetoothAdapter.disable();
    }

    //Nordic_Template
    //搜索回调接口
    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onLeScan:  " + device.getName() + " : " + rssi);
            String name = device.getName();
            if (name != null) {
                Log.d(TAG,"s搜索到设备名：" + name);
                if (name.contains("Nordic_Template")) {
                    Toast.makeText(MainActivity.this,"找到设备 正在连接 . . . ",Toast.LENGTH_SHORT).show();
                    mDevice = device;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    mScanning = false;
                    startConnect();
                }
            }

        }

    };
    //搜索设备
    private void scanLeDevice(final boolean enable) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }, 30000);
        mScanning = true;
        // 定义一个回调接口供扫描结束处理
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    private boolean isServiceConnected;

    //连接回调
   private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

       @Override
       public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
           super.onConnectionStateChange(gatt, status, newState);
           Log.d(TAG,"onConnectionStateChange   " + newState);
           //连接失败
           if(status != BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG,"连接失败");
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                gatt.close();
               if (mBluetoothGatt != null) {
                   mBluetoothGatt.disconnect();
                   mBluetoothGatt.close();
                   mBluetoothGatt = null;
               }
               if (mDevice != null) {
                   mBluetoothGatt = mDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback);
               }
           }

           //已连接
           if(newState == BluetoothProfile.STATE_CONNECTED){
               Log.d(TAG,"已连接");
               Message msg = new Message();
               msg.what = 2;
               mHandler.sendMessage(msg);
               mHandler.post(new Runnable() {
                  @Override
                  public void run() {
                      Toast.makeText(MainActivity.this, "已连接..." + mDevice.getName(), Toast.LENGTH_SHORT).show();
                  }
              });
               mBluetoothGatt.discoverServices();
           }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
               Log.d(TAG,"未连接");
               Message msg = new Message();
               msg.what = 3;
               mHandler.sendMessage(msg);
               if (mBluetoothGatt != null) {
                   Log.d(TAG,"mBluetoothGatt 重置");
                   mBluetoothGatt.disconnect();
                   mBluetoothGatt.close();
                   mBluetoothGatt = null;
               }
               gatt.close();
               if (mDevice != null) {
                   Log.d(TAG,"重连      mDevice != null");
                   mBluetoothGatt = mDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback);
               }
           }


       }

    //服务获取逻辑处理
       @Override
       public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG,"onServicesDiscovered: " + "发现服务 : " + status);
           if (status == BluetoothGatt.GATT_SUCCESS) {
               //通信成功
               isServiceConnected = true;

               Log.d(TAG, "onServicesDiscovered: " + "发现服务 : " + status);

               Log.d(TAG, "onServicesDiscovered: " + "读取数据 .... ");
               Message msg = new Message();
               msg.what = 4;
               mHandler.sendMessage(msg);

               if (mBluetoothGatt != null && isServiceConnected) {
                   BluetoothGattService gattService = mBluetoothGatt.getService(UUID_SERVICE);
                   BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID_NOTIFICATION);
                   boolean b = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                   if (b) {
                       List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                       for (final BluetoothGattDescriptor descriptor : descriptors) {
                           boolean b1 = descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                           if (b1) {
                               mBluetoothGatt.writeDescriptor(descriptor);
                               Log.d(TAG, "startRead: " + "监听收数据");
                               mHandler.post(new Runnable() {
                                   @Override
                                   public void run() {
                                       dev.add("开始监听数据");
                                       bleAdapter.notifyDataSetChanged();
                                   }
                               });
                           }

                       }

                   }
               }

           }
       }


       @Override
       public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
           super.onCharacteristicRead(gatt, characteristic, status);
           Log.d(TAG, "read value: " + characteristic.getValue());
           Log.d(TAG, "callback characteristic read status " + status
                   + " in thread " + Thread.currentThread());
           if (status == BluetoothGatt.GATT_SUCCESS) {
               Log.d(TAG, "read value: " + characteristic.getValue());
               final String s0 = new String(characteristic.getValue());
               mHandler.post(new Runnable() {
                   @Override
                   public void run() {
                       dev.add("写入方法回调 + " + s0);
                       bleAdapter.notifyDataSetChanged();
                   }
               });
           }
       }

       @Override
       public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
           super.onDescriptorRead(gatt, descriptor, status);
           Log.d(TAG, "onDescriptorRead: " + "设置成功");
       }

       @Override
       public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
           super.onDescriptorWrite(gatt, descriptor, status);
           Log.d(TAG,"onDescriptorWrite" + "设置成功");
       }


       @Override
       public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
           super.onCharacteristicWrite(gatt, characteristic, status);
           Log.d(TAG,"onCharacteristicWrite" + "设置成功");
           if (status == BluetoothGatt.GATT_SUCCESS) {
               Log.d(TAG, "write value: " + characteristic.getValue());
               mHandler.post(new Runnable() {
                   @Override
                   public void run() {
                       String s0 = new String(characteristic.getValue());
                       dev.add("发送方法回调 + " + s0);
                       bleAdapter.notifyDataSetChanged();
                   }
               });
           }
       }

        //读取数据逻辑处理
       @Override
       public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
           super.onCharacteristicChanged(gatt, characteristic);
           byte[] value = characteristic.getValue();
           Log.d(TAG, "onCharacteristicChanged: " + value);
           final String s0 = new String(value);
           Log.d(TAG, "onCharacteristicChanged: " + s0 + "、");
           for (byte b : value) {
               Log.d(TAG, "onCharacteristicChanged:  result----------" + b);
           }
           mHandler.post(new Runnable() {
               @Override
               public void run() {
                   dev.add("接受信息 " + s0);
                   bleAdapter.notifyDataSetChanged();
                   Log.d(TAG,"UI更新");
               }
           });

       }
   };

    public void startConnect() {
        if (mDevice != null) {
            Log.d(TAG,"Bluetooth is not null");
            if (mBluetoothGatt != null) {
                Log.d(TAG,"重置BluetoothGatt");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }

            mBluetoothGatt = mDevice.connectGatt(MainActivity.this, false, bluetoothGattCallback);
            Log.d(TAG,"连接中..........");
        }
    }


    //发送数据逻辑处理
    public void startSend(final byte[] bytes) {
        if (mBluetoothGatt != null && isServiceConnected) {
            BluetoothGattService gattService = mBluetoothGatt.getService(UUID_SERVICE);
            BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID_WRITE);
            characteristic.setValue(bytes);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(characteristic);
            Log.d(TAG,"------------startSend方法调用");
            Log.d(TAG,"发送的指令    =   " + bytes.toString());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String a = new String(bytes);
                    dev.add("发送信息 " + a);
                    bleAdapter.notifyDataSetChanged();
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();
        }
        super.onDestroy();

    }

    private void stopConnect(){
        if(mBluetoothGatt != null){

            mBluetoothGatt.close();
        }
    }
}
