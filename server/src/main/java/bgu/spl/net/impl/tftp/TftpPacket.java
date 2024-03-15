package bgu.spl.net.impl.tftp;

public class TftpPacket {
      public static final short RRQ = 1;
      public static final short WRQ = 2;
      public static final short DATA = 3;
      public static final short ACK = 4;
      public static final short ERROR = 5;
      public static final short DIRQ = 6;
      public static final short LOGRQ = 7;
      public static final short DELRQ = 8;
      public static final short BCAST = 9;
      public static final short DISC = 10;
      
      public static final short ERR_FileNotFound = 1;
      public static final short ERR_FileAccessViolation = 2;
      public static final short ERR_OP = 4;
      public static final short ERR_FileAlreadyExists = 5;
      public static final short ERR_UserNotLogged = 6;
      public static final short ERR_UserAlreadyLogged = 7;

      public static short OPCodeFromBytes(byte b1, byte b2) {
        return (short) (((short) b1 & 0xFF) << 8 | (short) (b2 & 0xFF));
      }

      // public static short bytesToShort(byte b1, byte b2)
      // {
      //     return (short) (((short) b1 & 0xFF) << 8 | (short) (b2 & 0xFF));
      // }

      public static byte[] shortToBytes(short s)
      {
          byte[] output = new byte[2];
          output[0] = (byte)((s >> 8) & 0xFF);
          output[1] = (byte)(s & 0xFF);
          return output;
      }

      public static byte[] ACK(short blockNumber) {
        byte[] output = new byte[4];

        byte[] opBytes = shortToBytes(ACK);
        output[0] = opBytes[0];
        output[1] = opBytes[1];

        byte[] blockBytes = shortToBytes(blockNumber);
        output[2] = blockBytes[0];
        output[3] = blockBytes[1];

        return output;
      }

      public static byte[] ERROR(short errCode, String message) {
        byte[] messageBytes = message.getBytes();
        byte[] output = new byte[4 + messageBytes.length + 1];

        byte[] opBytes = shortToBytes(ERROR);
        output[0] = opBytes[0];
        output[1] = opBytes[1];

        byte[] errCodeBytes = shortToBytes(errCode);
        output[2] = errCodeBytes[0];
        output[3] = errCodeBytes[1];

        // message
        for(int i = 0; i < messageBytes.length; i++) {
          output[i+4] = messageBytes[i];
        }

        output[output.length - 1] = '\0';

        return output;
      }

      public static byte[] BCAST(String filename, Boolean added) {
        byte[] fileNameBytes = filename.getBytes();
        byte[] output = new byte[2 + 1 + fileNameBytes.length + 1];

        byte[] opBytes = shortToBytes(BCAST);
        output[0] = opBytes[0];
        output[1] = opBytes[1];
        output[2] = 0;
        if (added) {
          output[2] = 1;
        }

        for(int i = 0; i < fileNameBytes.length; i++) {
          output[i+3] = fileNameBytes[i];
        }

        output[output.length - 1] = '\0';

        return output;
      }
}
