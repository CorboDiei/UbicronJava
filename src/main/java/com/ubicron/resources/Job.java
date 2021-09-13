package com.ubicron;

/** Copyright 2021 David Corbo
 *  Job definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;
import java.io.*;

public class Job {

    private String alias;
    private Map<String, String> environment;
    private List<String> commands;
    private Input input;
    private Output output;
    private SubCalls subCalls;
    private JSONItem tree;

    public Job(String alias, Map<String, String> environment, List<String> commands,
                Input input, Output output, SubCalls subCalls, JSONItem tree) {
        this.alias = alias;
        this.environment = environment;
        this.commands = commands;
        this.input = input;
        this.output = output;
        this.subCalls = subCalls;
        this.tree = tree;
    }

    public String getAlias() {
        return this.alias;
    }

    public Input getInput() {
        return this.input;
    }

    public SubCalls getSubCalls() {
        return this.subCalls;
    }

    public JSONItem getTree() {
        return this.tree;
    }

    public Input execute(Input input) {
        try {
            // merge inputs
            Input commandInput = this.input.merge(input);   

            List<List<String>> outputs = new ArrayList<>();
            for (String command : this.commands) {
                if (command.trim().length() == 0) outputs.add(new ArrayList<>());
                StringBuilder argBuilder = new StringBuilder();
                for (String arg : command.trim().split("\\s+")) {
                    if (arg.charAt(0) == '@') {
                        argBuilder.append(commandInput.getValue(arg.substring(1)) + " ");
                    } else {
                        argBuilder.append(arg + " ");
                    }
                }
                String args = argBuilder.toString().trim();
                ProcessBuilder pBuilder = new ProcessBuilder("/bin/bash", "-c", args);
                pBuilder.environment().putAll(this.environment);
                Process process = pBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                List<String> currentOutput = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    currentOutput.add(line);
                }
                outputs.add(currentOutput);
            }
            return this.output.generateInput(outputs);
        } catch (Exception e) {
            // e.printStackTrace();
            Log.log("[FAILURE] failed to run instance of " + alias);
            return null;
        }
    }

    public String toString() {
        return this.tree.print();
    }
}