package bgu.spl.net.impl.tftp;
import bgu.spl.net.srv.Server;

public class TftpServer {
    public static void main (String[] args){
        // we will use the test command from 4.5 so make sure they work before submission with 127.0.0.1 as ip and 7777 as port.
        int port = 7777;
        if (args.length != 0)
            port = Integer.parseInt(args[0]);
        Server.threadPerClient(
                port,
                () -> new TftpProtocol(), //protocol factory
                TftpEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}
