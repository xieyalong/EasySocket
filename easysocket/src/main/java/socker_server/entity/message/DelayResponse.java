package socker_server.entity.message;


import socker_server.entity.message.base.SuperResponse;

/**
 * Author：Alex
 * Date：2019/6/11
 * Note：
 */
public class DelayResponse extends SuperResponse {

    private String from;
    private  String data;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
