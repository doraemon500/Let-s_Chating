package LetChat.cpnt.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SubProxy {

    static ServerSocket serverSocket;
    static Socket socket;
    static Thread thread;

    public static void main(String[] args) {
        try {
            new SubProxy().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void start() throws IOException {
        serverSocket = new ServerSocket(5656);
        while (true) {
            socket = serverSocket.accept();

            thread = new thread(socket);
            thread.start();
        }
    }

}
 class thread extends Thread {
    Socket socket;
    DataOutputStream out;
    DataInputStream in;

    thread(Socket socket) {
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try {
            while(true) {
                String str = in.readUTF();
            }
        } catch (IOException e) {
            try {
                SubProxy.serverSocket.close();
                SubProxy.thread.stop();
                throw new RuntimeException(e);
            } catch (Exception je){}
        }

    }
}