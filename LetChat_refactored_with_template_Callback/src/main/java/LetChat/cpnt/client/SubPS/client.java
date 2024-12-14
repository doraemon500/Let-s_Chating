package LetChat.cpnt.client.SubPS;

import LetChat.cpnt.client.SubPS.FileLabour.GetFileTemplate;
import LetChat.cpnt.client.SubPS.FileLabour.InitializationTemplate;

import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;


public class client {
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
        } catch (Exception e) {
        }
    }
}

class ClientSender extends Thread {
    Socket socket;
    Socket FILESTREAMsocket;
    DataOutputStream out;
    OutputStream FILEOutputStream;
    String name;

    ClientSender(Socket socket, Socket FILESTREAMsocket, String name) {
        this.socket = socket;
        this.FILESTREAMsocket = FILESTREAMsocket;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            FILEOutputStream = FILESTREAMsocket.getOutputStream();
            this.name = name;

        } catch (Exception e) {
        }
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

                    long fileSize = file.length();
                    long totalReadyBytes = 0;
                    byte[] buffer = new byte[10000];
                    int readBytes;

                    if (!file.exists()) {
                        System.out.println("System Message :: 해당 파일은 존재하지 않습니다.");
                    } else {
                        FileInputStream fis = new FileInputStream(file);
                        try {
                            out.writeUTF(address + "," + input + "," + sig + "," + who);
                            while ((readBytes = fis.read(buffer)) > 0) {
                                FILEOutputStream.write(buffer, 0, readBytes);
                                totalReadyBytes += readBytes;
                                System.out.println("In progress: " + totalReadyBytes + "/" + fileSize + " Byte(s) (" + (totalReadyBytes * 100 / fileSize) + " %)");
                            }
                            System.out.println("System Message :: 파일 전송이 완료되었습니다.");
                        } catch (Exception e) {
                            System.out.println("System Message :: 파일 전송에 실패하였습니다.");
                            continue;
                        } finally {
                            fis.close();
                        }
                    }
                }
                out.writeUTF("[" + name + "]" + str);
            }
        } catch (IOException e) {
        }
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
                    CLTGetFile getFile = new CLTGetFile(socket0, in2, arr[1]);
                    getFile.start();
                }

                CLTInitialization init = new CLTInitialization(in2);
                init.start();
                System.out.println("System Message :: 다운로드가 무사히 완료되었습니다!");

            } catch (Exception e) {
                System.out.println(st);
            }
        }
    }

}

class CLTGetFile extends Thread {
    Socket socket;
    InputStream in;
    FileOutputStream file;

    CLTGetFile(Socket socket, InputStream in, String FILEname) throws FileNotFoundException {
        this.socket = socket;
        this.in = in;
        this.file = new FileOutputStream(FILEname);
    }

    public void run() {
        Callback();
    }

    void Callback() {
        FILECallback fileCallback =
                new FILECallback() {
                    @Override
                    public void GetFILEcallback(byte[] buffer, int readBytes) throws IOException {
                        file.write(buffer);
                        file.close();
                    }
                };
        new GetFileTemplate(in, fileCallback).ReadFILE();
    }
}


class CLTInitialization extends Thread {
    InputStream in;

    CLTInitialization(InputStream in) {
        this.in = in;
    }

    public void run() {
        Callback();
    }

    void Callback() {
        InitCallback initCallback =
                new InitCallback() {
                    @Override
                    public void Initializaioncallback() {
                        return;
                    }
                };
        new InitializationTemplate(in, initCallback).run();
    }
}
