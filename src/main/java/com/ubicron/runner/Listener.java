package com.ubicron;

/** Copyright 2021 David Corbo
 *  Listener definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

public class Listener extends Thread {

    private ServerSocket socket;

    private List<Job> jobOrder;
    private List<Instance> instOrder;
    private Map<String, Job> jobMap;
    private Queue<Instance> timedInst;
    private Map<String, Instance> instMap;
    private ExecutorService threadPool;
    private RunState state;

    public Listener(int port, List<Job> jobOrder, List<Instance> instOrder, 
        Map<String, Job> jobMap, Queue<Instance> timedInst,
        Map<String, Instance> instMap, ExecutorService tPool, RunState state) throws IOException {
        this.socket = new ServerSocket(port);
        this.jobOrder = jobOrder;
        this.instOrder = instOrder;
        this.jobMap = jobMap;
        this.timedInst = timedInst;
        this.instMap = instMap;
        this.threadPool = tPool;
        this.state = state;
    }

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {}
        
    }

    public void run() {
        while (this.state.listenerActive()) {
            try {
                Socket newCon = this.socket.accept();
                this.threadPool.execute(new RequestHandler(newCon, this.jobOrder, this.instOrder, 
                    this.jobMap, this.timedInst, this.instMap, this.threadPool, this.state));
            } catch (IOException e) {
                continue;
            }
        }
        if (!this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException e) {}
        }
        
    }
}   