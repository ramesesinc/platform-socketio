/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.gdx.script;

import com.rameses.osiris3.core.AppContext;
import com.rameses.service.ScriptServiceContext;
import com.rameses.service.ServiceProxy;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import io.socket.client.Manager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GdxNodeInvokerProxy {

    private AppContext context;
    private Map conf;
    private Map<String, Class> scripts = Collections.synchronizedMap(new HashMap());
    private GroovyClassLoader classLoader;
    private boolean failOnConnectionError;
    private Manager socketManager;
    
    public GdxNodeInvokerProxy(AppContext ctx, Map conf, Manager socketManager) {
        this.conf = conf;
        this.context = ctx;
        this.classLoader = new GroovyClassLoader(ctx.getClassLoader());
        this.failOnConnectionError = true;
        this.socketManager = socketManager;

        Object val = (conf == null ? null : conf.get("failOnConnectionError"));
        if ("false".equals(val + "")) {
            this.failOnConnectionError = false;
        }
    }

    public Object create(String serviceName, Map env) throws Exception {
        return create(serviceName, env, null);
    }

    public Object create(String serviceName, Map env, Class localInterface) throws Exception {
        ScriptServiceContext ect = new ScriptServiceContext(conf);
        if (!scripts.containsKey(serviceName)) {
            try {
                StringBuilder builder = new StringBuilder();
                builder.append("public class MyMetaClass  { \n");
                builder.append("    def invoker; \n");
                builder.append("    public Object invokeMethod(String string, Object args) { \n");
                builder.append("        return invoker.invokeMethod(string, args); \n");
                builder.append("    } \n");
                builder.append(" } ");
                Class metaClass = classLoader.parseClass(builder.toString());
                scripts.put(serviceName, metaClass);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        ServiceProxy sp = ect.create(serviceName, env);
        GdxNodeProxyInvocationHandler pih = new GdxNodeProxyInvocationHandler(sp, serviceName, conf, socketManager);

        Object obj = scripts.get(serviceName).newInstance();
        ((GroovyObject) obj).setProperty("invoker", pih);
        return obj;
    }
}
