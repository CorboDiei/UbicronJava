package com.ubicron;

/** Copyright 2021 David Corbo
 *  Daemon definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class RunState {

    private boolean daemonActive;
    private boolean listenerActive;
    private Thread daemon;
    private Listener listener;
    private ExecutorService threadPool;

    public RunState() {
        this.daemonActive = true;
        this.listenerActive = true;
    }

    public void shutdown() {
        shutdownDaemon();
        shutdownListener();

        try {
            this.daemon.interrupt();
            this.daemon.join();

            this.listener.closeSocket();

            this.listener.interrupt();
            this.listener.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.threadPool.shutdown();
    }

    public void addDaemon(Thread daemon) {
        this.daemon = daemon;
    }

    public void addListener(Listener listener) {
        this.listener = listener;
    }

    public void addThreadPool(ExecutorService tPool) {
        this.threadPool = tPool;
    }

    public void signalDaemon() {
        this.daemon.interrupt();
    }

    public synchronized void shutdownDaemon() {
        System.out.println("Shutting down daemon");
        this.daemonActive = false;
    }

    public synchronized void shutdownListener() {
        System.out.println("Shutting down listener");
        this.listenerActive = false;
    }

    public synchronized boolean daemonActive() {
        return this.daemonActive;
    }
    
    public synchronized boolean listenerActive() {
        return this.listenerActive;
    }
}