package bgu.spl.net.srv;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;     

import java.io.IOException;                                // might wanna insert some expectitions?

class ServerConnections<T> implements Connections<T> {
    private HashMap<Integer, ConnectionHandler<T>> ClientConnections;

    public ServerConnections(){
        ClientConnections =new HashMap<>();
    }

    public void connect(int connectionId, ConnectionHandler<T> handler){    //Adds the client to the Map if it has a new id
        ClientConnections.put(connectionId, handler);
    }

    public boolean send(int connectionId, T msg){                           //sends a message to an existing client returns true if the massege request was sent
        System.out.println("Tamar: "+ "send packet");
        if (!ClientConnections.containsKey(connectionId)) {
            System.out.println("Tamar: "+ "false");
            return false;
        }
        ClientConnections.get(connectionId).send(msg);
        return true;
    }

    public void disconnect(int connectionId){                               //Removes the client from the ClientConnections
        ClientConnections.remove(connectionId);
    }

    // private ConnectionPair<Integer,ConnectionHandler<T>> findClient(int connectionId){
    //     for(ConnectionPair<Integer,ConnectionHandler<T>> pair: ClientConnections){
    //         if (pair.getFirst() == connectionId){
    //             return pair;
    //         }
    //     }
    //     return null;
    // }
}




// class ConnectionPair<T, U> {
//     private final T first;
//     private final U second;

//     public ConnectionPair(T first, U second) {
//         this.first = first;
//         this.second = second;
//     }

//     public T getFirst() {
//         return first;
//     }

//     public U getSecond() {
//         return second;
//     }
// }