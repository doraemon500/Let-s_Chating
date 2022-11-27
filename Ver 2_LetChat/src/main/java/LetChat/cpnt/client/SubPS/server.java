package LetChat.cpnt.client.SubPS;

import LetChat.cpnt.client.SubPS.FileLabour.GetFileTemplate;
import LetChat.cpnt.client.SubPS.FileLabour.InitializationTemplate;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class server {
    static String serverIp = "127.0.0.1";
    static HashMap clients;
    static HashMap client;
    static HashMap InputStream_client;
    static String[] BackUpname;

    public server() {
        clients = new HashMap();
        client = new HashMap();
        InputStream_client = new HashMap();
        Collections.synchronizedMap(clients);
        Collections.synchronizedMap(client);
        Collections.synchronizedMap(InputStream_client);
    }

    public static void main(String args[]) {
        new server().start();
    }

    public static void start() {
        ServerSocket serverSocket = null;
        ServerSocket serverSocket1 = null;
        Socket socket = null;
        Socket socket1 = null;
        Socket subsocket;

        try {
            serverSocket = new ServerSocket(7777);
            serverSocket1 = new ServerSocket(7772);
            subsocket = new Socket(serverIp, 5656);
            System.out.println("System Message :: Working Well!");
            System.out.println("서버가 시작되었습니다");
            while (true) {
                socket = serverSocket.accept();
                socket1 = serverSocket1.accept();
                System.out.println("[[" + socket.getInetAddress() + ":" + socket.getPort() + "]]" + "에서 접속하셨습니다");
                ServerReceiver thread = new ServerReceiver(socket, socket1);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ServerReceiver extends Thread {
    Socket socket;
    Socket socket1;
    DataInputStream in;
    DataOutputStream out;
    InputStream in2;
    OutputStream out2;

    ServerReceiver(Socket socket, Socket socket1) {
        this.socket = socket;
        this.socket1 = socket1;
        try {
            in = new DataInputStream((socket.getInputStream()));
            out = new DataOutputStream((socket.getOutputStream()));

            in2 = socket1.getInputStream();
            out2 = socket1.getOutputStream();

        } catch (IOException e) {
        }
    }

    public void run() {
        String name = "";

        try {
            name = in.readUTF();
            sendToAll(" ##" + name + "님이 들어오셨습니다");

            server.clients.put(name, out); // Message
            server.client.put(name, out2);  // File_Send
            server.InputStream_client.put(name, in2);  // Get_File
            in2.mark(0);
            System.out.println("System Message :: 현재 서버접속자 수는" + server.clients.size() + "입니다.");
            synchronized (this) {
                while (in != null) {
                    String st = "";
                    try {
                        st = (String) in.readUTF();
                        String[] arr = st.split(",", 4);
                        if (arr[2].equals("5")) {
                            server.BackUpname = arr;
                            SVRGetFile thread = new SVRGetFile();
                            thread.start();
                            continue;
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    sendToAll(st);
                }
            }
        } catch (IOException e) {

        } finally {
            sendToAll("!!" + name + "님이 나가셨습니다");
            server.clients.remove(name);
            System.out.println("[[" + socket.getInetAddress() + ":" + socket.getPort() + "]]" + "에서 접속을 종료하셨습니다");
            System.out.println("System Message :: 현재 접속자 수는" + server.clients.size() + "입니다.");
        }
    }


    static synchronized void sendToAll(String msg) {
        Iterator it = server.clients.keySet().iterator();

        while (it.hasNext()) {
            try {
                DataOutputStream out = (DataOutputStream) server.clients.get(it.next());
                out.writeUTF(msg);
            } catch (IOException e) {
            }
        }
    }

    static void sendToOne(String[] arr) {
        Iterator it = server.clients.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.equals(arr[0])) {
                String st = "";
                try {
                    DataOutputStream outs = (DataOutputStream) server.clients.get(key);

                    st = arr[0] + "," + arr[1] + "," + arr[2];
                    outs.writeUTF(st);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}

class SVRGetFile extends Thread {

    String[] arr;
    InputStream in;
    OutputStream out;
    Iterator it;

    SVRGetFile() {
        this.arr = server.BackUpname;
    }

    public void run() {
        it = server.client.keySet().iterator();
        this.in = InputCallback();
        this.out = OutputCallback();
        FILECallback(out, in);

        Thread init = new SVRInitialization(in, this);
        init.start();
        ServerReceiver.sendToOne(arr);
    }

    InputStream InputCallback() {
        IterCallback iterCallback =
                new IterCallback() {
                    @Override
                    public Object IteratorCallback(String key) {
                        InputStream in = (InputStream) server.InputStream_client.get(key);
                        it = server.client.keySet().iterator();
                        return in;
                    }
                };
        return (InputStream) (new GetFileTemplate(arr[3], it, iterCallback).Iterator());
    }

    OutputStream OutputCallback() {
        IterCallback iterCallback =
                new IterCallback() {
                    @Override
                    public Object IteratorCallback(String key) {
                        OutputStream out = (OutputStream) server.client.get(key);
                        return out;
                    }
                };
        return (OutputStream) (new GetFileTemplate(arr[0], it, iterCallback).Iterator());
    }

    void FILECallback(OutputStream out, InputStream in) {
        FILECallback fileCallback =
                new FILECallback() {
                    @Override
                    public void GetFILEcallback(byte[] buffer, int readBytes) throws IOException {
                        out.write(buffer, 0, readBytes);
                    }
                };
        new GetFileTemplate(in, fileCallback).ReadFILE();
    }

}

class SVRInitialization extends Thread {
    InputStream in;
    Thread getFile;

    SVRInitialization(InputStream in, Thread getFile) {
        this.in = in;
        this.getFile = getFile;
    }

    public void run() {
        Callback();
    }

    void Callback() {
        InitCallback initCallback =
                new InitCallback() {
                    @Override
                    public void Initializaioncallback() throws InterruptedException {
                        getFile.join();
                    }
                };
        new InitializationTemplate(in, initCallback).run();
    }
}