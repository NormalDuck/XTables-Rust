package org.xtables;

/** Receives the values published to a channel this client subscribes to. */
@FunctionalInterface
public interface Updater {
    /** Called for each value, on the client's receive thread. */
    void update(Update update);
}
