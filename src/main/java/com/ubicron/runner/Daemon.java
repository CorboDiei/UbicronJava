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
    private Queue<Instance> timedInst;
    private ExecutorService threadPool;
    private State state;

    public Daemon(Map<String, Job> jobMap, Queue<Instance> timedInst,
        ExecutorService tPool, State state) {
        this.aliasToJob = jobMap;
        this.timedInst = timedInst;
        this.threadPool = tPool;
        this.state = state;
    }

    public void run() {
        System.out.println("Daemon running");
        while (state.daemonActive()) {
            if (!timedInst.isEmpty()) {
                Instance inst = timedInst.poll();
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
                        continue;
                    }
                    
                }
            }
            
        }

    }
}