package com.ubicron;

/** Copyright 2021 David Corbo
 *  Instance and Time definitions
 *  Last edited: 9/4/21 
 */

import java.util.*;
import java.lang.*;

public class Instance {

    public enum Types {
        INSTANT,
        IN,
        RECURRING,
        STANDBY,
        NULL
    }

    private String alias;
    private String job;
    private Input input;
    private Types type;
    private TimeInfo timeInfo;
    private long timeToRun;
    

    public Instance(String alias, String job, Input input, Types type, TimeInfo info, long ttr) {
        this.alias = alias;
        this.job = job; 
        this.input = input;
        this.type = type;
        this.timeInfo = info;
        this.timeToRun = ttr;
        if (this.type == Types.RECURRING) {
            this.timeToRun = this.timeInfo.timeToRun();
        }

    }

    public long getTimeToRun() {
        return this.timeToRun;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getJob() {
        return this.job;
    }

    public Types getType() {
        return this.type;
    }

    public Input getInput() {
        return this.input;
    }

}