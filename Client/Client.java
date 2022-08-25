import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class client {
    static String fnam;
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("USAGE: java Client 대화명");
            System.exit(0);
        }

        try {

            String serverIp = "127.0.0.1";
            Socket socket = new Socket(serverIp, 7777);
            System.out.println("서버에 연결되었습니다");
            Thread sender = new Thread(new ClientSender(socket, args[0]));
            Thread receiver = new Thread(new ClientReceiver(socket));

            sender.start();
            receiver.start();
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {}
    }

     static class ClientSender extends Thread  {
        Socket socket;
        DataOutputStream out;
        OutputStream out2;
        String name;

        ClientSender(Socket socket, String name) {
            this.socket = socket;
            try {
                out = new DataOutputStream(socket.getOutputStream());
                out2 = socket.getOutputStream();
                this.name = name;

            } catch (Exception e) {}
        }

        public void run() {
            Scanner scanner = new Scanner(System.in);
            try {
                if (out != null) {
                    out.writeUTF(name);
                }

                while (out != null || out2 != null) {

                    String str = scanner.nextLine();
                    if (str.equals("Send File")) {
                        String input = JOptionPane.showInputDialog("보내고자 하는 파일 명을 입력해주세요.");
                        fnam = input;
                        int sig = 55;
                        File file = new File(input);
                        String address = JOptionPane.showInputDialog("누구한테 보낼까요?");

                        long fileSize = file.length();
                        long totalReadyBytes = 0;
                        byte[] buffer = new byte[10000];
                        int readBytes;
                        double startTime = 0;

                        if (!file.exists())
                            System.out.println("해당 파일은 존재하지 않습니다.");
                        else {
                            FileInputStream fis = new FileInputStream(file);
                            startTime = System.currentTimeMillis();
                            try {
                                while ((readBytes = fis.read(buffer)) > 0) {
                                    out2.write(buffer, 0, readBytes);
                                    totalReadyBytes += readBytes;
                                    System.out.println("In progress: " + totalReadyBytes + "/" + fileSize + " Byte(s) (" + (totalReadyBytes * 100 / fileSize) + " %)");
                                }
                                out.writeUTF(address+","+input+","+sig);
                                System.out.println("파일 전송이 완료되었습니다.");
                            }catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("파일 전송에 실패하였습니다.");
                            } finally {
                                fis.close();
                                continue;
                            }
                        }
                    }
                    out.writeUTF("[" + name + "]" + str);
                }
            } catch (IOException e) {}
        }
    }

    static class ClientReceiver extends Thread {
        Socket socket;
        DataInputStream in;
        InputStream in2;

        ClientReceiver(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
                in2 =  socket.getInputStream();
            } catch (IOException e) {
            }
        }

        public synchronized void run() {
            while (in != null || in2 != null) {
                try {
                    if (in.readUTF() != null) {
                        getFile(fnam);
                        continue;
                    }
                } catch (Exception e) {
                } finally {
                    try{
                        System.out.println(in.readUTF());
                    } catch (Exception e){}
                }
            }
        }

        void getFile(String arr) throws Exception {

            FileOutputStream file = new FileOutputStream(arr);
            byte[] buffer = new byte[10000];
            int readBytes;

            while ((readBytes = in2.read(buffer)) != -1) {
                file.write(buffer, 0, readBytes);
            }
            file.close();

        }
    }


}
