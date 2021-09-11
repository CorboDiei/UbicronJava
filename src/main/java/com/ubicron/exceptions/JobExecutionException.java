package com.ubicron;

/** Copyright 2021 David Corbo
 *  Failure running job
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

public class JobExecutionException extends Exception {

    private String message;

    public JobExecutionException(String message) {
        this.message = message;
    }

    public String toString() {
        return "JobExecutionException: " + this.message;
    }
}