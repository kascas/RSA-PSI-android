package server;

import server.RSAPSI;
import sun.nio.ch.Net;
import utils.Network;

import java.net.*;
import java.util.concurrent.*;

// Server提供main方法
// 通过调用ServerThread类实现服务器通信
public class Server {
    // 线程池的线程数量
    int POOLSIZE = 20;
    int port = 0;
    
    public Server(int port) {
        // 监听端口
        this.port = port;
    }
    
    public void work() {
        // 创建线程池
        ExecutorService pool = Executors.newFixedThreadPool(POOLSIZE);
        // 对连接的客户计数
        int count = 0;
        try (ServerSocket server = new ServerSocket(this.port)) {
            while (true) {
                count++;
                System.out.println("Waiting for connection:");
                Socket socket = server.accept();
                System.out.println("Client " + String.valueOf(count) + " :" + socket.getInetAddress());
                Callable<Void> task = new ServerThread(socket);
                // 提交线程
                pool.submit(task);
            }
        } catch (java.io.IOException e) {
            System.out.println("Could not start server");
        }
    }
    
    // main方法在这里
///////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        // 1234为监听端口，“...”为存放的文件夹
        Server s = new Server(12345);
        s.work();
    }
///////////////////////////////////////////////////////////////////////////
}

class ServerThread implements Callable<Void> {
    
    private Socket socket;
    
    public ServerThread(Socket socket) {
        // 与一个客户的socket
        this.socket = socket;
    }
    
    public Void call() {
        
        String dir = "F:\\IDEA\\RSAPSI\\src\\ServerFile\\";
        RSAPSI rsa = new RSAPSI(dir, 1024);
        String recv, username, mode;
        
        recv = Network.receiveMsg(socket);   // disconnect
        mode = recv.split(",")[0];
        username = recv.split(",")[1];
        rsa.setUsername(username);
        
        if (mode.equals("true")) {
            rsa.newKey();
            Network.sendFile(socket, dir + "pub_key_" + username);
            
            Network.receiveMsg(socket);
            
            rsa.setBloom();
            Network.sendFile(socket, dir + "bloom");
        }
        
        Network.receiveFile(socket, dir + username);
        rsa.priKeyReader();
        rsa.sign();
        Network.sendFile(socket, dir + username + "_sign");
        
        System.out.println("disconnect");
        
        /*
        username = "13840092360";
        rsa.setUsername(username);
        rsa.setBloom();
        Network.sendFile(socket, dir + "bloom");
        Network.sendFile(socket, dir + "pub_key_" + username);
        Network.receiveFile(socket, dir + username);
        rsa.sign();
        Network.sendFile(socket, dir + username + "_sign");
        */
        return null;
    }
    
}
