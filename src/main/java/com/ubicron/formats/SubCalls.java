package com.ubicron;

/** Copyright 2021 David Corbo
 *  subCalls definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;

public class SubCalls {

    private Map<String, Input> jobToInput;
    private List<String> jobOrder;

    public SubCalls(JSONItem item) throws JSONAccessException {
        this.jobOrder = new ArrayList<>();
        this.jobToInput = new HashMap<>();
        for (String job : item.itemOrder) {
            this.jobOrder.add(job);
            this.jobToInput.put(job, new Input(item.structValue.get(job)));
        }
    }

    public Object[] getCalls() {
        return new Object[]{jobOrder, jobToInput};
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String key : this.jobOrder) {
            builder.append(key + ": " + this.jobToInput.get(key) + "\n");
        }
        return builder.toString();
    }
}