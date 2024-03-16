package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyboardHandler implements Runnable {

    OutputStream outputStream;
    private boolean waiting;
    private boolean err;
    private boolean logged;
    AtomicBoolean closed = new AtomicBoolean(false);
    String fileToDownload;


    public KeyboardHandler(OutputStream sockOutputStream){
        this.outputStream = sockOutputStream;
        this.waiting = false;
        this.err = false;
        this.logged = false;
    }

    public void run(){
        Scanner userInput = new Scanner(System.in); // get command from keyboard
        while(userInput.hasNext()){
            String input = userInput.nextLine();
            String[] inputBlocks = input.split(" ", 2);
            if(inputBlocks.length != 0 && getShortOP(inputBlocks[0]) != -1){ // we can get the OPERATION code
                TFTPRequest OPcode = TftpEnum.decodeOperationCode(getShortOP(inputBlocks[0]));
                if (inputBlocks.length > 1){ // check if this operation have data
                    try {
                        if (!handleOperationwithData(OPcode, inputBlocks[1]))
                            return;
                    }
                    catch(IllegalArgumentException | IOException e) {
                        e.printStackTrace();
                        continue;
                    } catch (InterruptedException e) {
                        userInput.close();
                        return;
                    }
                }
                else {
                    try {
                        if (!handleOperationwithoutData(OPcode))
                            return;
                    }
                    catch(IllegalArgumentException | IOException e) {
                        e.printStackTrace();
                        continue;
                    } catch (InterruptedException e) {
                        userInput.close();
                        return;
                    } 
                }
            }
        }
        userInput.close();
    }

    public boolean handleOperationwithData(TFTPRequest OPcode, String OPdata) throws IOException, InterruptedException, IllegalArgumentException {
        if (OPdata == null) {
            if (OPcode == TFTPRequest.LOGRQ){
                throw new IllegalArgumentException("missing userName");
            }
            else { // DELRQ, RRQ, WRQ,
                throw new IllegalArgumentException("missing fileName");
            }
        }

        switch(OPcode){
            case RRQ:
                RRQoperation(OPdata);
                break;
            case WRQ: 
                WRQoperation(OPdata);
                break;
            case LOGRQ:
                LOGRQoperation(OPdata);
                break;
            case DELRQ:
                DELRQoperation(OPdata);
                break;
            default:
                break;
        }
        return true;
    }

    public void RRQoperation(String data) throws IOException, InterruptedException { // SEND A REQUEST TO DOWNLOAD A FILE
        File newFile = new File(System.getProperty("user.dir") + "/" + data);
        if (!newFile.createNewFile()) {
            System.out.println("file already exists");
            return;
        }

        fileToDownload = data;
        boolean errorAccured = false;
        synchronized (this) {
            outputStream.write(TftpPacket.RRQ(data));
            outputStream.flush();
            errorAccured = checkForError();
        }

        fileToDownload = null;
        if (errorAccured) {
            newFile.delete();
        } else {
            System.out.println("RRQ " + data + " complete");
        }

    }

    public void WRQoperation(String data) throws IOException, InterruptedException { // SEND A REQUEST TO UPLOAD A FILE
        try {
            
            TftpReader file = new TftpFileInputStream(data); // open file
            synchronized (this) {
                outputStream.write(TftpPacket.WRQ(data)); // write to it
                outputStream.flush();
                if (checkForError()) {
                    return;
                }
           }

            TftpDataPacketGenerate builder = new TftpDataPacketGenerate(file);
            byte[] packet = builder.NextPacket();
            while (packet != null) {
                synchronized (this) {
                    outputStream.write(packet);
                    if (checkForError()) {
                        return;
                    }
                }
                packet = builder.NextPacket();
            }

        } catch (FileNotFoundException e) {
            System.out.println("file does not exists");
            return;
        }
    }

    public void LOGRQoperation(String data) throws IOException, InterruptedException {
        logged = true;
        synchronized (this) {
            outputStream.write(TftpPacket.LOGRQ(data));
            outputStream.flush();
            checkForError();
        }
    }

    public void DELRQoperation(String data) throws IOException, InterruptedException{
        synchronized (this) {
            outputStream.write(TftpPacket.DELRQ(data));
            outputStream.flush();
            checkForError();
        }
    }

    public boolean handleOperationwithoutData(TFTPRequest OPcode) throws IOException, InterruptedException, IllegalArgumentException {
        switch(OPcode){
            case DIRQ:
                DIRQoperation();
                break;
            case DISC:
                System.out.println("Tamar: "+ "DISC");
                DISCoperation();
                break;
            default:
                break;
        }
        return true;
    }
    
    public void DIRQoperation() throws IOException, InterruptedException{
        synchronized (this) {
            outputStream.write(TftpPacket.DIRQ());
            outputStream.flush();
            checkForError();
        }
    }

    public void DISCoperation() throws IOException, InterruptedException{
        System.out.println("Tamar: "+ "DISCoperation");
        if (!logged) {
            System.out.println("Tamar: "+ "user is not logged - close program");
            closed.set(true);
            System.exit(0);
            return;
        }

        synchronized (this) {
            outputStream.write(TftpPacket.DISC());
            outputStream.flush();
            closed.set(true);
            checkForError();
            System.exit(0);
        }
    }


    private boolean checkForError() throws InterruptedException {
        waiting = true;
        while (waiting) {
            this.wait();
        }
        boolean errorAccured = err;
        err = false;
        return errorAccured;
    }

    public short getShortOP(String opCode){
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
            case "DISC":
                return 10;
            default:
                return -1;
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    synchronized public void stopWaiting(boolean err) {
        waiting = false;
        this.err = err;
        this.notify();
    }
}
