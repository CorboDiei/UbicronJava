package com.ubicron;

/** Copyright 2021 David Corbo
 *  Instance and Time definitions
 *  Last edited: 9/4/21 
 */

import java.util.*;
import java.lang.*;

public class Instance {

    class TimeInfo {
        public TimeInfo() {

        }
    }

    private String alias;
    private TimeInfo timeInfo;
    private long timeToRun;
    private Input input;

    public Instance(String alias, TimeInfo timeInfo, long timeToRun, Input input) {
        this.alias = alias;
        this.timeInfo = timeInfo;
        this.timeToRun = timeToRun;
        this.input = input;
    }

    public static Instance create() {
        return new Instance("", null, 0l, new Input());
    }

}