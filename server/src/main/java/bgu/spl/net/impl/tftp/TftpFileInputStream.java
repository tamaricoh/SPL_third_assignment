package bgu.spl.net.impl.tftp;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TftpFileInputStream {

    private FileInputStream filePath;

    public TftpFileInputStream(String filePath) throws FileNotFoundException{
        try {
            this.filePath = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw e;
        }
    }

    public FileInputStream getFile(){
        return filePath;
    }
    
}
