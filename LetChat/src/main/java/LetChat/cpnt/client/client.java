package LetChat.cpnt.client;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class client  {
    static String serverIp = MainClient.objectIp;//"192.168.0.13";//"127.0.0.1";

    public static void main(String args[]) {
        String name = JOptionPane.showInputDialog("System Message :: 대화명을 입력해주세요");

        if (name.length() <= 0) {
            System.out.println("USAGE: java Client 대화명");
            System.exit(0);
        }

        try {
            Socket socket = new Socket(serverIp, 7777);
            Socket socket0 = new Socket(serverIp, 7772);
            System.out.println("서버에 연결되었습니다");
            Thread sender = new ClientSender(socket, socket0, name);
            Thread receiver = new ClientReceiver(socket, socket0);

            sender.start();
            receiver.start();
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {}
    }
}

 class ClientSender extends Thread  {
    Socket socket;
    Socket socket0;
    DataOutputStream out;
    OutputStream out2 ;
    String name;

    ClientSender(Socket socket,Socket socket0, String name) {
        this.socket = socket;
        this.socket0 = socket0;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            out2 = socket0.getOutputStream();
            this.name = name;

        } catch (Exception e) {}
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        try {
            if (out != null) {
                out.writeUTF(name);
            }

            while (out != null) {

                String str = scanner.nextLine();
                if (str.equals("Send File")) {
                    int sig = 5;

                    String input = JOptionPane.showInputDialog("보내고자 하는 파일 명을 입력해주세요.");
                    File file = new File(input);

                    String who = name;
                    String address = JOptionPane.showInputDialog("누구한테 보낼까요?");

                    if(new SendFile(file, input, who, address, out, out2, sig)
                            .run() == -1)
                        continue;
                }
                out.writeUTF("[" + name + "]" + str);
            }
        } catch (IOException e) {}
    }
}

 class ClientReceiver extends Thread {
    Socket socket;
    Socket socket0;
    DataInputStream in;
    InputStream in2;

    ClientReceiver(Socket socket, Socket socket0) {
        this.socket = socket;
        this.socket0 = socket0;
        try {
            in = new DataInputStream(socket.getInputStream());
            in2 = socket0.getInputStream();
        } catch (IOException e) {
        }
    }

    public void run() {
        in2.mark(0);
        while (in != null) {
            String st = "";
            try {
                st = in.readUTF();
                String[] arr = st.split(",", 3);
                if (st != null) {
                    GetFile getFile = new GetFile(socket0, in2, arr[1]);
                    getFile.start();
                }

                Initialization init = new Initialization(in2);
                init.start();
                System.out.println("System Message :: 다운로드가 무사히 완료되었습니다!");

            } catch(Exception e){
                System.out.println(st);
            }
        }
    }

}

