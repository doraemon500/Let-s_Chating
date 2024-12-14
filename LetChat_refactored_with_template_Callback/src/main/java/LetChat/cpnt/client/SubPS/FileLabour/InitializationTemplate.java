package LetChat.cpnt.client.SubPS.FileLabour;

import LetChat.cpnt.client.SubPS.InitCallback;

import java.io.IOException;
import java.io.InputStream;

public class InitializationTemplate {
    InputStream in;
    InitCallback init;

    public InitializationTemplate(InputStream in, InitCallback init){
        this.in = in;
        this.init = init;
    }

    public void run(){
        try {
            init.Initializaioncallback();
            in.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}