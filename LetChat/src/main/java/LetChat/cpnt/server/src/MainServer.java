package LetChat.cpnt.server;

import java.net.*;
import java.io.*;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import LetChat.cpnt.domain.User;
import LetChat.cpnt.dao.*;

public class MainServer {
    public static UserDao dao;

    public static void main(String[] args){
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
        dao = context.getBean(UserDaoJdbc.class);
        new MainServer().start();
    }

    public void start(){
        ServerSocket serverSocket = null;
        Socket socket = null;

        try{
                serverSocket = new ServerSocket(7555);
                while(true) {
                    socket = serverSocket.accept();

                    Thread thread = new ServerReceiver(socket);
                    thread.start();
                }
        }catch (Exception e){}


    }

    static class ServerReceiver extends Thread {

        Socket socket;
        DataOutputStream out;
        DataInputStream in;

        ServerReceiver(Socket socket) {
            this.socket = socket;
            try {
                this.out = new DataOutputStream(socket.getOutputStream());
                this.in = new DataInputStream(socket.getInputStream());
            } catch (Exception e) {
            }
        }


        public void run() {
            String info;
            User user;
            User userget;
            User init = new User(null, null, null, null);

            while (true) {
                try {
                    info = in.readUTF();
                    String[] arr = info.split(",", 5);
                    user = new User(arr[0], arr[1], arr[3], arr[4]);

                    switch(arr[2]){
                        case "login" :
                        {
                            userget = dao.get(arr[0]);
                            if (arr[1].equals(userget.getPassword()) && arr[4].equals(userget.getOpensign())) {
                                user = new User(arr[0], arr[1], arr[3], "2001");
                                init = user;
                                dao.update(user);
                                out.writeUTF("1");
                            } else out.writeUTF("0");
                            continue;
                        }
                        case "sign_in" :
                        {
                            try {
                                if (dao.get(arr[0]) != null) {
                                    out.writeUTF("-1");
                                    continue;
                                }
                            } catch (Exception e){
                                dao.add(user);
                                out.writeUTF("0");
                                continue;
                            }
                        }
                        case "join" :
                        {
                            List<User> list = dao.getAll();
                            out.writeUTF("0");
                            out.writeUTF(list.size() + "");
                            for(int i = 0; i < list.size(); i++){
                                out.writeUTF(list.get(i).getId() + "," + list.get(i).getIp());
                            }
                            continue;
                        }
                        case "create" :
                        {
                            String opensign = arr[4] ;
                            User user1 = new User(arr[0], arr[1], arr[3], opensign);
                            dao.update(user1);
                            out.writeUTF("1");
                            continue;
                        }
                        case "update" :
                        {
                            dao.update(user);
                            continue;
                        }
                    }
                }catch (SocketException Se){
                  user = new User(init.getId(), init.getPassword(), "Null", "2000");
                  dao.update(user);
                  break;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    try {
                        out.writeUTF("0");
                        System.out.println("System Message :: 잘못된 접근!");
                    } catch (Exception l){}
                }
            }
        }
    }


}
