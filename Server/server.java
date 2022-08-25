mport java.net.*;
import java.io.*;
import java.util.*;


public class server {
    HashMap clients;

    server() {
        clients = new HashMap();
        Collections.synchronizedMap(clients);
    }

    public static void main(String args[]) {
        new server().start();
    }

    public void start() {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(7777);
            System.out.println("서버가 시작되었습니다");
            while (true) {
                socket = serverSocket.accept();
                System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하셨습니다");
                ServerReceiver thread = new ServerReceiver(socket);
                thread.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ServerReceiver extends Thread {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;


        ServerReceiver(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream((socket.getInputStream()));
                out = new DataOutputStream((socket.getOutputStream()));

            } catch (IOException e) {
            }
        }

        public void run() {
            String name = "";

            try {
                name = in.readUTF();
                sendToAll("#" + name + "님이 들어오셨습니다");

                clients.put(name, out);
                System.out.println("현재 서버접속자 수는" + clients.size() + "입니다.");

                while(in != null) {
                    synchronized (this) {
                        try {
                            String st = "";
                            st = (String) in.readUTF();
                            String[] arr = st.split(",", 3);
                            if (arr[2].equals("55")) {
                                ServerFilereceiever thread = new ServerFilereceiever(socket);
                                thread.start();
                                continue;
                            }
                        } catch (Exception e) {
                        }
                        sendToAll(in.readUTF());
                    }
                }
            } catch (IOException e) {

            } finally {
                sendToAll("#" + name + "님이 나가셨습니다");
                clients.remove(name);
                System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하셨습니다");
                System.out.println("현재 접속자 수는" + clients.size() + "입니다.");
            }
        }
    }

    class ServerFilereceiever extends Thread {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        OutputStream out2;
        InputStream in2;


        ServerFilereceiever(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream((socket.getInputStream()));
                out = new DataOutputStream((socket.getOutputStream()));

                in2 = socket.getInputStream();
                out2 = socket.getOutputStream();

            } catch (IOException e) {
            }
        }

        public void run() {
            String st = "";
            try {
                st = (String) in.readUTF();
                String[] arr = st.split(",", 2);

                getFile(arr);
                sendToOne(arr);

            } catch (Exception e) {
                try {
                    sendToAll(in.readUTF());
                } catch (Exception a) {
                }
            }

        }

        void getFile(String[] arr) throws Exception {

            FileOutputStream file = new FileOutputStream(arr[1]);
            byte[] buffer = new byte[10000];
            int readBytes;

            while ((readBytes = in2.read(buffer)) != -1) {
                file.write(buffer, 0, readBytes);
            }
            file.close();

        }
    }

    void sendToAll(String msg) {
        Iterator it = clients.keySet().iterator();

        while (it.hasNext()) {
            try {
                DataOutputStream out = (DataOutputStream) clients.get(it.next());
                out.writeUTF(msg);
            } catch (IOException e) {
            }
        }
    }

    void sendToOne(String[] arr) {
        Iterator it = clients.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.equals(arr[0])) {
                try {
                    OutputStream out = (OutputStream) clients.get(key);
                    File file = new File(arr[1]);
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[10000];
                    int readBytes;

                    while ((readBytes = fis.read(buffer)) > 0)
                        out.write(buffer, 0, readBytes);
                    fis.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}
