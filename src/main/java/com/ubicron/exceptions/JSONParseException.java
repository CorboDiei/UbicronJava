package com.ubicron;

/** Copyright 2021 David Corbo
 *  Failure parsing JSON
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

public class JSONParseException extends Exception {
    private String message;

    public JSONParseException(String message) {
        this.message = message;
    }

    public String toString() {
        return "JSONParse exception: " + this.message;
    }
}