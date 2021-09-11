package com.ubicron;

/** Copyright 2021 David Corbo
 *  Daemon definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

public class State {

    private boolean daemonActive;
    private boolean listenerActive;
    private Daemon daemon;
    private Listener listener;

    public State() {
        this.daemonActive = true;
        this.listenerActive = true;
    }

    public synchronized void shutdownDaemon() {
        System.out.println("Shutting down daemon");
        this.daemonActive = false;
    }

    public synchronized void shutdownListener() {
        this.listenerActive = false;
    }

    public synchronized void addDaemon(Daemon daemon) {
        this.daemon = daemon;
    }

    public synchronized void addListener(Listener listener) {
        this.listener = listener;
    }

    public synchronized boolean daemonActive() {
        return this.daemonActive;
    }
    
    public synchronized boolean listenerActive() {
        return this.listenerActive;
    }
}