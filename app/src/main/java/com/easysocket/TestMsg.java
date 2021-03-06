package com.easysocket;

import com.easysocket.entity.basemsg.SuperSender;

/**
 * Author：Alex
 * Date：2019/12/6
 * Note：不带回调标识singer的消息
 */
public class TestMsg extends SuperSender {

    private String msgId;
    private String from;
    private  String data;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

//    @Override
//    public String toString() {
//        return "TestMessage{" +
//                "msgId='" + msgId + '\'' +
//                ", from='" + from + '\'' +
//                ", data='" + data + '\'' +
//                '}';
//    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
