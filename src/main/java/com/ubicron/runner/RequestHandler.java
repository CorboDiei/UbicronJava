package com.ubicron;

/** Copyright 2021 David Corbo
 *  RequestHandler definition
 *  Last edited: 9/12/21
 */

import java.util.*;
import java.lang.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

public class RequestHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private List<Job> jobOrder;
    private List<Instance> instOrder;
    private Map<String, Job> jobMap;
    private Queue<Instance> timedInst;
    private Map<String, Instance> instMap;
    private ExecutorService threadPool;
    private RunState state;

    public RequestHandler(Socket socket, List<Job> jobOrder, List<Instance> instOrder, 
        Map<String, Job> jobMap, Queue<Instance> timedInst,
        Map<String, Instance> instMap, ExecutorService tPool, RunState state) {
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {}
        
        this.jobOrder = jobOrder;
        this.instOrder = instOrder;
        this.jobMap = jobMap;
        this.timedInst = timedInst;
        this.instMap = instMap;
        this.threadPool = tPool;
        this.state = state;
    }

    private boolean add(int type, String json) {
        try {
            JSONItem item = new JSONItem(json);
            List<JSONItem> list = new ArrayList<>();
            list.add(item);
            if (type == 0) {
                Job job = ParseFiles.parseJobs(list).get(0);
                this.jobMap.put(job.getAlias(), job);
                this.jobOrder.add(job);
            } else {
                Instance inst = ParseFiles.parseInstances(list).get(0);
                switch (inst.getType()) {
                    case INSTANT:
                        this.instMap.put(inst.getAlias(), inst);
                        this.threadPool.execute(new JobExecutor(jobMap, jobMap.get(inst.getJob()),
                                inst.getInput(), threadPool));
                        break;
                    case IN:
                        this.timedInst.add(inst);
                        this.instMap.put(inst.getAlias(), inst);
                        this.state.signalDaemon();
                        break;
                    case RECURRING:
                        this.timedInst.add(inst);
                        this.instMap.put(inst.getAlias(), inst);
                        this.state.signalDaemon();
                        break;
                    case STANDBY:
                        this.instMap.put(inst.getAlias(), inst);
                        break;
                }
                this.instOrder.add(inst);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean remove(int type, String alias) {
        boolean response = false;
        if (type == 0) {
            if (this.jobMap.containsKey(alias)) {
                response = true;
                Job rem = this.jobMap.remove(alias);
                this.jobOrder.remove(rem);
            }
        } else {
            if (this.instMap.containsKey(alias)) {
                response = true;
                Instance rem = this.instMap.remove(alias);
                this.instOrder.remove(rem);
            }
        }
        return response;
    }

    private List<String> list(int type) {
        List<String> response = new ArrayList<>();
        if (type == 0) {
            for (String key : jobMap.keySet()) response.add(key);
        } else {
            for (String key : instMap.keySet()) response.add(key);
        }
        return response;
    }

    private List<String> view(int type, String alias) {
        List<String> response = new ArrayList<>();
        if (type == 0) {
            if (!this.jobMap.containsKey(alias)) {
                response.add("FAILURE");
                response.add("there is no job with alias " + alias);
            } else {
                response = new ArrayList<>(Arrays.asList(
                    this.jobMap.get(alias).toString().trim().split("\n")));
            }
        } else {
            if (!this.instMap.containsKey(alias)) {
                response.add("FAILURE");
                response.add("there is no instance with alias " + alias);
            } else {
                response = new ArrayList<>(Arrays.asList(
                    this.instMap.get(alias).toString().trim().split("\n")));
            }
        }
        return response;
    }

    private boolean execute(String instance) {
        if (!this.instMap.containsKey(instance)) return false;
        Instance inst = this.instMap.get(instance);
        this.threadPool.execute(new JobExecutor(this.jobMap, this.jobMap.get(inst.getJob()),
            inst.getInput(), this.threadPool));
        return true;
    }

    private boolean save() {
        return ParseFiles.saveTable(this.jobOrder, this.instOrder);
    }

    private List<String> parseMessage(List<String> message) {
        List<String> response = new ArrayList<>();
        if (message.size() > 0) {
            if (message.get(0).equals("ADD")) {
                if (message.size() > 2) {
                    int type = -1;
                    if (message.get(1).equals("JOB")) {
                        type = 0;
                    } else if (message.get(1).equals("INST")) {
                        type = 1;
                    } 
                    if (type == -1) {
                        response.add("INVALID");
                    } else {
                        StringBuilder json = new StringBuilder();
                        for (int i = 2; i < message.size(); i++) {
                            json.append(message.get(i));
                        }
                        if (add(type, json.toString())) {
                            response.add("SUCCESS");
                        } else {
                            response.add("FAILURE");
                        }
                    }
                } else {
                    response.add("INVALID");
                }
            } else if (message.get(0).equals("REMOVE")) {
                if (message.size() == 3) {
                    if (message.get(1).equals("JOB")) {
                        if (remove(0, message.get(2))) {
                            response.add("SUCCESS");
                        } else {
                            response.add("could not find job alias " + message.get(2));
                        }
                    } else if (message.get(1).equals("INST")) {
                        if (remove(1, message.get(2))) {
                            response.add("SUCCESS");
                        } else {
                            response.add("could not find instance alias " + message.get(2));
                        }
                    }
                } else {
                    response.add("INVALID");
                }
            } else if (message.get(0).equals("LIST")) {
                if (message.size() == 2) {
                    if (message.get(1).equals("JOB")) {
                        response = list(0);
                    } else if (message.get(1).equals("INST")) {
                        response = list(1);
                    }
                } else {
                    response.add("INVALID");
                }
            } else if (message.get(0).equals("VIEW")) {
                if (message.size() == 3) {
                    if (message.get(1).equals("JOB")) {
                        response = view(0, message.get(2));
                    } else if (message.get(1).equals("INST")) {
                        response = view(1, message.get(2));
                    }
                } else {
                    response.add("INVALID");
                }
            } else if (message.get(0).equals("EXECUTE")) {
                if (message.size() == 2) {
                    if (execute(message.get(1))) {
                        response.add("SUCCESS");
                    } else {
                        response.add("FAILURE");
                        response.add("could not find alias " + message.get(1));
                    }
                } else {
                    response.add("INVALID");
                }
            } else if (message.get(0).equals("SAVE")) {
                if (save()) {
                    response.add("SUCCESS");
                } else {
                    response.add("FAILURE");
                }
            } else {
                response.add("INVALID");
            }
        } else {
            response.add("EMPTY");
        }
        response.add("END");
        return response;
    
    }

    public void run() {
        try {
            List<String> message = new ArrayList<>();
            boolean r = true;
            String line;
            while ((line = this.in.readLine()) != null) {
                if (line.equals("END")) break;
                if (line.equals("STOP")) {
                    this.state.shutdown();
                    r = false;
                    break;
                }
                message.add(line);
            }

            if (r) {
                List<String> response = parseMessage(message);

                for (String responseLine : response) {
                    this.out.println(responseLine);
                }
            }
            

            this.out.close();
            this.in.close();
            this.socket.close();
            
        } catch (IOException e) {}
        
    }

}