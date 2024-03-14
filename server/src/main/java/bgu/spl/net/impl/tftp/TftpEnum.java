package bgu.spl.net.impl.tftp;

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

public class TftpEnum {

    public static TFTPRequest create(){
        return TFTPRequest.NONE;
    }

    public static short decodeOperationEnum(TFTPRequest opCode) {
        switch (opCode) {
            case RRQ:
                return 1;
            case WRQ:
                return 2;
            case DATA:
                return 3;
            case ACK:
                return 4;
            case ERROR:
                return 5;
            case DIRQ:
                return 6;
            case LOGRQ:
                return 7;
            case DELRQ:
                return 8;
            case BCAST:
                return 9;
            case DISC:
                return 10;
            default:
                return -1;
        }
    }
    
    public static TFTPRequest decodeOperationCode(short opCode) {
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

    public static Boolean TFTPRequestEndsWithZero(TFTPRequest opEnum){
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
