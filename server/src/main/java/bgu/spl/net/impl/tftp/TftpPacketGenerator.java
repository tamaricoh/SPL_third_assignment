package bgu.spl.net.impl.tftp;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;



public class TftpPacketGenerator{

    Charset encode = StandardCharsets.UTF_8;
    public TftpPacketGenerator(){}
    

    private byte[] generateACk(byte[] blockNum){ 
        byte[] ACKpacket = new byte[4];
        ACKpacket[0] = 0;
        ACKpacket[1] = 4;
        fill()
        return ACKpacket;
    }

    private byte[] generateError(byte[] errorNum, byte[] Errormsg){       // Data, BCAST
        byte[] ERRORpacket = new byte[Errormsg.length + 5];
        ERRORpacket[0] = 0; ERRORpacket[1] = 5; ERRORpacket[2] = errorNum[0]; ERRORpacket[3] = errorNum[1];
        fill(ERRORpacket, Errormsg, 4, ERRORpacket.length-2); ERRORpacket[ERRORpacket.length-1] = 0;
        return ERRORpacket;
        
    }


    /**
     * @param added - is equel to1 if true 0 otherwish 
     */
    private byte[] generateBCAST(byte[] added, byte[] fileName){ 
        byte[] msg = fileName.getBytes(encode);
        byte[] BCASTpacket = new byte[msg.length + 4];
        BCASTpacket[0] = 0; BCASTpacket[1] = 9; BCASTpacket[2] = added;
        fill(BCASTpacket, msg, 3, BCASTpacket.length-2); BCASTpacket[BCASTpacket.length-1] = 0;
        return BCASTpacket;
        
    }

    private byte[] generateDATA(byte[] size,byte[] blockNum,  byte[] data){
        byte[] DATApacket = new byte[data.length + 6];
        DATApacket[0] = 0; DATApacket[1] = 3; DATApacket[2] = size[0]; DATApacket[3] = size[1];
        DATApacket[4] = blockNum[0]; DATApacket[5] = blockNum[1];
        fill(DATApacket, data, 6, DATApacket.length-1);
        return DATApacket;
        
    }

    private void fill(byte[] a, byte[] b, int start, int end){
        for(int i = 0; i<b.length & i+start <= end; i++){
            a[start+i] = b[i];
        }
    }


}