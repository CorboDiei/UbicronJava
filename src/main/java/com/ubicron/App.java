package com.ubicron;

import java.util.*;
import java.lang.*;

/**
 * Hello world!
 *
 */
public class App {

    private static void usage() {
        System.out.println(String.join("\n",
            "Usage: ubicron",
            "        (to startup ubicron)",
            "    or  ubicron [action] [resource]",
            "        (to perform an action on the running ubicron instance)",
            "",
            "Actions:",
            "-l, --list                  To do some stuff"
        ));
    
    }

    private static void listJobs() {

    }

    private static void listInvocations() {

    }

    // private static 

    public static void main(String[] args) {
        usage();
    }
}
