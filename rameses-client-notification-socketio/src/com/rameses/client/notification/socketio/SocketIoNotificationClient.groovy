package com.rameses.client.notification.socketio;

import com.rameses.custom.impl.JsonUtil;
import com.rameses.rcp.common.*;
import com.rameses.rcp.annotations.*;
import com.rameses.osiris2.client.*;
import com.rameses.osiris2.common.*;
import com.rameses.rcp.framework.ClientContext;
import com.rameses.client.notification.*;
import io.socket.client.Manager;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.emitter.Emitter.Listener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import java.io.File;
import java.net.URL;
import java.net.URI;
import java.io.FileInputStream;

class SocketIoNotificationClient {
    public static String EVENT_NOTIFICATION = 'notification';
    public static String EVENT_QUEUE = 'queue';
    public static long DEFAULT_PORT = 5000;
    public static String DEFAULT_CHANNEL = '/';
    public static String ENV_HOST = 'socketio.host';
    public static String ENV_PORT = 'socketio.port';
    public static String ENV_CHANNEL = 'socketio.channel';
    

    private static SocketIoNotificationClient instance;
    private Socket socket;
    private def events;
    private Map conf;
    private File file;
    private def connected;
    private String EVENT = EVENT_NOTIFICATION;
    
    private SocketIoNotificationClient(Map conf) {
        this.events = [:];
        this.conf = conf;
    }
    
    private SocketIoNotificationClient(String filename) {
        this.events = [:];
        try {
            this.file = new File(filename);
        } catch(e) {
            e.printStackTrace();
        }
    }
    
    private SocketIoNotificationClient(File file) {
        this.events = [:];
        this.file = file;
    }
    
    public static SocketIoNotificationClient getInstance() {
        return getInstance([:]);
    }
    
    public static SocketIoNotificationClient getInstance(Map conf) {
        if (instance == null) {
            instance = new SocketIoNotificationClient(conf);
            instance.connect();
        }
        return instance;
    }
    
    public static SocketIoNotificationClient getInstance(String filename) {
        if (instance == null) {
            instance = new SocketIoNotificationClient(filename);
            instance.connect();
        }
        return instance;
    }
    
    public static SocketIoNotificationClient getInstance(File file) {
        if (instance == null) {
            instance = new SocketIoNotificationClient(file);
            instance.connect();
        }
        return instance;
    }
    
    public synchronized void register(event, NotificationHandler handler) {
        if (!events.containsKey(event)) {
            events[event] = [handler];
        } else {
            events[event] << handler;
        }
    }
    
    public synchronized void unregister(NotificationHandler handler) {
        events.each{k, handlers ->
            def list = handlers.findAll{ it.id == handler.id}
            if (list) {
                handlers.removeAll(list);
            }
        }
        
        println 'unregister handler: ' + handler.id;
    }
    
    def connectHandler = [
        call : { 
            System.out.println("ETRACS Client connected to socket.io");
        }
    ] as Emitter.Listener;
    
    def disconnectHandler = [
        call : { 
            System.out.println("ETRACS Client connected to socket.io");
        }
    ] as Emitter.Listener;
        
    
    public void connect() {
        try {
            connected = false;
            def env = ClientContext.currentContext.appEnv;
            String uri = getUri(env);
            String channel = getChannel(env);
            if (channel == DEFAULT_CHANNEL) {
                EVENT = EVENT_NOTIFICATION;
                socket = IO.socket(uri);
            } else {
                EVENT = EVENT_QUEUE;
                def manager = new Manager(new URI(uri));
                socket = manager.socket("/"+channel);
            }
            socket.connect();
            connected = true;
            registerHandler();
            println 'SocketIO Notification started...'; 
        } catch (e) {
            println 'SocketIO {ERROR] ' + e.message;
            e.printStackTrace();
        }
    }
    
    public void send(data) {
        send(EVENT, data);
    }
    
    public void send(event, data) {
        if (!connected) {
            println 'SocketIO [ERROR] Connection was not established.'
            return;
        }
        
        try {
            def param = ['_event_': event, data: data]
            socket.emit(EVENT, param);
        } catch ( e ) {
            println('SocketIo [ERROR] ' + e.message);
        }
    }
    
    public void receive(param) {
        def event = param._event_;
        def data = param.data;
        def handlers = events[event];
        new Thread(new NotifyHandler(handlers, data)).start();
    }
    
    protected Object getData(args) {
        return getData(args, null);
    }
    
    protected Object getData(args, key) {
        if (args.length == 0) {
            return null;
        }
        
        if (args[0] instanceof JSONObject) {
            try {
                JSONObject json = (JSONObject) args[0];
                String str = json.toString();
                Map data = (Map) JsonUtil.toMap(str);
                if (key == null) {
                    return data;
                }
                return data.get(key);
            } catch (Exception ex) {
                System.out.println("Error extracting value for " + key);
                return null;
            }
        } else {
            return args[0];
        }
    }
    
    private void registerHandler() {
        if (!connected) {
            return;
        }
        
        def listener = [
            call : {args ->
                receive(getData(args));
            }
        ] as Emitter.Listener;
        socket.on(EVENT, listener);
    }
 
    private String getUri(Map env) {
        // option 1. instance conf
        def host = (conf && conf.host ? conf.host : null);
        def port = (conf && conf.port ? conf.port : DEFAULT_PORT);
        
        // option 2. socketio.conf file 
        def socketio = getSocketIoConf();
        if (socketio) {
            host = socketio.host;
            port = (socketio.port ? socketio.port : DEFAULT_PORT);
        }
        
        // option 3. updates.xml
        def envhost = env.get(ENV_HOST);
        if (envhost) {
            host = envhost;
        }
        
        def envport = env.get(ENV_PORT);
        if (envport) {
            port = envport;
        }
        
        String uri = "http://" + host;
        if (host.indexOf(":") < 0) {
            uri += ":" + port;
        }
        println "uri => " + uri;
        return uri;
    }
    
    private String getChannel(Map env) {
        def socketio = getSocketIoConf();
        def channel = DEFAULT_CHANNEL;
        if (socketio?.channel) {
            channel = socketio.channel;
        }
        def envchannel = env.get(ENV_CHANNEL);
        if (envchannel) {
            channel = envchannel;
        }
        return channel;
    }
    
    private def getSocketIoConf() {
        def conf = null;
        if (file) {
            try {
                conf = [:];
                def p = new Properties();
                def fis = new FileInputStream(file);
                p.load(fis);
                conf.host = p.getProperty('host');
                conf.port = p.getProperty('port');
                conf.channel = p.getProperty('channel');
            }
            catch (ex) {
                println 'SocketIO [ERROR] ' + ex.message;
            }
        }
        return conf;
    }
}

private class NotifyHandler implements Runnable {
    def handlers;
    def data;

    NotifyHandler(handlers, data) {
        this.handlers = handlers;
        this.data = data;
    }

    public void run() {
        if (handlers == null) {
            return;
        }

        for (int i = 0; i < handlers.size(); i++) {
            def handler = handlers.get(i);
            if (handler == null) {
                continue;
            }
            try {
                handler.onMessage(data);
            } catch (Throwable t) {
                t.printStackTrace();
                System.out.println("handler error onMessage caused by " + t.getMessage());
            }
        }
    }
}