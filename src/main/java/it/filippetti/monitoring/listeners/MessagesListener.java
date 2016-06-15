package it.filippetti.monitoring.listeners;

/**
 * Created by Francesco on 14/06/2016.
 */
public interface MessagesListener {
    // Timestamp is in milliseconds
    void messageReceived(long timestamp);
}
