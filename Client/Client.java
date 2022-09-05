import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class client {
    static String serverIp = "127.0.0.1";

    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("USAGE: java Client 대화명");
            System.exit(0);
        }

        try {

            Socket socket = new Socket(serverIp, 7777);
            Socket socket0 = new Socket("127.0.0.1", 7772);
            System.out.println("서버에 연결되었습니다");
            Thread sender = new Thread(new ClientSender(socket, socket0, args[0]));
            Thread receiver = new Thread(new ClientReceiver(socket, socket0));

            sender.start();
            receiver.start();
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {}
    }

     static class ClientSender extends Thread  {
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
                        String input = JOptionPane.showInputDialog("보내고자 하는 파일 명을 입력해주세요.");
                        int sig = 5;
                        File file = new File(input);
                        String who = JOptionPane.showInputDialog("자신의 계정을 적어주세요!");
                        String address = JOptionPane.showInputDialog("누구한테 보낼까요?");

                        long fileSize = file.length();
                        long totalReadyBytes = 0;
                        byte[] buffer = new byte[10000];
                        int readBytes;
                        double startTime = 0;

                        if (!file.exists())
                            System.out.println("해당 파일은 존재하지 않습니다.");
                        else  {
                            FileInputStream fis = new FileInputStream(file);
                            try {
                                out.writeUTF(address+","+input+","+sig + "," + who );

                                while ((readBytes = fis.read(buffer)) > 0) {
                                    out2.write(buffer, 0, readBytes);
                                    totalReadyBytes += readBytes;
                                    System.out.println("In progress: " + totalReadyBytes + "/" + fileSize + " Byte(s) (" + (totalReadyBytes * 100 / fileSize) + " %)");
                                }
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
        ServerSocket serverSocket;
        Socket FileSocket;
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
                int cnt = 0;
                try {
                    st = in.readUTF();
                    String[] arr = st.split(",", 3);
                    if (st != null) {
                        GetFile getFile = new GetFile(socket0, in2, arr[1]);
                        getFile.start();
                    }

                    Initialization init = new Initialization(in2);
                    init.start();
                    System.out.println("다운로드가 무사히 완료되었습니다!");

                    } catch(Exception e){
                        System.out.println(st);
                    } finally{
                        cnt++;
                    }
                }
            }

    }

    static class GetFile extends Thread {
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
            }catch (Exception e){System.out.println("다운로드중 오류가 발생했어요 ㅠ..");}
        }
    }

   static class Initialization extends Thread {
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


}
