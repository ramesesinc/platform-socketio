package com.rameses.socketio;


public class API {
    
    private String host;
    private int port; 
    private String channel = "/";
    private boolean secured = false;
    
    public void setHost( String host ) {
        this.host = host; 
    }
    
    public void setPort( int port ) {
        this.port = port; 
    }
    
    public void setSecured(boolean secured) {
        this.secured = secured;
    }
    
    public String getUri(){
        StringBuilder sb = new StringBuilder(); 
        sb.append( secured ? "https" : "http").append("://"); 
        sb.append( host == null ? "localhost" : host ).append(":");
        sb.append( port <= 0 ? "3000" : (port+"")).append("/");
        return sb.toString(); 
    }

    public void setChannel(String channel) {
        if (channel != null) {
            this.channel = channel;
        }
    }
    
    public String getChannel() {
        return this.channel;
    }
    
}
