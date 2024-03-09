package bgu.spl.net.impl.tftp;

import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

enum TFTPRequest{
    NONE,
    RRQ,
    WRQ,
    DATA,
    ACK,
    ERROR,
    DIRQ,
    LOGRQ,
    DELRQ,
    BCAST,
    DISC
}

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private byte[] msgToBytes = new byte[1 << 10];
    private short OperationCode = -1;
    private short BytesCounter = 0;
    private TFTPRequest opCode = TFTPRequest.NONE;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        if (nextByte == '0' && TFTPRequestEndsWithZero(opCode)){ // and the msg needs to end with a zero
            return returnMSG();
        }

        // start create the msg
        msgToBytes[BytesCounter] = nextByte;
        BytesCounter++;

        // if we are reading the opcode
        if (BytesCounter < 2){
            return null;
        }

        // if we can determine the operation
        if (BytesCounter == 2){
            OperationCode = ( short ) ((( short ) msgToBytes [0]) << 8 | ( short ) ( msgToBytes [1]) );
        }

        // handle operations that ends after 2 bytes (DISC, DIRC, ACK, DATA) -
        opCode = decodeOperationCode(OperationCode);
        if (opCode == TFTPRequest.DIRQ || opCode == TFTPRequest.DISC){
            return returnMSG();
        }

        if (opCode == TFTPRequest.ACK && BytesCounter == 4){
            return returnMSG();
        }

        if (opCode == TFTPRequest.DATA && BytesCounter >= 6){
            short DATAnumOfBytes = ( short ) ((( short ) msgToBytes [2]) << 8 | ( short ) ( msgToBytes [3]) );
            if (BytesCounter - DATAnumOfBytes == 6){
                return returnMSG();
            }
        }

        

        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }

    private byte[] returnMSG(){
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

    private TFTPRequest decodeOperationCode(short opCode) {
        switch (opCode) {
            case 1:
                return TFTPRequest.RRQ;
            case 2:
                return TFTPRequest.WRQ;
            case 3:
                return TFTPRequest.DATA;
            case 4:
                return TFTPRequest.ACK;
            case 5:
                return TFTPRequest.ERROR;
            case 6:
                return TFTPRequest.DIRQ;
            case 7:
                return TFTPRequest.LOGRQ;
            case 8:
                return TFTPRequest.DELRQ;
            case 9:
                return TFTPRequest.BCAST;
            case 10:
                return TFTPRequest.DISC;
            default:
                return null;
        }
    }

    private Boolean TFTPRequestEndsWithZero(TFTPRequest opEnum){
        switch (opEnum) {
            case LOGRQ:
            case DELRQ:
            case RRQ:
            case WRQ:
            case BCAST:
            case ERROR:
                return true;
            default:
            return false;
        }
    }
}