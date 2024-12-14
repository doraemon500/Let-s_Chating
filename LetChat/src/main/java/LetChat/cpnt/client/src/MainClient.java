package LetChat.cpnt.client;

import LetChat.cpnt.server.server;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class MainClient {

    static String serverIp = "192.168.0.13";//"127.0.0.1"; //"192.168.0.13";
    static String currentIp;
    public static String objectIp;
    static User user;
    static Thread loginer;
    static Thread Sender;
    static Thread Receiver;
    static ServerSocket serverSocket;


    public static void main(String args[]) {
        try {
            currentIp = InetAddress.getLocalHost().getHostAddress();
         //   serverSocket = new ServerSocket(5656);
            Socket socket = new Socket(serverIp, 7555);
            System.out.println("서버에 연결되었습니다");

            loginer = new LoginThread(socket);

            loginer.start();
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class LoginThread extends Thread {

    Socket socket;
    DataOutputStream out;
    DataInputStream in;

    LoginThread(Socket socket) {
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
        }
    }

    public void run() {
        int check_num = 0;
        int mode;
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println(" # 0. 회원가입 // 1. 로그인");
                mode = Integer.parseInt(scanner.nextLine());

                if (mode == 0) {
                    if (sign_in() == -1) {
                        System.out.println("System Message :: 중복된 계정이 이미 존재합니다!");
                        throw new Exception();
                    }
                    continue;
                } else if (mode == 1) {
                    check_num = login();
                }

                if (check_num == 1) {
                    System.out.println("System Message :: 계정 로그인이 완료되었습니다!");
                    System.out.println("System Message :: 어서오십쇼!");

                    MainClient.Sender = new MainClientSender(socket, in, out);
                    MainClient.Receiver = new MainClientReceiver(socket, in, out);

                    MainClient.Sender.start();
                    MainClient.Receiver.start();
                    break;
                } else System.out.println("System Message :: 로그인 실패!");
            } catch (Exception e) {
            }
        }
    }

    int sign_in() {
        try {
            String ID = JOptionPane.showInputDialog(" # ID를 입력해주세요!");
            String pwd = JOptionPane.showInputDialog(" # 비밀번호를 입력해주세요!");
            String sig = "sign_in";

            out.writeUTF(ID + "," + pwd + "," + sig + "," + "Null" + "," + "2000");
            return Integer.parseInt(in.readUTF());
        } catch (Exception e) {
            return -1;
        }
    }

    int login() {

        String ID = JOptionPane.showInputDialog(" # ID를 입력해주세요!");
        String pwd = JOptionPane.showInputDialog(" #  비밀번호를 입력해주세요!");
        String sig = "login";

        try {
            out.writeUTF(ID + "," + pwd + "," + sig + "," + MainClient.currentIp + "," + "2000");
            MainClient.user = new User(ID, pwd, MainClient.currentIp, "2000");
            return Integer.parseInt(in.readUTF());
        } catch (Exception e) {
            return 0;
        }
    }


}

class MainClientSender extends Thread {

    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    MainClientSender(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        try {
            this.in = in;
            this.out = out;
        } catch (Exception e) {
        }
    }

    public void run() {
        String str;

        while (true) {
            System.out.println(" # 0. 참여하기 // 1. 채팅방 개설하기");
            Scanner scanner = new Scanner(System.in);
            str = scanner.nextLine();
            try {
                switch (str) {
                    case "0": {
                        out.writeUTF(MainClient.user.getId() + "," + MainClient.user.getPassword() + "," + "join" + "," + MainClient.user.getIp() + "," + MainClient.user.getOpensign());
                        MainClient.Receiver.join();
                        continue;
                    }
                    case "1": {
                        out.writeUTF(MainClient.user.getId() + "," + MainClient.user.getPassword() + "," + "create" + "," + MainClient.user.getIp() + "," + "300");
                        SubProxy.start();
                        continue;
                    }
                }
            } catch (Exception e) {
                try {
                    out.writeUTF(MainClient.user.getId() + "," + MainClient.user.getPassword() + "," + "update" + "," + MainClient.user.getIp() + "," + "2001");
                }catch (Exception ke){}
            }
        }
    }

}


class MainClientReceiver extends Thread {

    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    MainClientReceiver(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        try {
            this.in = in;
            this.out = out;
        } catch (Exception e) {
        }
    }

    public void run() {
        while (true) {
            try {
                String input = in.readUTF();

                switch (input) {
                    case "0": {
                        Scanner scanner = new Scanner(System.in);
                        String[] arr;
                        int size = Integer.parseInt(in.readUTF());
                        String[] str = new String[size];
                        System.out.println("==============================================================================================================================");
                        System.out.println("!!CHOOSE ONE ROOM AND JOIN THE CHAT!!");
                        for (int i = 0; i < size; i++) {
                            str[i] = in.readUTF();
                            arr = str[i].split(",", 2);
                            System.out.println("No." + i +" ||   " + str[i] + "   is working.");
                        }
                        int args = Integer.parseInt(scanner.nextLine());
                        arr = str[args].split(",", 2);
                        MainClient.objectIp = arr[1];
                        Process p = Runtime.getRuntime().exec("cmd /c start C:\\Users\\Public\\Documents\\Network2\\src\\main\\java\\LetChat\\cpnt\\client\\client.bat");
                        p.waitFor();
                        MainClient.Sender.interrupt();
                        continue;
                    }
                    case "1": {
                        MainClient.objectIp = MainClient.currentIp;
                        Process process = Runtime.getRuntime().exec("cmd /c start C:\\Users\\Public\\Documents\\Network2\\src\\main\\java\\LetChat\\cpnt\\client\\server.bat");
                        process.waitFor();
                        Process p = Runtime.getRuntime().exec("cmd /c start C:\\Users\\Public\\Documents\\Network2\\src\\main\\java\\LetChat\\cpnt\\client\\client.bat");
                        p.waitFor();
                        continue;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

}
