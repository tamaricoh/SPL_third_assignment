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
    private TftpPacketGenerator packetGenerator = new TftpPacketGenerator();
    private byte[] packet;

    private TftpFileOutputStream fileToWrite; // where to write the data after a WRQ operation
    private String fileName = "";


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
        System.out.println("Tamar: "+"process");
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
                System.out.println("Tamar: "+"LOGRQ");
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
        return shouldTerminate;
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
                    byte[] errorNum = {0, 1};
                    packet = packetGenerator.generateError(errorNum, message);
                    // connections.send(connectionId, packet);
                    return;
                }

                if (loggedIn){ // if user is not logged - do nothing
                    // create the data packet-------------------------
                    // send it ---------------------------------
                    return;
                }
                byte[] errorNum = {0, 6};
                packet = packetGenerator.generateError(errorNum, message);
                // connections.send(connectionId, packet);
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
                    byte[] errorNum = {0, 1};
                    packet = packetGenerator.generateError(errorNum, message);
                    // connections.send(connectionId, packet);
                    return;
                } catch(FileAlreadyExistsException e) {
                    byte[] errorNum = {0, 5};
                    packet = packetGenerator.generateError(errorNum, message);
                    // connections.send(connectionId, packet);
                    return;
                }

                if (loggedIn){ // if user is not logged - do nothing
                    this.fileToWrite = fileWrite; // where to write the data after a WRQ operation
                    this.fileName = fileName;
                    // send ACK -> start transfer the file
                    byte[] blockNum = {0, 0};
                    // packet = packetGenerator.generateACk(blockNum);
                    // connections.send(connectionId, packet);
                    return;
                }
                byte[] errorNum = {0, 6};
                packet = packetGenerator.generateError(errorNum, message);
                // connections.send(connectionId, packet);
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
            done = fileToWrite.writeToFile(data);
        } catch (IOException e) {
            fileToWrite = null;
            return;
        }

        // this block shold be for numOfBlocks ----------------------??????
        byte[] blockNum = {0, 0};
        // packet = packetGenerator.generateACk(blockNum);
        // connections.send(connectionId, packet);
        if (done) {
            byte[] transfer = {0, 1};
            packet = packetGenerator.generateBCAST(transfer, fileName.getBytes());
            // connections.send(connectionId, packet);
            fileToWrite = null;
        }
    }

    public void ACKoperation(byte[] message) {

    }

    public void ERRORoperation(byte[] message) {
        return;
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
        System.out.println("Tamar: "+"LOGRQoperation");
        String userName = MSGencoder(message);
        System.out.println("Tamar: "+"create userName");
        synchronized(connections) { // so the client wouldn't get another packet
            System.out.println("Tamar: "+"synchronized(connections)");
            if (this.loggedInUsers.containsKey(userName)){
                System.out.println("Tamar: "+"user already logged");
                byte[] errorNum = {0, 7};
                packet = packetGenerator.generateError(errorNum, message);
                connections.send(connectionId, packet);
                return;
            }
            System.out.println("Tamar: "+"logging in");
            loggedInUsers.put(userName, this.connectionId);
            this.loggedUser = userName;
            this.loggedIn = true;
            // packet = TftpPacket.ACKFor((short)0);
            connections.send(connectionId, TftpPacket.ACKFor((short)0));
        }
    }

    public void DELRQoperation(byte[] message) {
        synchronized(connections) { // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGencoder(message);
                File file = new File(fileName);
                if (!file.exists()) {
                    // send error file not found
                    byte[] errorNum = {0, 1};
                    packet = packetGenerator.generateError(errorNum, message);
                    // connections.send(connectionId, packet);
                    return;
                }

                if (loggedIn) { // if client is not logged, he can't make operations
                    
                    if (!file.delete()){
                        byte[] errorNum = {0, 2};
                        packet = packetGenerator.generateError(errorNum, message);
                        // connections.send(connectionId, packet);
                        return;
                    }
    
                    byte[] blockNum = {0, 0};
                    // packet = packetGenerator.generateACk(blockNum);
                    // connections.send(connectionId, packet);
                    byte[] transfer = {0, 0};
                    packet = packetGenerator.generateBCAST(transfer, fileName.getBytes());
                    // connections.send(connectionId, packet);
                    return;
                }
                byte[] errorNum = {0, 6};
                packet = packetGenerator.generateError(errorNum, message);
                // connections.send(connectionId, packet);
                return;
            }
        }
    }

    public void BCASToperation(byte[] message) {
        return;
    }

    public void DISCoperation(byte[] message) {
        synchronized(connections) { // so the client wouldn't get another packet
            if (loggedIn) { // if user is not logged - do nothing
                loggedInUsers.remove(this.loggedUser);
                shouldTerminate = true;
                byte[] blockNum = {0, 0};
                // packet = packetGenerator.generateACk(blockNum);
                // connections.send(connectionId, packet);
            }
            byte[] errorNum = {0, 6};
            packet = packetGenerator.generateError(errorNum, message);
            // connections.send(connectionId, packet);
            return;
        }
    }

    public String MSGencoder(byte[] message){
        return new String(message, StandardCharsets.UTF_8);
    }    
}

