package com.ubicron;

/** Copyright 2021 David Corbo
 *  Job definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

public class Job {

    private String alias;
    private List<String> environment;
    private List<String> commands;
    private Input input;
    private Output output;
    private SubCalls subCalls;

    public Job(String alias, List<String> environment, List<String> commands,
                Input input, Output output, SubCalls subCalls) {
        this.alias = alias;
        this.environment = environment;
        this.commands = commands;
        this.input = input;
        this.output = output;
        this.subCalls = subCalls;
    }

    public Output execute(Input input) {
        return null;
    }

}