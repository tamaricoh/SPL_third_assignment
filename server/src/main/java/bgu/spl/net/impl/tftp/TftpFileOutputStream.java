package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.io.FileNotFoundException;

public class TftpFileOutputStream {

    private FileOutputStream fileStrem;

    public TftpFileOutputStream(String filePath) throws FileNotFoundException, FileAlreadyExistsException{
        File file = new File(filePath);
        if (file.exists()) {
            throw new FileAlreadyExistsException(filePath);
        }

        this.fileStrem = new FileOutputStream(file);
    }
}