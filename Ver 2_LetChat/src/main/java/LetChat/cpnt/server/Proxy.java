package LetChat.cpnt.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

    static String serverIp = "192.168.0.13";//"127.0.0.1"; //"192.168.0.13";

    public static void main(String[] args){
       new Proxy().start();
    }

    public void start(){
        ServerSocket serverSocket = null;
        Socket socket;
        Socket socket1;

        try{
            serverSocket = new ServerSocket(5555);
            socket1 = new Socket(serverIp, 7555);
            while(true) {
                socket = serverSocket.accept();

                Thread psv = new psv(socket, socket1);
                psv.start();
            }
        }catch (Exception e){}


    }

    public class psv extends Thread {

        Socket socket;
        Socket socket1;
        DataOutputStream out;
        DataInputStream in;
        DataOutputStream out1;
        DataInputStream in1;
        Thread snd;
        Thread rcv;
        psv (Socket socket, Socket socket1){
            this.socket = socket;
            this.socket1 = socket1;

            try {
                this.out = new DataOutputStream(socket.getOutputStream());
                this.in = new DataInputStream(socket.getInputStream());
                this.out1 = new DataOutputStream(socket1.getOutputStream());
                this.in1 = new DataInputStream(socket1.getInputStream());
            } catch (Exception e){}
        }

        public void run() {
            snd = new snd(socket, socket1, in, out, in1, out1, snd, rcv);
            rcv = new rcv(socket, socket1, in, out, in1, out1, snd, rcv);

            snd.start();
            rcv.start();
        }

    }

    public class rcv extends Thread{

        Socket socket;
        Socket socket1;
        DataOutputStream out;
        DataInputStream in;
        DataOutputStream out1;
        DataInputStream in1;
        Thread snd;
        Thread rcv;
        rcv (Socket socket, Socket socket1, DataInputStream in, DataOutputStream out, DataInputStream in1, DataOutputStream out1, Thread snd, Thread rcv){

            this.socket = socket;
            this.socket1 = socket1;
            this.snd = snd;
            this.rcv = rcv;
            try {
                this.out = out;
                this.in = in;
                this.out1 = out1;
                this.in1 = in1;
            } catch (Exception e){}
        }

        public void run(){
            String fromCnt;

            while(true) {
                try {
                    while (true) {
                        fromCnt = in.readUTF();
                        System.out.println("Message from Client to Server :: " + fromCnt);
                        out1.writeUTF(fromCnt);
                   //     if (snd.isDaemon() || !snd.isAlive() || snd == null)
                        //    break;
                    }
                } catch (Exception e) {
                    System.out.println("Error!! Client to Server");
                    e.printStackTrace();
                }
            }
        }
    }

    public class snd extends Thread{

        Socket socket;
        Socket socket1;
        DataOutputStream out;
        DataInputStream in;
        DataOutputStream out1;
        DataInputStream in1;
        Thread snd;
        Thread rcv;
        snd (Socket socket, Socket socket1, DataInputStream in, DataOutputStream out, DataInputStream in1, DataOutputStream out1, Thread snd, Thread rcv){

            this.socket = socket;
            this.socket1 = socket1;
            this.snd = snd;
            this.rcv = rcv;
            try {
                this.out = out;
                this.in = in;
                this.out1 = out1;
                this.in1 = in1;
            } catch (Exception e){}
        }

        public void run(){
            String fromSvr;


                try{
                    while(true) {
                        fromSvr = in1.readUTF();
                        System.out.println("Message from Server to Client :: " + fromSvr);
                        out.writeUTF(fromSvr);
                     //   if(rcv.isDaemon() || !rcv.isAlive() || rcv == null)
                         //   break;
                    }
                }catch (Exception e){
                    System.out.println("Error!! Server to Client");
                    e.printStackTrace();
                }
        }

    }
}
