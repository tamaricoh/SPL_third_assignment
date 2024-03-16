package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.tftp.TftpEnum;


public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;
    private boolean shouldTerminate = false; // for the shouldTerminate() func to return
    private String pathToCurrDir = "./Flies/";

    private static HashMap<String, Integer> loggedInUsers = new HashMap<>(); // contain all logged users, // <userName, connectionID>
    private boolean loggedIn = false;
    private String loggedUser = "";
    private TftpDataPacketGenerate dataPacketGenerator;
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
        if (message.length < 2){
            throw new UnsupportedOperationException("No operation number");
        }
        TFTPRequest OperationCode = TftpEnum.decodeOperationCode(( short ) ((( short ) message [0]) << 8 | ( short ) ( message [1]) ));
        message = Arrays.copyOfRange(message, 2, message.length);

        switch (OperationCode) {
            case RRQ:
                RRQoperation(message);
                break;
            case WRQ:
                WRQoperation(message);
                break;
            case DATA:
                DATAoperation(message);
                break;
            case ACK:
                ACKoperation(message);
                break;
            case ERROR:
                ERRORoperation(message);
                break;
            case DIRQ:
                DIRQoperation(message);
                break;
            case LOGRQ:
                LOGRQoperation(message);
                break;
            case DELRQ:
                DELRQoperation(message);
                break;
            case BCAST:
                BCASToperation(message);
                break;
            case DISC:
                DISCoperation(message);
                break;
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
                    connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_FileNotFound, "File not found"));
                    return;
                }

                if (loggedIn){ // if user is not logged - do nothing
                    this.dataPacketGenerator = new TftpDataPacketGenerate(fileStream);
                    sendData(); // keep sending if get ACK
                    return;
                }
                connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_UserNotLogged, "user not logged"));
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
                    connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_FileNotFound, "File not found"));
                    return;
                } catch(FileAlreadyExistsException e) {
                    connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_FileAlreadyExists, "File already exists"));
                    return;
                }

                if (loggedIn){ // if user is not logged - do nothing
                    this.fileToWrite = fileWrite; // where to write the data after a WRQ operation
                    this.fileName = fileName;
                    // send ACK -> start transfer the file
                    connections.send(connectionId, TftpPacket.ACK((short)0));
                    return;
                }
                connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_UserNotLogged, "user not logged"));
                return;
                
            
            }
        }
    }

    public void DATAoperation(byte[] message) {
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

        connections.send(connectionId, TftpPacket.ACK(numOfBlocks));
        if (done) {
            sendBCAST(this.fileName, true);
            this.fileToWrite = null;
            this.fileName = "";
        }
    }

    public void ACKoperation(byte[] message) {
        synchronized(connections) {
            sendData();
        }
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

                    TftpDataReader dataReader = new TftpDataReader(data); 
                    this.dataPacketGenerator = new TftpDataPacketGenerate(dataReader);
                    sendData(); // send once, the next will come from acks if needed.
                }
            }
            return;
        }
    }

    public void LOGRQoperation(byte[] message) {
        String userName = MSGencoder(message);
        synchronized(connections) { // so the client wouldn't get another packet
            if (this.loggedUser != "" || loggedInUsers.containsKey(userName)){  
                connections.send(connectionId,TftpPacket.ERROR(TftpPacket.ERR_UserAlreadyLogged, "User/Client already logged in"));
                return;
            }
            loggedInUsers.put(userName, this.connectionId);
            this.loggedUser = userName;
            this.loggedIn = true;
            connections.send(connectionId, TftpPacket.ACK((short)0));
        }
    }

    public void DELRQoperation(byte[] message) {
        synchronized(connections) { // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGencoder(message);
                File file = new File(fileName);
                if (!file.exists()) {
                    // send error file not found
                    connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_FileNotFound, "File not found"));
                    return;
                }

                if (loggedIn) { // if client is not logged, he can't make operations
                    if (!file.delete()){
                        connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_FileAccessViolation, "deletion could not be resolve"));
                        return;
                    }
                    // send ACK & BCAST
                    connections.send(connectionId, TftpPacket.ACK((short)0));
                    sendBCAST(fileName, false);
                    return;
                }
                connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_UserNotLogged, "user not logged"));
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
                connections.send(connectionId, TftpPacket.ACK((short)0));
                return;
            }
            connections.send(connectionId, TftpPacket.ERROR(TftpPacket.ERR_UserNotLogged, "user not logged"));
            return;
        }
    }

    public String MSGencoder(byte[] message){
        return new String(message, StandardCharsets.UTF_8);
    }    

    private void sendBCAST(String fileName, Boolean ifAdd) {
        byte[] BCASTpacket = TftpPacket.BCAST(fileName, ifAdd);
        for (Integer id : loggedInUsers.values()) { // send each client
            connections.send(id, BCASTpacket);
        }
    }

    private void sendData() {
        if (dataPacketGenerator == null) {
            return;
        }
        try {
            byte[] dataPacket = dataPacketGenerator.NextPacket();
            if (dataPacket != null) {
                connections.send(this.connectionId, dataPacket);
            }
        } catch (IOException e) {
            dataPacketGenerator = null;
        };

    }
}

