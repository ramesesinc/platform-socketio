package com.rameses.gdx.script;

import com.rameses.gdx.util.Config;
import com.rameses.osiris3.xconnection.XConnection;
import com.rameses.osiris3.xconnection.XConnectionProvider;
import io.socket.client.Manager;
import java.net.URI;
import java.util.Map;


public class GdxNodeScriptConnectionProvider extends XConnectionProvider {

    private final static String PROVIDER_NAME = "node-script";
    private Manager manager;
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME; 
    }

    @Override
    public XConnection createConnection(String name, Map conf) { 
        if (manager == null) {
            Config config = new Config(conf);
            String uri = config.getUri();
            try {
                manager = new Manager(new URI(uri));
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return new GdxNodeScriptConnection(name, context, conf, manager);
    }

}
