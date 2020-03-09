package com.rameses.socketio;


import com.rameses.osiris3.core.AbstractContext;
import com.rameses.osiris3.xconnection.MessageConnection;
import com.rameses.osiris3.xconnection.MessageHandler;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketIoConnection extends MessageConnection {
    private final String EVENT_NOTIFICATION = "notification";
    private final String EVENT_QUEUE = "queue";
    private String name;
    private AbstractContext context;     
    private Map conf; 
    private boolean enabled = false;
    private boolean started = false;
    private Socket socket;
    private List<String> events;
    private String EVENT = EVENT_NOTIFICATION;
    
    private API api;

    public SocketIoConnection(String name, AbstractContext context, Map conf) {
        this.name = name;
        this.context = context;
        this.conf = conf; 
        enabled = ("false".equals(getProperty("enabled")+"") ? false : true);
        events = new ArrayList<String>();
    }
    
    private String getProperty( String name ) {
        return getProperty(name, conf); 
    }
    private String getProperty( String name, Map map ) {
        Object o = (map == null? null: map.get(name)); 
        return ( o == null ? null: o.toString()); 
    }
     
    public final boolean isEnabled() {
        return this.enabled; 
    }
    
    @Override
    public Map getConf() { 
        return conf; 
    }
    
    public void setAPI( API api ) {
        this.api = api; 
    }

    @Override
    public void start() { 
        if ( started ) {
            return;
        } 
        
        try {
            System.out.println(api.getUri());
            String channel = api.getChannel();
            if (channel == "/") {
                EVENT = EVENT_NOTIFICATION;
                socket = IO.socket(api.getUri());
            } else {
                EVENT = EVENT_QUEUE;
                Manager  manager = new Manager(new URI(api.getUri()));
                socket = manager.socket("/"+channel);
            }
            socket.connect();
            started = true;
            System.out.println( name +": connected");
        } catch(Throwable ex) {
            System.out.println( name +": Socket.io Connection not started caused by "+ ex.getMessage());
            ex.printStackTrace();  
        }
    } 
    
    public void send(String event, Map data)  {
        if (started) {
            try {
                if (!events.contains(event)) {
                    events.add(event);
                }
                Map params = new HashMap();
                params.put("_event_", event);
                params.put("data", data);
                socket.emit(EVENT, params);
            } catch( Exception ex) {
                System.out.println("SocketIo [ERROR] " + ex.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        System.out.println( name +" : Stopping Socket.io Connection" );
        if (started) {
            socket.disconnect();
        }
        super.stop();
    }
    
    /**************************************************************************
    * This is used for handling direct or P2P responses. The queue to create
    * will be a temporary queue.
    ***************************************************************************/ 
    @Override
    public void addResponseHandler(String tokenid, MessageHandler handler) throws Exception{
             
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
}
