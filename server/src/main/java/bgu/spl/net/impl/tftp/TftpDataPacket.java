package bgu.spl.net.impl.tftp;

// change alllllllllllllllllllllllllllllllllllllllllllllllll


public class TftpDataPacket{

    public static final int lim = 512;
    private byte[] packet;
    private short dataAdded;

    public TftpDataPacket(short blockNum){
        packet =  new byte[1 << 10];
        byte[] opBytes = enumToBytes(TFTPRequest.DATA);
        byte[] sizeBytes = shortToBytes((short)0);
        byte[] blockNumBytes = shortToBytes(blockNum);
        packet[0] = opBytes[0];
        packet[1] = opBytes[1];
        packet[2] = sizeBytes[0];
        packet[3] = sizeBytes[1];
        packet[4] = blockNumBytes[0];
        packet[5] = blockNumBytes[1];
        dataAdded = 0;
    }

    private byte[] enumToBytes(TFTPRequest opCode){
        short e = TftpEnum.decodeOperationEnum(opCode);
        byte[] output = shortToBytes(e);
        return output;
    }

    private byte[] shortToBytes(short code){
        byte[] output = new byte[2];
        output[0] = (byte)((code >> 8) & 0xFF);
        output[1] = (byte)(code & 0xFF);
        return output;
    }

    private void setPacketSize() {
        byte[] sizeBytes = shortToBytes(dataAdded);
        packet[2] = sizeBytes[0];
        packet[3] = sizeBytes[1];
    }

    public Boolean addByte(byte b) {
        packet[dataAdded+6] = b;
        dataAdded++;
        setPacketSize();
        return dataAdded == lim;
    }

    public Boolean IsEmpty() {
        return dataAdded == 0;
    }

    public byte[] GetRawPacket() {
        byte[] output = new byte[6+dataAdded];
        for(int i = 0; i< 6+dataAdded; i++) {
            output[i] = packet[i];
        }

        return output;
    }
}
