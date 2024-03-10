package bgu.spl.net.srv;
import java.util.concurrent.ConcurrentLinkedQueue;        // maybe we should use a concurrent type of list?\

import java.io.IOException;

class ServerConnections<T> implements Connections<T> {
    private ConcurrentLinkedQueue<ConnectionPair<Integer,ConnectionHandler<T>>> ClientConnections;

    public ServerConnections(){
        ClientConnections = new ConcurrentLinkedQueue<ConnectionPair<Integer,ConnectionHandler<T>>>();

    }

    public void connect(int connectionId, ConnectionHandler<T> handler){    //Adds the client to the Map if it has a new id
        ConnectionPair<Integer,ConnectionHandler<T>> clientPair = new ConnectionPair(connectionId, handler);
        if(findClient(connectionId) == null){
            ClientConnections.add(clientPair);
        }
    }

    public boolean send(int connectionId, T msg){                           //sends a message to an existing client returns true if the massege request was sent
        ConnectionPair<Integer,ConnectionHandler<T>> client = findClient(connectionId);
        Boolean send = (client != null);
        if(send){
            client.getSecond().send(msg);
        }
        return send;
    }

    public void disconnect(int connectionId){                               //Removes the client from the ClientConnections
        ConnectionPair<Integer,ConnectionHandler<T>> client = findClient(connectionId);
        if(client != null){
            ClientConnections.remove(client);
        }
    }

    private ConnectionPair<Integer,ConnectionHandler<T>> findClient(int connectionId){
        for(ConnectionPair<Integer,ConnectionHandler<T>> pair: ClientConnections){
            if (pair.getFirst() == connectionId){
                return pair;
            }
        }
        return null;
    }
}




class ConnectionPair<T, U> {
    private final T first;
    private final U second;

    public ConnectionPair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}