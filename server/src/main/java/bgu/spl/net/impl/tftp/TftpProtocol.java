package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.HashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.tftp.TftpEnum;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;
    private boolean shouldTerminate = false; // for the shouldTerminate() func to return
    private String pathToCurrDir = "./Flies";

    private HashMap<String, Integer> loggedInUsers = new HashMap<>(); // contain all logged users, // <userName, connectionID>
    private boolean loggedIn = false;
    private String loggedUser = "";

    private TftpFileOutputStream fileToWrite; // where to write the data after a WRQ operation


    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(byte[] message) {
        // SOME NOTES BEFORE IMPLEMENTATION:
        // 1) msg must be from length 2 at least (for the OPcode)
        // 2) for each operation, we will implement different func

        if (message.length < 2){
            throw new UnsupportedOperationException("No operation number");
        }
        TFTPRequest OperationCode = TftpEnum.decodeOperationCode(( short ) ((( short ) message [0]) << 8 | ( short ) ( message [1]) ));
        message = Arrays.copyOfRange(message, 2, message.length);

        switch (OperationCode) {
            case RRQ:
                RRQoperation(message);
            case WRQ:
                WRQoperation(message);
            case DATA:
                DATAoperation(message);
            case ACK:
                ACKoperation(message);
            case ERROR:
                ERRORoperation(message);
            case DIRQ:
                DIRQoperation(message);
            case LOGRQ:
                LOGRQoperation(message);
            case DELRQ:
                DELRQoperation(message);
            case BCAST:
                BCASToperation(message);
            case DISC:
                DISCoperation(message);
            default:
                return;
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    } 

    public void RRQoperation(byte[] message) { // Download file from the server Files folder to current working directory
        synchronized(connections){ // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGencoder(message);
                TftpFileInputStream fileStream;
                try {
                    // Creating a file in current working directories
                    fileStream = new TftpFileInputStream(fileName);
                } catch (FileNotFoundException e){
                    // send error ---------------------------------
                    return;
                }

                if (loggedIn){ // if user is not logged - do nothing
                    // create the data packet-------------------------
                    // send it ---------------------------------
                    return;
                }
                // send error ---------------------------------
                return;
            }
        }

    }

    public void WRQoperation(byte[] message) { // Upload File from current working directory to the server
        synchronized(connections){ // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGencoder(message);
                TftpFileOutputStream fileWrite;
                try {
                    // Check if file exist
                    fileWrite = new TftpFileOutputStream(fileName);
                } catch (FileNotFoundException e){ 
                    // send error ---------------------------------
                    return;
                } catch(FileAlreadyExistsException e) {
                    // send error ---------------------------------
                    return;
                }

                if (loggedIn){ // if user is not logged - do nothing
                    this.fileToWrite = fileWrite; // where to write the data after a WRQ operation
                    // create the ACK packet-------------------------
                    // send it ---------------------------------
                    return;
                }
                // send error ---------------------------------
                return;
                
            
            }
        }
    }

    public void DATAoperation(byte[] message) {
        // Write to the file that we open in WRQ
        if (fileToWrite == null) { // we didnt ask for WRQ operation. so theres no file to write to.
            return;
        }
        short Size =  ( short ) ((( short ) message [0]) << 8 | ( short ) ( message [1]) );
        short numOfBlocks =  ( short ) ((( short ) message [2]) << 8 | ( short ) ( message [3]) ); // send ACK on this block
        byte[] data = Arrays.copyOfRange(message, 4, Size+4);    

        Boolean done = false;
        try {
            //done = fileToWrite.Write(data);
            // Write to fileToWrite -----------------------------
        } catch (IOException e) {
            fileToWrite = null;
            return;
        };

        // send ACK
        if (done) {
            // send BCAST
            fileToWrite = null;
        }
    }

    public void ACKoperation(byte[] message) {

    }

    public void ERRORoperation(byte[] message) {

    }

    public void DIRQoperation(byte[] message) {
        synchronized(connections) { // so the client wouldn't get another packet
            if (loggedIn) { // if user is not logged - do nothing
                synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                    // SOME NOTES BEFORE IMPLEMENTATION:
                    // 1) this should build a DATA packet, so we need to know the packet size.
                    // 2) byte 0 is dividing the files names

                    int packetSize = 0;
                    File[] files = new File(pathToCurrDir).listFiles();
                    for(File file : files) {
                        packetSize += file.getName().getBytes().length + 1; // + 1 because byte 0 is dividing the files
                    }

                    byte[] data = new byte[packetSize]; // this is the 4th part ot the data packet
                    // create the data bytes array :
                    int index = 0; // loop while index < packetSize
                    for(File file : files){
                        for (byte byteB : file.getName().getBytes()){
                            data[index] = byteB;
                            index++;
                        }
                        data[index] = '\0'; // because byte 0 is dividing the files
                        index++;
                    }

                    // create DATA packet----------------------
                    // SEND IT
                }
            }
            return;
        }
    }

    public void LOGRQoperation(byte[] message) {
        String userName = MSGencoder(message);
        synchronized(connections) { // so the client wouldn't get another packet
            if (this.loggedInUsers.containsKey(userName)){
                // send error - user already logged ----------------------------------
                return;
            }
            loggedInUsers.put(userName, this.connectionId);
            this.loggedUser = userName;
            this.loggedIn = true;
            // send ACK packet-------------------------
        }
    }

    public void DELRQoperation(byte[] message) {
        synchronized(connections) { // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGencoder(message);
                File file = new File(fileName);
                if (!file.exists()) {
                    // send error --------------------------------- file not found
                    return;
                }

                if (loggedIn) { // if client is not logged, he can't make operations
                    
                    if (!file.delete()){
                        // send error ---------------------------------
                        return;
                    }
    
                    // create the ACK packet-------------------------
                    // send it --------------------------------- Bcast
                    return;
                }
                // send error ---------------------------------
                return;
                
            }
        }
    }

    public void BCASToperation(byte[] message) {

    }

    public void DISCoperation(byte[] message) {
        synchronized(connections) { // so the client wouldn't get another packet
            if (loggedIn) { // if user is not logged - do nothing
                loggedInUsers.remove(this.loggedUser);
                shouldTerminate = true;
                // send ACK packet-------------------------
            }
            return;
        }
    }

    public String MSGencoder(byte[] message){
        return new String(message, StandardCharsets.UTF_8);
    }    
}
