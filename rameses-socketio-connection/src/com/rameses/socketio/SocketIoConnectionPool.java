package com.rameses.socketio;

import com.rameses.osiris3.core.AbstractContext;
import com.rameses.osiris3.xconnection.MessageConnection;
import com.rameses.osiris3.xconnection.MessageHandler;
import java.util.HashMap;
import java.util.Map;

public class SocketIoConnectionPool extends MessageConnection
{
    private Map conf;
    private Map appConf;
    private AbstractContext context;
    private String name;
    private boolean started;

    private API api;
    private SocketIoConnection connection;

    public SocketIoConnectionPool(Map conf, AbstractContext context, String name){
        this.started = false;
        this.name = name;
        this.conf = conf;
        this.context = context;
        
        appConf = new HashMap();
        appConf.putAll(conf);
        
        api = new API(); 
        api.setHost(getProperty("host")); 
        api.setChannel(getProperty("channel"));
        try {
            api.setPort(Integer.parseInt(getProperty("port"))); 
        } catch(Throwable t) {;}
    }
            
    @Override
    public void start() {
        System.out.println("SocketIoConnectionPool... starting");
        if ( started ) {
            return;
        }
        connection = new SocketIoConnection(name, context, conf);
        connection.setAPI(api);
        connection.start();
        
        started = true;
    }

    @Override
    public void stop() {
        if (started) {
            connection.stop();
        }
    }

    @Override
    public Map getConf() {
        return conf;
    }
    
    public void send(String event, Map data)  {
        if (started) {
            connection.send(event, data);
        }
    }

    @Override
    public void send(Object data) {
    }

    @Override
    public void sendText(String data) {
    }

    @Override
    public void send(Object data, String queueName) {
    }

    @Override
    public void addResponseHandler(String tokenid, MessageHandler handler) throws Exception {
    }
    
    private String getProperty( String name ) {
        return getProperty(name, conf); 
    } 
    private String getProperty( String name, Map map ) {
        Object o = (map == null? null: map.get(name)); 
        return ( o == null ? null: o.toString()); 
    } 
    
}
