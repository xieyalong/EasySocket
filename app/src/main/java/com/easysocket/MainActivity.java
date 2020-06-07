package com.easysocket;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.easysocket.callback.ProgressDialogCallBack;
import com.easysocket.callback.SimpleCallBack;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.connection.heartbeat.HeartManager;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.callback.IProgressDialog;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //是否已连接
    private boolean isConnected;
    //连接或断开连接的按钮
    private Button controlConnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlConnect=findViewById(R.id.control_conn);

        findViewById(R.id.ping).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pingIp();
            }
        });
        //创建socket连接
        findViewById(R.id.create_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化socket
                initEasySocket();
                //监听socket行为
                EasySocket.getInstance().subscribeSocketAction(socketActionListener);
            }
        });

        //发送一个消息
        findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //发送有回调功能的消息
        findViewById(R.id.callback_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCallbackMsg();
            }
        });

        //启动心跳检测
        findViewById(R.id.start_heart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHeartbeat();
            }
        });

        //有进度条的消息
        findViewById(R.id.progress_msg).setOnClickListener(new View.OnClickListener() {
            //进度条接口
            private IProgressDialog progressDialog = new IProgressDialog() {
                @Override
                public Dialog getDialog() {
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setTitle("正在加载...");
                    return dialog;
                }
            };
            @Override
            public void onClick(View v) {
                CallbackSender sender = new CallbackSender();
                sender.setFrom("android");
                sender.setMsgId("delay_msg");
                EasySocket.getInstance()
                        .upCallbackMessage(sender)
                        .onCallBack(new ProgressDialogCallBack<String>(progressDialog, true, true, sender.getCallbackId()) {
                            @Override
                            public void onResponse(String s) {
                                LogUtil.d("进度条回调消息=" + s);
                            }

                            @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                e.printStackTrace();
                            }
                        });
            }
        });

        //连接或断开连接
        controlConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected){
                    EasySocket.getInstance().disconnect(false);
                }else {
                    EasySocket.getInstance().connect();
                }
            }
        });

        //销毁socket连接
        findViewById(R.id.destroy_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasySocket.getInstance().destroyConnection();
            }
        });
    }


    /**
     * 发送一个有回调的消息
     */
    private void sendCallbackMsg() {

        CallbackSender sender = new CallbackSender();
        sender.setMsgId("callback_msg");
        sender.setFrom("我来自android");
        EasySocket.getInstance().upCallbackMessage(sender)
                .onCallBack(new SimpleCallBack<CallbackResponse>(sender.getCallbackId()) {
                    @Override
                    public void onResponse(CallbackResponse response) {
                        LogUtil.d("回调消息=" + response.toString());
                        Toast.makeText(MainActivity.this,"回调消息："+response.toString(),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        e.printStackTrace();
                    }
                });
    }

    //启动心跳检测功能
    private void startHeartbeat() {

        //心跳实例
        ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
        clientHeartBeat.setMsgId("heart_beat");
        clientHeartBeat.setFrom("client");
        EasySocket.getInstance().startHeartBeat(clientHeartBeat, new HeartManager.HeartbeatListener() {
            @Override
            public boolean isServerHeartbeat(OriginReadData originReadData) {
                String msg = originReadData.getBodyString();
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    if ("heart_beat".equals(jsonObject.getString("msgId"))) {
                        LogUtil.d("收到服务器心跳");
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    /**
     * 发送一个的消息，
     */
    private void sendMessage() {
        TestMsg testMsg = new TestMsg();
        testMsg.setMsgId("test_msg");
        testMsg.setFrom("android");
        //发送
        EasySocket.getInstance().upObject(testMsg);
    }


    /**
     * socket行为监听
     */
    private ISocketActionListener socketActionListener = new SocketActionListener() {
        /**
         * socket连接成功
         * @param socketAddress
         */
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            super.onSocketConnSuccess(socketAddress);
            LogUtil.d("连接成功");
            controlConnect.setText("socket已连接，点击断开连接");
            isConnected=true;
        }

        /**
         * socket连接失败
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketConnFail(SocketAddress socketAddress, Boolean isNeedReconnect) {
            super.onSocketConnFail(socketAddress, isNeedReconnect);
            controlConnect.setText("socket连接被断开，点击进行连接");
            isConnected=false;
        }

        /**
         * socket断开连接
         * @param socketAddress
         * @param isNeedReconnect 是否需要重连
         */
        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, Boolean isNeedReconnect) {
            super.onSocketDisconnect(socketAddress, isNeedReconnect);
            LogUtil.d("socket断开连接，是否需要重连："+isNeedReconnect);
            controlConnect.setText("socket连接被断开，点击进行连接");
            isConnected=false;
        }

        /**
         * socket接收的数据
         * @param socketAddress
         * @param originReadData
         */
        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
            super.onSocketResponse(socketAddress, originReadData);
            LogUtil.d("socket监听器收到数据=" + originReadData.getBodyString());
        }
    };


    /**
     * 初始化EasySocket
     */
    private void initEasySocket() {
        String ip= NetworkUtils.getIPAddress(true);
        //socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setSocketAddress(new SocketAddress("192.168.3.19", 9999)) //主机地址
                .setCallbackIdKeyFactory(new CallbackIdKeyFactoryImpl())
                .build();

        //初始化EasySocket
        EasySocket.getInstance()
                .options(options) //项目配置
                .createConnection();//创建一个socket连接
    }
    public static boolean pingIp(){
        String ip="baidu";
        ip=   NetworkUtils.getIPAddress(true);

        if (true){

            int s=3;
            Process p;
            try {
                //ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 100  以秒为单位指定超时间隔，是指超时时间为100秒
//            p = Runtime.getRuntime().exec("ping -c 3 -w "+s+"000 " + ip);
                String mingling="adb shell ip -f inet addr show wlan0";
                p = Runtime.getRuntime().exec(mingling);
                int status = p.waitFor();
//            pingLog(p);
                if (status == 0) {
                    System.out.println("ping success");
                    return true;
                } else {
                    System.out.println("ping faild");
                }
            }catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
