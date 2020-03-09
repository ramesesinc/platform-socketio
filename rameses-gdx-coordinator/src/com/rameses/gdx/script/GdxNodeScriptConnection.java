package com.rameses.gdx.script;

import com.rameses.osiris3.core.AbstractContext;
import com.rameses.osiris3.core.AppContext;
import com.rameses.osiris3.script.messaging.ScriptConnection;
import io.socket.client.Manager;
import java.util.Map;

public class GdxNodeScriptConnection extends ScriptConnection {
    private GdxNodeInvokerProxy proxy;
    private Map conf;
    
    public GdxNodeScriptConnection(String name, AbstractContext ctx, Map conf, Manager socketManager) {
        super(name, ctx, conf);
        this.proxy = new GdxNodeInvokerProxy((AppContext) ctx, conf, socketManager);
        this.conf = conf;
    }
    
    public void start()  {
        //starting connection
    }
    
    public void stop() {
        //do nothing
    }

    
    public Object create(String serviceName, Map env) throws Exception{
        return  create(serviceName, env, null);
    }
    
    public <T> T create(String serviceName, Map env, Class<T> localInterface) throws Exception{
        return (T) proxy.create( serviceName, env, localInterface );
    }

    public Map getConf() {
        return conf;
    }
}

