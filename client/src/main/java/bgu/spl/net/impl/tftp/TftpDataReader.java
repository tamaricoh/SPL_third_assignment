package bgu.spl.net.impl.tftp;

import java.io.IOException;

public class TftpDataReader implements TftpReader {
    byte[] data;
    int indexPlace;

    public TftpDataReader(byte[] data) {
        this.data = data;
        this.indexPlace = 0; // beggining
    }

    public short read(byte[] d) throws IOException {
        if (indexPlace == data.length) {
            return -1; // end reading
        }
        short bytesRead = 0;
        while (bytesRead < d.length && indexPlace < data.length) {
            d[bytesRead] = data[indexPlace];
            bytesRead++;
            indexPlace++;
        }
        return bytesRead;
    }
}
