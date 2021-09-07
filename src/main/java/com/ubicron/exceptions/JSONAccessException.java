package com.ubicron;

/** Copyright 2021 David Corbo
 *  Failure parsing JSON
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

public class JSONAccessException extends Exception {
    private String message;

    public JSONAccessException(String message) {
        this.message = message;
    }

    public String toString() {
        return "JSONAccess exception: " + this.message;
    }
}