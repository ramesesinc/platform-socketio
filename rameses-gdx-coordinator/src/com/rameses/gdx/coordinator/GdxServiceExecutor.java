package com.rameses.gdx.coordinator;

import com.rameses.osiris3.core.AbstractContext;
import com.rameses.osiris3.script.messaging.ScriptConnection;
import com.rameses.osiris3.server.JsonUtil;
import com.rameses.osiris3.xconnection.XConnection;
import com.rameses.service.ScriptServiceContext;
import com.rameses.service.ServiceProxy;
import groovy.lang.GroovyObject;
import java.util.HashMap;
import java.util.Map;

public class GdxServiceExecutor {

    private AbstractContext context;
    private Map conf;

    public GdxServiceExecutor(AbstractContext context, Map conf) {
        this.context = context;
        this.conf = conf;
    }

    public Object process(Map params) {
        String serviceName = get(params, "service", "").toString();
        String methodName = get(params, "method", "").toString();
        String moduleName = get(params, "module", "").toString();
        String connectionName = get(params, "connection", "default").toString();
        Map args = (Map) get(params, "args", new HashMap());

        Map remoteConfig = (Map)get(args, "remoteConfig", null);
        if (remoteConfig != null) {
            moduleName = remoteConfig.get("module").toString();
            connectionName = remoteConfig.get("connection").toString();
            args = (Map) get(args, "data", null);
        }
        
        System.out.println("Config =========================");
        System.out.println("Service Name:" + serviceName);
        System.out.println("Method Name:" + methodName);
        System.out.println("Module Name:" + moduleName);
        System.out.println("Connection Name:" + connectionName);
        
        try {
            XConnection xc = context.getConnection(connectionName);
            if (xc instanceof ScriptConnection) {
                Object obj = ((ScriptConnection)xc).create("gdx/" + serviceName, new HashMap());
                Object result = ((GroovyObject)obj).invokeMethod(methodName, new Object[]{args});
                Map res = new HashMap();
                res.put("status", "OK");
                res.put("data", toJson(result));
                return res;
            } else {
                throw new Exception("Connection provider not supported." );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Map err = new HashMap();
            err.put("status", "ERROR");
            err.put("msg", ex.getMessage());
            return err;
        }
    }

    private boolean toBool(Object secured, boolean defValue) {
        if (secured == null || secured.toString().trim().length() == 0) {
            return defValue;
        }
        return secured.toString().matches("y|yes|1|t|true");
    }

    private Object get(Map params, String key, Object defValue) {
        Object val = params.get(key);
        if (val == null) {
            return defValue;
        }
        return val;
    }

    private String toJson(Object result) {
        String jsonData = "";
        if (result != null) {
            jsonData = JsonUtil.toString(result);
        }
        return jsonData;
    }

    private void printData(Map params) {
        System.out.println("process (params) ======================== ");
        for (Object key : params.keySet()) {
            System.out.println("  ** " + key + ": " + params.get(key));
        }
        System.out.println("==========================================");
    }
}
