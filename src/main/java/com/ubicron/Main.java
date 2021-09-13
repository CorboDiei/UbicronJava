package com.ubicron;

/** Copyright 2021 David Corbo
 *  Main entry point for ubicron commands
 *  Last edited: 9/12/21
 */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;

public class Main {

    private static boolean testDaemon(int port) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("END");
            boolean good = false;
            if (in.readLine().equals("EMPTY")) {
                good = true;
            }
            out.close();
            in.close();
            socket.close();
            return good;
        } catch (Exception e) {
            try {
                out.close();
                in.close();
                socket.close();
            } catch (Exception e1) {}
            return false;
        }
    }

    private static List<String> communicate(List<String> input, int port) {
        List<String> ret = new ArrayList<>();
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            for (String line : input) {
                out.println(line);
            }

            if (!input.get(0).equals("STOP")) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("END")) break;
                    ret.add(line);
                }
            }
            
            out.close();
            in.close();
            socket.close();
            
            return ret;
        } catch (Exception e) {
            try {
                out.close();
                in.close();
                socket.close();
            } catch (Exception e1) {}
            return null;
        }
    }

    private static void handleTest(int port) {
        if (!testDaemon(port)) {
            System.out.println("The ubicron daemon is not running. Run it with ubicron start");
            System.exit(0);
        }
    }


    private static void add(int type, int port) {
        handleTest(port);
        System.out.print("Enter your new " + ((type == 0) ? "job" : "instance"));
        System.out.println(", and press control-d when you are done:");
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String line;
            while ((line = systemIn.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            System.err.println("Failure");
            return;
        }
        
        JSONItem item = null;
        try {
            item = new JSONItem(builder.toString());
        } catch (JSONParseException e) {
            System.err.println("Error parsing: " + e.getMessage());
            return;
        }

        if (type == 0) {
            List<JSONItem> list = new ArrayList<>();
            list.add(item);
            try {
                ParseFiles.parseJobs(list).get(0);
            } catch (JSONAccessException e) {
                System.err.println("Error parsing: " + e.getMessage());
                return;
            }
        } else {
            List<JSONItem> list = new ArrayList<>();
            list.add(item);
            try {
                ParseFiles.parseInstances(list).get(0);
            } catch (JSONAccessException e) {
                System.err.println("Error parsing: " + e.getMessage());
                return;
            }
        }
        String json = builder.toString();
        List<String> commands = new ArrayList<>();
        commands.add("ADD");
        commands.add((type == 0) ? "JOB" : "INST");
        for (String jsonLine : json.trim().split("\n")) {
            commands.add(jsonLine);
        }
        commands.add("END");
        List<String> response = communicate(commands, port);
        if (response == null || response.size() == 0 || response.get(0).equals("FAILURE")) {
            System.err.println("Add operation failed");
        } else {
            System.out.println("Add operation successful");
        }
    }

    private static void remove(int type, String alias, int port) {
        handleTest(port);
        List<String> commands = new ArrayList<>();
        commands.add("REMOVE");
        commands.add((type == 0) ? "JOB" : "INST");
        commands.add(alias);
        commands.add("END");
        List<String> response = communicate(commands, port);
        if (response == null || response.size() == 0) {
            System.err.println("Remove operation failed");
        } else if (!response.get(0).equals("SUCCESS")) {
            System.err.println(response.get(0));
        } else {
            System.out.println("Successfully removed " + alias);
        }
    }


    private static void list(int type, int port) {
        handleTest(port);
        List<String> commands = new ArrayList<>();
        commands.add("LIST");
        commands.add((type == 0) ? "JOB" : "INST");
        commands.add("END");
        List<String> response = communicate(commands, port);
        if (response == null) {
            System.err.println("List operation failed");
        } else {
            System.out.print((type == 0) ? "Jobs: " : "Instances: ");
            for (int i = 0; i < response.size(); i++) {
                System.out.print("\"" + response.get(i) + "\"" + ((i == response.size() - 1) ? "\n" : ", "));
            }
        }
    }

    
    private static void view(int type, String alias, int port) {
        handleTest(port);
        List<String> commands = new ArrayList<>();
        commands.add("VIEW");
        commands.add((type == 0) ? "JOB" : "INST");
        commands.add(alias);
        commands.add("END");
        List<String> response = communicate(commands, port);
        if (response == null || response.size() == 0) {
            System.err.println("View operation failed");
        } else if (response.get(0).equals("FAILURE")) {
            System.err.println(response.get(1));
        } else {
            for (String line : response) {
                System.out.println(line);
            }
        }
    }

    private static void execute(String alias, int port) {
        handleTest(port);
        List<String> commands = new ArrayList<>();
        commands.add("EXECUTE");
        commands.add(alias);
        commands.add("END");
        List<String> response = communicate(commands, port);
        if (response == null || response.size() == 0) {
            System.err.println("View operation failed");
        } else if (response.get(0).equals("FAILURE")) {
            System.err.println(response.get(1));
        } else {
            System.out.println("Execution of \"" + alias + "\" succeeded");
        }
    }

    private static void save(int port) {
        handleTest(port);
        List<String> commands = new ArrayList<>();
        commands.add("SAVE");
        commands.add("END");
        List<String> response = communicate(commands, port);
        if (response == null || response.size() == 0 || response.get(0).equals("FAILURE")) {
            System.err.println("Save operation failed");
        } else  {
            System.out.println("Table saved");
        }
    }

    private static void start(int port) {
        if (testDaemon(port)) {
            System.out.println("The ubicron daemon is already running.");
            System.exit(0);
        }
        try {
            (new ProcessBuilder("/bin/bash", "-c", "ubicrond")).start();
            System.out.println("Started ubicron daemon");
        } catch (IOException e) {
            System.err.println("Couldn't start ubicron daemon");
        }
    }

    private static void stop(int port) {
        handleTest(port);
        List<String> input = new ArrayList<>();
        input.add("STOP");
        List<String> res = communicate(input, port);
        if (res == null) {
            System.err.println("Couldn't stop ubicron daemon");
        } else {
            System.out.println("Successfully stopped ubicron daemon");
        }
    }

    private static void usage() {
        System.out.println(String.join("\n",
            "Usage: ubicron [command] [arguments]",
            "where arguments are as follows: \n",
            "    add [resource]             you build and add a new resource",
            "    remove [resource] [alias]  removes the specified resource",
            "    list [resource]            lists all of the specified resource",
            "    view [resource] [alias]    shows the full definition of the specified resource",
            "    execute [alias]            executes the specified instance",
            "    save                       saves the daemon's state to the table file",
            "    start                      starts the daemon",
            "    stop                       stops the daemon\n",
            "resources include jobs and instances"));
        System.exit(0);
    }

    private static void resource() {
        System.out.println("The only valid resources are \"job\" and \"instance\"");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
        }

        Object[] cfg = ParseFiles.parseCfg();
        int port = (int) cfg[0];

        if (args[0].equals("add") || args[0].equals("a")) {
            if (args.length == 2) {
                if (args[1].equals("job") || args[1].equals("j")) {
                    add(0, port);
                } else if ((args[1].equals("instance") || args[1].equals("inst") || args[1].equals("i"))) {
                    add(1, port);
                } else {
                    resource();
                }
            } else {
                System.err.println("the add command takes 1 argument [resource]");
            }
        } else if (args[0].equals("remove") || args[0].equals("r")) {
            if (args.length == 3) {
                if (args[1].equals("job") || args[1].equals("j")) {
                    remove(0, args[2], port);
                } else if ((args[1].equals("instance") || args[1].equals("inst") || args[1].equals("i"))) {
                    remove(1, args[2], port);
                } else {
                    resource();
                }
            } else {
                System.err.println("the remove command takes 2 arguments [resource] [alias]");
            }
        } else if (args[0].equals("list") || args[0].equals("l")) {
            if (args.length == 2) {
                if (args[1].equals("job") || args[1].equals("j")) {
                    list(0, port);
                } else if ((args[1].equals("instance") || args[1].equals("inst") || args[1].equals("i"))) {
                    list(1, port);
                } else {
                    resource();
                }
            } else {
                System.err.println("the list command takes 1 argument [resource]");
            }
        } else if (args[0].equals("view") || args[0].equals("v")) {
            if (args.length == 3) {
                if (args[1].equals("job") || args[1].equals("j")) {
                    view(0, args[2], port);
                } else if ((args[1].equals("instance") || args[1].equals("inst") || args[1].equals("i"))) {
                    view(1, args[2], port);
                } else {
                    resource();
                }
            } else {
                System.err.println("the view command takes 2 arguments [resource] [alias]");
            }
        } else if (args[0].equals("execute") || args[0].equals("e")) {
            if (args.length == 2) {
                execute(args[1], port);
            } else {
                System.err.println("the execute command takes 1 argument [alias]");
            }
        } else if (args[0].equals("save")) {
            save(port);
        } else if (args[0].equals("start")) {
            start(port);
        } else if (args[0].equals("stop")) {
            stop(port);
        } else {
            usage();
        }
    }
}
