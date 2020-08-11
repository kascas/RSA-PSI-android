package utils;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

/**
 * @author: kascas
 * @date: 2020/8/10-12:41
 * @description:
 */
public class Network {
    
    public static void receiveFile(Socket socket, String file) {
        byte[] buf = new byte[65535];
        int i = 0;
        // file output stream
        FileOutputStream fout = null;
        // data input stream
        DataInputStream dataRead = null;
        
        try {
            System.out.println(file + "开始接收");
            fout = new FileOutputStream(file);
            dataRead = new DataInputStream(socket.getInputStream());
            String filename = dataRead.readUTF();
            int fileLen = dataRead.readInt();
            int count = 0;
            // 写入文件
            while (i != -1 && count <= fileLen) {
                i = dataRead.read(buf);
                count += i;
                if (i != -1) {
                    fout.write(buf, 0, i);
                }
            }
            System.out.println(file + "接收完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fout != null)
                    fout.close();
                //socket.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void sendFile(Socket socket, String file) {
        // data output stream
        DataOutputStream dataWrite = null;
        // file input stream
        FileInputStream fin = null;
        // buffer size = 128KB
        byte[] buf = new byte[65535];
        int i = 0;
        // open a file
        try {
            fin = new FileInputStream(file);
        } catch (FileNotFoundException exc) {
            System.out.println(file + "文件不存在");
            return;
        }
        // create a connection
        try {
            System.out.println(file + "开始传输");
            dataWrite = new DataOutputStream(socket.getOutputStream());
            dataWrite.writeUTF(file);
            dataWrite.flush();
            dataWrite.writeInt(file.length());
            dataWrite.flush();
            // 文件读写并将数据传输到socket的outputstream
            while (i != -1) {
                i = fin.read(buf);
                if (i != -1) {
                    dataWrite.write(buf, 0, i);
                }
                dataWrite.flush();
            }
            System.out.println(file + "传输完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
                //socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void sendMsg(Socket socket, String msg) {
        try {
            System.out.println("send message: " + msg);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            writer.println(msg);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String receiveMsg(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
