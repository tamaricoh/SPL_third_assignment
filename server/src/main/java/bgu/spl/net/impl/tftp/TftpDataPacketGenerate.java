package bgu.spl.net.impl.tftp;
import java.io.IOException;

public class TftpDataPacketGenerate {
    private short blockNum;
    private TftpReader data;
    private int limitationCheck;

    public TftpDataPacketGenerate(TftpReader data){
        blockNum = 1;
        this.data = data;
    }

    public byte[] NextPacket() throws IOException {
        byte[] buf = new byte[TftpDataPacket.lim];
        int bytesRead = data.read(buf);

        if (bytesRead == -1) { // we couldnt read
            if (blockNum == 1 || limitationCheck == TftpDataPacket.lim) { 
                TftpDataPacket packet = new TftpDataPacket(blockNum);
                blockNum++;
                limitationCheck = 0;
                return packet.GetBasePacket();
            }
            return null;
        }

        TftpDataPacket packet = new TftpDataPacket(blockNum);
        blockNum++;

        for (int i = 0; i < bytesRead; i++) {
            packet.addByte(buf[i]);
        }
        limitationCheck = bytesRead;
        return packet.GetBasePacket();
    }
}
