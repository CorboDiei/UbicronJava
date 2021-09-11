package com.ubicron;

/** Copyright 2021 David Corbo
 *  Daemon definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class Daemon implements Runnable {

    private Map<String, Job> aliasToJob;
    private List<Instance> timedInst;
    private ExecutorService threadPool;

    public Daemon(Map<String, Job> jobMap, List<Instance> timedInst, ExecutorService tPool) {
        this.aliasToJob = jobMap;
        this.timedInst = timedInst;
        this.threadPool = tPool;
    }

    public void run() {
        // TODO: 6
        System.out.println("Daemon running");
    }
}