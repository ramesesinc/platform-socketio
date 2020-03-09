package com.rameses.client.notification.socketio;

public interface NotificationHandler {
    public void onMessage(Object o);
    public String getId();
}
