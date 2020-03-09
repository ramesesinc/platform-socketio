package com.rameses.client.notification.socketio;

public class DefaultNotificationHandler implements NotificationHandler {
    private String id;

    public DefaultNotificationHandler() {
        id = "ID" + new java.rmi.server.UID();
    }
    
    public void onMessage(Object o) {
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id: " + id;
    }
    
    
}
