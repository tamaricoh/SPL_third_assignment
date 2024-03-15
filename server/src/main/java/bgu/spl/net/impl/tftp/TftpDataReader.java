package bgu.spl.net.impl.tftp;

import java.io.IOException;

public class TftpDataReader implements TftpReader {
    byte[] source;
    int currIndex;

    public TftpDataReader(byte[] source) {
        this.source = source;
        this.currIndex = 0;
    }

    public short read(byte[] dst) throws IOException {
        if (currIndex == source.length) {
            return -1;
        }

        short bytesRead = 0;
        while (bytesRead < dst.length && currIndex < source.length) {
            dst[bytesRead] = source[currIndex];
            bytesRead++;
            currIndex++;
        }
        return bytesRead;
    }
}
