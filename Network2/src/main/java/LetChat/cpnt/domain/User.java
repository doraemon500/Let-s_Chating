package LetChat.cpnt.domain;


public class User {
    String id;
    String pwd;
    String ip;
    String opensign;

    public User() {
    }

    public User(String id, String password, String ip, String opensign) {
        this.id = id;
        this.pwd = password;
        this.ip = ip;
        this.opensign = opensign;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPassword() {
        return pwd;
    }
    public void setPassword(String pwd) {
        this.pwd = pwd;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {this.ip = ip;}
    public String getOpensign() {
        return opensign;
    }
    public void setOpensign(String opensign) {this.opensign = opensign;}
}
