package LetChat.cpnt.client;

import java.net.*;
import java.io.*;

class SendFile {
    int sig;
    File file;
    String input;
    String who;
    String address;
    DataOutputStream out;
    OutputStream out2;


    SendFile(File file, String input, String who, String address, DataOutputStream out, OutputStream out2, int sig){
        this.file = file;
        this.input = input;
        this.who = who;
        this.address = address;
        this.sig = sig;
        try{
            this.out = out;
            this.out2 = out2;
        } catch (Exception e){}
    }

    public int run() throws IOException {
        long fileSize = file.length();
        long totalReadyBytes = 0;
        byte[] buffer = new byte[10000];
        int readBytes;

        if (!file.exists()) {
            System.out.println("System Message :: 해당 파일은 존재하지 않습니다.");
            return 0;
        } else {
            FileInputStream fis = new FileInputStream(file);
            try {
                out.writeUTF(address + "," + input + "," + sig + "," + who);

                while ((readBytes = fis.read(buffer)) > 0) {
                    out2.write(buffer, 0, readBytes);
                    totalReadyBytes += readBytes;
                    System.out.println("In progress: " + totalReadyBytes + "/" + fileSize + " Byte(s) (" + (totalReadyBytes * 100 / fileSize) + " %)");
                }
                System.out.println("System Message :: 파일 전송이 완료되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("System Message :: 파일 전송에 실패하였습니다.");
                return -1;
            } finally {
                fis.close();
                return 0;
            }
        }
    }
}

class GetFile extends Thread {
    Socket socket;
    InputStream in;
    String arr;
    GetFile(Socket socket, InputStream in, String arr){
        this.socket = socket;
        this.in = in;
        this.arr = arr;
    }

    public void run() {
        try {
            FileOutputStream file = new FileOutputStream(arr);
            byte[] buffer = new byte[10000];
            int readBytes;


            if ((readBytes = in.read(buffer)) != -1) {
                file.write(buffer, 0, readBytes);
            }
            file.close();
        }catch (Exception e){System.out.println("System Message :: 다운로드 실패! 다운로드 중 오류가 발생했습니다.");}
    }
}

class Initialization extends Thread {
    InputStream in;

    Initialization(InputStream in){
        this.in = in;
    }

    public void run() {
        try {
            in.reset();
        } catch (IOException e) {}
    }
}