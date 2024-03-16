package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class listenHandler implements Runnable {

    KeyboardHandler keyboardHandler;
    InputStream socketInput;
    OutputStream socketOutput;

    private TftpDataWriter writer;
    ByteArrayOutputStream DIRQstreamer;
    TftpEncoderDecoder encoderDecoder = new TftpEncoderDecoder();

    public listenHandler(OutputStream socketOutput, InputStream socketInput, KeyboardHandler keyboardHandler) {
        this.socketOutput = socketOutput;
        this.socketInput = socketInput;
        this.keyboardHandler = keyboardHandler;
    }

    

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                byte[] message = readMessage();
                if(message == null){
                    return;
                }
                process(message);
            } catch (IOException e) {
                if(!keyboardHandler.isClosed()){
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    private byte[] readMessage() throws IOException {
        while(true){
            int readByte = socketInput.read();
            if(readByte == -1){
                return null;
            }
            
            byte[] message = encoderDecoder.decodeNextByte((byte) readByte);
            if (message != null){
                return message;
            }
        }
    }

    public void process(byte[] message) throws IOException {
        if (message.length < 2) {
            throw new UnsupportedOperationException("Internal Error: invalid message got to process");
        }
        TFTPRequest OPCode = TftpEnum.decodeOperationCode(TftpPacket.OPCodeFromBytes(message[0], message[1]));
        byte[] OPdata = Arrays.copyOfRange(message, 2, message.length);
        switch (OPCode) {
            case DATA:
                DATA(OPdata);
                break;
            case ACK:
                ACK(OPdata);
                break;
            case ERROR:
                ERROR(OPdata);
                break;
            case BCAST:
                BCAST(OPdata);
                break;
            default:
                break;
        }
    }

    private void BCAST(byte[] message){
        String deletedOrAdded = message[0] == 0 ? "del" : "add";
        String fileName = new String(Arrays.copyOfRange(message, 1, message.length),StandardCharsets.UTF_8);
        System.out.println("BCAST "+deletedOrAdded+" " + fileName);
    }

    private void ACK(byte[] message){
        short blockNumber = TftpPacket.bytesToShort(message[0], message[1]);
        System.out.println("ACK "+blockNumber);
        keyboardHandler.stopWaiting(false);
    }

    private void ERROR(byte[] message){
        short OPcode = TftpPacket.bytesToShort(message[0], message[1]);
        String errorCause = new String(Arrays.copyOfRange(message, 2, message.length),StandardCharsets.UTF_8);
        System.out.println("Error "+OPcode+" "+errorCause);
        keyboardHandler.stopWaiting(true);
    }

    private void DATA(byte[] message) throws IOException {
        if (writer == null) {
            String fileToDownload = keyboardHandler.fileToDownload;
            if(fileToDownload != null){
                writer = new TftpDataWriter(System.getProperty("user.dir") + "/",fileToDownload);
            }else{
                DIRQstreamer = new ByteArrayOutputStream();
                writer = new TftpDataWriter(DIRQstreamer);
            }
        }

        short packetSize = TftpPacket.bytesToShort(message[0], message[1]);
        short blockNum = TftpPacket.bytesToShort(message[2], message[3]);
        byte[] data = Arrays.copyOfRange(message, 4, packetSize+4);

        Boolean done = false;
        try {
            done = writer.Write(data);
        } catch (IOException e) {
            e.printStackTrace();
            writer = null;
            DIRQstreamer = null;
            keyboardHandler.stopWaiting(true);
            return;
        };

        socketOutput.write(TftpPacket.ACK(blockNum));
        if (done) {
            if(DIRQstreamer != null){
                DIRQ();
            }
            writer = null;
            DIRQstreamer = null;
            keyboardHandler.stopWaiting(false);
        }
    }

    private void DIRQ(){
        byte[] dirqBytes = DIRQstreamer.toByteArray();
        for(int i=0; i < dirqBytes.length; i++){
            if(dirqBytes[i] == 0){
                dirqBytes[i] = '\n';
            }
        }
        System.out.print(new String(dirqBytes, 0, dirqBytes.length, StandardCharsets.UTF_8) + "\n");
    }
    
}
