package com.ubicron;

/** Copyright 2021 David Corbo
 *  Main entry point for ubicron commands
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

import org.apache.commons.cli.*;


public class Main {

    private static DaemonConnection accessDaemon() {
        return null;
    }

    private static void startDaemon() {
        System.out.println("Starting daemon");
    }

    private static void listJobs(boolean remote) {
        System.out.println("Printing jobs");
    }

    private static void listInstances(boolean remote) {
        System.out.println("Printing instances");
    }

    private static void addJob(boolean remote) {
        System.out.println(Job.create());
    }

    private static void addInstance(boolean remote) {
        System.out.println("Adding instance");
    }

    private static void deleteJob(boolean remote, String jobName) {

    }

    private static void deleteInstance(boolean remote, String instanceName) {

    }

    private static void printLog() {
        System.out.println("Printing log");
    }

    public static void main(String[] args) throws Exception {
        Set<String> resources = new HashSet<>();
        String JOB_RESOURCE = "job";
        String INST_RESOURCE = "instance";
        resources.add(JOB_RESOURCE);
        resources.add(INST_RESOURCE);

        Options options = new Options();

        Option list = new Option("l", "list", true, "list resources");
        options.addOption(list);

        Option add = new Option("a", "add", true, "add resource");
        options.addOption(add);

        Option delete = new Option("d", "delete", true, "delete resource");
        options.addOption(delete);

        Option remote = new Option("r", "remote", false, "access remote resources");
        options.addOption(remote);

        Option help = new Option("h", "help", false, "show help");
        options.addOption(help);

        Option log = new Option("log", false, "show log");
        options.addOption(log);

        Option start = new Option("s", "start", false, "startup ubicron daemon");
        options.addOption(start);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage() + "\n");
            formatter.printHelp("command line parameters", options);

            System.exit(1);
        }

        String listParam = cmd.getOptionValue("l");
        String addParam = cmd.getOptionValue("a");
        String delParam = cmd.getOptionValue("d");

        boolean r = cmd.hasOption("r");

        if (cmd.hasOption("h") || args.length == 0) {
            formatter.printHelp("command line parameters", options);
            
            System.exit(0);
        }

        if (cmd.hasOption("log")) {
            printLog();
        }

        if (r && !cmd.hasOption("l") && !cmd.hasOption("a")) {
            System.err.println("option remote must be used with a resource access like list or add\n");
            formatter.printHelp("command line parameters", options);

            System.exit(1);
        }

        if ((listParam != null && !resources.contains(listParam)) ||
            (addParam != null && !resources.contains(addParam))) {
            System.err.println("trying to access invalid resource "
                            + ((listParam == null) ? addParam : listParam));
            System.err.println("valid resources are job and instance\n");
            formatter.printHelp("command line parameters", options);

            System.exit(1);
        }

        if (cmd.hasOption("l")) {
            if (listParam.equals(JOB_RESOURCE)) {
                listJobs(r);
            } else {
                listInstances(r);
            }
        }

        if (cmd.hasOption("a")) {
            if (addParam.equals(JOB_RESOURCE)) {
                addJob(r);
            } else {
                addInstance(r);
            }
        }

        if (cmd.hasOption("d")) {
            if (addParam.equals(JOB_RESOURCE)) {
                deleteJob(r, delParam);
            } else {
                deleteInstance(r, delParam);
            }
        }

    }
}
