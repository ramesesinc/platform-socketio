package com.rameses.gdx.coordinator;

import com.rameses.osiris3.core.AbstractContext;
import com.rameses.osiris3.server.JsonUtil;
import com.rameses.service.ScriptServiceContext;
import com.rameses.service.ServiceProxy;
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
        Map args = (Map) get(params, "args", new HashMap());

        Map appConf = new HashMap();
        appConf.put("app.host", get(conf, "app.host", "localhost:8570"));
        appConf.put("app.cluster", get(conf, "app.cluster", "osiris3"));
        appConf.put("app.context", get(conf, "app.context", "gdx"));
        appConf.put("readTimeout", get(conf, "timeout", "60000"));
        appConf.put("debug", get(conf, "debug", false));

        System.out.println("Config =========================");
        for (Object key: appConf.keySet()){
            Object value = appConf.get(key);
            System.out.println(key + " " + value);  
        } 
        
        try {
            ScriptServiceContext ctx = new ScriptServiceContext(appConf);
            ServiceProxy svc = (ServiceProxy) ctx.create("remote/" + serviceName);
            Object result = svc.invoke(methodName, new Object[]{args});
            Map res = new HashMap();
            res.put("status", "OK");
            res.put("data", toJson(result));
            return res;
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
}
