package bgu.spl.net.impl.tftp;

import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;
// import bgu.spl.net.impl.tftp.TftpEnum;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private byte[] msgToBytes = new byte[1 << 10];
    private short OperationCode = -1;
    private short BytesCounter = 0;
    // private TFTPRequest opCode = TFTPRequest.NONE;
    private TFTPRequest opCode = TftpEnum.create();

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        System.out.println("Tamar: " + "decodeNextByte");
        if (nextByte == '\0' && TftpEnum.TFTPRequestEndsWithZero(opCode)){ // and the msg needs to end with a zero
            System.out.println("Tamar: " + "finish and EndsWithZero");
            return returnMSG();
        }

        // start create the msg
        msgToBytes[BytesCounter] = nextByte;
        BytesCounter++;

        // if we are reading the opcode
        if (BytesCounter < 2){
            System.out.println("Tamar: " + "reading the opcode");
            return null;
        }

        // if we can determine the operation
        if (BytesCounter == 2){
            System.out.println("Tamar: " + "determine the operation");
            OperationCode = ( short ) ((( short ) msgToBytes [0]) << 8 | ( short ) ( msgToBytes [1]) );
        }

        // handle operations that ends after 2 bytes (DISC, DIRC, ACK, DATA) -
        opCode = TftpEnum.decodeOperationCode(OperationCode);
        if (opCode == TFTPRequest.DIRQ || opCode == TFTPRequest.DISC){
            System.out.println("Tamar: " + "DIRQ or DISC");
            return returnMSG();
        }

        if (opCode == TFTPRequest.ACK && BytesCounter == 4){
            System.out.println("Tamar: " + "ACK");
            return returnMSG();
        }

        if (opCode == TFTPRequest.DATA && BytesCounter >= 6){
            short DATAnumOfBytes = ( short ) ((( short ) msgToBytes [2]) << 8 | ( short ) ( msgToBytes [3]) );
            if (BytesCounter - DATAnumOfBytes == 6){
                System.out.println("Tamar: " + "DATA");
                return returnMSG();
            }
        }

        return null;
    }

    @Override
    public byte[] encode(byte[] message) {              //TO BE IMPLEMENTED, this function is supposed to do somthing right?
        System.out.println("Tamar: " + "encode");
        return message;
    }

    private byte[] returnMSG(){
        System.out.println("Tamar: " + "returnMSG");
        // copy the msg
        byte[] msg = Arrays.copyOfRange(msgToBytes, 0, BytesCounter);
        // resets the fields
        msgToBytes = new byte[1 << 10];
        BytesCounter = 0;
        OperationCode = -1;
        opCode = TFTPRequest.NONE;
        // returns
        return msg;
    }
}