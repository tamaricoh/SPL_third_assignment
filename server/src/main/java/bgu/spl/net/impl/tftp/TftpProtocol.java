package bgu.spl.net.impl.tftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;
    private boolean shouldTerminate = false; // for the shouldTerminate() func to return
    private String pathToCurrDir = "./Flies";
    private boolean loggedIn = false;

    private TftpFileOutputStream fileToWrite; // if we want to write more then 512 bytes.
    private TftpFileInputStream fileToRead; // for RRQ and DIRQ

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

    public void RRQoperation(byte[] message) {
        synchronized(connections){ // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGdecoder(message);
                TftpFileInputStream fileStream;
                try {
                    fileStream = new TftpFileInputStream(fileName);
                } catch (FileNotFoundException e){
                    // send error ---------------------------------
                    return;
                }

                if (!loggedIn){ // if client is not logged
                    // send error ---------------------------------
                    return;
                }

                this.fileToRead = fileStream;
                // create the data packet-------------------------
                // send it ---------------------------------
            }
        }

    }

    public void WRQoperation(byte[] message) {
        synchronized(connections){ // so the client wouldn't get another packet
            synchronized(pathToCurrDir) { // so current directory wouldn't change during operation
                String fileName = pathToCurrDir+MSGdecoder(message);
                TftpFileOutputStream fileWrite;
                try {
                    fileWrite = new TftpFileOutputStream(fileName);
                } catch (FileNotFoundException e){
                    // send error ---------------------------------
                    return;
                } catch(FileAlreadyExistsException e) {
                    // send error ---------------------------------
                    return;
                }

                if (!loggedIn){ // if client is not logged
                    // send error ---------------------------------
                    return;
                }

                this.fileToWrite = fileWrite; // if we want to write more then 512 bytes.
                // create the ACK packet-------------------------
                // send it ---------------------------------
            
            }
        }
    }

    public void DATAoperation(byte[] message) {
        if (fileToRead == null) { // we didnt ask for RRQ or DIRQ operations
            return;
        }
        short Size =  ( short ) ((( short ) message [0]) << 8 | ( short ) ( message [1]) );
        short numOfBlocks =  ( short ) ((( short ) message [2]) << 8 | ( short ) ( message [3]) );
        byte[] data = Arrays.copyOfRange(message, 4, Size+4);
        // freate file, write to it, create ACK packet--------------------------------------
        this.fileToRead = null;
    }

    public void ACKoperation(byte[] message) {

    }

    public void ERRORoperation(byte[] message) {

    }

    public void DIRQoperation(byte[] message) {

    }

    public void LOGRQoperation(byte[] message) {
        this.loggedIn = true;
    }

    public void DELRQoperation(byte[] message) {

    }

    public void BCASToperation(byte[] message) {

    }

    public void DISCoperation(byte[] message) {

    }

    public String MSGdecoder(byte[] message){
        return new String(message, StandardCharsets.UTF_8);
    }

    
}
