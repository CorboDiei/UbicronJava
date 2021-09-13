package com.ubicron;

/** Copyright 2021 David Corbo
 *  Job definition
 *  Last edited: 9/11/21
 */

import java.util.*;
import java.lang.*;

public class Output {

    private JSONItem tree;
    private List<String> keys;
    private Map<String, Integer[]> keyToLine;
    
    private List<String> recKeys(JSONItem output) throws JSONAccessException {
        this.tree = output;
        List<String> ret = new ArrayList<>();
        if (output.type != JSONItem.Types.STRUCT) {
            ret.add("");
            return ret;
        }
        for (String item : output.itemOrder) {
            for (String path : recKeys(output.get(item))) {
                ret.add(item + "." + path);
            }
        }
        return ret;
    }

    public Output(JSONItem outputJSON) throws JSONAccessException {
        List<String> dottedKeys = recKeys(outputJSON);
        this.keys = new ArrayList<>();
        this.keyToLine = new HashMap<>();
        for (String key : dottedKeys) {
            String prunedKey = key.substring(0, key.length() - 1);
            this.keys.add(prunedKey);
            JSONItem line = outputJSON.get(prunedKey);
            if (line.type != JSONItem.Types.STRING)
                throw new JSONAccessException("output values must be strings");
            String[] linePosString = line.stringValue.split("\\.");
            if (linePosString.length != 2)
                throw new JSONAccessException("output values must have a command number and line number");
            Integer[] linePos = new Integer[2];
            try {
                linePos[0] = Integer.parseInt(linePosString[0]) - 1;
                linePos[1] = Integer.parseInt(linePosString[1]) - 1;
            } catch (NumberFormatException e) {
                throw new JSONAccessException("command and line specifiers must be integers");
            }
            this.keyToLine.put(prunedKey, linePos);
        }
    }

    public Input generateInput(List<List<String>> outputText) throws JobExecutionException {
        // System.out.println(outputText);
        try {
            JSONItem inputTree = new JSONItem("{}");
            for (String key : this.keys) {
                Integer[] lineVals = this.keyToLine.get(key);
                System.out.println(lineVals[0]);
                System.out.println(lineVals[1]);
                if (lineVals[0] < 0 || lineVals[0] >= outputText.size())
                    throw new JobExecutionException("command number outside of range");
                // System.out.println("l1");
                if (lineVals[1] < 0 || lineVals[1] >= outputText.get(lineVals[0]).size())
                    throw new JobExecutionException("line number outside of range");
                // System.out.println("l2");
                // JSONItem val = new JSONItem("\"" + outputText.get(lineVals[0]).get(lineVals[1]));
                // System.out.println(val.type);
                inputTree.insert(key, new JSONItem("\"" + outputText.get(lineVals[0]).get(lineVals[1]) + "\""));
                // System.out.println("l3");
            }
            return new Input(inputTree);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException("couldn't generate subsequent input");
        }
    }


    public List<String> getKeys() {
        return this.keys;
    }

    public String toString() {
        return this.tree.print();
    }
}
