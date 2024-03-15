package bgu.spl.net.srv;
import java.util.HashMap;

class ConnectionsImpl<T> implements Connections<T> {
    private HashMap<Integer, ConnectionHandler<T>> ClientConnections;

    public ConnectionsImpl(){
        ClientConnections =new HashMap<>();
    }

    public void connect(int connectionId, ConnectionHandler<T> handler){
        ClientConnections.put(connectionId, handler);
    }

    public boolean send(int connectionId, T msg){
        if (!ClientConnections.containsKey(connectionId)) { // check if this connection exists
            return false;
        }
        ClientConnections.get(connectionId).send(msg); // send through this specific connection
        return true;
    }

    public void disconnect(int connectionId){
        ClientConnections.remove(connectionId);
    }
}