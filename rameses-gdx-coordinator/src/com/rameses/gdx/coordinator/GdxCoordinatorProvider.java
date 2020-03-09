package com.rameses.gdx.coordinator;

import com.rameses.gdx.util.Config;
import com.rameses.osiris3.xconnection.XConnection;
import com.rameses.osiris3.xconnection.XConnectionProvider;
import java.util.HashMap;
import java.util.Map;


public class GdxCoordinatorProvider extends XConnectionProvider {

    private final static String PROVIDER_NAME = "coordinator";
    private Map<String, GdxCoordinator> sockets = new HashMap<String, GdxCoordinator>();
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME; 
    }

    @Override
    public XConnection createConnection(String name, Map conf) { 
        Config config = new Config(conf);
        GdxCoordinator socket = sockets.get(config.getChannel());
        if ( socket == null) {
            socket = new GdxCoordinator(name, context, conf);
            sockets.put(config.getChannel(), socket);
        }
        return socket;
    }

}
