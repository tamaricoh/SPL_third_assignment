package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.io.FileNotFoundException;

public class TftpFileOutputStream {

    private FileOutputStream fileStream;
    private String filePath;

    public TftpFileOutputStream(String filePath) throws FileNotFoundException, FileAlreadyExistsException{
        File file = new File(filePath);
        if (file.exists()) {
            throw new FileAlreadyExistsException(filePath);         //this part needs to be sent to the client..
        }
        this.filePath = filePath;
        this.fileStream = new FileOutputStream(file);
    }

    public boolean writeToFile(byte[] data) throws IOException {
        fileStream.write(data);

        if (data.length == 512){
            return false; // writen. but theres more data to write
        }

        fileStream.close();

        // check for ERROR
        File fileCheck = new File(filePath);
        if (!fileCheck.createNewFile()){
            throw new IOException("file in this name does not exists");
        }
        return true;
    }
}
