package bgu.spl.net.impl.tftp;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;
import java.lang.Thread;


public class KeyboardHandler<T> implements Runnable {

    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket serverSocket;
    private final OutputStream serverOutStream;
    private volatile boolean connected;
    volatile boolean terminate;
    volatile boolean waiting;

    public KeyboardHandler(Socket sock, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol) {
        this.serverSocket = sock;
        this.encdec = reader;
        this.protocol = protocol;
        serverOutStream = new BufferedOutputStream()
        connected = false;
        terminate = false;
        waiting = false;
    }

    @Override
    public void run() {
        Scanner userInput = new Scanner(System.in);
        while (!terminate){
            if(userInput.hasNext()){
                String input = userInput.nextLine();
                String[] inputBlocks = input.split(" ");
                byte opCode = getOpCode(inputBlocks[0]);
                if(opCode<0){continue;}
                send(getPacket(opCode, inputBlocks));
            }

        }
        userInput.close();

       
    }

    private byte getOpCode(String opCode){
        switch (opCode) {
            case "RRQ":
                return 1;
            case "WRQ":
                return 2;
            case "DIRQ":
                return 6;
            case "LOGRQ":
                return 7;
            case "DELRQ":
                return 8;
            case "DIS":
                return 10;
            default:
                return -1;
        }
    }


    private byte[] getPacket(byte opCode,String[] inputString ){
        switch (opCode) {
            case 1:
            case 2:
            case 7:
            case 8:
                if (inputString.length < 2 ){return null;}
                else{return generateSpecPacket((byte)opCode, inputString[1]);}
            case 6:
            case 10:
                byte[] DISPacket = new byte[2];
                DISPacket[0] =0; DISPacket[1] = (byte) opCode;
                return DISPacket;
        
            default:
                return null;
        }
    }
    

    public void send(byte[] message) {
        if (message !=null){
            try {
                serverSocket.getOutputStream().write(message);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private void waitResponse(){
        waiting = true;
        synchronized(serverOutStream){
            while(waiting){
                try {
                    wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        

    }








    private void fill(byte[] a, byte[] b, int start, int end){
        for(int i = 0; i<b.length & i+start <= end; i++){
            a[start+i] = b[i];
        }
    }

    private boolean acceptableFileName(byte[] fileName){ //checks whether the filename has a byte "0" and if so returns false, since this is a bad file name
        boolean noZero = true;
        for(byte b: fileName){
            if( b == 0) noZero = false;
        }
        return noZero;
    }

    private byte[] generateSpecPacket(byte opCode, String name){
        try {
            byte[] encodeName = name.getBytes("UTF-8");
            if(!acceptableFileName(encodeName)){
                //print error to user
                return null;
            }
            byte[] Packet = new byte[2+encodeName.length+ 1];
            Packet[0] = 0; Packet[1] = opCode; Packet[Packet.length-1] = 0;
            fill(Packet, encodeName,2,Packet.length-2);
            return Packet;
        } catch (Exception e) {
            return null;
        }
    }
}
