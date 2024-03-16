package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;

public class TftpDataWriter {
    private OutputStream fstream;
    private String filename;

    public TftpDataWriter(String dir, String filename)
            throws FileAlreadyExistsException, FileNotFoundException, IOException {
        File f = new File(dir + filename);
        if (!f.exists()) {
            throw new FileNotFoundException(dir + filename);
        }

        fstream = new FileOutputStream(f);
        this.filename = filename;
    }

    public TftpDataWriter(OutputStream stream) throws FileAlreadyExistsException, FileNotFoundException, IOException {
        fstream = stream;
    }

    // Returns true when we finished writing to the file.
    public Boolean Write(byte[] b) throws IOException {
        Boolean fullPacket = b.length == TftpDataPacket.lim;
        fstream.write(b);
        if (fullPacket) {
            return false;
        }

        fstream.close();

        return true;
    }

    public String GetFileName() {
        return filename;
    }

    public void Revert() {
        try {
            fstream.close();
        } catch (IOException e) {
        }
    }
}