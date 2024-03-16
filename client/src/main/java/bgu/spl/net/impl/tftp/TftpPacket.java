package bgu.spl.net.impl.tftp;

import java.nio.ByteBuffer;
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

      public static byte[] LOGRQ(String username) {
        return stringCmd(OPCODE_LOGRQ, username);
      }

      public static byte[] DELRQ(String filename) {
        return stringCmd(OPCODE_DELRQ, filename);
      }

      public static byte[] DIRQ() {
        return noArgsCmd(OPCODE_DIRQ);
      }

      public static byte[] DISC() {
        return noArgsCmd(OPCODE_DISC);
      }

      public static byte[] RRQ(String filename) {
        return stringCmd(OPCODE_RRQ, filename);
      }

      public static byte[] WRQ(String filename) {
        return stringCmd(OPCODE_WRQ, filename);
      }

      public static byte[] ACK(short blockNumber){
        ByteBuffer buf = bufferOfSize(2 + 2);
        buf.put(shortToBytes(OPCODE_ACK));
        buf.put(shortToBytes(blockNumber));
        return buf.array();
      }

      private static byte[] stringCmd(short opcode, String arg){
        byte[] argBytes = arg.getBytes();
        ByteBuffer buf = bufferOfSize(2 + argBytes.length + 1);
        buf.put(shortToBytes(opcode));
        buf.put(argBytes);
        buf.put((byte) '\0');
        return buf.array();
      }

      private static byte[] noArgsCmd(short opcode){
        return shortToBytes(opcode);
      }

      private static ByteBuffer bufferOfSize(int size){
        return ByteBuffer.wrap(new byte[size]);
      }
}