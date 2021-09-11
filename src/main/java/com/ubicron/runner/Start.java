package com.ubicron;

/** Copyright 2021 David Corbo
 *  Start definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class Start {

    private void shutdown(State state, Daemon daemon, Listener listener, ExecutorService tPool) {
        state.shutdownDaemon();
        state.shutdownListener();

        daemon.interrupt();
        daemon.join();

        listener.interrupt();
        listener.join();

        tPool.shutdown();
    }

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

        Queue<Instance> timedInst = new PriorityBlockingQueue<>(instances.size(),
            (o1, o2) -> (int) (o1.getTimeToRun() - o2.getTimeToRun()));

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
            // System.out.println(inst.getTimeToRun());
        }

        State state = new State();

        Thread daemon = new Thread(new Daemon(aliasToJob, timedInst, threadPool, state));
        daemon.start();
        state.

        Thread listener = new Thread(new Listener)

        Thread.sleep(30000);

        state.shutdown();



        // startup daemon, listen on port

        // System.out.println(table.print());
    }
}