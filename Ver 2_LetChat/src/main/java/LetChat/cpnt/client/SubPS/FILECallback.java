package LetChat.cpnt.client.SubPS;

import java.io.IOException;

public interface FILECallback
{
   public abstract void GetFILEcallback(byte[] buffer, int readBytes) throws IOException;
}