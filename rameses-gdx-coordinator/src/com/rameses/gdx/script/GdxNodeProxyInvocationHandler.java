/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.gdx.script;

import com.rameses.gdx.util.Config;
import com.rameses.gdx.util.DataUtil;
import com.rameses.service.ServiceProxy;
import com.rameses.util.AppException;
import io.socket.client.Ack;
import io.socket.client.Manager;
import io.socket.client.Socket;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GdxNodeProxyInvocationHandler implements InvocationHandler {

    private ServiceProxy proxy;
    private boolean failOnConnectionError;
    private final String EVENT_INVOKE = "invoke";
    private Socket socket;
    private Config config;
    private Map conf;
    private String channel;
    private String serviceName;
    private Object data;
    private Manager socketManager;

    public GdxNodeProxyInvocationHandler(ServiceProxy proxy, String remoteServiceName, Map conf, Manager socketManager) {
        this.proxy = proxy;
        this.conf = conf;
        this.config = new Config(conf);
        this.failOnConnectionError = true;
        this.socketManager = socketManager;

        String[] tokens = remoteServiceName.split(":");
        this.channel = tokens[0];
        this.serviceName = tokens[1];
        initNodeConnection();
    }

    void initNodeConnection() {
        try {
            socket = socketManager.socket("/" + config.getChannel());
            socket.connect();
        } catch (Throwable ex) {
            System.out.println("GdxNodeInvocation Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //do not use invokeMethod because it is a reserved word in groovy
    public Object invokeMethod(String methodName, Object[] args) throws Throwable {
        Map params = new HashMap();
        params.put("channel", this.channel);
        params.put("service", this.serviceName);
        params.put("method", methodName);
        if (args.length > 0) {
            Object data = ((Map)args[0]).remove("data");
            Map remoteConfig = (Map)((Map)args[0]).remove("remoteConfig");
            params.put("args", data);
            params.put("module", remoteConfig.get("module"));
            params.put("connection", remoteConfig.get("connection"));
        }

        try {
            final LinkedBlockingQueue queue = new LinkedBlockingQueue();

            socket.emit(EVENT_INVOKE, params, new Ack() {
                @Override
                public void call(Object... val) {
                    try {
                        queue.put(DataUtil.getData(val));
                    } catch (InterruptedException ex) {
                        //
                    }
                }
            });

            Object data = null;
            try {
                data = queue.poll(60, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new Exception("Request timeout.");
            }
            if (data instanceof Map) {
                Map map = (Map) data;
                if ("OK".equalsIgnoreCase(map.get("status").toString())) {
                    return map.get("data");
                } else {
                    throw new Exception(map.get("msg").toString());
                }
            } else {
                return data;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            if (isConnectionError(t) && !failOnConnectionError) {
                return null;
            } else if (t instanceof AppException) {
                throw t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getMessage(), t);
            }
        }
    }

    

    public Object invoke(Object sproxy, Method method, Object[] args) throws Throwable {
        return invokeMethod(method.getName(), args);
    }

    private boolean isConnectionError(Throwable e) {
        if (e instanceof java.net.ConnectException) {
            return true;
        } else if (e instanceof java.net.SocketException) {
            return true;
        } else if (e instanceof java.net.SocketTimeoutException) {
            return true;
        } else if (e instanceof java.net.UnknownHostException) {
            return true;
        } else if (e instanceof java.net.MalformedURLException) {
            return true;
        } else if (e instanceof java.net.ProtocolException) {
            return true;
        } else if (e instanceof java.net.UnknownServiceException) {
            return true;
        } else {
            return false;
        }
    }
}
