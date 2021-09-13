package com.ubicron;

/** Copyright 2021 David Corbo
 *  Start definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class Start {

    public static void main(String[] args) throws Exception {

        Object[] tableParserOutput = ParseFiles.parseTable();
        Object[] cfgParserOutput = ParseFiles.parseCfg();

        int portNum = (int) cfgParserOutput[0];

        List<Job> jobs = (List<Job>) tableParserOutput[0];
        List<Instance> instances = (List<Instance>) tableParserOutput[1];
        Map<String, Job> aliasToJob = new ConcurrentHashMap<>();
        Map<String, Instance> aliasToInstance = new ConcurrentHashMap<>();
        for (Job job : jobs) {
            aliasToJob.put(job.getAlias(), job);
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(20);

        Queue<Instance> timedInst = new PriorityBlockingQueue<>(instances.size() + 1,
            (o1, o2) -> (int) (o1.getTimeToRun() - o2.getTimeToRun()));

        for (Instance inst : instances) {
            switch (inst.getType()) {
                case INSTANT:
                    aliasToInstance.put(inst.getAlias(), inst);
                    threadPool.execute(new JobExecutor(aliasToJob, aliasToJob.get(inst.getJob()),
                            inst.getInput(), threadPool));
                    break;
                case IN:
                    timedInst.add(inst);
                    aliasToInstance.put(inst.getAlias(), inst);
                    break;
                case RECURRING:
                    timedInst.add(inst);
                    aliasToInstance.put(inst.getAlias(), inst);
                    break;
                case STANDBY:
                    aliasToInstance.put(inst.getAlias(), inst);
                    break;
            }
        }

        RunState state = new RunState();
        state.addThreadPool(threadPool);

        Thread daemon = new Thread(new Daemon(aliasToJob, timedInst, instances, threadPool, state));
        daemon.start();
        state.addDaemon(daemon);

        Listener listener = new Listener(portNum, jobs, instances, aliasToJob, timedInst,
            aliasToInstance, threadPool, state);
        listener.start();
        state.addListener(listener);
    }
}