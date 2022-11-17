package LetChat.cpnt.server;

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
                            ServerFilereceiever thread = new ServerFilereceiever(socket1, in2, out2);
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

class ServerFilereceiever extends Thread {
    Socket socket;
    OutputStream out;
    InputStream in0;

    ServerFilereceiever(Socket socket, InputStream in, OutputStream out) {
        this.socket = socket;
        try {
            this.in0 = in;
            this.out = out;
        } catch (Exception e) {
        }
    }

    public void run() {
        Iterator it = server.InputStream_client.keySet().iterator();

        try {
            String[] arr = server.BackUpname;

            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.equals(arr[3])) {
                    InputStream in = (InputStream) server.InputStream_client.get(key);
                    GetFile getFile = new GetFile(arr, in);
                    getFile.start();

                    Initialization init = new Initialization(in, getFile);
                    init.start();
                }
            }
            ServerReceiver.sendToOne(arr);
        } catch (Exception e) {
        }
    }
}


class GetFile extends Thread {

    String[] arr;
    InputStream in;

    GetFile(String[] arr, InputStream in) {
        this.arr = arr;
        try {
            this.in = in;
        } catch (Exception e) {
        }
    }

    public void run() {
        try {
            Iterator it = server.client.keySet().iterator();

            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.equals(arr[0])) {
                    OutputStream out = (OutputStream) server.client.get(key);
                    byte[] buffer = new byte[10000];
                    int readBytes;

                    if ((readBytes = in.read(buffer)) != -1) {
                        out.write(buffer, 0, readBytes);
                    }
                }
            }

        } catch (Exception e) {
        }
    }

}

class Initialization extends Thread {
    InputStream in;
    GetFile getFile;

    Initialization(InputStream in, GetFile getFile) {
        this.in = in;
        this.getFile = getFile;
    }

    public void run() {
        try {
            getFile.join();
            in.reset();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
