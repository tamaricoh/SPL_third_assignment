package bgu.spl.net.impl.tftp;


// change alllllllllllllllllllllllllllllllllllllllllllllllll


public class TftpPacket {
    public static final short OPCODE_RRQ = 1;
      public static final short OPCODE_WRQ = 2;
      public static final short OPCODE_DATA = 3;
      public static final short OPCODE_ACK = 4;
      public static final short OPCODE_ERROR = 5;
      public static final short OPCODE_DIRQ = 6;
      public static final short OPCODE_LOGRQ = 7;
      public static final short OPCODE_DELRQ = 8;
      public static final short OPCODE_BCAST = 9;
      public static final short OPCODE_DISC = 10;
      
      public static final short ERROR_FILE_NOT_FOUND = 1;
      public static final short ERROR_FILE_ACCESS_VIOLATION = 2;
      public static final short ERROR_ILLEGAL_OP = 4;
      public static final short ERROR_FILE_ALREADY_EXISTS = 5;
      public static final short ERROR_USER_NOT_LOGGED_IN = 6;
      public static final short ERROR_USER_ALREADY_LOGGED_IN = 7;

      public static short OPCodeFromBytes(byte b1, byte b2) {
        return bytesToShort(b1,b2);
      }

      public static short bytesToShort(byte b1, byte b2)
      {
          return (short) (((short) b1 & 0xFF) << 8 | (short) (b2 & 0xFF));
      }

      public static byte[] shortToBytes(short s)
      {
          byte[] res = new byte[2];
          res[0] = (byte)((s >> 8) & 0xFF);
          res[1] = (byte)(s & 0xFF);
          return res;
      }

      public static byte[] ACKFor(short blockNumber) {
        byte[] res = new byte[4];

        byte[] opBytes = shortToBytes(OPCODE_ACK);
        byte[] blockBytes = shortToBytes(blockNumber);

        res[0] = opBytes[0];
        res[1] = opBytes[1];
        res[2] = blockBytes[0];
        res[3] = blockBytes[1];

        return res;
      }

      public static byte[] ERRORFor(short errCode, String msg) {
        byte[] msgBytes = msg.getBytes();
        byte[] res = new byte[4 + msgBytes.length + 1];

        byte[] opBytes = shortToBytes(OPCODE_ERROR);
        byte[] errCodeBytes = shortToBytes(errCode);
        res[0] = opBytes[0];
        res[1] = opBytes[1];
        res[2] = errCodeBytes[0];
        res[3] = errCodeBytes[1];

        for(int i = 0; i < msgBytes.length; i++) {
          res[i+4] = msgBytes[i];
        }

        res[res.length - 1] = '\0';

        return res;
      }

      public static byte[] BCASTFor(String filename, Boolean added) {
        byte[] filenameBytes = filename.getBytes();
        byte[] res = new byte[2 + 1 + filenameBytes.length + 1];

        byte[] opBytes = shortToBytes(OPCODE_BCAST);
        res[0] = opBytes[0];
        res[1] = opBytes[1];
        res[2] = 0;
        if (added) {
          res[2] = 1;
        }

        for(int i = 0; i < filenameBytes.length; i++) {
          res[i+3] = filenameBytes[i];
        }

        res[res.length - 1] = '\0';

        return res;
      }
}
