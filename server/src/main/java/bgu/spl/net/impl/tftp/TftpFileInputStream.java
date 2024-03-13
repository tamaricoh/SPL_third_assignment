package bgu.spl.net.impl.tftp;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TftpFileInputStream {

    private FileInputStream filePath;
    private ByteArrayOutputStream b;

    public TftpFileInputStream(String filePath) throws FileNotFoundException{
        try {
            this.filePath = new FileInputStream(filePath);
            this.b = new ByteArrayOutputStream();
        } catch (FileNotFoundException e) {
            throw e;
        }
    }

    public FileInputStream getFile(){
        return filePath;
    }

    public byte[] read(byte[] data) throws IOException{
        byte[] buffer = new byte[506];
        int bytesRead;
        while ((bytesRead = filePath.read(buffer)) != -1) {
            b.write(buffer, 0, bytesRead);
        }
        return b.toByteArray();
    }
    
}
