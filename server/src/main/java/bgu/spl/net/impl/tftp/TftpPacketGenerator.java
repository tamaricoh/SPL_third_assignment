package bgu.spl.net.impl.tftp;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;



public class TftpPacketGenerator{

    Charset encode = StandardCharsets.UTF_8;
    public TftpPacketGenerator(){}
    

    public byte[] generateACk(byte[] blockNum){ 
        byte[] ACKpacket = new byte[4];
        ACKpacket[0] = 0; ACKpacket[1] = 4;
        fill(ACKpacket, blockNum,2,3);
        return ACKpacket;
    }

    public byte[] generateError(byte[] errorNum, byte[] Errormsg){       // Data, BCAST
        byte[] ERRORpacket = new byte[Errormsg.length + 5];
        ERRORpacket[0] = 0; ERRORpacket[1] = 5;
        fill(ERRORpacket, errorNum,2,3);
        fill(ERRORpacket, Errormsg, 4, ERRORpacket.length-2);
         ERRORpacket[ERRORpacket.length-1] = 0;
        return ERRORpacket;
        
    }


    /**
     * @param added - is equel to 1 if true, or 0 otherwish 
     */
    public byte[] generateBCAST(byte[] added, byte[] fileName){ 
        byte[] BCASTpacket = new byte[fileName.length + 4];
        BCASTpacket[0] = 0; BCASTpacket[1] = 9;
        BCASTpacket[2] = added[0];
        fill(BCASTpacket, fileName, 3, BCASTpacket.length-2);
         BCASTpacket[BCASTpacket.length-1] = 0;
        return BCASTpacket;
        
    }

    public byte[] generateDATA(byte[] size,byte[] blockNum,  byte[] data){
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