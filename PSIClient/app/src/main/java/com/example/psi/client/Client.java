package com.example.psi.client;

import android.util.Log;

import com.example.psi.bloom.Bloom;
import com.example.psi.bloom.BloomIO;
import com.example.psi.util.Network;

import java.io.File;
import java.net.*;


public class Client {

    private String ip;
    private int port;
    private String rootDir;
    private Socket socket;
    private RSAPSI psi;
    private Bloom bloom;
    public boolean mode;

    public Client(String ip, int port, String rootDir) {
        this.ip = ip;
        this.port = port;
        this.rootDir = rootDir;
        psi = new RSAPSI();
        try {
            socket = new Socket(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setBloom(String bloomFile) {
        bloom = BloomIO.bloomReader(bloomFile);
    }

    public void sendMsg() {
        Network.sendMsg(socket, "msg");
    }

    public void login(String username) {
        if (!new File(rootDir + "/r.txt").exists() ||
                !new File(rootDir + "/pub_key").exists() ||
                !new File(rootDir + "/bloom").exists()) {
            mode = true;
            Network.sendMsg(socket, "true," + username);
        } else {
            mode = false;
            Network.sendMsg(socket, "false," + username);
        }
    }

    public void base() {
        Network.receiveFile(socket, rootDir + "/pub_key");
        Log.d("主界面-base", "pub_key is received");
        psi.setKey(rootDir + "/pub_key");
        psi.blindFactorGenerator(rootDir + "/r.txt");
        Log.d("主界面-base", "r.txt is ready");
    }

    public void setup() {
        Network.receiveFile(socket, rootDir + "/bloom");
        Log.d("主界面-setup", "bloom is received");
        bloom = RSAPSI.setBloom(rootDir + "/bloom");
    }


    public void online() {
        psi.setKey(rootDir + "/pub_key");
        psi.blind(rootDir + "/y.txt", rootDir + "/a.txt", rootDir + "/r.txt");
        Network.sendFile(socket, rootDir + "/a.txt");
        Log.d("主界面-online", "a.txt is sent");
        Network.receiveFile(socket, rootDir + "/b.txt");
        Log.d("主界面-online", "b.txt is received");
        psi.unblindAndCheck(rootDir + "/b.txt", rootDir + "/s.txt", rootDir + "/r.txt", rootDir + "/y.txt", bloom);
        Log.d("主界面-online", "s.txt is ready");
    }


}