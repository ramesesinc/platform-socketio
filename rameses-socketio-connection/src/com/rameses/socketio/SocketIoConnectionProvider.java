package com.rameses.socketio;

import com.rameses.osiris3.xconnection.XConnection;
import com.rameses.osiris3.xconnection.XConnectionProvider;
import java.util.HashMap;
import java.util.Map;


public class SocketIoConnectionProvider extends XConnectionProvider {

    private final static String PROVIDER_NAME = "socketio";
    
    private Map<String, SocketIoConnectionPool> connections = new HashMap<String, SocketIoConnectionPool>();
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME; 
    }

    @Override
    public XConnection createConnection(String name, Map conf) { 
        String channel = getProperty("channel", conf);
        System.out.println("Creating connection for " + channel);
        SocketIoConnectionPool connection = connections.get(channel);
        if (connection == null) {
            connection = new SocketIoConnectionPool(conf, context, name);
            connections.put(channel, connection);
        }
        return connection;
    }
    
    private String getProperty( String name, Map map ) {
        Object o = (map == null? null: map.get(name)); 
        return ( o == null ? "/": o.toString()); 
    } 

}
