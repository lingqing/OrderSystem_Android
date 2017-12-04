package com.example.liang.ordersystemapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.net.http.*;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Food.FoodManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.felhr.deviceids.*;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

public class PayWayActivity extends Activity {

    // view
    private Button alipayBtn;
    private Button wxpayBtn;
    private Button backBtn;
    private ImageView qrCodeView;

    private static String qrBaseURL = "http://andyhacker.cn/orderm/wxpay/qrcode.php?data=";
    private static String wxpayUrl = "http://andyhacker.cn/orderm/wxpay/wxpayfromclient.php";
    private static String wxQueryUrl = "http://andyhacker.cn/orderm/wxpay/orderquery.php?out_trade_no=";

    private static String alipayUrl = "http://andyhacker.cn/orderm/alpay/alpayfromclient.php";
//    private static String alipayUrl = "http://192.168.199.172/alpay/alpayfromclient.php";
    private static String aliQueryUrl = "http://andyhacker.cn/orderm/alpay/f2fpay/orderquery.php?out_trade_no=";
//    private static String aliQueryUrl = "http://192.168.199.172/alpay/f2fpay/orderquery.php?out_trade_no=";

    //
    private String outTradeNo = null;
    private String orderLabel = null;
    private String sumPrice = null;

    private boolean isAli = false;
    private boolean isQueryStop = true;

    Thread queryThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_way);

        Intent intent = getIntent();
        outTradeNo = intent.getStringExtra("outTradeNo");
        orderLabel = intent.getStringExtra("orderLabel");
        sumPrice = intent.getStringExtra("sumPrice");
        // 支付宝支付
        alipayBtn = (Button)findViewById(R.id.btn_alipay);
        alipayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alipay();
            }
        });
        // 微信支付
        wxpayBtn = (Button)findViewById(R.id.btn_wxpay);
        wxpayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                qrCodeView.       // Todo : set image to wait
                wxpay();
            }
        });

        backBtn = (Button)findViewById(R.id.back_to_order_id);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TOdo
                sendCommadToPLC();

//                finish();
            }
        });
        qrCodeView = (ImageView)findViewById(R.id.qr_code_image_id);

        queryThread = new Thread(queryRunnable);
    }
    // 支付
    private boolean payAndQuery(boolean _isAli){
        isAli = _isAli;

        new Thread(payThreadRunnable).start();
        return false;
    }

    private boolean alipay(){
        return payAndQuery(true);
    }

    private boolean wxpay(){
        return payAndQuery(false);
    }

    private Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case 1:
                    qrCodeView.setImageBitmap((Bitmap) msg.obj);
                    break;
                case 2:
                    if (msg.arg2 == 1){
                        qrCodeView.setImageBitmap(FoodManager.getPaySucceedImg());
                        isQueryStop = true;
                        sendCommadToPLC();
                    }
                default:
                    break;
            }

        }
    };

    /// Pay Thread
    Runnable payThreadRunnable = new Runnable() {
        @Override
        public void run() {
            // 停止之前的查询循环
            isQueryStop = true;
            try {
                queryThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            FormBody body = new FormBody.Builder()
                    .add("outTradeNo", outTradeNo)
                    .add("orderLabel", orderLabel)
                    .add("sumPrice", sumPrice)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(isAli ? alipayUrl : wxpayUrl)
                    .post(body)
                    .build();

            try{
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Http", "失败");
//                            Toast.makeText(PayWayActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                            Toast.makeText(PayWayActivity.this, "连接成功" + response.toString(), Toast.LENGTH_SHORT).show();
                        String qrStr = response.body().string();
                        Log.d("Http", "成功"+ qrStr);

                        Bitmap bitmap = FoodManager.getHttpBitMap(qrBaseURL + qrStr);
                        // 更新付款二维码
                        Message msg = new Message();
                        msg.arg1 = 1;       // 1 ： 更新二维码
                        msg.obj = bitmap;
                        updateHandler.sendMessage(msg); // 发送更新qrcode 消息
                        isQueryStop = false;
                        queryThread.start();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // query Thread
    Runnable queryRunnable = new Runnable(){
        @Override
        public void run() {
            String queryUrl = (isAli?aliQueryUrl:wxQueryUrl) + outTradeNo;
            OkHttpClient client = new OkHttpClient();
            Request queryReqest = new Request.Builder()
                    .url(queryUrl)
                    .get()
                    .build();

            while (!isQueryStop) {
                Log.d("Loop", queryUrl);
                client.newCall(queryReqest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Query", "查询失败");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            Log.d("HTTP Query", jsonStr);
                            JSONObject jo = new JSONObject(jsonStr);
                            Message msg = new Message();
                            msg.arg1 = 2;           // 付款成功，发送PLC控制命令
                            msg.arg2 = 1;

                            if (isAli){
                                if(jo.getString("code").equals("10000")){
                                    Log.d("Pay", "扫码成功");
                                    if(jo.getString("trade_status").equals("TRADE_SUCCESS")){
                                        updateHandler.sendMessage(msg);
                                        isQueryStop =true;
                                    }
                                }
                                // Todo: else
                            }
                            else {      // wxpay
                                if (jo.getString("trade_state").equals("SUCCESS")){
                                    updateHandler.sendMessage(msg);
                                    isQueryStop = true;
                                }
                                // Todo: else
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void sendCommadToPLC(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                // 发送命令到PLC
                UsbManager usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
                if (!usbDevices.isEmpty()) {
                    // Todo
                    Iterator<UsbDevice> iterator = usbDevices.values().iterator();
                    UsbDevice device = iterator.next();
                    Log.d("USB", "USB Device name :" + device.getDeviceName());
                    if(usbManager.hasPermission(device)) {
                        UsbDeviceConnection usbConnection = usbManager.openDevice(device);
                        if (usbConnection == null) {
                            Log.d("USB", "USB 设备连接失败");
                        } else {
                            Log.d("USB", "USB 设备连接成功");
                            UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);
                            serial.open();
                            serial.setBaudRate(115200);
                            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serial.setParity(UsbSerialInterface.PARITY_ODD);
                            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                            serial.write("Test".getBytes());  // Todo : 添加发送指令
                            usbConnection.close();
                        }
                    }
                    else {
                        Log.d("USB", "没有设备 "+ device.getDeviceName() + "权限");
                    }

                }
                else {
                    Log.d("USB", "没有USB设备");
                }
                // 等待一段时间
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent1 = new Intent();
                intent1.putExtra("needClear", true);
                setResult(889, intent1);
                finish();
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        isQueryStop = true;
        super.onBackPressed();
    }
}
