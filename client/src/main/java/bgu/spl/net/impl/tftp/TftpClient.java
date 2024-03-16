package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.net.Socket;

import java.io.InputStream;
import java.io.OutputStream;

public class TftpClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            args = new String[] { "localhost", "7777" };
        }

        if (args.length == 1) {
            args = new String[] { args[0], "7777" };
        }

        try (Socket sock = new Socket(args[0], Integer.parseInt(args[1]))){
            
            OutputStream outputStream = sock.getOutputStream();
            InputStream inputStream = sock.getInputStream();

            KeyboardHandler keyboard = new KeyboardHandler(outputStream);
            Thread keyboardThread = new Thread(keyboard);
            Thread listeningThread = new Thread(new listenHandler(outputStream, inputStream, keyboard));

            keyboardThread.start();
            listeningThread.start();
            keyboardThread.join();
            sock.close();
            listeningThread.interrupt();
            listeningThread.join();
        }
    }
}
