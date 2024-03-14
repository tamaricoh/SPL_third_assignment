package bgu.spl.net.impl.tftp;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TftpFileInputStream{

    private FileInputStream stream;

    public TftpFileInputStream(String filePath) throws FileNotFoundException{
        try {
            this.stream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw e;
        }
    }


    public short read(byte[] data){
        short size = 0;
        try {
            size = (short) stream.read(data,0,512);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return size;
    }
    
}
