package com.ubicron;

/** Copyright 2021 David Corbo
 *  Daemon definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class Daemon implements Runnable {

    private volatile boolean running;

    private Map<String, Job> aliasToJob;
    private Queue<Instance> timedInst;
    private List<Instance> activeInst;
    private ExecutorService threadPool;
    private RunState state;

    public Daemon(Map<String, Job> jobMap, Queue<Instance> timedInst,
        List<Instance> activeInst, ExecutorService tPool, RunState state) {
        
        this.aliasToJob = jobMap;
        this.timedInst = timedInst;
        this.threadPool = tPool;
        this.state = state;
        this.activeInst = activeInst;
    }

    public void run() {
        System.out.println("Daemon running");
        while (state.daemonActive()) {
            if (!timedInst.isEmpty()) {
                Instance inst = timedInst.poll();
                if (!this.activeInst.contains(inst)) continue;
                if (inst.getTimeToRun() <= System.currentTimeMillis()) {
                    // execute job
                    JobExecutor jobEx = new JobExecutor(this.aliasToJob,
                        this.aliasToJob.get(inst.getJob()), inst.getInput(), this.threadPool);
                    threadPool.execute(jobEx);
                    if (inst.newTimeToRun()) this.timedInst.add(inst);
                } else {
                    // go to sleep
                    this.timedInst.add(inst);
                    try {
                        Thread.sleep(inst.getTimeToRun() - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        continue;
                    }
                }
            } else {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    continue;
                }
            }
        }
    }
}