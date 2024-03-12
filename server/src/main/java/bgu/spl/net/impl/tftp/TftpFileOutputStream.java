package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.io.FileNotFoundException;

public class TftpFileOutputStream {

    private FileOutputStream fileStream;

    public TftpFileOutputStream(String filePath) throws FileNotFoundException, FileAlreadyExistsException{
        File file = new File(filePath);
        if (file.exists()) {
            throw new FileAlreadyExistsException(filePath);         //this part needs to be sent to the client..
        }

        this.fileStream = new FileOutputStream(file);
    }

    public boolean writeToFile(byte[] data) throws IOException {
        try {
            fileStream.write(data);
            return true; // Write successful
        } catch (IOException e) {
            e.printStackTrace(); 
            return false; // Write failed
        }
    }
}
