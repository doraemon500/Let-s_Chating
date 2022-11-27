package LetChat.cpnt.client.SubPS.FileLabour;

import LetChat.cpnt.client.SubPS.FILECallback;
import LetChat.cpnt.client.SubPS.IterCallback;

import java.io.InputStream;
import java.net.Socket;
import java.util.Iterator;

public class GetFileTemplate {
    static Socket socket;
    static InputStream in;
    static FILECallback fileCallback;
    static IterCallback iterCallback;
    static String arr;
    static Iterator it;

    public GetFileTemplate(InputStream in, FILECallback fileCallback) {
        this.in = in;
        this.fileCallback = fileCallback;
    }

    public GetFileTemplate(String arr, Iterator it, IterCallback iterCallback) {
        this.arr = arr;
        this.it = it;
        this.iterCallback = iterCallback;
    }

    public static void ReadFILE() {
        try {
            byte[] buffer = new byte[10000];
            int readBytes;
            if ((readBytes = in.read(buffer)) != -1) {
                fileCallback.GetFILEcallback(buffer, readBytes);
            }
        } catch (Exception e) {
            System.out.println("System Message :: 다운로드 실패! 다운로드 중 오류가 발생했습니다.");
            throw new RuntimeException(e);
        }
    }

    public static Object Iterator() {
        try{
            while(it.hasNext()) {
                String key = (String)it.next();
                if(key.equals(arr)) {
                    Object rst = iterCallback.IteratorCallback(key);
                    return rst;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return -1;
    }
}
