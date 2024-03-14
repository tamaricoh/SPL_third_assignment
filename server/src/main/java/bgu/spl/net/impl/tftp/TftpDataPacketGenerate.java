package bgu.spl.net.impl.tftp;

// change alllllllllllllllllllllllllllllllllllllllllllllllll


import java.io.IOException;
// import bgu.spl.net.impl.tftp.TftpReader;

public class TftpDataPacketGenerate {
    private short currBlockNum;
    private TftpFileInputStream data;
    private int lastReadAmount;

    public TftpDataPacketGenerate(TftpFileInputStream data){
        currBlockNum = 1;
        this.data = data;
    }

    public byte[] NextPacket() throws IOException {
        byte[] buf = new byte[TftpDataPacket.lim];
        int bytesRead = data.read(buf);

        if (bytesRead == -1) {
            if (currBlockNum == 1 || lastReadAmount == TftpDataPacket.lim) {
                // In the case where the source was empty from the start OR the source was a multiple of MAX_DATA_SECTION_SIZE, we want to return an empty packet to mark the end.
                TftpDataPacket packet = new TftpDataPacket(currBlockNum);
                currBlockNum++;
                lastReadAmount = 0;
                return packet.GetRawPacket();
            }

            return null;
        }

        TftpDataPacket packet = new TftpDataPacket(currBlockNum);
        currBlockNum++;

        for (int i = 0; i < bytesRead; i++) {
            packet.addByte(buf[i]);
        }
        lastReadAmount = bytesRead;

        return packet.GetRawPacket();
    }
}
