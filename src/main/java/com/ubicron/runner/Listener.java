package com.ubicron;

/** Copyright 2021 David Corbo
 *  Listener definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class Listener {

    public static void main(String[] args) throws Exception {

        Object[] parserOutput = ParseTable.parseTable();

        List<Job> jobs = (List<Job>) parserOutput[0];
        List<Instance> instances = (List<Instance>) parserOutput[1];
        Map<String, Job> aliasToJob = new ConcurrentHashMap<>();
        Map<String, Instance> aliasToInstance = new ConcurrentHashMap<>();
        for (Job job : jobs) {
            aliasToJob.put(job.getAlias(), job);
        }
        for (Instance instance : instances) {
            aliasToInstance.put(instance.getAlias(), instance);
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(20);

        List<Instance> timedInst = new CopyOnWriteArrayList<>();

        for (Instance inst : instances) {
            switch (inst.getType()) {
                case INSTANT:
                    JobExecutor jobEx = new JobExecutor(aliasToJob, aliasToJob.get(inst.getJob()),
                            inst.getInput(), threadPool);
                    threadPool.execute(jobEx);
                    break;
                case IN:
                    timedInst.add(inst);
                    break;
                case RECURRING:
                    timedInst.add(inst);
                    break;
                case STANDBY:
                    break;
            }
        }

        Thread daemon = new Thread(new Daemon(aliasToJob, timedInst, threadPool));
        daemon.start();

        Thread.sleep(1000);
        daemon.join();
        threadPool.shutdown();



        // startup daemon, listen on port

        // System.out.println(table.print());
    }
}