package com.ubicron;

/** Copyright 2021 David Corbo
 *  Job definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;
import java.text.*;
import org.quartz.*;

public class TimeInfo {

    private final long[] TIME_MULTS = new long[]{1000, 60000, 3600000, 86400000};

    private Instance.Types type;
    private CronExpression cronEx;
    private long[] inVals;

    public TimeInfo(String time, Instance.Types type) throws JSONParseException {
        this.type = type;
        try {
            if (type == Instance.Types.RECURRING) {
                this.cronEx = new CronExpression(time);
            } else if (type == Instance.Types.IN) {
                String[] items = time.trim().split("\\s+");
                if (items.length > 4)
                    throw new JSONParseException("timed instances can only specify seconds, minutes, hours, and days");
                this.inVals = new long[items.length];
                for (int i = 0; i < items.length; i++) {
                    try {
                        this.inVals[i] = Long.parseLong(items[i]);
                        if (this.inVals[i] < 0)
                            throw new JSONParseException("timed instance specifiers must be positive");
                    } catch (NumberFormatException e) {
                        throw new JSONParseException("timed instance specifiers can only be integers");
                    }
                }
            } else {
                throw new JSONParseException("time info is only created on recurring or timed instances");
            }
            
        } catch (ParseException e) {
            throw new JSONParseException("could not parse recurrence specifier");
        }
    }

    public long timeToRun() {
        if (this.type == Instance.Types.RECURRING) {
            Date currentTime = new Date();
            Date nextRunTime = this.cronEx.getNextValidTimeAfter(currentTime);
            return nextRunTime.getTime();
        } else if (this.type == Instance.Types.IN) {
            long total = 0l;
            for (int i = 0; i < this.inVals.length; i++) {
                total += this.inVals[i] * TIME_MULTS[i];
            }
            return total + System.currentTimeMillis();
        } else {
            return -1l;
        }
    }
}