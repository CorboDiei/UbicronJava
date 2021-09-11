package com.ubicron;

/** Copyright 2021 David Corbo
 *  Module for parsing ubicrontab
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

public class ParseTable {

    private static List<Job> parseJobs(List<JSONItem> jobItems) throws JSONAccessException {
        List<Job> jobList = new CopyOnWriteArrayList<>();
        for (JSONItem item : jobItems) {
            if (item.type != JSONItem.Types.STRUCT)
                throw new JSONAccessException("job is not of type object");

            if (item.get("alias").type != JSONItem.Types.STRING)
                throw new JSONAccessException("alias value is not of type string");
            String alias = item.get("alias").stringValue;

            if (item.get("env").type != JSONItem.Types.STRUCT)
                throw new JSONAccessException("env value is not of type object");
            Map<String, String> env = new HashMap<>();
            Map<String, JSONItem> envMap = item.get("env").structValue;
            for (String key : envMap.keySet()) {
                if (envMap.get(key).type != JSONItem.Types.STRING)
                    throw new JSONAccessException("env value is not of type string");
                env.put(key, envMap.get(key).stringValue);
            }

            if (item.get("commands").type != JSONItem.Types.ARRAY)
                throw new JSONAccessException("commands value is not of type array");
            List<String> commands = new ArrayList<>();
            for (JSONItem com : item.get("commands").arrayValue) {
                if (com.type != JSONItem.Types.STRING)
                    throw new JSONAccessException("command value is not of type string");
                commands.add(com.stringValue);
            }

            if (item.get("input").type != JSONItem.Types.STRUCT)
                throw new JSONAccessException("input value is not of type object");
            Input input = new Input(item.get("input"));

            if (item.get("output").type != JSONItem.Types.STRUCT)
                throw new JSONAccessException("output value is not of type object");
            Output output = new Output(item.get("output"));

            if (item.get("subcalls").type != JSONItem.Types.STRUCT)
                throw new JSONAccessException("subcalls value is not of type object");
            SubCalls subcalls = new SubCalls(item.get("subcalls"));

            jobList.add(new Job(alias, env, commands, input, output, subcalls));
        }
        return jobList;
    }

    private static List<Instance> parseInstances(List<JSONItem> instanceItems) throws JSONAccessException {
        List<Instance> instanceList = new CopyOnWriteArrayList<>();
        for (JSONItem item : instanceItems) {
            if (item.get("alias").type != JSONItem.Types.STRING)
                throw new JSONAccessException("alias value is not of type string");
            String alias = item.get("alias").stringValue;
        
            if (item.get("job").type != JSONItem.Types.STRING)
                    throw new JSONAccessException("job value is not of type string");
                String job = item.get("job").stringValue;

            if (item.get("input").type != JSONItem.Types.STRUCT)
                    throw new JSONAccessException("input value is not of type object");
                Input input = new Input(item.get("input"));
            
            if (item.get("type").type != JSONItem.Types.INT)
                throw new JSONAccessException("type value is not of type int");
            Instance.Types type = Instance.Types.NULL;
            TimeInfo info = null;
            long ttr = -1l;
            switch (item.get("type").intValue) {
                case 0:
                    type = Instance.Types.INSTANT;
                    break;
                case 1:
                    type = Instance.Types.IN;
                    if (item.get("in").type != JSONItem.Types.INT)
                        throw new JSONAccessException("in value is not of type int");
                    ttr = item.get("in").intValue;
                    break;
                case 2:
                    type = Instance.Types.RECURRING;
                    if (item.get("time").type != JSONItem.Types.STRING)
                        throw new JSONAccessException("time value is not of type string");
                    try {
                        info = new TimeInfo(item.get("time").stringValue);
                    } catch (JSONParseException e) {
                        throw new JSONAccessException("couldn't parse time info");
                    }
                    break;
                case 3:
                    type = Instance.Types.STANDBY;
                    break;
                default:
                    throw new JSONAccessException("invalid type value");
            }
            instanceList.add(new Instance(alias, job, input, type, info, ttr));
        }
        return instanceList;
    }

    public static Object[] parseTable() throws Exception {
        // get table text
        String tableText = null;
        try {
            // table file should be at /var/lib/ubicron/.ubicrontab
            // create table file if it doesn't exist
            Path ubiLib = Paths.get("/var/lib/ubicron");
            Files.createDirectories(ubiLib);
            File tabFile = new File("/var/lib/ubicron/.ubicrontab");
            tabFile.createNewFile();
            Path tabPath = Paths.get("/var/lib/ubicron/.ubicrontab");
            byte[] encoded = Files.readAllBytes(tabPath);
            tableText = new String(encoded, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // parse table file
        List<Job> jobs = new ArrayList<>();
        List<Instance> instances = new ArrayList<>();
        if (tableText.trim().length() != 0) {
            try {
                JSONItem table = new JSONItem(tableText);
                JSONItem jobList = table.get("jobs");
                if (jobList.type != JSONItem.Types.ARRAY) 
                    throw new JSONParseException("jobs json item is not array");
                List<JSONItem> jobItems = jobList.arrayValue;
                jobs = parseJobs(jobItems);
                JSONItem instanceList = table.get("instances");
                if (instanceList.type != JSONItem.Types.ARRAY) 
                    throw new JSONParseException("instances json item is not array");
                List<JSONItem> instanceItems = instanceList.arrayValue;
                instances = parseInstances(instanceItems);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return new Object[]{jobs, instances};
    }

}