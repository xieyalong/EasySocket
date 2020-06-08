package socker_server;



import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import socker_server.iowork.IOManager;

/**
 * 服务器端启动
 */
public class MainClass {

    private static final int PORT = 9999;
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService mExecutorService = null;
    HandlerIO.ServiceListener serviceListener;

    public static void main(String[] args) {
        new MainClass();
        System.out.println("java running");
    }
    public  void  init(){
        new MainClass();
    }

    public MainClass() {
        try {

            server = new ServerSocket(PORT);
            mExecutorService = Executors.newCachedThreadPool();
            System.out.println("服务端运行 server is running");
            Socket client;
            while (true) {//死循环 监听客户端发送的数据
                client = server.accept();
                mList.add(client);
                mExecutorService.execute(new Service(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Service implements Runnable {
        private Socket socket;
        IOManager ioManager;

        public Service(Socket socket) {
            this.socket = socket;
            System.out.println("连接服务端成功 connect server sucessful: "+  socket.getInetAddress().getHostAddress());
        }

        @Override
        public void run() {
            ioManager = new IOManager(socket);
            ioManager.startIO();
        }
    }

}
