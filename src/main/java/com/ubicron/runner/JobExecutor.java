package com.ubicron;

/** Copyright 2021 David Corbo
 *  Executor definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class JobExecutor implements Runnable {
    private Map<String, Job> aliasToJob;
    private Job job;
    private Input input;
    private ExecutorService threadPool;

    public JobExecutor(Map<String, Job> jobMap, Job job, Input input,
            ExecutorService tPool) {
        this.aliasToJob = jobMap;
        this.job = job;
        this.input = input;
        this.threadPool = tPool;
    }

    public void run() {
        try {
            Input genInput = job.execute(this.input);
            System.out.println(genInput);
            if (genInput == null) return;
            Object[] fromCalls = job.getSubCalls().getCalls();
            List<String> jobOrder = (List<String>) fromCalls[0];
            Map<String, Input> jobToInput = (Map<String, Input>) fromCalls[1];

            for (String calledJob : jobOrder) {
                Input subInput = jobToInput.get(calledJob).fillSubCall(genInput);
                System.out.println(subInput);
                JobExecutor jobEx = new JobExecutor(this.aliasToJob,
                    this.aliasToJob.get(calledJob), subInput, this.threadPool);
                this.threadPool.execute(jobEx);
            }
            Log.log("[SUCCESS] successfully ran job " + this.job.getAlias());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}