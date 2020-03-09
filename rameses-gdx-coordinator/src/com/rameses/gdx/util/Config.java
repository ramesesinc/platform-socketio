package com.rameses.gdx.util;

import java.util.Map;


public class Config {
    public final int DEFAULT_TIMEOUT = 60000;
    
    private String host;
    private String channel;
    private boolean debug = false;
    private boolean secured = false;
    private Map conf;
    
    public Config(Map conf) {
        this.conf = conf;
        host = getProperty("host");
        channel = getProperty("channel");
        debug = toBool(getProperty("debug"));
    }
    
    public Map getConf() {
        return conf;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    public boolean isSecured() {
        return secured;
    }
    
    public String getUri(){
        StringBuilder sb = new StringBuilder(); 
        sb.append( secured ? "https" : "http").append("://"); 
        sb.append( host == null ? "localhost" : host );
        sb.append("/");
        return sb.toString(); 
    }
    
    private String getProperty( String name ) {
        return getProperty(name, conf); 
    } 
    
    private String getProperty( String name, Map map ) {
        Object o = (map == null? null: map.get(name)); 
        return ( o == null ? null: o.toString()); 
    } 

    private boolean toBool(String val) {
       if (val == null) {
           return false;
       }
       return val.matches("1|y|yes|t|true");
    }
    
}
